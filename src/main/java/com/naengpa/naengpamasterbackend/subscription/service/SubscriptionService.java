package com.naengpa.naengpamasterbackend.subscription.service;

import com.naengpa.naengpamasterbackend.fridge.entity.Fridge;
import com.naengpa.naengpamasterbackend.fridge.entity.FridgeMember;
import com.naengpa.naengpamasterbackend.fridge.entity.FridgeMemberStatus;
import com.naengpa.naengpamasterbackend.fridge.repository.FridgeMemberRepository;
import com.naengpa.naengpamasterbackend.fridge.service.FridgeService;
import com.naengpa.naengpamasterbackend.member.entity.Member;
import com.naengpa.naengpamasterbackend.member.repository.MemberRepository;
import com.naengpa.naengpamasterbackend.payment.repository.BillingKeyRepository;
import com.naengpa.naengpamasterbackend.subscription.dto.response.SubscriptionStatusResponse;
import com.naengpa.naengpamasterbackend.subscription.entity.Subscription;
import com.naengpa.naengpamasterbackend.subscription.entity.SubscriptionPlan;
import com.naengpa.naengpamasterbackend.subscription.entity.SubscriptionStatus;
import com.naengpa.naengpamasterbackend.subscription.repository.SubscriptionPlanRepository;
import com.naengpa.naengpamasterbackend.subscription.repository.SubscriptionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class SubscriptionService {

    private static final List<SubscriptionStatus> PREMIUM_STATUSES = List.of(
            SubscriptionStatus.TRIALING,
            SubscriptionStatus.ACTIVE
    );

    private final SubscriptionRepository subscriptionRepository;
    private final MemberRepository memberRepository;
    private final FridgeMemberRepository fridgeMemberRepository;
    private final FridgeService fridgeService;
    private final BillingKeyRepository billingKeyRepository;
    private final SubscriptionPlanRepository subscriptionPlanRepository;

    // 로그인 회원이 접근 가능한 냉장고 중 프리미엄 구독이 있는지 조회
    @Transactional
    public SubscriptionStatusResponse getMySubscription(String email) {
        Member member = findMemberByEmail(email);
        Fridge fridge = fridgeService.getOrCreateDefaultFridge(member);

        return fridgeMemberRepository.findAllByMemberIdAndStatus(member.getId(), FridgeMemberStatus.ACTIVE)
                .stream()
                .map(FridgeMember::getFridgeId)
                .distinct()
                .flatMap(fridgeId -> subscriptionRepository.findFirstByFridgeIdAndStatusInOrderBySubscriptionIdDesc(
                        fridgeId,
                        PREMIUM_STATUSES
                ).stream())
                .findFirst()
                .map(SubscriptionStatusResponse::from)
                .orElseGet(() -> SubscriptionStatusResponse.free(member.getId(), fridge.getFridgeId()));
    }

    // 가족 공유 냉장고처럼 프리미엄 권한이 필요한 기능에서 사용하는 단순 검증용 조회
    @Transactional(readOnly = true)
    public boolean hasPremiumFridge(Long fridgeId) {
        return subscriptionRepository.findFirstByFridgeIdAndStatusInOrderBySubscriptionIdDesc(
                        fridgeId,
                        PREMIUM_STATUSES
                )
                .isPresent();
    }

    // 카드 등록이 끝난 회원에게 최초 1회 무료체험 구독을 시작
    @Transactional
    public SubscriptionStatusResponse startTrial(String email) {
        Member member = findMemberByEmail(email);

        // 무료체험 종료 후 자동결제를 이어가야 하므로 활성 빌링키가 먼저 있어야 함
        if (billingKeyRepository.findFirstByMemberIdAndIsActiveTrueOrderByBillingKeyIdDesc(member.getId()).isEmpty()) {
            throw new IllegalArgumentException("등록된 결제 수단이 없습니다.");
        }

        // 무료체험은 회원당 최초 1회만 허용
        if (subscriptionRepository.existsByMemberIdAndTrialStartedAtIsNotNull(member.getId())) {
            throw new IllegalStateException("이미 무료체험을 사용한 회원입니다.");
        }

        // 이미 프리미엄 이용 중이면 같은 회원에게 TRIALING 구독을 중복 생성하지 않음
        subscriptionRepository.findFirstByMemberIdAndStatusInOrderBySubscriptionIdDesc(
                member.getId(),
                PREMIUM_STATUSES
        ).ifPresent(subscription -> {
            throw new IllegalStateException("이미 구독 중입니다.");
        });

        Fridge fridge = fridgeService.getOrCreateDefaultFridge(member);
        SubscriptionPlan plan = subscriptionPlanRepository.findByCodeAndActiveTrue("MONTHLY_PREMIUM")
                .orElseThrow(() -> new IllegalArgumentException("활성화된 구독 플랜을 찾을 수 없습니다."));

        LocalDateTime trialStartedAt = LocalDateTime.now();
        // 무료체험 일수는 코드 고정값이 아니라 subscription_plans.trial_days 정책을 따름
        LocalDateTime trialEndsAt = trialStartedAt.plusDays(plan.getTrialDays());
        Subscription subscription = subscriptionRepository.save(Subscription.createTrial(
                member.getId(),
                fridge.getFridgeId(),
                plan.getSubscriptionPlanId(),
                trialStartedAt,
                trialEndsAt
        ));

        return SubscriptionStatusResponse.from(subscription);
    }

    private Member findMemberByEmail(String email) {
        return memberRepository.findByEmail(email)
                .orElseThrow(() -> new BadCredentialsException("회원을 찾을 수 없습니다."));
    }
}
