package com.naengpa.naengpamasterbackend.shopping.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDate;

public record ShoppingItemMoveToFridgeRequest (
        @Schema(description = "유통기한. 값이 없으면 사전 재료 기본 유통기한을 적용합니다.", example = "2026-08-04")
        LocalDate expiryDate,
        @Schema(description = "냉장고 등록 메모", example = "장보기에서 냉장고로 추가")
        String memo
){
}
