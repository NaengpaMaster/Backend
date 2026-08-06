package com.naengpa.naengpamasterbackend.agent.conversation.service;

import com.naengpa.naengpamasterbackend.agent.conversation.dto.response.ConversationMessageResponse;
import com.naengpa.naengpamasterbackend.agent.conversation.dto.response.ConversationSessionResponse;
import com.naengpa.naengpamasterbackend.agent.conversation.entity.ConversationSession;
import com.naengpa.naengpamasterbackend.agent.conversation.repository.ConversationMessageRepository;
import com.naengpa.naengpamasterbackend.agent.conversation.repository.ConversationSessionRepository;
import com.naengpa.naengpamasterbackend.global.exception.AiConversationSessionNotFoundException;
import com.naengpa.naengpamasterbackend.member.entity.Member;
import com.naengpa.naengpamasterbackend.member.repository.MemberRepository;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ConversationQueryService {

    private final ConversationMessageRepository conversationMessageRepository;
    private final ConversationSessionRepository conversationSessionRepository;
    private final MemberRepository memberRepository;

    public ConversationQueryService(
            ConversationMessageRepository conversationMessageRepository,
            ConversationSessionRepository conversationSessionRepository,
            MemberRepository memberRepository
    ) {
        this.conversationMessageRepository = conversationMessageRepository;
        this.conversationSessionRepository = conversationSessionRepository;
        this.memberRepository = memberRepository;
    }

    // 로그인한 회원의 AI 대화방 목록을 최신순으로 조회
    public List<ConversationSessionResponse> findConversationSessions(String email) {
        Member member = findMemberByEmail(email);

        return conversationSessionRepository
                .findByMemberIdAndIsDeletedFalseOrderByCreatedAtDesc(member.getId())
                .stream()
                .map(ConversationSessionResponse::from)
                .toList();
    }

    // 특정 AI 대화방의 메시지를 조회
    // 먼저 세션 소유자를 확인해서 다른 회원의 대화 기록을 조회하지 못하게 막음
    public List<ConversationMessageResponse> findConversationMessages(
            String email,
            Long sessionId
    ) {
        Member member = findMemberByEmail(email);

        ConversationSession conversationSession = conversationSessionRepository
                .findByConversationSessionIdAndMemberIdAndIsDeletedFalse(
                        sessionId,
                        member.getId()
                )
                .orElseThrow(AiConversationSessionNotFoundException::new);

        return conversationMessageRepository
                .findByConversationSessionIdOrderByCreatedAtAsc(
                        conversationSession.getConversationSessionId()
                )
                .stream()
                .map(ConversationMessageResponse::from)
                .toList();
    }

    // Spring Security에서 받은 email로 실제 회원 정보를 찾음
    // 이후 조회 로직은 memberId 기준으로 진행
    private Member findMemberByEmail(String email) {
        return memberRepository.findByEmail(email)
                .orElseThrow(() -> new BadCredentialsException("회원을 찾을 수 없습니다."));
    }
}
