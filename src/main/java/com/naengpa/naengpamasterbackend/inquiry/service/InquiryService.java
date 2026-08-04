package com.naengpa.naengpamasterbackend.inquiry.service;

import com.naengpa.naengpamasterbackend.global.exception.InquiryAlreadyAnsweredException;
import com.naengpa.naengpamasterbackend.global.exception.InquiryNotFoundException;
import com.naengpa.naengpamasterbackend.global.exception.MemberNotFoundException;
import com.naengpa.naengpamasterbackend.inquiry.dto.request.InquiryRequest;
import com.naengpa.naengpamasterbackend.inquiry.dto.response.InquiryDetailResponse;
import com.naengpa.naengpamasterbackend.inquiry.dto.response.InquiryResponse;
import com.naengpa.naengpamasterbackend.inquiry.entity.Inquiry;
import com.naengpa.naengpamasterbackend.inquiry.entity.InquiryAnswer;
import com.naengpa.naengpamasterbackend.inquiry.repository.InquiryAnswerRepository;
import com.naengpa.naengpamasterbackend.inquiry.repository.InquiryRepository;
import com.naengpa.naengpamasterbackend.member.entity.Member;
import com.naengpa.naengpamasterbackend.member.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class InquiryService {

    private final InquiryRepository inquiryRepository;
    private final InquiryAnswerRepository inquiryAnswerRepository;
    private final MemberRepository memberRepository;

    // 목록 조회
    @Transactional(readOnly = true)
    public Page<InquiryResponse> getInquiries(String email, Pageable pageable) {

        Long memberId = resolveMemberId(email);

        return inquiryRepository.findByMemberIdAndIsDeletedFalseOrderByCreatedAtDesc(memberId, pageable)
                .map(InquiryResponse::from);
    }

    // 상세 조회
    @Transactional(readOnly = true)
    public InquiryDetailResponse getInquiryDetail(Long inquiryId, String email) {
        Long memberId = resolveMemberId(email);

        Inquiry inquiry = inquiryRepository
                .findByIdAndMemberIdAndIsDeletedFalse(inquiryId, memberId)
                .orElseThrow(InquiryNotFoundException::new);

        InquiryAnswer inquiryAnswer = inquiryAnswerRepository
                .findByInquiryIdAndIsDeletedFalse(inquiryId)
                .orElse(null);
        return InquiryDetailResponse.from(inquiry ,inquiryAnswer);
    }

    // 등록
    @Transactional
    public void createInquiry(InquiryRequest request, String email) {
        Long memberId = resolveMemberId(email);
        Inquiry inquiry = Inquiry.create(request, memberId);
        inquiryRepository.save(inquiry);
    }

    // 수정
    @Transactional
    public void updateInquiry(Long inquiryId, InquiryRequest request, String email) {
        Long memberId = resolveMemberId(email);
        Inquiry inquiry = inquiryRepository
                .findByIdAndMemberIdAndIsDeletedFalse(inquiryId, memberId)
                .orElseThrow(InquiryNotFoundException::new);

        validateNotAnswered(inquiry);

        inquiry.update(request);
    }

    // 삭제
    @Transactional
    public void deleteInquiry(Long inquiryId, String email) {
        Long memberId = resolveMemberId(email);
        Inquiry inquiry = inquiryRepository
                .findByIdAndMemberIdAndIsDeletedFalse(inquiryId, memberId)
                .orElseThrow(InquiryNotFoundException::new);

        validateNotAnswered(inquiry);

        inquiry.delete();
    }

    private void validateNotAnswered(Inquiry inquiry) {
        if (inquiry.getIsAnswered()) {
            throw new InquiryAlreadyAnsweredException();
        }
    }
    private Long resolveMemberId(String email) {
        return memberRepository.findByEmail(email)
                .map(Member::getId)
                .orElseThrow(MemberNotFoundException::new);
    }
}

