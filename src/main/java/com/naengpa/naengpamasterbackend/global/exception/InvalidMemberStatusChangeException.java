package com.naengpa.naengpamasterbackend.global.exception;

public class InvalidMemberStatusChangeException extends RuntimeException {
    public InvalidMemberStatusChangeException() {
        super("관리자는 자기 자신을 비활성화할 수 없습니다.");
    }
}
