package com.naengpa.naengpamasterbackend.payment.service;

import com.naengpa.naengpamasterbackend.member.entity.Member;
import com.naengpa.naengpamasterbackend.member.repository.MemberRepository;
import com.naengpa.naengpamasterbackend.payment.dto.response.MyBillingKeyResponse;
import com.naengpa.naengpamasterbackend.payment.dto.response.MyPaymentHistoryResponse;
import com.naengpa.naengpamasterbackend.payment.repository.BillingKeyRepository;
import com.naengpa.naengpamasterbackend.payment.repository.PaymentRepository;
import com.naengpa.naengpamasterbackend.subscription.dto.response.SubscriptionStatusResponse;
import com.naengpa.naengpamasterbackend.subscription.entity.SubscriptionStatus;
import com.naengpa.naengpamasterbackend.subscription.repository.SubscriptionRepository;
import com.naengpa.naengpamasterbackend.subscription.service.SubscriptionService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class MySubscriptionBillingService {

    private static final List<SubscriptionStatus> CANCELABLE_STATUSES = List.of(
            SubscriptionStatus.TRIALING,
            SubscriptionStatus.ACTIVE
    );

    private final MemberRepository memberRepository;
    private final BillingKeyRepository billingKeyRepository;
    private final PaymentRepository paymentRepository;
    private final SubscriptionRepository subscriptionRepository;
    private final SubscriptionService subscriptionService;

    @Transactional(readOnly = true)
    public MyBillingKeyResponse getMyBillingKey(String email) {
        Member member = findMemberByEmail(email);

        // 활성 카드 1개만 화면에 노출하고 빌링키 원문은 숨김
        return billingKeyRepository.findFirstByMemberIdAndIsActiveTrueOrderByBillingKeyIdDesc(member.getId())
                .map(MyBillingKeyResponse::from)
                .orElse(null);
    }

    @Transactional(readOnly = true)
    public List<MyPaymentHistoryResponse> getMyPayments(String email) {
        Member member = findMemberByEmail(email);

        // 결제 내역은 최신순으로 그대로 내려주고 화면에서 상태 라벨만 바꿈
        return paymentRepository.findAllByMemberIdOrderByCreatedAtDesc(member.getId())
                .stream()
                .map(MyPaymentHistoryResponse::from)
                .toList();
    }

    @Transactional
    public SubscriptionStatusResponse cancelMySubscription(String email) {
        Member member = findMemberByEmail(email);

        // 즉시 해지가 아니라 다음 결제만 중단하므로 status는 유지
        subscriptionRepository.findFirstByMemberIdAndStatusInOrderBySubscriptionIdDesc(
                        member.getId(),
                        CANCELABLE_STATUSES
                )
                .ifPresent(subscription -> {
                    if (!subscription.isCancelReserved()) {
                        subscription.reserveCancel();
                    }
                });

        return subscriptionService.getMySubscription(email);
    }

    private Member findMemberByEmail(String email) {
        return memberRepository.findByEmail(email)
                .orElseThrow(() -> new BadCredentialsException("회원을 찾을 수 없습니다."));
    }
}
