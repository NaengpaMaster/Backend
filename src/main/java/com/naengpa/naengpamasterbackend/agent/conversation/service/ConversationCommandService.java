package com.naengpa.naengpamasterbackend.agent.conversation.service;

import com.naengpa.naengpamasterbackend.agent.conversation.entity.ConversationMessage;
import com.naengpa.naengpamasterbackend.agent.conversation.entity.ConversationMessageRole;
import com.naengpa.naengpamasterbackend.agent.conversation.entity.ConversationSession;
import com.naengpa.naengpamasterbackend.agent.conversation.repository.ConversationMessageRepository;
import com.naengpa.naengpamasterbackend.agent.conversation.repository.ConversationSessionRepository;
import com.naengpa.naengpamasterbackend.agent.shopping.dto.response.ShoppingRecommendationItemResponse;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class ConversationCommandService {

    private final ConversationSessionRepository conversationSessionRepository;
    private final ConversationMessageRepository conversationMessageRepository;

    public ConversationCommandService(
            ConversationSessionRepository conversationSessionRepository,
            ConversationMessageRepository conversationMessageRepository
    ) {
        this.conversationSessionRepository = conversationSessionRepository;
        this.conversationMessageRepository = conversationMessageRepository;
    }

    // AI 장보기 추천이 끝난 뒤, 사용자의 요청과 AI 응답을 하나의 대화 세션으로 저장
    @Transactional
    public void saveShoppingRecommendationHistory(
            Long memberId,
            List<ShoppingRecommendationItemResponse> recommendedItems
    ) {
        ConversationSession conversationSession = conversationSessionRepository.save(
                ConversationSession.create(memberId, "AI 장보기 추천")
        );

        Long conversationSessionId = conversationSession.getConversationSessionId();

        conversationMessageRepository.save(
                ConversationMessage.create(
                        conversationSessionId,
                        ConversationMessageRole.USER,
                        "냉장고와 장보기 목록을 기준으로 필요한 재료를 추천해줘"
                )
        );

        conversationMessageRepository.save(
                ConversationMessage.create(
                        conversationSessionId,
                        ConversationMessageRole.ASSISTANT,
                        createAssistantMessage(recommendedItems)
                )
        );
    }

    // 추천 결과를 사람이 읽을 수 있는 문장으로 바꿔서 ASSISTANT 메시지에 저장
    private String createAssistantMessage(List<ShoppingRecommendationItemResponse> recommendedItems) {
        if (recommendedItems.isEmpty()) {
            return "현재 추천할 장보기 재료가 없습니다.";
        }

        String productNames = recommendedItems.stream()
                .map(ShoppingRecommendationItemResponse::productName)
                .collect(Collectors.joining(", "));

        return productNames + " 재료를 추천합니다.";
    }
}
