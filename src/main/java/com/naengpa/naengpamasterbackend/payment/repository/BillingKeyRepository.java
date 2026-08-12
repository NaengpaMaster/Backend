package com.naengpa.naengpamasterbackend.payment.repository;

import com.naengpa.naengpamasterbackend.payment.entity.BillingKey;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface BillingKeyRepository extends JpaRepository<BillingKey, Long> {

    Optional<BillingKey> findFirstByMemberIdAndIsActiveTrueOrderByBillingKeyIdDesc(Long memberId);

    boolean existsByTossCustomerKey(String tossCustomerKey);

    boolean existsByTossBillingKey(String tossBillingKey);
}