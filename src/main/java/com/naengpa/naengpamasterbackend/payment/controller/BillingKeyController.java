package com.naengpa.naengpamasterbackend.payment.controller;

import com.naengpa.naengpamasterbackend.global.response.ApiResponse;
import com.naengpa.naengpamasterbackend.payment.dto.request.BillingKeyIssueRequest;
import com.naengpa.naengpamasterbackend.payment.dto.response.BillingKeyResponse;
import com.naengpa.naengpamasterbackend.payment.service.BillingKeyService;
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
@RequestMapping("/api/v1/billing-keys")
@RequiredArgsConstructor
@Tag(name = "TossPayments 빌링키", description = "TossPayments 자동결제 빌링키 등록 API")
public class BillingKeyController {

    private final BillingKeyService billingKeyService;

    @Operation(summary = "빌링키 등록", description = "TossPayments 자동결제 인증 성공 후 전달받은 authKey/customerKey로 빌링키를 발급하고 저장합니다.")
    @PostMapping
    public ResponseEntity<ApiResponse<BillingKeyResponse>> issueBillingKey(
            @Parameter(hidden = true) Authentication authentication,
            @Valid @RequestBody BillingKeyIssueRequest request
    ) {
        BillingKeyResponse response = billingKeyService.issueBillingKey(authentication.getName(), request);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("빌링키가 등록되었습니다.", response));
    }
}