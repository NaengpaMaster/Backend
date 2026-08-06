package com.naengpa.naengpamasterbackend.member.repository;

import com.naengpa.naengpamasterbackend.member.entity.MemberStatusHistory;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MemberStatusHistoryRepository extends JpaRepository<MemberStatusHistory, Long> {
}
