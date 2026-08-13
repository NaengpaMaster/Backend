package com.naengpa.naengpamasterbackend.settlement.dto.response;

import com.naengpa.naengpamasterbackend.settlement.entity.SettlementPaymentDetail;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "월별 정산 상세 결제 응답")
public record SettlementPaymentDetailResponse(

        @Schema(description = "정산 상세 결제 ID", example = "1")
        Long settlementPaymentDetailId,

        @Schema(description = "결제 ID", example = "10")
        Long paymentId,

        @Schema(description = "결제 금액", example = "2900")
        int amount,

        @Schema(description = "Toss 수수료", example = "87")
        int tossFeeAmount,

        @Schema(description = "LLM 비용", example = "0")
        int llmCostAmount,

        @Schema(description = "순매출", example = "2813")
        int netAmount
) {

    // 정산 상세 Entity를 관리자 화면에서 보여줄 결제 단위 응답으로 변환
    public static SettlementPaymentDetailResponse from(SettlementPaymentDetail detail) {
        return new SettlementPaymentDetailResponse(
                detail.getSettlementPaymentDetailId(),
                detail.getPaymentId(),
                detail.getAmount(),
                detail.getTossFeeAmount(),
                detail.getLlmCostAmount(),
                detail.getNetAmount()
        );
    }
}
