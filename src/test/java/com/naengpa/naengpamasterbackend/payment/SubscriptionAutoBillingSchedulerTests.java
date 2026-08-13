package com.naengpa.naengpamasterbackend.payment;

import com.naengpa.naengpamasterbackend.payment.scheduler.SubscriptionAutoBillingScheduler;
import com.naengpa.naengpamasterbackend.payment.service.SubscriptionPaymentService;
import com.naengpa.naengpamasterbackend.subscription.entity.Subscription;
import com.naengpa.naengpamasterbackend.subscription.entity.SubscriptionStatus;
import com.naengpa.naengpamasterbackend.subscription.repository.SubscriptionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class SubscriptionAutoBillingSchedulerTests {

    private SubscriptionRepository subscriptionRepository;
    private SubscriptionPaymentService subscriptionPaymentService;
    private SubscriptionAutoBillingScheduler scheduler;

    @BeforeEach
    void setUp() {
        subscriptionRepository = mock(SubscriptionRepository.class);
        subscriptionPaymentService = mock(SubscriptionPaymentService.class);
        scheduler = new SubscriptionAutoBillingScheduler(subscriptionRepository, subscriptionPaymentService);
    }

    @Test
    @DisplayName("자동결제 대상 구독을 조회하고 구독별 자동결제를 실행한다")
    void runAutoBilling_processesDueSubscriptions() {
        // given
        Subscription first = createSubscription(1L, 10L);
        Subscription second = createSubscription(2L, 20L);

        when(subscriptionRepository.findAllByStatusInAndNextBillingAtLessThanEqualAndCanceledAtIsNull(
                eq(List.of(SubscriptionStatus.TRIALING, SubscriptionStatus.ACTIVE)),
                any(LocalDateTime.class)
        )).thenReturn(List.of(first, second));

        // when
        scheduler.runAutoBilling();

        // then
        verify(subscriptionPaymentService).processAutoBilling(first);
        verify(subscriptionPaymentService).processAutoBilling(second);
    }

    @Test
    @DisplayName("한 구독 자동결제가 실패해도 다음 구독 처리를 계속한다")
    void runAutoBilling_continuesWhenOneSubscriptionFails() {
        // given
        Subscription first = createSubscription(1L, 10L);
        Subscription second = createSubscription(2L, 20L);

        when(subscriptionRepository.findAllByStatusInAndNextBillingAtLessThanEqualAndCanceledAtIsNull(
                eq(List.of(SubscriptionStatus.TRIALING, SubscriptionStatus.ACTIVE)),
                any(LocalDateTime.class)
        )).thenReturn(List.of(first, second));
        doThrow(new IllegalArgumentException("등록된 결제 수단이 없습니다."))
                .when(subscriptionPaymentService)
                .processAutoBilling(first);

        // when
        scheduler.runAutoBilling();

        // then
        verify(subscriptionPaymentService).processAutoBilling(first);
        verify(subscriptionPaymentService).processAutoBilling(second);
    }

    private Subscription createSubscription(Long subscriptionId, Long memberId) {
        Subscription subscription = Subscription.createTrial(
                memberId,
                100L + subscriptionId,
                10L,
                LocalDateTime.now().minusDays(7),
                LocalDateTime.now()
        );
        ReflectionTestUtils.setField(subscription, "subscriptionId", subscriptionId);
        return subscription;
    }
}
