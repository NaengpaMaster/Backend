package com.naengpa.naengpamasterbackend.receipt.entity;

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

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "receipt_analysis_items")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ReceiptAnalysisItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "receipt_analysis_item_id")
    private Long receiptAnalysisItemId;

    @Column(name = "receipt_analysis_id", nullable = false)
    private Long receiptAnalysisId;

    @Column(name = "product_id")
    private Long productId;

    // Agent가 읽은 원래 이름
    @Column(name = "extracted_name", nullable = false)
    private String extractedName;

    // 백엔드에서 정제한 이름
    @Column(name = "normalized_name")
    private String normalizedName;

    // 실제 사전 재료 이름
    @Column(name = "matched_product_name")
    private String matchedProductName;

    @Column(nullable = false, length = 50)
    private String quantity;

    @Column(name = "expiry_date")
    private LocalDate expiryDate;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private ReceiptAnalysisItemStatus status;

    @Column(length = 1000)
    private String memo;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    public static ReceiptAnalysisItem createPending(
            Long receiptAnalysisId,
            Long productId,
            String extractedName,
            String normalizedName,
            String matchedProductName,
            String quantity,
            LocalDate expiryDate
    ) {
        ReceiptAnalysisItem item = new ReceiptAnalysisItem();
        item.receiptAnalysisId = receiptAnalysisId;
        item.productId = productId;
        item.extractedName = extractedName;
        item.normalizedName = normalizedName;
        item.matchedProductName = matchedProductName;
        item.quantity = quantity;
        item.expiryDate = expiryDate;
        item.status = ReceiptAnalysisItemStatus.PENDING;
        item.memo = "영수증으로 일괄 등록";
        return item;
    }

    @PrePersist
    void prePersist() {
        createdAt = LocalDateTime.now();
    }
}