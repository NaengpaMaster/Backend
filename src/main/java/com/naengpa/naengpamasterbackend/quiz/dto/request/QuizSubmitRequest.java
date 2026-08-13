package com.naengpa.naengpamasterbackend.quiz.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "오늘의 퀴즈 제출 요청")
public record QuizSubmitRequest(

    @Schema(description = "제출할 퀴즈 ID", example = "15")
    Long quizId,

    @Schema(
            description = "사용자가 선택한 O/X 답변. O=true, X=false",
            example = "true"
    )
    Boolean submittedAnswer
){}
