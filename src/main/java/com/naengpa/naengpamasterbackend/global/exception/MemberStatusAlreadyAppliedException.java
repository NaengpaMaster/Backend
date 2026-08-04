package com.naengpa.naengpamasterbackend.global.exception;

public class MemberStatusAlreadyAppliedException extends RuntimeException {
    public MemberStatusAlreadyAppliedException() {
        super("회원 상태가 이미 변경되었습니다.");
    }

}
