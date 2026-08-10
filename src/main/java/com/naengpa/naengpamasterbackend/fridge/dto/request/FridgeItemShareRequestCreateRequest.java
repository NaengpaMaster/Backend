package com.naengpa.naengpamasterbackend.fridge.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record FridgeItemShareRequestCreateRequest(
        @Schema(description = "요청할 재료가 있는 냉장고 ID", example = "1")
        @NotNull(message = "요청할 냉장고를 선택해주세요.")
        Long sourceFridgeId,

        @Schema(description = "요청자가 받을 냉장고 ID", example = "2")
        @NotNull(message = "받을 냉장고를 선택해주세요.")
        Long targetFridgeId,

        @Schema(description = "요청 수량", example = "2개")
        @NotBlank(message = "요청 수량을 입력해주세요.")
        @Size(max = 100, message = "요청 수량은 100자 이하여야 합니다.")
        String requestedQuantity,

        @Schema(description = "요청 메시지", example = "양파 2개만 나눠줄 수 있어?")
        @Size(max = 1000, message = "요청 메시지는 1000자 이하여야 합니다.")
        String message
) {
}
