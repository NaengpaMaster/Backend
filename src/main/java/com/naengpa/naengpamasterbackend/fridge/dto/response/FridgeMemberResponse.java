package com.naengpa.naengpamasterbackend.fridge.dto.response;

import com.naengpa.naengpamasterbackend.fridge.entity.FridgeMember;
import com.naengpa.naengpamasterbackend.fridge.entity.FridgeMemberRole;
import com.naengpa.naengpamasterbackend.fridge.entity.FridgeMemberStatus;
import com.naengpa.naengpamasterbackend.member.entity.Member;

import java.time.LocalDateTime;

public record FridgeMemberResponse(
        Long fridgeMemberId,
        Long memberId,
        String email,
        String nickname,
        FridgeMemberRole role,
        FridgeMemberStatus status,
        LocalDateTime joinedAt
) {
    public static FridgeMemberResponse of(FridgeMember fridgeMember, Member member) {
        return new FridgeMemberResponse(
                fridgeMember.getFridgeMemberId(),
                member.getId(),
                member.getEmail(),
                member.getNickname(),
                fridgeMember.getRole(),
                fridgeMember.getStatus(),
                fridgeMember.getJoinedAt()
        );
    }
}
