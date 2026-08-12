package com.naengpa.naengpamasterbackend.fridge.report.service;

import com.naengpa.naengpamasterbackend.fridge.entity.Fridge;
import com.naengpa.naengpamasterbackend.fridge.entity.FridgeItem;
import com.naengpa.naengpamasterbackend.fridge.report.dto.WeeklyConsumedCategorySummary;
import com.naengpa.naengpamasterbackend.fridge.report.dto.WeeklyConsumedProductSummary;
import com.naengpa.naengpamasterbackend.fridge.report.dto.WeeklyFridgeReportSummary;
import com.naengpa.naengpamasterbackend.fridge.report.dto.WeeklyRemainingFridgeItemSummary;
import com.naengpa.naengpamasterbackend.fridge.report.entity.ConsumedProduct;
import com.naengpa.naengpamasterbackend.fridge.report.repository.ConsumedProductRepository;
import com.naengpa.naengpamasterbackend.fridge.repository.FridgeItemRepository;
import com.naengpa.naengpamasterbackend.fridge.repository.FridgeRepository;
import com.naengpa.naengpamasterbackend.product.entity.Product;
import com.naengpa.naengpamasterbackend.product.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class WeeklyFridgeReportAggregationService {

    private static final int REPORT_DAYS = 7;
    private static final int TOP_PRODUCT_LIMIT = 5;
    private static final int REMAINING_ITEM_LIMIT = 8;

    private final ConsumedProductRepository consumedProductRepository;
    private final FridgeRepository fridgeRepository;
    private final FridgeItemRepository fridgeItemRepository;
    private final ProductRepository productRepository;
    private final Clock clock;

    @Transactional(readOnly = true)
    public WeeklyFridgeReportSummary aggregate(Long fridgeId, LocalDate endDate) {
        Fridge fridge = fridgeRepository.findById(fridgeId)
                .orElseThrow(() -> new IllegalArgumentException("냉장고를 찾을 수 없습니다."));
        LocalDate reportEndDate = endDate == null ? LocalDate.now(clock) : endDate;
        LocalDate startDate = reportEndDate.minusDays(REPORT_DAYS - 1L);
        LocalDateTime startAt = startDate.atStartOfDay();
        LocalDateTime endAt = reportEndDate.plusDays(1).atStartOfDay();

        List<ConsumedProduct> consumedProducts =
                consumedProductRepository.findAllByFridgeIdAndConsumedAtGreaterThanEqualAndConsumedAtLessThan(
                        fridgeId,
                        startAt,
                        endAt
                );
        long totalCount = consumedProducts.size();

        return new WeeklyFridgeReportSummary(
                fridgeId,
                fridge.getName(),
                startDate,
                reportEndDate,
                totalCount,
                topProducts(consumedProducts),
                categories(consumedProducts, totalCount),
                remainingItems(fridgeId)
        );
    }

    private List<WeeklyConsumedProductSummary> topProducts(List<ConsumedProduct> consumedProducts) {
        return consumedProducts.stream()
                .collect(Collectors.groupingBy(
                        ConsumedProduct::getProductId,
                        Collectors.collectingAndThen(Collectors.toList(), Function.identity())
                ))
                .values()
                .stream()
                .map(products -> new WeeklyConsumedProductSummary(
                        products.getFirst().getProductId(),
                        products.getFirst().getProductName(),
                        products.size()
                ))
                .sorted(Comparator.comparingLong(WeeklyConsumedProductSummary::count).reversed()
                        .thenComparing(WeeklyConsumedProductSummary::productName))
                .limit(TOP_PRODUCT_LIMIT)
                .toList();
    }

    private List<WeeklyConsumedCategorySummary> categories(List<ConsumedProduct> consumedProducts, long totalCount) {
        if (totalCount == 0) {
            return List.of();
        }

        Map<Long, List<ConsumedProduct>> categoryGroups = consumedProducts.stream()
                .collect(Collectors.groupingBy(ConsumedProduct::getProductCategoryId));

        return categoryGroups.values()
                .stream()
                .map(products -> {
                    long count = products.size();
                    BigDecimal ratio = BigDecimal.valueOf(count)
                            .multiply(BigDecimal.valueOf(100))
                            .divide(BigDecimal.valueOf(totalCount), 1, RoundingMode.HALF_UP);
                    return new WeeklyConsumedCategorySummary(
                            products.getFirst().getProductCategoryId(),
                            products.getFirst().getCategoryName(),
                            count,
                            ratio
                    );
                })
                .sorted(Comparator.comparingLong(WeeklyConsumedCategorySummary::count).reversed()
                        .thenComparing(WeeklyConsumedCategorySummary::categoryName))
                .toList();
    }

    private List<WeeklyRemainingFridgeItemSummary> remainingItems(Long fridgeId) {
        List<FridgeItem> fridgeItems = fridgeItemRepository.findByFridgeIdAndIsDeletedFalse(fridgeId)
                .stream()
                .sorted(Comparator.comparing(
                                FridgeItem::getExpiryDate,
                                Comparator.nullsLast(Comparator.naturalOrder())
                        )
                        .thenComparing(FridgeItem::getCreatedAt, Comparator.nullsLast(Comparator.reverseOrder())))
                .limit(REMAINING_ITEM_LIMIT)
                .toList();
        if (fridgeItems.isEmpty()) {
            return List.of();
        }

        Map<Long, String> productNames = productRepository.findByProductIdIn(
                        fridgeItems.stream()
                                .map(FridgeItem::getProductId)
                                .distinct()
                                .toList()
                )
                .stream()
                .collect(Collectors.toMap(Product::getProductId, Product::getName));

        return fridgeItems.stream()
                .map(item -> new WeeklyRemainingFridgeItemSummary(
                        item.getFridgeItemId(),
                        productNames.getOrDefault(item.getProductId(), "알 수 없는 식재료"),
                        item.getQuantity(),
                        item.getExpiryDate()
                ))
                .toList();
    }
}
