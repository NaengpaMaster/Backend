package com.naengpa.naengpamasterbackend.community.share.dto.request;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

public record CommunitySharePostCreateRequest(
        @NotBlank(message = "제목을 입력해주세요.")
        @Size(max = 100, message = "제목은 100자 이하로 입력해주세요.")
        String title,

        @NotNull(message = "나눔할 재료를 선택해주세요.")
        Long productId,

        @NotBlank(message = "나눌 수량을 입력해주세요.")
        @Size(max = 100, message = "수량은 100자 이하로 입력해주세요.")
        String quantity,

        @NotNull(message = "총 구매 금액을 입력해주세요.")
        @Min(value = 0, message = "총 구매 금액은 0원 이상이어야 합니다.")
        @Max(value = 1_000_000, message = "총 구매 금액은 100만원 이하로 입력해주세요.")
        Integer totalPrice,

        @NotNull(message = "모집 인원을 입력해주세요.")
        @Min(value = 2, message = "모집 인원은 최소 2명입니다.")
        @Max(value = 20, message = "모집 인원은 최대 20명입니다.")
        Integer participantLimit,

        @NotNull(message = "위도 정보가 필요합니다.")
        @DecimalMin(value = "-90.0", message = "위도 값이 올바르지 않습니다.")
        @DecimalMax(value = "90.0", message = "위도 값이 올바르지 않습니다.")
        BigDecimal latitude,

        @NotNull(message = "경도 정보가 필요합니다.")
        @DecimalMin(value = "-180.0", message = "경도 값이 올바르지 않습니다.")
        @DecimalMax(value = "180.0", message = "경도 값이 올바르지 않습니다.")
        BigDecimal longitude,

        @NotBlank(message = "거래 위치를 입력해주세요.")
        @Size(max = 255, message = "거래 위치는 255자 이하로 입력해주세요.")
        String address,

        @NotBlank(message = "상세 설명을 입력해주세요.")
        @Size(max = 1000, message = "설명은 1000자 이하로 입력해주세요.")
        String description
) {
}
