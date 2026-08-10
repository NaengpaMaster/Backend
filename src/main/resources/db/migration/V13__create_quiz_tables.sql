CREATE TABLE quizzes (
                      quiz_id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
                      statement TEXT NOT NULL,
                      answer BOOLEAN NOT NULL,
                      explanation TEXT,
                      source_product_name VARCHAR(100),
                      source_product_id BIGINT,
                      quiz_date DATE NOT NULL UNIQUE,
                      created_at TIMESTAMP NOT NULL
);

CREATE TABLE quiz_results (
                             quiz_result_id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
                             member_id BIGINT NOT NULL,
                             quiz_id BIGINT NOT NULL,
                             submitted_answer BOOLEAN NOT NULL,
                             is_correct BOOLEAN NOT NULL,
                             submitted_at TIMESTAMP NOT NULL,
                             UNIQUE (member_id, quiz_id),
                             CONSTRAINT fk_quiz_results_member FOREIGN KEY (member_id) REFERENCES members(member_id),
                             CONSTRAINT fk_quiz_results_quiz FOREIGN KEY (quiz_id) REFERENCES quizzes(quiz_id)
);