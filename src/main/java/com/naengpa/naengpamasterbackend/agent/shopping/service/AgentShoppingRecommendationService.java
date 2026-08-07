package com.naengpa.naengpamasterbackend.agent.shopping.service;

import com.naengpa.naengpamasterbackend.agent.conversation.service.ConversationCommandService;
import com.naengpa.naengpamasterbackend.agent.shopping.client.AgentShoppingRecommendationClient;
import com.naengpa.naengpamasterbackend.agent.shopping.client.dto.AgentProductPayload;
import com.naengpa.naengpamasterbackend.agent.shopping.client.dto.AgentShoppingRecommendationRequest;
import com.naengpa.naengpamasterbackend.agent.shopping.client.dto.AgentShoppingRecommendationResponse;
import com.naengpa.naengpamasterbackend.agent.shopping.dto.request.ShoppingRecommendationRequest;
import com.naengpa.naengpamasterbackend.agent.shopping.dto.response.ShoppingRecommendationItemResponse;
import com.naengpa.naengpamasterbackend.agent.shopping.dto.response.ShoppingRecommendationResponse;
import com.naengpa.naengpamasterbackend.agent.usage.service.LlmUsageLogService;
import com.naengpa.naengpamasterbackend.fridge.entity.FridgeItem;
import com.naengpa.naengpamasterbackend.fridge.repository.FridgeItemRepository;
import com.naengpa.naengpamasterbackend.member.entity.Member;
import com.naengpa.naengpamasterbackend.member.repository.MemberExcludedProductRepository;
import com.naengpa.naengpamasterbackend.member.repository.MemberFavoriteFoodRepository;
import com.naengpa.naengpamasterbackend.member.repository.MemberRepository;
import com.naengpa.naengpamasterbackend.product.entity.Product;
import com.naengpa.naengpamasterbackend.product.repository.ProductRepository;
import com.naengpa.naengpamasterbackend.shopping.entity.ShoppingItem;
import com.naengpa.naengpamasterbackend.shopping.repository.ShoppingItemRepository;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
public class AgentShoppingRecommendationService {

    private final FridgeItemRepository fridgeItemRepository;
    private final ShoppingItemRepository shoppingItemRepository;
    private final MemberRepository memberRepository;
    private final ProductRepository productRepository;
    private final MemberExcludedProductRepository memberExcludedProductRepository;
    private final MemberFavoriteFoodRepository memberFavoriteFoodRepository;
    private final ConversationCommandService conversationCommandService;
    private final LlmUsageLogService llmUsageLogService;
    private final AgentShoppingRecommendationClient agentShoppingRecommendationClient;
    private final boolean agentEnabled;

    public AgentShoppingRecommendationService(
            FridgeItemRepository fridgeItemRepository,
            ShoppingItemRepository shoppingItemRepository,
            MemberRepository memberRepository,
            ProductRepository productRepository,
            MemberExcludedProductRepository memberExcludedProductRepository,
            MemberFavoriteFoodRepository memberFavoriteFoodRepository,
            ConversationCommandService conversationCommandService,
            LlmUsageLogService llmUsageLogService,
            AgentShoppingRecommendationClient agentShoppingRecommendationClient,
            @Value("${agent.enabled}") boolean agentEnabled
    ) {
        this.fridgeItemRepository = fridgeItemRepository;
        this.shoppingItemRepository = shoppingItemRepository;
        this.memberRepository = memberRepository;
        this.productRepository = productRepository;
        this.memberExcludedProductRepository = memberExcludedProductRepository;
        this.memberFavoriteFoodRepository = memberFavoriteFoodRepository;
        this.conversationCommandService = conversationCommandService;
        this.llmUsageLogService = llmUsageLogService;
        this.agentShoppingRecommendationClient = agentShoppingRecommendationClient;
        this.agentEnabled = agentEnabled;
    }


    public ShoppingRecommendationResponse recommend(
            String email, @Valid ShoppingRecommendationRequest request
    ) {
        Member member = memberRepository.findByEmail(email)
                .orElseThrow(() -> new BadCredentialsException("회원을 찾을 수 없습니다."));

        try {
            List<FridgeItem> fridgeItems =
                    fridgeItemRepository.findByMemberIdAndIsDeletedFalse(member.getId());

            List<ShoppingItem> shoppingItems =
                    shoppingItemRepository.findByMemberIdAndIsDeletedFalse(member.getId());

            // 이미 냉장고에 있거나 장보기 예정인 재료, 회원이 못 먹는 재료는 추천 후보에서 제외
            Set<Long> excludedProductIds = new HashSet<>();

            fridgeItems.stream()
                    .map(FridgeItem::getProductId)
                    .forEach(excludedProductIds::add);

            shoppingItems.stream()
                    .filter(shoppingItem -> !shoppingItem.getIsPurchased())
                    .map(ShoppingItem::getProductId)
                    .forEach(excludedProductIds::add);

            // 못 먹는 재료는 백엔드에서 먼저 제거해 Agent가 실수로 추천하지 못하게 함
            memberExcludedProductRepository.findAllByMemberWithProduct(member).stream()
                    .map(memberExcludedProduct -> memberExcludedProduct.getProduct().getProductId())
                    .forEach(excludedProductIds::add);

            // 사용자가 재추천을 누른 경우, 화면에 이미 노출된 추천 재료도 다음 후보에서 제외
            excludedProductIds.addAll(request.excludeProductIds());

            // 선호 음식은 Product 카테고리와 직접 매핑하지 않고, Agent의 추천 판단 기준으로 전달
            List<String> favoriteFoods = memberFavoriteFoodRepository.findAllByMemberOrderByIdAsc(member).stream()
                    .map(memberFavoriteFood -> memberFavoriteFood.getFoodCategory().getName())
                    .toList();

            int limit = normalizeLimit(request.limit());

            List<Product> activeProducts = productRepository.findByIsActiveTrue();

            List<Product> candidateProducts = activeProducts.stream()
                    .filter(product -> !excludedProductIds.contains(product.getProductId()))
                    .limit(Math.max(limit * 10L, 50L))
                    .toList();

            List<ShoppingRecommendationItemResponse> items = agentEnabled
                    ? recommendWithAgent(member.getId(), fridgeItems, shoppingItems, candidateProducts, favoriteFoods, limit)
                    : recommendWithRuleBased(candidateProducts, favoriteFoods, limit);

            // 추천 결과를 바로 장보기 DB에 넣지는 않고, 사용자가 나중에 볼 수 있도록 AI 대화 기록만 저장
            conversationCommandService.saveShoppingRecommendationHistory(member.getId(), items);

            if (!agentEnabled) {
                llmUsageLogService.saveRuleBasedSuccessLog(member.getId());
            }

            return new ShoppingRecommendationResponse(items);
        } catch (RuntimeException exception) {
            if (agentEnabled) {
                throw exception;
            }

            llmUsageLogService.saveRuleBasedFailureLog(member.getId(), exception.getMessage());
            throw exception;
        }
    }

    private int normalizeLimit(Integer requestLimit) {
        // 백엔드에서도 추천 개수를 보정해서 Agent로 과도한 후보 요청이 넘어가지 않게 함
        int limit = requestLimit == null ? 5 : requestLimit;

        if (limit < 1) {
            return 5;
        }

        if (limit > 20) {
            return 20;
        }

        return limit;
    }

    private List<ShoppingRecommendationItemResponse> recommendWithAgent(
            Long memberId,
            List<FridgeItem> fridgeItems,
            List<ShoppingItem> shoppingItems,
            List<Product> candidateProducts,
            List<String> favoriteFoods,
            int limit
    ) {
        try {
            AgentShoppingRecommendationResponse response = agentShoppingRecommendationClient.recommend(
                    new AgentShoppingRecommendationRequest(
                            limit,
                            favoriteFoods,
                            toProductPayloads(fridgeItems.stream()
                                    .map(FridgeItem::getProductId)
                                    .toList()),
                            toProductPayloads(shoppingItems.stream()
                                    .filter(shoppingItem -> !shoppingItem.getIsPurchased())
                                    .map(ShoppingItem::getProductId)
                                    .toList()),
                            candidateProducts.stream()
                                    .map(AgentProductPayload::from)
                                    .toList()
                    )
            );

            // Agent가 반환한 usage 값을 LLM 사용량 로그로 저장
            llmUsageLogService.saveSuccessLog(
                    memberId,
                    response.usage().modelName(),
                    response.usage().promptTokens(),
                    response.usage().completionTokens(),
                    response.usage().totalTokens(),
                    response.usage().estimatedCost()
            );

            return response.items().stream()
                    .map(item -> new ShoppingRecommendationItemResponse(
                            item.productId(),
                            item.productCategoryId(),
                            item.productName(),
                            item.quantity(),
                            item.reason()
                    ))
                    .toList();
        } catch (RuntimeException exception) {
            // Agent 호출 실패 시 실패 로그를 남기고, 추천 결과 저장은 진행하지 않음
            llmUsageLogService.saveFailureLog(memberId, "agent-api", exception.getMessage());
            throw new IllegalStateException("AI 추천 서버 호출에 실패했습니다.", exception);
        }
    }

    private List<ShoppingRecommendationItemResponse> recommendWithRuleBased(
            List<Product> candidateProducts,
            List<String> favoriteFoods,
            int limit
    ) {
        // Agent 연동을 끈 테스트/개발 환경에서는 기존 rule-based 추천으로 동작
        String reason = buildRuleBasedReason(favoriteFoods);

        return candidateProducts.stream()
                .limit(limit)
                .map(product -> new ShoppingRecommendationItemResponse(
                        product.getProductId(),
                        product.getProductCategoryId(),
                        product.getName(),
                        "1개",
                        reason
                ))
                .toList();
    }

    private String buildRuleBasedReason(List<String> favoriteFoods) {
        if (favoriteFoods.isEmpty()) {
            return "냉장고와 장보기 목록에 없고 못 먹는 재료가 아닌 재료입니다.";
        }

        return "선호 음식(" + String.join(", ", favoriteFoods)
                + ")과 못 먹는 재료 정보를 기준으로 추천되었습니다.";
    }

    private List<AgentProductPayload> toProductPayloads(List<Long> productIds) {
        if (productIds.isEmpty()) {
            return List.of();
        }

        return productRepository.findByProductIdIn(productIds).stream()
                .map(AgentProductPayload::from)
                .toList();
    }
}
