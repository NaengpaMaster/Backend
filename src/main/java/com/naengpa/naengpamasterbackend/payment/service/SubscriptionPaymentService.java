package com.naengpa.naengpamasterbackend.payment.service;

import com.naengpa.naengpamasterbackend.fridge.entity.Fridge;
import com.naengpa.naengpamasterbackend.fridge.service.FridgeService;
import com.naengpa.naengpamasterbackend.member.entity.Member;
import com.naengpa.naengpamasterbackend.member.repository.MemberRepository;
import com.naengpa.naengpamasterbackend.payment.client.TossBillingClient;
import com.naengpa.naengpamasterbackend.payment.client.TossBillingClient.TossBillingPaymentResponse;
import com.naengpa.naengpamasterbackend.payment.dto.request.SubscriptionPaymentRequest;
import com.naengpa.naengpamasterbackend.payment.dto.response.SubscriptionPaymentResponse;
import com.naengpa.naengpamasterbackend.payment.entity.BillingKey;
import com.naengpa.naengpamasterbackend.payment.entity.Payment;
import com.naengpa.naengpamasterbackend.payment.entity.PaymentPlanType;
import com.naengpa.naengpamasterbackend.payment.exception.TossPaymentException;
import com.naengpa.naengpamasterbackend.payment.repository.BillingKeyRepository;
import com.naengpa.naengpamasterbackend.payment.repository.PaymentRepository;
import com.naengpa.naengpamasterbackend.subscription.entity.BillingPeriod;
import com.naengpa.naengpamasterbackend.subscription.entity.Subscription;
import com.naengpa.naengpamasterbackend.subscription.entity.SubscriptionPlan;
import com.naengpa.naengpamasterbackend.subscription.entity.SubscriptionStatus;
import com.naengpa.naengpamasterbackend.subscription.repository.SubscriptionPlanRepository;
import com.naengpa.naengpamasterbackend.subscription.repository.SubscriptionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class SubscriptionPaymentService {

    private final MemberRepository memberRepository;
    private final BillingKeyRepository billingKeyRepository;
    private final PaymentRepository paymentRepository;
    private final SubscriptionPlanRepository subscriptionPlanRepository;
    private final SubscriptionRepository subscriptionRepository;
    private final FridgeService fridgeService;
    private final TossBillingClient tossBillingClient;

    // 저장된 빌링키로 Toss 자동결제를 승인하고, 성공하면 구독 상태를 ACTIVE로 갱신
    @Transactional
    public SubscriptionPaymentResponse approveSubscriptionPayment(String email, SubscriptionPaymentRequest request) {
        Member member = findMemberByEmail(email);

        // 구독 취소자는 현재 결제 기간까지만 사용하고 다음 자동결제는 진행하지 않는다.
        validateNotCanceled(member.getId());

        // #328에서 등록한 활성 결제 수단을 가져온다.
        BillingKey billingKey = findActiveBillingKey(member.getId());

        // 결제 금액은 프론트 요청값이 아니라 DB에 저장된 구독 플랜 가격을 기준으로 한다.
        SubscriptionPlan plan = findPlan(request.planType());

        // 이번 결제가 보장하는 구독 기간을 계산한다.
        LocalDate periodStart = LocalDate.now();
        LocalDate periodEnd = calculatePeriodEnd(periodStart, plan);

        // Toss 호출 전에 READY 결제 이력을 먼저 저장한다.
        // 이렇게 해야 Toss 호출 실패 시에도 실패 로그를 남길 수 있다.
        Payment payment = paymentRepository.save(Payment.ready(
                member.getId(),
                billingKey.getBillingKeyId(),
                createOrderId(),
                createOrderName(request.planType()),
                plan.getPrice(),
                request.planType(),
                periodStart,
                periodEnd
        ));

        try {
            // 저장된 billingKey로 Toss 자동결제 승인 API를 호출한다.
            TossBillingPaymentResponse tossResponse = tossBillingClient.approveBillingPayment(
                    billingKey.getTossBillingKey(),
                    billingKey.getTossCustomerKey(),
                    payment.getOrderId(),
                    payment.getOrderName(),
                    payment.getAmount()
            );

            // Toss 승인 성공 결과를 payments에 반영한다.
            payment.markSuccess(
                    tossResponse.getPaymentKey(),
                    parseApprovedAt(tossResponse.getApprovedAt())
            );

            // 결제 성공 후 구독을 ACTIVE 상태로 생성하거나 갱신한다.
            activateSubscription(member, plan, periodStart, periodEnd);

            return SubscriptionPaymentResponse.from(payment);
        } catch (TossPaymentException exception) {
            // Toss 결제 실패도 payments에 남긴다. 3회 미만이면 RETRYING, 3회 이상이면 FAILED가 된다.
            payment.markFailed(exception.getMessage(), payment.getRetryCount() + 1, calculateNextRetryAt());
            throw exception;
        }
    }

    // 인증 객체의 email 기준으로 현재 회원을 찾음
    private Member findMemberByEmail(String email) {
        return memberRepository.findByEmail(email)
                .orElseThrow(() -> new BadCredentialsException("회원을 찾을 수 없습니다."));
    }

    // 사용자가 구독 취소 상태라면 다음 자동결제를 차단
    private void validateNotCanceled(Long memberId) {
        subscriptionRepository.findFirstByMemberIdAndStatusInOrderBySubscriptionIdDesc(
                memberId,
                List.of(SubscriptionStatus.CANCELED)
        ).ifPresent(subscription -> {
            throw new IllegalArgumentException("구독 취소자는 다음 결제를 진행할 수 없습니다.");
        });
    }

    // 회원의 가장 최근 활성 빌링키를 조회
    private BillingKey findActiveBillingKey(Long memberId) {
        return billingKeyRepository.findFirstByMemberIdAndIsActiveTrueOrderByBillingKeyIdDesc(memberId)
                .orElseThrow(() -> new IllegalArgumentException("등록된 결제 수단이 없습니다."));
    }

    // 요청한 플랜 타입에 맞는 활성 구독 플랜을 조회
    private SubscriptionPlan findPlan(PaymentPlanType planType) {
        String code = planType == PaymentPlanType.MONTHLY ? "MONTHLY_PREMIUM" : "YEARLY_PREMIUM";

        return subscriptionPlanRepository.findByCodeAndActiveTrue(code)
                .orElseThrow(() -> new IllegalArgumentException("활성화된 구독 플랜을 찾을 수 없습니다."));
    }

    // 월간 플랜이면 1개월, 연간 플랜이면 1년 단위로 구독 종료일을 계산
    private LocalDate calculatePeriodEnd(LocalDate periodStart, SubscriptionPlan plan) {
        if (plan.getBillingPeriod() == BillingPeriod.YEAR) {
            return periodStart.plusYears(plan.getBillingInterval());
        }

        return periodStart.plusMonths(plan.getBillingInterval());
    }

    // 결제 성공 후 회원의 기본 냉장고 기준으로 구독을 생성하거나 갱신
    private void activateSubscription(Member member, SubscriptionPlan plan, LocalDate periodStart, LocalDate periodEnd) {
        Fridge fridge = fridgeService.getOrCreateDefaultFridge(member);

        LocalDateTime periodStartAt = periodStart.atStartOfDay();
        LocalDateTime periodEndAt = periodEnd.atStartOfDay();
        LocalDateTime nextBillingAt = periodEndAt;

        subscriptionRepository.findFirstByMemberIdAndStatusInOrderBySubscriptionIdDesc(
                        member.getId(),
                        List.of(SubscriptionStatus.TRIALING, SubscriptionStatus.ACTIVE)
                )
                .ifPresentOrElse(
                        subscription -> subscription.renew(
                                plan.getSubscriptionPlanId(),
                                periodStartAt,
                                periodEndAt,
                                nextBillingAt
                        ),
                        () -> subscriptionRepository.save(Subscription.createActive(
                                member.getId(),
                                fridge.getFridgeId(),
                                plan.getSubscriptionPlanId(),
                                periodStartAt,
                                periodEndAt,
                                nextBillingAt
                        ))
                );
    }

    // Toss orderId는 unique여야 하므로 UUID로 생성
    private String createOrderId() {
        return "naengpa-subscription-" + UUID.randomUUID();
    }

    // Toss 결제 내역과 관리자 화면에서 보일 주문명 생성
    private String createOrderName(PaymentPlanType planType) {
        return planType == PaymentPlanType.MONTHLY
                ? "냉파마스터 월간 구독"
                : "냉파마스터 연간 구독";
    }

    // Toss approvedAt 문자열을 LocalDateTime으로 변환
    private LocalDateTime parseApprovedAt(String approvedAt) {
        if (approvedAt == null || approvedAt.isBlank()) {
            return LocalDateTime.now();
        }

        return OffsetDateTime.parse(approvedAt).toLocalDateTime();
    }

    // MVP에서는 실패 후 1시간 뒤 재시도 대상으로 표시
    private LocalDateTime calculateNextRetryAt() {
        return LocalDateTime.now().plusHours(1);
    }
}
