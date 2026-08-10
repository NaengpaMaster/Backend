package com.naengpa.naengpamasterbackend.admin.dto.response;

import com.naengpa.naengpamasterbackend.member.entity.HouseholdType;
import com.naengpa.naengpamasterbackend.member.entity.Member;
import com.naengpa.naengpamasterbackend.member.entity.MemberRole;
import com.naengpa.naengpamasterbackend.member.entity.MemberStatus;

import java.time.LocalDateTime;

public record AdminMemberDetailResponse(
        Long memberId,
        String email,
        String nickname,
        HouseholdType householdType,
        LocalDateTime createdAt,
        MemberStatus status,
        MemberRole role,
        Integer naengpaScore
) {
    public static AdminMemberDetailResponse of(Member member, Integer naengpaScore) {
        return new AdminMemberDetailResponse(
                member.getId(),
                member.getEmail(),
                member.getNickname(),
                member.getHouseholdType(),
                member.getCreatedAt(),
                member.getStatus(),
                member.getRole(),
                naengpaScore
        );
    }
}
