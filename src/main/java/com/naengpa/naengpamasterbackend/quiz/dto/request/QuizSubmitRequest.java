package com.naengpa.naengpamasterbackend.quiz.dto.request;

public record QuizSubmitRequest(
    Long quizId,
    Boolean submittedAnswer
){}
