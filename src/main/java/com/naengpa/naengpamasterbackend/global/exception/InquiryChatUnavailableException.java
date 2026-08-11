package com.naengpa.naengpamasterbackend.global.exception;

public class InquiryChatUnavailableException extends RuntimeException {

    public InquiryChatUnavailableException(Throwable cause) {
        super("문의 챗봇 응답을 생성하지 못했습니다. 잠시 후 다시 시도해주세요.", cause);
    }
}
