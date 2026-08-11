package com.naengpa.naengpamasterbackend.admin.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import io.swagger.v3.oas.annotations.media.Schema;

public record AdminAnswerRequest(
        @Schema(description = "문의 답변 내용", example = "문의하신 기능은 냉장고 메뉴에서 이용할 수 있습니다.", maxLength = 2000)
        @NotBlank(message = "답변 내용은 필수입니다.")
        @Size(max = 2000, message = "답변 내용은 2000자 이하여야 합니다.")
        String content
) {}
