package com.naengpa.naengpamasterbackend.admin.dto.response;

import org.springframework.data.domain.Page;

import java.util.List;

public record AdminProductPageResponse(
        List<AdminProductResponse> content,
        int totalPages,
        long totalElements,
        long totalProductCount,
        long activeProductCount
) {
    public static AdminProductPageResponse from(
            Page<AdminProductResponse> page,
            long totalProductCount,
            long activeProductCount
    ) {
        return new AdminProductPageResponse(
                page.getContent(),
                page.getTotalPages(),
                page.getTotalElements(),
                totalProductCount,
                activeProductCount
        );
    }
}
