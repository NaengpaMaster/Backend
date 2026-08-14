package com.naengpa.naengpamasterbackend.quiz.client;

import com.naengpa.naengpamasterbackend.quiz.entity.Quiz;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

@Service
public class QuizGenerationClient {

    private final RestClient restClient;

    public QuizGenerationClient(
            @Value("${agent.base-url}") String baseUrl,
            @Value("${agent.api-key}") String apiKey
    ){
        this.restClient = RestClient.builder()
                .baseUrl(baseUrl)
                .defaultHeader("Authorization", "Bearer " + apiKey)
                .build();
    }

    public QuizGenerationResult generateQuiz(String ingredient) {
        return restClient.get()
                .uri("/quiz/generate?ingredient={ingredient}", ingredient)
                .retrieve()
                .body(QuizGenerationResult.class);
    }

    public record QuizGenerationResult(
            String statement,
            Boolean answer,
            String explanation,
            String confidence,
            UsageInfo usage
    ) {
        public record UsageInfo(
                String model,
                Integer promptTokens,
                Integer completionTokens,
                Integer totalTokens
        ) {}
    }
}

