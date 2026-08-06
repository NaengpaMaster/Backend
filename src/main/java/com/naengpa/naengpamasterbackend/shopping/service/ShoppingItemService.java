package com.naengpa.naengpamasterbackend.shopping.service;

import com.naengpa.naengpamasterbackend.fridge.entity.FridgeItem;
import com.naengpa.naengpamasterbackend.fridge.dto.response.FridgeItemResponse;
import com.naengpa.naengpamasterbackend.fridge.repository.FridgeItemRepository;
import com.naengpa.naengpamasterbackend.fridge.service.FridgeService;
import com.naengpa.naengpamasterbackend.global.exception.DuplicateShoppingItemException;
import com.naengpa.naengpamasterbackend.global.exception.ShoppingItemNotFoundException;
import com.naengpa.naengpamasterbackend.member.entity.Member;
import com.naengpa.naengpamasterbackend.member.repository.MemberRepository;
import com.naengpa.naengpamasterbackend.product.entity.Product;
import com.naengpa.naengpamasterbackend.product.exception.ProductNotFoundException;
import com.naengpa.naengpamasterbackend.product.repository.ProductRepository;
import com.naengpa.naengpamasterbackend.product.service.ProductService;
import com.naengpa.naengpamasterbackend.shopping.dto.request.ShoppingItemCheckRequest;
import com.naengpa.naengpamasterbackend.shopping.dto.request.ShoppingItemCreateRequest;
import com.naengpa.naengpamasterbackend.shopping.dto.request.ShoppingItemMoveToFridgeRequest;
import com.naengpa.naengpamasterbackend.shopping.dto.request.ShoppingItemUpdateRequest;
import com.naengpa.naengpamasterbackend.shopping.dto.response.ShoppingItemListResponse;
import com.naengpa.naengpamasterbackend.shopping.dto.response.ShoppingItemResponse;
import com.naengpa.naengpamasterbackend.shopping.entity.ShoppingItem;
import com.naengpa.naengpamasterbackend.shopping.repository.ShoppingItemRepository;
import jakarta.validation.Valid;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
public class ShoppingItemService {

    private final ShoppingItemRepository shoppingItemRepository;
    private final MemberRepository memberRepository;
    private final ProductService productService;
    private final ProductRepository productRepository;
    private final FridgeItemRepository fridgeItemRepository;
    private final FridgeService fridgeService;

    public ShoppingItemService(
            ShoppingItemRepository shoppingItemRepository,
            MemberRepository memberRepository,
            ProductService productService,
            ProductRepository productRepository,
            FridgeItemRepository fridgeItemRepository,
            FridgeService fridgeService
            ) {
        this.shoppingItemRepository = shoppingItemRepository;
        this.memberRepository = memberRepository;
        this.productService = productService;
        this.productRepository = productRepository;
        this.fridgeItemRepository = fridgeItemRepository;
        this.fridgeService = fridgeService;
    }

    //회원 인증 공통 로직
    private Member findMemberByEmail(String email) {
        return memberRepository.findByEmail(email)
                .orElseThrow(() -> new BadCredentialsException("회원을 찾을 수 없습니다."));
    }

    private ShoppingItem findOwnedShoppingItem(Long shoppingItemId, Long memberId) {
        return shoppingItemRepository.findByShoppingItemIdAndMemberIdAndIsDeletedFalse(shoppingItemId, memberId)
                .orElseThrow(ShoppingItemNotFoundException::new);
    }

    //장보기 등록
    public ShoppingItemResponse createShoppingItem(String email , @Valid ShoppingItemCreateRequest request) {
        Member member = findMemberByEmail(email);

        //존재하는 사전 재료인지
        productService.validateExists(request.productId());

        //중복 체크
        if (shoppingItemRepository.existsByMemberIdAndProductIdAndIsDeletedFalseAndIsPurchasedFalse(
                member.getId(),
                request.productId()
        )) {
            throw new DuplicateShoppingItemException();
        }

        Long fridgeId = fridgeService.findOrCreateFridgeId(member);

        ShoppingItem shoppingItem = ShoppingItem.create(
                member.getId(),
                fridgeId,
                request.productId(),
                request.quantity()
        );

        return ShoppingItemResponse.from(shoppingItemRepository.save(shoppingItem));
    }

    //장보기 조회
    public List<ShoppingItemListResponse> findShoppingItems(String email) {
        Member member = findMemberByEmail(email);


        List<ShoppingItem> shoppingItems = shoppingItemRepository.findByMemberIdAndIsDeletedFalse(
                member.getId());

        return toListResponse(shoppingItems);
    }

    //ShoppingItem + Product 정보를 합쳐서 화면용 응답으로 바꾸는 메서드
    private List<ShoppingItemListResponse> toListResponse(List<ShoppingItem> shoppingItems) {
        List<Long> productIds = shoppingItems.stream()
                .map(ShoppingItem::getProductId)
                .toList();

        List<Product> products = productRepository.findByProductIdIn(productIds);

        return shoppingItems.stream()
                .map(shoppingItem -> {
                    Product product = products.stream()
                            .filter(p -> p.getProductId().equals(shoppingItem.getProductId()))
                            .findFirst()
                            .orElseThrow(() -> new ProductNotFoundException(shoppingItem.getProductId()));

                    return new ShoppingItemListResponse(
                            shoppingItem.getShoppingItemId(),
                            shoppingItem.getProductId(),
                            product.getProductCategoryId(),
                            product.getName(),
                            shoppingItem.getQuantity(),
                            shoppingItem.getIsPurchased()
                    );
                })
                .toList();
    }

    //장바구니 삭제
    @Transactional
    public void deleteShoppingItem(String email, @Valid Long shoppingItemId) {
        Member member = findMemberByEmail(email);

        ShoppingItem shoppingItem = findOwnedShoppingItem(shoppingItemId, member.getId());

        shoppingItem.delete();


    }

    //장바구니 구매 여부
    @Transactional
    public ShoppingItemResponse updateShoppingItemPurchased(
            String email,
            Long shoppingItemId,
            ShoppingItemCheckRequest request
    ) {
        Member member = findMemberByEmail(email);

        ShoppingItem shoppingItem = findOwnedShoppingItem(shoppingItemId, member.getId());

        shoppingItem.updatePurchased(request.isPurchased());

        return ShoppingItemResponse.from(shoppingItem);
    }

    @Transactional
    public ShoppingItemResponse updateShoppingItem(
            String email,
            Long shoppingItemId,
            ShoppingItemUpdateRequest request
    ) {
        Member member = findMemberByEmail(email);

        ShoppingItem shoppingItem = findOwnedShoppingItem(shoppingItemId, member.getId());

        shoppingItem.updateQuantity(request.quantity());

        return ShoppingItemResponse.from(shoppingItem);
    }

    //장보기 항목 냉장고 추가
    @Transactional
    public FridgeItemResponse moveShoppingItemToFridge(
            String email,
            Long shoppingItemId,
            ShoppingItemMoveToFridgeRequest request
    ) {
        Member member = findMemberByEmail(email);

        ShoppingItem shoppingItem = findOwnedShoppingItem(shoppingItemId, member.getId());

        Product product = productRepository
                .findById(shoppingItem.getProductId())
                .orElseThrow(() -> new ProductNotFoundException(shoppingItem.getProductId()));

        // 프론트에서 별도 유통기한을 보내지 않으면 사전 재료의 기본 유통기한을 사용
        LocalDate expiryDate = request == null ? null : request.expiryDate();
        String memo = request == null ? null : request.memo();

        if (expiryDate == null && product.getDefaultExpiryDays() != null) {
            expiryDate = LocalDate.now().plusDays(product.getDefaultExpiryDays());
        }

        Long fridgeId = fridgeService.findOrCreateFridgeId(member);

        FridgeItem fridgeItem = FridgeItem.create(
                fridgeId,
                member.getId(),
                shoppingItem.getProductId(),
                shoppingItem.getQuantity(),
                expiryDate,
                memo
        );

        FridgeItem savedFridgeItem = fridgeItemRepository.save(fridgeItem);
        shoppingItem.delete();

        return FridgeItemResponse.from(savedFridgeItem);
    }

}
