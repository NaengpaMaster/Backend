package com.naengpa.naengpamasterbackend.receipt.dto.request;

import com.naengpa.naengpamasterbackend.agent.shopping.client.dto.AgentLlmUsageResponse;

import java.util.List;

// Agent OCR 응답을 백엔드 저장/매칭 로직으로 넘기기 위한 요청 DTO
public record ReceiptOcrSaveRequest(
        String rawText,
        List<ReceiptOcrItemRequest> items,
        AgentLlmUsageResponse usage
) {
}
