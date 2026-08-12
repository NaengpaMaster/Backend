package com.naengpa.naengpamasterbackend.payment.repository;

import com.naengpa.naengpamasterbackend.payment.entity.PaymentWebhookEvent;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PaymentWebhookEventRepository extends JpaRepository<PaymentWebhookEvent, Long> {

    boolean existsByEventId(String eventId);
}