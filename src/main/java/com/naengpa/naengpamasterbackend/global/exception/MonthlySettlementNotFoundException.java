package com.naengpa.naengpamasterbackend.global.exception;

public class MonthlySettlementNotFoundException extends RuntimeException {

    public MonthlySettlementNotFoundException() {
        super("월별 정산을 찾을 수 없습니다.");
    }
}
