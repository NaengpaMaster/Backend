package com.naengpa.naengpamasterbackend.fridge.report.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "consumed_products")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ConsumedProduct {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "consumed_product_id")
    private Long consumedProductId;

    @Column(name = "fridge_id", nullable = false)
    private Long fridgeId;

    @Column(name = "actor_member_id", nullable = false)
    private Long actorMemberId;

    @Column(name = "product_id", nullable = false)
    private Long productId;

    @Column(name = "product_category_id", nullable = false)
    private Long productCategoryId;

    @Column(name = "product_name", nullable = false, length = 255)
    private String productName;

    @Column(name = "category_name", nullable = false, length = 255)
    private String categoryName;

    @Column(nullable = false, length = 100)
    private String quantity;

    @Column(name = "consumed_at", nullable = false)
    private LocalDateTime consumedAt;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    public static ConsumedProduct create(
            Long fridgeId,
            Long actorMemberId,
            Long productId,
            Long productCategoryId,
            String productName,
            String categoryName,
            String quantity,
            LocalDateTime consumedAt
    ) {
        ConsumedProduct consumedProduct = new ConsumedProduct();
        consumedProduct.fridgeId = fridgeId;
        consumedProduct.actorMemberId = actorMemberId;
        consumedProduct.productId = productId;
        consumedProduct.productCategoryId = productCategoryId;
        consumedProduct.productName = productName;
        consumedProduct.categoryName = categoryName;
        consumedProduct.quantity = quantity;
        consumedProduct.consumedAt = consumedAt == null ? LocalDateTime.now() : consumedAt;
        return consumedProduct;
    }

    @PrePersist
    void prePersist() {
        createdAt = LocalDateTime.now();
        if (consumedAt == null) {
            consumedAt = createdAt;
        }
    }
}
