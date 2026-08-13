package com.naengpa.naengpamasterbackend.fridge.report.service;

import com.naengpa.naengpamasterbackend.fridge.entity.FridgeItem;
import com.naengpa.naengpamasterbackend.fridge.report.entity.ConsumedProduct;
import com.naengpa.naengpamasterbackend.fridge.report.repository.ConsumedProductRepository;
import com.naengpa.naengpamasterbackend.product.entity.Product;
import com.naengpa.naengpamasterbackend.product.entity.ProductCategory;
import com.naengpa.naengpamasterbackend.product.exception.ProductNotFoundException;
import com.naengpa.naengpamasterbackend.product.repository.ProductCategoryRepository;
import com.naengpa.naengpamasterbackend.product.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class ConsumedProductService {

    private final ConsumedProductRepository consumedProductRepository;
    private final ProductRepository productRepository;
    private final ProductCategoryRepository productCategoryRepository;

    @Transactional
    public void recordConsumption(FridgeItem fridgeItem, Long actorMemberId, String quantity) {
        Product product = productRepository.findById(fridgeItem.getProductId())
                .orElseThrow(() -> new ProductNotFoundException(fridgeItem.getProductId()));
        ProductCategory category = productCategoryRepository.findById(product.getProductCategoryId())
                .orElseThrow(() -> new IllegalArgumentException("재료 카테고리를 찾을 수 없습니다."));

        consumedProductRepository.save(ConsumedProduct.create(
                fridgeItem.getFridgeId(),
                actorMemberId,
                product.getProductId(),
                product.getProductCategoryId(),
                product.getName(),
                category.getName(),
                resolveQuantity(quantity),
                LocalDateTime.now()
        ));
    }

    private String resolveQuantity(String quantity) {
        if (quantity == null || quantity.isBlank()) {
            return "수량 미입력";
        }
        return quantity.trim();
    }
}
