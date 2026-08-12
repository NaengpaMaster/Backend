package com.naengpa.naengpamasterbackend.payment.dto.response;

import com.naengpa.naengpamasterbackend.payment.entity.BillingKey;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;

@Schema(description = "내 등록 카드 응답")
public record MyBillingKeyResponse(
        Long billingKeyId,
        String cardCompany,
        String cardNumberMasked,
        LocalDateTime createdAt
) {
    public static MyBillingKeyResponse from(BillingKey billingKey) {
        return new MyBillingKeyResponse(
                billingKey.getBillingKeyId(),
                billingKey.getCardCompany(),
                billingKey.getCardNumberMasked(),
                billingKey.getCreatedAt()
        );
    }
}
