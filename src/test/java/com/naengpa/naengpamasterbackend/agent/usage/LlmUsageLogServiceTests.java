package com.naengpa.naengpamasterbackend.agent.usage;

import com.naengpa.naengpamasterbackend.agent.usage.entity.LlmCallStatus;
import com.naengpa.naengpamasterbackend.agent.usage.repository.LlmUsageLogRepository;
import com.naengpa.naengpamasterbackend.agent.usage.service.LlmUsageLogService;
import com.naengpa.naengpamasterbackend.member.entity.HouseholdType;
import com.naengpa.naengpamasterbackend.member.entity.Member;
import com.naengpa.naengpamasterbackend.member.repository.MemberRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Transactional
class LlmUsageLogServiceTests {

    @Autowired
    private LlmUsageLogService llmUsageLogService;

    @Autowired
    private LlmUsageLogRepository llmUsageLogRepository;

    @Autowired
    private MemberRepository memberRepository;

    @Test
    @DisplayName("LLM 성공 로그를 저장한다")
    void saveRuleBasedSuccessLog_savesSuccessLog() {
        // given
        Member member = memberRepository.save(Member.createUser(
                "llm-success@test.com",
                "password",
                "사용량성공테스트유저",
                HouseholdType.ONE_PERSON
        ));

        // when
        llmUsageLogService.saveRuleBasedSuccessLog(member.getId());

        // then
        var logs = llmUsageLogRepository.findByMemberIdOrderByCreatedAtDesc(member.getId());

        assertThat(logs).hasSize(1);
        assertThat(logs.get(0).getModelName()).isEqualTo("rule-based-mvp");
        assertThat(logs.get(0).getPromptTokens()).isZero();
        assertThat(logs.get(0).getCompletionTokens()).isZero();
        assertThat(logs.get(0).getTotalTokens()).isZero();
        assertThat(logs.get(0).getEstimatedCost()).isEqualByComparingTo(BigDecimal.ZERO);
        assertThat(logs.get(0).getStatus()).isEqualTo(LlmCallStatus.SUCCESS);
        assertThat(logs.get(0).getFailureMessage()).isNull();
    }

    @Test
    @DisplayName("LLM 실패 로그를 저장한다")
    void saveRuleBasedFailureLog_savesFailureLog() {
        // given
        Member member = memberRepository.save(Member.createUser(
                "llm-failed@test.com",
                "password",
                "사용량실패테스트유저",
                HouseholdType.ONE_PERSON
        ));

        // when
        llmUsageLogService.saveRuleBasedFailureLog(member.getId(), "LLM 호출 실패");

        // then
        var logs = llmUsageLogRepository.findByMemberIdOrderByCreatedAtDesc(member.getId());

        assertThat(logs).hasSize(1);
        assertThat(logs.get(0).getModelName()).isEqualTo("rule-based-mvp");
        assertThat(logs.get(0).getStatus()).isEqualTo(LlmCallStatus.FAILED);
        assertThat(logs.get(0).getFailureMessage()).isEqualTo("LLM 호출 실패");
    }

    @Test
    @DisplayName("로그인한 회원의 LLM 사용량 로그만 조회한다")
    void findMyUsageLogs_returnsOnlyMyLogs() {
        // given
        Member member = memberRepository.save(Member.createUser(
                "llm-my-log@test.com",
                "password",
                "내사용량테스트유저",
                HouseholdType.ONE_PERSON
        ));

        Member other = memberRepository.save(Member.createUser(
                "llm-other-log@test.com",
                "password",
                "다른사용량테스트유저",
                HouseholdType.ONE_PERSON
        ));

        llmUsageLogService.saveRuleBasedSuccessLog(member.getId());
        llmUsageLogService.saveRuleBasedSuccessLog(other.getId());

        // when
        var result = llmUsageLogService.findMyUsageLogs(member.getEmail());

        // then
        assertThat(result).hasSize(1);
        assertThat(result.get(0).modelName()).isEqualTo("rule-based-mvp");
        assertThat(result.get(0).status()).isEqualTo(LlmCallStatus.SUCCESS);
    }

    @Test
    @DisplayName("LLM 사용량 로그가 없으면 빈 배열을 반환한다")
    void findMyUsageLogs_returnsEmptyListWhenNoLogs() {
        // given
        Member member = memberRepository.save(Member.createUser(
                "llm-empty@test.com",
                "password",
                "사용량없음테스트유저",
                HouseholdType.ONE_PERSON
        ));

        // when
        var result = llmUsageLogService.findMyUsageLogs(member.getEmail());

        // then
        assertThat(result).isEmpty();
    }
}
