package com.naengpa.naengpamasterbackend.settlement.dto.response;

import com.naengpa.naengpamasterbackend.settlement.entity.MonthlySettlement;
import com.naengpa.naengpamasterbackend.settlement.entity.SettlementPaymentDetail;
import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

@Schema(description = "월별 정산 상세 조회 응답")
public record MonthlySettlementDetailResponse(

        @Schema(description = "월별 정산 요약")
        MonthlySettlementResponse settlement,

        @Schema(description = "정산에 포함된 결제 상세 목록")
        List<SettlementPaymentDetailResponse> paymentDetails
) {

    // 정산 요약과 포함 결제 내역을 하나의 상세 응답으로 묶음
    public static MonthlySettlementDetailResponse from(
            MonthlySettlement settlement,
            List<SettlementPaymentDetail> details
    ) {
        return new MonthlySettlementDetailResponse(
                MonthlySettlementResponse.from(settlement),
                details.stream()
                        .map(SettlementPaymentDetailResponse::from)
                        .toList()
        );
    }
}
