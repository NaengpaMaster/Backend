package com.naengpa.naengpamasterbackend.payment.dto.response;

import com.naengpa.naengpamasterbackend.payment.entity.Payment;
import com.naengpa.naengpamasterbackend.payment.entity.PaymentPlanType;
import com.naengpa.naengpamasterbackend.payment.entity.PaymentStatus;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDate;

@Schema(description = "구독 자동결제 응답")
public record SubscriptionPaymentResponse(

        @Schema(description = "결제 ID")
        Long paymentId,

        @Schema(description = "주문 ID")
        String orderId,

        @Schema(description = "결제 금액")
        int amount,

        @Schema(description = "구독 플랜 타입")
        PaymentPlanType planType,

        @Schema(description = "결제 상태")
        PaymentStatus status,

        @Schema(description = "구독 시작일")
        LocalDate billingPeriodStart,

        @Schema(description = "구독 종료일")
        LocalDate billingPeriodEnd
) {

    public static SubscriptionPaymentResponse from(Payment payment) {
        return new SubscriptionPaymentResponse(
                payment.getPaymentId(),
                payment.getOrderId(),
                payment.getAmount(),
                payment.getPlanType(),
                payment.getStatus(),
                payment.getBillingPeriodStart(),
                payment.getBillingPeriodEnd()
        );
    }
}