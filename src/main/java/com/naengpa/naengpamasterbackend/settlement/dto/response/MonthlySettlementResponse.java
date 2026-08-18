package com.naengpa.naengpamasterbackend.settlement.dto.response;

import com.naengpa.naengpamasterbackend.settlement.entity.MonthlySettlement;
import com.naengpa.naengpamasterbackend.settlement.entity.SettlementStatus;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;

@Schema(description = "월별 정산 생성 응답")
public record MonthlySettlementResponse(

        @Schema(description = "월별 정산 ID", example = "1")
        Long monthlySettlementId,

        @Schema(description = "정산 월", example = "2026-08")
        String settlementMonth,

        @Schema(description = "총매출", example = "29000")
        int grossAmount,

        @Schema(description = "취소 금액", example = "0")
        int canceledAmount,

        @Schema(description = "Toss 수수료", example = "870")
        int tossFeeAmount,

        @Schema(description = "LLM 비용", example = "0")
        int llmCostAmount,

        @Schema(description = "순매출", example = "28130")
        int netAmount,

        @Schema(description = "구독자 수", example = "10")
        int subscriberCount,

        @Schema(description = "결제 건수", example = "10")
        int paymentCount,

        @Schema(description = "정산 상태", example = "PENDING")
        SettlementStatus status,

        @Schema(description = "정산 확정 시각")
        LocalDateTime confirmedAt,

        @Schema(description = "정산 마감 시각")
        LocalDateTime paidAt
) {

    // Entity를 API 응답 형태로 변환
    public static MonthlySettlementResponse from(MonthlySettlement settlement) {
        return new MonthlySettlementResponse(
                settlement.getMonthlySettlementId(),
                settlement.getSettlementMonth(),
                settlement.getGrossAmount(),
                settlement.getCanceledAmount(),
                settlement.getTossFeeAmount(),
                settlement.getLlmCostAmount(),
                settlement.getNetAmount(),
                settlement.getSubscriberCount(),
                settlement.getPaymentCount(),
                settlement.getStatus(),
                settlement.getConfirmedAt(),
                settlement.getPaidAt()
        );
    }
}