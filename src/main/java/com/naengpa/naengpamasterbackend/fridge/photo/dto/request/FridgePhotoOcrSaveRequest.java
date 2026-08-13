package com.naengpa.naengpamasterbackend.fridge.photo.dto.request;

import com.naengpa.naengpamasterbackend.agent.shopping.client.dto.AgentLlmUsageResponse;

import java.util.List;

public record FridgePhotoOcrSaveRequest(
        String rawText,
        List<FridgePhotoOcrItemRequest> items,
        AgentLlmUsageResponse usage
) {
}
