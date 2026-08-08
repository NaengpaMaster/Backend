package com.naengpa.naengpamasterbackend.fridge.service;

import com.naengpa.naengpamasterbackend.fridge.dto.request.FridgeItemCreateRequest;
import com.naengpa.naengpamasterbackend.fridge.dto.request.FridgeItemShareRequestAcceptRequest;
import com.naengpa.naengpamasterbackend.fridge.dto.request.FridgeItemShareRequestCreateRequest;
import com.naengpa.naengpamasterbackend.fridge.dto.request.FridgeItemTransferRequest;
import com.naengpa.naengpamasterbackend.fridge.dto.request.FridgeItemUpdateRequest;
import com.naengpa.naengpamasterbackend.fridge.dto.request.FridgeItemUsePartialRequest;
import com.naengpa.naengpamasterbackend.fridge.dto.response.FridgeItemListResponse;
import com.naengpa.naengpamasterbackend.fridge.dto.response.FridgeItemResponse;
import com.naengpa.naengpamasterbackend.fridge.dto.response.FridgeItemShareRequestResponse;
import com.naengpa.naengpamasterbackend.fridge.entity.Fridge;
import com.naengpa.naengpamasterbackend.fridge.entity.FridgeItem;
import com.naengpa.naengpamasterbackend.fridge.entity.FridgeItemHistory;
import com.naengpa.naengpamasterbackend.fridge.entity.FridgeItemHistoryAction;
import com.naengpa.naengpamasterbackend.fridge.entity.FridgeItemShareRequest;
import com.naengpa.naengpamasterbackend.fridge.entity.FridgeItemShareRequestStatus;
import com.naengpa.naengpamasterbackend.fridge.repository.FridgeItemShareRequestRepository;
import com.naengpa.naengpamasterbackend.fridge.repository.FridgeItemHistoryRepository;
import com.naengpa.naengpamasterbackend.fridge.repository.FridgeItemRepository;
import com.naengpa.naengpamasterbackend.member.entity.Member;
import com.naengpa.naengpamasterbackend.member.repository.MemberRepository;
import com.naengpa.naengpamasterbackend.notification.service.NotificationService;
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
    private final FridgeItemHistoryRepository fridgeItemHistoryRepository;
    private final MemberRepository memberRepository;
    private final ProductService productService;
    private final ProductRepository productRepository;
    private final FridgeService fridgeService;
    private final FridgeItemShareRequestRepository fridgeItemShareRequestRepository;
    private final NotificationService notificationService;

    private Member findMemberByEmail(String email) {
        return memberRepository.findByEmail(email)
                .orElseThrow(() -> new BadCredentialsException("회원을 찾을 수 없습니다."));
    }

    private FridgeItem findAccessibleFridgeItem(Long fridgeItemId, Long fridgeId) {
        return fridgeItemRepository.findByFridgeItemIdAndFridgeIdAndIsDeletedFalse(fridgeItemId, fridgeId)
                .orElseThrow(() -> new AccessDeniedException("접근할 수 없는 냉장고 재료입니다."));
    }

    private Fridge resolveFridge(Member member, Long fridgeId) {
        if (fridgeId == null) {
            return fridgeService.getMyActiveFridge(member);
        }
        return fridgeService.getAccessibleFridge(fridgeId, member.getId());
    }

    public FridgeItemService(
            FridgeItemRepository fridgeItemRepository,
            FridgeItemHistoryRepository fridgeItemHistoryRepository,
            MemberRepository memberRepository,
            ProductService productService,
            ProductRepository productRepository,
            FridgeService fridgeService,
            FridgeItemShareRequestRepository fridgeItemShareRequestRepository,
            NotificationService notificationService) {
        this.fridgeItemRepository = fridgeItemRepository;
        this.fridgeItemHistoryRepository = fridgeItemHistoryRepository;
        this.memberRepository = memberRepository;
        this.productService = productService;
        this.productRepository = productRepository;
        this.fridgeService = fridgeService;
        this.fridgeItemShareRequestRepository = fridgeItemShareRequestRepository;
        this.notificationService = notificationService;
    }

    // 냉장고 재료 등록
    @Transactional
    public FridgeItemResponse createFridgeItem(String email, FridgeItemCreateRequest request) {
        return createFridgeItem(email, null, request);
    }

    @Transactional
    public FridgeItemResponse createFridgeItem(String email, Long fridgeId, FridgeItemCreateRequest request) {

        Member member = findMemberByEmail(email);
        Fridge fridge = resolveFridge(member, fridgeId);
        productService.validateExists(request.productId());
        Product product = productRepository.findById(request.productId())
                .orElseThrow(() -> new ProductNotFoundException(request.productId()));

        FridgeItem fridgeItem = FridgeItem.create(
                member.getId(),
                fridge.getFridgeId(),
                request.productId(),
                request.quantity(),
                request.expiryDate(),
                request.memo()
        );

        FridgeItem savedFridgeItem = fridgeItemRepository.save(fridgeItem);
        saveHistory(savedFridgeItem, member.getId(), FridgeItemHistoryAction.CREATED, product.getName());

        return FridgeItemResponse.from(savedFridgeItem);
    }

    //냉장고 재료 목록 조회
    @Transactional(readOnly = true)
    public List<FridgeItemListResponse> findFridgeItem(String email) {
        return findFridgeItem(email, null);
    }

    @Transactional(readOnly = true)
    public List<FridgeItemListResponse> findFridgeItem(String email, Long fridgeId) {
        Member member = findMemberByEmail(email);
        Fridge fridge = resolveFridge(member, fridgeId);

        List<FridgeItem> fridgeItems =
                fridgeItemRepository.findByFridgeIdAndIsDeletedFalse(fridge.getFridgeId());

        return toListResponse(fridgeItems);
    }


    //냉장고 카테고리별 조회
    @Transactional(readOnly = true)
    public List<FridgeItemListResponse> findFridgeItemsByCategory(String email, Long categoryId) {
        return findFridgeItemsByCategory(email, categoryId, null);
    }

    @Transactional(readOnly = true)
    public List<FridgeItemListResponse> findFridgeItemsByCategory(String email, Long categoryId, Long fridgeId) {
        Member member = findMemberByEmail(email);
        Fridge fridge = resolveFridge(member, fridgeId);

        List<Long> productIds = productRepository.findByProductCategoryId(categoryId)
                .stream()
                .map(Product::getProductId)
                .toList();

        if (productIds.isEmpty()) {
            return List.of();
        }
        List<FridgeItem> fridgeItems =
                fridgeItemRepository.findByFridgeIdAndProductIdInAndIsDeletedFalse(fridge.getFridgeId(), productIds);

        return toListResponse(fridgeItems);
    }

    //
    private List<FridgeItemListResponse> toListResponse(List<FridgeItem> fridgeItems) {
        if (fridgeItems.isEmpty()) {
            return List.of();
        }

        List<Long> productIds = fridgeItems.stream()
                .map(FridgeItem::getProductId)
                .distinct()
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
        return updateFridgeItem(email, fridgeItemId, null, request);
    }

    @Transactional
    public FridgeItemResponse updateFridgeItem(String email, Long fridgeItemId, Long fridgeId, FridgeItemUpdateRequest request) {
        Member member = findMemberByEmail(email);
        Fridge fridge = resolveFridge(member, fridgeId);
        FridgeItem fridgeItem = findAccessibleFridgeItem(fridgeItemId, fridge.getFridgeId());

        productService.validateExists(request.productId());
        Product product = productRepository.findById(request.productId())
                .orElseThrow(() -> new ProductNotFoundException(request.productId()));

        fridgeItem.update(
                request.productId(),
                request.quantity(),
                request.expiryDate(),
                request.memo()
        );
        saveHistory(fridgeItem, member.getId(), FridgeItemHistoryAction.UPDATED, product.getName());
        return FridgeItemResponse.from(fridgeItem);
    }

    //냉장고 재료 삭제
    @Transactional
    public void deleteFridgeItem(String email, Long fridgeItemId) {
        deleteFridgeItem(email, fridgeItemId, null);
    }

    @Transactional
    public void deleteFridgeItem(String email, Long fridgeItemId, Long fridgeId) {
        Member member = findMemberByEmail(email);
        Fridge fridge = resolveFridge(member, fridgeId);

        FridgeItem fridgeItem = findAccessibleFridgeItem(fridgeItemId, fridge.getFridgeId());

        fridgeItem.delete();
        saveHistory(fridgeItem, member.getId(), FridgeItemHistoryAction.DELETED, findProductName(fridgeItem.getProductId()));
    }

    //냉장고 재료 전부 사용
    @Transactional
    public void useAllFridgeItem(String email, Long fridgeItemId) {
        useAllFridgeItem(email, fridgeItemId, null);
    }

    @Transactional
    public void useAllFridgeItem(String email, Long fridgeItemId, Long fridgeId) {
        Member member = findMemberByEmail(email);
        Fridge fridge = resolveFridge(member, fridgeId);

        FridgeItem fridgeItem = findAccessibleFridgeItem(fridgeItemId, fridge.getFridgeId());

        fridgeItem.useAll();
        saveHistory(fridgeItem, member.getId(), FridgeItemHistoryAction.USED_ALL, findProductName(fridgeItem.getProductId()));
    }

    //냉장고 재료 일부 사용
    @Transactional
    public FridgeItemResponse usePartialFridgeItem(
            String email,
            Long fridgeItemId,
            FridgeItemUsePartialRequest request
    ) {
        return usePartialFridgeItem(email, fridgeItemId, null, request);
    }

    @Transactional
    public FridgeItemResponse usePartialFridgeItem(
            String email,
            Long fridgeItemId,
            Long fridgeId,
            FridgeItemUsePartialRequest request
    ) {
        Member member = findMemberByEmail(email);
        Fridge fridge = resolveFridge(member, fridgeId);

        FridgeItem fridgeItem = findAccessibleFridgeItem(fridgeItemId, fridge.getFridgeId());

        fridgeItem.usePartial(request.quantity());
        saveHistory(fridgeItem, member.getId(), FridgeItemHistoryAction.USED_PARTIAL, findProductName(fridgeItem.getProductId()));

        return FridgeItemResponse.from(fridgeItem);
    }

    @Transactional
    public FridgeItemResponse transferFridgeItem(
            String email,
            Long fridgeItemId,
            FridgeItemTransferRequest request
    ) {
        Member member = findMemberByEmail(email);
        Fridge sourceFridge = fridgeService.getAccessibleFridge(request.sourceFridgeId(), member.getId());
        Fridge targetFridge = fridgeService.getAccessibleFridge(request.targetFridgeId(), member.getId());

        if (sourceFridge.getFridgeId().equals(targetFridge.getFridgeId())) {
            throw new IllegalArgumentException("같은 냉장고로는 식재료를 전달할 수 없습니다.");
        }

        FridgeItem sourceItem = findAccessibleFridgeItem(fridgeItemId, sourceFridge.getFridgeId());
        Product product = productRepository.findById(sourceItem.getProductId())
                .orElseThrow(() -> new ProductNotFoundException(sourceItem.getProductId()));

        boolean transferAll = Boolean.TRUE.equals(request.transferAll());
        String remainingQuantity = request.remainingQuantity() == null ? null : request.remainingQuantity().trim();
        if (transferAll) {
            sourceItem.useAll();
            saveHistory(sourceItem, member.getId(), FridgeItemHistoryAction.USED_ALL, product.getName());
        } else {
            if (remainingQuantity == null || remainingQuantity.isBlank()) {
                throw new IllegalArgumentException("일부만 나눌 때는 내 냉장고에 남길 수량을 입력해주세요.");
            }
            sourceItem.usePartial(remainingQuantity);
            saveHistory(sourceItem, member.getId(), FridgeItemHistoryAction.USED_PARTIAL, product.getName());
        }

        FridgeItem targetItem = FridgeItem.create(
                member.getId(),
                targetFridge.getFridgeId(),
                sourceItem.getProductId(),
                request.transferQuantity(),
                request.expiryDate() == null ? sourceItem.getExpiryDate() : request.expiryDate(),
                request.memo()
        );
        FridgeItem savedTargetItem = fridgeItemRepository.save(targetItem);
        saveHistory(savedTargetItem, member.getId(), FridgeItemHistoryAction.CREATED, product.getName());

        return FridgeItemResponse.from(savedTargetItem);
    }

    @Transactional
    public FridgeItemShareRequestResponse requestFridgeItem(
            String email,
            Long fridgeItemId,
            FridgeItemShareRequestCreateRequest request
    ) {
        Member requester = findMemberByEmail(email);
        Fridge targetFridge = fridgeService.getAccessibleFridge(request.targetFridgeId(), requester.getId());
        FridgeItem sourceItem = fridgeItemRepository.findByFridgeItemIdAndIsDeletedFalse(fridgeItemId)
                .orElseThrow(() -> new AccessDeniedException("접근할 수 없는 냉장고 재료입니다."));
        Fridge sourceFridge = fridgeService.getAccessibleFridge(sourceItem.getFridgeId(), requester.getId());

        if (sourceFridge.getFridgeId().equals(targetFridge.getFridgeId())) {
            throw new IllegalArgumentException("같은 냉장고로는 식재료를 요청할 수 없습니다.");
        }

        if (requester.getId().equals(sourceFridge.getOwnerMemberId())) {
            throw new IllegalArgumentException("내 냉장고의 식재료는 요청할 수 없습니다.");
        }

        Product product = productRepository.findById(sourceItem.getProductId())
                .orElseThrow(() -> new ProductNotFoundException(sourceItem.getProductId()));

        FridgeItemShareRequest savedRequest = fridgeItemShareRequestRepository.save(
                FridgeItemShareRequest.create(
                        requester.getId(),
                        sourceFridge.getOwnerMemberId(),
                        sourceFridge.getFridgeId(),
                        targetFridge.getFridgeId(),
                        sourceItem.getFridgeItemId(),
                        sourceItem.getProductId(),
                        request.requestedQuantity().trim(),
                        request.message() == null ? null : request.message().trim()
                )
        );

        notificationService.createFridgeItemRequestedNotification(
                sourceFridge.getOwnerMemberId(),
                savedRequest.getFridgeItemShareRequestId(),
                requester.getNickname(),
                product.getName(),
                savedRequest.getRequestedQuantity(),
                savedRequest.getMessage()
        );

        return FridgeItemShareRequestResponse.of(savedRequest, product.getName());
    }

    @Transactional
    public FridgeItemResponse acceptShareRequest(
            String email,
            Long shareRequestId,
            FridgeItemShareRequestAcceptRequest request
    ) {
        Member requestedMember = findMemberByEmail(email);
        FridgeItemShareRequest shareRequest = fridgeItemShareRequestRepository
                .findByFridgeItemShareRequestIdAndRequestedMemberIdAndStatus(
                        shareRequestId,
                        requestedMember.getId(),
                        FridgeItemShareRequestStatus.PENDING
                )
                .orElseThrow(() -> new AccessDeniedException("수락할 식재료 요청을 찾을 수 없습니다."));

        Fridge sourceFridge = fridgeService.getAccessibleFridge(shareRequest.getSourceFridgeId(), requestedMember.getId());
        Fridge targetFridge = fridgeService.getAccessibleFridge(shareRequest.getTargetFridgeId(), requestedMember.getId());
        FridgeItem sourceItem = findAccessibleFridgeItem(shareRequest.getFridgeItemId(), sourceFridge.getFridgeId());
        Product product = productRepository.findById(sourceItem.getProductId())
                .orElseThrow(() -> new ProductNotFoundException(sourceItem.getProductId()));

        boolean transferAll = Boolean.TRUE.equals(request.transferAll());
        String remainingQuantity = request.remainingQuantity() == null ? null : request.remainingQuantity().trim();
        if (transferAll) {
            sourceItem.useAll();
            saveHistory(sourceItem, requestedMember.getId(), FridgeItemHistoryAction.USED_ALL, product.getName());
        } else {
            if (remainingQuantity == null || remainingQuantity.isBlank()) {
                throw new IllegalArgumentException("일부만 전달할 때는 내 냉장고에 남길 수량을 입력해주세요.");
            }
            sourceItem.usePartial(remainingQuantity);
            saveHistory(sourceItem, requestedMember.getId(), FridgeItemHistoryAction.USED_PARTIAL, product.getName());
        }

        FridgeItem targetItem = FridgeItem.create(
                requestedMember.getId(),
                targetFridge.getFridgeId(),
                sourceItem.getProductId(),
                shareRequest.getRequestedQuantity(),
                sourceItem.getExpiryDate(),
                request.memo() == null || request.memo().isBlank() ? "식재료 요청 수락" : request.memo().trim()
        );
        FridgeItem savedTargetItem = fridgeItemRepository.save(targetItem);
        saveHistory(savedTargetItem, requestedMember.getId(), FridgeItemHistoryAction.CREATED, product.getName());

        shareRequest.accept();
        return FridgeItemResponse.from(savedTargetItem);
    }

    @Transactional
    public FridgeItemShareRequestResponse rejectShareRequest(String email, Long shareRequestId) {
        Member requestedMember = findMemberByEmail(email);
        FridgeItemShareRequest shareRequest = fridgeItemShareRequestRepository
                .findByFridgeItemShareRequestIdAndRequestedMemberIdAndStatus(
                        shareRequestId,
                        requestedMember.getId(),
                        FridgeItemShareRequestStatus.PENDING
                )
                .orElseThrow(() -> new AccessDeniedException("거절할 식재료 요청을 찾을 수 없습니다."));
        Product product = productRepository.findById(shareRequest.getProductId())
                .orElseThrow(() -> new ProductNotFoundException(shareRequest.getProductId()));

        shareRequest.reject();
        return FridgeItemShareRequestResponse.of(shareRequest, product.getName());
    }

    //유통기한 임박 재료 조회
    public List<FridgeItemListResponse> findExpiringSoonFridgeItems(String email) {
        Member member = findMemberByEmail(email);
        Fridge fridge = fridgeService.getOrCreateDefaultFridge(member);

        LocalDate today = LocalDate.now();
        LocalDate threeDaysLater = today.plusDays(3);

        List<FridgeItem> fridgeItems =
                fridgeItemRepository.findByFridgeIdAndExpiryDateBetweenAndIsDeletedFalse(
                        fridge.getFridgeId(),
                        today,
                        threeDaysLater
                );

        return toListResponse(fridgeItems);
    }

    //만료 재료 조회
    public List<FridgeItemListResponse> findExpiredFridgeItems(String email) {
        Member member = findMemberByEmail(email);
        Fridge fridge = fridgeService.getOrCreateDefaultFridge(member);

        LocalDate today = LocalDate.now();

        List<FridgeItem> fridgeItems =
                fridgeItemRepository.findByFridgeIdAndExpiryDateBeforeAndIsDeletedFalse(
                        fridge.getFridgeId(),
                        today
                );

        return toListResponse(fridgeItems);
    }

    private void saveHistory(
            FridgeItem fridgeItem,
            Long actorMemberId,
            FridgeItemHistoryAction actionType,
            String productName
    ) {
        fridgeItemHistoryRepository.save(FridgeItemHistory.create(
                fridgeItem,
                actorMemberId,
                actionType,
                productName
        ));
    }

    private String findProductName(Long productId) {
        return productRepository.findById(productId)
                .map(Product::getName)
                .orElse("알 수 없는 재료");
    }
}
