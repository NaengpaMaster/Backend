package com.naengpa.naengpamasterbackend.inquiry.knowledge.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record InquiryKnowledgeDocumentRequest(
        @NotBlank(message = "정책 문서 제목은 필수입니다.")
        @Size(max = 200, message = "정책 문서 제목은 200자 이하여야 합니다.")
        String title,

        @NotBlank(message = "정책 문서 내용은 필수입니다.")
        String content,

        @NotBlank(message = "정책 문서 출처 이름은 필수입니다.")
        @Size(max = 255, message = "정책 문서 출처 이름은 255자 이하여야 합니다.")
        String sourceName
) {
}

