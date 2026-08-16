package com.naengpa.naengpamasterbackend.community.share.repository;

import com.naengpa.naengpamasterbackend.community.share.entity.CommunityShareParticipant;
import com.naengpa.naengpamasterbackend.community.share.entity.CommunitySharePost;
import com.naengpa.naengpamasterbackend.community.share.entity.CommunityShareParticipantStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

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

    @Query("""
            select post
            from CommunityShareParticipant participant
            join CommunitySharePost post on post.communitySharePostId = participant.communitySharePostId
            where participant.memberId = :memberId
              and participant.status = :status
            order by participant.updatedAt desc, participant.createdAt desc
            """)
    Page<CommunitySharePost> findJoinedPostsByMemberId(
            @Param("memberId") Long memberId,
            @Param("status") CommunityShareParticipantStatus status,
            Pageable pageable
    );

}
