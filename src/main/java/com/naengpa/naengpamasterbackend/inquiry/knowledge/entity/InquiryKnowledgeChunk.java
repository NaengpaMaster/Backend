package com.naengpa.naengpamasterbackend.inquiry.knowledge.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "inquiry_knowledge_chunks")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class InquiryKnowledgeChunk {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "inquiry_knowledge_chunk_id")
    private Long id;

    @Column(name = "inquiry_knowledge_document_id", nullable = false)
    private Long inquiryKnowledgeDocumentId;

    @Column(name = "chunk_order", nullable = false)
    private Integer chunkOrder;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String content;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    public static InquiryKnowledgeChunk create(Long documentId, int chunkOrder, String content) {
        InquiryKnowledgeChunk chunk = new InquiryKnowledgeChunk();
        chunk.inquiryKnowledgeDocumentId = documentId;
        chunk.chunkOrder = chunkOrder;
        chunk.content = content;
        chunk.createdAt = LocalDateTime.now();
        return chunk;
    }
}

