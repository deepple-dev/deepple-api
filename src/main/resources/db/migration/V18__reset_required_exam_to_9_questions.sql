-- #452 필수 과목 문항 15개 → 9문항 변경 + 답변 6유형 재매핑
-- 문항/유형 매핑이 바뀌므로 기존 제출/집계 결과를 초기화하고 전원 재응시를 유도한다.

SET
@now = NOW(6);

-- -----------------------------------------------------------------------------
-- 0) 기존 연애고사 데이터 리셋 (submit → answer → question → subject 순서)
-- -----------------------------------------------------------------------------
DELETE
FROM dating_exam_submit;
DELETE
FROM dating_exam_answer;
DELETE
FROM dating_exam_question;
DELETE
FROM dating_exam_subject;

-- 과거 집계 결과 및 제출 플래그 초기화 (과거 15문항·구 매핑 기준이라 무의미)
DELETE
FROM dating_exam_submit_result;
UPDATE members
SET is_dating_exam_submitted = FALSE;

-- -----------------------------------------------------------------------------
-- 1) 과목 4개 INSERT (모두 REQUIRED)
-- -----------------------------------------------------------------------------
INSERT INTO dating_exam_subject (name, type, is_public, created_at, updated_at)
VALUES ('가치관', 'REQUIRED', TRUE, @now, @now);
SET
@sub1 = LAST_INSERT_ID();

INSERT INTO dating_exam_subject (name, type, is_public, created_at, updated_at)
VALUES ('데이트', 'REQUIRED', TRUE, @now, @now);
SET
@sub2 = LAST_INSERT_ID();

INSERT INTO dating_exam_subject (name, type, is_public, created_at, updated_at)
VALUES ('취향', 'REQUIRED', TRUE, @now, @now);
SET
@sub3 = LAST_INSERT_ID();

INSERT INTO dating_exam_subject (name, type, is_public, created_at, updated_at)
VALUES ('결혼', 'REQUIRED', TRUE, @now, @now);
SET
@sub4 = LAST_INSERT_ID();

-- -----------------------------------------------------------------------------
-- 2) 가치관 과목 (3문항)
-- -----------------------------------------------------------------------------
INSERT INTO dating_exam_question (subject_id, content, created_at, updated_at)
VALUES (@sub1, '인생에서 가장 포기할 수 없는 가치는?', @now, @now);
SET
@q1 = LAST_INSERT_ID();
INSERT INTO dating_exam_answer (question_id, content, personality_type, created_at, updated_at)
VALUES (@q1, '사랑하는 사람과의 정서적 교감', 'DEVOTED_ROMANTIC', @now, @now),
       (@q1, '나만의 주관과 개인의 신념', 'DECISIVE_INDEPENDENT', @now, @now),
       (@q1, '안정적인 삶과 경제력', 'REALISTIC_SHELTER', @now, @now),
       (@q1, '끊임없는 자기계발과 성장', 'GROWING_RUNNING_MATE', @now, @now);

INSERT INTO dating_exam_question (subject_id, content, created_at, updated_at)
VALUES (@sub1, '연인이 3년간 해외 출장을 가게 되었다면?', @now, @now);
SET
@q2 = LAST_INSERT_ID();
INSERT INTO dating_exam_answer (question_id, content, personality_type, created_at, updated_at)
VALUES (@q2, '현실적인 미래를 위해 보내준다', 'REALISTIC_SHELTER', @now, @now),
       (@q2, '못 견딜 것 같아 가지 말라고 붙잡는다', 'DEVOTED_ROMANTIC', @now, @now),
       (@q2, '서로의 커리어를 응원하며 기다린다', 'GROWING_RUNNING_MATE', @now, @now),
       (@q2, '장거리 연애가 내 삶을 해친다면 쿨하게 이별', 'DECISIVE_INDEPENDENT', @now, @now);

INSERT INTO dating_exam_question (subject_id, content, created_at, updated_at)
VALUES (@sub1, '연인이 이성 친구와 만나는 것에 대한 생각은?', @now, @now);
SET
@q3 = LAST_INSERT_ID();
INSERT INTO dating_exam_answer (question_id, content, personality_type, created_at, updated_at)
VALUES (@q3, '사생활이므로 내가 관여할 영역이 아님', 'DECISIVE_INDEPENDENT', @now, @now),
       (@q3, '질투 나고 속상해서 최대한 안 했으면 함', 'DEVOTED_ROMANTIC', @now, @now),
       (@q3, '내 성장에 방해만 안 된다면 터치 안 함', 'GROWING_RUNNING_MATE', @now, @now),
       (@q3, '미리 말하고 적당한 선만 지키면 문제 없음', 'RATIONAL_REALIST', @now, @now);

-- -----------------------------------------------------------------------------
-- 3) 데이트 과목 (2문항)
-- -----------------------------------------------------------------------------
INSERT INTO dating_exam_question (subject_id, content, created_at, updated_at)
VALUES (@sub2, '가기 싫은 장소를 연인이 꼭 같이 가자고 한다면?', @now, @now);
SET
@q4 = LAST_INSERT_ID();
INSERT INTO dating_exam_answer (question_id, content, personality_type, created_at, updated_at)
VALUES (@q4, '가성비나 효율을 따져보고 대체안을 제안한다', 'REALISTIC_SHELTER', @now, @now),
       (@q4, '연인이 가고 싶은 이유를 듣고 경험 삼아 가본다', 'STIMULATING_ADVENTURER', @now, @now),
       (@q4, '연인이 기뻐하는 모습이 보고 싶어 기꺼이 간다', 'DEVOTED_ROMANTIC', @now, @now),
       (@q4, '내가 싫어하는 건 명확히 말하고 거절한다', 'DECISIVE_INDEPENDENT', @now, @now);

INSERT INTO dating_exam_question (subject_id, content, created_at, updated_at)
VALUES (@sub2, '우리 사이 연락 빈도, 어느 정도가 가장 좋을까?', @now, @now);
SET
@q5 = LAST_INSERT_ID();
INSERT INTO dating_exam_answer (question_id, content, personality_type, created_at, updated_at)
VALUES (@q5, '무조건 즉시 답장! 연결된 느낌이 중요', 'DEVOTED_ROMANTIC', @now, @now),
       (@q5, '3시간 미만, 일과 방해 안 되는 선', 'RATIONAL_REALIST', @now, @now),
       (@q5, '퇴근 후나 자기 전, 생존신고만 하면 됨', 'DECISIVE_INDEPENDENT', @now, @now),
       (@q5, '생산적인 시간 후, 틈틈이 근황 공유', 'GROWING_RUNNING_MATE', @now, @now);

-- -----------------------------------------------------------------------------
-- 4) 취향 과목 (2문항)
-- -----------------------------------------------------------------------------
INSERT INTO dating_exam_question (subject_id, content, created_at, updated_at)
VALUES (@sub3, '연인과 이것만큼은 꼭 맞았으면 하는 것은?', @now, @now);
SET
@q6 = LAST_INSERT_ID();
INSERT INTO dating_exam_answer (question_id, content, personality_type, created_at, updated_at)
VALUES (@q6, '미래를 그리는 생활 패턴과 연락 빈도', 'REALISTIC_SHELTER', @now, @now),
       (@q6, '서로의 영역을 인정하는 취향 존중', 'DECISIVE_INDEPENDENT', @now, @now),
       (@q6, '웃음 코드가 통하는 유머 감각', 'STIMULATING_ADVENTURER', @now, @now),
       (@q6, '함께 즐길 수 있는 자기계발적 취미', 'GROWING_RUNNING_MATE', @now, @now);

INSERT INTO dating_exam_question (subject_id, content, created_at, updated_at)
VALUES (@sub3, '연인과 대화할 때 내가 더 선호하는 주제는?', @now, @now);
SET
@q7 = LAST_INSERT_ID();
INSERT INTO dating_exam_answer (question_id, content, personality_type, created_at, updated_at)
VALUES (@q7, '각자의 전문 분야나 깊이 있는 철학', 'RATIONAL_REALIST', @now, @now),
       (@q7, '오늘 하루 느꼈던 감정과 서로의 기분', 'DEVOTED_ROMANTIC', @now, @now),
       (@q7, '재테크, 부동산 등 실질적인 미래 설계', 'REALISTIC_SHELTER', @now, @now),
       (@q7, '최근 읽은 책이나 새로운 지식 정보', 'GROWING_RUNNING_MATE', @now, @now);

-- -----------------------------------------------------------------------------
-- 5) 결혼 과목 (2문항)
-- -----------------------------------------------------------------------------
INSERT INTO dating_exam_question (subject_id, content, created_at, updated_at)
VALUES (@sub4, '배우자의 조건 중 가장 중요한 하나는?', @now, @now);
SET
@q8 = LAST_INSERT_ID();
INSERT INTO dating_exam_answer (question_id, content, personality_type, created_at, updated_at)
VALUES (@q8, '깊은 배려심과 따뜻한 성격', 'DEVOTED_ROMANTIC', @now, @now),
       (@q8, '나와 취향 또는 가치관이 얼마나 일치하는지', 'STIMULATING_ADVENTURER', @now, @now),
       (@q8, '세상을 대하는 태도와 성장 가능성', 'GROWING_RUNNING_MATE', @now, @now),
       (@q8, '경제력이나 직업적 능력', 'REALISTIC_SHELTER', @now, @now);

INSERT INTO dating_exam_question (subject_id, content, created_at, updated_at)
VALUES (@sub4, '나에게 결혼이란 인생에서 어떤 의미일까?', @now, @now);
SET
@q9 = LAST_INSERT_ID();
INSERT INTO dating_exam_answer (question_id, content, personality_type, created_at, updated_at)
VALUES (@q9, '비바람을 피해 쉴 수 있는 든든한 집', 'REALISTIC_SHELTER', @now, @now),
       (@q9, '함께 더 멀리 나아갈 수 있는 베이스캠프', 'GROWING_RUNNING_MATE', @now, @now),
       (@q9, '세상 끝까지 내 편이 되어줄 운명적 동행', 'DEVOTED_ROMANTIC', @now, @now),
       (@q9, '나의 삶을 풍요롭게 해줄 선택지 중 하나', 'DECISIVE_INDEPENDENT', @now, @now);
