--같은 inquiry_id에 대해 is_deleted = false인 답변은 최대 1개만 허용
CREATE UNIQUE INDEX uq_inquiry_answers_active
    ON inquiry_answers (inquiry_id)
    WHERE is_deleted = false;