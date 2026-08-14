package com.naengpa.naengpamasterbackend.community.share.dto.request;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;

import java.math.BigDecimal;

public record CommunitySharePostSearchRequest(
        @DecimalMin(value = "-90.0", message = "위도 값이 올바르지 않습니다.")
        @DecimalMax(value = "90.0", message = "위도 값이 올바르지 않습니다.")
        BigDecimal latitude,

        @DecimalMin(value = "-180.0", message = "경도 값이 올바르지 않습니다.")
        @DecimalMax(value = "180.0", message = "경도 값이 올바르지 않습니다.")
        BigDecimal longitude,

        @DecimalMin(value = "0.5", message = "검색 반경은 500m 이상이어야 합니다.")
        @DecimalMax(value = "2.0", message = "검색 반경은 2km 이하로 입력해주세요.")
        BigDecimal radiusKm,

        @Min(value = 0, message = "페이지 번호는 0 이상이어야 합니다.")
        Integer page
) {
}
