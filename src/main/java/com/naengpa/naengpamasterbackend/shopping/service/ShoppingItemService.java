package com.naengpa.naengpamasterbackend.shopping.service;

import com.naengpa.naengpamasterbackend.fridge.entity.FridgeItem;
import com.naengpa.naengpamasterbackend.fridge.entity.FridgeItemHistory;
import com.naengpa.naengpamasterbackend.fridge.entity.FridgeItemHistoryAction;
import com.naengpa.naengpamasterbackend.fridge.dto.response.FridgeItemResponse;
import com.naengpa.naengpamasterbackend.fridge.entity.Fridge;
import com.naengpa.naengpamasterbackend.fridge.repository.FridgeItemHistoryRepository;
import com.naengpa.naengpamasterbackend.fridge.repository.FridgeItemRepository;
import com.naengpa.naengpamasterbackend.fridge.service.FridgeService;
import com.naengpa.naengpamasterbackend.global.exception.DuplicateShoppingItemException;
import com.naengpa.naengpamasterbackend.global.exception.ShoppingItemNotFoundException;
import com.naengpa.naengpamasterbackend.member.entity.Member;
import com.naengpa.naengpamasterbackend.member.repository.MemberRepository;
import com.naengpa.naengpamasterbackend.product.entity.Product;
import com.naengpa.naengpamasterbackend.product.exception.ProductNotFoundException;
import com.naengpa.naengpamasterbackend.product.repository.ProductRepository;
import com.naengpa.naengpamasterbackend.shopping.dto.request.ShoppingItemCheckRequest;
import com.naengpa.naengpamasterbackend.shopping.dto.request.ShoppingItemCreateRequest;
import com.naengpa.naengpamasterbackend.shopping.dto.request.ShoppingItemMoveToFridgeRequest;
import com.naengpa.naengpamasterbackend.shopping.dto.request.ShoppingItemUpdateRequest;
import com.naengpa.naengpamasterbackend.shopping.dto.response.ShoppingItemListResponse;
import com.naengpa.naengpamasterbackend.shopping.dto.response.ShoppingItemResponse;
import com.naengpa.naengpamasterbackend.shopping.entity.ShoppingItem;
import com.naengpa.naengpamasterbackend.shopping.entity.ShoppingItemHistory;
import com.naengpa.naengpamasterbackend.shopping.entity.ShoppingItemHistoryAction;
import com.naengpa.naengpamasterbackend.shopping.repository.ShoppingItemHistoryRepository;
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
    private final ProductRepository productRepository;
    private final FridgeItemRepository fridgeItemRepository;
    private final FridgeItemHistoryRepository fridgeItemHistoryRepository;
    private final FridgeService fridgeService;
    private final ShoppingItemHistoryRepository shoppingItemHistoryRepository;

    public ShoppingItemService(
            ShoppingItemRepository shoppingItemRepository,
            MemberRepository memberRepository,
            ProductRepository productRepository,
            FridgeItemRepository fridgeItemRepository,
            FridgeItemHistoryRepository fridgeItemHistoryRepository,
            FridgeService fridgeService,
            ShoppingItemHistoryRepository shoppingItemHistoryRepository
            ) {
        this.shoppingItemRepository = shoppingItemRepository;
        this.memberRepository = memberRepository;
        this.productRepository = productRepository;
        this.fridgeItemRepository = fridgeItemRepository;
        this.fridgeItemHistoryRepository = fridgeItemHistoryRepository;
        this.fridgeService = fridgeService;
        this.shoppingItemHistoryRepository = shoppingItemHistoryRepository;
    }

    //회원 인증 공통 로직
    private Member findMemberByEmail(String email) {
        return memberRepository.findByEmail(email)
                .orElseThrow(() -> new BadCredentialsException("회원을 찾을 수 없습니다."));
    }

    private ShoppingItem findAccessibleShoppingItem(Long shoppingItemId, Long fridgeId) {
        return shoppingItemRepository.findByShoppingItemIdAndFridgeIdAndIsDeletedFalse(shoppingItemId, fridgeId)
                .orElseThrow(ShoppingItemNotFoundException::new);
    }

    private Product findProduct(Long productId) {
        return productRepository
                .findById(productId)
                .orElseThrow(() -> new ProductNotFoundException(productId));
    }

    private Long resolveFridgeId(Member member, Long fridgeId) {
        if (fridgeId == null) {
            return fridgeService.getMyActiveFridge(member).getFridgeId();
        }
        Fridge fridge = fridgeService.getAccessibleFridge(fridgeId, member.getId());
        return fridge.getFridgeId();
    }

    private void saveShoppingHistory(
            ShoppingItem shoppingItem,
            Long actorMemberId,
            ShoppingItemHistoryAction actionType,
            String productName
    ) {
        shoppingItemHistoryRepository.save(
                ShoppingItemHistory.create(shoppingItem, actorMemberId, actionType, productName)
        );
    }

    //장보기 등록
    @Transactional
    public ShoppingItemResponse createShoppingItem(String email , @Valid ShoppingItemCreateRequest request) {
        return createShoppingItem(email, null, request);
    }

    @Transactional
    public ShoppingItemResponse createShoppingItem(String email, Long fridgeId, @Valid ShoppingItemCreateRequest request) {
        Member member = findMemberByEmail(email);
        Long targetFridgeId = resolveFridgeId(member, fridgeId);

        //존재하는 사전 재료인지
        Product product = findProduct(request.productId());

        //중복 체크
        if (shoppingItemRepository.existsByFridgeIdAndProductIdAndIsDeletedFalseAndIsPurchasedFalse(
                targetFridgeId,
                request.productId()
        )) {
            throw new DuplicateShoppingItemException();
        }


        ShoppingItem shoppingItem = ShoppingItem.create(
                member.getId(),
                targetFridgeId,
                request.productId(),
                request.quantity()
        );

        ShoppingItem savedShoppingItem = shoppingItemRepository.save(shoppingItem);
        saveShoppingHistory(savedShoppingItem, member.getId(), ShoppingItemHistoryAction.CREATED, product.getName());

        return ShoppingItemResponse.from(savedShoppingItem);
    }

    //장보기 조회
    public List<ShoppingItemListResponse> findShoppingItems(String email) {
        return findShoppingItems(email, null);
    }

    public List<ShoppingItemListResponse> findShoppingItems(String email, Long fridgeId) {
        Member member = findMemberByEmail(email);
        Long targetFridgeId = resolveFridgeId(member, fridgeId);


        List<ShoppingItem> shoppingItems = shoppingItemRepository.findByFridgeIdAndIsDeletedFalse(targetFridgeId);

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
        deleteShoppingItem(email, shoppingItemId, null);
    }

    @Transactional
    public void deleteShoppingItem(String email, @Valid Long shoppingItemId, Long fridgeId) {
        Member member = findMemberByEmail(email);
        Long targetFridgeId = resolveFridgeId(member, fridgeId);

        ShoppingItem shoppingItem = findAccessibleShoppingItem(shoppingItemId, targetFridgeId);
        String productName = findProduct(shoppingItem.getProductId()).getName();

        shoppingItem.delete();
        saveShoppingHistory(shoppingItem, member.getId(), ShoppingItemHistoryAction.DELETED, productName);

    }

    //장바구니 구매 여부
    @Transactional
    public ShoppingItemResponse updateShoppingItemPurchased(
            String email,
            Long shoppingItemId,
            ShoppingItemCheckRequest request
    ) {
        return updateShoppingItemPurchased(email, shoppingItemId, null, request);
    }

    @Transactional
    public ShoppingItemResponse updateShoppingItemPurchased(
            String email,
            Long shoppingItemId,
            Long fridgeId,
            ShoppingItemCheckRequest request
    ) {
        Member member = findMemberByEmail(email);
        Long targetFridgeId = resolveFridgeId(member, fridgeId);

        ShoppingItem shoppingItem = findAccessibleShoppingItem(shoppingItemId, targetFridgeId);
        String productName = findProduct(shoppingItem.getProductId()).getName();

        shoppingItem.updatePurchased(request.isPurchased());
        ShoppingItemHistoryAction actionType = request.isPurchased()
                ? ShoppingItemHistoryAction.CHECKED
                : ShoppingItemHistoryAction.UNCHECKED;
        saveShoppingHistory(shoppingItem, member.getId(), actionType, productName);

        return ShoppingItemResponse.from(shoppingItem);
    }

    @Transactional
    public ShoppingItemResponse updateShoppingItem(
            String email,
            Long shoppingItemId,
            ShoppingItemUpdateRequest request
    ) {
        return updateShoppingItem(email, shoppingItemId, null, request);
    }

    @Transactional
    public ShoppingItemResponse updateShoppingItem(
            String email,
            Long shoppingItemId,
            Long fridgeId,
            ShoppingItemUpdateRequest request
    ) {
        Member member = findMemberByEmail(email);
        Long targetFridgeId = resolveFridgeId(member, fridgeId);

        ShoppingItem shoppingItem = findAccessibleShoppingItem(shoppingItemId, targetFridgeId);
        String productName = findProduct(shoppingItem.getProductId()).getName();

        shoppingItem.updateQuantity(request.quantity());
        saveShoppingHistory(shoppingItem, member.getId(), ShoppingItemHistoryAction.UPDATED, productName);

        return ShoppingItemResponse.from(shoppingItem);
    }

    //장보기 항목 냉장고 추가
    @Transactional
    public FridgeItemResponse moveShoppingItemToFridge(
            String email,
            Long shoppingItemId,
            ShoppingItemMoveToFridgeRequest request
    ) {
        return moveShoppingItemToFridge(email, shoppingItemId, null, request);
    }

    @Transactional
    public FridgeItemResponse moveShoppingItemToFridge(
            String email,
            Long shoppingItemId,
            Long fridgeId,
            ShoppingItemMoveToFridgeRequest request
    ) {
        Member member = findMemberByEmail(email);
        Long targetFridgeId = resolveFridgeId(member, fridgeId);

        ShoppingItem shoppingItem = findAccessibleShoppingItem(shoppingItemId, targetFridgeId);

        Product product = findProduct(shoppingItem.getProductId());

        // 프론트에서 별도 유통기한을 보내지 않으면 사전 재료의 기본 유통기한을 사용
        LocalDate expiryDate = request == null ? null : request.expiryDate();
        String memo = request == null ? null : request.memo();

        if (expiryDate == null && product.getDefaultExpiryDays() != null) {
            expiryDate = LocalDate.now().plusDays(product.getDefaultExpiryDays());
        }


        FridgeItem fridgeItem = FridgeItem.create(
                member.getId(),
                targetFridgeId,
                shoppingItem.getProductId(),
                shoppingItem.getQuantity(),
                expiryDate,
                memo
        );

        FridgeItem savedFridgeItem = fridgeItemRepository.save(fridgeItem);
        shoppingItem.delete();
        fridgeItemHistoryRepository.save(
                FridgeItemHistory.create(
                        savedFridgeItem,
                        member.getId(),
                        FridgeItemHistoryAction.CREATED,
                        product.getName()
                )
        );
        saveShoppingHistory(shoppingItem, member.getId(), ShoppingItemHistoryAction.MOVED_TO_FRIDGE, product.getName());

        return FridgeItemResponse.from(savedFridgeItem);
    }

}
