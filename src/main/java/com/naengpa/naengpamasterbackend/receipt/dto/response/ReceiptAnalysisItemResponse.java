package com.naengpa.naengpamasterbackend.receipt.dto.response;

import com.naengpa.naengpamasterbackend.receipt.entity.ReceiptAnalysisItem;
import com.naengpa.naengpamasterbackend.receipt.entity.ReceiptAnalysisItemStatus;

import java.time.LocalDate;

public record ReceiptAnalysisItemResponse(
        Long receiptAnalysisItemId,
        Long productId,
        String extractedName,
        String normalizedName,
        String matchedProductName,
        String quantity,
        LocalDate expiryDate,
        ReceiptAnalysisItemStatus status,
        String memo
) {

    // Entity 전체를 노출하지 않고 화면에 필요한 후보 항목 값만 응답 DTO로 변환
    public static ReceiptAnalysisItemResponse from(ReceiptAnalysisItem item) {
        return new ReceiptAnalysisItemResponse(
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
