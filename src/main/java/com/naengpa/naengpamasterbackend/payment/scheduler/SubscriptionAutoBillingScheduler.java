package com.naengpa.naengpamasterbackend.payment.scheduler;

import com.naengpa.naengpamasterbackend.payment.service.SubscriptionPaymentService;
import com.naengpa.naengpamasterbackend.subscription.entity.Subscription;
import com.naengpa.naengpamasterbackend.subscription.entity.SubscriptionStatus;
import com.naengpa.naengpamasterbackend.subscription.repository.SubscriptionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class SubscriptionAutoBillingScheduler {

    private static final List<SubscriptionStatus> AUTO_BILLING_STATUSES = List.of(
            SubscriptionStatus.TRIALING,
            SubscriptionStatus.ACTIVE
    );

    private final SubscriptionRepository subscriptionRepository;
    private final SubscriptionPaymentService subscriptionPaymentService;

    @Scheduled(cron = "0 0 0 * * *", zone = "Asia/Seoul")
    public void runAutoBilling() {
        LocalDateTime now = LocalDateTime.now();

        // 무료체험 종료일 또는 다음 결제일이 도래했고, 해지 예약되지 않은 구독만 자동결제 대상으로 조회
        List<Subscription> subscriptions = subscriptionRepository
                .findAllByStatusInAndNextBillingAtLessThanEqualAndCanceledAtIsNull(
                        AUTO_BILLING_STATUSES,
                        now
                );

        log.info("구독 자동결제 스케줄러 시작 - 대상 {}건", subscriptions.size());

        for (Subscription subscription : subscriptions) {
            try {
                // 구독 1건의 실제 결제/성공/실패 처리는 서비스에 위임
                subscriptionPaymentService.processAutoBilling(subscription);
            } catch (Exception exception) {
                // 한 회원의 결제 실패가 전체 자동결제 배치를 중단시키지 않도록 회원 단위로 격리
                log.warn(
                        "구독 자동결제 실패 - subscriptionId={}, memberId={}, reason={}",
                        subscription.getSubscriptionId(),
                        subscription.getMemberId(),
                        exception.getMessage()
                );
            }
        }

        log.info("구독 자동결제 스케줄러 종료");
    }
}
