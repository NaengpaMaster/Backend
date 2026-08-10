package com.naengpa.naengpamasterbackend.fridge.dto.response;

import com.naengpa.naengpamasterbackend.fridge.entity.FridgeItemShareRequest;
import com.naengpa.naengpamasterbackend.fridge.entity.FridgeItemShareRequestStatus;

import java.time.LocalDateTime;

public record FridgeItemShareRequestResponse(
        Long shareRequestId,
        Long requesterMemberId,
        Long requestedMemberId,
        Long sourceFridgeId,
        Long targetFridgeId,
        Long fridgeItemId,
        Long productId,
        String productName,
        String requestedQuantity,
        String message,
        FridgeItemShareRequestStatus status,
        LocalDateTime requestedAt
) {
    public static FridgeItemShareRequestResponse of(FridgeItemShareRequest request, String productName) {
        return new FridgeItemShareRequestResponse(
                request.getFridgeItemShareRequestId(),
                request.getRequesterMemberId(),
                request.getRequestedMemberId(),
                request.getSourceFridgeId(),
                request.getTargetFridgeId(),
                request.getFridgeItemId(),
                request.getProductId(),
                productName,
                request.getRequestedQuantity(),
                request.getMessage(),
                request.getStatus(),
                request.getRequestedAt()
        );
    }
}
