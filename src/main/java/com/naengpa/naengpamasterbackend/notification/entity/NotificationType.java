package com.naengpa.naengpamasterbackend.notification.entity;

public enum NotificationType {
    // 소비기한이 오늘부터 3일 이내로 남은 냉장고 재료 알림
    EXPIRY_SOON,

    // 소비기한이 지난 냉장고 재료 알림
    EXPIRED,

    // 문의사항에 관리자 답변이 등록된 알림
    INQUIRY_ANSWERED,

    // 댓글에 답글이 등록된 알림
    COMMENT_REPLIED,

    // 가족 냉장고 식재료 요청 알림
    FRIDGE_ITEM_REQUESTED,

    // 특정 도메인에 묶이지 않는 일반 알림
    GENERAL
}
