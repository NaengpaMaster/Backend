package com.naengpa.naengpamasterbackend.payment.dto.response;

import com.naengpa.naengpamasterbackend.payment.entity.Payment;
import com.naengpa.naengpamasterbackend.payment.entity.PaymentPlanType;
import com.naengpa.naengpamasterbackend.payment.entity.PaymentStatus;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Schema(description = "내 구독 결제 내역 응답")
public record MyPaymentHistoryResponse(
        Long paymentId,
        String orderName,
        int amount,
        PaymentPlanType planType,
        PaymentStatus status,
        LocalDate billingPeriodStart,
        LocalDate billingPeriodEnd,
        LocalDateTime approvedAt,
        String failedReason,
        LocalDateTime createdAt
) {
    public static MyPaymentHistoryResponse from(Payment payment) {
        return new MyPaymentHistoryResponse(
                payment.getPaymentId(),
                payment.getOrderName(),
                payment.getAmount(),
                payment.getPlanType(),
                payment.getStatus(),
                payment.getBillingPeriodStart(),
                payment.getBillingPeriodEnd(),
                payment.getApprovedAt(),
                payment.getFailedReason(),
                payment.getCreatedAt()
        );
    }
}
