package com.naengpa.naengpamasterbackend.global.exception;

public class DuplicateShoppingItemException extends RuntimeException {
    public DuplicateShoppingItemException() {
        super("이미 장보기 목록에 등록된 재료입니다.");
    }
}