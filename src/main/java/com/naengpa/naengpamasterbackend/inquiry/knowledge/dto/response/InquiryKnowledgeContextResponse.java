package com.naengpa.naengpamasterbackend.inquiry.knowledge.dto.response;

import com.naengpa.naengpamasterbackend.inquiry.knowledge.repository.InquiryKnowledgeContextProjection;

public record InquiryKnowledgeContextResponse(
        String sourceName,
        String content
) {
    public static InquiryKnowledgeContextResponse from(InquiryKnowledgeContextProjection projection) {
        return new InquiryKnowledgeContextResponse(
                projection.getSourceName(),
                projection.getContent()
        );
    }
}

