package com.naengpa.naengpamasterbackend.admin.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;

public record AdminHomeResponse(
        @Schema(description = "현재 활성 회원 수", example = "1200")
        long activeMemberCount,
        @Schema(description = "현재 비활성 회원 수", example = "35")
        long inactiveMemberCount,
        @Schema(description = "오늘 신규 가입 회원 수", example = "8")
        long todayNewMemberCount,
        @Schema(description = "오늘 비활성 처리된 고유 회원 수", example = "2")
        long todayInactiveMemberCount,
        @Schema(description = "전체 미답변 문의 수", example = "7")
        long pendingInquiryCount,
        @Schema(description = "24시간을 초과한 미답변 문의 수", example = "1")
        long overduePendingInquiryCount,
        @Schema(description = "전체 레시피 수", example = "340")
        long totalRecipeCount,
        @Schema(description = "회원이 등록한 레시피 수", example = "120")
        long memberRecipeCount,
        @Schema(description = "활성 사전 재료 수", example = "88")
        long activeProductCount,
        @Schema(description = "비활성 사전 재료 수", example = "4")
        long inactiveProductCount,
        @Schema(description = "운영 요약 조회 시각", example = "2026-08-11T10:30:00")
        LocalDateTime refreshedAt
) {
}
