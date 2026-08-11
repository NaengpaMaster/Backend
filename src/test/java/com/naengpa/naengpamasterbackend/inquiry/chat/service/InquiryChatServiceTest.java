package com.naengpa.naengpamasterbackend.inquiry.chat.service;

import com.naengpa.naengpamasterbackend.agent.shopping.client.dto.AgentLlmUsageResponse;
import com.naengpa.naengpamasterbackend.agent.usage.service.LlmUsageLogService;
import com.naengpa.naengpamasterbackend.global.exception.InquiryChatUnavailableException;
import com.naengpa.naengpamasterbackend.inquiry.chat.client.InquiryChatAgentClient;
import com.naengpa.naengpamasterbackend.inquiry.chat.client.dto.InquiryChatAgentResponse;
import com.naengpa.naengpamasterbackend.inquiry.chat.dto.request.InquiryChatMessageRequest;
import com.naengpa.naengpamasterbackend.inquiry.chat.dto.response.InquiryChatAnswerResponse;
import com.naengpa.naengpamasterbackend.inquiry.chat.entity.InquiryChatMessage;
import com.naengpa.naengpamasterbackend.inquiry.chat.entity.InquiryChatSession;
import com.naengpa.naengpamasterbackend.inquiry.chat.repository.InquiryChatMessageRepository;
import com.naengpa.naengpamasterbackend.inquiry.chat.repository.InquiryChatSessionRepository;
import com.naengpa.naengpamasterbackend.inquiry.knowledge.service.InquiryKnowledgeService;
import com.naengpa.naengpamasterbackend.member.entity.Member;
import com.naengpa.naengpamasterbackend.member.repository.MemberRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class InquiryChatServiceTest {

    @Mock InquiryChatSessionRepository sessionRepository;
    @Mock InquiryChatMessageRepository messageRepository;
    @Mock MemberRepository memberRepository;
    @Mock InquiryKnowledgeService knowledgeService;
    @Mock InquiryChatAgentClient agentClient;
    @Mock LlmUsageLogService usageLogService;

    private InquiryChatService service;

    @BeforeEach
    void setUp() {
        service = new InquiryChatService(
                sessionRepository,
                messageRepository,
                memberRepository,
                knowledgeService,
                agentClient,
                usageLogService
        );
    }

    @Test
    void createsSessionAndStoresQuestionAndAnswer() {
        Member member = mock(Member.class);
        InquiryChatSession session = mock(InquiryChatSession.class);
        AgentLlmUsageResponse usage = new AgentLlmUsageResponse("gpt-4.1-mini", 10, 20, 30, BigDecimal.ZERO);

        when(member.getId()).thenReturn(1L);
        when(memberRepository.findByEmail("member@test.com")).thenReturn(Optional.of(member));
        when(session.getId()).thenReturn(7L);
        when(sessionRepository.save(any(InquiryChatSession.class))).thenReturn(session);
        when(messageRepository.findTop10BySessionIdOrderByCreatedAtDesc(7L)).thenReturn(List.of());
        when(messageRepository.save(any(InquiryChatMessage.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(agentClient.answer(any())).thenReturn(new InquiryChatAgentResponse("답변", true, List.of("inquiry.md"), usage));

        InquiryChatAnswerResponse response = service.sendMessage(
                "member@test.com",
                new InquiryChatMessageRequest(null, "문의 등록은 어디서 해?")
        );

        assertThat(response.conversationSessionId()).isEqualTo(7L);
        assertThat(response.answer()).isEqualTo("답변");
        assertThat(response.answerable()).isTrue();
        assertThat(response.sources()).containsExactly("inquiry.md");
        verify(messageRepository, times(2)).save(any(InquiryChatMessage.class));
        verify(usageLogService).saveInquirySuccessLog(eq(1L), eq("gpt-4.1-mini"), eq(10), eq(20), eq(30), eq(BigDecimal.ZERO));
    }

    @Test
    void convertsAgentFailureToKoreanDomainMessage() {
        Member member = mock(Member.class);
        InquiryChatSession session = mock(InquiryChatSession.class);

        when(member.getId()).thenReturn(1L);
        when(memberRepository.findByEmail("member@test.com")).thenReturn(Optional.of(member));
        when(session.getId()).thenReturn(7L);
        when(sessionRepository.save(any(InquiryChatSession.class))).thenReturn(session);
        when(messageRepository.findTop10BySessionIdOrderByCreatedAtDesc(7L)).thenReturn(List.of());
        when(messageRepository.save(any(InquiryChatMessage.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(agentClient.answer(any())).thenThrow(new RuntimeException("I/O error"));

        assertThatThrownBy(() -> service.sendMessage(
                "member@test.com",
                new InquiryChatMessageRequest(null, "문의 등록은 어디서 해?")
        ))
                .isInstanceOf(InquiryChatUnavailableException.class)
                .hasMessage("문의 챗봇 응답을 생성하지 못했습니다. 잠시 후 다시 시도해주세요.");

        verify(usageLogService).saveInquiryFailureLog(
                1L,
                "agent-api",
                "문의 챗봇 응답을 생성하지 못했습니다. 잠시 후 다시 시도해주세요."
        );
    }
}
