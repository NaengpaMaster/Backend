package com.naengpa.naengpamasterbackend.receipt.dto.request;

import java.util.List;

public record ReceiptFridgeRegisterRequest(
        List<Long> receiptItemIds
) {
}
