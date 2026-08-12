package com.naengpa.naengpamasterbackend.receipt.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record ReceiptAnalysisItemUpdateRequest(
        @NotNull(message = "재료를 선택해주세요.")
        Long productId,
        @NotBlank(message = "수량을 입력해주세요.")
        String quantity
) {
}
