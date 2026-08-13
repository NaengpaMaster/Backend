package com.naengpa.naengpamasterbackend.admin.projection;

public interface RecipeCountProjection {

    Long getTotalCount();

    Long getBaseCount();

    Long getMemberCount();

    Long getAdminCount();
}
