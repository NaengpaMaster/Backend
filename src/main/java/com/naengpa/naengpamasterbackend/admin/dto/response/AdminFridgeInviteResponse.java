package com.naengpa.naengpamasterbackend.admin.dto.response;

import com.naengpa.naengpamasterbackend.fridge.entity.FridgeInvite;
import com.naengpa.naengpamasterbackend.member.entity.Member;

import java.time.LocalDateTime;

public record AdminFridgeInviteResponse(
        Long fridgeInviteId,
        Long inviterMemberId,
        String inviterEmail,
        String inviterNickname,
        Long inviteeMemberId,
        String inviteeEmail,
        String inviteeNickname,
        String status,
        LocalDateTime createdAt,
        LocalDateTime respondedAt
) {
    public static AdminFridgeInviteResponse of(FridgeInvite invite, Member inviter, Member invitee) {
        return new AdminFridgeInviteResponse(
                invite.getFridgeInviteId(),
                invite.getInviterMemberId(),
                inviter == null ? null : inviter.getEmail(),
                inviter == null ? null : inviter.getNickname(),
                invite.getInviteeMemberId(),
                invitee == null ? null : invitee.getEmail(),
                invitee == null ? null : invitee.getNickname(),
                invite.getStatus().name(),
                invite.getCreatedAt(),
                invite.getRespondedAt()
        );
    }
}
