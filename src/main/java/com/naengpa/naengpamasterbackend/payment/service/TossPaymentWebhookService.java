package com.naengpa.naengpamasterbackend.payment.service;

import com.naengpa.naengpamasterbackend.payment.dto.request.TossPaymentWebhookRequest;
import com.naengpa.naengpamasterbackend.payment.entity.Payment;
import com.naengpa.naengpamasterbackend.payment.entity.PaymentWebhookEvent;
import com.naengpa.naengpamasterbackend.payment.exception.TossPaymentException;
import com.naengpa.naengpamasterbackend.payment.repository.PaymentRepository;
import com.naengpa.naengpamasterbackend.payment.repository.PaymentWebhookEventRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class TossPaymentWebhookService {

    private final PaymentWebhookEventRepository paymentWebhookEventRepository;
    private final PaymentRepository paymentRepository;

    // TossPayments 웹훅을 저장하고, eventId 중복이 아니면 결제 상태를 반영
    @Transactional
    public void handleWebhook(String transmissionId, TossPaymentWebhookRequest request) {
        String eventId = resolvedEventId(transmissionId, request);
        String eventType = requiredEventType(request);

        if (paymentWebhookEventRepository.existsByEventId(eventId)) {
            return;
        }

        PaymentWebhookEvent webhookEvent = paymentWebhookEventRepository.save(PaymentWebhookEvent.create(
                eventId,
                eventType,
                request.resolvedPaymentKey(),
                defaultPayload(request)
        ));

        applyPaymentStatus(request, eventType);

        webhookEvent.markProcessed();
    }

    // Toss 웹훅 eventType에 따라 payments 상태를 갱신
    private void applyPaymentStatus(TossPaymentWebhookRequest request, String eventType) {
        String paymentKey = request.resolvedPaymentKey();
        if (paymentKey == null || paymentKey.isBlank()) {
            return;
        }

        paymentRepository.findByTossPaymentKey(paymentKey)
                .ifPresent(payment -> applyPaymentStatus(payment, eventType, request.resolvedPaymentStatus()));
    }

    private void applyPaymentStatus(Payment payment, String eventType, String paymentStatus) {
        if (isSuccessEvent(eventType, paymentStatus)) {
            payment.markSuccess(payment.getTossPaymentKey(), payment.getApprovedAt());
            return;
        }

        if (isFailedEvent(eventType, paymentStatus)) {
            payment.markFailedByWebhook("TossPayments 웹훅 결제 실패 이벤트가 수신되었습니다.");
            return;
        }

        if (isCanceledEvent(eventType, paymentStatus)) {
            payment.markCanceled("TossPayments 웹훅 결제 취소 이벤트가 수신되었습니다.");
        }
    }

    private boolean isSuccessEvent(String eventType, String paymentStatus) {
        return "PAYMENT_APPROVED".equals(eventType)
                || "PAYMENT_SUCCESS".equals(eventType)
                || "DONE".equals(eventType)
                || "DONE".equals(paymentStatus);
    }

    private boolean isFailedEvent(String eventType, String paymentStatus) {
        return "PAYMENT_FAILED".equals(eventType)
                || "FAILED".equals(eventType)
                || "FAILED".equals(paymentStatus)
                || "ABORTED".equals(paymentStatus)
                || "EXPIRED".equals(paymentStatus);
    }

    private boolean isCanceledEvent(String eventType, String paymentStatus) {
        return "PAYMENT_CANCELED".equals(eventType)
                || "CANCELED".equals(eventType)
                || "CANCELED".equals(paymentStatus);
    }

    private String defaultPayload(TossPaymentWebhookRequest request) {
        if (request.payload() != null && !request.payload().isBlank()) {
            return request.payload();
        }

        return """
                {"eventId":"%s","eventType":"%s","paymentKey":"%s"}
                """.formatted(
                request.eventId(),
                request.eventType(),
                request.paymentKey()
        );
    }

    private String resolvedEventId(String transmissionId, TossPaymentWebhookRequest request) {
        if (transmissionId != null && !transmissionId.isBlank()) {
            return transmissionId;
        }

        if (request.eventId() != null && !request.eventId().isBlank()) {
            return request.eventId();
        }

        throw new TossPaymentException("TossPayments 웹훅 transmission-id가 없습니다.");
    }

    private String requiredEventType(TossPaymentWebhookRequest request) {
        if (request.eventType() == null || request.eventType().isBlank()) {
            throw new TossPaymentException("TossPayments 웹훅 eventType이 없습니다.");
        }
        return request.eventType();
    }
}
