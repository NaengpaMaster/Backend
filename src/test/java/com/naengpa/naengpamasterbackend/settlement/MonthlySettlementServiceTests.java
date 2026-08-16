package com.naengpa.naengpamasterbackend.settlement;

import com.naengpa.naengpamasterbackend.global.exception.MonthlySettlementNotFoundException;
import com.naengpa.naengpamasterbackend.payment.entity.Payment;
import com.naengpa.naengpamasterbackend.payment.entity.PaymentPlanType;
import com.naengpa.naengpamasterbackend.payment.entity.PaymentStatus;
import com.naengpa.naengpamasterbackend.payment.repository.PaymentRepository;
import com.naengpa.naengpamasterbackend.settlement.dto.response.MonthlySettlementResponse;
import com.naengpa.naengpamasterbackend.settlement.entity.MonthlySettlement;
import com.naengpa.naengpamasterbackend.settlement.entity.SettlementPaymentDetail;
import com.naengpa.naengpamasterbackend.settlement.entity.SettlementStatus;
import com.naengpa.naengpamasterbackend.settlement.repository.MonthlySettlementRepository;
import com.naengpa.naengpamasterbackend.settlement.repository.SettlementPaymentDetailRepository;
import com.naengpa.naengpamasterbackend.settlement.service.MonthlySettlementService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.List;
import java.util.Optional;
import java.util.stream.StreamSupport;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class MonthlySettlementServiceTests {

    private PaymentRepository paymentRepository;
    private MonthlySettlementRepository monthlySettlementRepository;
    private SettlementPaymentDetailRepository settlementPaymentDetailRepository;
    private MonthlySettlementService monthlySettlementService;

    @BeforeEach
    void setUp() {
        paymentRepository = mock(PaymentRepository.class);
        monthlySettlementRepository = mock(MonthlySettlementRepository.class);
        settlementPaymentDetailRepository = mock(SettlementPaymentDetailRepository.class);

        monthlySettlementService = new MonthlySettlementService(
                paymentRepository,
                monthlySettlementRepository,
                settlementPaymentDetailRepository
        );
    }

    @Test
    @DisplayName("정산 목록 조회 시 상태와 월 필터를 적용한다")
    void findMonthlySettlements_filtersByStatusAndSettlementMonth() {
        // given
        MonthlySettlement settlement = MonthlySettlement.createPending(
                "2026-08",
                2_900,
                0,
                87,
                0,
                2_813,
                1,
                1
        );
        ReflectionTestUtils.setField(settlement, "monthlySettlementId", 1L);

        when(monthlySettlementRepository.findByStatusAndSettlementMonthOrderBySettlementMonthDesc(
                SettlementStatus.PENDING,
                "2026-08"
        )).thenReturn(List.of(settlement));

        // when
        var responses = monthlySettlementService.findMonthlySettlements(
                SettlementStatus.PENDING,
                "2026-08"
        );

        // then
        assertThat(responses).hasSize(1);
        assertThat(responses.get(0).monthlySettlementId()).isEqualTo(1L);
        assertThat(responses.get(0).settlementMonth()).isEqualTo("2026-08");
        assertThat(responses.get(0).status()).isEqualTo(SettlementStatus.PENDING);
    }

    @Test
    @DisplayName("정산 목록 조회 결과가 없으면 빈 배열을 반환한다")
    void findMonthlySettlements_returnsEmptyListWhenNoSettlement() {
        // given
        when(monthlySettlementRepository.findAllByOrderBySettlementMonthDesc()).thenReturn(List.of());

        // when
        var responses = monthlySettlementService.findMonthlySettlements(null, null);

        // then
        assertThat(responses).isEmpty();
    }

    @Test
    @DisplayName("정산 상세 조회 시 정산 요약과 상세 결제 내역을 함께 반환한다")
    void findMonthlySettlementDetail_returnsSettlementWithPaymentDetails() {
        // given
        MonthlySettlement settlement = MonthlySettlement.createPending(
                "2026-08",
                2_900,
                0,
                87,
                0,
                2_813,
                1,
                1
        );
        ReflectionTestUtils.setField(settlement, "monthlySettlementId", 1L);

        SettlementPaymentDetail detail = SettlementPaymentDetail.create(
                1L,
                10L,
                2_900,
                87,
                0,
                2_813
        );
        ReflectionTestUtils.setField(detail, "settlementPaymentDetailId", 100L);

        when(monthlySettlementRepository.findById(1L)).thenReturn(Optional.of(settlement));
        when(settlementPaymentDetailRepository.findByMonthlySettlementIdOrderBySettlementPaymentDetailIdAsc(1L))
                .thenReturn(List.of(detail));

        // when
        var response = monthlySettlementService.findMonthlySettlementDetail(1L);

        // then
        assertThat(response.settlement().monthlySettlementId()).isEqualTo(1L);
        assertThat(response.paymentDetails()).hasSize(1);
        assertThat(response.paymentDetails().get(0).paymentId()).isEqualTo(10L);
        assertThat(response.paymentDetails().get(0).netAmount()).isEqualTo(2_813);
    }

    @Test
    @DisplayName("존재하지 않는 정산 상세를 조회하면 예외가 발생한다")
    void findMonthlySettlementDetail_throwsWhenSettlementNotFound() {
        // given
        when(monthlySettlementRepository.findById(999L)).thenReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> monthlySettlementService.findMonthlySettlementDetail(999L))
                .isInstanceOf(MonthlySettlementNotFoundException.class)
                .hasMessage("월별 정산을 찾을 수 없습니다.");

        verify(settlementPaymentDetailRepository, never())
                .findByMonthlySettlementIdOrderBySettlementPaymentDetailIdAsc(anyLong());
    }

    @Test
    @DisplayName("PENDING 정산은 CONFIRMED 상태로 확정할 수 있다")
    void confirmMonthlySettlement_changesPendingToConfirmed() {
        // given
        MonthlySettlement settlement = pendingSettlement(1L);
        when(monthlySettlementRepository.findById(1L)).thenReturn(Optional.of(settlement));

        // when
        MonthlySettlementResponse response = monthlySettlementService.confirmMonthlySettlement(1L);

        // then
        assertThat(response.status()).isEqualTo(SettlementStatus.CONFIRMED);
        assertThat(response.confirmedAt()).isNotNull();
        assertThat(settlement.getStatus()).isEqualTo(SettlementStatus.CONFIRMED);
    }

    @Test
    @DisplayName("CONFIRMED 정산은 PAID 상태로 지급 완료 처리할 수 있다")
    void markMonthlySettlementPaid_changesConfirmedToPaid() {
        // given
        MonthlySettlement settlement = pendingSettlement(1L);
        settlement.confirm();
        when(monthlySettlementRepository.findById(1L)).thenReturn(Optional.of(settlement));

        // when
        MonthlySettlementResponse response = monthlySettlementService.markMonthlySettlementPaid(1L);

        // then
        assertThat(response.status()).isEqualTo(SettlementStatus.PAID);
        assertThat(response.paidAt()).isNotNull();
        assertThat(settlement.getStatus()).isEqualTo(SettlementStatus.PAID);
    }

    @Test
    @DisplayName("PENDING 정산은 CANCELED 상태로 취소할 수 있다")
    void cancelMonthlySettlement_changesPendingToCanceled() {
        // given
        MonthlySettlement settlement = pendingSettlement(1L);
        when(monthlySettlementRepository.findById(1L)).thenReturn(Optional.of(settlement));

        // when
        MonthlySettlementResponse response = monthlySettlementService.cancelMonthlySettlement(1L);

        // then
        assertThat(response.status()).isEqualTo(SettlementStatus.CANCELED);
        assertThat(settlement.getStatus()).isEqualTo(SettlementStatus.CANCELED);
    }

    @Test
    @DisplayName("CONFIRMED 정산은 CANCELED 상태로 취소할 수 있다")
    void cancelMonthlySettlement_changesConfirmedToCanceled() {
        // given
        MonthlySettlement settlement = pendingSettlement(1L);
        settlement.confirm();
        when(monthlySettlementRepository.findById(1L)).thenReturn(Optional.of(settlement));

        // when
        MonthlySettlementResponse response = monthlySettlementService.cancelMonthlySettlement(1L);

        // then
        assertThat(response.status()).isEqualTo(SettlementStatus.CANCELED);
        assertThat(settlement.getStatus()).isEqualTo(SettlementStatus.CANCELED);
    }

    @Test
    @DisplayName("PENDING 정산은 바로 PAID 처리할 수 없다")
    void markMonthlySettlementPaid_throwsWhenSettlementIsPending() {
        // given
        MonthlySettlement settlement = pendingSettlement(1L);
        when(monthlySettlementRepository.findById(1L)).thenReturn(Optional.of(settlement));

        // when & then
        assertThatThrownBy(() -> monthlySettlementService.markMonthlySettlementPaid(1L))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("CONFIRMED 정산만 지급 완료 처리할 수 있습니다.");
    }

    @Test
    @DisplayName("PAID 정산은 취소할 수 없다")
    void cancelMonthlySettlement_throwsWhenSettlementIsPaid() {
        // given
        MonthlySettlement settlement = pendingSettlement(1L);
        settlement.confirm();
        settlement.markPaid();
        when(monthlySettlementRepository.findById(1L)).thenReturn(Optional.of(settlement));

        // when & then
        assertThatThrownBy(() -> monthlySettlementService.cancelMonthlySettlement(1L))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("이미 지급 완료된 정산은 취소할 수 없습니다.");
    }

    @Test
    @DisplayName("CANCELED 정산은 다시 확정할 수 없다")
    void confirmMonthlySettlement_throwsWhenSettlementIsCanceled() {
        // given
        MonthlySettlement settlement = pendingSettlement(1L);
        settlement.cancel();
        when(monthlySettlementRepository.findById(1L)).thenReturn(Optional.of(settlement));

        // when & then
        assertThatThrownBy(() -> monthlySettlementService.confirmMonthlySettlement(1L))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("PENDING 정산만 확정할 수 있습니다.");
    }

    @Test
    @DisplayName("존재하지 않는 정산은 상태 변경할 수 없다")
    void confirmMonthlySettlement_throwsWhenSettlementNotFound() {
        // given
        when(monthlySettlementRepository.findById(999L)).thenReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> monthlySettlementService.confirmMonthlySettlement(999L))
                .isInstanceOf(MonthlySettlementNotFoundException.class)
                .hasMessage("월별 정산을 찾을 수 없습니다.");
    }

    @Test
    @DisplayName("월별 정산 생성 시 해당 월 성공 결제만 집계하고 상세 결제 내역을 저장한다")
    void createMonthlySettlement_createsPendingSettlementFromSuccessPayments() {
        // given
        YearMonth settlementMonth = YearMonth.of(2026, 8);
        Payment monthlyPayment = successPayment(1L, 1L, 2_900);
        Payment yearlyPayment = successPayment(2L, 2L, 27_840);

        when(paymentRepository.findAllByStatusAndApprovedAtGreaterThanEqualAndApprovedAtLessThan(
                eq(PaymentStatus.SUCCESS),
                eq(LocalDateTime.of(2026, 8, 1, 0, 0)),
                eq(LocalDateTime.of(2026, 9, 1, 0, 0))
        )).thenReturn(List.of(monthlyPayment, yearlyPayment));
        when(monthlySettlementRepository.findBySettlementMonth("2026-08")).thenReturn(Optional.empty());
        when(monthlySettlementRepository.save(any(MonthlySettlement.class))).thenAnswer(invocation -> {
            MonthlySettlement settlement = invocation.getArgument(0);
            ReflectionTestUtils.setField(settlement, "monthlySettlementId", 10L);
            return settlement;
        });

        // when
        MonthlySettlement settlement = monthlySettlementService.createMonthlySettlement(settlementMonth);

        // then
        assertThat(settlement.getSettlementMonth()).isEqualTo("2026-08");
        assertThat(settlement.getGrossAmount()).isEqualTo(30_740);
        assertThat(settlement.getTossFeeAmount()).isEqualTo(922);
        assertThat(settlement.getLlmCostAmount()).isZero();
        assertThat(settlement.getNetAmount()).isEqualTo(29_818);
        assertThat(settlement.getSubscriberCount()).isEqualTo(2);
        assertThat(settlement.getPaymentCount()).isEqualTo(2);
        assertThat(settlement.getStatus()).isEqualTo(SettlementStatus.PENDING);

        List<SettlementPaymentDetail> details = captureSavedDetails();
        assertThat(details).hasSize(2);
        assertThat(details)
                .extracting(SettlementPaymentDetail::getMonthlySettlementId)
                .containsOnly(10L);
        assertThat(details)
                .extracting(SettlementPaymentDetail::getPaymentId)
                .containsExactly(1L, 2L);
        assertThat(details)
                .extracting(SettlementPaymentDetail::getNetAmount)
                .containsExactly(2_813, 27_005);

        verify(settlementPaymentDetailRepository, never()).deleteAllByMonthlySettlementId(anyLong());
    }

    @Test
    @DisplayName("이미 생성된 PENDING 정산은 기존 상세 내역을 지우고 재계산한다")
    void createMonthlySettlement_recalculatesPendingSettlement() {
        // given
        YearMonth settlementMonth = YearMonth.of(2026, 8);
        MonthlySettlement existingSettlement = MonthlySettlement.createPending(
                "2026-08",
                1_000,
                0,
                30,
                0,
                970,
                1,
                1
        );
        ReflectionTestUtils.setField(existingSettlement, "monthlySettlementId", 20L);

        Payment payment = successPayment(3L, 1L, 2_900);

        when(paymentRepository.findAllByStatusAndApprovedAtGreaterThanEqualAndApprovedAtLessThan(
                eq(PaymentStatus.SUCCESS),
                any(LocalDateTime.class),
                any(LocalDateTime.class)
        )).thenReturn(List.of(payment));
        when(monthlySettlementRepository.findBySettlementMonth("2026-08"))
                .thenReturn(Optional.of(existingSettlement));
        when(monthlySettlementRepository.save(existingSettlement)).thenReturn(existingSettlement);

        // when
        MonthlySettlement settlement = monthlySettlementService.createMonthlySettlement(settlementMonth);

        // then
        assertThat(settlement.getMonthlySettlementId()).isEqualTo(20L);
        assertThat(settlement.getGrossAmount()).isEqualTo(2_900);
        assertThat(settlement.getTossFeeAmount()).isEqualTo(87);
        assertThat(settlement.getNetAmount()).isEqualTo(2_813);
        assertThat(settlement.getSubscriberCount()).isEqualTo(1);
        assertThat(settlement.getPaymentCount()).isEqualTo(1);

        verify(settlementPaymentDetailRepository).deleteAllByMonthlySettlementId(20L);

        List<SettlementPaymentDetail> details = captureSavedDetails();
        assertThat(details).hasSize(1);
        assertThat(details.get(0).getMonthlySettlementId()).isEqualTo(20L);
        assertThat(details.get(0).getPaymentId()).isEqualTo(3L);
    }

    @Test
    @DisplayName("확정된 정산은 재계산할 수 없다")
    void createMonthlySettlement_throwsWhenSettlementIsNotPending() {
        // given
        YearMonth settlementMonth = YearMonth.of(2026, 8);
        MonthlySettlement confirmedSettlement = MonthlySettlement.createPending(
                "2026-08",
                2_900,
                0,
                87,
                0,
                2_813,
                1,
                1
        );
        ReflectionTestUtils.setField(confirmedSettlement, "monthlySettlementId", 30L);
        ReflectionTestUtils.setField(confirmedSettlement, "status", SettlementStatus.CONFIRMED);

        when(paymentRepository.findAllByStatusAndApprovedAtGreaterThanEqualAndApprovedAtLessThan(
                eq(PaymentStatus.SUCCESS),
                any(LocalDateTime.class),
                any(LocalDateTime.class)
        )).thenReturn(List.of(successPayment(4L, 1L, 2_900)));
        when(monthlySettlementRepository.findBySettlementMonth("2026-08"))
                .thenReturn(Optional.of(confirmedSettlement));

        // when & then
        assertThatThrownBy(() -> monthlySettlementService.createMonthlySettlement(settlementMonth))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("확정된 정산은 재계산할 수 없습니다.");

        verify(monthlySettlementRepository, never()).save(any(MonthlySettlement.class));
        verify(settlementPaymentDetailRepository, never()).deleteAllByMonthlySettlementId(anyLong());
        verify(settlementPaymentDetailRepository, never()).saveAll(any());
    }

    private Payment successPayment(Long paymentId, Long memberId, int amount) {
        Payment payment = Payment.ready(
                memberId,
                1L,
                "order-" + paymentId,
                "냉파마스터 구독",
                amount,
                PaymentPlanType.MONTHLY,
                LocalDate.of(2026, 8, 13),
                LocalDate.of(2026, 9, 13)
        );
        payment.markSuccess("payment-key-" + paymentId, LocalDateTime.of(2026, 8, 13, 10, 0));
        ReflectionTestUtils.setField(payment, "paymentId", paymentId);
        return payment;
    }

    private MonthlySettlement pendingSettlement(Long settlementId) {
        MonthlySettlement settlement = MonthlySettlement.createPending(
                "2026-08",
                2_900,
                0,
                87,
                0,
                2_813,
                1,
                1
        );
        ReflectionTestUtils.setField(settlement, "monthlySettlementId", settlementId);
        return settlement;
    }

    @SuppressWarnings("unchecked")
    private List<SettlementPaymentDetail> captureSavedDetails() {
        ArgumentCaptor<Iterable<SettlementPaymentDetail>> captor = ArgumentCaptor.forClass(Iterable.class);
        verify(settlementPaymentDetailRepository).saveAll(captor.capture());
        return StreamSupport.stream(captor.getValue().spliterator(), false).toList();
    }
}
