package com.naengpa.naengpamasterbackend.agent.usage.service;

import com.naengpa.naengpamasterbackend.agent.usage.dto.response.LlmUsageLogResponse;
import com.naengpa.naengpamasterbackend.agent.usage.entity.LlmUsageLog;
import com.naengpa.naengpamasterbackend.agent.usage.repository.LlmUsageLogRepository;
import com.naengpa.naengpamasterbackend.member.entity.Member;
import com.naengpa.naengpamasterbackend.member.repository.MemberRepository;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
public class LlmUsageLogService {

    public static final String RULE_BASED_MVP_MODEL = "rule-based-mvp";

    private final LlmUsageLogRepository llmUsageLogRepository;
    private final MemberRepository memberRepository;

    public LlmUsageLogService(
            LlmUsageLogRepository llmUsageLogRepository,
            MemberRepository memberRepository
    ) {
        this.llmUsageLogRepository = llmUsageLogRepository;
        this.memberRepository = memberRepository;
    }

    @Transactional(readOnly = true)
    public List<LlmUsageLogResponse> findMyUsageLogs(String email) {
        Member member = findMemberByEmail(email);

        return llmUsageLogRepository.findByMemberIdOrderByCreatedAtDesc(member.getId())
                .stream()
                .map(LlmUsageLogResponse::from)
                .toList();
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void saveRuleBasedSuccessLog(Long memberId) {
        llmUsageLogRepository.save(LlmUsageLog.success(
                memberId,
                RULE_BASED_MVP_MODEL,
                0,
                0,
                0,
                BigDecimal.ZERO
        ));
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void saveRuleBasedFailureLog(Long memberId, String failureMessage) {
        llmUsageLogRepository.save(LlmUsageLog.failed(
                memberId,
                RULE_BASED_MVP_MODEL,
                failureMessage
        ));
    }

    private Member findMemberByEmail(String email) {
        return memberRepository.findByEmail(email)
                .orElseThrow(() -> new BadCredentialsException("회원을 찾을 수 없습니다."));
    }
}
