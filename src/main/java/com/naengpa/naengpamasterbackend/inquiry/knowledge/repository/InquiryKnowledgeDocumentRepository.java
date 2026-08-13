package com.naengpa.naengpamasterbackend.inquiry.knowledge.repository;

import com.naengpa.naengpamasterbackend.inquiry.knowledge.entity.InquiryKnowledgeDocument;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface InquiryKnowledgeDocumentRepository extends JpaRepository<InquiryKnowledgeDocument, Long> {

    // 출처 이름이 일치하는 정책 문서를 조회합니다.
    Optional<InquiryKnowledgeDocument> findBySourceName(String sourceName);
}
