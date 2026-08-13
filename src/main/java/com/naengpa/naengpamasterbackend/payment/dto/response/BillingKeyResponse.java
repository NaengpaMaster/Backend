package com.naengpa.naengpamasterbackend.payment.dto.response;

import com.naengpa.naengpamasterbackend.payment.entity.BillingKey;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;

@Schema(description = "빌링키 등록 응답")
public record BillingKeyResponse(

        @Schema(description = "빌링키 ID")
        Long billingKeyId,

        @Schema(description = "TossPayments 고객 식별 키")
        String customerKey,

        @Schema(description = "카드사")
        String cardCompany,

        @Schema(description = "마스킹 카드 번호")
        String cardNumberMasked,

        @Schema(description = "활성 여부")
        Boolean active,

        @Schema(description = "등록 일시")
        LocalDateTime createdAt
) {

    public static BillingKeyResponse from(BillingKey billingKey) {
        return new BillingKeyResponse(
                billingKey.getBillingKeyId(),
                billingKey.getTossCustomerKey(),
                billingKey.getCardCompany(),
                billingKey.getCardNumberMasked(),
                billingKey.getIsActive(),
                billingKey.getCreatedAt()
        );
    }
}