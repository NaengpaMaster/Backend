package com.naengpa.naengpamasterbackend.fridge.dto.response;

import com.naengpa.naengpamasterbackend.fridge.entity.Fridge;
import com.naengpa.naengpamasterbackend.fridge.entity.FridgeMember;
import com.naengpa.naengpamasterbackend.fridge.entity.FridgeMemberRole;
import com.naengpa.naengpamasterbackend.fridge.entity.FridgeStatus;
import com.naengpa.naengpamasterbackend.member.entity.Member;

public record FridgeAccessResponse(
        Long fridgeId,
        Long ownerMemberId,
        String ownerNickname,
        String ownerEmail,
        String name,
        FridgeStatus status,
        FridgeMemberRole role,
        boolean mine
) {
    public static FridgeAccessResponse of(Fridge fridge, FridgeMember fridgeMember, Member owner, Long currentMemberId) {
        return new FridgeAccessResponse(
                fridge.getFridgeId(),
                fridge.getOwnerMemberId(),
                owner == null ? null : owner.getNickname(),
                owner == null ? null : owner.getEmail(),
                fridge.getName(),
                fridge.getStatus(),
                fridgeMember.getRole(),
                fridge.getOwnerMemberId().equals(currentMemberId)
        );
    }
}
