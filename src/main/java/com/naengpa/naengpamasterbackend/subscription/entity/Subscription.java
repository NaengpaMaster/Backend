package com.naengpa.naengpamasterbackend.subscription.entity;

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
@Table(name = "subscriptions")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Subscription {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "subscription_id")
    private Long subscriptionId;

    @Column(name = "member_id", nullable = false)
    private Long memberId;

    @Column(name = "fridge_id", nullable = false)
    private Long fridgeId;

    @Column(name = "subscription_plan_id", nullable = false)
    private Long subscriptionPlanId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private SubscriptionStatus status;

    @Column(name = "trial_started_at")
    private LocalDateTime trialStartedAt;

    @Column(name = "trial_ends_at")
    private LocalDateTime trialEndsAt;

    @Column(name = "current_period_start_at")
    private LocalDateTime currentPeriodStartAt;

    @Column(name = "current_period_end_at")
    private LocalDateTime currentPeriodEndAt;

    @Column(name = "next_billing_at")
    private LocalDateTime nextBillingAt;

    @Column(name = "canceled_at")
    private LocalDateTime canceledAt;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    public boolean allowsFamilyShare() {
        return status == SubscriptionStatus.TRIALING || status == SubscriptionStatus.ACTIVE;
    }

    public static Subscription createActive(
            Long memberId,
            Long fridgeId,
            Long subscriptionPlanId,
            LocalDateTime periodStartAt,
            LocalDateTime periodEndAt,
            LocalDateTime nextBillingAt
    ) {
        Subscription subscription = new Subscription();
        subscription.memberId = memberId;
        subscription.fridgeId = fridgeId;
        subscription.subscriptionPlanId = subscriptionPlanId;
        subscription.status = SubscriptionStatus.ACTIVE;
        subscription.currentPeriodStartAt = periodStartAt;
        subscription.currentPeriodEndAt = periodEndAt;
        subscription.nextBillingAt = nextBillingAt;
        return subscription;
    }

    public void renew(
            Long subscriptionPlanId,
            LocalDateTime periodStartAt,
            LocalDateTime periodEndAt,
            LocalDateTime nextBillingAt
    ) {
        this.subscriptionPlanId = subscriptionPlanId;
        this.status = SubscriptionStatus.ACTIVE;
        this.currentPeriodStartAt = periodStartAt;
        this.currentPeriodEndAt = periodEndAt;
        this.nextBillingAt = nextBillingAt;
        this.canceledAt = null;
    }

    // 현재 이용 기간은 유지하고 다음 자동결제만 중단
    public void reserveCancel() {
        this.nextBillingAt = null;
        this.canceledAt = LocalDateTime.now();
    }

    public boolean isCancelReserved() {
        return canceledAt != null && nextBillingAt == null;
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
