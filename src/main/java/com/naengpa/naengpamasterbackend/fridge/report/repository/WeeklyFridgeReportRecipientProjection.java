package com.naengpa.naengpamasterbackend.fridge.report.repository;

public interface WeeklyFridgeReportRecipientProjection {

    Long getFridgeId();

    String getFridgeName();

    Long getReceiverMemberId();

    String getReceiverEmail();
}
