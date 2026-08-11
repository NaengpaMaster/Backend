package com.naengpa.naengpamasterbackend.inquiry.chat.client.dto;

import com.naengpa.naengpamasterbackend.inquiry.knowledge.dto.response.InquiryKnowledgeContextResponse;

import java.util.List;

public record InquiryChatAgentRequest(
        String question,
        List<InquiryChatHistoryMessage> history,
        List<InquiryKnowledgeContextResponse> contexts
) {
}
