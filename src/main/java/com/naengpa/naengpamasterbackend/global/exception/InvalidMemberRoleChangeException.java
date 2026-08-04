package com.naengpa.naengpamasterbackend.global.exception;

public class InvalidMemberRoleChangeException extends RuntimeException {
    public InvalidMemberRoleChangeException() {
        super("관리자는 자기 자신의 권한을 해제할 수 없습니다.");
    }


}
