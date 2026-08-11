package com.naengpa.naengpamasterbackend.admin.controller;

import com.naengpa.naengpamasterbackend.admin.dto.response.AdminWeeklyFridgeReportDispatchResponse;
import com.naengpa.naengpamasterbackend.fridge.report.service.WeeklyFridgeReportDispatchService;
import com.naengpa.naengpamasterbackend.global.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/admin/weekly-fridge-reports")
@RequiredArgsConstructor
@Tag(name = "관리자 주간 냉장고 리포트", description = "관리자 주간 냉장고 리포트 메일 수동 발송 API")
public class AdminWeeklyFridgeReportController {

    private final WeeklyFridgeReportDispatchService dispatchService;

    @Operation(
            summary = "주간 냉장고 리포트 메일 수동 발송",
            description = "관리자가 주간 냉장고 소비 리포트 메일 발송을 즉시 실행합니다. force=true이면 이미 발송된 대상도 메일은 재발송하되 성공 로그는 중복 저장하지 않습니다."
    )
    @PostMapping("/send")
    public ResponseEntity<ApiResponse<AdminWeeklyFridgeReportDispatchResponse>> sendWeeklyFridgeReports(
            @Parameter(description = "중복 발송 방지 여부를 무시하고 강제 발송할지 여부")
            @RequestParam(defaultValue = "false") boolean force
    ) {
        int sentCount = dispatchService.dispatchWeeklyReports(force);
        return ResponseEntity.ok(ApiResponse.success(
                "주간 냉장고 리포트 메일 발송을 실행했습니다.",
                new AdminWeeklyFridgeReportDispatchResponse(sentCount)
        ));
    }
}
