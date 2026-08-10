package com.naengpa.naengpamasterbackend.admin.controller;

import com.naengpa.naengpamasterbackend.admin.dto.response.AdminMemberStatusHistoryResponse;
import com.naengpa.naengpamasterbackend.admin.service.AdminMemberService;
import com.naengpa.naengpamasterbackend.admin.statistics.StatisticsPeriod;
import com.naengpa.naengpamasterbackend.global.response.ApiResponse;
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
public class AdminMemberStatusHistoryController {

    private final AdminMemberService adminMemberService;

    @GetMapping
    public ResponseEntity<ApiResponse<Page<AdminMemberStatusHistoryResponse>>> getMemberStatusHistories(
            @RequestParam LocalDate startDate,
            @RequestParam LocalDate endDate,
            @PageableDefault(size = 10) Pageable pageable
    ) {
        StatisticsPeriod period = StatisticsPeriod.of(startDate, endDate);

        return ResponseEntity.ok(ApiResponse.success(
                adminMemberService.getMemberStatusHistories(period, pageable)
        ));
    }
}
