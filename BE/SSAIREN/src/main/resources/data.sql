-- =============================================
-- 테스트 데이터 초기화 스크립트
-- JPA가 테이블을 자동 생성한 후 실행됩니다.
-- =============================================
-- 1. 기존 데이터 삭제
TRUNCATE TABLE paramedics CASCADE;
TRUNCATE TABLE fire_states CASCADE;

-- 2. 시퀀스 초기화
ALTER SEQUENCE paramedics_id_seq RESTART WITH 1;
ALTER SEQUENCE fire_states_id_seq RESTART WITH 1;

-- 1. 소방서 데이터 삽입 (서울시 25개 소방서)
INSERT INTO fire_states (name) VALUES
('강남소방서'),
('강동소방서'),
('강북소방서'),
('강서소방서'),
('관악소방서'),
('광진소방서'),
('구로소방서'),
('금천소방서'),
('노원소방서'),
('도봉소방서'),
('동대문소방서'),
('동작소방서'),
('마포소방서'),
('서대문소방서'),
('서초소방서'),
('성동소방서'),
('성북소방서'),
('송파소방서'),
('양천소방서'),
('영등포소방서'),
('용산소방서'),
('은평소방서'),
('종로소방서'),
('중부소방서'),
('중랑소방서');

-- 2. 구급대원 데이터 삽입
-- 비밀번호: Password123! (BCrypt 암호화됨)
-- rank: 소방사, 소방교, 소방장, 소방위 등 (한글)
-- status: ACTIVE, INACTIVE, ON_DUTY, OFF_DUTY
INSERT INTO paramedics (fire_state_id, student_number, password, name, rank, status, created_at, updated_at) VALUES
(1, '20240001', '$2a$10$Jew95TQpanH3116uIBeOGOWMaWUv1oZe2PoJBmjYhmUzsXcdI1DDO', '최상인', '소방총감', 'ACTIVE', NOW(), NOW()),
(1, '20240002', '$2a$10$Jew95TQpanH3116uIBeOGOWMaWUv1oZe2PoJBmjYhmUzsXcdI1DDO', '이영희', '소방교', 'ACTIVE', NOW(), NOW()),
(2, '20240003', '$2a$10$Jew95TQpanH3116uIBeOGOWMaWUv1oZe2PoJBmjYhmUzsXcdI1DDO', '박민수', '소방장', 'ON_DUTY', NOW(), NOW()),
(3, '20240004', '$2a$10$Jew95TQpanH3116uIBeOGOWMaWUv1oZe2PoJBmjYhmUzsXcdI1DDO', '최지훈', '소방사', 'INACTIVE', NOW(), NOW());

-- 3. 병원 데이터 삽입
-- 비밀번호: Password123! (BCrypt 암호화됨) - 구급대원과 동일한 비밀번호 사용
-- name: 병원 이름 (간단한 이름)
-- official_name: 병원 실제 이름 (정식 명칭)
INSERT INTO hospitals (name, official_name, password, created_at, updated_at) VALUES
('서울대병원', '서울대학교병원', '$2a$10$Jew95TQpanH3116uIBeOGOWMaWUv1oZe2PoJBmjYhmUzsXcdI1DDO', NOW(), NOW()),
('삼성서울병원', '삼성서울병원', '$2a$10$Jew95TQpanH3116uIBeOGOWMaWUv1oZe2PoJBmjYhmUzsXcdI1DDO', NOW(), NOW()),
('서울아산병원', '서울아산병원', '$2a$10$Jew95TQpanH3116uIBeOGOWMaWUv1oZe2PoJBmjYhmUzsXcdI1DDO', NOW(), NOW()),
('강남세브란스병원', '연세대학교 강남세브란스병원', '$2a$10$Jew95TQpanH3116uIBeOGOWMaWUv1oZe2PoJBmjYhmUzsXcdI1DDO', NOW(), NOW());