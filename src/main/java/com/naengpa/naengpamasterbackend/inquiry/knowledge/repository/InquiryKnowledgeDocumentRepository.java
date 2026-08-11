package com.naengpa.naengpamasterbackend.inquiry.knowledge.repository;

import com.naengpa.naengpamasterbackend.inquiry.knowledge.entity.InquiryKnowledgeDocument;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface InquiryKnowledgeDocumentRepository extends JpaRepository<InquiryKnowledgeDocument, Long> {

    Optional<InquiryKnowledgeDocument> findBySourceName(String sourceName);
}

