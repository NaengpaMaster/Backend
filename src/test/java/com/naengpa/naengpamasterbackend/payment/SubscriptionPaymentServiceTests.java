package com.naengpa.naengpamasterbackend.payment;

import com.naengpa.naengpamasterbackend.fridge.entity.Fridge;
import com.naengpa.naengpamasterbackend.fridge.service.FridgeService;
import com.naengpa.naengpamasterbackend.member.entity.HouseholdType;
import com.naengpa.naengpamasterbackend.member.entity.Member;
import com.naengpa.naengpamasterbackend.member.repository.MemberRepository;
import com.naengpa.naengpamasterbackend.payment.client.TossBillingClient;
import com.naengpa.naengpamasterbackend.payment.client.TossBillingClient.TossBillingPaymentResponse;
import com.naengpa.naengpamasterbackend.payment.dto.request.SubscriptionPaymentRequest;
import com.naengpa.naengpamasterbackend.payment.entity.BillingKey;
import com.naengpa.naengpamasterbackend.payment.entity.Payment;
import com.naengpa.naengpamasterbackend.payment.entity.PaymentPlanType;
import com.naengpa.naengpamasterbackend.payment.entity.PaymentStatus;
import com.naengpa.naengpamasterbackend.payment.exception.TossPaymentException;
import com.naengpa.naengpamasterbackend.payment.repository.BillingKeyRepository;
import com.naengpa.naengpamasterbackend.payment.repository.PaymentRepository;
import com.naengpa.naengpamasterbackend.payment.service.SubscriptionPaymentService;
import com.naengpa.naengpamasterbackend.subscription.entity.BillingPeriod;
import com.naengpa.naengpamasterbackend.subscription.entity.Subscription;
import com.naengpa.naengpamasterbackend.subscription.entity.SubscriptionPlan;
import com.naengpa.naengpamasterbackend.subscription.entity.SubscriptionStatus;
import com.naengpa.naengpamasterbackend.subscription.repository.SubscriptionPlanRepository;
import com.naengpa.naengpamasterbackend.subscription.repository.SubscriptionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class SubscriptionPaymentServiceTests {

    private MemberRepository memberRepository;
    private BillingKeyRepository billingKeyRepository;
    private PaymentRepository paymentRepository;
    private SubscriptionPlanRepository subscriptionPlanRepository;
    private SubscriptionRepository subscriptionRepository;
    private FridgeService fridgeService;
    private TossBillingClient tossBillingClient;
    private SubscriptionPaymentService subscriptionPaymentService;

    @BeforeEach
    void setUp() {
        memberRepository = mock(MemberRepository.class);
        billingKeyRepository = mock(BillingKeyRepository.class);
        paymentRepository = mock(PaymentRepository.class);
        subscriptionPlanRepository = mock(SubscriptionPlanRepository.class);
        subscriptionRepository = mock(SubscriptionRepository.class);
        fridgeService = mock(FridgeService.class);
        tossBillingClient = mock(TossBillingClient.class);

        subscriptionPaymentService = new SubscriptionPaymentService(
                memberRepository,
                billingKeyRepository,
                paymentRepository,
                subscriptionPlanRepository,
                subscriptionRepository,
                fridgeService,
                tossBillingClient
        );
    }

    @Test
    @DisplayName("구독 자동결제 성공 시 결제 성공 로그를 남기고 구독을 ACTIVE로 생성한다")
    void approveSubscriptionPayment_savesSuccessPaymentAndActivatesSubscription() {
        // given
        Member member = createMember(1L, "pay@test.com");
        BillingKey billingKey = createBillingKey(member.getId());
        SubscriptionPlan plan = createPlan(10L, 2900, BillingPeriod.MONTH, 1);
        Fridge fridge = createFridge(100L);
        TossBillingPaymentResponse tossResponse = createTossPaymentResponse("payment-key");

        when(memberRepository.findByEmail(member.getEmail())).thenReturn(Optional.of(member));
        when(subscriptionRepository.findFirstByMemberIdAndStatusInOrderBySubscriptionIdDesc(
                eq(member.getId()),
                eq(List.of(SubscriptionStatus.CANCELED))
        )).thenReturn(Optional.empty());
        when(billingKeyRepository.findFirstByMemberIdAndIsActiveTrueOrderByBillingKeyIdDesc(member.getId()))
                .thenReturn(Optional.of(billingKey));
        when(subscriptionPlanRepository.findByCodeAndActiveTrue("MONTHLY_PREMIUM")).thenReturn(Optional.of(plan));
        when(paymentRepository.save(any(Payment.class))).thenAnswer(invocation -> {
            Payment payment = invocation.getArgument(0);
            ReflectionTestUtils.setField(payment, "paymentId", 1L);
            return payment;
        });
        when(tossBillingClient.approveBillingPayment(
                eq(billingKey.getTossBillingKey()),
                eq(billingKey.getTossCustomerKey()),
                any(String.class),
                eq("냉파마스터 월간 구독"),
                eq(2900)
        )).thenReturn(tossResponse);
        when(fridgeService.getOrCreateDefaultFridge(member)).thenReturn(fridge);
        when(subscriptionRepository.findFirstByMemberIdAndStatusInOrderBySubscriptionIdDesc(
                eq(member.getId()),
                eq(List.of(SubscriptionStatus.TRIALING, SubscriptionStatus.ACTIVE))
        )).thenReturn(Optional.empty());

        // when
        var response = subscriptionPaymentService.approveSubscriptionPayment(
                member.getEmail(),
                new SubscriptionPaymentRequest(PaymentPlanType.MONTHLY)
        );

        // then
        assertThat(response.paymentId()).isEqualTo(1L);
        assertThat(response.amount()).isEqualTo(2900);
        assertThat(response.planType()).isEqualTo(PaymentPlanType.MONTHLY);
        assertThat(response.status()).isEqualTo(PaymentStatus.SUCCESS);
        verify(subscriptionRepository).save(any(Subscription.class));
    }

    @Test
    @DisplayName("Toss 자동결제 실패 시 결제 실패 로그를 남기고 예외를 전달한다")
    void approveSubscriptionPayment_marksPaymentRetryingWhenTossFails() {
        // given
        Member member = createMember(1L, "pay-fail@test.com");
        BillingKey billingKey = createBillingKey(member.getId());
        SubscriptionPlan plan = createPlan(10L, 2900, BillingPeriod.MONTH, 1);

        when(memberRepository.findByEmail(member.getEmail())).thenReturn(Optional.of(member));
        when(subscriptionRepository.findFirstByMemberIdAndStatusInOrderBySubscriptionIdDesc(
                eq(member.getId()),
                eq(List.of(SubscriptionStatus.CANCELED))
        )).thenReturn(Optional.empty());
        when(billingKeyRepository.findFirstByMemberIdAndIsActiveTrueOrderByBillingKeyIdDesc(member.getId()))
                .thenReturn(Optional.of(billingKey));
        when(subscriptionPlanRepository.findByCodeAndActiveTrue("MONTHLY_PREMIUM")).thenReturn(Optional.of(plan));
        when(paymentRepository.save(any(Payment.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(tossBillingClient.approveBillingPayment(
                eq(billingKey.getTossBillingKey()),
                eq(billingKey.getTossCustomerKey()),
                any(String.class),
                eq("냉파마스터 월간 구독"),
                eq(2900)
        )).thenThrow(new TossPaymentException("TossPayments 자동결제 승인에 실패했습니다."));

        // when & then
        assertThatThrownBy(() -> subscriptionPaymentService.approveSubscriptionPayment(
                member.getEmail(),
                new SubscriptionPaymentRequest(PaymentPlanType.MONTHLY)
        ))
                .isInstanceOf(TossPaymentException.class)
                .hasMessage("TossPayments 자동결제 승인에 실패했습니다.");
    }

    @Test
    @DisplayName("스케줄러 자동결제 성공 시 결제 성공 이력을 남기고 구독 기간을 갱신한다")
    void processAutoBilling_succeedsAndRenewsSubscription() {
        // given
        Member member = createMember(1L, "auto-success@test.com");
        BillingKey billingKey = createBillingKey(member.getId());
        SubscriptionPlan plan = createPlan(10L, 2900, BillingPeriod.MONTH, 1);
        Subscription subscription = createTrialSubscription(member.getId(), plan.getSubscriptionPlanId());
        TossBillingPaymentResponse tossResponse = createTossPaymentResponse("auto-payment-key");

        when(memberRepository.findById(member.getId())).thenReturn(Optional.of(member));
        when(subscriptionPlanRepository.findById(plan.getSubscriptionPlanId())).thenReturn(Optional.of(plan));
        when(paymentRepository.countByMemberIdAndBillingPeriodStartAndBillingPeriodEndAndStatusIn(
                eq(member.getId()),
                any(),
                any(),
                eq(List.of(PaymentStatus.RETRYING, PaymentStatus.FAILED))
        )).thenReturn(0L);
        when(paymentRepository.save(any(Payment.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(billingKeyRepository.findFirstByMemberIdAndIsActiveTrueOrderByBillingKeyIdDesc(member.getId()))
                .thenReturn(Optional.of(billingKey));
        when(tossBillingClient.approveBillingPayment(
                eq(billingKey.getTossBillingKey()),
                eq(billingKey.getTossCustomerKey()),
                any(String.class),
                eq("냉파마스터 월간 구독"),
                eq(2900)
        )).thenReturn(tossResponse);

        // when
        subscriptionPaymentService.processAutoBilling(subscription);

        // then
        assertThat(subscription.getStatus()).isEqualTo(SubscriptionStatus.ACTIVE);
        assertThat(subscription.getCurrentPeriodEndAt()).isNotNull();
        assertThat(subscription.getNextBillingAt()).isEqualTo(subscription.getCurrentPeriodEndAt());
    }

    @Test
    @DisplayName("스케줄러 자동결제 시 활성 빌링키가 없으면 실패 이력을 남긴다")
    void processAutoBilling_marksPaymentFailedWhenBillingKeyNotFound() {
        // given
        Member member = createMember(1L, "auto-no-card@test.com");
        SubscriptionPlan plan = createPlan(10L, 2900, BillingPeriod.MONTH, 1);
        Subscription subscription = createTrialSubscription(member.getId(), plan.getSubscriptionPlanId());

        when(memberRepository.findById(member.getId())).thenReturn(Optional.of(member));
        when(subscriptionPlanRepository.findById(plan.getSubscriptionPlanId())).thenReturn(Optional.of(plan));
        when(paymentRepository.countByMemberIdAndBillingPeriodStartAndBillingPeriodEndAndStatusIn(
                eq(member.getId()),
                any(),
                any(),
                eq(List.of(PaymentStatus.RETRYING, PaymentStatus.FAILED))
        )).thenReturn(0L);
        when(paymentRepository.save(any(Payment.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(billingKeyRepository.findFirstByMemberIdAndIsActiveTrueOrderByBillingKeyIdDesc(member.getId()))
                .thenReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> subscriptionPaymentService.processAutoBilling(subscription))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("등록된 결제 수단이 없습니다.");
    }

    @Test
    @DisplayName("스케줄러 자동결제가 3회째 실패하면 구독을 EXPIRED 처리한다")
    void processAutoBilling_expiresSubscriptionWhenThirdFailure() {
        // given
        Member member = createMember(1L, "auto-third-fail@test.com");
        SubscriptionPlan plan = createPlan(10L, 2900, BillingPeriod.MONTH, 1);
        Subscription subscription = createTrialSubscription(member.getId(), plan.getSubscriptionPlanId());

        when(memberRepository.findById(member.getId())).thenReturn(Optional.of(member));
        when(subscriptionPlanRepository.findById(plan.getSubscriptionPlanId())).thenReturn(Optional.of(plan));
        when(paymentRepository.countByMemberIdAndBillingPeriodStartAndBillingPeriodEndAndStatusIn(
                eq(member.getId()),
                any(),
                any(),
                eq(List.of(PaymentStatus.RETRYING, PaymentStatus.FAILED))
        )).thenReturn(2L);
        when(paymentRepository.save(any(Payment.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(billingKeyRepository.findFirstByMemberIdAndIsActiveTrueOrderByBillingKeyIdDesc(member.getId()))
                .thenReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> subscriptionPaymentService.processAutoBilling(subscription))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("등록된 결제 수단이 없습니다.");
        assertThat(subscription.getStatus()).isEqualTo(SubscriptionStatus.EXPIRED);
        assertThat(subscription.getNextBillingAt()).isNull();
    }

    private Member createMember(Long memberId, String email) {
        Member member = Member.createUser(
                email,
                "password",
                "결제테스트유저",
                HouseholdType.ONE_PERSON
        );
        ReflectionTestUtils.setField(member, "id", memberId);
        return member;
    }

    private BillingKey createBillingKey(Long memberId) {
        BillingKey billingKey = BillingKey.create(
                memberId,
                "customer-key",
                "billing-key",
                "삼성",
                "123456******7890"
        );
        ReflectionTestUtils.setField(billingKey, "billingKeyId", 20L);
        return billingKey;
    }

    private SubscriptionPlan createPlan(Long planId, int price, BillingPeriod billingPeriod, int billingInterval) {
        SubscriptionPlan plan = mock(SubscriptionPlan.class);
        when(plan.getSubscriptionPlanId()).thenReturn(planId);
        when(plan.getPrice()).thenReturn(price);
        when(plan.getBillingPeriod()).thenReturn(billingPeriod);
        when(plan.getBillingInterval()).thenReturn(billingInterval);
        return plan;
    }

    private Fridge createFridge(Long fridgeId) {
        Fridge fridge = mock(Fridge.class);
        when(fridge.getFridgeId()).thenReturn(fridgeId);
        return fridge;
    }

    private Subscription createTrialSubscription(Long memberId, Long planId) {
        LocalDateTime now = LocalDateTime.now();
        Subscription subscription = Subscription.createTrial(
                memberId,
                100L,
                planId,
                now.minusDays(7),
                now
        );
        ReflectionTestUtils.setField(subscription, "subscriptionId", 30L);
        return subscription;
    }

    private TossBillingPaymentResponse createTossPaymentResponse(String paymentKey) {
        TossBillingPaymentResponse response = new TossBillingPaymentResponse();
        ReflectionTestUtils.setField(response, "paymentKey", paymentKey);
        ReflectionTestUtils.setField(response, "approvedAt", "2026-08-12T10:00:00+09:00");
        return response;
    }
}
