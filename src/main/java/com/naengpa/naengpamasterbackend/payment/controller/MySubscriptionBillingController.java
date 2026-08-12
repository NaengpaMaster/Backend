package com.naengpa.naengpamasterbackend.payment.controller;

import com.naengpa.naengpamasterbackend.global.response.ApiResponse;
import com.naengpa.naengpamasterbackend.payment.dto.response.MyBillingKeyResponse;
import com.naengpa.naengpamasterbackend.payment.dto.response.MyPaymentHistoryResponse;
import com.naengpa.naengpamasterbackend.payment.service.MySubscriptionBillingService;
import com.naengpa.naengpamasterbackend.subscription.dto.response.SubscriptionStatusResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
@Tag(name = "내 구독 결제 관리", description = "사용자 구독 카드, 결제 내역, 해지 예약 API")
public class MySubscriptionBillingController {

    private final MySubscriptionBillingService mySubscriptionBillingService;

    @Operation(summary = "내 등록 카드 조회", description = "현재 활성화된 구독 결제 카드를 조회합니다.")
    @GetMapping("/billing-keys/me")
    public ResponseEntity<ApiResponse<MyBillingKeyResponse>> getMyBillingKey(
            @Parameter(hidden = true) Authentication authentication
    ) {
        return ResponseEntity.ok(ApiResponse.success(
                "등록 카드 조회에 성공했습니다.",
                mySubscriptionBillingService.getMyBillingKey(authentication.getName())
        ));
    }

    @Operation(summary = "내 구독 결제 내역 조회", description = "로그인한 회원의 구독 결제 내역을 최신순으로 조회합니다.")
    @GetMapping("/subscriptions/payments")
    public ResponseEntity<ApiResponse<List<MyPaymentHistoryResponse>>> getMyPayments(
            @Parameter(hidden = true) Authentication authentication
    ) {
        return ResponseEntity.ok(ApiResponse.success(
                "구독 결제 내역 조회에 성공했습니다.",
                mySubscriptionBillingService.getMyPayments(authentication.getName())
        ));
    }

    @Operation(summary = "구독 해지 예약", description = "현재 이용 기간은 유지하고 다음 자동결제를 중단합니다.")
    @PatchMapping("/subscriptions/cancel")
    public ResponseEntity<ApiResponse<SubscriptionStatusResponse>> cancelMySubscription(
            @Parameter(hidden = true) Authentication authentication
    ) {
        return ResponseEntity.ok(ApiResponse.success(
                "구독 해지가 예약되었습니다.",
                mySubscriptionBillingService.cancelMySubscription(authentication.getName())
        ));
    }
}
