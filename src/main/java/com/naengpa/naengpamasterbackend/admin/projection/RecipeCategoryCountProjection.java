package com.naengpa.naengpamasterbackend.admin.projection;

public interface RecipeCategoryCountProjection {

    String getCategoryName();

    Long getRecipeCount();

    Long getBaseRecipeCount();

    Long getMemberRecipeCount();

    Long getAdminRecipeCount();
}
