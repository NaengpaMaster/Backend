package com.naengpa.naengpamasterbackend.settlement.service;

import com.naengpa.naengpamasterbackend.global.exception.MonthlySettlementNotFoundException;
import com.naengpa.naengpamasterbackend.payment.entity.Payment;
import com.naengpa.naengpamasterbackend.payment.entity.PaymentStatus;
import com.naengpa.naengpamasterbackend.payment.repository.PaymentRepository;
import com.naengpa.naengpamasterbackend.settlement.dto.response.MonthlySettlementDetailResponse;
import com.naengpa.naengpamasterbackend.settlement.dto.response.MonthlySettlementResponse;
import com.naengpa.naengpamasterbackend.settlement.entity.MonthlySettlement;
import com.naengpa.naengpamasterbackend.settlement.entity.SettlementPaymentDetail;
import com.naengpa.naengpamasterbackend.settlement.entity.SettlementStatus;
import com.naengpa.naengpamasterbackend.settlement.repository.MonthlySettlementRepository;
import com.naengpa.naengpamasterbackend.settlement.repository.SettlementPaymentDetailRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.YearMonth;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class MonthlySettlementService {

    private static final int TOSS_FEE_PERCENT = 3;
    private static final int MVP_LLM_COST_AMOUNT = 0;

    private final PaymentRepository paymentRepository;
    private final MonthlySettlementRepository monthlySettlementRepository;
    private final SettlementPaymentDetailRepository settlementPaymentDetailRepository;

    // 관리자 정산 목록 조회. 상태와 월 필터는 둘 다 선택값
    @Transactional(readOnly = true)
    public List<MonthlySettlementResponse> findMonthlySettlements(
            SettlementStatus status,
            String settlementMonth
    ) {
        return findSettlementsByFilter(status, settlementMonth).stream()
                .map(MonthlySettlementResponse::from)
                .toList();
    }

    // 관리자 정산 상세 조회. 정산 요약과 해당 정산에 포함된 결제 내역을 함께 반환
    @Transactional(readOnly = true)
    public MonthlySettlementDetailResponse findMonthlySettlementDetail(Long settlementId) {
        MonthlySettlement settlement = monthlySettlementRepository.findById(settlementId)
                .orElseThrow(MonthlySettlementNotFoundException::new);

        List<SettlementPaymentDetail> details = settlementPaymentDetailRepository
                .findByMonthlySettlementIdOrderBySettlementPaymentDetailIdAsc(settlementId);

        return MonthlySettlementDetailResponse.from(settlement, details);
    }

    // 관리자 요청 기준으로 특정 월의 구독 매출 정산을 생성하거나 PENDING 상태에서 재계산
    @Transactional
    public MonthlySettlement createMonthlySettlement(YearMonth settlementMonth) {
        // 정산 월의 시작 시각과 다음 달 시작 시각을 계산
        var startAt = settlementMonth.atDay(1).atStartOfDay();
        var endAt = settlementMonth.plusMonths(1).atDay(1).atStartOfDay();

        // 정산 대상은 승인일 기준 해당 월에 포함된 SUCCESS 결제만 사용
        List<Payment> payments = paymentRepository
                .findAllByStatusAndApprovedAtGreaterThanEqualAndApprovedAtLessThan(
                        PaymentStatus.SUCCESS,
                        startAt,
                        endAt
                );

        // 총매출: 해당 월 성공 결제 금액 합계
        int grossAmount = payments.stream()
                .mapToInt(Payment::getAmount)
                .sum();

        // MVP에서는 취소 결제를 정산 대상에서 제외하므로 취소 금액은 0원으로 둠
        int canceledAmount = 0;

        // Toss 수수료: 총매출의 3%
        int tossFeeAmount = calculateTossFee(grossAmount);

        // LLM 비용은 MVP에서 0원. 추후 llm_usage_logs 합산으로 교체 가능
        int llmCostAmount = MVP_LLM_COST_AMOUNT;

        // 순매출: 총매출 - Toss 수수료 - LLM 비용
        int netAmount = grossAmount - tossFeeAmount - llmCostAmount;

        // 구독자 수: 해당 월 성공 결제 회원 수 중복 제거
        int subscriberCount = (int) payments.stream()
                .map(Payment::getMemberId)
                .distinct()
                .count();

        int paymentCount = payments.size();
        String settlementMonthValue = settlementMonth.toString();

        MonthlySettlement settlement = monthlySettlementRepository
                .findBySettlementMonth(settlementMonthValue)
                .map(existingSettlement -> {
                    // 같은 월 정산이 이미 있으면 PENDING 상태에서만 재계산
                    existingSettlement.recalculate(
                            grossAmount,
                            canceledAmount,
                            tossFeeAmount,
                            llmCostAmount,
                            netAmount,
                            subscriberCount,
                            paymentCount
                    );

                    // 재계산 시 기존 상세 결제 내역을 지우고 다시 생성
                    settlementPaymentDetailRepository
                            .deleteAllByMonthlySettlementId(existingSettlement.getMonthlySettlementId());

                    return existingSettlement;
                })
                .orElseGet(() -> MonthlySettlement.createPending(
                        settlementMonthValue,
                        grossAmount,
                        canceledAmount,
                        tossFeeAmount,
                        llmCostAmount,
                        netAmount,
                        subscriberCount,
                        paymentCount
                ));

        MonthlySettlement savedSettlement = monthlySettlementRepository.save(settlement);

        // 월별 정산에 포함된 결제 목록을 상세 테이블에 저장
        List<SettlementPaymentDetail> details = payments.stream()
                .map(payment -> createDetail(savedSettlement.getMonthlySettlementId(), payment))
                .collect(Collectors.toList());

        settlementPaymentDetailRepository.saveAll(details);

        return savedSettlement;
    }

    // Toss 수수료는 원 단위 정수로 계산
    private int calculateTossFee(int grossAmount) {
        return grossAmount * TOSS_FEE_PERCENT / 100;
    }

    // Optional 필터 조합을 명확하게 분기해 Repository 메서드를 단순하게 유지
    private List<MonthlySettlement> findSettlementsByFilter(
            SettlementStatus status,
            String settlementMonth
    ) {
        boolean hasStatus = status != null;
        boolean hasSettlementMonth = settlementMonth != null && !settlementMonth.isBlank();

        if (hasStatus && hasSettlementMonth) {
            return monthlySettlementRepository
                    .findByStatusAndSettlementMonthOrderBySettlementMonthDesc(status, settlementMonth);
        }

        if (hasStatus) {
            return monthlySettlementRepository.findByStatusOrderBySettlementMonthDesc(status);
        }

        if (hasSettlementMonth) {
            return monthlySettlementRepository.findBySettlementMonthOrderBySettlementMonthDesc(settlementMonth);
        }

        return monthlySettlementRepository.findAllByOrderBySettlementMonthDesc();
    }

    // 결제 1건이 월별 정산에 얼마로 반영됐는지 상세 내역 생성
    private SettlementPaymentDetail createDetail(Long monthlySettlementId, Payment payment) {
        int amount = payment.getAmount();
        int tossFeeAmount = calculateTossFee(amount);
        int llmCostAmount = MVP_LLM_COST_AMOUNT;
        int netAmount = amount - tossFeeAmount - llmCostAmount;

        return SettlementPaymentDetail.create(
                monthlySettlementId,
                payment.getPaymentId(),
                amount,
                tossFeeAmount,
                llmCostAmount,
                netAmount
        );
    }
}
