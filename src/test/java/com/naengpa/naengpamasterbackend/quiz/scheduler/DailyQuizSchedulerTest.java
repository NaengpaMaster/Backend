package com.naengpa.naengpamasterbackend.quiz.scheduler;

import com.naengpa.naengpamasterbackend.product.repository.ProductRepository;
import com.naengpa.naengpamasterbackend.quiz.client.QuizGenerationClient;
import com.naengpa.naengpamasterbackend.quiz.dto.ProductNameId;
import com.naengpa.naengpamasterbackend.quiz.entity.Quiz;
import com.naengpa.naengpamasterbackend.quiz.repository.QuizRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class DailyQuizSchedulerTest {

    @Mock QuizGenerationClient quizGenerationClient;
    @Mock QuizRepository quizRepository;
    @Mock ProductRepository productRepository;

    @InjectMocks
    DailyQuizScheduler scheduler;

    @Test
    @DisplayName("오늘 퀴즈가 있으면 생성을 건너 뛴다.")
    void skipIfQuizAlreadyExists(){
        given(quizRepository.existsByQuizDate(LocalDate.now())).willReturn(true);

        scheduler.generateDailyQuiz();

        verify(quizGenerationClient, never()).generateQuiz(any());
    }

    @Test
    @DisplayName("정상적으로 퀴즈를 생성하면 첫 시도에 저장된다")
    void generatesQuizSuccessfullyOnFirstTry(){
        given(quizRepository.existsByQuizDate(LocalDate.now())).willReturn(false);
        given(productRepository.findAllActiveProductNameIds())
                .willReturn(List.of(new ProductNameId(1L, "상추")));

        QuizGenerationClient.QuizGenerationResult result =
                new QuizGenerationClient.QuizGenerationResult(
                        "상추는 신문지로 감싸서 냉장 보관하면 더 오래간다.",
                        true,
                        "상추는 수분 손실이 빠른 채소입니다.",
                        "high");

        given(quizGenerationClient.generateQuiz("상추")).willReturn(result);

        scheduler.generateDailyQuiz();

        verify(quizGenerationClient, times(1)).generateQuiz("상추");
        verify(quizRepository, times(1)).save(any(Quiz.class));
    }

    @Test
    @DisplayName("3회 재시도 후에도 실패하면 과거 퀴즈를 복제해 저장한다")
    void fallsBackToPastQuizAfterThreeFailures(){
        given(quizRepository.existsByQuizDate(LocalDate.now())).willReturn(false);
        given(productRepository.findAllActiveProductNameIds())
                .willReturn(List.of(new ProductNameId(1L, "상추")));

        given(quizGenerationClient.generateQuiz("상추"))
                .willThrow(new RuntimeException("통신 실패"));

        Quiz pastQuiz = Quiz.create("과거 문제", true, "과거 해설", "돼지고기", 2L, LocalDate.now().minusDays(1));
        given(quizRepository.findRandomPastQuiz()).willReturn(Optional.of(pastQuiz));

        scheduler.generateDailyQuiz();

        verify(quizGenerationClient, times(3)).generateQuiz("상추");
        verify(quizRepository).save(any(Quiz.class));

    }

    @Test
    @DisplayName("신뢰도가 낮으면 재시도하고, 이후 정상 결과가 오면 저장한다")
    void retriesWhenConfidenceIsLow(){
        given(quizRepository.existsByQuizDate(LocalDate.now())).willReturn(false);
        given(productRepository.findAllActiveProductNameIds())
                .willReturn(List.of(new ProductNameId(1L, "상추")));

        QuizGenerationClient.QuizGenerationResult lowConfidenceResult =
                new QuizGenerationClient.QuizGenerationResult(
                        "상추는...", true, "해설", "low");

        QuizGenerationClient.QuizGenerationResult highConfidenceResult =
                new QuizGenerationClient.QuizGenerationResult(
                        "상추는 신문지로...", true, "정확한 해설", "high");

        given(quizGenerationClient.generateQuiz("상추"))
                .willReturn(lowConfidenceResult) //1차 시도: 신뢰도 낮음
                .willReturn(highConfidenceResult); //2차 시도: 신뢰도 높음, 성공

        scheduler.generateDailyQuiz();

        verify(quizGenerationClient, times(2)).generateQuiz("상추");
        verify(quizRepository).save(any(Quiz.class));
    }
}
