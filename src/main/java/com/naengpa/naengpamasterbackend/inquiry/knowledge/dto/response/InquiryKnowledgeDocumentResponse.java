package com.naengpa.naengpamasterbackend.inquiry.knowledge.dto.response;

import com.naengpa.naengpamasterbackend.inquiry.knowledge.entity.InquiryKnowledgeDocument;

public record InquiryKnowledgeDocumentResponse(
        Long documentId,
        String title,
        String sourceName,
        int version,
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

