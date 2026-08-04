package com.naengpa.naengpamasterbackend.agent.conversation.repository;

import com.naengpa.naengpamasterbackend.agent.conversation.entity.ConversationSession;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ConversationSessionRepository extends JpaRepository<ConversationSession, Long> {

    //로그인한 회원의 삭제되지 않은 세션 목록을 최신순으로 조회
    List<ConversationSession> findByMemberIdAndIsDeletedFalseOrderByCreatedAtDesc(Long memberId);

}
