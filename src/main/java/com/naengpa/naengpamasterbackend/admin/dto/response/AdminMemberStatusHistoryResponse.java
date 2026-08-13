package com.naengpa.naengpamasterbackend.admin.dto.response;

import com.naengpa.naengpamasterbackend.member.entity.MemberStatus;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;

public record AdminMemberStatusHistoryResponse(
        @Schema(description = "회원 상태 변경 이력 ID", example = "1")
        Long memberStatusHistoryId,
        @Schema(description = "회원 ID", example = "10")
        Long memberId,
        @Schema(description = "회원 닉네임", example = "냉파초보")
        String nickname,
        @Schema(description = "회원 이메일", example = "member@example.com")
        String email,
        @Schema(description = "변경 전 상태", example = "ACTIVE")
        MemberStatus previousStatus,
        @Schema(description = "변경 후 상태", example = "INACTIVE")
        MemberStatus changedStatus,
        @Schema(description = "현재 회원 상태", example = "ACTIVE")
        MemberStatus currentStatus,
        @Schema(description = "상태 변경 시각", example = "2026-08-11T10:00:00")
        LocalDateTime changedAt
) {
}
