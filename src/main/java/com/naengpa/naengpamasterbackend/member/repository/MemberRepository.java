package com.naengpa.naengpamasterbackend.member.repository;

import com.naengpa.naengpamasterbackend.member.entity.Member;
import com.naengpa.naengpamasterbackend.member.entity.MemberStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface MemberRepository extends JpaRepository<Member, Long> {

    Optional<Member> findByEmail(String email);

    boolean existsByEmail(String email);

    boolean existsByNickname(String nickname);

    List<Member> findAllByStatusAndDeletedAtIsNull(MemberStatus status);

    List<Member> findByIdIn(List<Long> memberIds);
}
