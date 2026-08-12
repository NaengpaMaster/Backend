package com.naengpa.naengpamasterbackend.fridge.photo.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record FridgePhotoItemUpdateRequest(
        @NotNull Long productId,
        @NotBlank String quantity
) {
}
