package com.naengpa.naengpamasterbackend.agent.usage.repository;

import com.naengpa.naengpamasterbackend.agent.usage.entity.LlmUsageLog;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface LlmUsageLogRepository extends JpaRepository<LlmUsageLog, Long> {

    List<LlmUsageLog> findByMemberIdOrderByCreatedAtDesc(Long memberId);

    List<LlmUsageLog> findAllByOrderByCreatedAtDesc();
}
