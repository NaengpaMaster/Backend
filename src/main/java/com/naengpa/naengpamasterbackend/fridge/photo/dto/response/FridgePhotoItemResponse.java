package com.naengpa.naengpamasterbackend.fridge.photo.dto.response;

import com.naengpa.naengpamasterbackend.receipt.entity.ReceiptAnalysisItem;
import com.naengpa.naengpamasterbackend.receipt.entity.ReceiptAnalysisItemStatus;

import java.time.LocalDate;

public record FridgePhotoItemResponse(
        Long fridgePhotoItemId,
        Long productId,
        String extractedName,
        String normalizedName,
        String matchedProductName,
        String quantity,
        LocalDate expiryDate,
        ReceiptAnalysisItemStatus status,
        String memo
) {
    public static FridgePhotoItemResponse from(ReceiptAnalysisItem item) {
        return new FridgePhotoItemResponse(
                item.getReceiptAnalysisItemId(),
                item.getProductId(),
                item.getExtractedName(),
                item.getNormalizedName(),
                item.getMatchedProductName(),
                item.getQuantity(),
                item.getExpiryDate(),
                item.getStatus(),
                item.getMemo()
        );
    }
}
