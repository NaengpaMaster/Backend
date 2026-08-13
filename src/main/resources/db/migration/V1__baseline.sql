-- Existing RDS schema baseline.
-- 현재 RDS DB에는 1차 프로젝트 기준 테이블이 이미 생성되어 있으므로,
-- V1에서는 새 테이블/컬럼을 만들지 않고 기준점만 남긴다.

-- Flyway 사용 규칙
-- 1. DB 변경이 필요한 경우 이 폴더에 새 migration 파일을 추가한다.
-- 2. 파일명은 V번호__작업내용.sql 형식을 사용한다.
--    예: V2__add_default_expiry_days_to_products.sql
--    예: V3__create_coupon_table.sql
-- 3. 이미 dev에 머지된 migration 파일은 수정하지 않는다.
-- 4. 기존 migration 수정이 필요하면 새 번호로 추가 migration을 만든다.
-- 5. 여러 명이 동시에 작업해 번호가 겹치면, 늦게 머지하는 사람이 번호를 바꾼다.

-- New schema changes start from V2.
