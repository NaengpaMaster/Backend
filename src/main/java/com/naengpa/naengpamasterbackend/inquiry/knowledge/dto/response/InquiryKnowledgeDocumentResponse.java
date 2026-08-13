package com.naengpa.naengpamasterbackend.inquiry.knowledge.dto.response;

import com.naengpa.naengpamasterbackend.inquiry.knowledge.entity.InquiryKnowledgeDocument;
import io.swagger.v3.oas.annotations.media.Schema;

public record InquiryKnowledgeDocumentResponse(
        @Schema(description = "정책 문서 ID", example = "1")
        Long documentId,
        @Schema(description = "정책 문서 제목", example = "냉장고 및 사전재료 이용 정책")
        String title,
        @Schema(description = "정책 문서 출처 파일명", example = "product-policy.md")
        String sourceName,
        @Schema(description = "정책 문서 버전", example = "2")
        int version,
        @Schema(description = "생성된 검색용 청크 수", example = "3")
        int chunkCount
) {
    public static InquiryKnowledgeDocumentResponse from(
            InquiryKnowledgeDocument document,
            int chunkCount
    ) {
        return new InquiryKnowledgeDocumentResponse(
                document.getId(),
                document.getTitle(),
                document.getSourceName(),
                document.getVersion(),
                chunkCount
        );
    }
}
