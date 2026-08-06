package com.naengpa.naengpamasterbackend.agent.usage.service;

import com.naengpa.naengpamasterbackend.agent.usage.dto.response.AdminLlmUsageLogResponse;
import com.naengpa.naengpamasterbackend.agent.usage.entity.LlmUsageLog;
import com.naengpa.naengpamasterbackend.agent.usage.repository.LlmUsageLogRepository;
import com.naengpa.naengpamasterbackend.member.entity.Member;
import com.naengpa.naengpamasterbackend.member.repository.MemberRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class AdminLlmUsageLogService {

    private final LlmUsageLogRepository llmUsageLogRepository;
    private final MemberRepository memberRepository;

    public AdminLlmUsageLogService(
            LlmUsageLogRepository llmUsageLogRepository,
            MemberRepository memberRepository
    ) {
        this.llmUsageLogRepository = llmUsageLogRepository;
        this.memberRepository = memberRepository;
    }

    @Transactional(readOnly = true)
    public List<AdminLlmUsageLogResponse> findAllUsageLogs() {
        List<LlmUsageLog> logs = llmUsageLogRepository.findAllByOrderByCreatedAtDesc();
        List<Long> memberIds = logs.stream()
                .map(LlmUsageLog::getMemberId)
                .distinct()
                .toList();

        Map<Long, Member> membersById = memberRepository.findByIdIn(memberIds)
                .stream()
                .collect(Collectors.toMap(Member::getId, Function.identity()));

        return logs.stream()
                .map(log -> AdminLlmUsageLogResponse.from(log, membersById.get(log.getMemberId())))
                .toList();
    }
}
