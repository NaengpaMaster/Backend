package com.naengpa.naengpamasterbackend.fridge.service;

import com.naengpa.naengpamasterbackend.fridge.dto.request.FridgeItemCreateRequest;
import com.naengpa.naengpamasterbackend.fridge.dto.request.FridgeItemUpdateRequest;
import com.naengpa.naengpamasterbackend.fridge.dto.request.FridgeItemUsePartialRequest;
import com.naengpa.naengpamasterbackend.fridge.dto.response.FridgeItemListResponse;
import com.naengpa.naengpamasterbackend.fridge.dto.response.FridgeItemResponse;
import com.naengpa.naengpamasterbackend.fridge.entity.FridgeItem;
import com.naengpa.naengpamasterbackend.fridge.repository.FridgeItemRepository;
import com.naengpa.naengpamasterbackend.member.entity.Member;
import com.naengpa.naengpamasterbackend.member.repository.MemberRepository;
import com.naengpa.naengpamasterbackend.product.entity.Product;
import com.naengpa.naengpamasterbackend.product.exception.ProductNotFoundException;
import com.naengpa.naengpamasterbackend.product.repository.ProductRepository;
import com.naengpa.naengpamasterbackend.product.service.ProductService;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
public class FridgeItemService {

    private final FridgeItemRepository fridgeItemRepository;
    private final MemberRepository memberRepository;
    private final ProductService productService;
    private final ProductRepository productRepository;
    private final FridgeService fridgeService;

    private Member findMemberByEmail(String email) {
        return memberRepository.findByEmail(email)
                .orElseThrow(() -> new BadCredentialsException("회원을 찾을 수 없습니다."));
    }

    private FridgeItem findOwnedFridgeItem(Long fridgeItemId, Long memberId) {
        return fridgeItemRepository.findByFridgeItemIdAndMemberIdAndIsDeletedFalse(fridgeItemId, memberId)
                .orElseThrow(() -> new AccessDeniedException("본인 냉장고 재료만 접근할 수 있습니다."));
    }

    public FridgeItemService(
            FridgeItemRepository fridgeItemRepository,
            MemberRepository memberRepository,
            ProductService productService,
            ProductRepository productRepository,
            FridgeService fridgeService) {
        this.fridgeItemRepository = fridgeItemRepository;
        this.memberRepository = memberRepository;
        this.productService = productService;
        this.productRepository = productRepository;
        this.fridgeService = fridgeService;
    }

    // 냉장고 재료 등록
    @Transactional
    public FridgeItemResponse createFridgeItem(String email, FridgeItemCreateRequest request) {

        Member member = findMemberByEmail(email);
        //존재하는 사전 재료인지
        productService.validateExists(request.productId());
        Long fridgeId = fridgeService.findOrCreateFridgeId(member);

        FridgeItem fridgeItem = FridgeItem.create(
                fridgeId,
                member.getId(),
                request.productId(),
                request.quantity(),
                request.expiryDate(),
                request.memo()
        );

        return FridgeItemResponse.from(fridgeItemRepository.save(fridgeItem));
    }

    //냉장고 재료 목록 조회
    public List<FridgeItemListResponse> findFridgeItem(String email) {
        Member member = findMemberByEmail(email);

        List<FridgeItem> fridgeItems =
                fridgeItemRepository.findByMemberIdAndIsDeletedFalse(member.getId());

        return toListResponse(fridgeItems);
    }


    //냉장고 카테고리별 조회
    public List<FridgeItemListResponse> findFridgeItemsByCategory(String email, Long categoryId) {
        Member member = findMemberByEmail(email);

        List<Long> productIds = productRepository.findByProductCategoryId(categoryId)
                .stream()
                .map(Product::getProductId)
                .toList();

        if (productIds.isEmpty()) {
            return List.of();
        }
        List<FridgeItem> fridgeItems =
                fridgeItemRepository.findByMemberIdAndProductIdInAndIsDeletedFalse(member.getId(), productIds);

        return toListResponse(fridgeItems);
    }

    //
    private List<FridgeItemListResponse> toListResponse(List<FridgeItem> fridgeItems) {
        List<Long> productIds = fridgeItems.stream()
                .map(FridgeItem::getProductId)
                .toList();

        List<Product> products = productRepository.findByProductIdIn(productIds);

        return fridgeItems.stream()
                .map(fridgeItem -> {
                    Product product = products.stream()
                            .filter(p -> p.getProductId().equals(fridgeItem.getProductId()))
                            .findFirst()
                            .orElseThrow(() -> new ProductNotFoundException(fridgeItem.getProductId()));

                    return new FridgeItemListResponse(
                            fridgeItem.getFridgeItemId(),
                            fridgeItem.getProductId(),
                            product.getProductCategoryId(),
                            product.getName(),
                            fridgeItem.getQuantity(),
                            fridgeItem.getExpiryDate(),
                            fridgeItem.getMemo()
                    );
                })
                .toList();
    }

    //냉장고 재료 수정
    @Transactional
    public FridgeItemResponse updateFridgeItem(String email, Long fridgeItemId, FridgeItemUpdateRequest request) {
        Member member = findMemberByEmail(email);
        FridgeItem fridgeItem = findOwnedFridgeItem(fridgeItemId, member.getId());

        productService.validateExists(request.productId());

        fridgeItem.update(
                request.productId(),
                request.quantity(),
                request.expiryDate(),
                request.memo()
        );
        return FridgeItemResponse.from(fridgeItem);
    }

    //냉장고 재료 삭제
    @Transactional
    public void deleteFridgeItem(String email, Long fridgeItemId) {
        Member member = findMemberByEmail(email);

        FridgeItem fridgeItem = findOwnedFridgeItem(fridgeItemId, member.getId());

        fridgeItem.delete();
    }

    //냉장고 재료 전부 사용
    @Transactional
    public void useAllFridgeItem(String email, Long fridgeItemId) {
        Member member = findMemberByEmail(email);

        FridgeItem fridgeItem = findOwnedFridgeItem(fridgeItemId, member.getId());

        fridgeItem.useAll();
    }

    //냉장고 재료 일부 사용
    @Transactional
    public FridgeItemResponse usePartialFridgeItem(
            String email,
            Long fridgeItemId,
            FridgeItemUsePartialRequest request
    ) {
        Member member = findMemberByEmail(email);

        FridgeItem fridgeItem = findOwnedFridgeItem(fridgeItemId, member.getId());

        fridgeItem.usePartial(request.quantity());

        return FridgeItemResponse.from(fridgeItem);
    }

    //유통기한 임박 재료 조회
    public List<FridgeItemListResponse> findExpiringSoonFridgeItems(String email) {
        Member member = findMemberByEmail(email);

        LocalDate today = LocalDate.now();
        LocalDate threeDaysLater = today.plusDays(3);

        List<FridgeItem> fridgeItems =
                fridgeItemRepository.findByMemberIdAndExpiryDateBetweenAndIsDeletedFalse(
                        member.getId(),
                        today,
                        threeDaysLater
                );

        return toListResponse(fridgeItems);
    }

    //만료 재료 조회
    public List<FridgeItemListResponse> findExpiredFridgeItems(String email) {
        Member member = findMemberByEmail(email);

        LocalDate today = LocalDate.now();

        List<FridgeItem> fridgeItems =
                fridgeItemRepository.findByMemberIdAndExpiryDateBeforeAndIsDeletedFalse(
                        member.getId(),
                        today
                );

        return toListResponse(fridgeItems);
    }
}
