package com.naengpa.naengpamasterbackend.agent.usage.service;

import com.naengpa.naengpamasterbackend.agent.usage.dto.response.AdminLlmUsageLogResponse;
import com.naengpa.naengpamasterbackend.agent.usage.dto.response.AdminLlmUsageLogPageResponse;
import com.naengpa.naengpamasterbackend.agent.usage.entity.LlmUsageLog;
import com.naengpa.naengpamasterbackend.agent.usage.entity.LlmFeatureType;
import com.naengpa.naengpamasterbackend.agent.usage.entity.LlmCallStatus;
import com.naengpa.naengpamasterbackend.agent.usage.repository.LlmUsageLogRepository;
import com.naengpa.naengpamasterbackend.member.entity.Member;
import com.naengpa.naengpamasterbackend.member.repository.MemberRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

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
    public AdminLlmUsageLogPageResponse findAllUsageLogs(LlmFeatureType featureType, Pageable pageable) {
        Page<LlmUsageLog> logs = featureType == null
                ? llmUsageLogRepository.findAllByOrderByCreatedAtDesc(pageable)
                : llmUsageLogRepository.findByFeatureTypeOrderByCreatedAtDesc(featureType, pageable);
        List<Long> memberIds = logs.getContent().stream()
                .map(LlmUsageLog::getMemberId)
                .distinct()
                .toList();

        Map<Long, Member> membersById = memberRepository.findByIdIn(memberIds)
                .stream()
                .collect(Collectors.toMap(Member::getId, Function.identity()));

        Page<AdminLlmUsageLogResponse> responses = logs
                .map(log -> AdminLlmUsageLogResponse.from(log, membersById.get(log.getMemberId())));

        return AdminLlmUsageLogPageResponse.from(
                responses,
                llmUsageLogRepository.summarize(featureType, LlmCallStatus.SUCCESS, LlmCallStatus.FAILED)
        );
    }
}
