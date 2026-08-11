package com.naengpa.naengpamasterbackend.agent.usage;

import com.naengpa.naengpamasterbackend.agent.usage.entity.LlmCallStatus;
import com.naengpa.naengpamasterbackend.agent.usage.entity.LlmFeatureType;
import com.naengpa.naengpamasterbackend.agent.usage.service.AdminLlmUsageLogService;
import com.naengpa.naengpamasterbackend.agent.usage.service.LlmUsageLogService;
import com.naengpa.naengpamasterbackend.member.entity.HouseholdType;
import com.naengpa.naengpamasterbackend.member.entity.Member;
import com.naengpa.naengpamasterbackend.member.repository.MemberRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Transactional
class AdminLlmUsageLogServiceTests {

    @Autowired
    private AdminLlmUsageLogService adminLlmUsageLogService;

    @Autowired
    private LlmUsageLogService llmUsageLogService;

    @Autowired
    private MemberRepository memberRepository;

    @Test
    @DisplayName("관리자는 전체 회원의 LLM 사용량 로그를 조회한다")
    void findAllUsageLogs_returnsAllMembersLogs() {
        // given
        Member successMember = memberRepository.save(Member.createUser(
                "admin-llm-success@test.com",
                "password",
                "관리자사용량성공유저",
                HouseholdType.ONE_PERSON
        ));
        Member failedMember = memberRepository.save(Member.createUser(
                "admin-llm-failed@test.com",
                "password",
                "관리자사용량실패유저",
                HouseholdType.ONE_PERSON
        ));

        llmUsageLogService.saveRuleBasedSuccessLog(successMember.getId());
        llmUsageLogService.saveRuleBasedFailureLog(failedMember.getId(), "agent api failed");

        // when
        var result = adminLlmUsageLogService.findAllUsageLogs();

        // then
        assertThat(result)
                .extracting("email")
                .contains(successMember.getEmail(), failedMember.getEmail());

        assertThat(result)
                .filteredOn(log -> log.memberId().equals(successMember.getId()))
                .first()
                .satisfies(log -> {
                    assertThat(log.nickname()).isEqualTo(successMember.getNickname());
                    assertThat(log.featureType()).isEqualTo(LlmFeatureType.SHOPPING_RECOMMENDATION);
                    assertThat(log.status()).isEqualTo(LlmCallStatus.SUCCESS);
                });

        assertThat(result)
                .filteredOn(log -> log.memberId().equals(failedMember.getId()))
                .first()
                .satisfies(log -> {
                    assertThat(log.nickname()).isEqualTo(failedMember.getNickname());
                    assertThat(log.status()).isEqualTo(LlmCallStatus.FAILED);
                    assertThat(log.failureMessage()).isEqualTo("agent api failed");
                });
    }
}
