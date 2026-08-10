package com.naengpa.naengpamasterbackend.fridge.repository;

import com.naengpa.naengpamasterbackend.fridge.entity.FridgeMember;
import com.naengpa.naengpamasterbackend.fridge.entity.FridgeMemberRole;
import com.naengpa.naengpamasterbackend.fridge.entity.FridgeMemberStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface FridgeMemberRepository extends JpaRepository<FridgeMember, Long> {

    boolean existsByFridgeIdAndMemberIdAndStatus(Long fridgeId, Long memberId, FridgeMemberStatus status);

    Optional<FridgeMember> findByFridgeIdAndMemberIdAndStatus(Long fridgeId, Long memberId, FridgeMemberStatus status);

    Optional<FridgeMember> findByFridgeIdAndMemberId(Long fridgeId, Long memberId);

    Optional<FridgeMember> findFirstByMemberIdAndStatusOrderByFridgeMemberIdAsc(Long memberId, FridgeMemberStatus status);

    List<FridgeMember> findAllByMemberIdAndStatus(Long memberId, FridgeMemberStatus status);

    List<FridgeMember> findAllByFridgeIdAndStatus(Long fridgeId, FridgeMemberStatus status);

    boolean existsByFridgeIdAndMemberIdAndRoleAndStatus(
            Long fridgeId,
            Long memberId,
            FridgeMemberRole role,
            FridgeMemberStatus status
    );
}
