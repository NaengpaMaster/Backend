package com.naengpa.naengpamasterbackend.payment.entity;

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
@Table(name = "payment_webhook_events")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class PaymentWebhookEvent {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "payment_webhook_event_id")
    private Long paymentWebhookEventId;

    @Column(name = "event_id", nullable = false, unique = true)
    private String eventId;

    @Column(name = "event_type", nullable = false, length = 100)
    private String eventType;

    @Column(name = "toss_payment_key")
    private String tossPaymentKey;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String payload;

    @Column(nullable = false)
    private Boolean processed;

    @Column(name = "processed_at")
    private LocalDateTime processedAt;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    // Toss 웹훅 원문을 먼저 저장해 중복 수신과 재처리 기준으로 사용
    public static PaymentWebhookEvent create(
            String eventId,
            String eventType,
            String tossPaymentKey,
            String payload
    ) {
        PaymentWebhookEvent event = new PaymentWebhookEvent();
        event.eventId = eventId;
        event.eventType = eventType;
        event.tossPaymentKey = tossPaymentKey;
        event.payload = payload;
        event.processed = false;
        return event;
    }

    // 결제 상태 반영까지 끝난 웹훅을 처리 완료로 표시
    public void markProcessed() {
        this.processed = true;
        this.processedAt = LocalDateTime.now();
    }

    @PrePersist
    void prePersist() {
        this.createdAt = LocalDateTime.now();
    }
}