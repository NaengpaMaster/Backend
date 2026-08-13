package com.naengpa.naengpamasterbackend.settlement.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "settlement_payment_details")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class SettlementPaymentDetail {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "settlement_payment_detail_id")
    private Long settlementPaymentDetailId;

    @Column(name = "monthly_settlement_id", nullable = false)
    private Long monthlySettlementId;

    @Column(name = "payment_id", nullable = false, unique = true)
    private Long paymentId;

    @Column(nullable = false)
    private int amount;

    @Column(name = "toss_fee_amount", nullable = false)
    private int tossFeeAmount;

    @Column(name = "llm_cost_amount", nullable = false)
    private int llmCostAmount;

    @Column(name = "net_amount", nullable = false)
    private int netAmount;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    // 월별 정산에 어떤 결제가 포함됐는지 추적하기 위한 상세 row
    public static SettlementPaymentDetail create(
            Long monthlySettlementId,
            Long paymentId,
            int amount,
            int tossFeeAmount,
            int llmCostAmount,
            int netAmount
    ) {
        SettlementPaymentDetail detail = new SettlementPaymentDetail();
        detail.monthlySettlementId = monthlySettlementId;
        detail.paymentId = paymentId;
        detail.amount = amount;
        detail.tossFeeAmount = tossFeeAmount;
        detail.llmCostAmount = llmCostAmount;
        detail.netAmount = netAmount;
        return detail;
    }

    @PrePersist
    void prePersist() {
        createdAt = LocalDateTime.now();
    }
}
