package com.naengpa.naengpamasterbackend.fridge.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

public record FridgeItemUsePartialRequest(
        @Schema(description = "남은 수량", example = "100g")
        @NotBlank(message = "남은 수량을 입력해주세요.")
        String quantity
) {
}
