package com.naengpa.naengpamasterbackend.fridge.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Size;

public record FridgeItemShareRequestAcceptRequest(
        @Schema(description = "전체 전달 여부. true면 요청받은 재료를 현재 냉장고에서 삭제합니다.", example = "false")
        Boolean transferAll,

        @Schema(description = "일부 전달 후 내 냉장고에 남길 수량", example = "1개")
        String remainingQuantity,

        @Schema(description = "받는 냉장고에 남길 메모", example = "요청 수락으로 전달")
        @Size(max = 1000, message = "메모는 1000자 이하여야 합니다.")
        String memo
) {
}
