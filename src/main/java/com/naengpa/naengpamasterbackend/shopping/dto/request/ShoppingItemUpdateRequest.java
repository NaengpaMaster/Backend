package com.naengpa.naengpamasterbackend.shopping.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

public record ShoppingItemUpdateRequest(
        @Schema(description = "수량", example = "2개")
        @NotBlank(message = "수량을 입력해주세요.")
        String quantity
) {
}
