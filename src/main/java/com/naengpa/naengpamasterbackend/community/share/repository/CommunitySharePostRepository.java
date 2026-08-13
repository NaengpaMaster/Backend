package com.naengpa.naengpamasterbackend.community.share.repository;

import com.naengpa.naengpamasterbackend.community.share.entity.CommunitySharePost;
import com.naengpa.naengpamasterbackend.community.share.entity.CommunitySharePostStatus;
import jakarta.persistence.LockModeType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;

import java.util.List;
import java.util.Optional;

public interface CommunitySharePostRepository extends JpaRepository<CommunitySharePost, Long> {

    List<CommunitySharePost> findTop100ByStatusOrderByCreatedAtDesc(CommunitySharePostStatus status);

    List<CommunitySharePost> findTop100ByMemberIdOrderByCreatedAtDesc(Long memberId);

    List<CommunitySharePost> findByStatusOrderByCreatedAtDesc(CommunitySharePostStatus status);

    Page<CommunitySharePost> findAllByOrderByCreatedAtDesc(Pageable pageable);

    Page<CommunitySharePost> findByStatusOrderByCreatedAtDesc(CommunitySharePostStatus status, Pageable pageable);

    Page<CommunitySharePost> findByMemberIdOrderByCreatedAtDesc(Long memberId, Pageable pageable);

    long countByStatus(CommunitySharePostStatus status);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    Optional<CommunitySharePost> findWithLockByCommunitySharePostId(Long communitySharePostId);
}
