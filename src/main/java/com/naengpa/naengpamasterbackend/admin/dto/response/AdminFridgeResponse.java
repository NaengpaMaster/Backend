package com.naengpa.naengpamasterbackend.admin.dto.response;

import java.time.LocalDateTime;
import java.util.List;

public record AdminFridgeResponse(
        Long fridgeId,
        String name,
        Long ownerMemberId,
        String ownerEmail,
        String ownerNickname,
        String status,
        String subscriptionStatus,
        int activeMemberCount,
        int maxMemberCount,
        int pendingInviteCount,
        LocalDateTime createdAt,
        List<AdminFridgeMemberResponse> members,
        List<AdminFridgeInviteResponse> pendingInvites
) {
}
