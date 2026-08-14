package com.naengpa.naengpamasterbackend.admin.service;

import com.naengpa.naengpamasterbackend.admin.dto.response.AdminCommunitySharePostResponse;
import com.naengpa.naengpamasterbackend.admin.dto.response.AdminCommunityShareSummaryResponse;
import com.naengpa.naengpamasterbackend.community.share.entity.CommunityShareParticipantStatus;
import com.naengpa.naengpamasterbackend.community.share.entity.CommunitySharePost;
import com.naengpa.naengpamasterbackend.community.share.entity.CommunitySharePostStatus;
import com.naengpa.naengpamasterbackend.community.share.repository.CommunityShareParticipantRepository;
import com.naengpa.naengpamasterbackend.community.share.repository.CommunitySharePostRepository;
import com.naengpa.naengpamasterbackend.global.response.PageResponse;
import com.naengpa.naengpamasterbackend.member.entity.Member;
import com.naengpa.naengpamasterbackend.member.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AdminCommunityShareService {

    private final CommunitySharePostRepository communitySharePostRepository;
    private final CommunityShareParticipantRepository communityShareParticipantRepository;
    private final MemberRepository memberRepository;

    @Transactional(readOnly = true)
    public AdminCommunityShareSummaryResponse getSummary() {
        return new AdminCommunityShareSummaryResponse(
                communitySharePostRepository.count(),
                communitySharePostRepository.countByStatus(CommunitySharePostStatus.OPEN),
                communitySharePostRepository.countByStatus(CommunitySharePostStatus.CLOSED),
                communitySharePostRepository.countByStatus(CommunitySharePostStatus.CANCELLED),
                communityShareParticipantRepository.count()
        );
    }

    @Transactional(readOnly = true)
    public PageResponse<AdminCommunitySharePostResponse> getPosts(
            CommunitySharePostStatus status,
            Pageable pageable
    ) {
        Page<CommunitySharePost> page = status == null
                ? communitySharePostRepository.findAllByOrderByCreatedAtDesc(pageable)
                : communitySharePostRepository.findByStatusOrderByCreatedAtDesc(status, pageable);

        return PageResponse.of(
                toResponses(page.getContent()),
                page.getNumber(),
                page.getSize(),
                page.getTotalElements()
        );
    }

    @Transactional
    public AdminCommunitySharePostResponse cancelPost(Long communitySharePostId) {
        CommunitySharePost post = communitySharePostRepository.findById(communitySharePostId)
                .orElseThrow(() -> new IllegalArgumentException("나눔 글을 찾을 수 없습니다."));

        post.cancel();
        return toResponse(post, findMemberMap(List.of(post)).get(post.getMemberId()));
    }

    private List<AdminCommunitySharePostResponse> toResponses(List<CommunitySharePost> posts) {
        if (posts.isEmpty()) {
            return List.of();
        }

        Map<Long, Member> members = findMemberMap(posts);
        return posts.stream()
                .map(post -> toResponse(post, members.get(post.getMemberId())))
                .toList();
    }

    private Map<Long, Member> findMemberMap(List<CommunitySharePost> posts) {
        List<Long> memberIds = posts.stream()
                .map(CommunitySharePost::getMemberId)
                .distinct()
                .toList();
        return memberRepository.findByIdIn(memberIds).stream()
                .collect(Collectors.toMap(Member::getId, Function.identity()));
    }

    private AdminCommunitySharePostResponse toResponse(CommunitySharePost post, Member owner) {
        int joinedCount = (int) communityShareParticipantRepository.countByCommunitySharePostIdAndStatus(
                post.getCommunitySharePostId(),
                CommunityShareParticipantStatus.JOINED
        ) + 1;

        return AdminCommunitySharePostResponse.from(
                post,
                owner == null ? "알 수 없음" : owner.getNickname(),
                owner == null ? null : owner.getEmail(),
                joinedCount
        );
    }
}
