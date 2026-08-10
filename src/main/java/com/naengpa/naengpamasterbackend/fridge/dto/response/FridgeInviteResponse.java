package com.naengpa.naengpamasterbackend.fridge.dto.response;

import com.naengpa.naengpamasterbackend.fridge.entity.FridgeInvite;
import com.naengpa.naengpamasterbackend.fridge.entity.FridgeInviteStatus;
import com.naengpa.naengpamasterbackend.member.entity.Member;

import java.time.LocalDateTime;

public record FridgeInviteResponse(
        Long fridgeInviteId,
        Long fridgeId,
        Long inviterMemberId,
        String inviterNickname,
        String inviterEmail,
        Long inviteeMemberId,
        String inviteeNickname,
        String inviteeEmail,
        FridgeInviteStatus status,
        LocalDateTime createdAt
) {
    public static FridgeInviteResponse of(FridgeInvite invite, Member inviter, Member invitee) {
        return new FridgeInviteResponse(
                invite.getFridgeInviteId(),
                invite.getFridgeId(),
                inviter.getId(),
                inviter.getNickname(),
                inviter.getEmail(),
                invitee.getId(),
                invitee.getNickname(),
                invitee.getEmail(),
                invite.getStatus(),
                invite.getCreatedAt()
        );
    }
}
