package com.naengpa.naengpamasterbackend.receipt.dto.request;

// OCR로 추출된 상품 후보 1건
public record ReceiptOcrItemRequest(
        String name,
        String quantity
) {
}
