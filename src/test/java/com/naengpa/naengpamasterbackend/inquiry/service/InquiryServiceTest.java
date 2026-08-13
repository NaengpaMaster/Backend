package com.naengpa.naengpamasterbackend.inquiry.service;

import com.naengpa.naengpamasterbackend.global.exception.InquiryAlreadyAnsweredException;
import com.naengpa.naengpamasterbackend.global.exception.InquiryNotFoundException;
import com.naengpa.naengpamasterbackend.inquiry.dto.request.InquiryRequest;
import com.naengpa.naengpamasterbackend.inquiry.entity.Inquiry;
import com.naengpa.naengpamasterbackend.inquiry.repository.InquiryAnswerRepository;
import com.naengpa.naengpamasterbackend.inquiry.repository.InquiryRepository;
import com.naengpa.naengpamasterbackend.member.entity.Member;
import com.naengpa.naengpamasterbackend.member.repository.MemberRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

class InquiryServiceTest {

    private InquiryRepository inquiryRepository;
    private InquiryAnswerRepository inquiryAnswerRepository;
    private MemberRepository memberRepository;
    private InquiryService inquiryService;

    @BeforeEach
    void setUp() {
        inquiryRepository = mock(InquiryRepository.class);
        inquiryAnswerRepository = mock(InquiryAnswerRepository.class);
        memberRepository = mock(MemberRepository.class);
        inquiryService = new InquiryService(
                inquiryRepository,
                inquiryAnswerRepository,
                memberRepository
        );
    }

    @Test
    void getInquiryDetailRejectsInquiryNotOwnedByMember() {
        Long inquiryId = 1L;
        Long memberId = 10L;
        String email = "member@example.com";
        Member member = member(memberId);

        given(memberRepository.findByEmail(email)).willReturn(Optional.of(member));
        given(inquiryRepository.findByIdAndMemberIdAndIsDeletedFalse(inquiryId, memberId))
                .willReturn(Optional.empty());

        assertThatThrownBy(() -> inquiryService.getInquiryDetail(inquiryId, email))
                .isInstanceOf(InquiryNotFoundException.class);

        verify(inquiryAnswerRepository, never()).findByInquiryIdAndIsDeletedFalse(inquiryId);
    }

    @Test
    void updateInquiryUpdatesInquiryOwnedByMemberWhenUnanswered() {
        Long inquiryId = 1L;
        Long memberId = 10L;
        String email = "member@example.com";
        Inquiry inquiry = mock(Inquiry.class);
        InquiryRequest request = new InquiryRequest("수정 제목", "수정 내용");

        Member member = member(memberId);

        given(memberRepository.findByEmail(email)).willReturn(Optional.of(member));
        given(inquiryRepository.findByIdAndMemberIdAndIsDeletedFalse(inquiryId, memberId))
                .willReturn(Optional.of(inquiry));
        given(inquiry.getIsAnswered()).willReturn(false);

        inquiryService.updateInquiry(inquiryId, request, email);

        verify(inquiry).update(request);
    }

    @Test
    void updateInquiryRejectsInquiryNotOwnedByMember() {
        Long inquiryId = 1L;
        Long memberId = 10L;
        String email = "member@example.com";
        InquiryRequest request = new InquiryRequest("수정 제목", "수정 내용");

        Member member = member(memberId);

        given(memberRepository.findByEmail(email)).willReturn(Optional.of(member));
        given(inquiryRepository.findByIdAndMemberIdAndIsDeletedFalse(inquiryId, memberId))
                .willReturn(Optional.empty());

        assertThatThrownBy(() -> inquiryService.updateInquiry(inquiryId, request, email))
                .isInstanceOf(InquiryNotFoundException.class);
    }

    @Test
    void updateInquiryRejectsAnsweredInquiry() {
        Long inquiryId = 1L;
        Long memberId = 10L;
        String email = "member@example.com";
        Inquiry inquiry = mock(Inquiry.class);
        InquiryRequest request = new InquiryRequest("수정 제목", "수정 내용");

        Member member = member(memberId);

        given(memberRepository.findByEmail(email)).willReturn(Optional.of(member));
        given(inquiryRepository.findByIdAndMemberIdAndIsDeletedFalse(inquiryId, memberId))
                .willReturn(Optional.of(inquiry));
        given(inquiry.getIsAnswered()).willReturn(true);

        assertThatThrownBy(() -> inquiryService.updateInquiry(inquiryId, request, email))
                .isInstanceOf(InquiryAlreadyAnsweredException.class);

        verify(inquiry, never()).update(request);
    }

    @Test
    void deleteInquirySoftDeletesInquiryOwnedByMemberWhenUnanswered() {
        Long inquiryId = 1L;
        Long memberId = 10L;
        String email = "member@example.com";
        Inquiry inquiry = mock(Inquiry.class);

        Member member = member(memberId);

        given(memberRepository.findByEmail(email)).willReturn(Optional.of(member));
        given(inquiryRepository.findByIdAndMemberIdAndIsDeletedFalse(inquiryId, memberId))
                .willReturn(Optional.of(inquiry));
        given(inquiry.getIsAnswered()).willReturn(false);

        inquiryService.deleteInquiry(inquiryId, email);

        verify(inquiry).delete();
    }

    @Test
    void deleteInquiryRejectsInquiryNotOwnedByMember() {
        Long inquiryId = 1L;
        Long memberId = 10L;
        String email = "member@example.com";

        Member member = member(memberId);

        given(memberRepository.findByEmail(email)).willReturn(Optional.of(member));
        given(inquiryRepository.findByIdAndMemberIdAndIsDeletedFalse(inquiryId, memberId))
                .willReturn(Optional.empty());

        assertThatThrownBy(() -> inquiryService.deleteInquiry(inquiryId, email))
                .isInstanceOf(InquiryNotFoundException.class);
    }

    private Member member(Long memberId) {
        Member member = mock(Member.class);
        given(member.getId()).willReturn(memberId);
        return member;
    }
}
