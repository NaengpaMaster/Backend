package com.naengpa.naengpamasterbackend.quiz.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "퀴즈 제출 결과 응답")
public record QuizSubmitResponse(

    @Schema(description = "정답 여부", example = "true")
    Boolean isCorrect,

    @Schema(
            description = "퀴즈 정답 해설",
            example = "복분자는 물기가 많으면 쉽게 곰팡이가 생길 수 있어 씻지 않고 냉장 보관하는 것이 좋습니다."
    )
    String explanation,

    @Schema(description = "퀴즈 제출로 변경된 냉파 점수", example = "2")
    Integer scoreDelta
){}
