package com.naengpa.naengpamasterbackend.inquiry.chat.repository;

import com.naengpa.naengpamasterbackend.inquiry.chat.entity.InquiryChatSession;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface InquiryChatSessionRepository extends JpaRepository<InquiryChatSession, Long> {

    List<InquiryChatSession> findByMemberIdAndDeletedFalseOrderByUpdatedAtDescCreatedAtDesc(Long memberId);

    Optional<InquiryChatSession> findByIdAndMemberIdAndDeletedFalse(Long id, Long memberId);
}
