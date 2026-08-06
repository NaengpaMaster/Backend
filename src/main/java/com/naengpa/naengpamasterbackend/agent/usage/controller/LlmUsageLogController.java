package com.naengpa.naengpamasterbackend.agent.usage.controller;

import com.naengpa.naengpamasterbackend.agent.usage.dto.response.LlmUsageLogResponse;
import com.naengpa.naengpamasterbackend.agent.usage.service.LlmUsageLogService;
import com.naengpa.naengpamasterbackend.global.response.ApiResponse;
import io.swagger.v3.oas.annotations.Parameter;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/ai/usage-logs")
public class LlmUsageLogController {

    private final LlmUsageLogService llmUsageLogService;

    public LlmUsageLogController(LlmUsageLogService llmUsageLogService) {
        this.llmUsageLogService = llmUsageLogService;
    }

    // API-706
    // 로그인한 회원의 LLM 사용량 로그를 최신순으로 조회
    @GetMapping
    public ResponseEntity<ApiResponse<List<LlmUsageLogResponse>>> findMyUsageLogs(
            @Parameter(hidden = true) Authentication authentication
    ) {
        List<LlmUsageLogResponse> response =
                llmUsageLogService.findMyUsageLogs(authentication.getName());

        return ResponseEntity.ok(
                ApiResponse.success("LLM 사용량 로그 조회에 성공했습니다.", response)
        );
    }
}
