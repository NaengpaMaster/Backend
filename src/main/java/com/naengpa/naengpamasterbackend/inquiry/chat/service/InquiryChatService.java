package com.naengpa.naengpamasterbackend.inquiry.chat.service;

import com.naengpa.naengpamasterbackend.agent.shopping.client.dto.AgentLlmUsageResponse;
import com.naengpa.naengpamasterbackend.agent.usage.service.LlmUsageLogService;
import com.naengpa.naengpamasterbackend.global.exception.InquiryChatSessionNotFoundException;
import com.naengpa.naengpamasterbackend.global.exception.InquiryChatUnavailableException;
import com.naengpa.naengpamasterbackend.inquiry.chat.client.InquiryChatAgentClient;
import com.naengpa.naengpamasterbackend.inquiry.chat.client.dto.InquiryChatAgentRequest;
import com.naengpa.naengpamasterbackend.inquiry.chat.client.dto.InquiryChatAgentResponse;
import com.naengpa.naengpamasterbackend.inquiry.chat.client.dto.InquiryChatHistoryMessage;
import com.naengpa.naengpamasterbackend.inquiry.chat.dto.request.InquiryChatMessageRequest;
import com.naengpa.naengpamasterbackend.inquiry.chat.dto.response.InquiryChatAnswerResponse;
import com.naengpa.naengpamasterbackend.inquiry.chat.entity.InquiryChatMessage;
import com.naengpa.naengpamasterbackend.inquiry.chat.entity.InquiryChatMessageRole;
import com.naengpa.naengpamasterbackend.inquiry.chat.entity.InquiryChatSession;
import com.naengpa.naengpamasterbackend.inquiry.chat.repository.InquiryChatMessageRepository;
import com.naengpa.naengpamasterbackend.inquiry.chat.repository.InquiryChatSessionRepository;
import com.naengpa.naengpamasterbackend.inquiry.knowledge.dto.response.InquiryKnowledgeContextResponse;
import com.naengpa.naengpamasterbackend.inquiry.knowledge.service.InquiryKnowledgeService;
import com.naengpa.naengpamasterbackend.member.entity.Member;
import com.naengpa.naengpamasterbackend.member.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Service
@RequiredArgsConstructor
public class InquiryChatService {

    private static final int CONTEXT_LIMIT = 5;

    private final InquiryChatSessionRepository sessionRepository;
    private final InquiryChatMessageRepository messageRepository;
    private final MemberRepository memberRepository;
    private final InquiryKnowledgeService knowledgeService;
    private final InquiryChatAgentClient agentClient;
    private final LlmUsageLogService usageLogService;

    @Transactional
    public InquiryChatAnswerResponse sendMessage(String email, InquiryChatMessageRequest request) {
        Member member = memberRepository.findByEmail(email)
                .orElseThrow(() -> new BadCredentialsException("회원을 찾을 수 없습니다."));
        String question = request.content().trim();
        InquiryChatSession session = resolveSession(member.getId(), request.conversationSessionId(), question);

        List<InquiryChatHistoryMessage> history = recentHistory(session.getId());
        List<InquiryKnowledgeContextResponse> contexts =
                knowledgeService.findRelevantContexts(question, CONTEXT_LIMIT);

        InquiryChatMessage userMessage = messageRepository.save(
                InquiryChatMessage.create(session.getId(), InquiryChatMessageRole.USER, question)
        );

        try {
            InquiryChatAgentResponse agentResponse = agentClient.answer(
                    new InquiryChatAgentRequest(question, history, contexts)
            );
            if (agentResponse == null || agentResponse.answer() == null || agentResponse.usage() == null) {
                throw new IllegalStateException("Agent 응답이 올바르지 않습니다.");
            }

            InquiryChatMessage answerMessage = messageRepository.save(
                    InquiryChatMessage.create(
                            session.getId(), InquiryChatMessageRole.ASSISTANT, agentResponse.answer()
                    )
            );
            session.touch();
            saveUsage(member.getId(), agentResponse.usage());

            return new InquiryChatAnswerResponse(
                    session.getId(),
                    userMessage.getContent(),
                    answerMessage.getContent(),
                    agentResponse.answerable(),
                    agentResponse.sources() == null ? List.of() : agentResponse.sources(),
                    answerMessage.getCreatedAt()
            );
        } catch (RuntimeException exception) {
            InquiryChatUnavailableException chatException = new InquiryChatUnavailableException(exception);
            usageLogService.saveInquiryFailureLog(member.getId(), "agent-api", chatException.getMessage());
            throw chatException;
        }
    }

    private InquiryChatSession resolveSession(Long memberId, Long sessionId, String question) {
        if (sessionId != null) {
            return sessionRepository.findByIdAndMemberIdAndDeletedFalse(sessionId, memberId)
                    .orElseThrow(InquiryChatSessionNotFoundException::new);
        }

        String title = question.length() <= 30 ? question : question.substring(0, 30);
        return sessionRepository.save(InquiryChatSession.create(memberId, title));
    }

    private List<InquiryChatHistoryMessage> recentHistory(Long sessionId) {
        List<InquiryChatMessage> messages = new ArrayList<>(
                messageRepository.findTop10BySessionIdOrderByCreatedAtDesc(sessionId)
        );
        Collections.reverse(messages);
        return messages.stream().map(InquiryChatHistoryMessage::from).toList();
    }

    private void saveUsage(Long memberId, AgentLlmUsageResponse usage) {
        usageLogService.saveInquirySuccessLog(
                memberId,
                usage.modelName(),
                usage.promptTokens(),
                usage.completionTokens(),
                usage.totalTokens(),
                usage.estimatedCost()
        );
    }
}
