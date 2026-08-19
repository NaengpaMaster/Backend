package com.naengpa.naengpamasterbackend.quiz.scheduler;

import com.naengpa.naengpamasterbackend.agent.usage.service.LlmUsageLogService;
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
    private final LlmUsageLogService llmUsageLogService;

    @Scheduled(cron = "0 0 0 * * *")
    public void generateDailyQuiz() {
        log.info("퀴즈 생성 스케줄러 시작 - triggeredBy: AUTO");
        generateQuizInternal(null);
    }

    public void generateDailyQuizManually(Long memberId) {
        log.info("퀴즈 생성 스케줄러 시작 - triggeredBy: MANUAL, memberId: {}", memberId);
        generateQuizInternal(memberId);
    }

    private void generateQuizInternal(Long memberId){

        LocalDate today = LocalDate.now();

        if (quizRepository.existsByQuizDate(today) ) {
            log.info("오늘의 퀴즈가 이미 존재합니다. 생성을 건너뜁니다.");
            return;
        }

        ProductNameId ingredient = pickRandomIngredient();

        QuizGenerationClient.QuizGenerationResult result = generateWithRetry(ingredient.name(), 3);

        if (result == null){
            handleGenerationFailure(today, memberId);
            return;
        }

        Quiz quiz = Quiz.create(
                result.statement(),
                result.answer(),
                result.explanation(),
                ingredient.name(),
                ingredient.id(),
                today
        );

        quizRepository.save(quiz);

        llmUsageLogService.saveQuizSuccessLog(
                memberId,
                result.usage().model(),
                result.usage().promptTokens(),
                result.usage().completionTokens(),
                result.usage().totalTokens(),
                null
        );

        log.info("퀴즈 생성 완료 - ingredient: {}", ingredient.name());
    }

    private QuizGenerationClient.QuizGenerationResult generateWithRetry(String ingredient, int maxRetry){
        for (int i = 0; i < maxRetry; i++){
            try{
                QuizGenerationClient.QuizGenerationResult result = quizGenerationClient.generateQuiz(ingredient);

                if("low".equals(result.confidence())){
                    log.warn("[품질 문제] 퀴즈 신뢰도 낮음 (시도 {}/{})", i + 1, maxRetry);
                    waitBeforeRetry(i);
                    continue;
                }
                return result;
            } catch (Exception e){
                log.warn("[통신 문제] 퀴즈 생성 API 호출 실패 (시도 {}/{} - {})", i + 1, maxRetry, e.getMessage());
                waitBeforeRetry(i);
            }
        }
        return null;
    }

    private void waitBeforeRetry(int attemptIndex){
        long waitMillis = 5000L * (attemptIndex + 1);
        log.info("재시도 전 {}ms 대기", waitMillis);
        try{
            Thread.sleep(waitMillis);
        }catch (InterruptedException e){
            Thread.currentThread().interrupt();
        }
    }

    private void handleGenerationFailure(LocalDate today, Long memberId){
        log.error("퀴즈 생성 3회 모두 실패, 과거 퀴즈로 대체 - date: {}", today);

        llmUsageLogService.saveQuizFailureLog(memberId, "agent-api", "퀴즈 생성 3회 재시도 실패");

        Quiz pastQuiz = quizRepository.findRandomPastQuiz()
                .orElseThrow(() -> new IllegalStateException("대체할 과거 퀴즈도 존재하지 않습니다."));

        Quiz duplicated = Quiz.createFrom(pastQuiz, today);
        quizRepository.save(duplicated);
    }

    private ProductNameId pickRandomIngredient() {
        List<ProductNameId> ingredients = productRepository.findAllActiveProductNameIds();
        if (ingredients.isEmpty()){
            throw new IllegalStateException("사용 가능한 재료가 없습니다.");
        }
        return ingredients.get(new Random().nextInt(ingredients.size()));
    }
}
