package com.naengpa.naengpamasterbackend.agent.shopping.controller;

import com.naengpa.naengpamasterbackend.agent.shopping.dto.request.ShoppingRecommendationRequest;
import com.naengpa.naengpamasterbackend.agent.shopping.dto.response.ShoppingRecommendationResponse;
import com.naengpa.naengpamasterbackend.agent.shopping.service.AgentShoppingRecommendationService;
import com.naengpa.naengpamasterbackend.global.response.ApiResponse;
import com.naengpa.naengpamasterbackend.subscription.service.SubscriptionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/agent")
@Tag(name = "AI 장보기 추천 재료", description="AI 장보기 추천 재료 API")
public class AgentShoppingRecommendationController {

    private final AgentShoppingRecommendationService agentShoppingRecommendationService;
    private final SubscriptionService subscriptionService;

    public AgentShoppingRecommendationController(
            AgentShoppingRecommendationService agentShoppingRecommendationService,
            SubscriptionService subscriptionService
    ) {
        this.agentShoppingRecommendationService = agentShoppingRecommendationService;
        this.subscriptionService = subscriptionService;
    }

    // AI 장보기 추천 생성
    @Operation(
            summary = "AI 장보기 추천 재료",
            description = "구독 중인 사용자만 AI 추천 장보기 재료를 생성할 수 있습니다. 선택한 fridgeId의 냉장고/장보기 상태와 회원 선호 정보를 기준으로 추천합니다."
    )
    @PostMapping("/shopping-recommendations")
    public ResponseEntity<ApiResponse<ShoppingRecommendationResponse>> shoppingRecommendations(
            @Parameter(hidden = true) Authentication authentication,
            @Valid @RequestBody ShoppingRecommendationRequest request
            ) {
                if (!subscriptionService.getMySubscription(authentication.getName()).premium()) {
                    throw new AccessDeniedException("AI 장보기 추천은 구독 후 사용할 수 있습니다.");
                }
                ShoppingRecommendationResponse response =
                        agentShoppingRecommendationService.recommend(authentication.getName(), request);
            return ResponseEntity.ok(
                    ApiResponse.success("AI 장보기 추천이 생성되었습니다.", response)
            );
    }
}
