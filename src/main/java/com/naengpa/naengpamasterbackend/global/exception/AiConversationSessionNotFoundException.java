package com.naengpa.naengpamasterbackend.global.exception;

public class AiConversationSessionNotFoundException extends RuntimeException {

    public AiConversationSessionNotFoundException() {
        super("AI 대화 세션을 찾을 수 없습니다.");
    }
}
