package com.naengpa.naengpamasterbackend.shopping.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

public record ShoppingItemCheckRequest (
    @Schema(description = "구매 여부", example = "true")
    @NotNull(message = "구매 여부를 선택해주세요.")
    Boolean isPurchased
){}
