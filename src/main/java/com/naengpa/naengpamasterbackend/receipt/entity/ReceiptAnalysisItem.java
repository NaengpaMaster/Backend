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

    public void updateMatchedProduct(
            Long productId,
            String matchedProductName,
            String quantity,
            LocalDate expiryDate
    ) {
        // OCR 후보를 사용자가 선택한 사전 재료 기준으로 다시 매칭
        this.productId = productId;
        this.normalizedName = matchedProductName;
        this.matchedProductName = matchedProductName;
        this.quantity = quantity;
        this.expiryDate = expiryDate;
        this.updatedAt = LocalDateTime.now();
    }

    public void reject() {
        // 제외된 후보는 냉장고 등록 흐름에서 제외
        this.status = ReceiptAnalysisItemStatus.REJECTED;
        this.updatedAt = LocalDateTime.now();
    }

    public void register() {
        // 냉장고 등록이 끝난 후보는 중복 등록되지 않도록 상태 변경
        this.status = ReceiptAnalysisItemStatus.REGISTERED;
        this.updatedAt = LocalDateTime.now();
    }

    public boolean isPending() {
        return status == ReceiptAnalysisItemStatus.PENDING;
    }

    @PrePersist
    void prePersist() {
        createdAt = LocalDateTime.now();
    }
}
