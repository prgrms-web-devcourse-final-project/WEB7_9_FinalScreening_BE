-- 1. Review 테이블 수정
-- 1-1. Foreign Key 제거 (존재 여부 확인 후 삭제)
SET @constraint_name := (SELECT DISTINCT CONSTRAINT_NAME FROM information_schema.TABLE_CONSTRAINTS
                         WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'review'
                           AND CONSTRAINT_NAME = 'FKrl7b0my7pmicpl5l591p7qdu7' LIMIT 1);
SET @query := IF(@constraint_name IS NOT NULL, 'ALTER TABLE review DROP FOREIGN KEY FKrl7b0my7pmicpl5l591p7qdu7', 'SELECT "Review FK already gone"');
PREPARE stmt FROM @query; EXECUTE stmt; DEALLOCATE PREPARE stmt;

-- 1-2. Unique Key 제거
SET @index_name := (SELECT DISTINCT INDEX_NAME FROM information_schema.STATISTICS
                    WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'review'
                      AND INDEX_NAME = 'uk_review_reviewer_reviewee_post' LIMIT 1);
SET @query := IF(@index_name IS NOT NULL, 'ALTER TABLE review DROP INDEX uk_review_reviewer_reviewee_post', 'SELECT "Review UK already gone"');
PREPARE stmt FROM @query; EXECUTE stmt; DEALLOCATE PREPARE stmt;

-- 1-3. post_id 삭제 및 party_id 추가
SET @col_exists := (SELECT COUNT(*) FROM information_schema.COLUMNS WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'review' AND COLUMN_NAME = 'post_id');
SET @query := IF(@col_exists > 0, 'ALTER TABLE review DROP COLUMN post_id', 'SELECT "post_id already gone from review"');
PREPARE stmt FROM @query; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @col_exists := (SELECT COUNT(*) FROM information_schema.COLUMNS WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'review' AND COLUMN_NAME = 'party_id');
SET @query := IF(@col_exists = 0, 'ALTER TABLE review ADD COLUMN party_id BIGINT NOT NULL', 'SELECT "party_id already exists in review"');
PREPARE stmt FROM @query; EXECUTE stmt; DEALLOCATE PREPARE stmt;

-- 1-4. 새로운 제약 조건 추가
ALTER TABLE review ADD CONSTRAINT fk_review_party FOREIGN KEY (party_id) REFERENCES party (party_id);
ALTER TABLE review ADD CONSTRAINT uk_review_reviewer_reviewee_party UNIQUE (reviewer_id, reviewee_id, party_id);


-- 2. ReviewRequest 테이블 수정
-- 2-1. Foreign Key 제거
SET @constraint_name := (SELECT DISTINCT CONSTRAINT_NAME FROM information_schema.TABLE_CONSTRAINTS
                         WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'review_request'
                           AND CONSTRAINT_NAME = 'FK3ak0tik0pid86c0tfrixbbga4' LIMIT 1);
SET @query := IF(@constraint_name IS NOT NULL, 'ALTER TABLE review_request DROP FOREIGN KEY FK3ak0tik0pid86c0tfrixbbga4', 'SELECT "ReviewRequest FK already gone"');
PREPARE stmt FROM @query; EXECUTE stmt; DEALLOCATE PREPARE stmt;

-- 2-2. Unique Key 제거
SET @index_name := (SELECT DISTINCT INDEX_NAME FROM information_schema.STATISTICS
                    WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'review_request'
                      AND INDEX_NAME = 'uk_review_request_post_user' LIMIT 1);
SET @query := IF(@index_name IS NOT NULL, 'ALTER TABLE review_request DROP INDEX uk_review_request_post_user', 'SELECT "ReviewRequest UK already gone"');
PREPARE stmt FROM @query; EXECUTE stmt; DEALLOCATE PREPARE stmt;

-- 2-3. post_id 삭제 및 party_id 추가
SET @col_exists := (SELECT COUNT(*) FROM information_schema.COLUMNS WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'review_request' AND COLUMN_NAME = 'post_id');
SET @query := IF(@col_exists > 0, 'ALTER TABLE review_request DROP COLUMN post_id', 'SELECT "post_id already gone from review_request"');
PREPARE stmt FROM @query; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @col_exists := (SELECT COUNT(*) FROM information_schema.COLUMNS WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'review_request' AND COLUMN_NAME = 'party_id');
SET @query := IF(@col_exists = 0, 'ALTER TABLE review_request ADD COLUMN party_id BIGINT NOT NULL', 'SELECT "party_id already exists in review_request"');
PREPARE stmt FROM @query; EXECUTE stmt; DEALLOCATE PREPARE stmt;

-- 2-4. 새로운 제약 조건 추가
ALTER TABLE review_request ADD CONSTRAINT fk_review_request_party FOREIGN KEY (party_id) REFERENCES party (party_id);
ALTER TABLE review_request ADD CONSTRAINT uk_review_request_party_user UNIQUE (party_id, request_user_id);