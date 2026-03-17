SET
@now = NOW(6);

-- 과목 4개 INSERT (모두 REQUIRED)
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

-- 가치관 과목 문항 & 답변
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
VALUES (@sub1, '연애를 유지하는 데 가장 필수적인 감정은?', @now, @now);
SET
@q2 = LAST_INSERT_ID();
INSERT INTO dating_exam_answer (question_id, content, personality_type, created_at, updated_at)
VALUES (@q2, '서로를 향한 존경심', 'GROWING_RUNNING_MATE', @now, @now),
       (@q2, '평온함과 안정감', 'REALISTIC_SHELTER', @now, @now),
       (@q2, '적당한 거리감과 존중', 'DECISIVE_INDEPENDENT', @now, @now),
       (@q2, '뜨거운 설렘과 깊은 배려', 'DEVOTED_ROMANTIC', @now, @now);

INSERT INTO dating_exam_question (subject_id, content, created_at, updated_at)
VALUES (@sub1, '연인이 3년간 해외 출장을 가게 되었다면?', @now, @now);
SET
@q3 = LAST_INSERT_ID();
INSERT INTO dating_exam_answer (question_id, content, personality_type, created_at, updated_at)
VALUES (@q3, '현실적인 미래를 위해 보내준다', 'REALISTIC_SHELTER', @now, @now),
       (@q3, '못 견딜 것 같아 가지 말라고 붙잡는다', 'DEVOTED_ROMANTIC', @now, @now),
       (@q3, '서로의 커리어를 응원하며 기다린다', 'GROWING_RUNNING_MATE', @now, @now),
       (@q3, '장거리 연애가 내 삶을 해친다면 쿨하게 이별', 'DECISIVE_INDEPENDENT', @now, @now);

INSERT INTO dating_exam_question (subject_id, content, created_at, updated_at)
VALUES (@sub1, '연인이 이성 친구와 만나는 것에 대한 생각은?', @now, @now);
SET
@q4 = LAST_INSERT_ID();
INSERT INTO dating_exam_answer (question_id, content, personality_type, created_at, updated_at)
VALUES (@q4, '사생활이므로 내가 관여할 영역이 아님', 'DECISIVE_INDEPENDENT', @now, @now),
       (@q4, '질투 나고 속상해서 최대한 안 했으면 함', 'DEVOTED_ROMANTIC', @now, @now),
       (@q4, '내 성장에 방해만 안 된다면 터치 안 함', 'GROWING_RUNNING_MATE', @now, @now),
       (@q4, '미리 말하고 적당한 선만 지키면 문제 없음', 'REALISTIC_SHELTER', @now, @now);

-- 데이트 과목 문항 & 답변
INSERT INTO dating_exam_question (subject_id, content, created_at, updated_at)
VALUES (@sub2, '선호하는 데이트 횟수와 시간은?', @now, @now);
SET
@q5 = LAST_INSERT_ID();
INSERT INTO dating_exam_answer (question_id, content, personality_type, created_at, updated_at)
VALUES (@q5, '서로의 일정에 맞춰 효율적으로 조율', 'GROWING_RUNNING_MATE', @now, @now),
       (@q5, '각자의 개인 시간이 보장되는 월 4회 미만', 'DECISIVE_INDEPENDENT', @now, @now),
       (@q5, '경제적 혹은 체력적 부담이 없는 주 1~2회', 'REALISTIC_SHELTER', @now, @now),
       (@q5, '보고 싶을 때마다! 주 3회 이상도 가능', 'DEVOTED_ROMANTIC', @now, @now);

INSERT INTO dating_exam_question (subject_id, content, created_at, updated_at)
VALUES (@sub2, '가기 싫은 장소를 연인이 꼭 같이 가자고 한다면?', @now, @now);
SET
@q6 = LAST_INSERT_ID();
INSERT INTO dating_exam_answer (question_id, content, personality_type, created_at, updated_at)
VALUES (@q6, '가성비나 효율을 따져보고 대체안을 제안한다', 'REALISTIC_SHELTER', @now, @now),
       (@q6, '연인이 가고 싶은 이유를 듣고 경험 삼아 가본다', 'GROWING_RUNNING_MATE', @now, @now),
       (@q6, '연인이 기뻐하는 모습이 보고 싶어 기꺼이 간다', 'DEVOTED_ROMANTIC', @now, @now),
       (@q6, '내가 싫어하는 건 명확히 말하고 거절한다', 'DECISIVE_INDEPENDENT', @now, @now);

INSERT INTO dating_exam_question (subject_id, content, created_at, updated_at)
VALUES (@sub2, '우리 사이 연락 빈도, 어느 정도가 가장 좋을까?', @now, @now);
SET
@q7 = LAST_INSERT_ID();
INSERT INTO dating_exam_answer (question_id, content, personality_type, created_at, updated_at)
VALUES (@q7, '무조건 즉시 답장! 연결된 느낌이 중요', 'DEVOTED_ROMANTIC', @now, @now),
       (@q7, '3시간 미만, 일과 방해 안 되는 선', 'REALISTIC_SHELTER', @now, @now),
       (@q7, '퇴근 후나 자기 전, 생존신고만 하면 됨', 'DECISIVE_INDEPENDENT', @now, @now),
       (@q7, '생산적인 시간 후, 틈틈이 근황 공유', 'GROWING_RUNNING_MATE', @now, @now);

INSERT INTO dating_exam_question (subject_id, content, created_at, updated_at)
VALUES (@sub2, '하루 데이트 비용, 어느 정도가 적당할까?', @now, @now);
SET
@q8 = LAST_INSERT_ID();
INSERT INTO dating_exam_answer (question_id, content, personality_type, created_at, updated_at)
VALUES (@q8, '서로 부담 없는 선에서 각자 더치페이', 'DECISIVE_INDEPENDENT', @now, @now),
       (@q8, '가계에 부담 없는 5만원 내외', 'REALISTIC_SHELTER', @now, @now),
       (@q8, '미래를 위한 저축과 즐거움 사이 적정선', 'GROWING_RUNNING_MATE', @now, @now),
       (@q8, '연인이 좋아한다면 아끼지 않고 지출', 'DEVOTED_ROMANTIC', @now, @now);

-- 취향 과목 문항 & 답변
INSERT INTO dating_exam_question (subject_id, content, created_at, updated_at)
VALUES (@sub3, '연인에게 선물 받는다면 무엇이 가장 좋을까?', @now, @now);
SET
@q9 = LAST_INSERT_ID();
INSERT INTO dating_exam_answer (question_id, content, personality_type, created_at, updated_at)
VALUES (@q9, '내 성장에 도움되는 책', 'GROWING_RUNNING_MATE', @now, @now),
       (@q9, '정성과 마음이 가득 담긴 편지', 'DEVOTED_ROMANTIC', @now, @now),
       (@q9, '오래 쓸 수 있는 실용적인 선물', 'REALISTIC_SHELTER', @now, @now),
       (@q9, '취향 타지 않는 깔끔한 기프트카드', 'DECISIVE_INDEPENDENT', @now, @now);

INSERT INTO dating_exam_question (subject_id, content, created_at, updated_at)
VALUES (@sub3, '연인과 이것만큼은 꼭 맞았으면 하는 것은?', @now, @now);
SET
@q10 = LAST_INSERT_ID();
INSERT INTO dating_exam_answer (question_id, content, personality_type, created_at, updated_at)
VALUES (@q10, '미래를 그리는 생활 패턴과 연락 빈도', 'REALISTIC_SHELTER', @now, @now),
       (@q10, '서로의 영역을 인정하는 취향 존중', 'DECISIVE_INDEPENDENT', @now, @now),
       (@q10, '웃음 코드가 통하는 유머 감각', 'DEVOTED_ROMANTIC', @now, @now),
       (@q10, '함께 즐길 수 있는 자기계발적 취미', 'GROWING_RUNNING_MATE', @now, @now);

INSERT INTO dating_exam_question (subject_id, content, created_at, updated_at)
VALUES (@sub3, '연인과 카페에 간다면 내가 선택할 자리는?', @now, @now);
SET
@q11 = LAST_INSERT_ID();
INSERT INTO dating_exam_answer (question_id, content, personality_type, created_at, updated_at)
VALUES (@q11, '서로 나란히 앉아 스킨십하기 좋은 자리', 'DEVOTED_ROMANTIC', @now, @now),
       (@q11, '노트북을 하거나 독서하기 좋은 자리', 'GROWING_RUNNING_MATE', @now, @now),
       (@q11, '구석진 곳이나 창밖을 보는 독립된 자리', 'DECISIVE_INDEPENDENT', @now, @now),
       (@q11, '대화하기 편하고 의자가 푹신한 자리', 'REALISTIC_SHELTER', @now, @now);

INSERT INTO dating_exam_question (subject_id, content, created_at, updated_at)
VALUES (@sub3, '연인과 대화할 때 내가 더 선호하는 주제는?', @now, @now);
SET
@q12 = LAST_INSERT_ID();
INSERT INTO dating_exam_answer (question_id, content, personality_type, created_at, updated_at)
VALUES (@q12, '각자의 전문 분야나 깊이 있는 철학', 'DECISIVE_INDEPENDENT', @now, @now),
       (@q12, '오늘 하루 느꼈던 감정과 서로의 기분', 'DEVOTED_ROMANTIC', @now, @now),
       (@q12, '재테크, 부동산 등 실질적인 미래 설계', 'REALISTIC_SHELTER', @now, @now),
       (@q12, '최근 읽은 책이나 새로운 지식 정보', 'GROWING_RUNNING_MATE', @now, @now);

-- 결혼 과목 문항 & 답변
INSERT INTO dating_exam_question (subject_id, content, created_at, updated_at)
VALUES (@sub4, '내가 꿈꾸는 신혼여행 장소는?', @now, @now);
SET
@q13 = LAST_INSERT_ID();
INSERT INTO dating_exam_answer (question_id, content, personality_type, created_at, updated_at)
VALUES (@q13, '새로운 경험을 할 수 있는 유럽 배낭여행', 'GROWING_RUNNING_MATE', @now, @now),
       (@q13, '가성비와 휴식을 모두 잡는 동남아', 'REALISTIC_SHELTER', @now, @now),
       (@q13, '계획 없이 발길 닿는 대로 떠나는 여행', 'DECISIVE_INDEPENDENT', @now, @now),
       (@q13, '오로지 우리 둘만 아는 로맨틱한 섬', 'DEVOTED_ROMANTIC', @now, @now);

INSERT INTO dating_exam_question (subject_id, content, created_at, updated_at)
VALUES (@sub4, '배우자의 조건 중 가장 중요한 하나는?', @now, @now);
SET
@q14 = LAST_INSERT_ID();
INSERT INTO dating_exam_answer (question_id, content, personality_type, created_at, updated_at)
VALUES (@q14, '깊은 배려심과 따뜻한 성격', 'DEVOTED_ROMANTIC', @now, @now),
       (@q14, '나와 취향 또는 가치관이 얼마나 일치하는지', 'DECISIVE_INDEPENDENT', @now, @now),
       (@q14, '세상을 대하는 태도와 성장 가능성', 'GROWING_RUNNING_MATE', @now, @now),
       (@q14, '경제력이나 직업적 능력', 'REALISTIC_SHELTER', @now, @now);

INSERT INTO dating_exam_question (subject_id, content, created_at, updated_at)
VALUES (@sub4, '나에게 결혼이란 인생에서 어떤 의미일까?', @now, @now);
SET
@q15 = LAST_INSERT_ID();
INSERT INTO dating_exam_answer (question_id, content, personality_type, created_at, updated_at)
VALUES (@q15, '비바람을 피해 쉴 수 있는 든든한 집', 'REALISTIC_SHELTER', @now, @now),
       (@q15, '함께 더 멀리 나아갈 수 있는 베이스캠프', 'GROWING_RUNNING_MATE', @now, @now),
       (@q15, '세상 끝까지 내 편이 되어줄 운명적 동행', 'DEVOTED_ROMANTIC', @now, @now),
       (@q15, '나의 삶을 풍요롭게 해줄 선택지 중 하나', 'DECISIVE_INDEPENDENT', @now, @now);