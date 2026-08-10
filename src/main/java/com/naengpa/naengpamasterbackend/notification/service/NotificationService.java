package com.naengpa.naengpamasterbackend.notification.service;

import com.naengpa.naengpamasterbackend.fridge.entity.FridgeItem;
import com.naengpa.naengpamasterbackend.fridge.repository.FridgeItemRepository;
import com.naengpa.naengpamasterbackend.global.exception.MemberNotFoundException;
import com.naengpa.naengpamasterbackend.notification.dto.response.NotificationResponse;
import com.naengpa.naengpamasterbackend.notification.entity.Notification;
import com.naengpa.naengpamasterbackend.notification.entity.NotificationTargetType;
import com.naengpa.naengpamasterbackend.notification.entity.NotificationType;
import com.naengpa.naengpamasterbackend.notification.repository.NotificationRepository;
import com.naengpa.naengpamasterbackend.product.entity.Product;
import com.naengpa.naengpamasterbackend.product.repository.ProductRepository;
import com.naengpa.naengpamasterbackend.member.entity.Member;
import com.naengpa.naengpamasterbackend.member.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class NotificationService {

    private final NotificationRepository notificationRepository;
    private final MemberRepository memberRepository;
    private final FridgeItemRepository fridgeItemRepository;
    private final ProductRepository productRepository;

    @Transactional
    public List<NotificationResponse> getUnreadNotifications(String email) {
        Member member = findMember(email);

        return notificationRepository.findByMemberIdAndReadAtIsNullOrderByCreatedAtDesc(member.getId()).stream()
                .map(NotificationResponse::from)
                .toList();
    }

    @Transactional
    public void markAsRead(String email, Long notificationId) {
        Member member = findMember(email);
        Notification notification = notificationRepository.findByNotificationIdAndMemberId(notificationId, member.getId())
                .orElseThrow(() -> new AccessDeniedException("본인 알림만 확인할 수 있습니다."));

        notification.markAsRead();
    }

    @Transactional
    public void markAllAsRead(String email) {
        Member member = findMember(email);
        notificationRepository.findByMemberIdAndReadAtIsNullOrderByCreatedAtDesc(member.getId())
                .forEach(Notification::markAsRead);
    }

    @Transactional
    public void createInquiryAnsweredNotification(Long memberId, Long inquiryId) {
        boolean exists = notificationRepository.existsByMemberIdAndTypeAndTargetTypeAndTargetId(
                memberId,
                NotificationType.INQUIRY_ANSWERED,
                NotificationTargetType.INQUIRY,
                inquiryId
        );
        if (exists) {
            return;
        }

        notificationRepository.save(Notification.create(
                memberId,
                NotificationType.INQUIRY_ANSWERED,
                "문의 답변 알림",
                "문의하신 내용에 답변이 등록되었습니다.",
                NotificationTargetType.INQUIRY,
                inquiryId,
                null
        ));
    }

    @Transactional
    public void createFridgeItemRequestedNotification(
            Long requestedMemberId,
            Long shareRequestId,
            String requesterName,
            String productName,
            String requestedQuantity,
            String requestMessage
    ) {
        String content = requestMessage == null || requestMessage.isBlank()
                ? requesterName + "님이 " + productName + " " + requestedQuantity + "을(를) 요청했어요."
                : requesterName + "님: " + requestMessage;

        notificationRepository.save(Notification.create(
                requestedMemberId,
                NotificationType.FRIDGE_ITEM_REQUESTED,
                "식재료 요청 알림",
                content,
                NotificationTargetType.FRIDGE_ITEM_SHARE_REQUEST,
                shareRequestId,
                null
        ));
    }

    @Transactional
    public void createExpiryNotifications(Long memberId) {
        LocalDate today = LocalDate.now();
        LocalDate threeDaysLater = today.plusDays(3);

        List<FridgeItem> expiringItems = fridgeItemRepository.findByMemberIdAndExpiryDateBetweenAndIsDeletedFalse(
                memberId,
                today,
                threeDaysLater
        );
        List<FridgeItem> expiredItems = fridgeItemRepository.findByMemberIdAndExpiryDateBeforeAndIsDeletedFalse(
                memberId,
                today
        );

        List<Long> productIds = expiringItems.stream()
                .map(FridgeItem::getProductId)
                .collect(Collectors.toList());
        productIds.addAll(expiredItems.stream().map(FridgeItem::getProductId).toList());

        Map<Long, String> productNames = productIds.isEmpty()
                ? Map.of()
                : productRepository.findByProductIdInAndIsActiveTrue(productIds).stream()
                        .collect(Collectors.toMap(Product::getProductId, Product::getName, (a, b) -> a));

        expiringItems.forEach(item -> createExpiryNotificationIfAbsent(
                memberId,
                item,
                productNames.getOrDefault(item.getProductId(), "재료"),
                NotificationType.EXPIRY_SOON
        ));
        expiredItems.forEach(item -> createExpiryNotificationIfAbsent(
                memberId,
                item,
                productNames.getOrDefault(item.getProductId(), "재료"),
                NotificationType.EXPIRED
        ));
    }

    private void createExpiryNotificationIfAbsent(
            Long memberId,
            FridgeItem item,
            String productName,
            NotificationType type
    ) {
        boolean exists = notificationRepository.existsByMemberIdAndTypeAndTargetTypeAndTargetIdAndTargetExpiryDate(
                memberId,
                type,
                NotificationTargetType.FRIDGE_ITEM,
                item.getFridgeItemId(),
                item.getExpiryDate()
        );
        if (exists) {
            return;
        }

        String title = type == NotificationType.EXPIRED ? "소비기한 만료 알림" : "소비기한 임박 알림";
        String content = type == NotificationType.EXPIRED
                ? productName + "의 소비기한이 지났습니다."
                : productName + "의 소비기한이 3일 이내입니다.";

        notificationRepository.save(Notification.create(
                memberId,
                type,
                title,
                content,
                NotificationTargetType.FRIDGE_ITEM,
                item.getFridgeItemId(),
                item.getExpiryDate()
        ));
    }

    private Member findMember(String email) {
        return memberRepository.findByEmail(email)
                .orElseThrow(MemberNotFoundException::new);
    }
}
