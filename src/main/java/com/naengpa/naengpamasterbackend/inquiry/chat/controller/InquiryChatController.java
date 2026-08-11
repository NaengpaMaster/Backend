package com.naengpa.naengpamasterbackend.inquiry.chat.controller;

import com.naengpa.naengpamasterbackend.global.response.ApiResponse;
import com.naengpa.naengpamasterbackend.inquiry.chat.dto.request.InquiryChatMessageRequest;
import com.naengpa.naengpamasterbackend.inquiry.chat.dto.response.InquiryChatAnswerResponse;
import com.naengpa.naengpamasterbackend.inquiry.chat.dto.response.InquiryChatMessageResponse;
import com.naengpa.naengpamasterbackend.inquiry.chat.dto.response.InquiryChatSessionResponse;
import com.naengpa.naengpamasterbackend.inquiry.chat.service.InquiryChatQueryService;
import com.naengpa.naengpamasterbackend.inquiry.chat.service.InquiryChatService;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "문의 Q&A 챗봇", description = "사용자 문의 Q&A 챗봇 API")
@SecurityRequirement(name = "bearerAuth")
@RestController
@RequestMapping("/api/v1/inquiry-chat")
@RequiredArgsConstructor
public class InquiryChatController {

    private final InquiryChatService chatService;
    private final InquiryChatQueryService queryService;

    @Operation(summary = "챗봇 질문 전송", description = "새 세션 또는 기존 세션에 질문을 전송하고 정책 문서를 기반으로 생성된 답변을 받습니다.")
    @PostMapping("/messages")
    public ResponseEntity<ApiResponse<InquiryChatAnswerResponse>> sendMessage(
            @Parameter(hidden = true) Authentication authentication,
            @Valid @RequestBody InquiryChatMessageRequest request
    ) {
        return ResponseEntity.ok(ApiResponse.success(chatService.sendMessage(authentication.getName(), request)));
    }

    @Operation(summary = "챗봇 대화 세션 목록 조회", description = "로그인한 사용자의 챗봇 대화 세션을 조회합니다.")
    @GetMapping("/sessions")
    public ResponseEntity<ApiResponse<List<InquiryChatSessionResponse>>> findSessions(
            @Parameter(hidden = true) Authentication authentication
    ) {
        return ResponseEntity.ok(ApiResponse.success(queryService.findSessions(authentication.getName())));
    }

    @Operation(summary = "챗봇 대화 메시지 조회", description = "로그인한 사용자가 소유한 세션의 메시지 목록을 조회합니다.")
    @GetMapping("/sessions/{sessionId}/messages")
    public ResponseEntity<ApiResponse<List<InquiryChatMessageResponse>>> findMessages(
            @Parameter(hidden = true) Authentication authentication,
            @Parameter(description = "챗봇 대화 세션 ID", example = "1") @PathVariable Long sessionId
    ) {
        return ResponseEntity.ok(ApiResponse.success(
                queryService.findMessages(authentication.getName(), sessionId)
        ));
    }
}
