package com.naengpa.naengpamasterbackend.fridge.repository;

import com.naengpa.naengpamasterbackend.fridge.entity.FridgeItemShareRequest;
import com.naengpa.naengpamasterbackend.fridge.entity.FridgeItemShareRequestStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface FridgeItemShareRequestRepository extends JpaRepository<FridgeItemShareRequest, Long> {

    List<FridgeItemShareRequest> findAllByRequestedMemberIdAndStatusOrderByRequestedAtDesc(
            Long requestedMemberId,
            FridgeItemShareRequestStatus status
    );

    Optional<FridgeItemShareRequest> findByFridgeItemShareRequestIdAndRequestedMemberIdAndStatus(
            Long fridgeItemShareRequestId,
            Long requestedMemberId,
            FridgeItemShareRequestStatus status
    );
}
