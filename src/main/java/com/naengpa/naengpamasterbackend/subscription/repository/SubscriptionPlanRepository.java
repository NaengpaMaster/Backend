package com.naengpa.naengpamasterbackend.subscription.repository;

import com.naengpa.naengpamasterbackend.subscription.entity.SubscriptionPlan;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface SubscriptionPlanRepository extends JpaRepository<SubscriptionPlan, Long> {

    Optional<SubscriptionPlan> findByCodeAndActiveTrue(String code);
}
