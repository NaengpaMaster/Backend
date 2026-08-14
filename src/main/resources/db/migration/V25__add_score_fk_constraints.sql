-- V25__add_score_fk_constraints.sql
ALTER TABLE scores
    ADD CONSTRAINT fk_scores_member
        FOREIGN KEY (member_id) REFERENCES members(member_id)
            ON DELETE CASCADE;

ALTER TABLE score_histories
    ADD CONSTRAINT fk_score_histories_member
        FOREIGN KEY (member_id) REFERENCES members(member_id)
            ON DELETE CASCADE;