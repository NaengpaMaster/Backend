package com.naengpa.naengpamasterbackend.payment.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "payments")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Payment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "payment_id")
    private Long paymentId;

    @Column(name = "member_id", nullable = false)
    private Long memberId;

    @Column(name = "billing_key_id")
    private Long billingKeyId;

    @Column(name = "toss_payment_key", unique = true)
    private String tossPaymentKey;

    @Column(name = "order_id", nullable = false, unique = true)
    private String orderId;

    @Column(name = "order_name", nullable = false)
    private String orderName;

    @Column(nullable = false)
    private int amount;

    @Enumerated(EnumType.STRING)
    @Column(name = "plan_type", nullable = false, length = 30)
    private PaymentPlanType planType;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private PaymentStatus status;

    @Column(name = "billing_period_start")
    private LocalDate billingPeriodStart;

    @Column(name = "billing_period_end")
    private LocalDate billingPeriodEnd;

    @Column(name = "approved_at")
    private LocalDateTime approvedAt;

    @Column(name = "failed_reason", columnDefinition = "TEXT")
    private String failedReason;

    @Column(name = "retry_count", nullable = false)
    private int retryCount;

    @Column(name = "last_failed_at")
    private LocalDateTime lastFailedAt;

    @Column(name = "next_retry_at")
    private LocalDateTime nextRetryAt;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    // Toss 자동결제를 요청하기 전 READY 상태의 결제 이력을 먼저 생성
    public static Payment ready(
            Long memberId,
            Long billingKeyId,
            String orderId,
            String orderName,
            int amount,
            PaymentPlanType planType,
            LocalDate billingPeriodStart,
            LocalDate billingPeriodEnd
    ) {
        Payment payment = new Payment();
        payment.memberId = memberId;
        payment.billingKeyId = billingKeyId;
        payment.orderId = orderId;
        payment.orderName = orderName;
        payment.amount = amount;
        payment.planType = planType;
        payment.status = PaymentStatus.READY;
        payment.billingPeriodStart = billingPeriodStart;
        payment.billingPeriodEnd = billingPeriodEnd;
        payment.retryCount = 0;
        return payment;
    }

    // Toss 자동결제가 성공하면 paymentKey와 승인 시각을 저장
    public void markSuccess(String tossPaymentKey, LocalDateTime approvedAt) {
        this.tossPaymentKey = tossPaymentKey;
        this.status = PaymentStatus.SUCCESS;
        this.approvedAt = approvedAt;
        this.failedReason = null;
        this.lastFailedAt = null;
        this.nextRetryAt = null;
    }

    // Toss 자동결제가 실패하면 실패 사유와 재시도 정보를 저장
    public void markFailed(String failedReason, int retryCount, LocalDateTime nextRetryAt) {
        this.status = retryCount >= 3 ? PaymentStatus.FAILED : PaymentStatus.RETRYING;
        this.failedReason = failedReason;
        this.retryCount = retryCount;
        this.lastFailedAt = LocalDateTime.now();
        this.nextRetryAt = nextRetryAt;
    }

    @PrePersist
    void prePersist() {
        this.createdAt = LocalDateTime.now();
    }

    @PreUpdate
    void preUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}