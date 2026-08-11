package com.naengpa.naengpamasterbackend.inquiry.chat.repository;

import com.naengpa.naengpamasterbackend.inquiry.chat.entity.InquiryChatMessage;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface InquiryChatMessageRepository extends JpaRepository<InquiryChatMessage, Long> {

    List<InquiryChatMessage> findBySessionIdOrderByCreatedAtAsc(Long sessionId);

    List<InquiryChatMessage> findTop10BySessionIdOrderByCreatedAtDesc(Long sessionId);
}
