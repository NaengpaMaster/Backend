package com.naengpa.naengpamasterbackend.subscription.controller;

import com.naengpa.naengpamasterbackend.global.response.ApiResponse;
import com.naengpa.naengpamasterbackend.subscription.dto.response.SubscriptionStatusResponse;
import com.naengpa.naengpamasterbackend.subscription.service.SubscriptionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/subscriptions")
@RequiredArgsConstructor
@Tag(name = "구독 상태", description = "회원의 FREE/PREMIUM 구독 상태 조회 API")
public class SubscriptionController {

    private final SubscriptionService subscriptionService;

    @Operation(summary = "내 구독 상태 조회", description = "로그인한 회원이 접근 가능한 냉장고 기준으로 프리미엄 구독 여부와 구독 기간 정보를 조회합니다.")
    @GetMapping("/me")
    public ResponseEntity<ApiResponse<SubscriptionStatusResponse>> getMySubscription(
            @Parameter(hidden = true) Authentication authentication
    ) {
        return ResponseEntity.ok(ApiResponse.success(subscriptionService.getMySubscription(authentication.getName())));
    }
}
