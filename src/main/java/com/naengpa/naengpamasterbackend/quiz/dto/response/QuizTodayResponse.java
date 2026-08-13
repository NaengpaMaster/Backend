package com.naengpa.naengpamasterbackend.quiz.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "오늘의 퀴즈 조회 응답")
public record QuizTodayResponse (

    @Schema(description = "퀴즈 ID", example = "15")
    Long quizId,

    @Schema(
            description = "O/X 퀴즈 문장",
            example = "복분자는 냉장 보관할 때 씻지 않은 상태로 보관하는 것이 신선도 유지에 도움이 된다."
    )
    String statement,

    @Schema(description = "퀴즈 생성에 사용된 식재료명", example = "복분자")
    String sourceProductName,

    @Schema(description = "오늘 퀴즈 제출 여부", example = "true")
    Boolean alreadySolved,

    @Schema(
            description = "사용자가 제출한 답변. 제출 전에는 null",
            example = "true",
            nullable = true
    )
    Boolean submittedAnswer,

    @Schema(
            description = "사용자 답변의 정답 여부. 제출 전에는 null",
            example = "false",
            nullable = true
    )
    Boolean isCorrect,

    @Schema(
            description = "퀴즈 해설. 제출 전에는 null",
            example = "복분자는 물기가 많으면 쉽게 곰팡이가 생길 수 있어 씻지 않고 냉장 보관하는 것이 효과적입니다.",
            nullable = true
    )
    String explanation
){}
