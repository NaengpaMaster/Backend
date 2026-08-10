package com.naengpa.naengpamasterbackend.admin.dto.response;

import com.naengpa.naengpamasterbackend.member.entity.MemberStatus;

import java.time.LocalDateTime;

public record AdminMemberStatusHistoryResponse(
        Long memberStatusHistoryId,
        Long memberId,
        String nickname,
        String email,
        MemberStatus previousStatus,
        MemberStatus changedStatus,
        MemberStatus currentStatus,
        LocalDateTime changedAt
) {
}
