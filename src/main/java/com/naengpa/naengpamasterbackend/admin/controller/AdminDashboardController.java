package com.naengpa.naengpamasterbackend.admin.controller;

import com.naengpa.naengpamasterbackend.admin.dto.response.AdminDashboardResponse;
import com.naengpa.naengpamasterbackend.admin.service.AdminDashboardService;
import com.naengpa.naengpamasterbackend.global.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/admin/dashboard")
@RequiredArgsConstructor
@Tag(name = "관리자 대시보드", description = "관리자 대시보드 통계 요약 API")
public class AdminDashboardController {

    private final AdminDashboardService adminDashboardService;

    @Operation(summary = "관리자 대시보드 조회", description = "관리자 화면에서 사용할 회원/문의/레시피 등 운영 대시보드 요약 정보를 조회합니다.")
    @GetMapping
    public ResponseEntity<ApiResponse<AdminDashboardResponse>> getDashboard() {
        return ResponseEntity.ok(ApiResponse.success(adminDashboardService.getDashboard()));
    }


}
