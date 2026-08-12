package com.naengpa.naengpamasterbackend.payment.repository;

import com.naengpa.naengpamasterbackend.payment.entity.Payment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface PaymentRepository extends JpaRepository<Payment, Long> {

    Optional<Payment> findByTossPaymentKey(String tossPaymentKey);

    List<Payment> findAllByMemberIdOrderByCreatedAtDesc(Long memberId);
}
