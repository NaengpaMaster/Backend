package com.naengpa.naengpamasterbackend.global.exception;

public class MemberRoleAlreadyAppliedException extends RuntimeException {
    public MemberRoleAlreadyAppliedException() {
        super("이미 회원 권한이 변경되었습니다.");
    }
}
