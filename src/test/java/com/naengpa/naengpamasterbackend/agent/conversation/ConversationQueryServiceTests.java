package com.naengpa.naengpamasterbackend.agent.conversation;

import com.naengpa.naengpamasterbackend.agent.conversation.entity.ConversationMessage;
import com.naengpa.naengpamasterbackend.agent.conversation.entity.ConversationMessageRole;
import com.naengpa.naengpamasterbackend.agent.conversation.entity.ConversationSession;
import com.naengpa.naengpamasterbackend.agent.conversation.repository.ConversationMessageRepository;
import com.naengpa.naengpamasterbackend.agent.conversation.repository.ConversationSessionRepository;
import com.naengpa.naengpamasterbackend.agent.conversation.service.ConversationQueryService;
import com.naengpa.naengpamasterbackend.global.exception.AiConversationSessionNotFoundException;
import com.naengpa.naengpamasterbackend.member.entity.HouseholdType;
import com.naengpa.naengpamasterbackend.member.entity.Member;
import com.naengpa.naengpamasterbackend.member.repository.MemberRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@Transactional
class ConversationQueryServiceTests {

    @Autowired
    private ConversationQueryService conversationQueryService;

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private ConversationSessionRepository conversationSessionRepository;

    @Autowired
    private ConversationMessageRepository conversationMessageRepository;

    @Test
    @DisplayName("로그인한 회원의 AI 대화 세션 목록을 최신순으로 조회한다")
    void findConversationSessions_returnsMySessions() {
        // given
        Member member = memberRepository.save(Member.createUser(
                "conversation-list@test.com",
                "password",
                "대화목록테스트유저",
                HouseholdType.ONE_PERSON
        ));

        conversationSessionRepository.save(
                ConversationSession.create(member.getId(), "AI 장보기 추천")
        );

        // when
        var result = conversationQueryService.findConversationSessions(member.getEmail());

        // then
        assertThat(result).hasSize(1);
        assertThat(result.get(0).title()).isEqualTo("AI 장보기 추천");
    }

    @Test
    @DisplayName("본인 AI 대화 세션의 메시지 목록을 오래된 순서대로 조회한다")
    void findConversationMessages_returnsMySessionMessages() {
        // given
        Member member = memberRepository.save(Member.createUser(
                "conversation-message@test.com",
                "password",
                "대화메시지테스트유저",
                HouseholdType.ONE_PERSON
        ));

        ConversationSession session = conversationSessionRepository.save(
                ConversationSession.create(member.getId(), "AI 장보기 추천")
        );

        conversationMessageRepository.save(ConversationMessage.create(
                session.getConversationSessionId(),
                ConversationMessageRole.USER,
                "장보기 추천해줘"
        ));

        conversationMessageRepository.save(ConversationMessage.create(
                session.getConversationSessionId(),
                ConversationMessageRole.ASSISTANT,
                "감자를 추천합니다."
        ));

        // when
        var result = conversationQueryService.findConversationMessages(
                member.getEmail(),
                session.getConversationSessionId()
        );

        // then
        assertThat(result).hasSize(2);
        assertThat(result)
                .extracting(message -> message.role())
                .containsExactly(ConversationMessageRole.USER, ConversationMessageRole.ASSISTANT);
    }

    @Test
    @DisplayName("본인 세션이 아니면 AI 대화 메시지 조회에 실패한다")
    void findConversationMessages_throwsExceptionWhenSessionIsNotMine() {
        // given
        Member owner = memberRepository.save(Member.createUser(
                "conversation-owner@test.com",
                "password",
                "대화소유자",
                HouseholdType.ONE_PERSON
        ));

        Member other = memberRepository.save(Member.createUser(
                "conversation-other@test.com",
                "password",
                "다른회원",
                HouseholdType.ONE_PERSON
        ));

        ConversationSession ownerSession = conversationSessionRepository.save(
                ConversationSession.create(owner.getId(), "AI 장보기 추천")
        );

        // when & then
        assertThatThrownBy(() -> conversationQueryService.findConversationMessages(
                other.getEmail(),
                ownerSession.getConversationSessionId()
        )).isInstanceOf(AiConversationSessionNotFoundException.class);
    }
}
