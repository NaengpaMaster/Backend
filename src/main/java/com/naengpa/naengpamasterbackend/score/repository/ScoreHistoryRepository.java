package com.naengpa.naengpamasterbackend.score.repository;

import com.naengpa.naengpamasterbackend.score.entity.ScoreHistory;
import org.springframework.data.domain.Page;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.domain.Pageable;

public interface ScoreHistoryRepository extends JpaRepository<ScoreHistory, Long> {

    Page<ScoreHistory> findByMemberIdOrderByCreatedAtDesc(Long memberId, Pageable pageable);

}
