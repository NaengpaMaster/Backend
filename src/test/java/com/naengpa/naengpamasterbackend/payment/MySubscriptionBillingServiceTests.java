package com.naengpa.naengpamasterbackend.payment;

import com.naengpa.naengpamasterbackend.global.exception.SubscriptionNotFoundException;
import com.naengpa.naengpamasterbackend.member.entity.HouseholdType;
import com.naengpa.naengpamasterbackend.member.entity.Member;
import com.naengpa.naengpamasterbackend.member.repository.MemberRepository;
import com.naengpa.naengpamasterbackend.payment.repository.BillingKeyRepository;
import com.naengpa.naengpamasterbackend.payment.repository.PaymentRepository;
import com.naengpa.naengpamasterbackend.payment.service.MySubscriptionBillingService;
import com.naengpa.naengpamasterbackend.subscription.dto.response.SubscriptionStatusResponse;
import com.naengpa.naengpamasterbackend.subscription.entity.Subscription;
import com.naengpa.naengpamasterbackend.subscription.entity.SubscriptionStatus;
import com.naengpa.naengpamasterbackend.subscription.repository.SubscriptionRepository;
import com.naengpa.naengpamasterbackend.subscription.service.SubscriptionService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class MySubscriptionBillingServiceTests {

    private MemberRepository memberRepository;
    private SubscriptionRepository subscriptionRepository;
    private SubscriptionService subscriptionService;
    private MySubscriptionBillingService mySubscriptionBillingService;

    @BeforeEach
    void setUp() {
        memberRepository = mock(MemberRepository.class);
        subscriptionRepository = mock(SubscriptionRepository.class);
        subscriptionService = mock(SubscriptionService.class);

        mySubscriptionBillingService = new MySubscriptionBillingService(
                memberRepository,
                mock(BillingKeyRepository.class),
                mock(PaymentRepository.class),
                subscriptionRepository,
                subscriptionService
        );
    }

    @Test
    @DisplayName("구독 해지 예약 시 다음 자동결제를 중단하고 현재 구독 상태를 반환한다")
    void cancelMySubscription_reservesCancel() {
        // given
        Member member = createMember(1L, "cancel@test.com");
        Subscription subscription = createActiveSubscription(member.getId());

        when(memberRepository.findByEmail(member.getEmail())).thenReturn(Optional.of(member));
        when(subscriptionRepository.findFirstByMemberIdAndStatusInOrderBySubscriptionIdDesc(
                eq(member.getId()),
                eq(List.of(SubscriptionStatus.TRIALING, SubscriptionStatus.ACTIVE))
        )).thenReturn(Optional.of(subscription));
        when(subscriptionService.getMySubscription(member.getEmail())).thenReturn(mock(SubscriptionStatusResponse.class));

        // when
        mySubscriptionBillingService.cancelMySubscription(member.getEmail());

        // then
        verify(subscriptionService).getMySubscription(member.getEmail());
        assertThat(subscription.isCancelReserved()).isTrue();
    }

    @Test
    @DisplayName("이미 해지 예약된 구독은 중복 해지 요청을 막는다")
    void cancelMySubscription_throwsWhenAlreadyReserved() {
        // given
        Member member = createMember(1L, "already-canceled@test.com");
        Subscription subscription = createActiveSubscription(member.getId());
        subscription.reserveCancel();

        when(memberRepository.findByEmail(member.getEmail())).thenReturn(Optional.of(member));
        when(subscriptionRepository.findFirstByMemberIdAndStatusInOrderBySubscriptionIdDesc(
                eq(member.getId()),
                eq(List.of(SubscriptionStatus.TRIALING, SubscriptionStatus.ACTIVE))
        )).thenReturn(Optional.of(subscription));

        // when & then
        assertThatThrownBy(() -> mySubscriptionBillingService.cancelMySubscription(member.getEmail()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("이미 해지 예약된 구독입니다.");
    }

    @Test
    @DisplayName("해지 예약 취소 시 canceledAt을 비우고 nextBillingAt을 이용 종료일로 복구한다")
    void revokeCancelMySubscription_restoresNextBillingAt() {
        // given
        Member member = createMember(1L, "revoke@test.com");
        Subscription subscription = createActiveSubscription(member.getId());
        subscription.reserveCancel();

        when(memberRepository.findByEmail(member.getEmail())).thenReturn(Optional.of(member));
        when(subscriptionRepository.findFirstByMemberIdAndStatusInOrderBySubscriptionIdDesc(
                eq(member.getId()),
                eq(List.of(SubscriptionStatus.TRIALING, SubscriptionStatus.ACTIVE))
        )).thenReturn(Optional.of(subscription));
        when(subscriptionService.getMySubscription(member.getEmail())).thenReturn(mock(SubscriptionStatusResponse.class));

        // when
        mySubscriptionBillingService.revokeCancelMySubscription(member.getEmail());

        // then
        verify(subscriptionService).getMySubscription(member.getEmail());
        assertThat(subscription.isCancelReserved()).isFalse();
    }

    @Test
    @DisplayName("구독이 없으면 해지 예약할 수 없다")
    void cancelMySubscription_throwsWhenSubscriptionNotFound() {
        // given
        Member member = createMember(1L, "none@test.com");

        when(memberRepository.findByEmail(member.getEmail())).thenReturn(Optional.of(member));
        when(subscriptionRepository.findFirstByMemberIdAndStatusInOrderBySubscriptionIdDesc(
                eq(member.getId()),
                eq(List.of(SubscriptionStatus.TRIALING, SubscriptionStatus.ACTIVE))
        )).thenReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> mySubscriptionBillingService.cancelMySubscription(member.getEmail()))
                .isInstanceOf(SubscriptionNotFoundException.class)
                .hasMessage("구독 정보를 찾을 수 없습니다.");
    }

    private Member createMember(Long memberId, String email) {
        Member member = Member.createUser(email, "password", "구독관리유저", HouseholdType.ONE_PERSON);
        ReflectionTestUtils.setField(member, "id", memberId);
        return member;
    }

    private Subscription createActiveSubscription(Long memberId) {
        Subscription subscription = Subscription.createActive(
                memberId,
                10L,
                100L,
                LocalDateTime.now(),
                LocalDateTime.now().plusMonths(1),
                LocalDateTime.now().plusMonths(1)
        );
        ReflectionTestUtils.setField(subscription, "subscriptionId", 30L);
        return subscription;
    }
}
