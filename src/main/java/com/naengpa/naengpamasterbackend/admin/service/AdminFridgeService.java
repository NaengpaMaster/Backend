package com.naengpa.naengpamasterbackend.admin.service;

import com.naengpa.naengpamasterbackend.admin.dto.response.AdminFridgeInviteResponse;
import com.naengpa.naengpamasterbackend.admin.dto.response.AdminFridgeMemberResponse;
import com.naengpa.naengpamasterbackend.admin.dto.response.AdminFridgeResponse;
import com.naengpa.naengpamasterbackend.fridge.entity.Fridge;
import com.naengpa.naengpamasterbackend.fridge.entity.FridgeInvite;
import com.naengpa.naengpamasterbackend.fridge.entity.FridgeInviteStatus;
import com.naengpa.naengpamasterbackend.fridge.entity.FridgeMember;
import com.naengpa.naengpamasterbackend.fridge.entity.FridgeMemberRole;
import com.naengpa.naengpamasterbackend.fridge.entity.FridgeMemberStatus;
import com.naengpa.naengpamasterbackend.fridge.repository.FridgeInviteRepository;
import com.naengpa.naengpamasterbackend.fridge.repository.FridgeMemberRepository;
import com.naengpa.naengpamasterbackend.fridge.repository.FridgeRepository;
import com.naengpa.naengpamasterbackend.member.entity.Member;
import com.naengpa.naengpamasterbackend.member.entity.MemberRole;
import com.naengpa.naengpamasterbackend.member.repository.MemberRepository;
import com.naengpa.naengpamasterbackend.subscription.entity.Subscription;
import com.naengpa.naengpamasterbackend.subscription.entity.SubscriptionStatus;
import com.naengpa.naengpamasterbackend.subscription.repository.SubscriptionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AdminFridgeService {

    private static final int MAX_ACTIVE_FRIDGE_MEMBERS = 4;
    private static final List<SubscriptionStatus> PREMIUM_STATUSES = List.of(
            SubscriptionStatus.TRIALING,
            SubscriptionStatus.ACTIVE
    );

    private final FridgeRepository fridgeRepository;
    private final FridgeMemberRepository fridgeMemberRepository;
    private final FridgeInviteRepository fridgeInviteRepository;
    private final MemberRepository memberRepository;
    private final SubscriptionRepository subscriptionRepository;

    @Transactional(readOnly = true)
    public List<AdminFridgeResponse> getFridges() {
        return fridgeRepository.findAll().stream()
                .filter(fridge -> !hasAdminParticipant(fridge.getFridgeId(), fridge.getOwnerMemberId()))
                .sorted(Comparator.comparing(Fridge::getFridgeId).reversed())
                .map(this::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public AdminFridgeResponse getFridge(Long fridgeId) {
        Fridge fridge = fridgeRepository.findById(fridgeId)
                .orElseThrow(() -> new IllegalArgumentException("냉장고를 찾을 수 없습니다."));
        if (hasAdminParticipant(fridge.getFridgeId(), fridge.getOwnerMemberId())) {
            throw new IllegalArgumentException("관리자 계정의 냉장고는 가족공유 관리 대상이 아닙니다.");
        }
        return toResponse(fridge);
    }

    @Transactional
    public void removeMember(Long fridgeId, Long memberId) {
        FridgeMember fridgeMember = fridgeMemberRepository.findByFridgeIdAndMemberIdAndStatus(
                        fridgeId,
                        memberId,
                        FridgeMemberStatus.ACTIVE
                )
                .orElseThrow(() -> new IllegalArgumentException("활성 가족 구성원을 찾을 수 없습니다."));

        if (fridgeMember.getRole() == FridgeMemberRole.OWNER) {
            throw new AccessDeniedException("냉장고 소유자는 관리자 최소 조치에서 내보낼 수 없습니다.");
        }

        if (!hasActiveSubscription(fridgeId)) {
            throw new AccessDeniedException("구독 냉장고에서만 구성원 내보내기를 사용할 수 있습니다.");
        }

        fridgeMember.leave();
    }


    @Transactional
    public void cancelInvite(Long fridgeId, Long inviteId) {
        FridgeInvite invite = fridgeInviteRepository.findByFridgeInviteIdAndFridgeIdAndStatus(
                        inviteId,
                        fridgeId,
                        FridgeInviteStatus.PENDING
                )
                .orElseThrow(() -> new IllegalArgumentException("취소할 대기중 초대를 찾을 수 없습니다."));

        invite.expire();
    }

    private boolean hasAdminParticipant(Long fridgeId, Long ownerMemberId) {
        if (memberRepository.findById(ownerMemberId)
                .map(owner -> owner.getRole() == MemberRole.ADMIN)
                .orElse(false)) {
            return true;
        }

        List<Long> memberIds = fridgeMemberRepository.findAllByFridgeIdAndStatus(
                        fridgeId,
                        FridgeMemberStatus.ACTIVE
                )
                .stream()
                .map(FridgeMember::getMemberId)
                .distinct()
                .toList();

        return memberRepository.findAllById(memberIds).stream()
                .anyMatch(member -> member.getRole() == MemberRole.ADMIN);
    }

    private boolean hasActiveSubscription(Long fridgeId) {
        return subscriptionRepository.findFirstByFridgeIdAndStatusInOrderBySubscriptionIdDesc(
                fridgeId,
                PREMIUM_STATUSES
        ).isPresent();
    }

    private AdminFridgeResponse toResponse(Fridge fridge) {
        List<FridgeMember> members = fridgeMemberRepository.findAllByFridgeIdAndStatus(
                fridge.getFridgeId(),
                FridgeMemberStatus.ACTIVE
        );
        List<FridgeInvite> pendingInvites = fridgeInviteRepository.findAllByFridgeIdAndStatus(
                fridge.getFridgeId(),
                FridgeInviteStatus.PENDING
        );

        List<Long> memberIds = members.stream()
                .map(FridgeMember::getMemberId)
                .distinct()
                .toList();
        Map<Long, Member> membersById = memberRepository.findAllById(memberIds).stream()
                .collect(Collectors.toMap(Member::getId, member -> member));

        List<Long> inviteMemberIds = pendingInvites.stream()
                .flatMap(invite -> List.of(invite.getInviterMemberId(), invite.getInviteeMemberId()).stream())
                .distinct()
                .toList();
        Map<Long, Member> inviteMembersById = memberRepository.findAllById(inviteMemberIds).stream()
                .collect(Collectors.toMap(Member::getId, member -> member));

        Member owner = memberRepository.findById(fridge.getOwnerMemberId()).orElse(null);
        String subscriptionStatus = subscriptionRepository.findFirstByFridgeIdAndStatusInOrderBySubscriptionIdDesc(
                        fridge.getFridgeId(),
                        PREMIUM_STATUSES
                )
                .map(Subscription::getStatus)
                .map(Enum::name)
                .orElse("FREE");

        return new AdminFridgeResponse(
                fridge.getFridgeId(),
                fridge.getName(),
                fridge.getOwnerMemberId(),
                owner == null ? null : owner.getEmail(),
                owner == null ? null : owner.getNickname(),
                fridge.getStatus().name(),
                subscriptionStatus,
                members.size(),
                MAX_ACTIVE_FRIDGE_MEMBERS,
                pendingInvites.size(),
                fridge.getCreatedAt(),
                members.stream()
                        .map(member -> AdminFridgeMemberResponse.of(member, membersById.get(member.getMemberId())))
                        .toList(),
                pendingInvites.stream()
                        .map(invite -> AdminFridgeInviteResponse.of(
                                invite,
                                inviteMembersById.get(invite.getInviterMemberId()),
                                inviteMembersById.get(invite.getInviteeMemberId())
                        ))
                        .toList()
        );
    }
}
