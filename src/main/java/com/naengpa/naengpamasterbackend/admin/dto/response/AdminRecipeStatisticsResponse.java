package com.naengpa.naengpamasterbackend.admin.dto.response;

import java.time.LocalDate;
import java.util.List;

public record AdminRecipeStatisticsResponse(
        LocalDate startDate,
        LocalDate endDate,
        Long totalRecipeCount,
        Long baseRecipeCount,
        Long memberRecipeCount,
        Long adminRecipeCount,
        List<CategoryStatistics> categoryStatistics
) {
    public record CategoryStatistics(
            String categoryName,
            Long recipeCount,
            Long baseRecipeCount,
            Long memberRecipeCount,
            Long adminRecipeCount
    ) {
    }
}
