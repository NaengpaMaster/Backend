package com.naengpa.naengpamasterbackend.payment.service;

import com.naengpa.naengpamasterbackend.member.entity.Member;
import com.naengpa.naengpamasterbackend.member.repository.MemberRepository;
import com.naengpa.naengpamasterbackend.payment.client.TossBillingClient;
import com.naengpa.naengpamasterbackend.payment.client.TossBillingClient.TossBillingKeyIssueResponse;
import com.naengpa.naengpamasterbackend.payment.dto.request.BillingKeyIssueRequest;
import com.naengpa.naengpamasterbackend.payment.dto.response.BillingKeyResponse;
import com.naengpa.naengpamasterbackend.payment.entity.BillingKey;
import com.naengpa.naengpamasterbackend.payment.repository.BillingKeyRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class BillingKeyService {

    private final BillingKeyRepository billingKeyRepository;
    private final MemberRepository memberRepository;
    private final TossBillingClient tossBillingClient;

    // Toss 인증 성공 후 전달받은 authKey/customerKey로 빌링키를 발급하고 DB에 저장
    @Transactional
    public BillingKeyResponse issueBillingKey(String email, BillingKeyIssueRequest request) {
        Member member = findMemberByEmail(email);

        // customerKey는 Toss 고객 식별 키이므로 같은 값이 중복 저장되지 않도록 먼저 차단
        if (billingKeyRepository.existsByTossCustomerKey(request.customerKey())) {
            throw new IllegalArgumentException("이미 등록된 customerKey입니다.");
        }

        // TossPayments 서버에 authKey/customerKey를 전달해 실제 자동결제용 billingKey를 발급받음
        TossBillingKeyIssueResponse tossResponse = tossBillingClient.issueBillingKey(
                request.authKey(),
                request.customerKey()
        );

        // billingKey도 unique 대상이므로 DB 저장 전에 중복 여부를 확인
        if (billingKeyRepository.existsByTossBillingKey(tossResponse.getBillingKey())) {
            throw new IllegalArgumentException("이미 등록된 빌링키입니다.");
        }

        // 한 회원이 사용할 활성 빌링키는 하나만 유지. 새 카드 등록 시 기존 활성 키는 비활성화
        billingKeyRepository.findFirstByMemberIdAndIsActiveTrueOrderByBillingKeyIdDesc(member.getId())
                .ifPresent(BillingKey::deactivate);

        // Toss 응답에서 필요한 값만 골라 billing_keys 테이블에 저장할 엔티티를 생성
        BillingKey billingKey = BillingKey.create(
                member.getId(),
                tossResponse.getCustomerKey(),
                tossResponse.getBillingKey(),
                tossResponse.cardCompany(),
                tossResponse.cardNumberMasked()
        );

        BillingKey savedBillingKey = billingKeyRepository.save(billingKey);

        // 프론트에는 민감한 tossBillingKey를 내려주지 않고 등록 결과만 응답
        return BillingKeyResponse.from(savedBillingKey);
    }

    // 인증 객체의 email 기준으로 현재 회원을 찾음
    private Member findMemberByEmail(String email) {
        return memberRepository.findByEmail(email)
                .orElseThrow(() -> new BadCredentialsException("회원을 찾을 수 없습니다."));
    }
}