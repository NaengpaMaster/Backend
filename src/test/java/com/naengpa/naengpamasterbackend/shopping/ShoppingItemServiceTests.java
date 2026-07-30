package com.naengpa.naengpamasterbackend.shopping;

import com.naengpa.naengpamasterbackend.fridge.dto.response.FridgeItemResponse;
import com.naengpa.naengpamasterbackend.global.exception.DuplicateShoppingItemException;
import com.naengpa.naengpamasterbackend.member.entity.HouseholdType;
import com.naengpa.naengpamasterbackend.member.entity.Member;
import com.naengpa.naengpamasterbackend.member.repository.MemberRepository;
import com.naengpa.naengpamasterbackend.product.repository.ProductRepository
import com.naengpa.naengpamasterbackend.shopping.dto.request.ShoppingItemCheckRequest;
import com.naengpa.naengpamasterbackend.shopping.dto.request.ShoppingItemCreateRequest;
import com.naengpa.naengpamasterbackend.shopping.dto.request.ShoppingItemMoveToFridgeRequest;
import com.naengpa.naengpamasterbackend.shopping.dto.request.ShoppingItemUpdateRequest;
import com.naengpa.naengpamasterbackend.shopping.dto.response.ShoppingItemListResponse;
import com.naengpa.naengpamasterbackend.shopping.dto.response.ShoppingItemResponse;
import com.naengpa.naengpamasterbackend.shopping.service.ShoppingItemService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;

@SpringBootTest
@Transactional
class ShoppingItemServiceTests {

    @Autowired
    private ShoppingItemService shoppingItemService;
    @Autowired
    private MemberRepository memberRepository;

    @BeforeEach
    void setUp() {
        if (!memberRepository.existsByEmail("test-user@example.com")) {
            memberRepository.save(Member.createUser(
                    "test-user@example.com",
                    "password",
                    "테스트유저",
                    HouseholdType.ONE_PERSON
            ));
        }
    }

    @Test
    @DisplayName("장보기 항목 추가 시 ShoppingItemResponse를 반환")
    void createShoppingItem_returnsShoppingItemResponse() {
        // given
        String email = "test-user@example.com";
        ShoppingItemCreateRequest request = new ShoppingItemCreateRequest(
                1L,
                "1개"
        );

        // when
        ShoppingItemResponse result = shoppingItemService.createShoppingItem(email, request);

        // then
        assertThat(result.shoppingItemId()).isNotNull();
        assertThat(result.memberId()).isNotNull();
        assertThat(result.productId()).isEqualTo(1L);
        assertThat(result.quantity()).isEqualTo("1개");
    }

    @Test
    @DisplayName("장보기 항목 추가 시 구매 여부는 false로 저장")
    void createShoppingItem_setsIsPurchasedFalse() {
        // given
        String email = "test-user@example.com";
        ShoppingItemCreateRequest request = new ShoppingItemCreateRequest(
                1L,
                "1개"
        );

        // when
        ShoppingItemResponse result = shoppingItemService.createShoppingItem(email, request);

        // then
        assertThat(result.isPurchased()).isFalse();
    }

    @Test
    @DisplayName("장보기 목록 조회 시 내가 등록한 장보기 항목을 반환")
    void findShoppingItems_returnsMyShoppingItems() {
        // given
        String email = "test-user@example.com";

        ShoppingItemCreateRequest request = new ShoppingItemCreateRequest(
                1L,
                "1개"
        );

        ShoppingItemResponse created = shoppingItemService.createShoppingItem(email, request);

        // when
        List<ShoppingItemListResponse> result = shoppingItemService.findShoppingItems(email);

        // then
        assertThat(result)
                .extracting(ShoppingItemListResponse::shoppingItemId)
                .contains(created.shoppingItemId());
    }

    @Test
    @DisplayName("장보기 항목 삭제 시 목록 조회에서 제외")
    void deleteShoppingItem_excludesDeletedItemFromList() {
        // given
        String email = "test-user@example.com";
        ShoppingItemCreateRequest request = new ShoppingItemCreateRequest(
                1L,
                "1개"
        );

        ShoppingItemResponse created = shoppingItemService.createShoppingItem(email, request);

        // when
        shoppingItemService.deleteShoppingItem(email, created.shoppingItemId());

        // then
        List<ShoppingItemListResponse> result = shoppingItemService.findShoppingItems(email);

        assertThat(result)
                .extracting(ShoppingItemListResponse::shoppingItemId)
                .doesNotContain(created.shoppingItemId());
    }

    @Test
    @DisplayName("장보기 항목 체크 시 구매 여부가 true로 변경")
    void updateShoppingItemPurchased_setsIsPurchasedTrue() {
        // given
        String email = "test-user@example.com";
        ShoppingItemCreateRequest createRequest = new ShoppingItemCreateRequest(
                1L,
                "1개"
        );

        ShoppingItemResponse created = shoppingItemService.createShoppingItem(email, createRequest);
        ShoppingItemCheckRequest request = new ShoppingItemCheckRequest(true);

        // when
        ShoppingItemResponse result = shoppingItemService.updateShoppingItemPurchased(
                email,
                created.shoppingItemId(),
                request
        );

        // then
        assertThat(result.isPurchased()).isTrue();
    }

    @Test
    @DisplayName("장보기 항목 수정 시 수량이 변경")
    void updateShoppingItem_changesQuantity() {
        // given
        String email = "test-user@example.com";
        ShoppingItemCreateRequest createRequest = new ShoppingItemCreateRequest(
                1L,
                "1개"
        );

        ShoppingItemResponse created = shoppingItemService.createShoppingItem(email, createRequest);
        ShoppingItemUpdateRequest request = new ShoppingItemUpdateRequest("2개");

        // when
        ShoppingItemResponse result = shoppingItemService.updateShoppingItem(
                email,
                created.shoppingItemId(),
                request
        );

        // then
        assertThat(result.shoppingItemId()).isEqualTo(created.shoppingItemId());
        assertThat(result.productId()).isEqualTo(created.productId());
        assertThat(result.quantity()).isEqualTo("2개");
    }

    @Test
    @DisplayName("장보기 항목 냉장고 반영 시 냉장고 재료를 생성하고 장보기 목록에서 제외")
    void moveShoppingItemToFridge_createsFridgeItemAndRemovesShoppingItem() {
        // given
        String email = "test-user@example.com";

        ShoppingItemCreateRequest createRequest = new ShoppingItemCreateRequest(
                1L,
                "1개"
        );

        ShoppingItemResponse created = shoppingItemService.createShoppingItem(email, createRequest);

        ShoppingItemMoveToFridgeRequest request = new ShoppingItemMoveToFridgeRequest(
                LocalDate.now().plusDays(7),
                "장보기에서 냉장고로 추가"
        );

        // when
        FridgeItemResponse result = shoppingItemService.moveShoppingItemToFridge(
                email,
                created.shoppingItemId(),
                request
        );

        // then
        assertThat(result.fridgeItemId()).isNotNull();
        assertThat(result.productId()).isEqualTo(created.productId());
        assertThat(result.quantity()).isEqualTo(created.quantity());
        assertThat(result.expiryDate()).isEqualTo(request.expiryDate());
        assertThat(result.memo()).isEqualTo(request.memo());

        List<ShoppingItemListResponse> shoppingItems = shoppingItemService.findShoppingItems(email);

        assertThat(shoppingItems)
                .extracting(ShoppingItemListResponse::shoppingItemId)
                .doesNotContain(created.shoppingItemId());
    }

    @Test
    @DisplayName("장보기 항목 냉장고 반영 시 유통기한 요청값이 없으면 상품 기본 유통기한을 적용")
    void moveShoppingItemToFridge_usesDefaultExpiryDaysWhenExpiryDateIsNull() {
        // given
        String email = "test-user@example.com";

        ShoppingItemCreateRequest createRequest = new ShoppingItemCreateRequest(
                1L,
                "1개"
        );

        ShoppingItemResponse created = shoppingItemService.createShoppingItem(email, createRequest);

        ShoppingItemMoveToFridgeRequest request = new ShoppingItemMoveToFridgeRequest(
                null,
                "장보기에서 냉장고로 추가"
        );

        // when
        FridgeItemResponse result = shoppingItemService.moveShoppingItemToFridge(
                email,
                created.shoppingItemId(),
                request
        );

        // then
        assertThat(result.expiryDate()).isEqualTo(LocalDate.now().plusDays(30));
        assertThat(result.memo()).isEqualTo(request.memo());
    }

    @Test
    @DisplayName("같은 사용자, 같은 상품, 미삭제, 미구매 장보기 항목은 중복 등록할 수 없다")
    void createShoppingItem_throwsExceptionWhenActiveUnpurchasedItemAlreadyExists() {
        // given
        String email = "test-user@example.com";
        ShoppingItemCreateRequest request = new ShoppingItemCreateRequest(
                1L,
                "1개"
        );

        shoppingItemService.createShoppingItem(email, request);

        // when & then
        assertThatThrownBy(() -> shoppingItemService.createShoppingItem(email, request))
                .isInstanceOf(DuplicateShoppingItemException.class)
                .hasMessage("이미 장보기 목록에 등록된 재료입니다.");
    }

    @Test
    @DisplayName("삭제된 같은 상품은 다시 장보기 항목으로 등록할 수 있다")
    void createShoppingItem_allowsRecreateWhenSameProductWasDeleted() {
        // given
        String email = "test-user@example.com";
        ShoppingItemCreateRequest request = new ShoppingItemCreateRequest(
                1L,
                "1개"
        );

        ShoppingItemResponse created = shoppingItemService.createShoppingItem(email, request);
        shoppingItemService.deleteShoppingItem(email, created.shoppingItemId());

        // when
        ShoppingItemResponse result = shoppingItemService.createShoppingItem(email, request);

        // then
        assertThat(result.shoppingItemId()).isNotEqualTo(created.shoppingItemId());
        assertThat(result.productId()).isEqualTo(1L);
        assertThat(result.quantity()).isEqualTo("1개");
    }

    @Test
    @DisplayName("구매 완료된 같은 상품은 다시 장보기 항목으로 등록할 수 있다")
    void createShoppingItem_allowsRecreateWhenSameProductWasPurchased() {
        // given
        String email = "test-user@example.com";
        ShoppingItemCreateRequest request = new ShoppingItemCreateRequest(
                1L,
                "1개"
        );

        ShoppingItemResponse created = shoppingItemService.createShoppingItem(email, request);
        shoppingItemService.updateShoppingItemPurchased(
                email,
                created.shoppingItemId(),
                new ShoppingItemCheckRequest(true)
        );

        // when
        ShoppingItemResponse result = shoppingItemService.createShoppingItem(email, request);

        // then
        assertThat(result.shoppingItemId()).isNotEqualTo(created.shoppingItemId());
        assertThat(result.productId()).isEqualTo(1L);
        assertThat(result.isPurchased()).isFalse();
    }

    @Test
    @DisplayName("다른 사용자의 같은 상품은 중복 장보기 항목으로 보지 않는다")
    void createShoppingItem_allowsSameProductForDifferentMember() {
        // given
        String firstEmail = "test-user@example.com";
        String secondEmail = "second-user@example.com";

        if (!memberRepository.existsByEmail(secondEmail)) {
            memberRepository.save(Member.createUser(
                    secondEmail,
                    "password",
                    "두번째유저",
                    HouseholdType.ONE_PERSON
            ));
        }

        ShoppingItemCreateRequest request = new ShoppingItemCreateRequest(
                1L,
                "1개"
        );

        ShoppingItemResponse firstResult = shoppingItemService.createShoppingItem(firstEmail, request);

        // when
        ShoppingItemResponse secondResult = shoppingItemService.createShoppingItem(secondEmail, request);

        // then
        assertThat(secondResult.shoppingItemId()).isNotEqualTo(firstResult.shoppingItemId());
        assertThat(secondResult.productId()).isEqualTo(1L);
    }
}
