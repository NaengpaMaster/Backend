package com.naengpa.naengpamasterbackend.agent.usage.service;

import com.naengpa.naengpamasterbackend.agent.usage.dto.response.LlmUsageLogResponse;
import com.naengpa.naengpamasterbackend.agent.usage.entity.LlmUsageLog;
import com.naengpa.naengpamasterbackend.agent.usage.entity.LlmFeatureType;
import com.naengpa.naengpamasterbackend.agent.usage.repository.LlmUsageLogRepository;
import com.naengpa.naengpamasterbackend.member.entity.Member;
import com.naengpa.naengpamasterbackend.member.repository.MemberRepository;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.annotation.Propagation;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

@Service
public class LlmUsageLogService {

    public static final String RULE_BASED_MVP_MODEL = "rule-based-mvp";
    public static final String GPT_4_1_MINI_MODEL = "gpt-4.1-mini";
    private static final BigDecimal GPT_4_1_MINI_INPUT_PRICE_PER_1M = BigDecimal.valueOf(0.40);
    private static final BigDecimal GPT_4_1_MINI_OUTPUT_PRICE_PER_1M = BigDecimal.valueOf(1.60);
    private static final BigDecimal TOKENS_PER_MILLION = BigDecimal.valueOf(1_000_000);

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
                LlmFeatureType.SHOPPING_RECOMMENDATION,
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
        BigDecimal cost = resolveEstimatedCost(modelName, promptTokens, completionTokens, estimatedCost);

        // FastAPI Agent가 반환한 실제 LLM 사용량을 저장하고, 비용이 없으면 백엔드 단가 기준으로 계산
        llmUsageLogRepository.save(LlmUsageLog.success(
                memberId,
                LlmFeatureType.SHOPPING_RECOMMENDATION,
                modelName,
                promptTokens,
                completionTokens,
                totalTokens,
                cost
        ));
    }

    @Transactional
    public void saveRuleBasedFailureLog(Long memberId, String failureMessage) {
        // 추천 처리 중 예외가 발생하면 실패 이력을 남김
        llmUsageLogRepository.save(LlmUsageLog.failed(
                memberId,
                LlmFeatureType.SHOPPING_RECOMMENDATION,
                RULE_BASED_MVP_MODEL,
                truncateFailureMessage(failureMessage)
        ));
    }

    @Transactional
    public void saveFailureLog(Long memberId, String modelName, String failureMessage) {
        // Agent 서버나 LLM 호출 실패도 사용량 이력에 남겨 장애 추적이 가능하게 함
        llmUsageLogRepository.save(LlmUsageLog.failed(
                memberId,
                LlmFeatureType.SHOPPING_RECOMMENDATION,
                modelName,
                truncateFailureMessage(failureMessage)
        ));
    }

    @Transactional
    public void saveInquirySuccessLog(
            Long memberId,
            String modelName,
            Integer promptTokens,
            Integer completionTokens,
            Integer totalTokens,
            BigDecimal estimatedCost
    ) {
        llmUsageLogRepository.save(LlmUsageLog.success(
                memberId,
                LlmFeatureType.INQUIRY_QNA,
                modelName,
                promptTokens,
                completionTokens,
                totalTokens,
                resolveEstimatedCost(modelName, promptTokens, completionTokens, estimatedCost)
        ));
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void saveInquiryFailureLog(Long memberId, String modelName, String failureMessage) {
        llmUsageLogRepository.save(LlmUsageLog.failed(
                memberId,
                LlmFeatureType.INQUIRY_QNA,
                modelName,
                truncateFailureMessage(failureMessage)
        ));
    }

    @Transactional
    public void saveReceiptOcrSuccessLog(
            Long memberId,
            String modelName,
            Integer promptTokens,
            Integer completionTokens,
            Integer totalTokens,
            BigDecimal estimatedCost
    ) {
        llmUsageLogRepository.save(LlmUsageLog.success(
                memberId,
                LlmFeatureType.RECEIPT_OCR,
                modelName,
                promptTokens,
                completionTokens,
                totalTokens,
                resolveEstimatedCost(modelName, promptTokens, completionTokens, estimatedCost)
        ));
    }

    @Transactional
    public void saveFridgePhotoSuccessLog(
            Long memberId,
            String modelName,
            Integer promptTokens,
            Integer completionTokens,
            Integer totalTokens,
            BigDecimal estimatedCost
    ) {
        llmUsageLogRepository.save(LlmUsageLog.success(
                memberId,
                LlmFeatureType.FRIDGE_PHOTO_ANALYSIS,
                modelName,
                promptTokens,
                completionTokens,
                totalTokens,
                resolveEstimatedCost(modelName, promptTokens, completionTokens, estimatedCost)
        ));
    }

    private String truncateFailureMessage(String failureMessage) {
        if (failureMessage == null || failureMessage.length() <= FAILURE_MESSAGE_MAX_LENGTH) {
            return failureMessage;
        }

        return failureMessage.substring(0, FAILURE_MESSAGE_MAX_LENGTH);
    }

    private BigDecimal resolveEstimatedCost(
            String modelName,
            Integer promptTokens,
            Integer completionTokens,
            BigDecimal estimatedCost
    ) {
        if (estimatedCost != null && estimatedCost.compareTo(BigDecimal.ZERO) > 0) {
            return estimatedCost;
        }

        if (GPT_4_1_MINI_MODEL.equals(modelName)) {
            return calculateGpt41MiniEstimatedCost(promptTokens, completionTokens);
        }

        return estimatedCost == null ? BigDecimal.ZERO : estimatedCost;
    }

    private BigDecimal calculateGpt41MiniEstimatedCost(
            Integer promptTokens,
            Integer completionTokens
    ) {
        BigDecimal inputCost = BigDecimal.valueOf(promptTokens == null ? 0 : promptTokens)
                .multiply(GPT_4_1_MINI_INPUT_PRICE_PER_1M)
                .divide(TOKENS_PER_MILLION, 8, RoundingMode.HALF_UP);

        BigDecimal outputCost = BigDecimal.valueOf(completionTokens == null ? 0 : completionTokens)
                .multiply(GPT_4_1_MINI_OUTPUT_PRICE_PER_1M)
                .divide(TOKENS_PER_MILLION, 8, RoundingMode.HALF_UP);

        return inputCost.add(outputCost);
    }

    private Member findMemberByEmail(String email) {
        // Security Authentication#getName()에 담긴 email 기준으로 현재 로그인 회원을 조회
        return memberRepository.findByEmail(email)
                .orElseThrow(() -> new BadCredentialsException("회원을 찾을 수 없습니다."));
    }

    @Transactional
    public void saveQuizSuccessLog(
            Long memberId,
            String modelName,
            Integer promptTokens,
            Integer completionTokens,
            Integer totalTokens,
            BigDecimal estimatedCost
    ) {
        llmUsageLogRepository.save(LlmUsageLog.success(
                memberId,
                LlmFeatureType.QUIZ_GENERATION,
                modelName,
                promptTokens,
                completionTokens,
                totalTokens,
                resolveEstimatedCost(modelName, promptTokens, completionTokens, estimatedCost)
        ));
    }

    @Transactional
    public void saveQuizFailureLog(Long memberId, String modelName, String failureMessage) {
        llmUsageLogRepository.save(LlmUsageLog.failed(
                memberId,
                LlmFeatureType.QUIZ_GENERATION,
                modelName,
                truncateFailureMessage(failureMessage)
        ));
    }

}
