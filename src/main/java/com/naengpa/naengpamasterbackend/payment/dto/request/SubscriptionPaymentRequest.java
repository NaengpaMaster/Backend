package com.naengpa.naengpamasterbackend.payment.dto.request;

import com.naengpa.naengpamasterbackend.payment.entity.PaymentPlanType;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

@Schema(description = "구독 자동결제 요청")
public record SubscriptionPaymentRequest(

        @Schema(description = "결제할 구독 플랜 타입", example = "MONTHLY")
        @NotNull(message = "구독 플랜을 선택해주세요.")
        PaymentPlanType planType
) {
}