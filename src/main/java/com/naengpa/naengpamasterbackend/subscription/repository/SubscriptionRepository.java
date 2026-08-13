package com.naengpa.naengpamasterbackend.subscription.repository;

import com.naengpa.naengpamasterbackend.subscription.entity.Subscription;
import com.naengpa.naengpamasterbackend.subscription.entity.SubscriptionStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface SubscriptionRepository extends JpaRepository<Subscription, Long> {

    Optional<Subscription> findFirstByFridgeIdAndStatusInOrderBySubscriptionIdDesc(
            Long fridgeId,
            Collection<SubscriptionStatus> statuses
    );

    Optional<Subscription> findFirstByMemberIdAndStatusInOrderBySubscriptionIdDesc(
            Long memberId,
            Collection<SubscriptionStatus> statuses
    );

    boolean existsByMemberIdAndTrialStartedAtIsNotNull(Long memberId);

    List<Subscription> findAllByStatusInAndNextBillingAtLessThanEqualAndCanceledAtIsNull(
            Collection<SubscriptionStatus> statuses,
            LocalDateTime now
    );
}
