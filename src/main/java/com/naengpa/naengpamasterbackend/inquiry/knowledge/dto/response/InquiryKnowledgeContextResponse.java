package com.naengpa.naengpamasterbackend.inquiry.knowledge.dto.response;

import com.naengpa.naengpamasterbackend.inquiry.knowledge.repository.InquiryKnowledgeContextProjection;
import io.swagger.v3.oas.annotations.media.Schema;

public record InquiryKnowledgeContextResponse(
        @Schema(description = "검색된 정책 문서 출처", example = "product-policy.md")
        String sourceName,
        @Schema(description = "질문과 관련된 정책 문서 청크 내용", example = "일반 회원은 사전재료를 직접 등록할 수 없습니다.")
        String content
) {
    public static InquiryKnowledgeContextResponse from(InquiryKnowledgeContextProjection projection) {
        return new InquiryKnowledgeContextResponse(
                projection.getSourceName(),
                projection.getContent()
        );
    }
}
