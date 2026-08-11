package com.naengpa.naengpamasterbackend.receipt.dto.response;

import com.naengpa.naengpamasterbackend.receipt.entity.ReceiptAnalysis;
import com.naengpa.naengpamasterbackend.receipt.entity.ReceiptAnalysisStatus;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "영수증 이미지 업로드 응답")
public record ReceiptImageUploadResponse(
        @Schema(description = "영수증 분석 ID", example = "1")
        Long receiptAnalysisId,

        @Schema(description = "원본 파일명", example = "receipt.jpg")
        String originalFileName,

        @Schema(description = "영수증 분석 상태", example = "PENDING")
        ReceiptAnalysisStatus status
) {
    public static ReceiptImageUploadResponse from(ReceiptAnalysis receiptAnalysis) {
        return new ReceiptImageUploadResponse(
                receiptAnalysis.getReceiptAnalysisId(),
                receiptAnalysis.getOriginalFileName(),
                receiptAnalysis.getStatus()
        );
    }
}
