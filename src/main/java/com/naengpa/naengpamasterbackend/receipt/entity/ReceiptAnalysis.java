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

import java.time.LocalDateTime;

@Entity
@Table(name = "receipt_analysis")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ReceiptAnalysis {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "receipt_analysis_id")
    private Long receiptAnalysisId;

    @Column(name = "member_id", nullable = false)
    private Long memberId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private ReceiptAnalysisStatus status;

    @Column(name = "original_file_name")
    private String originalFileName;

    @Column(name = "s3_object_key", length = 500)
    private String s3ObjectKey;

    @Column(name = "image_deleted", nullable = false)
    private Boolean imageDeleted;

    // OCR이 문읽어낸 영수증 전체 텍스트를 저장. 길이가 길 수 있어 DB TEXT 타입으로 매핑
    @Column(name = "raw_ocr_text", columnDefinition = "TEXT")
    private String rawOcrText;

    @Column(name = "error_message", columnDefinition = "TEXT")
    private String errorMessage;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    public static ReceiptAnalysis createPending(
            Long memberId,
            String originalFileName,
            String s3ObjectKey
    ) {
        ReceiptAnalysis receiptAnalysis = new ReceiptAnalysis();
        receiptAnalysis.memberId = memberId;
        receiptAnalysis.status = ReceiptAnalysisStatus.PENDING;
        receiptAnalysis.originalFileName = originalFileName;
        receiptAnalysis.s3ObjectKey = s3ObjectKey;
        receiptAnalysis.imageDeleted = false;
        return receiptAnalysis;
    }

    // 새 영수증 분석 row가 처음 저장되기 직전에 생성 시각을 자동으로 채움
    @PrePersist
    void prePersist() {
        createdAt = LocalDateTime.now();
    }
}
