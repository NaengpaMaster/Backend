package com.naengpa.naengpamasterbackend.payment.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

import java.util.Map;

@Schema(description = "TossPayments 웹훅 요청")
public record TossPaymentWebhookRequest(

        @Schema(description = "테스트용 이벤트 ID. 실제 Toss 웹훅은 transmission-id 헤더를 우선 사용합니다.")
        String eventId,

        @Schema(description = "TossPayments 웹훅 이벤트 타입")
        @NotBlank(message = "eventType은 필수입니다.")
        String eventType,

        @Schema(description = "테스트용 결제 키. 실제 Toss 웹훅은 data.paymentKey를 우선 사용합니다.")
        String paymentKey,

        @Schema(description = "웹훅 원문 JSON")
        String payload,

        @Schema(description = "TossPayments 웹훅 data 객체")
        Map<String, Object> data
) {

    public String resolvedPaymentKey() {
        if (paymentKey != null && !paymentKey.isBlank()) {
            return paymentKey;
        }
        return stringValue("paymentKey");
    }

    public String resolvedPaymentStatus() {
        return stringValue("status");
    }

    private String stringValue(String key) {
        if (data == null) {
            return null;
        }

        Object value = data.get(key);
        return value == null ? null : String.valueOf(value);
    }
}
