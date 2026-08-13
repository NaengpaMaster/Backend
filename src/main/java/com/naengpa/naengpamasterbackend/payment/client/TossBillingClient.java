package com.naengpa.naengpamasterbackend.payment.client;

import com.naengpa.naengpamasterbackend.payment.exception.TossPaymentException;
import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;

import java.nio.charset.StandardCharsets;
import java.util.Base64;


// authKey + customerKey를 Toss로 보내고
// Toss가 돌려준 billingKey + 카드 정보를 받아오기
@Component
public class TossBillingClient {

    private final RestClient restClient;
    private final String secretKey;

    public TossBillingClient(
            RestClient.Builder restClientBuilder,
            @Value("${toss-payments.base-url}") String baseUrl,
            @Value("${toss-payments.secret-key}") String secretKey
    ) {
        this.restClient = restClientBuilder
                .baseUrl(baseUrl)
                .build();
        this.secretKey = secretKey;
    }

    public TossBillingKeyIssueResponse issueBillingKey(String authKey, String customerKey) {
        if (!StringUtils.hasText(secretKey)) {
            throw new TossPaymentException("TossPayments Secret Key가 설정되지 않았습니다.");
        }

        try {
            return restClient.post()
                    .uri("/v1/billing/authorizations/issue")
                    .header("Authorization", basicAuthHeader())
                    .body(new TossBillingKeyIssueRequest(authKey, customerKey))
                    .retrieve()
                    .body(TossBillingKeyIssueResponse.class);
        } catch (RestClientResponseException exception) {
            throw new TossPaymentException("TossPayments 빌링키 발급에 실패했습니다.");
        }
    }

    // 저장된 billingKey로 Toss 자동결제를 승인
    public TossBillingPaymentResponse approveBillingPayment(
            String billingKey,
            String customerKey,
            String orderId,
            String orderName,
            int amount
    ) {
        if (!StringUtils.hasText(secretKey)) {
            throw new TossPaymentException("TossPayments Secret Key가 설정되지 않았습니다.");
        }

        try {
            return restClient.post()
                    .uri("/v1/billing/{billingKey}", billingKey)
                    .header("Authorization", basicAuthHeader())
                    .body(new TossBillingPaymentRequest(customerKey, amount, orderId, orderName))
                    .retrieve()
                    .body(TossBillingPaymentResponse.class);
        } catch (RestClientResponseException exception) {
            throw new TossPaymentException("TossPayments 자동결제 승인에 실패했습니다.");
        }
    }

    private String basicAuthHeader() {
        String token = Base64.getEncoder()
                .encodeToString((secretKey + ":").getBytes(StandardCharsets.UTF_8));
        return "Basic " + token;
    }

    // Toss API 요청 전용 내부 DTO
    private record TossBillingKeyIssueRequest(
            String authKey,
            String customerKey
    ) {
    }

    // Toss 자동결제 승인 요청 전용 내부 DTO
    private record TossBillingPaymentRequest(
            String customerKey,
            int amount,
            String orderId,
            String orderName
    ) {
    }

    @Getter
    public static class TossBillingKeyIssueResponse {

        private String customerKey;
        private String billingKey;
        private String method;
        private Card card;

        public String cardCompany() {
            if (card == null) {
                return null;
            }
            return card.company;
        }

        public String cardNumberMasked() {
            if (card == null) {
                return null;
            }
            return card.number;
        }

        @Getter
        public static class Card {
            private String company;
            private String number;
        }
    }

    @Getter
    public static class TossBillingPaymentResponse {

        private String paymentKey;
        private String orderId;
        private String orderName;
        private String status;
        private Integer totalAmount;
        private String approvedAt;
    }

}
