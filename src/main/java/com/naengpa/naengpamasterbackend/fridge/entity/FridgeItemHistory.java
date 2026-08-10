package com.naengpa.naengpamasterbackend.fridge.entity;

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
@Table(name = "fridge_item_histories")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class FridgeItemHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "fridge_item_history_id")
    private Long fridgeItemHistoryId;

    @Column(name = "fridge_item_id")
    private Long fridgeItemId;

    @Column(name = "fridge_id", nullable = false)
    private Long fridgeId;

    @Column(name = "actor_member_id", nullable = false)
    private Long actorMemberId;

    @Enumerated(EnumType.STRING)
    @Column(name = "action_type", nullable = false, length = 30)
    private FridgeItemHistoryAction actionType;

    @Column(name = "product_id", nullable = false)
    private Long productId;

    @Column(name = "product_name", nullable = false, length = 255)
    private String productName;

    @Column(length = 100)
    private String quantity;

    @Column(length = 1000)
    private String memo;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    public static FridgeItemHistory create(
            FridgeItem fridgeItem,
            Long actorMemberId,
            FridgeItemHistoryAction actionType,
            String productName
    ) {
        FridgeItemHistory history = new FridgeItemHistory();
        history.fridgeItemId = fridgeItem.getFridgeItemId();
        history.fridgeId = fridgeItem.getFridgeId();
        history.actorMemberId = actorMemberId;
        history.actionType = actionType;
        history.productId = fridgeItem.getProductId();
        history.productName = productName;
        history.quantity = fridgeItem.getQuantity();
        history.memo = fridgeItem.getMemo();
        return history;
    }

    @PrePersist
    void prePersist() {
        createdAt = LocalDateTime.now();
    }
}
