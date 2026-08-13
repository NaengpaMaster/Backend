package com.naengpa.naengpamasterbackend.inquiry.knowledge.repository;

import com.naengpa.naengpamasterbackend.inquiry.knowledge.entity.InquiryKnowledgeChunk;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface InquiryKnowledgeChunkRepository extends JpaRepository<InquiryKnowledgeChunk, Long> {

    // 정책 문서에 연결된 기존 청크를 모두 삭제합니다.
    @Modifying(flushAutomatically = true, clearAutomatically = true)
    @Query("DELETE FROM InquiryKnowledgeChunk c WHERE c.inquiryKnowledgeDocumentId = :documentId")
    void deleteAllByDocumentId(@Param("documentId") Long documentId);

    // 질문과 유사한 활성 정책 문서 청크를 pg_trgm 유사도순으로 조회합니다.
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
