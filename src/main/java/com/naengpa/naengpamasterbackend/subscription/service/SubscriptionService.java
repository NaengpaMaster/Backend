package com.naengpa.naengpamasterbackend.subscription.service;

import com.naengpa.naengpamasterbackend.fridge.entity.Fridge;
import com.naengpa.naengpamasterbackend.fridge.entity.FridgeMember;
import com.naengpa.naengpamasterbackend.fridge.entity.FridgeMemberStatus;
import com.naengpa.naengpamasterbackend.fridge.repository.FridgeMemberRepository;
import com.naengpa.naengpamasterbackend.fridge.service.FridgeService;
import com.naengpa.naengpamasterbackend.member.entity.Member;
import com.naengpa.naengpamasterbackend.member.repository.MemberRepository;
import com.naengpa.naengpamasterbackend.subscription.dto.response.SubscriptionStatusResponse;
import com.naengpa.naengpamasterbackend.subscription.entity.Subscription;
import com.naengpa.naengpamasterbackend.subscription.entity.SubscriptionStatus;
import com.naengpa.naengpamasterbackend.subscription.repository.SubscriptionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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

    @Transactional(readOnly = true)
    public boolean hasPremiumFridge(Long fridgeId) {
        return subscriptionRepository.findFirstByFridgeIdAndStatusInOrderBySubscriptionIdDesc(
                        fridgeId,
                        PREMIUM_STATUSES
                )
                .isPresent();
    }

    private Member findMemberByEmail(String email) {
        return memberRepository.findByEmail(email)
                .orElseThrow(() -> new BadCredentialsException("회원을 찾을 수 없습니다."));
    }
}
