package com.naengpa.naengpamasterbackend.inquiry.chat.client.dto;

import com.naengpa.naengpamasterbackend.agent.shopping.client.dto.AgentLlmUsageResponse;

import java.util.List;

public record InquiryChatAgentResponse(
        String answer,
        boolean answerable,
        List<String> sources,
        AgentLlmUsageResponse usage
) {
}
