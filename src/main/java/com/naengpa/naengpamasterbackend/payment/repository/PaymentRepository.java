package com.naengpa.naengpamasterbackend.payment.repository;

import com.naengpa.naengpamasterbackend.payment.entity.Payment;
import com.naengpa.naengpamasterbackend.payment.entity.PaymentStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface PaymentRepository extends JpaRepository<Payment, Long> {

    Optional<Payment> findByTossPaymentKey(String tossPaymentKey);

    List<Payment> findAllByMemberIdOrderByCreatedAtDesc(Long memberId);

    long countByMemberIdAndBillingPeriodStartAndBillingPeriodEndAndStatusIn(
            Long memberId,
            LocalDate billingPeriodStart,
            LocalDate billingPeriodEnd,
            List<PaymentStatus> statuses
    );

    List<Payment> findAllByStatusAndApprovedAtGreaterThanEqualAndApprovedAtLessThan(
            PaymentStatus status,
            LocalDateTime startAt,
            LocalDateTime endAt
    );
}
