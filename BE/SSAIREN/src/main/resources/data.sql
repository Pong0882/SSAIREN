-- =============================================
-- Spring Boot 초기 데이터 (JPA 테이블 생성 후 실행)
-- =============================================

-- 1. 소방서 데이터
INSERT INTO fire_states (name) VALUES
('서울중앙소방서'),
('부산해운대소방서'),
('대구수성소방서'),
('인천남동소방서');

-- 2. 구급대원 데이터
-- 비밀번호: Password123! (평문)
-- rank: 소방사, 소방교, 소방장, 소방위 등 (한글)
-- status: ACTIVE, INACTIVE, ON_DUTY, OFF_DUTY
INSERT INTO paramedics (fire_state_id, student_number, password, name, rank, status, created_at, updated_at) VALUES
(1, '20240001', 'Password123!', '김철수', '소방사', 'ACTIVE', NOW(), NOW()),
(1, '20240002', 'Password123!', '이영희', '소방교', 'ACTIVE', NOW(), NOW()),
(2, '20240003', 'Password123!', '박민수', '소방장', 'ON_DUTY', NOW(), NOW()),
(3, '20240004', 'Password123!', '최지훈', '소방사', 'INACTIVE', NOW(), NOW());