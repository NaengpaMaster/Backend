package com.naengpa.naengpamasterbackend.fridge.report;

import com.naengpa.naengpamasterbackend.fridge.entity.Fridge;
import com.naengpa.naengpamasterbackend.fridge.report.dto.WeeklyFridgeReportRecipient;
import com.naengpa.naengpamasterbackend.fridge.report.dto.WeeklyFridgeReportSummary;
import com.naengpa.naengpamasterbackend.fridge.report.entity.ConsumedProduct;
import com.naengpa.naengpamasterbackend.fridge.report.entity.WeeklyFridgeReportDeliveryStatus;
import com.naengpa.naengpamasterbackend.fridge.report.repository.ConsumedProductRepository;
import com.naengpa.naengpamasterbackend.fridge.report.repository.WeeklyFridgeReportDeliveryLogRepository;
import com.naengpa.naengpamasterbackend.fridge.report.service.WeeklyFridgeReportAggregationService;
import com.naengpa.naengpamasterbackend.fridge.report.service.WeeklyFridgeReportDispatchService;
import com.naengpa.naengpamasterbackend.fridge.report.service.WeeklyFridgeReportMailService;
import com.naengpa.naengpamasterbackend.fridge.report.service.WeeklyFridgeReportRecipientService;
import com.naengpa.naengpamasterbackend.fridge.repository.FridgeItemRepository;
import com.naengpa.naengpamasterbackend.fridge.repository.FridgeRepository;
import com.naengpa.naengpamasterbackend.product.repository.ProductRepository;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class WeeklyFridgeReportServiceTests {

    private static final Clock CLOCK = Clock.fixed(
            Instant.parse("2026-08-11T00:00:00Z"),
            ZoneId.of("Asia/Seoul")
    );

    @Test
    void aggregateBuildsWeeklyProductAndCategorySummary() {
        ConsumedProductRepository consumedProductRepository = mock(ConsumedProductRepository.class);
        FridgeRepository fridgeRepository = mock(FridgeRepository.class);
        FridgeItemRepository fridgeItemRepository = mock(FridgeItemRepository.class);
        ProductRepository productRepository = mock(ProductRepository.class);
        WeeklyFridgeReportAggregationService service = new WeeklyFridgeReportAggregationService(
                consumedProductRepository,
                fridgeRepository,
                fridgeItemRepository,
                productRepository,
                CLOCK
        );
        Fridge fridge = Fridge.createDefault(1L, "공주");
        ReflectionTestUtils.setField(fridge, "fridgeId", 10L);

        when(fridgeRepository.findById(10L)).thenReturn(Optional.of(fridge));
        when(consumedProductRepository.findAllByFridgeIdAndConsumedAtGreaterThanEqualAndConsumedAtLessThan(
                any(),
                any(),
                any()
        )).thenReturn(List.of(
                consumedProduct(10L, 1L, 1L, "감자", 1L, "채소"),
                consumedProduct(10L, 1L, 1L, "감자", 1L, "채소"),
                consumedProduct(10L, 1L, 2L, "우유", 2L, "유제품")
        ));
        when(fridgeItemRepository.findByFridgeIdAndIsDeletedFalse(10L)).thenReturn(List.of());

        WeeklyFridgeReportSummary result = service.aggregate(10L, LocalDate.of(2026, 8, 10));

        assertThat(result.totalConsumedCount()).isEqualTo(3);
        assertThat(result.topProducts().getFirst().productName()).isEqualTo("감자");
        assertThat(result.topProducts().getFirst().count()).isEqualTo(2);
        assertThat(result.categories())
                .extracting(category -> category.categoryName() + ":" + category.count())
                .containsExactly("채소:2", "유제품:1");
        assertThat(result.remainingItems()).isEmpty();
    }

    @Test
    void dispatchSkipsAlreadyDeliveredRecipient() {
        WeeklyFridgeReportRecipientService recipientService = mock(WeeklyFridgeReportRecipientService.class);
        WeeklyFridgeReportAggregationService aggregationService = mock(WeeklyFridgeReportAggregationService.class);
        WeeklyFridgeReportMailService mailService = mock(WeeklyFridgeReportMailService.class);
        WeeklyFridgeReportDeliveryLogRepository deliveryLogRepository = mock(WeeklyFridgeReportDeliveryLogRepository.class);
        WeeklyFridgeReportDispatchService dispatchService = new WeeklyFridgeReportDispatchService(
                recipientService,
                aggregationService,
                mailService,
                deliveryLogRepository,
                CLOCK
        );
        WeeklyFridgeReportRecipient recipient = new WeeklyFridgeReportRecipient(
                10L,
                "공주의 냉장고",
                1L,
                "user@example.com"
        );

        when(recipientService.findRecipients()).thenReturn(List.of(recipient));
        when(deliveryLogRepository.existsByFridgeIdAndReceiverMemberIdAndReportWeekAndStatus(
                10L,
                1L,
                "2026-W33",
                WeeklyFridgeReportDeliveryStatus.SUCCESS
        ))
                .thenReturn(true);

        int sentCount = dispatchService.dispatchWeeklyReports();

        assertThat(sentCount).isZero();
        verify(mailService, never()).send(any(), any());
    }

    @Test
    void dispatchForceSendsAlreadyDeliveredRecipient() {
        WeeklyFridgeReportRecipientService recipientService = mock(WeeklyFridgeReportRecipientService.class);
        WeeklyFridgeReportAggregationService aggregationService = mock(WeeklyFridgeReportAggregationService.class);
        WeeklyFridgeReportMailService mailService = mock(WeeklyFridgeReportMailService.class);
        WeeklyFridgeReportDeliveryLogRepository deliveryLogRepository = mock(WeeklyFridgeReportDeliveryLogRepository.class);
        WeeklyFridgeReportDispatchService dispatchService = new WeeklyFridgeReportDispatchService(
                recipientService,
                aggregationService,
                mailService,
                deliveryLogRepository,
                CLOCK
        );
        WeeklyFridgeReportRecipient recipient = new WeeklyFridgeReportRecipient(
                10L,
                "공주의 냉장고",
                1L,
                "user@example.com"
        );

        when(recipientService.findRecipients()).thenReturn(List.of(recipient));
        when(aggregationService.aggregate(eq(10L), any())).thenReturn(emptySummary(10L, "공주의 냉장고"));
        when(deliveryLogRepository.existsByFridgeIdAndReceiverMemberIdAndReportWeekAndStatus(
                10L,
                1L,
                "2026-W33",
                WeeklyFridgeReportDeliveryStatus.SUCCESS
        ))
                .thenReturn(true);

        int sentCount = dispatchService.dispatchWeeklyReports(true);

        assertThat(sentCount).isEqualTo(1);
        verify(mailService).send(eq("user@example.com"), any());
        verify(aggregationService).aggregate(10L, LocalDate.of(2026, 8, 11));
        verify(deliveryLogRepository, never()).save(any());
    }

    @Test
    void dispatchSendsOneMailForSameMemberWithMultipleFridges() {
        WeeklyFridgeReportRecipientService recipientService = mock(WeeklyFridgeReportRecipientService.class);
        WeeklyFridgeReportAggregationService aggregationService = mock(WeeklyFridgeReportAggregationService.class);
        WeeklyFridgeReportMailService mailService = mock(WeeklyFridgeReportMailService.class);
        WeeklyFridgeReportDeliveryLogRepository deliveryLogRepository = mock(WeeklyFridgeReportDeliveryLogRepository.class);
        WeeklyFridgeReportDispatchService dispatchService = new WeeklyFridgeReportDispatchService(
                recipientService,
                aggregationService,
                mailService,
                deliveryLogRepository,
                CLOCK
        );
        WeeklyFridgeReportRecipient myFridge = new WeeklyFridgeReportRecipient(
                10L,
                "내 냉장고",
                1L,
                "user@example.com"
        );
        WeeklyFridgeReportRecipient familyFridge = new WeeklyFridgeReportRecipient(
                20L,
                "가족 냉장고",
                1L,
                "user@example.com"
        );

        when(recipientService.findRecipients()).thenReturn(List.of(myFridge, familyFridge));
        when(aggregationService.aggregate(eq(10L), any())).thenReturn(emptySummary(10L, "내 냉장고"));
        when(aggregationService.aggregate(eq(20L), any())).thenReturn(emptySummary(20L, "가족 냉장고"));

        int sentCount = dispatchService.dispatchWeeklyReports();

        assertThat(sentCount).isEqualTo(1);
        verify(mailService, times(1)).send(eq("user@example.com"), any());
        verify(deliveryLogRepository, times(2)).save(any());
    }

    private ConsumedProduct consumedProduct(
            Long fridgeId,
            Long actorMemberId,
            Long productId,
            String productName,
            Long productCategoryId,
            String categoryName
    ) {
        return ConsumedProduct.create(
                fridgeId,
                actorMemberId,
                productId,
                productCategoryId,
                productName,
                categoryName,
                "1개",
                LocalDateTime.of(2026, 8, 10, 12, 0)
        );
    }

    private WeeklyFridgeReportSummary emptySummary(Long fridgeId, String fridgeName) {
        return new WeeklyFridgeReportSummary(
                fridgeId,
                fridgeName,
                LocalDate.of(2026, 8, 4),
                LocalDate.of(2026, 8, 10),
                0,
                List.of(),
                List.of(),
                List.of()
        );
    }
}
