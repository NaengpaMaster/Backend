package com.naengpa.naengpamasterbackend.admin.dto.response;

import com.naengpa.naengpamasterbackend.member.entity.HouseholdType;
import com.naengpa.naengpamasterbackend.member.entity.Member;
import com.naengpa.naengpamasterbackend.member.entity.MemberRole;
import com.naengpa.naengpamasterbackend.member.entity.MemberStatus;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;

public record AdminMemberResponse(
        @Schema(description = "회원 ID", example = "10") Long memberId,
        @Schema(description = "회원 이메일", example = "member@example.com") String email,
        @Schema(description = "회원 닉네임", example = "냉파초보") String nickname,
        @Schema(description = "회원 역할", example = "USER") MemberRole role,
        @Schema(description = "가구 유형", example = "ONE_PERSON") HouseholdType householdType,
        @Schema(description = "회원 상태", example = "ACTIVE") MemberStatus status,
        @Schema(description = "가입 시각", example = "2026-08-01T12:00:00") LocalDateTime createdAt,
        @Schema(description = "탈퇴 시각", example = "2026-08-10T12:00:00", nullable = true) LocalDateTime deletedAt
) {

    public static AdminMemberResponse from(Member member) {
        return new AdminMemberResponse(
                member.getId(),
                member.getEmail(),
                member.getNickname(),
                member.getRole(),
                member.getHouseholdType(),
                member.getStatus(),
                member.getCreatedAt(),
                member.getDeletedAt()
        );
    }
}
