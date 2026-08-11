package com.naengpa.naengpamasterbackend.inquiry.knowledge.repository;

import com.naengpa.naengpamasterbackend.inquiry.knowledge.entity.InquiryKnowledgeChunk;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface InquiryKnowledgeChunkRepository extends JpaRepository<InquiryKnowledgeChunk, Long> {

    @Modifying(flushAutomatically = true, clearAutomatically = true)
    @Query("DELETE FROM InquiryKnowledgeChunk c WHERE c.inquiryKnowledgeDocumentId = :documentId")
    void deleteAllByDocumentId(@Param("documentId") Long documentId);

    // 소규모 정책 문서는 pg_trgm 검색으로 하며, 검색 품질이 부족할 때 임베딩 검색으로 교체한다.
    @Query(value = """
            SELECT d.source_name AS "sourceName",
                   c.content AS "content"
            FROM inquiry_knowledge_chunks c
            JOIN inquiry_knowledge_documents d
              ON d.inquiry_knowledge_document_id = c.inquiry_knowledge_document_id
            WHERE d.is_active = true
              AND word_similarity(:question, c.content) >= 0.15
            ORDER BY word_similarity(:question, c.content) DESC
            LIMIT :limit
            """, nativeQuery = true)
    List<InquiryKnowledgeContextProjection> findRelevantContexts(
            @Param("question") String question,
            @Param("limit") int limit
    );
}
