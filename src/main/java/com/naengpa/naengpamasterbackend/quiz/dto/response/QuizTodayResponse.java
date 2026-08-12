package com.naengpa.naengpamasterbackend.quiz.dto.response;

public record QuizTodayResponse (
    Long quizId,
    String statement,
    String sourceProductName,
    Boolean alreadySolved,
    Boolean submittedAnswer,
    Boolean isCorrect,
    String explanation
){}
