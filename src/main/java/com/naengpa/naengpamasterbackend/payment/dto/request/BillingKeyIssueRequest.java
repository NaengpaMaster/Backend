package com.naengpa.naengpamasterbackend.payment.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

@Schema(description = "TossPayments 빌링키 발급 요청")
public record BillingKeyIssueRequest (

        @Schema(description = "TossPayments 자동결제 인증 성공 후 전달받은 인증키")
        @NotBlank(message = "authKey는 필수입니다.")
        String authKey,

        @Schema(description = "TossPayments 자동 결제 인증에 사용한 고객 식별 키")
        @NotBlank(message = "customerKey는 필수입니다.")
        String customerKey

){
}
