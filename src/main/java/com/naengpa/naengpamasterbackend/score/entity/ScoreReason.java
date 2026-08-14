package com.naengpa.naengpamasterbackend.score.entity;

public enum ScoreReason {

    EXPIRED_PRODUCT("만료 재료 1일"),
    RECIPE_CREATED("레시피 1건 등록"),
    NO_EXPIRED_4DAYS("만료 재료 없음 유지 4일"),
    QUIZ_CORRECT("오늘의 퀴즈 정답"),
    SIGNUP_BONUS("회원 가입 축하 보너스");

    private final String description;

    ScoreReason(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }

}
