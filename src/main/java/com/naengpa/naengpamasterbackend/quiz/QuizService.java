package com.naengpa.naengpamasterbackend.quiz;

import com.naengpa.naengpamasterbackend.quiz.dto.request.QuizSubmitRequest;
import com.naengpa.naengpamasterbackend.quiz.dto.response.QuizSubmitResponse;
import com.naengpa.naengpamasterbackend.quiz.dto.response.QuizTodayResponse;

public interface QuizService {
    QuizTodayResponse getTodayQuiz(String email);
    QuizSubmitResponse submitQuiz(String email, QuizSubmitRequest request);
}
