package com.naengpa.naengpamasterbackend.inquiry.chat.repository;

import com.naengpa.naengpamasterbackend.inquiry.chat.entity.InquiryChatSession;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface InquiryChatSessionRepository extends JpaRepository<InquiryChatSession, Long> {

    // 회원의 삭제되지 않은 대화 세션을 최근 활동순으로 조회합니다.
    List<InquiryChatSession> findByMemberIdAndDeletedFalseOrderByUpdatedAtDescCreatedAtDesc(Long memberId);

    // 회원이 소유한 삭제되지 않은 대화 세션을 조회합니다.
    Optional<InquiryChatSession> findByIdAndMemberIdAndDeletedFalse(Long id, Long memberId);
}
