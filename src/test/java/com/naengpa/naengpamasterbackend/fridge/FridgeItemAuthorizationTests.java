package com.naengpa.naengpamasterbackend.fridge;

import com.naengpa.naengpamasterbackend.fridge.dto.request.FridgeItemUpdateRequest;
import com.naengpa.naengpamasterbackend.fridge.entity.FridgeItem;
import com.naengpa.naengpamasterbackend.fridge.repository.FridgeItemRepository;
import com.naengpa.naengpamasterbackend.fridge.service.FridgeItemService;
import com.naengpa.naengpamasterbackend.fridge.service.FridgeService;
import com.naengpa.naengpamasterbackend.member.entity.Member;
import com.naengpa.naengpamasterbackend.member.repository.MemberRepository;
import com.naengpa.naengpamasterbackend.product.service.ProductService;
import org.junit.jupiter.api.Test;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDate;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class FridgeItemAuthorizationTests {

    private final FridgeItemRepository fridgeItemRepository = mock(FridgeItemRepository.class);
    private final MemberRepository memberRepository = mock(MemberRepository.class);
    private final ProductService productService = mock(ProductService.class);
    private final FridgeItemService fridgeItemService = new FridgeItemService(
            fridgeItemRepository,
            memberRepository,
            productService,
            mock(com.naengpa.naengpamasterbackend.product.repository.ProductRepository.class),
            mock(FridgeService.class)
    );

    @Test
    void updateOtherMembersFridgeItemIsDenied() {
        Member member = Member.createUser("owner@example.com", "encoded", "owner", null);
        ReflectionTestUtils.setField(member, "id", 1L);
        FridgeItemUpdateRequest request = new FridgeItemUpdateRequest(
                1L,
                "2개",
                LocalDate.now().plusDays(3),
                "변조 요청"
        );

        when(memberRepository.findByEmail("owner@example.com")).thenReturn(Optional.of(member));
        when(fridgeItemRepository.findByFridgeItemIdAndMemberIdAndIsDeletedFalse(99L, 1L))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> fridgeItemService.updateFridgeItem("owner@example.com", 99L, request))
                .isInstanceOf(AccessDeniedException.class)
                .hasMessage("본인 냉장고 재료만 접근할 수 있습니다.");

        verify(productService, never()).validateExists(1L);
    }

    @Test
    void deleteOtherMembersFridgeItemIsDenied() {
        Member member = Member.createUser("owner@example.com", "encoded", "owner", null);
        ReflectionTestUtils.setField(member, "id", 1L);

        when(memberRepository.findByEmail("owner@example.com")).thenReturn(Optional.of(member));
        when(fridgeItemRepository.findByFridgeItemIdAndMemberIdAndIsDeletedFalse(99L, 1L))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> fridgeItemService.deleteFridgeItem("owner@example.com", 99L))
                .isInstanceOf(AccessDeniedException.class)
                .hasMessage("본인 냉장고 재료만 접근할 수 있습니다.");
    }

    @Test
    void updateOwnFridgeItemSucceeds() {
        Member member = Member.createUser("owner@example.com", "encoded", "owner", null);
        ReflectionTestUtils.setField(member, "id", 1L);
        FridgeItem fridgeItem = FridgeItem.create(
                1L,
                1L,
                1L,
                "1개",
                LocalDate.now().plusDays(1),
                "기존"
        );
        ReflectionTestUtils.setField(fridgeItem, "fridgeItemId", 10L);
        FridgeItemUpdateRequest request = new FridgeItemUpdateRequest(
                2L,
                "3개",
                LocalDate.now().plusDays(5),
                "수정"
        );

        when(memberRepository.findByEmail("owner@example.com")).thenReturn(Optional.of(member));
        when(fridgeItemRepository.findByFridgeItemIdAndMemberIdAndIsDeletedFalse(10L, 1L))
                .thenReturn(Optional.of(fridgeItem));

        fridgeItemService.updateFridgeItem("owner@example.com", 10L, request);

        verify(productService).validateExists(2L);
    }
}
