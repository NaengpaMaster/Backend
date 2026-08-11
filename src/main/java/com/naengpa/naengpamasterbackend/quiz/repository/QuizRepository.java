package com.naengpa.naengpamasterbackend.quiz.repository;

import com.naengpa.naengpamasterbackend.quiz.entity.Quiz;
import org.springframework.data.jpa.repository.JpaRepository;
import java.time.LocalDate;
import java.util.Optional;

public interface QuizRepository extends JpaRepository<Quiz, Long> {
    Optional<Quiz> findByQuizDate(LocalDate quizDate);
}
