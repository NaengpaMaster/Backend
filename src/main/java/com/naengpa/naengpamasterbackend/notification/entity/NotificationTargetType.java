package com.naengpa.naengpamasterbackend.notification.entity;

public enum NotificationTargetType {
    // fridge_items 테이블의 냉장고 재료
    FRIDGE_ITEM,

    // fridge_item_share_requests 테이블의 식재료 요청
    FRIDGE_ITEM_SHARE_REQUEST,

    // inquiries 테이블의 문의사항
    INQUIRY,

    // recipe_comments 테이블의 댓글
    COMMENT,

    // recipes 테이블의 레시피
    RECIPE
}
