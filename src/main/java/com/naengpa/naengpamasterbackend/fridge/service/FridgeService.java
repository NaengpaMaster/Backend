package com.naengpa.naengpamasterbackend.fridge.service;

import com.naengpa.naengpamasterbackend.fridge.dto.response.FridgeAccessResponse;
import com.naengpa.naengpamasterbackend.fridge.dto.response.FridgeResponse;
import com.naengpa.naengpamasterbackend.fridge.dto.response.FridgeInviteResponse;
import com.naengpa.naengpamasterbackend.fridge.dto.response.FridgeMemberResponse;
import com.naengpa.naengpamasterbackend.fridge.entity.Fridge;
import com.naengpa.naengpamasterbackend.fridge.entity.FridgeInvite;
import com.naengpa.naengpamasterbackend.fridge.entity.FridgeInviteStatus;
import com.naengpa.naengpamasterbackend.fridge.entity.FridgeMember;
import com.naengpa.naengpamasterbackend.fridge.entity.FridgeMemberRole;
import com.naengpa.naengpamasterbackend.fridge.entity.FridgeMemberStatus;
import com.naengpa.naengpamasterbackend.fridge.entity.FridgeStatus;
import com.naengpa.naengpamasterbackend.fridge.repository.FridgeInviteRepository;
import com.naengpa.naengpamasterbackend.fridge.repository.FridgeMemberRepository;
import com.naengpa.naengpamasterbackend.fridge.repository.FridgeRepository;
import com.naengpa.naengpamasterbackend.member.entity.Member;
import com.naengpa.naengpamasterbackend.member.entity.MemberRole;
import com.naengpa.naengpamasterbackend.member.entity.MemberStatus;
import com.naengpa.naengpamasterbackend.member.repository.MemberRepository;
import com.naengpa.naengpamasterbackend.subscription.entity.SubscriptionStatus;
import com.naengpa.naengpamasterbackend.subscription.repository.SubscriptionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class FridgeService {

    private static final List<SubscriptionStatus> PREMIUM_STATUSES = List.of(
            SubscriptionStatus.TRIALING,
            SubscriptionStatus.ACTIVE
    );
    private static final int MAX_ACTIVE_FRIDGE_MEMBERS = 4;

    private final FridgeRepository fridgeRepository;
    private final FridgeInviteRepository fridgeInviteRepository;
    private final FridgeMemberRepository fridgeMemberRepository;
    private final MemberRepository memberRepository;
    private final SubscriptionRepository subscriptionRepository;

    @Transactional
    public Fridge createDefaultFridge(Member member) {
        Fridge fridge = fridgeRepository.save(Fridge.createDefault(member.getId(), member.getNickname()));
        fridgeMemberRepository.save(FridgeMember.createOwner(fridge.getFridgeId(), member.getId()));
        return fridge;
    }

    @Transactional
    public Fridge getOrCreateDefaultFridge(Member member) {
        return fridgeRepository.findFirstByOwnerMemberIdAndStatusOrderByFridgeIdAsc(
                        member.getId(),
                        FridgeStatus.ACTIVE
                )
                .orElseGet(() -> createDefaultFridge(member));
    }


    @Transactional
    public synchronized Long findOrCreateFridgeId(Member member) {
        return getOrCreateDefaultFridge(member).getFridgeId();
    }

    @Transactional(readOnly = true)
    public Fridge getAccessibleFridge(Long fridgeId, Long memberId) {
        if (!fridgeMemberRepository.existsByFridgeIdAndMemberIdAndStatus(
                fridgeId,
                memberId,
                FridgeMemberStatus.ACTIVE
        )) {
            throw new AccessDeniedException("접근할 수 없는 냉장고입니다.");
        }
        return fridgeRepository.findById(fridgeId)
                .filter(fridge -> fridge.getStatus() == FridgeStatus.ACTIVE)
                .orElseThrow(() -> new AccessDeniedException("접근할 수 없는 냉장고입니다."));
    }

    @Transactional
    public FridgeResponse getMyDefaultFridge(String email) {
        Member member = findMemberByEmail(email);
        Fridge fridge = getOrCreateDefaultFridge(member);
        return FridgeResponse.from(fridge);
    }

    @Transactional
    public List<FridgeAccessResponse> getAccessibleFridges(String email) {
        Member member = findMemberByEmail(email);
        getOrCreateDefaultFridge(member);
        synchronizeFamilyFridgeLinks(member);

        List<FridgeMember> fridgeMembers = fridgeMemberRepository.findAllByMemberIdAndStatus(
                member.getId(),
                FridgeMemberStatus.ACTIVE
        );
        List<Fridge> fridges = fridgeRepository.findAllById(
                        fridgeMembers.stream()
                                .map(FridgeMember::getFridgeId)
                                .distinct()
                                .toList()
                )
                .stream()
                .filter(fridge -> fridge.getStatus() == FridgeStatus.ACTIVE)
                .toList();
        Map<Long, Fridge> fridgesById = fridges.stream()
                .collect(Collectors.toMap(Fridge::getFridgeId, fridge -> fridge));
        Map<Long, Member> ownersById = memberRepository.findAllById(
                        fridges.stream()
                                .map(Fridge::getOwnerMemberId)
                                .distinct()
                                .toList()
                )
                .stream()
                .collect(Collectors.toMap(Member::getId, owner -> owner));

        return fridgeMembers.stream()
                .map(fridgeMember -> {
                    Fridge fridge = fridgesById.get(fridgeMember.getFridgeId());
                    if (fridge == null) {
                        return null;
                    }
                    return FridgeAccessResponse.of(
                            fridge,
                            fridgeMember,
                            ownersById.get(fridge.getOwnerMemberId()),
                            member.getId()
                    );
                })
                .filter(response -> response != null)
                .sorted((left, right) -> Boolean.compare(right.mine(), left.mine()))
                .toList();
    }

    @Transactional(readOnly = true)
    public List<FridgeMemberResponse> getMyFridgeMembers(String email) {
        Member member = findMemberByEmail(email);
        Fridge fridge = getMyActiveFridge(member);
        validateActiveMember(fridge.getFridgeId(), member.getId());

        List<FridgeMember> fridgeMembers = fridgeMemberRepository.findAllByFridgeIdAndStatus(
                fridge.getFridgeId(),
                FridgeMemberStatus.ACTIVE
        );
        Map<Long, Member> membersById = memberRepository.findAllById(
                        fridgeMembers.stream()
                                .map(FridgeMember::getMemberId)
                                .toList()
                )
                .stream()
                .collect(Collectors.toMap(Member::getId, memberItem -> memberItem));

        return fridgeMembers.stream()
                .map(fridgeMember -> FridgeMemberResponse.of(fridgeMember, membersById.get(fridgeMember.getMemberId())))
                .toList();
    }

    @Transactional
    public FridgeInviteResponse inviteMember(String email, String inviteeEmail) {
        Member owner = findMemberByEmail(email);
        Fridge fridge = getOrCreateDefaultFridge(owner);
        validateOwner(fridge.getFridgeId(), owner.getId());
        validatePremium(fridge.getFridgeId());

        Member invitee = memberRepository.findByEmail(inviteeEmail)
                .filter(member -> member.getStatus() == MemberStatus.ACTIVE && member.getDeletedAt() == null)
                .orElseThrow(() -> new IllegalArgumentException("아직 가입되지 않은 이메일입니다. 먼저 회원가입을 요청해주세요."));

        validateMemberCapacity(fridge.getFridgeId());

        if (owner.getId().equals(invitee.getId())) {
            throw new IllegalArgumentException("본인은 초대할 수 없습니다.");
        }

        if (invitee.getRole() == MemberRole.ADMIN) {
            throw new IllegalArgumentException("관리자 계정은 가족 냉장고에 초대할 수 없습니다.");
        }

        if (fridgeMemberRepository.existsByFridgeIdAndMemberIdAndStatus(
                fridge.getFridgeId(),
                invitee.getId(),
                FridgeMemberStatus.ACTIVE
        )) {
            throw new IllegalArgumentException("이미 가족 냉장고에 참여 중인 회원입니다.");
        }

        if (fridgeInviteRepository.existsByFridgeIdAndInviteeMemberIdAndStatus(
                fridge.getFridgeId(),
                invitee.getId(),
                FridgeInviteStatus.PENDING
        )) {
            throw new IllegalArgumentException("이미 가족 공유 신청을 보낸 회원입니다.");
        }

        FridgeInvite invite = fridgeInviteRepository.save(
                FridgeInvite.create(fridge.getFridgeId(), owner.getId(), invitee.getId())
        );

        return FridgeInviteResponse.of(invite, owner, invitee);
    }

    @Transactional(readOnly = true)
    public List<FridgeInviteResponse> getPendingInvites(String email) {
        Member member = findMemberByEmail(email);
        List<FridgeInvite> invites = fridgeInviteRepository.findAllByInviteeMemberIdAndStatus(
                member.getId(),
                FridgeInviteStatus.PENDING
        );
        return toInviteResponses(invites);
    }

    @Transactional(readOnly = true)
    public List<FridgeInviteResponse> getMyFridgePendingInvites(String email) {
        Member owner = findMemberByEmail(email);
        Fridge fridge = getMyActiveFridge(owner);
        validateOwner(fridge.getFridgeId(), owner.getId());
        validatePremium(fridge.getFridgeId());

        List<FridgeInvite> invites = fridgeInviteRepository.findAllByFridgeIdAndStatus(
                fridge.getFridgeId(),
                FridgeInviteStatus.PENDING
        );
        return toInviteResponses(invites);
    }

    @Transactional
    public FridgeMemberResponse acceptInvite(String email, Long inviteId) {
        Member invitee = findMemberByEmail(email);
        FridgeInvite invite = fridgeInviteRepository.findByFridgeInviteIdAndInviteeMemberIdAndStatus(
                        inviteId,
                        invitee.getId(),
                        FridgeInviteStatus.PENDING
                )
                .orElseThrow(() -> new AccessDeniedException("수락할 가족 공유 신청을 찾을 수 없습니다."));

        Fridge fridge = fridgeRepository.findById(invite.getFridgeId())
                .filter(fridgeItem -> fridgeItem.getStatus() == FridgeStatus.ACTIVE)
                .orElseThrow(() -> new AccessDeniedException("참여할 냉장고를 찾을 수 없습니다."));
        validatePremium(fridge.getFridgeId());
        validateMemberCapacity(fridge.getFridgeId());

        FridgeMember fridgeMember = fridgeMemberRepository.findByFridgeIdAndMemberId(
                        fridge.getFridgeId(),
                        invitee.getId()
                )
                .map(existingMember -> {
                    existingMember.activateAsMember();
                    return existingMember;
                })
                .orElseGet(() -> fridgeMemberRepository.save(
                        FridgeMember.createMember(fridge.getFridgeId(), invitee.getId())
                ));
        synchronizeFamilyFridgeLinks(invitee);
        invite.accept();

        return FridgeMemberResponse.of(fridgeMember, invitee);
    }

    @Transactional
    public void rejectInvite(String email, Long inviteId) {
        Member invitee = findMemberByEmail(email);
        FridgeInvite invite = fridgeInviteRepository.findByFridgeInviteIdAndInviteeMemberIdAndStatus(
                        inviteId,
                        invitee.getId(),
                        FridgeInviteStatus.PENDING
                )
                .orElseThrow(() -> new AccessDeniedException("거절할 가족 공유 신청을 찾을 수 없습니다."));
        invite.reject();
    }

    @Transactional
    public void removeMember(String email, Long memberId) {
        Member owner = findMemberByEmail(email);
        Fridge fridge = getOrCreateDefaultFridge(owner);
        validateOwner(fridge.getFridgeId(), owner.getId());
        validatePremium(fridge.getFridgeId());

        if (owner.getId().equals(memberId)) {
            throw new IllegalArgumentException("소유자는 가족 냉장고에서 제거할 수 없습니다.");
        }

        FridgeMember fridgeMember = fridgeMemberRepository.findByFridgeIdAndMemberIdAndStatus(
                        fridge.getFridgeId(),
                        memberId,
                        FridgeMemberStatus.ACTIVE
                )
                .orElseThrow(() -> new AccessDeniedException("가족 구성원을 찾을 수 없습니다."));

        if (fridgeMember.getRole() == FridgeMemberRole.OWNER) {
            throw new IllegalArgumentException("소유자는 가족 냉장고에서 제거할 수 없습니다.");
        }

        fridgeMember.leave();
        deactivateReciprocalMember(fridge.getOwnerMemberId(), memberId);
    }

    private Member findMemberByEmail(String email) {
        return memberRepository.findByEmail(email)
                .orElseThrow(() -> new BadCredentialsException("회원을 찾을 수 없습니다."));
    }

    @Transactional
    public Fridge getMyActiveFridge(Member member) {
        return fridgeMemberRepository.findAllByMemberIdAndStatus(member.getId(), FridgeMemberStatus.ACTIVE)
                .stream()
                .map(FridgeMember::getFridgeId)
                .distinct()
                .filter(this::hasPremium)
                .findFirst()
                .flatMap(fridgeRepository::findById)
                .filter(fridge -> fridge.getStatus() == FridgeStatus.ACTIVE)
                .orElseGet(() -> getOrCreateDefaultFridge(member));
    }

    private List<FridgeInviteResponse> toInviteResponses(List<FridgeInvite> invites) {
        List<Long> memberIds = invites.stream()
                .flatMap(invite -> List.of(invite.getInviterMemberId(), invite.getInviteeMemberId()).stream())
                .distinct()
                .toList();
        Map<Long, Member> membersById = memberRepository.findAllById(memberIds)
                .stream()
                .collect(Collectors.toMap(Member::getId, member -> member));

        return invites.stream()
                .map(invite -> FridgeInviteResponse.of(
                        invite,
                        membersById.get(invite.getInviterMemberId()),
                        membersById.get(invite.getInviteeMemberId())
                ))
                .toList();
    }

    private void validateActiveMember(Long fridgeId, Long memberId) {
        if (!fridgeMemberRepository.existsByFridgeIdAndMemberIdAndStatus(
                fridgeId,
                memberId,
                FridgeMemberStatus.ACTIVE
        )) {
            throw new AccessDeniedException("접근할 수 없는 냉장고입니다.");
        }
    }

    private void validateOwner(Long fridgeId, Long memberId) {
        if (!fridgeMemberRepository.existsByFridgeIdAndMemberIdAndRoleAndStatus(
                fridgeId,
                memberId,
                FridgeMemberRole.OWNER,
                FridgeMemberStatus.ACTIVE
        )) {
            throw new AccessDeniedException("가족 냉장고 관리 권한이 없습니다.");
        }
    }

    private void validatePremium(Long fridgeId) {
        if (!hasPremium(fridgeId)) {
            throw new AccessDeniedException("프리미엄 구독자만 가족 공유 냉장고를 관리할 수 있습니다.");
        }
    }

    private void validateMemberCapacity(Long fridgeId) {
        int activeMemberCount = fridgeMemberRepository.findAllByFridgeIdAndStatus(
                fridgeId,
                FridgeMemberStatus.ACTIVE
        ).size();
        if (activeMemberCount >= MAX_ACTIVE_FRIDGE_MEMBERS) {
            throw new IllegalArgumentException("가족 공유 냉장고는 본인 포함 최대 4명까지 사용할 수 있습니다.");
        }
    }

    private boolean hasPremium(Long fridgeId) {
        return subscriptionRepository.findFirstByFridgeIdAndStatusInOrderBySubscriptionIdDesc(
                fridgeId,
                PREMIUM_STATUSES
        ).isPresent();
    }

    private void synchronizeFamilyFridgeLinks(Member member) {
        List<FridgeMember> myMemberships = fridgeMemberRepository.findAllByMemberIdAndStatus(
                member.getId(),
                FridgeMemberStatus.ACTIVE
        );
        List<Long> premiumFridgeIds = myMemberships.stream()
                .map(FridgeMember::getFridgeId)
                .distinct()
                .filter(this::hasPremium)
                .toList();
        if (premiumFridgeIds.isEmpty()) {
            return;
        }

        List<Long> familyMemberIds = premiumFridgeIds.stream()
                .flatMap(fridgeId -> fridgeMemberRepository.findAllByFridgeIdAndStatus(
                        fridgeId,
                        FridgeMemberStatus.ACTIVE
                ).stream())
                .map(FridgeMember::getMemberId)
                .filter(Objects::nonNull)
                .distinct()
                .toList();

        familyMemberIds.forEach(this::getOrCreateDefaultFridgeByMemberId);
        List<Fridge> familyDefaultFridges = fridgeRepository.findAllById(
                        familyMemberIds.stream()
                                .map(this::getOrCreateDefaultFridgeByMemberId)
                                .map(Fridge::getFridgeId)
                                .toList()
                )
                .stream()
                .filter(fridge -> fridge.getStatus() == FridgeStatus.ACTIVE)
                .toList();

        for (Fridge familyFridge : familyDefaultFridges) {
            for (Long familyMemberId : familyMemberIds) {
                if (familyFridge.getOwnerMemberId().equals(familyMemberId)) {
                    continue;
                }
                activateOrCreateMember(familyFridge.getFridgeId(), familyMemberId);
            }
        }
    }

    private Fridge getOrCreateDefaultFridgeByMemberId(Long memberId) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new BadCredentialsException("회원을 찾을 수 없습니다."));
        return getOrCreateDefaultFridge(member);
    }

    private void activateOrCreateMember(Long fridgeId, Long memberId) {
        fridgeMemberRepository.findByFridgeIdAndMemberId(fridgeId, memberId)
                .ifPresentOrElse(
                        FridgeMember::activateAsMember,
                        () -> fridgeMemberRepository.save(FridgeMember.createMember(fridgeId, memberId))
                );
    }

    private void deactivateReciprocalMember(Long ownerMemberId, Long removedMemberId) {
        fridgeRepository.findFirstByOwnerMemberIdAndStatusOrderByFridgeIdAsc(
                        removedMemberId,
                        FridgeStatus.ACTIVE
                )
                .flatMap(removedMemberFridge -> fridgeMemberRepository.findByFridgeIdAndMemberIdAndStatus(
                        removedMemberFridge.getFridgeId(),
                        ownerMemberId,
                        FridgeMemberStatus.ACTIVE
                ))
                .ifPresent(FridgeMember::leave);
    }

}
