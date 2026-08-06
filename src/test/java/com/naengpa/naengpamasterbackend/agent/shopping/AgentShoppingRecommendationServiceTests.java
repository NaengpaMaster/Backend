package com.naengpa.naengpamasterbackend.agent.shopping;

import com.naengpa.naengpamasterbackend.agent.shopping.dto.request.ShoppingRecommendationRequest;
import com.naengpa.naengpamasterbackend.agent.shopping.dto.response.ShoppingRecommendationResponse;
import com.naengpa.naengpamasterbackend.agent.shopping.service.AgentShoppingRecommendationService;
import com.naengpa.naengpamasterbackend.agent.conversation.entity.ConversationMessageRole;
import com.naengpa.naengpamasterbackend.agent.conversation.repository.ConversationMessageRepository;
import com.naengpa.naengpamasterbackend.agent.conversation.repository.ConversationSessionRepository;
import com.naengpa.naengpamasterbackend.agent.usage.entity.LlmCallStatus;
import com.naengpa.naengpamasterbackend.agent.usage.repository.LlmUsageLogRepository;
import com.naengpa.naengpamasterbackend.member.entity.HouseholdType;
import com.naengpa.naengpamasterbackend.member.entity.Member;
import com.naengpa.naengpamasterbackend.member.repository.MemberRepository;
import com.naengpa.naengpamasterbackend.product.entity.Product;
import com.naengpa.naengpamasterbackend.product.repository.ProductRepository;
import com.naengpa.naengpamasterbackend.shopping.entity.ShoppingItem;
import com.naengpa.naengpamasterbackend.shopping.repository.ShoppingItemRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;

@SpringBootTest
@Transactional
class AgentShoppingRecommendationServiceTests {

    @Autowired
    private AgentShoppingRecommendationService agentShoppingRecommendationService;

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private ShoppingItemRepository shoppingItemRepository;

    @Autowired
    private ConversationSessionRepository conversationSessionRepository;

    @Autowired
    private ConversationMessageRepository conversationMessageRepository;

    @Autowired
    private LlmUsageLogRepository llmUsageLogRepository;

    @Test
    @DisplayName("AI 장보기 추천 시 미구매 장보기 항목은 제외")
    void recommend_excludesUnpurchasedShoppingItems() {
        // given
        Member member = memberRepository.save(Member.createUser(
                "agent-recommend@test.com",
                "password",
                "추천테스트유저",
                HouseholdType.ONE_PERSON
        ));

        Product shoppingProduct = productRepository.save(Product.create(
                1L,
                "추천제외장보기재료",
                7
        ));

        shoppingItemRepository.save(ShoppingItem.create(
                member.getId(),
                shoppingProduct.getProductId(),
                "1개"
        ));

        ShoppingRecommendationRequest request = new ShoppingRecommendationRequest(20);

        // when
        ShoppingRecommendationResponse result =
                agentShoppingRecommendationService.recommend(member.getEmail(), request);

        // then
        assertThat(result.items())
                .extracting(item -> item.productId())
                .doesNotContain(shoppingProduct.getProductId());

        assertThat(result.items()).hasSizeLessThanOrEqualTo(20);
    }

    @Test
    @DisplayName("AI 장보기 추천 결과는 요청 limit 개수 이하로 반환")
    void recommend_returnsItemsWithinLimit() {
        // given
        Member member = memberRepository.save(Member.createUser(
                "agent-limit@test.com",
                "password",
                "추천개수테스트유저",
                HouseholdType.ONE_PERSON
        ));

        ShoppingRecommendationRequest request = new ShoppingRecommendationRequest(1);

        // when
        ShoppingRecommendationResponse result =
                agentShoppingRecommendationService.recommend(member.getEmail(), request);

        // then
        assertThat(result.items()).hasSizeLessThanOrEqualTo(1);
    }

    @Test
    @DisplayName("AI 장보기 추천 요청 limit이 없으면 기본값으로 추천")
    void recommend_usesDefaultLimitWhenRequestLimitIsNull() {
        // given
        Member member = memberRepository.save(Member.createUser(
                "agent-default-limit@test.com",
                "password",
                "추천기본값테스트유저",
                HouseholdType.ONE_PERSON
        ));

        ShoppingRecommendationRequest request = new ShoppingRecommendationRequest(null);

        // when
        ShoppingRecommendationResponse result =
                agentShoppingRecommendationService.recommend(member.getEmail(), request);

        // then
        assertThat(result.items()).hasSizeLessThanOrEqualTo(5);
    }

    @Test
    @DisplayName("존재하지 않는 회원이면 AI 장보기 추천 실패")
    void recommend_throwsExceptionWhenMemberNotFound() {
        // given
        String email = "not-found-agent@test.com";
        ShoppingRecommendationRequest request = new ShoppingRecommendationRequest(5);

        // when & then
        assertThatThrownBy(() -> agentShoppingRecommendationService.recommend(email, request))
                .isInstanceOf(BadCredentialsException.class);
    }

    @Test
    @DisplayName("AI 장보기 추천 요청 시 대화 세션과 USER/ASSISTANT 메시지가 저장된다")
    void recommend_savesConversationSessionAndMessages() {
        // given
        Member member = memberRepository.save(Member.createUser(
                "agent-history@test.com",
                "password",
                "추천기록테스트유저",
                HouseholdType.ONE_PERSON
        ));

        ShoppingRecommendationRequest request = new ShoppingRecommendationRequest(3);

        // when
        agentShoppingRecommendationService.recommend(member.getEmail(), request);

        // then
        var sessions = conversationSessionRepository
                .findByMemberIdAndIsDeletedFalseOrderByCreatedAtDesc(member.getId());

        assertThat(sessions).hasSize(1);
        assertThat(sessions.get(0).getTitle()).isEqualTo("AI 장보기 추천");

        var messages = conversationMessageRepository
                .findByConversationSessionIdOrderByCreatedAtAsc(
                        sessions.get(0).getConversationSessionId()
                );

        assertThat(messages).hasSize(2);
        assertThat(messages)
                .extracting(message -> message.getRole())
                .containsExactly(ConversationMessageRole.USER, ConversationMessageRole.ASSISTANT);
    }

    @Test
    @DisplayName("AI 장보기 추천 요청 성공 시 LLM 사용량 성공 로그가 저장된다")
    void recommend_savesLlmSuccessUsageLog() {
        // given
        Member member = memberRepository.save(Member.createUser(
                "agent-usage@test.com",
                "password",
                "추천사용량테스트유저",
                HouseholdType.ONE_PERSON
        ));

        ShoppingRecommendationRequest request = new ShoppingRecommendationRequest(3);

        // when
        agentShoppingRecommendationService.recommend(member.getEmail(), request);

        // then
        var logs = llmUsageLogRepository.findByMemberIdOrderByCreatedAtDesc(member.getId());

        assertThat(logs).hasSize(1);
        assertThat(logs.get(0).getModelName()).isEqualTo("rule-based-mvp");
        assertThat(logs.get(0).getStatus()).isEqualTo(LlmCallStatus.SUCCESS);
        assertThat(logs.get(0).getTotalTokens()).isZero();
    }
}
