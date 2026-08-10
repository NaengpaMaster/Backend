CREATE TABLE inquiry_knowledge_chunks (
    inquiry_knowledge_chunk_id BIGSERIAL PRIMARY KEY,
    inquiry_knowledge_document_id BIGINT NOT NULL,
    chunk_order INTEGER NOT NULL,
    content TEXT NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_inquiry_knowledge_chunks_document
        FOREIGN KEY (inquiry_knowledge_document_id)
        REFERENCES inquiry_knowledge_documents(inquiry_knowledge_document_id)
        ON DELETE CASCADE,

    CONSTRAINT uq_inquiry_knowledge_chunks_document_order
        UNIQUE (inquiry_knowledge_document_id, chunk_order),

    CONSTRAINT chk_inquiry_knowledge_chunks_order
        CHECK (chunk_order >= 0)
);

COMMENT ON TABLE inquiry_knowledge_chunks IS '문의 Q&A 챗봇 검색용 정책 문서 조각';

CREATE INDEX idx_inquiry_knowledge_chunks_document
    ON inquiry_knowledge_chunks(inquiry_knowledge_document_id);

CREATE INDEX idx_inquiry_knowledge_chunks_content_trgm
    ON inquiry_knowledge_chunks USING gin (content gin_trgm_ops);

