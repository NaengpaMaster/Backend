package com.naengpa.naengpamasterbackend.subscription;

import com.naengpa.naengpamasterbackend.fridge.entity.Fridge;
import com.naengpa.naengpamasterbackend.fridge.repository.FridgeMemberRepository;
import com.naengpa.naengpamasterbackend.fridge.service.FridgeService;
import com.naengpa.naengpamasterbackend.member.entity.HouseholdType;
import com.naengpa.naengpamasterbackend.member.entity.Member;
import com.naengpa.naengpamasterbackend.member.repository.MemberRepository;
import com.naengpa.naengpamasterbackend.payment.entity.BillingKey;
import com.naengpa.naengpamasterbackend.payment.repository.BillingKeyRepository;
import com.naengpa.naengpamasterbackend.subscription.entity.Subscription;
import com.naengpa.naengpamasterbackend.subscription.entity.SubscriptionPlan;
import com.naengpa.naengpamasterbackend.subscription.entity.SubscriptionStatus;
import com.naengpa.naengpamasterbackend.subscription.repository.SubscriptionPlanRepository;
import com.naengpa.naengpamasterbackend.subscription.repository.SubscriptionRepository;
import com.naengpa.naengpamasterbackend.subscription.service.SubscriptionService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class SubscriptionServiceTests {

    private SubscriptionRepository subscriptionRepository;
    private MemberRepository memberRepository;
    private FridgeService fridgeService;
    private BillingKeyRepository billingKeyRepository;
    private SubscriptionPlanRepository subscriptionPlanRepository;
    private SubscriptionService subscriptionService;

    @BeforeEach
    void setUp() {
        subscriptionRepository = mock(SubscriptionRepository.class);
        memberRepository = mock(MemberRepository.class);
        fridgeService = mock(FridgeService.class);
        billingKeyRepository = mock(BillingKeyRepository.class);
        subscriptionPlanRepository = mock(SubscriptionPlanRepository.class);

        subscriptionService = new SubscriptionService(
                subscriptionRepository,
                memberRepository,
                mock(FridgeMemberRepository.class),
                fridgeService,
                billingKeyRepository,
                subscriptionPlanRepository
        );
    }

    @Test
    @DisplayName("활성 빌링키가 있으면 7일 무료체험 구독을 시작한다")
    void startTrial_createsTrialSubscription() {
        // given
        Member member = createMember(1L, "trial@test.com");
        Fridge fridge = createFridge(10L);
        SubscriptionPlan plan = createPlan(100L, 7);
        BillingKey billingKey = BillingKey.create(member.getId(), "customer-key", "billing-key", "삼성", "1234");

        when(memberRepository.findByEmail(member.getEmail())).thenReturn(Optional.of(member));
        when(billingKeyRepository.findFirstByMemberIdAndIsActiveTrueOrderByBillingKeyIdDesc(member.getId()))
                .thenReturn(Optional.of(billingKey));
        when(subscriptionRepository.existsByMemberIdAndTrialStartedAtIsNotNull(member.getId()))
                .thenReturn(false);
        when(subscriptionRepository.findFirstByMemberIdAndStatusInOrderBySubscriptionIdDesc(
                eq(member.getId()),
                eq(List.of(SubscriptionStatus.TRIALING, SubscriptionStatus.ACTIVE))
        )).thenReturn(Optional.empty());
        when(fridgeService.getOrCreateDefaultFridge(member)).thenReturn(fridge);
        when(subscriptionPlanRepository.findByCodeAndActiveTrue("MONTHLY_PREMIUM")).thenReturn(Optional.of(plan));
        when(subscriptionRepository.save(any(Subscription.class))).thenAnswer(invocation -> {
            Subscription subscription = invocation.getArgument(0);
            ReflectionTestUtils.setField(subscription, "subscriptionId", 30L);
            return subscription;
        });

        // when
        var response = subscriptionService.startTrial(member.getEmail());

        // then
        assertThat(response.subscriptionId()).isEqualTo(30L);
        assertThat(response.status()).isEqualTo(SubscriptionStatus.TRIALING);
        assertThat(response.premium()).isTrue();
        assertThat(response.trialEndsAt()).isNotNull();
        assertThat(response.nextBillingAt()).isEqualTo(response.trialEndsAt());
    }

    @Test
    @DisplayName("활성 빌링키가 없으면 무료체험을 시작할 수 없다")
    void startTrial_throwsWhenActiveBillingKeyNotFound() {
        // given
        Member member = createMember(1L, "no-card@test.com");

        when(memberRepository.findByEmail(member.getEmail())).thenReturn(Optional.of(member));
        when(billingKeyRepository.findFirstByMemberIdAndIsActiveTrueOrderByBillingKeyIdDesc(member.getId()))
                .thenReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> subscriptionService.startTrial(member.getEmail()))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("등록된 결제 수단이 없습니다.");
    }

    @Test
    @DisplayName("이미 무료체험을 사용한 회원은 다시 시작할 수 없다")
    void startTrial_throwsWhenTrialAlreadyUsed() {
        // given
        Member member = createMember(1L, "used-trial@test.com");
        BillingKey billingKey = BillingKey.create(member.getId(), "customer-key", "billing-key", "삼성", "1234");

        when(memberRepository.findByEmail(member.getEmail())).thenReturn(Optional.of(member));
        when(billingKeyRepository.findFirstByMemberIdAndIsActiveTrueOrderByBillingKeyIdDesc(member.getId()))
                .thenReturn(Optional.of(billingKey));
        when(subscriptionRepository.existsByMemberIdAndTrialStartedAtIsNotNull(member.getId()))
                .thenReturn(true);

        // when & then
        assertThatThrownBy(() -> subscriptionService.startTrial(member.getEmail()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("이미 무료체험을 사용한 회원입니다.");
    }

    private Member createMember(Long memberId, String email) {
        Member member = Member.createUser(email, "password", "무료체험유저", HouseholdType.ONE_PERSON);
        ReflectionTestUtils.setField(member, "id", memberId);
        return member;
    }

    private Fridge createFridge(Long fridgeId) {
        Fridge fridge = mock(Fridge.class);
        when(fridge.getFridgeId()).thenReturn(fridgeId);
        return fridge;
    }

    private SubscriptionPlan createPlan(Long planId, int trialDays) {
        SubscriptionPlan plan = mock(SubscriptionPlan.class);
        when(plan.getSubscriptionPlanId()).thenReturn(planId);
        when(plan.getTrialDays()).thenReturn(trialDays);
        return plan;
    }
}
