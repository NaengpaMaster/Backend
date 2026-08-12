package com.naengpa.naengpamasterbackend.payment.controller;

import com.naengpa.naengpamasterbackend.global.response.ApiResponse;
import com.naengpa.naengpamasterbackend.payment.dto.request.TossPaymentWebhookRequest;
import com.naengpa.naengpamasterbackend.payment.service.TossPaymentWebhookService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/toss-payments/webhook")
@RequiredArgsConstructor
@Tag(name = "TossPayments 웹훅", description = "TossPayments 결제 이벤트 수신 API")
public class TossPaymentWebhookController {

    private final TossPaymentWebhookService tossPaymentWebhookService;

    @Operation(summary = "TossPayments 웹훅 수신", description = "TossPayments 결제 성공/실패/취소 이벤트를 수신하고 결제 상태를 동기화합니다.")
    @PostMapping
    public ResponseEntity<ApiResponse<Void>> handleWebhook(
            @RequestHeader(value = "tosspayments-webhook-transmission-id", required = false) String transmissionId,
            @RequestBody TossPaymentWebhookRequest request
    ) {
        tossPaymentWebhookService.handleWebhook(transmissionId, request);

        return ResponseEntity.ok(ApiResponse.success("TossPayments 웹훅이 처리되었습니다.", null));
    }
}
