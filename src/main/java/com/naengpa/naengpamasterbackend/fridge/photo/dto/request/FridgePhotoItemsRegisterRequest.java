package com.naengpa.naengpamasterbackend.fridge.photo.dto.request;

import java.util.List;

public record FridgePhotoItemsRegisterRequest(
        List<Long> fridgePhotoItemIds
) {
}
