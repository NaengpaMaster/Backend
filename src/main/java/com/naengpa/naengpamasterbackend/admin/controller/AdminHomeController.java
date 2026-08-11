package com.naengpa.naengpamasterbackend.admin.controller;

import com.naengpa.naengpamasterbackend.admin.dto.response.AdminHomeResponse;
import com.naengpa.naengpamasterbackend.admin.service.AdminHomeService;
import com.naengpa.naengpamasterbackend.global.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/admin/home")
@RequiredArgsConstructor
@Tag(name = "관리자 홈", description = "관리자 홈 운영 요약 API")
public class AdminHomeController {

    private final AdminHomeService adminHomeService;

    @Operation(summary = "관리자 홈 요약 조회", description = "활성 회원, 신규 가입, 비활성 전환, 미답변 문의 등 관리자 홈 화면 요약 지표를 조회합니다.")
    @GetMapping
    public ResponseEntity<ApiResponse<AdminHomeResponse>> getHome() {
        return ResponseEntity.ok(ApiResponse.success(adminHomeService.getHome()));
    }
}
