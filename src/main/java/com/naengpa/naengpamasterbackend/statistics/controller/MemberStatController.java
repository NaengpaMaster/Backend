package com.naengpa.naengpamasterbackend.statistics.controller;

import com.naengpa.naengpamasterbackend.global.response.ApiResponse;
import com.naengpa.naengpamasterbackend.statistics.dto.response.ExpiredProductCategoryResponse;
import com.naengpa.naengpamasterbackend.statistics.dto.response.ExpiredRecordResponse;
import com.naengpa.naengpamasterbackend.statistics.dto.response.TopIngredientResponse;
import com.naengpa.naengpamasterbackend.statistics.service.MemberStatService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Tag(name = "통계-회원", description = "회원 통계 조회 및 점수 분석 API")
@RestController
@RequestMapping("/api/v1/member-stats")
@RequiredArgsConstructor
public class MemberStatController {

    private final MemberStatService memberStatService;

    //가장 많이 만료된 재료 TOP 5
    @Operation(
            summary = "가장 많이 만료된 재료 TOP 5 조회",
            description = "지정한 기간 동안 사용자의 만료 재료 중 가장 많이 발생한 재료 TOP 5를 조회합니다."
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "TOP 5 조회 성공"
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "401",
                    description = "인증되지 않은 사용자"
            )
    })
    @GetMapping("/top-ingredients")
    public ResponseEntity<ApiResponse<List<TopIngredientResponse>>>
    getTop5Ingredients(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestParam(defaultValue = "7") int days
    ) {
        List<TopIngredientResponse> data = memberStatService.getTop5Ingredients(userDetails.getUsername(), days);
        return ResponseEntity.ok(ApiResponse.success("가장 많이 만료된 재료 TOP5 조회에 성공 했습니다.", data));
    }

    //카테고리별 만료량
    @Operation(
            summary = "카테고리별 만료량 조회",
            description = "지정한 기간 동안 사용자의 만료 재료를 카테고리별로 집계하여 조회합니다."
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "카테고리별 만료량 조회 성공"
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "401",
                    description = "인증되지 않은 사용자"
            )
    })
    @GetMapping("/expired-categories")
    public ResponseEntity<ApiResponse<List<ExpiredProductCategoryResponse>>> getExpiredProductCategories(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestParam(defaultValue = "7") int days
    ) {
        List<ExpiredProductCategoryResponse> data = memberStatService.getExpiredProductCategories(userDetails.getUsername(), days);
        return ResponseEntity.ok(ApiResponse.success("카테고리별 만료량 조회에 성공 했습니다.", data));
    }

    //최근 만료 기록
    @Operation(
            summary = "최근 만료 기록 조회",
            description = "지정한 기간 동안 사용자의 최근 식재료 만료 내역을 조회합니다."
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "최근 만료 기록 조회 성공"
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "401",
                    description = "인증되지 않은 사용자"
            )
    })
    @GetMapping("/expired-records")
    public ResponseEntity<ApiResponse<List<ExpiredRecordResponse>>> getExpiredRecords(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestParam(defaultValue = "7") int days
    ) {
        List<ExpiredRecordResponse> data = memberStatService.getExpiredRecords(userDetails.getUsername(), days);
        return ResponseEntity.ok(ApiResponse.success("재료 만료 내역 조회에 성공 했습니다.", data));
    }

}
