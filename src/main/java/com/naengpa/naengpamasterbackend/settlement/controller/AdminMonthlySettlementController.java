package com.naengpa.naengpamasterbackend.settlement.controller;

import com.naengpa.naengpamasterbackend.global.response.ApiResponse;
import com.naengpa.naengpamasterbackend.settlement.dto.response.MonthlySettlementDetailResponse;
import com.naengpa.naengpamasterbackend.settlement.dto.response.MonthlySettlementResponse;
import com.naengpa.naengpamasterbackend.settlement.entity.SettlementStatus;
import com.naengpa.naengpamasterbackend.settlement.service.MonthlySettlementService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.PatchMapping;

import java.time.YearMonth;
import java.util.List;

@RestController
@RequestMapping("/api/v1/admin/settlements")
@RequiredArgsConstructor
@Tag(name = "관리자 정산", description = "관리자 월별 구독 매출 정산 API")
public class AdminMonthlySettlementController {

    private final MonthlySettlementService monthlySettlementService;

    @Operation(
            summary = "월별 정산 목록 조회",
            description = "관리자가 월별 정산 목록을 조회합니다. 상태와 정산 월로 필터링할 수 있습니다."
    )
    @io.swagger.v3.oas.annotations.responses.ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "월별 정산 목록 조회 성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "관리자 권한 없음")
    })
    @GetMapping
    public ResponseEntity<ApiResponse<List<MonthlySettlementResponse>>> findMonthlySettlements(
            @Parameter(description = "정산 상태", example = "PENDING")
            @RequestParam(required = false) SettlementStatus status,

            @Parameter(description = "정산 월", example = "2026-08")
            @RequestParam(required = false) String settlementMonth
    ) {
        List<MonthlySettlementResponse> response = monthlySettlementService.findMonthlySettlements(
                status,
                settlementMonth
        );

        return ResponseEntity.ok(ApiResponse.success(
                "월별 정산 목록 조회에 성공했습니다.",
                response
        ));
    }

    @Operation(
            summary = "월별 정산 확정",
            description = "PENDING 상태의 월별 정산을 CONFIRMED 상태로 변경합니다."
    )
    @io.swagger.v3.oas.annotations.responses.ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "월별 정산 확정 성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "관리자 권한 없음"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "월별 정산 없음"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "409", description = "허용되지 않는 상태 변경")
    })
    @PatchMapping("/{settlementId}/confirm")
    public ResponseEntity<ApiResponse<MonthlySettlementResponse>> confirmMonthlySettlement(
            @Parameter(description = "월별 정산 ID", example = "1")
            @PathVariable Long settlementId
    ) {
        MonthlySettlementResponse response = monthlySettlementService.confirmMonthlySettlement(settlementId);

        return ResponseEntity.ok(ApiResponse.success(
                "월별 정산이 확정되었습니다.",
                response
        ));
    }

    @Operation(
            summary = "월별 정산 지급 완료 처리",
            description = "CONFIRMED 상태의 월별 정산을 PAID 상태로 변경합니다."
    )
    @io.swagger.v3.oas.annotations.responses.ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "월별 정산 지급 완료 처리 성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "관리자 권한 없음"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "월별 정산 없음"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "409", description = "허용되지 않는 상태 변경")
    })
    @PatchMapping("/{settlementId}/paid")
    public ResponseEntity<ApiResponse<MonthlySettlementResponse>> markMonthlySettlementPaid(
            @Parameter(description = "월별 정산 ID", example = "1")
            @PathVariable Long settlementId
    ) {
        MonthlySettlementResponse response = monthlySettlementService.markMonthlySettlementPaid(settlementId);

        return ResponseEntity.ok(ApiResponse.success(
                "월별 정산이 지급 완료 처리되었습니다.",
                response
        ));
    }

    @Operation(
            summary = "월별 정산 취소",
            description = "PENDING 또는 CONFIRMED 상태의 월별 정산을 CANCELED 상태로 변경합니다."
    )
    @io.swagger.v3.oas.annotations.responses.ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "월별 정산 취소 성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "관리자 권한 없음"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "월별 정산 없음"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "409", description = "허용되지 않는 상태 변경")
    })
    @PatchMapping("/{settlementId}/cancel")
    public ResponseEntity<ApiResponse<MonthlySettlementResponse>> cancelMonthlySettlement(
            @Parameter(description = "월별 정산 ID", example = "1")
            @PathVariable Long settlementId
    ) {
        MonthlySettlementResponse response = monthlySettlementService.cancelMonthlySettlement(settlementId);

        return ResponseEntity.ok(ApiResponse.success(
                "월별 정산이 취소되었습니다.",
                response
        ));
    }

    @Operation(
            summary = "월별 정산 상세 조회",
            description = "관리자가 월별 정산 요약과 포함 결제 상세 내역을 조회합니다."
    )
    @io.swagger.v3.oas.annotations.responses.ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "월별 정산 상세 조회 성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "관리자 권한 없음"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "월별 정산 없음")
    })
    @GetMapping("/{settlementId}")
    public ResponseEntity<ApiResponse<MonthlySettlementDetailResponse>> findMonthlySettlementDetail(
            @Parameter(description = "월별 정산 ID", example = "1")
            @PathVariable Long settlementId
    ) {
        MonthlySettlementDetailResponse response = monthlySettlementService
                .findMonthlySettlementDetail(settlementId);

        return ResponseEntity.ok(ApiResponse.success(
                "월별 정산 상세 조회에 성공했습니다.",
                response
        ));
    }

    @Operation(
            summary = "월별 정산 생성",
            description = "지정한 월 또는 전월의 성공 결제를 기준으로 PENDING 정산을 생성하거나 재계산합니다."
    )
    @io.swagger.v3.oas.annotations.responses.ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "월별 정산 생성 성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "정산 월 형식 오류"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "관리자 권한 없음"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "409", description = "확정된 정산 재계산 불가")
    })
    @PostMapping("/monthly")
    public ResponseEntity<ApiResponse<MonthlySettlementResponse>> createMonthlySettlement(
            @Parameter(description = "정산 월, 생략 시 전월 기준", example = "2026-08")
            @RequestParam(required = false) String settlementMonth
    ) {
        // settlementMonth가 없으면 매월 1일 전월분 정산 생성 정책에 맞춰 전월을 기본값으로 사용
        YearMonth targetMonth = settlementMonth == null || settlementMonth.isBlank()
                ? YearMonth.now().minusMonths(1)
                : YearMonth.parse(settlementMonth);

        MonthlySettlementResponse response = MonthlySettlementResponse.from(
                monthlySettlementService.createMonthlySettlement(targetMonth)
        );

        return ResponseEntity.ok(ApiResponse.success(
                "월별 정산이 생성되었습니다.",
                response
        ));
    }
}
