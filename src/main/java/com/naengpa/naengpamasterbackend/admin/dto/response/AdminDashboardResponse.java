package com.naengpa.naengpamasterbackend.admin.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

public record AdminDashboardResponse(
        @Schema(description = "활성 관리자 수", example = "3") Long adminCount,
        @Schema(description = "활성 일반 회원 수", example = "1200") Long activeMembers,
        @Schema(description = "비활성 일반 회원 수", example = "35") Long inactiveMembers,
        @Schema(description = "미답변 문의 수", example = "7") Long pendingInquiries
) {}
