package com.naengpa.naengpamasterbackend.subscription.controller;

import com.naengpa.naengpamasterbackend.global.response.ApiResponse;
import com.naengpa.naengpamasterbackend.subscription.dto.response.SubscriptionStatusResponse;
import com.naengpa.naengpamasterbackend.subscription.service.SubscriptionService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/subscriptions")
@RequiredArgsConstructor
public class SubscriptionController {

    private final SubscriptionService subscriptionService;

    @GetMapping("/me")
    public ResponseEntity<ApiResponse<SubscriptionStatusResponse>> getMySubscription(Authentication authentication) {
        return ResponseEntity.ok(ApiResponse.success(subscriptionService.getMySubscription(authentication.getName())));
    }
}
