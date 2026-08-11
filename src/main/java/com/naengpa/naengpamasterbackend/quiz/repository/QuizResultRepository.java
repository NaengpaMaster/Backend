package com.naengpa.naengpamasterbackend.quiz.repository;

import com.naengpa.naengpamasterbackend.quiz.entity.QuizResult;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface QuizResultRepository extends JpaRepository<QuizResult, Long> {
    boolean existsByMemberIdAndQuizId(Long memberId, Long quizId);
    Optional<QuizResult> findByMemberIdAndQuizId(Long memberId, Long quizId);
}
