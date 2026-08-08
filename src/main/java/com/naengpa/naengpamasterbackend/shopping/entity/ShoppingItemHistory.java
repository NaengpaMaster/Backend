package com.naengpa.naengpamasterbackend.shopping.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
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
@Table(name = "shopping_item_histories")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ShoppingItemHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "shopping_item_history_id")
    private Long shoppingItemHistoryId;

    @Column(name = "shopping_item_id")
    private Long shoppingItemId;

    @Column(name = "fridge_id", nullable = false)
    private Long fridgeId;

    @Column(name = "actor_member_id", nullable = false)
    private Long actorMemberId;

    @Enumerated(EnumType.STRING)
    @Column(name = "action_type", nullable = false, length = 30)
    private ShoppingItemHistoryAction actionType;

    @Column(name = "product_id", nullable = false)
    private Long productId;

    @Column(name = "product_name", nullable = false, length = 255)
    private String productName;

    @Column(length = 100)
    private String quantity;

    @Column(name = "is_purchased")
    private Boolean isPurchased;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    public static ShoppingItemHistory create(
            ShoppingItem shoppingItem,
            Long actorMemberId,
            ShoppingItemHistoryAction actionType,
            String productName
    ) {
        ShoppingItemHistory history = new ShoppingItemHistory();
        history.shoppingItemId = shoppingItem.getShoppingItemId();
        history.fridgeId = shoppingItem.getFridgeId();
        history.actorMemberId = actorMemberId;
        history.actionType = actionType;
        history.productId = shoppingItem.getProductId();
        history.productName = productName;
        history.quantity = shoppingItem.getQuantity();
        history.isPurchased = shoppingItem.getIsPurchased();
        return history;
    }

    @PrePersist
    void prePersist() {
        createdAt = LocalDateTime.now();
    }
}
