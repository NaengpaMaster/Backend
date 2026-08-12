package com.naengpa.naengpamasterbackend.payment.repository;

import com.naengpa.naengpamasterbackend.payment.entity.Payment;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PaymentRepository extends JpaRepository<Payment, Long> {
}
