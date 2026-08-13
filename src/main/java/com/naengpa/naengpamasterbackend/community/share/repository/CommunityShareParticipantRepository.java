package com.naengpa.naengpamasterbackend.community.share.repository;

import com.naengpa.naengpamasterbackend.community.share.entity.CommunityShareParticipant;
import com.naengpa.naengpamasterbackend.community.share.entity.CommunityShareParticipantStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface CommunityShareParticipantRepository extends JpaRepository<CommunityShareParticipant, Long> {

    long countByCommunitySharePostIdAndStatus(Long communitySharePostId, CommunityShareParticipantStatus status);

    Optional<CommunityShareParticipant> findByCommunitySharePostIdAndMemberId(Long communitySharePostId, Long memberId);

    List<CommunityShareParticipant> findByCommunitySharePostIdInAndStatus(
            Collection<Long> communitySharePostIds,
            CommunityShareParticipantStatus status
    );

}
