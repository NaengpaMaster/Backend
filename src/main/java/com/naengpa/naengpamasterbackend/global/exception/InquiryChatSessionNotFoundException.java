package com.naengpa.naengpamasterbackend.global.exception;

public class InquiryChatSessionNotFoundException extends RuntimeException {

    public InquiryChatSessionNotFoundException() {
        super("문의 챗봇 대화 세션을 찾을 수 없습니다.");
    }
}
