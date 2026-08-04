package com.naengpa.naengpamasterbackend.agent.shopping.controller;

import com.naengpa.naengpamasterbackend.agent.shopping.dto.request.ShoppingRecommendationRequest;
import com.naengpa.naengpamasterbackend.agent.shopping.dto.response.ShoppingRecommendationResponse;
import com.naengpa.naengpamasterbackend.agent.shopping.service.AgentShoppingRecommendationService;
import com.naengpa.naengpamasterbackend.global.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/agent")
@Tag(name = "AI 장보기 추천 재료", description="AI 장보기 추천 재료 API")
public class AgentShoppingRecommendationController {

    private final AgentShoppingRecommendationService agentShoppingRecommendationService;

    public AgentShoppingRecommendationController(AgentShoppingRecommendationService agentShoppingRecommendationService) {
        this.agentShoppingRecommendationService = agentShoppingRecommendationService;
    }

    // AI 장보기 추천 생성
    @Operation(
            summary = "AI 장보기 추천 재료",
            description = "AI 추천 장보기 재료를 생성한다."
    )
    @PostMapping("/shopping-recommendations")
    public ResponseEntity<ApiResponse<ShoppingRecommendationResponse>> shoppingRecommendations(
            @Parameter(hidden = true) Authentication authentication,
            @Valid @RequestBody ShoppingRecommendationRequest request
            ) {
                ShoppingRecommendationResponse response =
                        agentShoppingRecommendationService.recommend(authentication.getName(), request);
            return ResponseEntity.ok(
                    ApiResponse.success("AI 장보기 추천이 생성되었습니다.", response)
            );
    }
}
