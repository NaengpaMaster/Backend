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

    // 로그인한 회원이 작성한 문의 목록을 조회합니다.
    @Transactional(readOnly = true)
    public Page<InquiryResponse> getInquiries(String email, Pageable pageable) {

        Long memberId = resolveMemberId(email);

        return inquiryRepository.findByMemberIdAndIsDeletedFalseOrderByCreatedAtDesc(memberId, pageable)
                .map(InquiryResponse::from);
    }

    // 로그인한 회원이 소유한 문의와 답변을 조회합니다.
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

    // 로그인한 회원의 새 문의를 등록합니다.
    @Transactional
    public void createInquiry(InquiryRequest request, String email) {
        Long memberId = resolveMemberId(email);
        Inquiry inquiry = Inquiry.create(request, memberId);
        inquiryRepository.save(inquiry);
    }

    // 답변 전인 본인 문의의 제목과 내용을 수정합니다.
    @Transactional
    public void updateInquiry(Long inquiryId, InquiryRequest request, String email) {
        Long memberId = resolveMemberId(email);
        Inquiry inquiry = inquiryRepository
                .findByIdAndMemberIdAndIsDeletedFalse(inquiryId, memberId)
                .orElseThrow(InquiryNotFoundException::new);

        validateNotAnswered(inquiry);

        inquiry.update(request);
    }

    // 답변 전인 본인 문의를 삭제 처리합니다.
    @Transactional
    public void deleteInquiry(Long inquiryId, String email) {
        Long memberId = resolveMemberId(email);
        Inquiry inquiry = inquiryRepository
                .findByIdAndMemberIdAndIsDeletedFalse(inquiryId, memberId)
                .orElseThrow(InquiryNotFoundException::new);

        validateNotAnswered(inquiry);

        inquiry.delete();
    }

    // 답변이 완료된 문의의 수정·삭제 요청을 차단합니다.
    private void validateNotAnswered(Inquiry inquiry) {
        if (inquiry.getIsAnswered()) {
            throw new InquiryAlreadyAnsweredException();
        }
    }
    // 인증 이메일에 해당하는 회원 ID를 조회합니다.
    private Long resolveMemberId(String email) {
        return memberRepository.findByEmail(email)
                .map(Member::getId)
                .orElseThrow(MemberNotFoundException::new);
    }
}
