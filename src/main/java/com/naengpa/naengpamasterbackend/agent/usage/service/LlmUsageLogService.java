package com.naengpa.naengpamasterbackend.agent.usage.service;

import com.naengpa.naengpamasterbackend.agent.usage.dto.response.LlmUsageLogResponse;
import com.naengpa.naengpamasterbackend.agent.usage.entity.LlmUsageLog;
import com.naengpa.naengpamasterbackend.agent.usage.repository.LlmUsageLogRepository;
import com.naengpa.naengpamasterbackend.member.entity.Member;
import com.naengpa.naengpamasterbackend.member.repository.MemberRepository;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
public class LlmUsageLogService {

    public static final String RULE_BASED_MVP_MODEL = "rule-based-mvp";

    private final LlmUsageLogRepository llmUsageLogRepository;
    private final MemberRepository memberRepository;
    private static final int FAILURE_MESSAGE_MAX_LENGTH = 255;

    public LlmUsageLogService(
            LlmUsageLogRepository llmUsageLogRepository,
            MemberRepository memberRepository
    ) {
        this.llmUsageLogRepository = llmUsageLogRepository;
        this.memberRepository = memberRepository;
    }

    @Transactional(readOnly = true)
    public List<LlmUsageLogResponse> findMyUsageLogs(String email) {
        // 로그인한 회원의 사용량 로그만 조회하기 위해 email로 회원을 먼저 찾음
        Member member = findMemberByEmail(email);

        // 최신 요청 기록이 먼저 보이도록 createdAt 내림차순으로 조회 후 응답 DTO로 변환
        return llmUsageLogRepository.findByMemberIdOrderByCreatedAtDesc(member.getId())
                .stream()
                .map(LlmUsageLogResponse::from)
                .toList();
    }

    @Transactional
    public void saveRuleBasedSuccessLog(Long memberId) {
        // 현재 추천 MVP는 실제 LLM 호출 전 단계라 토큰 수와 비용을 0으로 기록
        llmUsageLogRepository.save(LlmUsageLog.success(
                memberId,
                RULE_BASED_MVP_MODEL,
                0,
                0,
                0,
                BigDecimal.ZERO
        ));
    }

    @Transactional
    public void saveSuccessLog(
            Long memberId,
            String modelName,
            Integer promptTokens,
            Integer completionTokens,
            Integer totalTokens,
            BigDecimal estimatedCost
    ) {
        // FastAPI Agent가 반환한 실제 LLM 사용량을 그대로 저장
        llmUsageLogRepository.save(LlmUsageLog.success(
                memberId,
                modelName,
                promptTokens,
                completionTokens,
                totalTokens,
                estimatedCost
        ));
    }

    @Transactional
    public void saveRuleBasedFailureLog(Long memberId, String failureMessage) {
        // 추천 처리 중 예외가 발생하면 실패 이력을 남김
        llmUsageLogRepository.save(LlmUsageLog.failed(
                memberId,
                RULE_BASED_MVP_MODEL,
                truncateFailureMessage(failureMessage)
        ));
    }

    @Transactional
    public void saveFailureLog(Long memberId, String modelName, String failureMessage) {
        // Agent 서버나 LLM 호출 실패도 사용량 이력에 남겨 장애 추적이 가능하게 함
        llmUsageLogRepository.save(LlmUsageLog.failed(
                memberId,
                modelName,
                truncateFailureMessage(failureMessage)
        ));
    }

    private String truncateFailureMessage(String failureMessage) {
        if (failureMessage == null || failureMessage.length() <= FAILURE_MESSAGE_MAX_LENGTH) {
            return failureMessage;
        }

        return failureMessage.substring(0, FAILURE_MESSAGE_MAX_LENGTH);
    }

    private Member findMemberByEmail(String email) {
        // Security Authentication#getName()에 담긴 email 기준으로 현재 로그인 회원을 조회
        return memberRepository.findByEmail(email)
                .orElseThrow(() -> new BadCredentialsException("회원을 찾을 수 없습니다."));
    }
}
