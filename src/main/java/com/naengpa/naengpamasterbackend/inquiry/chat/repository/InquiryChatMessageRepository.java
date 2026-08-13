package com.naengpa.naengpamasterbackend.inquiry.chat.repository;

import com.naengpa.naengpamasterbackend.inquiry.chat.entity.InquiryChatMessage;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface InquiryChatMessageRepository extends JpaRepository<InquiryChatMessage, Long> {

    // 세션의 전체 메시지를 생성 시각 오름차순으로 조회합니다.
    List<InquiryChatMessage> findBySessionIdOrderByCreatedAtAsc(Long sessionId);

    // 세션의 최근 메시지 10개를 최신순으로 조회합니다.
    List<InquiryChatMessage> findTop10BySessionIdOrderByCreatedAtDesc(Long sessionId);
}
