package com.naengpa.naengpamasterbackend.payment;

import com.naengpa.naengpamasterbackend.member.entity.HouseholdType;
import com.naengpa.naengpamasterbackend.member.entity.Member;
import com.naengpa.naengpamasterbackend.member.repository.MemberRepository;
import com.naengpa.naengpamasterbackend.payment.client.TossBillingClient;
import com.naengpa.naengpamasterbackend.payment.client.TossBillingClient.TossBillingKeyIssueResponse;
import com.naengpa.naengpamasterbackend.payment.dto.request.BillingKeyIssueRequest;
import com.naengpa.naengpamasterbackend.payment.entity.BillingKey;
import com.naengpa.naengpamasterbackend.payment.repository.BillingKeyRepository;
import com.naengpa.naengpamasterbackend.payment.service.BillingKeyService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class BillingKeyServiceTests {

    private BillingKeyRepository billingKeyRepository;
    private MemberRepository memberRepository;
    private TossBillingClient tossBillingClient;
    private BillingKeyService billingKeyService;

    @BeforeEach
    void setUp() {
        billingKeyRepository = mock(BillingKeyRepository.class);
        memberRepository = mock(MemberRepository.class);
        tossBillingClient = mock(TossBillingClient.class);

        billingKeyService = new BillingKeyService(
                billingKeyRepository,
                memberRepository,
                tossBillingClient
        );
    }

    @Test
    @DisplayName("빌링키 발급 성공 시 기존 활성 빌링키를 비활성화하고 새 빌링키를 저장한다")
    void issueBillingKey_deactivatesCurrentActiveKeyAndSavesNewKey() {
        // given
        Member member = createMember(1L, "user@test.com");
        BillingKey currentBillingKey = BillingKey.create(
                member.getId(),
                "old-customer-key",
                "old-billing-key",
                "현대",
                "123456******7890"
        );

        TossBillingKeyIssueResponse tossResponse = mock(TossBillingKeyIssueResponse.class);
        when(tossResponse.getCustomerKey()).thenReturn("new-customer-key");
        when(tossResponse.getBillingKey()).thenReturn("new-billing-key");
        when(tossResponse.cardCompany()).thenReturn("삼성");
        when(tossResponse.cardNumberMasked()).thenReturn("987654******3210");

        when(memberRepository.findByEmail(member.getEmail())).thenReturn(Optional.of(member));
        when(billingKeyRepository.existsByTossCustomerKey("new-customer-key")).thenReturn(false);
        when(tossBillingClient.issueBillingKey("auth-key", "new-customer-key")).thenReturn(tossResponse);
        when(billingKeyRepository.existsByTossBillingKey("new-billing-key")).thenReturn(false);
        when(billingKeyRepository.findFirstByMemberIdAndIsActiveTrueOrderByBillingKeyIdDesc(member.getId()))
                .thenReturn(Optional.of(currentBillingKey));
        when(billingKeyRepository.save(any(BillingKey.class))).thenAnswer(invocation -> {
            BillingKey saved = invocation.getArgument(0);
            ReflectionTestUtils.setField(saved, "billingKeyId", 10L);
            return saved;
        });

        // when
        var response = billingKeyService.issueBillingKey(
                member.getEmail(),
                new BillingKeyIssueRequest("auth-key", "new-customer-key")
        );

        // then
        assertThat(currentBillingKey.getIsActive()).isFalse();
        assertThat(currentBillingKey.getDeactivatedAt()).isNotNull();

        assertThat(response.billingKeyId()).isEqualTo(10L);
        assertThat(response.customerKey()).isEqualTo("new-customer-key");
        assertThat(response.cardCompany()).isEqualTo("삼성");
        assertThat(response.cardNumberMasked()).isEqualTo("987654******3210");

        verify(billingKeyRepository).save(any(BillingKey.class));
    }

    @Test
    @DisplayName("이미 등록된 customerKey면 빌링키 발급을 차단한다")
    void issueBillingKey_throwsWhenCustomerKeyAlreadyExists() {
        // given
        Member member = createMember(1L, "user@test.com");

        when(memberRepository.findByEmail(member.getEmail())).thenReturn(Optional.of(member));
        when(billingKeyRepository.existsByTossCustomerKey("duplicated-customer-key")).thenReturn(true);

        // when & then
        assertThatThrownBy(() -> billingKeyService.issueBillingKey(
                member.getEmail(),
                new BillingKeyIssueRequest("auth-key", "duplicated-customer-key")
        ))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("이미 등록된 customerKey입니다.");
    }

    private Member createMember(Long memberId, String email) {
        Member member = Member.createUser(
                email,
                "password",
                "결제테스트유저",
                HouseholdType.ONE_PERSON
        );
        ReflectionTestUtils.setField(member, "id", memberId);
        return member;
    }
}