package com.naengpa.naengpamasterbackend.inquiry.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import io.swagger.v3.oas.annotations.media.Schema;

public record InquiryRequest(
        @Schema(description = "문의 제목", example = "냉장고 재료 등록 방법 문의", maxLength = 300)
        @NotBlank(message = "title은 필수입니다.")
        @Size(max = 300, message = "title은 300자 이하여야 합니다.")
        String title,

        @Schema(description = "문의 내용", example = "냉장고에 재료를 등록하는 방법을 알려주세요.", maxLength = 1000)
        @NotBlank(message = "content는 필수입니다.")
        @Size(max = 1000, message = "content는 1000자 이하여야 합니다.")
        String content
) {}
