package com.naengpa.naengpamasterbackend.inquiry.chat.service;

import com.naengpa.naengpamasterbackend.global.exception.InquiryChatSessionNotFoundException;
import com.naengpa.naengpamasterbackend.inquiry.chat.dto.response.InquiryChatMessageResponse;
import com.naengpa.naengpamasterbackend.inquiry.chat.dto.response.InquiryChatSessionResponse;
import com.naengpa.naengpamasterbackend.inquiry.chat.entity.InquiryChatSession;
import com.naengpa.naengpamasterbackend.inquiry.chat.repository.InquiryChatMessageRepository;
import com.naengpa.naengpamasterbackend.inquiry.chat.repository.InquiryChatSessionRepository;
import com.naengpa.naengpamasterbackend.member.entity.Member;
import com.naengpa.naengpamasterbackend.member.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class InquiryChatQueryService {

    private final InquiryChatSessionRepository sessionRepository;
    private final InquiryChatMessageRepository messageRepository;
    private final MemberRepository memberRepository;

    // 로그인한 회원의 삭제되지 않은 대화 세션을 최신순으로 조회합니다.
    @Transactional(readOnly = true)
    public List<InquiryChatSessionResponse> findSessions(String email) {
        Member member = findMember(email);
        return sessionRepository.findByMemberIdAndDeletedFalseOrderByUpdatedAtDescCreatedAtDesc(member.getId())
                .stream()
                .map(InquiryChatSessionResponse::from)
                .toList();
    }

    // 로그인한 회원이 소유한 세션의 메시지를 시간순으로 조회합니다.
    @Transactional(readOnly = true)
    public List<InquiryChatMessageResponse> findMessages(String email, Long sessionId) {
        InquiryChatSession session = findOwnedSession(findMember(email).getId(), sessionId);
        return messageRepository.findBySessionIdOrderByCreatedAtAsc(session.getId())
                .stream()
                .map(InquiryChatMessageResponse::from)
                .toList();
    }

    // 인증 이메일에 해당하는 회원을 조회합니다.
    private Member findMember(String email) {
        return memberRepository.findByEmail(email)
                .orElseThrow(() -> new BadCredentialsException("회원을 찾을 수 없습니다."));
    }

    // 회원이 소유한 삭제되지 않은 대화 세션을 조회합니다.
    private InquiryChatSession findOwnedSession(Long memberId, Long sessionId) {
        return sessionRepository.findByIdAndMemberIdAndDeletedFalse(sessionId, memberId)
                .orElseThrow(InquiryChatSessionNotFoundException::new);
    }
}
