package com.naengpa.naengpamasterbackend.fridge.photo.dto.response;

import com.naengpa.naengpamasterbackend.receipt.entity.ReceiptAnalysis;
import com.naengpa.naengpamasterbackend.receipt.entity.ReceiptAnalysisStatus;
import io.swagger.v3.oas.annotations.media.Schema;

public record FridgePhotoImageUploadResponse(
        @Schema(description = "냉장고 사진 분석 ID", example = "1")
        Long fridgePhotoAnalysisId,
        @Schema(description = "원본 파일명", example = "fridge.jpg")
        String originalFileName,
        @Schema(description = "분석 상태", example = "PENDING")
        ReceiptAnalysisStatus status
) {
    public static FridgePhotoImageUploadResponse from(ReceiptAnalysis analysis) {
        return new FridgePhotoImageUploadResponse(
                analysis.getReceiptAnalysisId(),
                analysis.getOriginalFileName(),
                analysis.getStatus()
        );
    }
}
