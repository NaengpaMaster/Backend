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
@Table(name = "fridge_item_share_requests")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class FridgeItemShareRequest {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "fridge_item_share_request_id")
    private Long fridgeItemShareRequestId;

    @Column(name = "requester_member_id", nullable = false)
    private Long requesterMemberId;

    @Column(name = "requested_member_id", nullable = false)
    private Long requestedMemberId;

    @Column(name = "source_fridge_id", nullable = false)
    private Long sourceFridgeId;

    @Column(name = "target_fridge_id", nullable = false)
    private Long targetFridgeId;

    @Column(name = "fridge_item_id", nullable = false)
    private Long fridgeItemId;

    @Column(name = "product_id", nullable = false)
    private Long productId;

    @Column(name = "requested_quantity", nullable = false, length = 100)
    private String requestedQuantity;

    @Column(length = 1000)
    private String message;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private FridgeItemShareRequestStatus status = FridgeItemShareRequestStatus.PENDING;

    @Column(name = "requested_at", nullable = false, updatable = false)
    private LocalDateTime requestedAt;

    @Column(name = "responded_at")
    private LocalDateTime respondedAt;

    public static FridgeItemShareRequest create(
            Long requesterMemberId,
            Long requestedMemberId,
            Long sourceFridgeId,
            Long targetFridgeId,
            Long fridgeItemId,
            Long productId,
            String requestedQuantity,
            String message
    ) {
        FridgeItemShareRequest request = new FridgeItemShareRequest();
        request.requesterMemberId = requesterMemberId;
        request.requestedMemberId = requestedMemberId;
        request.sourceFridgeId = sourceFridgeId;
        request.targetFridgeId = targetFridgeId;
        request.fridgeItemId = fridgeItemId;
        request.productId = productId;
        request.requestedQuantity = requestedQuantity;
        request.message = message;
        request.status = FridgeItemShareRequestStatus.PENDING;
        return request;
    }

    @PrePersist
    void prePersist() {
        requestedAt = LocalDateTime.now();
    }
}
