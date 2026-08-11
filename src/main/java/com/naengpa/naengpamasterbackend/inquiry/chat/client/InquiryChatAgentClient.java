package com.naengpa.naengpamasterbackend.inquiry.chat.client;

import com.naengpa.naengpamasterbackend.inquiry.chat.client.dto.InquiryChatAgentRequest;
import com.naengpa.naengpamasterbackend.inquiry.chat.client.dto.InquiryChatAgentResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Component
public class InquiryChatAgentClient {

    private final RestClient restClient;

    public InquiryChatAgentClient(
            RestClient.Builder restClientBuilder,
            @Value("${agent.base-url}") String agentBaseUrl,
            @Value("${agent.api-key}") String agentApiKey
    ) {
        this.restClient = restClientBuilder
                .baseUrl(agentBaseUrl)
                .defaultHeader("X-Agent-Api-Key", agentApiKey)
                .build();
    }

    public InquiryChatAgentResponse answer(InquiryChatAgentRequest request) {
        return restClient.post()
                .uri("/agent/v1/inquiry-chat/answers")
                .body(request)
                .retrieve()
                .body(InquiryChatAgentResponse.class);
    }
}
