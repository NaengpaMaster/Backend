package com.naengpa.naengpamasterbackend.fridge.dto.response;

import com.naengpa.naengpamasterbackend.fridge.entity.Fridge;
import com.naengpa.naengpamasterbackend.fridge.entity.FridgeStatus;

public record FridgeResponse(
        Long fridgeId,
        Long ownerMemberId,
        String name,
        FridgeStatus status
) {
    public static FridgeResponse from(Fridge fridge) {
        return new FridgeResponse(
                fridge.getFridgeId(),
                fridge.getOwnerMemberId(),
                fridge.getName(),
                fridge.getStatus()
        );
    }
}
