package com.naengpa.naengpamasterbackend.inquiry.knowledge.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import io.swagger.v3.oas.annotations.media.Schema;

public record InquiryKnowledgeDocumentRequest(
        @Schema(description = "정책 문서 제목", example = "냉장고 및 사전재료 이용 정책", maxLength = 200)
        @NotBlank(message = "정책 문서 제목은 필수입니다.")
        @Size(max = 200, message = "정책 문서 제목은 200자 이하여야 합니다.")
        String title,

        @Schema(description = "정책 문서 본문", example = "일반 회원은 사전재료를 직접 등록할 수 없습니다.")
        @NotBlank(message = "정책 문서 내용은 필수입니다.")
        String content,

        @Schema(description = "정책 문서 출처 파일명", example = "product-policy.md", maxLength = 255)
        @NotBlank(message = "정책 문서 출처 이름은 필수입니다.")
        @Size(max = 255, message = "정책 문서 출처 이름은 255자 이하여야 합니다.")
        String sourceName
) {
}
