package com.naengpa.naengpamasterbackend.settlement.entity;

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

import java.time.LocalDateTime;

@Entity
@Table(name = "monthly_settlements")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class MonthlySettlement {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "monthly_settlement_id")
    private Long monthlySettlementId;

    @Column(name = "settlement_month", nullable = false, length = 7, unique = true)
    private String settlementMonth;

    @Column(name = "gross_amount", nullable = false)
    private int grossAmount;

    @Column(name = "canceled_amount", nullable = false)
    private int canceledAmount;

    @Column(name = "toss_fee_amount", nullable = false)
    private int tossFeeAmount;

    @Column(name = "llm_cost_amount", nullable = false)
    private int llmCostAmount;

    @Column(name = "net_amount", nullable = false)
    private int netAmount;

    @Column(name = "subscriber_count", nullable = false)
    private int subscriberCount;

    @Column(name = "payment_count", nullable = false)
    private int paymentCount;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private SettlementStatus status;

    @Column(name = "confirmed_at")
    private LocalDateTime confirmedAt;

    @Column(name = "paid_at")
    private LocalDateTime paidAt;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    // 월별 정산은 먼저 PENDING 상태로 생성하고, 관리자가 검토 후 확정/지급 처리
    public static MonthlySettlement createPending(
            String settlementMonth,
            int grossAmount,
            int canceledAmount,
            int tossFeeAmount,
            int llmCostAmount,
            int netAmount,
            int subscriberCount,
            int paymentCount
    ) {
        MonthlySettlement settlement = new MonthlySettlement();
        settlement.settlementMonth = settlementMonth;
        settlement.grossAmount = grossAmount;
        settlement.canceledAmount = canceledAmount;
        settlement.tossFeeAmount = tossFeeAmount;
        settlement.llmCostAmount = llmCostAmount;
        settlement.netAmount = netAmount;
        settlement.subscriberCount = subscriberCount;
        settlement.paymentCount = paymentCount;
        settlement.status = SettlementStatus.PENDING;
        return settlement;
    }

    // PENDING 정산은 결제 데이터가 바뀌면 같은 월 기준으로 다시 계산 가능
    public void recalculate(
            int grossAmount,
            int canceledAmount,
            int tossFeeAmount,
            int llmCostAmount,
            int netAmount,
            int subscriberCount,
            int paymentCount
    ) {
        if (status != SettlementStatus.PENDING) {
            throw new IllegalStateException("확정된 정산은 재계산할 수 없습니다.");
        }

        this.grossAmount = grossAmount;
        this.canceledAmount = canceledAmount;
        this.tossFeeAmount = tossFeeAmount;
        this.llmCostAmount = llmCostAmount;
        this.netAmount = netAmount;
        this.subscriberCount = subscriberCount;
        this.paymentCount = paymentCount;
    }

    // PENDING 정산을 확정 상태로 변경
    public void confirm() {
        if (status != SettlementStatus.PENDING) {
            throw new IllegalStateException("PENDING 정산만 확정할 수 있습니다.");
        }

        status = SettlementStatus.CONFIRMED;
        confirmedAt = LocalDateTime.now();
    }

    // CONFIRMED 정산을 지급 완료 상태로 변경
    public void markPaid() {
        if (status != SettlementStatus.CONFIRMED) {
            throw new IllegalStateException("CONFIRMED 정산만 지급 완료 처리할 수 있습니다.");
        }

        status = SettlementStatus.PAID;
        paidAt = LocalDateTime.now();
    }

    // PENDING 또는 CONFIRMED 정산을 취소 상태로 변경
    public void cancel() {
        if (status == SettlementStatus.PAID) {
            throw new IllegalStateException("이미 지급 완료된 정산은 취소할 수 없습니다.");
        }

        if (status == SettlementStatus.CANCELED) {
            throw new IllegalStateException("이미 취소된 정산입니다.");
        }

        if (status != SettlementStatus.PENDING && status != SettlementStatus.CONFIRMED) {
            throw new IllegalStateException("취소할 수 없는 정산 상태입니다.");
        }

        status = SettlementStatus.CANCELED;
    }

    @PrePersist
    void prePersist() {
        createdAt = LocalDateTime.now();
    }

    @PreUpdate
    void preUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
