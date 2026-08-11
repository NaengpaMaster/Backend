package com.naengpa.naengpamasterbackend.admin.controller;

import com.naengpa.naengpamasterbackend.admin.dto.response.AdminMemberStatusHistoryResponse;
import com.naengpa.naengpamasterbackend.admin.service.AdminMemberService;
import com.naengpa.naengpamasterbackend.admin.statistics.StatisticsPeriod;
import com.naengpa.naengpamasterbackend.global.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;

@RestController
@RequestMapping("/api/v1/admin/member-status-histories")
@RequiredArgsConstructor
@Tag(name = "관리자 회원 상태 이력", description = "관리자 회원 상태 변경 이력 조회 API")
public class AdminMemberStatusHistoryController {

    private final AdminMemberService adminMemberService;

    @Operation(summary = "회원 상태 변경 이력 조회", description = "관리자가 지정한 기간의 회원 활성/비활성 상태 변경 이력을 페이지로 조회합니다.")
    @GetMapping
    public ResponseEntity<ApiResponse<Page<AdminMemberStatusHistoryResponse>>> getMemberStatusHistories(
            @Parameter(description = "조회 시작일") @RequestParam LocalDate startDate,
            @Parameter(description = "조회 종료일") @RequestParam LocalDate endDate,
            @PageableDefault(size = 10) Pageable pageable
    ) {
        StatisticsPeriod period = StatisticsPeriod.of(startDate, endDate);

        return ResponseEntity.ok(ApiResponse.success(
                adminMemberService.getMemberStatusHistories(period, pageable)
        ));
    }
}
