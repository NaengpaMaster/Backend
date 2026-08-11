package com.naengpa.naengpamasterbackend.inquiry.chat.controller;

import com.naengpa.naengpamasterbackend.global.response.ApiResponse;
import com.naengpa.naengpamasterbackend.inquiry.chat.dto.request.InquiryChatMessageRequest;
import com.naengpa.naengpamasterbackend.inquiry.chat.dto.response.InquiryChatAnswerResponse;
import com.naengpa.naengpamasterbackend.inquiry.chat.dto.response.InquiryChatMessageResponse;
import com.naengpa.naengpamasterbackend.inquiry.chat.dto.response.InquiryChatSessionResponse;
import com.naengpa.naengpamasterbackend.inquiry.chat.service.InquiryChatQueryService;
import com.naengpa.naengpamasterbackend.inquiry.chat.service.InquiryChatService;
import io.swagger.v3.oas.annotations.Parameter;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/inquiry-chat")
@RequiredArgsConstructor
public class InquiryChatController {

    private final InquiryChatService chatService;
    private final InquiryChatQueryService queryService;

    @PostMapping("/messages")
    public ResponseEntity<ApiResponse<InquiryChatAnswerResponse>> sendMessage(
            @Parameter(hidden = true) Authentication authentication,
            @Valid @RequestBody InquiryChatMessageRequest request
    ) {
        return ResponseEntity.ok(ApiResponse.success(chatService.sendMessage(authentication.getName(), request)));
    }

    @GetMapping("/sessions")
    public ResponseEntity<ApiResponse<List<InquiryChatSessionResponse>>> findSessions(
            @Parameter(hidden = true) Authentication authentication
    ) {
        return ResponseEntity.ok(ApiResponse.success(queryService.findSessions(authentication.getName())));
    }

    @GetMapping("/sessions/{sessionId}/messages")
    public ResponseEntity<ApiResponse<List<InquiryChatMessageResponse>>> findMessages(
            @Parameter(hidden = true) Authentication authentication,
            @PathVariable Long sessionId
    ) {
        return ResponseEntity.ok(ApiResponse.success(
                queryService.findMessages(authentication.getName(), sessionId)
        ));
    }
}
