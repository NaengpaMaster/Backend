package com.naengpa.naengpamasterbackend.global.exception;

public class QuizAlreadySolvedException extends RuntimeException {
    public QuizAlreadySolvedException(String message) {
        super(message);
    }
}
