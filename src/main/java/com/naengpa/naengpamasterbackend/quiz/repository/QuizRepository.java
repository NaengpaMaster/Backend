package com.naengpa.naengpamasterbackend.quiz.repository;

import com.naengpa.naengpamasterbackend.quiz.entity.Quiz;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDate;
import java.util.Optional;

public interface QuizRepository extends JpaRepository<Quiz, Long> {
    Optional<Quiz> findByQuizDate(LocalDate quizDate);
    boolean existsByQuizDate(LocalDate quizDate);

    @Query(value = "SELECT * FROM quizzes ORDER BY RANDOM() LIMIT 1", nativeQuery = true)
    Optional<Quiz> findRandomPastQuiz();
}
