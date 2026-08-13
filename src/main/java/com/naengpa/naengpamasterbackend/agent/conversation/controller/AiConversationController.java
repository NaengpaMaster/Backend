package com.naengpa.naengpamasterbackend.agent.conversation.controller;

import com.naengpa.naengpamasterbackend.agent.conversation.dto.response.ConversationMessageResponse;
import com.naengpa.naengpamasterbackend.agent.conversation.dto.response.ConversationSessionResponse;
import com.naengpa.naengpamasterbackend.agent.conversation.service.ConversationQueryService;
import com.naengpa.naengpamasterbackend.global.response.ApiResponse;
import io.swagger.v3.oas.annotations.Parameter;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/ai/conversation-sessions")
public class AiConversationController {

    private final ConversationQueryService conversationQueryService;

    public AiConversationController(ConversationQueryService conversationQueryService) {
        this.conversationQueryService = conversationQueryService;
    }

    // API-704
    // 로그인한 회원의 AI 대화 세션 목록을 조회
    @GetMapping
    public ResponseEntity<ApiResponse<List<ConversationSessionResponse>>> findConversationSessions(
            @Parameter(hidden = true) Authentication authentication
    ) {
        List<ConversationSessionResponse> response =
                conversationQueryService.findConversationSessions(authentication.getName());

        return ResponseEntity.ok(
                ApiResponse.success("AI 대화 세션 목록 조회에 성공했습니다.", response)
        );
    }

    // API-705
    // 특정 AI 대화 세션 안에 저장된 USER/ASSISTANT 메시지 목록을 조회
    @GetMapping("/{sessionId}/messages")
    public ResponseEntity<ApiResponse<List<ConversationMessageResponse>>> findConversationMessages(
            @Parameter(hidden = true) Authentication authentication,
            @PathVariable Long sessionId
    ) {
        // sessionId만으로 바로 조회하지 않고, 서비스에서 현재 회원의 세션인지 먼저 검증
        List<ConversationMessageResponse> response =
                conversationQueryService.findConversationMessages(
                        authentication.getName(),
                        sessionId
                );

        return ResponseEntity.ok(
                ApiResponse.success("AI 대화 메시지 목록 조회에 성공했습니다.", response)
        );
    }
}
