package com.naengpa.naengpamasterbackend.fridge.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

public record FridgeItemTransferRequest(
        @Schema(description = "보내는 냉장고 ID", example = "1")
        @NotNull(message = "보내는 냉장고를 선택해주세요.")
        Long sourceFridgeId,

        @Schema(description = "받는 냉장고 ID", example = "2")
        @NotNull(message = "받는 냉장고를 선택해주세요.")
        Long targetFridgeId,

        @Schema(description = "상대 냉장고로 보낼 수량", example = "2개")
        @NotBlank(message = "보낼 수량을 입력해주세요.")
        String transferQuantity,

        @Schema(description = "전체 전달 여부. true면 기존 냉장고 재료를 삭제합니다.", example = "false")
        Boolean transferAll,

        @Schema(description = "내 냉장고에 남길 수량. 비우면 전체 전달", example = "1개")
        String remainingQuantity,

        @Schema(description = "받는 냉장고에 등록할 유통기한", example = "2026-08-15")
        LocalDate expiryDate,

        @Schema(description = "받는 냉장고에 남길 메모", example = "부모님께 전달")
        @Size(max = 1000, message = "메모는 1000자 이하여야 합니다.")
        String memo
) {
}
