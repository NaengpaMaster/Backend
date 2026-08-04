package com.naengpa.naengpamasterbackend.agent.shopping.service;

import com.naengpa.naengpamasterbackend.agent.shopping.dto.request.ShoppingRecommendationRequest;
import com.naengpa.naengpamasterbackend.agent.shopping.dto.response.ShoppingRecommendationItemResponse;
import com.naengpa.naengpamasterbackend.agent.shopping.dto.response.ShoppingRecommendationResponse;
import com.naengpa.naengpamasterbackend.fridge.entity.FridgeItem;
import com.naengpa.naengpamasterbackend.fridge.repository.FridgeItemRepository;
import com.naengpa.naengpamasterbackend.member.entity.Member;
import com.naengpa.naengpamasterbackend.member.repository.MemberRepository;
import com.naengpa.naengpamasterbackend.product.repository.ProductRepository;
import com.naengpa.naengpamasterbackend.shopping.entity.ShoppingItem;
import com.naengpa.naengpamasterbackend.shopping.repository.ShoppingItemRepository;
import jakarta.validation.Valid;
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

    public AgentShoppingRecommendationService(FridgeItemRepository fridgeItemRepository, ShoppingItemRepository shoppingItemRepository, MemberRepository memberRepository, ProductRepository productRepository) {
        this.fridgeItemRepository = fridgeItemRepository;
        this.shoppingItemRepository = shoppingItemRepository;
        this.memberRepository = memberRepository;
        this.productRepository = productRepository;
    }


    public ShoppingRecommendationResponse recommend(
            String email, @Valid ShoppingRecommendationRequest request
    ) {
        Member member = memberRepository.findByEmail(email)
                .orElseThrow(() -> new BadCredentialsException("회원을 찾을 수 없습니다."));

        List<FridgeItem> fridgeItems =
                fridgeItemRepository.findByMemberIdAndIsDeletedFalse(member.getId());

        List<ShoppingItem> shoppingItems =
                shoppingItemRepository.findByMemberIdAndIsDeletedFalse(member.getId());

        // 이미 냉장고에 있거나 장보기 예정인 재료는 제외하고, 남은 사전 재료를 추천한다
        Set<Long> excludedProductIds = new HashSet<>();

        fridgeItems.stream()
                .map(FridgeItem::getProductId)
                .forEach(excludedProductIds::add);

        shoppingItems.stream()
                .filter(shoppingItem -> !shoppingItem.getIsPurchased())
                .map(ShoppingItem::getProductId)
                .forEach(excludedProductIds::add);

        // 추천 수량 제한
        int limit = request.limit() == null ? 5 : request.limit();

        if (limit < 1) {
            limit = 5;
        }

        if (limit > 20) {
            limit = 20;
        }

        List<ShoppingRecommendationItemResponse> items =
                productRepository.findByIsActiveTrue().stream()
                        .filter(product -> !excludedProductIds.contains(product.getProductId()))
                        .limit(limit)
                        .map(product -> new ShoppingRecommendationItemResponse(
                                product.getProductId(),
                                product.getProductCategoryId(),
                                product.getName(),
                                "1개",
                                "냉장고와 장보기 목록에 없는 재료입니다."
                        ))
                        .toList();

        return new ShoppingRecommendationResponse(items);
    }
}
