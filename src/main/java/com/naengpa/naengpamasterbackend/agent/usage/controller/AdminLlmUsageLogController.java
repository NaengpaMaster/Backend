package com.naengpa.naengpamasterbackend.agent.usage.controller;

import com.naengpa.naengpamasterbackend.agent.usage.dto.response.AdminLlmUsageLogResponse;
import com.naengpa.naengpamasterbackend.agent.usage.dto.response.AdminLlmUsageLogPageResponse;
import com.naengpa.naengpamasterbackend.agent.usage.entity.LlmFeatureType;
import com.naengpa.naengpamasterbackend.agent.usage.service.AdminLlmUsageLogService;
import com.naengpa.naengpamasterbackend.global.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;

import java.util.List;

@Tag(name = "Admin LLM Usage", description = "관리자 LLM 사용량 로그 API")
@RestController
@RequestMapping("/api/v1/admin/ai/usage-logs")
public class AdminLlmUsageLogController {

    private final AdminLlmUsageLogService adminLlmUsageLogService;

    public AdminLlmUsageLogController(AdminLlmUsageLogService adminLlmUsageLogService) {
        this.adminLlmUsageLogService = adminLlmUsageLogService;
    }

    @Operation(summary = "관리자 LLM 사용량 로그 전체 조회", description = "전체 회원의 LLM 사용량 로그를 최신순으로 조회합니다.")
    @GetMapping
    public ResponseEntity<ApiResponse<AdminLlmUsageLogPageResponse>> findAllUsageLogs(
            @RequestParam(required = false) LlmFeatureType featureType,
            @PageableDefault(size = 10) Pageable pageable
    ) {
        AdminLlmUsageLogPageResponse response = adminLlmUsageLogService.findAllUsageLogs(featureType, pageable);

        return ResponseEntity.ok(
                ApiResponse.success("관리자 LLM 사용량 로그 조회에 성공했습니다.", response)
        );
    }
}
