package com.naengpa.naengpamasterbackend.admin.controller;

import com.naengpa.naengpamasterbackend.admin.dto.response.AdminHomeResponse;
import com.naengpa.naengpamasterbackend.admin.service.AdminHomeService;
import com.naengpa.naengpamasterbackend.global.response.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/admin/home")
@RequiredArgsConstructor
public class AdminHomeController {

    private final AdminHomeService adminHomeService;

    @GetMapping
    public ResponseEntity<ApiResponse<AdminHomeResponse>> getHome() {
        return ResponseEntity.ok(ApiResponse.success(adminHomeService.getHome()));
    }
}
