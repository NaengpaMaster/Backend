package com.naengpa.naengpamasterbackend.payment;

import com.naengpa.naengpamasterbackend.payment.dto.request.TossPaymentWebhookRequest;
import com.naengpa.naengpamasterbackend.payment.entity.Payment;
import com.naengpa.naengpamasterbackend.payment.entity.PaymentPlanType;
import com.naengpa.naengpamasterbackend.payment.entity.PaymentStatus;
import com.naengpa.naengpamasterbackend.payment.entity.PaymentWebhookEvent;
import com.naengpa.naengpamasterbackend.payment.repository.PaymentRepository;
import com.naengpa.naengpamasterbackend.payment.repository.PaymentWebhookEventRepository;
import com.naengpa.naengpamasterbackend.payment.service.TossPaymentWebhookService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class TossPaymentWebhookServiceTests {

    private PaymentWebhookEventRepository paymentWebhookEventRepository;
    private PaymentRepository paymentRepository;
    private TossPaymentWebhookService tossPaymentWebhookService;

    @BeforeEach
    void setUp() {
        paymentWebhookEventRepository = mock(PaymentWebhookEventRepository.class);
        paymentRepository = mock(PaymentRepository.class);
        tossPaymentWebhookService = new TossPaymentWebhookService(paymentWebhookEventRepository, paymentRepository);
    }

    @Test
    @DisplayName("처음 수신한 성공 웹훅은 이벤트를 저장하고 결제 상태를 SUCCESS로 동기화한다")
    void handleWebhook_savesEventAndMarksPaymentSuccess() {
        // given
        Payment payment = createReadyPayment();
        payment.markSuccess("payment-key", null);

        when(paymentWebhookEventRepository.existsByEventId("event-1")).thenReturn(false);
        when(paymentWebhookEventRepository.save(any(PaymentWebhookEvent.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));
        when(paymentRepository.findByTossPaymentKey("payment-key")).thenReturn(Optional.of(payment));

        // when
        tossPaymentWebhookService.handleWebhook("event-1", new TossPaymentWebhookRequest(
                "event-1",
                "PAYMENT_APPROVED",
                "payment-key",
                "{}",
                null
        ));

        // then
        assertThat(payment.getStatus()).isEqualTo(PaymentStatus.SUCCESS);
        verify(paymentWebhookEventRepository).save(any(PaymentWebhookEvent.class));
    }

    @Test
    @DisplayName("이미 수신한 eventId면 이벤트 저장과 결제 상태 변경을 하지 않는다")
    void handleWebhook_ignoresDuplicatedEventId() {
        // given
        when(paymentWebhookEventRepository.existsByEventId("event-1")).thenReturn(true);

        // when
        tossPaymentWebhookService.handleWebhook("event-1", new TossPaymentWebhookRequest(
                "event-1",
                "PAYMENT_CANCELED",
                "payment-key",
                "{}",
                null
        ));

        // then
        verify(paymentWebhookEventRepository, never()).save(any(PaymentWebhookEvent.class));
        verify(paymentRepository, never()).findByTossPaymentKey("payment-key");
    }

    @Test
    @DisplayName("취소 웹훅은 결제 상태를 CANCELED로 동기화한다")
    void handleWebhook_marksPaymentCanceled() {
        // given
        Payment payment = createReadyPayment();
        payment.markSuccess("payment-key", null);

        when(paymentWebhookEventRepository.existsByEventId("event-cancel")).thenReturn(false);
        when(paymentWebhookEventRepository.save(any(PaymentWebhookEvent.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));
        when(paymentRepository.findByTossPaymentKey("payment-key")).thenReturn(Optional.of(payment));

        // when
        tossPaymentWebhookService.handleWebhook("event-cancel", new TossPaymentWebhookRequest(
                "event-cancel",
                "PAYMENT_CANCELED",
                "payment-key",
                "{}",
                null
        ));

        // then
        assertThat(payment.getStatus()).isEqualTo(PaymentStatus.CANCELED);
    }

    @Test
    @DisplayName("Toss 실제 PAYMENT_STATUS_CHANGED 웹훅은 data.paymentKey와 data.status로 결제 상태를 반영한다")
    void handleWebhook_usesTossDataPayload() {
        // given
        Payment payment = createReadyPayment();
        payment.markSuccess("payment-key", null);

        when(paymentWebhookEventRepository.existsByEventId("transmission-1")).thenReturn(false);
        when(paymentWebhookEventRepository.save(any(PaymentWebhookEvent.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));
        when(paymentRepository.findByTossPaymentKey("payment-key")).thenReturn(Optional.of(payment));

        // when
        tossPaymentWebhookService.handleWebhook("transmission-1", new TossPaymentWebhookRequest(
                null,
                "PAYMENT_STATUS_CHANGED",
                null,
                null,
                Map.of(
                        "paymentKey", "payment-key",
                        "status", "CANCELED"
                )
        ));

        // then
        assertThat(payment.getStatus()).isEqualTo(PaymentStatus.CANCELED);
    }

    private Payment createReadyPayment() {
        return Payment.ready(
                1L,
                10L,
                "order-id",
                "냉파마스터 월간 구독",
                2900,
                PaymentPlanType.MONTHLY,
                LocalDate.now(),
                LocalDate.now().plusMonths(1)
        );
    }
}
