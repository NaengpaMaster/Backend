package com.naengpa.naengpamasterbackend.fridge.repository;

import com.naengpa.naengpamasterbackend.fridge.entity.FridgeItem;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface FridgeItemRepository extends JpaRepository<FridgeItem, Long> {

    List<FridgeItem> findByMemberIdAndIsDeletedFalse(Long memberId);

    List<FridgeItem> findByFridgeIdAndIsDeletedFalse(Long fridgeId);

    List<FridgeItem> findByFridgeIdAndMemberIdAndIsDeletedFalse(Long fridgeId, Long memberId);

    List<FridgeItem> findByMemberIdAndProductIdInAndIsDeletedFalse(Long memberId, List<Long> productIds);

    List<FridgeItem> findByFridgeIdAndProductIdInAndIsDeletedFalse(Long fridgeId, List<Long> productIds);

    Optional<FridgeItem> findByFridgeItemIdAndMemberIdAndIsDeletedFalse(Long fridgeItemId, Long memberId);

    Optional<FridgeItem> findByFridgeItemIdAndFridgeIdAndIsDeletedFalse(Long fridgeItemId, Long fridgeId);

    Optional<FridgeItem> findByFridgeItemIdAndIsDeletedFalse(Long fridgeItemId);

    List<FridgeItem> findByMemberIdAndExpiryDateBetweenAndIsDeletedFalse(
            Long memberId,
            LocalDate startDate,
            LocalDate endDate
    );

    List<FridgeItem> findByFridgeIdAndExpiryDateBetweenAndIsDeletedFalse(
            Long fridgeId,
            LocalDate startDate,
            LocalDate endDate
    );

    List<FridgeItem> findByMemberIdAndExpiryDateBeforeAndIsDeletedFalse(
            Long memberId,
            LocalDate today
    );

    List<FridgeItem> findByFridgeIdAndExpiryDateBeforeAndIsDeletedFalse(
            Long fridgeId,
            LocalDate today
    );

}
