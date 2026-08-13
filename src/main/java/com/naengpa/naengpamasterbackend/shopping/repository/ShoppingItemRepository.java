package com.naengpa.naengpamasterbackend.shopping.repository;

import com.naengpa.naengpamasterbackend.shopping.entity.ShoppingItem;
import jakarta.validation.Valid;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ShoppingItemRepository extends JpaRepository<ShoppingItem, Long> {


    List<ShoppingItem> findByMemberIdAndIsDeletedFalse(Long memberId);

    List<ShoppingItem> findByFridgeIdAndIsDeletedFalse(Long fridgeId);

    List<ShoppingItem> findByFridgeIdAndMemberIdAndIsDeletedFalse(Long fridgeId, Long memberId);

    Optional<ShoppingItem> findByShoppingItemIdAndMemberIdAndIsDeletedFalse(
            Long shoppingItemId,
            Long memberId
    );

    Optional<ShoppingItem> findByShoppingItemIdAndFridgeIdAndIsDeletedFalse(
            Long shoppingItemId,
            Long fridgeId
    );

    boolean existsByMemberIdAndProductIdAndIsDeletedFalseAndIsPurchasedFalse(
            Long memberId,
            Long productId
    );

    boolean existsByFridgeIdAndProductIdAndIsDeletedFalseAndIsPurchasedFalse(
            Long fridgeId,
            Long productId
    );

}
