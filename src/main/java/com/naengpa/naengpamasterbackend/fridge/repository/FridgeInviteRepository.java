package com.naengpa.naengpamasterbackend.fridge.repository;

import com.naengpa.naengpamasterbackend.fridge.entity.FridgeInvite;
import com.naengpa.naengpamasterbackend.fridge.entity.FridgeInviteStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface FridgeInviteRepository extends JpaRepository<FridgeInvite, Long> {

    boolean existsByFridgeIdAndInviteeMemberIdAndStatus(Long fridgeId, Long inviteeMemberId, FridgeInviteStatus status);

    List<FridgeInvite> findAllByFridgeIdAndStatus(Long fridgeId, FridgeInviteStatus status);

    List<FridgeInvite> findAllByInviteeMemberIdAndStatus(Long inviteeMemberId, FridgeInviteStatus status);

    Optional<FridgeInvite> findByFridgeInviteIdAndInviteeMemberIdAndStatus(
            Long fridgeInviteId,
            Long inviteeMemberId,
            FridgeInviteStatus status
    );
}
