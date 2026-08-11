package com.naengpa.naengpamasterbackend.quiz.dto.response;

public record QuizSubmitResponse(
    Boolean isCorrect,
    String explanation,
    Integer scoreDelta
){}
