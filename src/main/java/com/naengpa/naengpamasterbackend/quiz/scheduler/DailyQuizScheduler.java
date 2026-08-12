package com.naengpa.naengpamasterbackend.quiz.scheduler;

import com.naengpa.naengpamasterbackend.product.repository.ProductRepository;
import com.naengpa.naengpamasterbackend.quiz.client.QuizGenerationClient;
import com.naengpa.naengpamasterbackend.quiz.dto.ProductNameId;
import com.naengpa.naengpamasterbackend.quiz.entity.Quiz;
import com.naengpa.naengpamasterbackend.quiz.repository.QuizRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.List;
import java.util.Random;

@Slf4j
@Component
@RequiredArgsConstructor
public class DailyQuizScheduler {

    private final QuizGenerationClient quizGenerationClient;
    private final QuizRepository quizRepository;
    private final ProductRepository productRepository;

    @Scheduled(cron = "0 9 10 * * *")
    public void generateDailyQuiz() {
        LocalDate today = LocalDate.now();

        if (quizRepository.existsByQuizDate(today) ) {
            log.info("오늘의 퀴즈가 이미 존재합니다. 생성을 건너뜁니다.");
            return;
        }

        log.info("퀴즈 생성 스케줄러 시작");

        ProductNameId ingredient = pickRandomIngredient();

        QuizGenerationClient.QuizGenerationResult result =
                quizGenerationClient.generateQuiz(ingredient.name());

        Quiz quiz = Quiz.create(
                result.statement(),
                result.answer(),
                result.explanation(),
                ingredient.name(),
                ingredient.id(),
                today
        );

        quizRepository.save(quiz);

        log.info("퀴즈 생성 완료 - ingredient: {}", ingredient.name());
    }

    private ProductNameId pickRandomIngredient() {
        List<ProductNameId> ingredients = productRepository.findAllActiveProductNameIds();
        if (ingredients.isEmpty()){
            throw new IllegalStateException("사용 가능한 재료가 없습니다.");
        }
        return ingredients.get(new Random().nextInt(ingredients.size()));
    }
}
