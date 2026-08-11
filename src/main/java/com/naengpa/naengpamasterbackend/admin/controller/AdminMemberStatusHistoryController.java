package com.naengpa.naengpamasterbackend.admin.controller;

import com.naengpa.naengpamasterbackend.admin.dto.response.AdminMemberStatusHistoryResponse;
import com.naengpa.naengpamasterbackend.admin.service.AdminMemberService;
import com.naengpa.naengpamasterbackend.admin.statistics.StatisticsPeriod;
import com.naengpa.naengpamasterbackend.global.response.ApiResponse;
import lombok.RequiredArgsConstructor;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;

@Tag(name = "관리자 회원 상태 이력", description = "회원 상태 변경 이력 조회 API")
@SecurityRequirement(name = "bearerAuth")
@RestController
@RequestMapping("/api/v1/admin/member-status-histories")
@RequiredArgsConstructor
public class AdminMemberStatusHistoryController {

    private final AdminMemberService adminMemberService;

    @Operation(summary = "회원 상태 변경 이력 조회", description = "선택 기간의 회원 상태 변경 이력을 페이지 단위로 조회합니다. 시작일은 종료일보다 늦을 수 없습니다.")
    @GetMapping
    public ResponseEntity<ApiResponse<Page<AdminMemberStatusHistoryResponse>>> getMemberStatusHistories(
            @Parameter(description = "조회 시작일", example = "2026-08-01") @RequestParam LocalDate startDate,
            @Parameter(description = "조회 종료일", example = "2026-08-31") @RequestParam LocalDate endDate,
            @PageableDefault(size = 10) Pageable pageable
    ) {
        StatisticsPeriod period = StatisticsPeriod.of(startDate, endDate);

        return ResponseEntity.ok(ApiResponse.success(
                adminMemberService.getMemberStatusHistories(period, pageable)
        ));
    }
}
