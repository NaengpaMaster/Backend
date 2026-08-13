package com.naengpa.naengpamasterbackend.subscription.dto.response;

import com.naengpa.naengpamasterbackend.subscription.entity.Subscription;
import com.naengpa.naengpamasterbackend.subscription.entity.SubscriptionStatus;

import java.time.LocalDateTime;

public record SubscriptionStatusResponse(
        Long subscriptionId,
        Long memberId,
        Long fridgeId,
        SubscriptionStatus status,
        boolean premium,
        LocalDateTime trialEndsAt,
        LocalDateTime currentPeriodEndAt,
        LocalDateTime availableUntil,
        LocalDateTime nextBillingAt,
        LocalDateTime canceledAt,
        boolean cancelReserved
) {
    public static SubscriptionStatusResponse free(Long memberId, Long fridgeId) {
        return new SubscriptionStatusResponse(
                null,
                memberId,
                fridgeId,
                null,
                false,
                null,
                null,
                null,
                null,
                null,
                false
        );
    }

    public static SubscriptionStatusResponse from(Subscription subscription) {
        return new SubscriptionStatusResponse(
                subscription.getSubscriptionId(),
                subscription.getMemberId(),
                subscription.getFridgeId(),
                subscription.getStatus(),
                subscription.allowsFamilyShare(),
                subscription.getTrialEndsAt(),
                subscription.getCurrentPeriodEndAt(),
                subscription.getAvailableUntil(),
                subscription.getNextBillingAt(),
                subscription.getCanceledAt(),
                subscription.isCancelReserved()
        );
    }
}