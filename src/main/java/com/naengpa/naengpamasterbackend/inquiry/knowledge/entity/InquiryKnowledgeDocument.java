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
@Table(name = "inquiry_knowledge_documents")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class InquiryKnowledgeDocument {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "inquiry_knowledge_document_id")
    private Long id;

    @Column(nullable = false, length = 200)
    private String title;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String content;

    @Column(name = "source_name", nullable = false, unique = true, length = 255)
    private String sourceName;

    @Column(nullable = false)
    private Integer version;

    @Column(name = "is_active", nullable = false)
    private Boolean isActive;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    public static InquiryKnowledgeDocument create(String title, String content, String sourceName) {
        InquiryKnowledgeDocument document = new InquiryKnowledgeDocument();
        document.title = title;
        document.content = content;
        document.sourceName = sourceName;
        document.version = 1;
        document.isActive = true;
        document.createdAt = LocalDateTime.now();
        return document;
    }

    public void update(String title, String content) {
        this.title = title;
        this.content = content;
        this.version++;
        this.isActive = true;
        this.updatedAt = LocalDateTime.now();
    }
}

