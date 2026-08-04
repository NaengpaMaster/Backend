package com.naengpa.naengpamasterbackend.shopping.dto.request;


import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.NotBlank;

public record ShoppingItemCreateRequest (

    @Schema(description = "사전 재료 ID", example = "1")
    @NotNull(message = "재료를 선택해주세요.")
    Long productId,

    @Schema(description = "수량", example = "1개")
    @NotBlank(message = "수량을 입력해주세요.")
    String quantity
) {}
