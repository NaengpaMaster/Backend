package com.naengpa.naengpamasterbackend.payment.controller;

import com.naengpa.naengpamasterbackend.global.response.ApiResponse;
import com.naengpa.naengpamasterbackend.payment.dto.request.SubscriptionPaymentRequest;
import com.naengpa.naengpamasterbackend.payment.dto.response.SubscriptionPaymentResponse;
import com.naengpa.naengpamasterbackend.payment.service.SubscriptionPaymentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/subscriptions/payments")
@RequiredArgsConstructor
@Tag(name = "TossPayments 구독 결제", description = "TossPayments 빌링키 기반 구독 자동결제 API")
public class SubscriptionPaymentController {

    private final SubscriptionPaymentService subscriptionPaymentService;

    @Operation(summary = "구독 자동결제 승인", description = "저장된 TossPayments 빌링키로 월간/연간 구독 결제를 승인하고 구독 상태를 갱신합니다.")
    @PostMapping
    public ResponseEntity<ApiResponse<SubscriptionPaymentResponse>> approveSubscriptionPayment(
            @Parameter(hidden = true) Authentication authentication,
            @Valid @RequestBody SubscriptionPaymentRequest request
    ) {
        SubscriptionPaymentResponse response = subscriptionPaymentService.approveSubscriptionPayment(
                authentication.getName(),
                request
        );

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("구독 결제가 승인되었습니다.", response));
    }
}