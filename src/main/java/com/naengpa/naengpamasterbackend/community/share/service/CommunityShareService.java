package com.naengpa.naengpamasterbackend.community.share.service;

import com.naengpa.naengpamasterbackend.community.share.dto.request.CommunitySharePostCreateRequest;
import com.naengpa.naengpamasterbackend.community.share.dto.request.CommunitySharePostSearchRequest;
import com.naengpa.naengpamasterbackend.community.share.dto.response.CommunitySharePostResponse;
import com.naengpa.naengpamasterbackend.community.share.entity.CommunityShareParticipant;
import com.naengpa.naengpamasterbackend.community.share.entity.CommunityShareParticipantStatus;
import com.naengpa.naengpamasterbackend.community.share.entity.CommunitySharePost;
import com.naengpa.naengpamasterbackend.community.share.entity.CommunitySharePostStatus;
import com.naengpa.naengpamasterbackend.community.share.repository.CommunityShareParticipantRepository;
import com.naengpa.naengpamasterbackend.community.share.repository.CommunitySharePostRepository;
import com.naengpa.naengpamasterbackend.global.exception.MemberNotFoundException;
import com.naengpa.naengpamasterbackend.global.response.PageResponse;
import com.naengpa.naengpamasterbackend.member.entity.Member;
import com.naengpa.naengpamasterbackend.member.repository.MemberRepository;
import com.naengpa.naengpamasterbackend.product.entity.Product;
import com.naengpa.naengpamasterbackend.product.exception.ProductNotFoundException;
import com.naengpa.naengpamasterbackend.product.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CommunityShareService {

    private static final BigDecimal DEFAULT_RADIUS_KM = BigDecimal.valueOf(0.5);
    private static final int PAGE_SIZE = 10;
    private final CommunitySharePostRepository communitySharePostRepository;
    private final CommunityShareParticipantRepository communityShareParticipantRepository;
    private final MemberRepository memberRepository;
    private final ProductRepository productRepository;

    @Transactional
    public CommunitySharePostResponse createPost(String email, CommunitySharePostCreateRequest request) {
        Member member = findMember(email);
        Product product = productRepository.findById(request.productId())
                .filter(Product::getIsActive)
                .orElseThrow(() -> new ProductNotFoundException(request.productId()));

        CommunitySharePost post = CommunitySharePost.create(
                member.getId(),
                request.title(),
                product.getName(),
                product.getProductId(),
                request.quantity(),
                request.totalPrice(),
                request.participantLimit(),
                request.latitude(),
                request.longitude(),
                request.address(),
                request.description()
        );

        CommunitySharePost savedPost = communitySharePostRepository.save(post);
        return toResponse(savedPost, member, member.getId(), 1, true, null);
    }

    @Transactional(readOnly = true)
    public PageResponse<CommunitySharePostResponse> findOpenPosts(String email, CommunitySharePostSearchRequest request) {
        Member member = findMember(email);
        List<CommunitySharePost> posts = communitySharePostRepository
                .findByStatusOrderByCreatedAtDesc(CommunitySharePostStatus.OPEN);

        BigDecimal latitude = request.latitude();
        BigDecimal longitude = request.longitude();
        double radiusKm = request.radiusKm() == null ? DEFAULT_RADIUS_KM.doubleValue() : request.radiusKm().doubleValue();

        List<PostWithDistance> filteredPosts = posts.stream()
                .map(post -> new PostWithDistance(post, calculateDistanceKm(latitude, longitude, post)))
                .filter(item -> item.distanceKm() == null || item.distanceKm() <= radiusKm)
                .toList();

        int page = request.page() == null ? 0 : request.page();
        List<PostWithDistance> pagePosts = slice(filteredPosts, page);
        return PageResponse.of(toResponses(pagePosts, member.getId()), page, PAGE_SIZE, filteredPosts.size());
    }

    @Transactional(readOnly = true)
    public PageResponse<CommunitySharePostResponse> findMyPosts(String email, int page) {
        Member member = findMember(email);
        Page<CommunitySharePost> postPage = communitySharePostRepository.findByMemberIdOrderByCreatedAtDesc(
                member.getId(),
                PageRequest.of(Math.max(page, 0), PAGE_SIZE)
        );
        List<PostWithDistance> posts = postPage.getContent()
                .stream()
                .map(post -> new PostWithDistance(post, null))
                .toList();

        return PageResponse.of(toResponses(posts, member.getId()), postPage.getNumber(), PAGE_SIZE, postPage.getTotalElements());
    }

    @Transactional(readOnly = true)
    public PageResponse<CommunitySharePostResponse> findMyJoinedPosts(String email, int page) {
        Member member = findMember(email);
        Page<CommunitySharePost> postPage = communityShareParticipantRepository.findJoinedPostsByMemberId(
                member.getId(),
                CommunityShareParticipantStatus.JOINED,
                PageRequest.of(Math.max(page, 0), PAGE_SIZE)
        );
        List<PostWithDistance> posts = postPage.getContent()
                .stream()
                .map(post -> new PostWithDistance(post, null))
                .toList();

        return PageResponse.of(toResponses(posts, member.getId()), postPage.getNumber(), PAGE_SIZE, postPage.getTotalElements());
    }

    private List<PostWithDistance> slice(List<PostWithDistance> posts, int page) {
        int safePage = Math.max(page, 0);
        int fromIndex = safePage * PAGE_SIZE;
        if (fromIndex >= posts.size()) {
            return List.of();
        }
        int toIndex = Math.min(fromIndex + PAGE_SIZE, posts.size());
        return posts.subList(fromIndex, toIndex);
    }

    @Transactional
    public CommunitySharePostResponse joinPost(String email, Long communitySharePostId) {
        Member member = findMember(email);
        CommunitySharePost post = communitySharePostRepository.findWithLockByCommunitySharePostId(communitySharePostId)
                .orElseThrow(() -> new IllegalArgumentException("나눔 글을 찾을 수 없습니다."));

        if (!post.isOpen()) {
            throw new IllegalStateException("이미 마감된 나눔입니다.");
        }
        if (post.isOwnedBy(member.getId())) {
            throw new IllegalArgumentException("내가 올린 나눔에는 참여할 수 없습니다.");
        }

        long participantCount = communityShareParticipantRepository.countByCommunitySharePostIdAndStatus(
                communitySharePostId,
                CommunityShareParticipantStatus.JOINED
        );
        if (participantCount + 1 >= post.getParticipantLimit()) {
            post.close();
            throw new IllegalStateException("모집 인원이 마감되었습니다.");
        }

        CommunityShareParticipant participant = communityShareParticipantRepository
                .findByCommunitySharePostIdAndMemberId(communitySharePostId, member.getId())
                .orElse(null);

        if (participant == null) {
            participant = communityShareParticipantRepository.save(CommunityShareParticipant.join(communitySharePostId, member.getId()));
        } else if (participant.isJoined()) {
            throw new IllegalStateException("이미 참여한 나눔입니다.");
        } else {
            participant.rejoin();
        }

        int nextJoinedCount = (int) communityShareParticipantRepository.countByCommunitySharePostIdAndStatus(
                communitySharePostId,
                CommunityShareParticipantStatus.JOINED
        ) + 1;
        if (nextJoinedCount >= post.getParticipantLimit()) {
            post.close();
        }

        Member owner = findMemberById(post.getMemberId());
        return toResponse(post, owner, member.getId(), nextJoinedCount, true, null);
    }

    @Transactional
    public CommunitySharePostResponse cancelJoin(String email, Long communitySharePostId) {
        Member member = findMember(email);
        CommunitySharePost post = communitySharePostRepository.findById(communitySharePostId)
                .orElseThrow(() -> new IllegalArgumentException("나눔 글을 찾을 수 없습니다."));
        CommunityShareParticipant participant = communityShareParticipantRepository
                .findByCommunitySharePostIdAndMemberId(communitySharePostId, member.getId())
                .orElseThrow(() -> new IllegalArgumentException("참여한 나눔이 아닙니다."));

        participant.cancel();
        int joinedCount = (int) communityShareParticipantRepository.countByCommunitySharePostIdAndStatus(
                communitySharePostId,
                CommunityShareParticipantStatus.JOINED
        ) + 1;
        Member owner = findMemberById(post.getMemberId());
        return toResponse(post, owner, member.getId(), joinedCount, false, null);
    }

    @Transactional
    public CommunitySharePostResponse closePost(String email, Long communitySharePostId) {
        return changePostStatus(email, communitySharePostId, true);
    }

    @Transactional
    public CommunitySharePostResponse cancelPost(String email, Long communitySharePostId) {
        return changePostStatus(email, communitySharePostId, false);
    }

    private CommunitySharePostResponse changePostStatus(String email, Long communitySharePostId, boolean close) {
        Member member = findMember(email);
        CommunitySharePost post = communitySharePostRepository.findById(communitySharePostId)
                .orElseThrow(() -> new IllegalArgumentException("나눔 글을 찾을 수 없습니다."));

        if (!post.isOwnedBy(member.getId())) {
            throw new AccessDeniedException("내가 올린 나눔만 변경할 수 있습니다.");
        }

        if (close) {
            post.close();
        } else {
            post.cancel();
        }

        int joinedCount = (int) communityShareParticipantRepository.countByCommunitySharePostIdAndStatus(
                communitySharePostId,
                CommunityShareParticipantStatus.JOINED
        ) + 1;
        return toResponse(post, member, member.getId(), joinedCount, true, null);
    }

    private List<CommunitySharePostResponse> toResponses(List<PostWithDistance> posts, Long currentMemberId) {
        if (posts.isEmpty()) {
            return List.of();
        }

        List<Long> memberIds = posts.stream()
                .map(item -> item.post().getMemberId())
                .distinct()
                .toList();
        Map<Long, Member> members = memberRepository.findByIdIn(memberIds).stream()
                .collect(Collectors.toMap(Member::getId, Function.identity()));

        List<Long> postIds = posts.stream()
                .map(item -> item.post().getCommunitySharePostId())
                .toList();
        List<CommunityShareParticipant> participants = communityShareParticipantRepository
                .findByCommunitySharePostIdInAndStatus(postIds, CommunityShareParticipantStatus.JOINED);
        Map<Long, Long> joinedCounts = participants.stream()
                .collect(Collectors.groupingBy(CommunityShareParticipant::getCommunitySharePostId, Collectors.counting()));
        Map<Long, Boolean> joinedMap = participants.stream()
                .filter(participant -> participant.getMemberId().equals(currentMemberId))
                .collect(Collectors.toMap(CommunityShareParticipant::getCommunitySharePostId, participant -> true, (a, b) -> a));

        return posts.stream()
                .map(item -> {
                    CommunitySharePost post = item.post();
                    Member owner = members.get(post.getMemberId());
                    return toResponse(
                            post,
                            owner,
                            currentMemberId,
                            joinedCounts.getOrDefault(post.getCommunitySharePostId(), 0L).intValue() + 1,
                            post.isOwnedBy(currentMemberId) || joinedMap.getOrDefault(post.getCommunitySharePostId(), false),
                            item.distanceKm()
                    );
                })
                .toList();
    }

    private CommunitySharePostResponse toResponse(
            CommunitySharePost post,
            Member owner,
            Long currentMemberId,
            int joinedCount,
            boolean joined,
            Double distanceKm
    ) {
        return CommunitySharePostResponse.from(
                post,
                owner == null ? "알 수 없음" : owner.getNickname(),
                currentMemberId,
                joinedCount,
                joined,
                distanceKm == null ? null : BigDecimal.valueOf(distanceKm).setScale(1, RoundingMode.HALF_UP).doubleValue()
        );
    }

    private Double calculateDistanceKm(BigDecimal latitude, BigDecimal longitude, CommunitySharePost post) {
        if (latitude == null || longitude == null) {
            return null;
        }

        double userLatitude = Math.toRadians(latitude.doubleValue());
        double postLatitude = Math.toRadians(post.getLatitude().doubleValue());
        double deltaLatitude = postLatitude - userLatitude;
        double deltaLongitude = Math.toRadians(post.getLongitude().doubleValue() - longitude.doubleValue());

        double a = Math.sin(deltaLatitude / 2) * Math.sin(deltaLatitude / 2)
                + Math.cos(userLatitude) * Math.cos(postLatitude)
                * Math.sin(deltaLongitude / 2) * Math.sin(deltaLongitude / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return 6371.0 * c;
    }

    private Member findMember(String email) {
        return memberRepository.findByEmail(email)
                .orElseThrow(MemberNotFoundException::new);
    }

    private Member findMemberById(Long memberId) {
        return memberRepository.findById(memberId)
                .orElseThrow(MemberNotFoundException::new);
    }

    private record PostWithDistance(CommunitySharePost post, Double distanceKm) {
    }
}
