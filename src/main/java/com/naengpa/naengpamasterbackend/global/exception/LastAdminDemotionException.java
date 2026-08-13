package com.naengpa.naengpamasterbackend.global.exception;

public class LastAdminDemotionException extends RuntimeException {
    public LastAdminDemotionException() {
        super("최소 한 명의 관리자가 필요하므로 마지막 관리자의 권한을 해제할 수 없습니다.");
    }
}
