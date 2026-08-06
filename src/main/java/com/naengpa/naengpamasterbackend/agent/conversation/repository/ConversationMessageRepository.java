package com.naengpa.naengpamasterbackend.agent.conversation.repository;

import com.naengpa.naengpamasterbackend.agent.conversation.entity.ConversationMessage;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ConversationMessageRepository extends JpaRepository<ConversationMessage, Long> {

    //특정 세션의 메시지를 오래된 순서대로 조회
    List<ConversationMessage> findByConversationSessionIdOrderByCreatedAtAsc(Long conversationSessionId);
}
