package com.naengpa.naengpamasterbackend.admin.dto.response;

public record AdminStatisticsSummaryResponse(
        Double averageScore,
        Long scoreMemberCount,
        Long registeredIngredientCount,
        Long expiredIngredientCount,
        Long previousExpiredIngredientCount,
        Double expiredIngredientChangeRate,
        Long createdRecipeCount
) {
}
