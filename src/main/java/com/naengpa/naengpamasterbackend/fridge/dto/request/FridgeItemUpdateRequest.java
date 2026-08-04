package com.naengpa.naengpamasterbackend.fridge.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

public record FridgeItemUpdateRequest(
        @Schema(description = "사전 재료 ID", example = "1")
        @NotNull(message = "재료를 선택해주세요.")
        Long productId,

        @Schema(description = "수량", example = "2개")
        @NotBlank(message = "수량을 입력해주세요.")
        String quantity,

        @Schema(description = "유통기한", example = "2026-08-04")
        LocalDate expiryDate,

        @Schema(description = "메모", example = "빨리 먹기")
        @Size(max = 1000, message = "메모는 1000자 이하여야 합니다.")
        String memo
) {
}
