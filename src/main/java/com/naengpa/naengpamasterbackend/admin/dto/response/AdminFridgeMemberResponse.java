package com.naengpa.naengpamasterbackend.admin.dto.response;

import com.naengpa.naengpamasterbackend.fridge.entity.FridgeMember;
import com.naengpa.naengpamasterbackend.member.entity.Member;

import java.time.LocalDateTime;

public record AdminFridgeMemberResponse(
        Long fridgeMemberId,
        Long memberId,
        String email,
        String nickname,
        String role,
        String status,
        LocalDateTime joinedAt,
        LocalDateTime leftAt
) {
    public static AdminFridgeMemberResponse of(FridgeMember fridgeMember, Member member) {
        return new AdminFridgeMemberResponse(
                fridgeMember.getFridgeMemberId(),
                fridgeMember.getMemberId(),
                member == null ? null : member.getEmail(),
                member == null ? null : member.getNickname(),
                fridgeMember.getRole().name(),
                fridgeMember.getStatus().name(),
                fridgeMember.getJoinedAt(),
                fridgeMember.getLeftAt()
        );
    }
}
