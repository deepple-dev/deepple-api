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
VALUES (@sub1, '연인과 의견이 크게 다를 때, 나는 어떻게 하나요?', @now, @now);
SET
@q1 = LAST_INSERT_ID();
INSERT INTO dating_exam_answer (question_id, content, personality_type, created_at, updated_at)
VALUES (@q1, '상대방의 의견을 먼저 존중하고 맞춰준다', 'DEVOTED_ROMANTIC', @now, @now),
       (@q1, '내 의견을 명확히 전달하고 설득한다', 'DECISIVE_INDEPENDENT', @now, @now),
       (@q1, '현실적으로 더 나은 선택이 무엇인지 따져본다', 'REALISTIC_SHELTER', @now, @now),
       (@q1, '서로의 의견을 분석하고 합리적인 결론을 찾는다', 'GROWING_RUNNING_MATE', @now, @now);

INSERT INTO dating_exam_question (subject_id, content, created_at, updated_at)
VALUES (@sub1, '연애에서 가장 중요하게 생각하는 가치는?', @now, @now);
SET
@q2 = LAST_INSERT_ID();
INSERT INTO dating_exam_answer (question_id, content, personality_type, created_at, updated_at)
VALUES (@q2, '함께 성장하고 발전하는 관계', 'GROWING_RUNNING_MATE', @now, @now),
       (@q2, '안정적이고 편안한 일상 공유', 'REALISTIC_SHELTER', @now, @now),
       (@q2, '서로의 독립성과 개인 시간 존중', 'DECISIVE_INDEPENDENT', @now, @now),
       (@q2, '깊은 정서적 교감과 헌신', 'DEVOTED_ROMANTIC', @now, @now);

INSERT INTO dating_exam_question (subject_id, content, created_at, updated_at)
VALUES (@sub1, '연인이 나의 중요한 결정에 반대할 때?', @now, @now);
SET
@q3 = LAST_INSERT_ID();
INSERT INTO dating_exam_answer (question_id, content, personality_type, created_at, updated_at)
VALUES (@q3, '현실적인 장단점을 함께 비교해본다', 'REALISTIC_SHELTER', @now, @now),
       (@q3, '연인의 마음이 상하지 않도록 조율한다', 'DEVOTED_ROMANTIC', @now, @now),
       (@q3, '상대의 반대 이유를 듣고 더 나은 방향을 함께 모색한다', 'GROWING_RUNNING_MATE', @now, @now),
       (@q3, '내 결정을 밀고 나간다', 'DECISIVE_INDEPENDENT', @now, @now);

INSERT INTO dating_exam_question (subject_id, content, created_at, updated_at)
VALUES (@sub1, '이상적인 커플의 모습은?', @now, @now);
SET
@q4 = LAST_INSERT_ID();
INSERT INTO dating_exam_answer (question_id, content, personality_type, created_at, updated_at)
VALUES (@q4, '각자의 삶을 존중하면서 함께하는 사이', 'DECISIVE_INDEPENDENT', @now, @now),
       (@q4, '어떤 상황에서도 서로를 최우선으로 생각하는 사이', 'DEVOTED_ROMANTIC', @now, @now),
       (@q4, '오래도록 편안하고 안정적인 사이', 'REALISTIC_SHELTER', @now, @now),
       (@q4, '서로에게 좋은 영향을 주며 함께 발전하는 사이', 'GROWING_RUNNING_MATE', @now, @now);

-- 데이트 과목 문항 & 답변
INSERT INTO dating_exam_question (subject_id, content, created_at, updated_at)
VALUES (@sub2, '이상적인 데이트는?', @now, @now);
SET
@q5 = LAST_INSERT_ID();
INSERT INTO dating_exam_answer (question_id, content, personality_type, created_at, updated_at)
VALUES (@q5, '집에서 편하게 함께 쉬는 데이트', 'REALISTIC_SHELTER', @now, @now),
       (@q5, '새로운 것을 함께 배우거나 체험하는 데이트', 'GROWING_RUNNING_MATE', @now, @now),
       (@q5, '각자 하고 싶은 것을 하다가 합류하는 데이트', 'DECISIVE_INDEPENDENT', @now, @now),
       (@q5, '분위기 좋은 곳에서 둘만의 시간을 보내는 데이트', 'DEVOTED_ROMANTIC', @now, @now);

INSERT INTO dating_exam_question (subject_id, content, created_at, updated_at)
VALUES (@sub2, '데이트 비용은 어떻게 하는 게 좋을까요?', @now, @now);
SET
@q6 = LAST_INSERT_ID();
INSERT INTO dating_exam_answer (question_id, content, personality_type, created_at, updated_at)
VALUES (@q6, '내가 더 많이 내더라도 상대를 기쁘게 해주고 싶다', 'DEVOTED_ROMANTIC', @now, @now),
       (@q6, '각자 부담하는 것이 편하다', 'DECISIVE_INDEPENDENT', @now, @now),
       (@q6, '번갈아가며 내거나 상황에 맞게 유연하게', 'GROWING_RUNNING_MATE', @now, @now),
       (@q6, '수입과 지출을 고려해 합리적으로 분담', 'REALISTIC_SHELTER', @now, @now);

INSERT INTO dating_exam_question (subject_id, content, created_at, updated_at)
VALUES (@sub2, '연인과의 연락 빈도는?', @now, @now);
SET
@q7 = LAST_INSERT_ID();
INSERT INTO dating_exam_answer (question_id, content, personality_type, created_at, updated_at)
VALUES (@q7, '하루에 몇 번 서로의 근황을 공유한다', 'GROWING_RUNNING_MATE', @now, @now),
       (@q7, '정해진 시간에 규칙적으로 연락한다', 'REALISTIC_SHELTER', @now, @now),
       (@q7, '수시로 연락하며 항상 연결되어 있고 싶다', 'DEVOTED_ROMANTIC', @now, @now),
       (@q7, '필요할 때만 연락하면 된다', 'DECISIVE_INDEPENDENT', @now, @now);

INSERT INTO dating_exam_question (subject_id, content, created_at, updated_at)
VALUES (@sub2, '기념일에 대한 생각은?', @now, @now);
SET
@q8 = LAST_INSERT_ID();
INSERT INTO dating_exam_answer (question_id, content, personality_type, created_at, updated_at)
VALUES (@q8, '특별히 챙기지 않아도 괜찮다', 'DECISIVE_INDEPENDENT', @now, @now),
       (@q8, '부담 없이 맛있는 것 먹으며 소소하게 챙긴다', 'REALISTIC_SHELTER', @now, @now),
       (@q8, '의미 있는 경험을 함께 하는 것이 중요하다', 'GROWING_RUNNING_MATE', @now, @now),
       (@q8, '깜짝 이벤트와 선물로 특별하게 보내고 싶다', 'DEVOTED_ROMANTIC', @now, @now);

-- 취향 과목 문항 & 답변
INSERT INTO dating_exam_question (subject_id, content, created_at, updated_at)
VALUES (@sub3, '주말에 가장 하고 싶은 것은?', @now, @now);
SET
@q9 = LAST_INSERT_ID();
INSERT INTO dating_exam_answer (question_id, content, personality_type, created_at, updated_at)
VALUES (@q9, '자기계발이나 새로운 도전하기', 'GROWING_RUNNING_MATE', @now, @now),
       (@q9, '연인과 로맨틱한 시간 보내기', 'DEVOTED_ROMANTIC', @now, @now),
       (@q9, '집에서 편하게 쉬기', 'REALISTIC_SHELTER', @now, @now),
       (@q9, '혼자만의 취미 활동에 집중하기', 'DECISIVE_INDEPENDENT', @now, @now);

INSERT INTO dating_exam_question (subject_id, content, created_at, updated_at)
VALUES (@sub3, '여행 스타일은?', @now, @now);
SET
@q10 = LAST_INSERT_ID();
INSERT INTO dating_exam_answer (question_id, content, personality_type, created_at, updated_at)
VALUES (@q10, '편하고 안전한 곳에서 느긋하게', 'REALISTIC_SHELTER', @now, @now),
       (@q10, '각자 가고 싶은 곳을 정해서 자유롭게', 'DECISIVE_INDEPENDENT', @now, @now),
       (@q10, '둘만의 로맨틱한 여행지에서 힐링', 'DEVOTED_ROMANTIC', @now, @now),
       (@q10, '새로운 문화와 경험을 탐험하는 여행', 'GROWING_RUNNING_MATE', @now, @now);

INSERT INTO dating_exam_question (subject_id, content, created_at, updated_at)
VALUES (@sub3, '선물을 고를 때 기준은?', @now, @now);
SET
@q11 = LAST_INSERT_ID();
INSERT INTO dating_exam_answer (question_id, content, personality_type, created_at, updated_at)
VALUES (@q11, '감동을 줄 수 있는 의미 있는 것', 'DEVOTED_ROMANTIC', @now, @now),
       (@q11, '상대방의 성장에 도움이 되는 것', 'GROWING_RUNNING_MATE', @now, @now),
       (@q11, '실용적이고 쓸모 있는 것', 'DECISIVE_INDEPENDENT', @now, @now),
       (@q11, '부담 없으면서 센스 있는 것', 'REALISTIC_SHELTER', @now, @now);

INSERT INTO dating_exam_question (subject_id, content, created_at, updated_at)
VALUES (@sub3, '좋아하는 대화 주제는?', @now, @now);
SET
@q12 = LAST_INSERT_ID();
INSERT INTO dating_exam_answer (question_id, content, personality_type, created_at, updated_at)
VALUES (@q12, '각자의 관심사와 목표에 대한 이야기', 'DECISIVE_INDEPENDENT', @now, @now),
       (@q12, '일상적이고 편안한 수다', 'REALISTIC_SHELTER', @now, @now),
       (@q12, '서로의 꿈과 미래 계획', 'GROWING_RUNNING_MATE', @now, @now),
       (@q12, '서로의 감정과 하루 이야기', 'DEVOTED_ROMANTIC', @now, @now);

-- 결혼 과목 문항 & 답변
INSERT INTO dating_exam_question (subject_id, content, created_at, updated_at)
VALUES (@sub4, '결혼 후 가장 중요한 것은?', @now, @now);
SET
@q13 = LAST_INSERT_ID();
INSERT INTO dating_exam_answer (question_id, content, personality_type, created_at, updated_at)
VALUES (@q13, '경제적 안정과 편안한 가정', 'REALISTIC_SHELTER', @now, @now),
       (@q13, '변함없는 사랑과 헌신', 'DEVOTED_ROMANTIC', @now, @now),
       (@q13, '부부가 함께 성장해 나가는 것', 'GROWING_RUNNING_MATE', @now, @now),
       (@q13, '서로의 개인 생활을 존중하는 것', 'DECISIVE_INDEPENDENT', @now, @now);

INSERT INTO dating_exam_question (subject_id, content, created_at, updated_at)
VALUES (@sub4, '맞벌이와 외벌이에 대한 생각은?', @now, @now);
SET
@q14 = LAST_INSERT_ID();
INSERT INTO dating_exam_answer (question_id, content, personality_type, created_at, updated_at)
VALUES (@q14, '서로의 상황에 맞게 유연하게 결정한다', 'GROWING_RUNNING_MATE', @now, @now),
       (@q14, '각자 커리어를 유지하는 것이 중요하다', 'DECISIVE_INDEPENDENT', @now, @now),
       (@q14, '경제적으로 더 합리적인 선택을 한다', 'REALISTIC_SHELTER', @now, @now),
       (@q14, '가정에 더 집중하고 싶은 쪽을 지지한다', 'DEVOTED_ROMANTIC', @now, @now);

INSERT INTO dating_exam_question (subject_id, content, created_at, updated_at)
VALUES (@sub4, '자녀 계획에 대한 생각은?', @now, @now);
SET
@q15 = LAST_INSERT_ID();
INSERT INTO dating_exam_answer (question_id, content, personality_type, created_at, updated_at)
VALUES (@q15, '사랑의 결실로 가정을 꾸리고 싶다', 'DEVOTED_ROMANTIC', @now, @now),
       (@q15, '현실적인 여건을 고려해 신중히 결정하고 싶다', 'REALISTIC_SHELTER', @now, @now),
       (@q15, '개인의 삶과 자유를 우선시하고 싶다', 'DECISIVE_INDEPENDENT', @now, @now),
       (@q15, '아이를 통해 함께 성장하는 경험을 하고 싶다', 'GROWING_RUNNING_MATE', @now, @now);

INSERT INTO dating_exam_question (subject_id, content, created_at, updated_at)
VALUES (@sub4, '시댁/처가와의 관계에서 중요한 것은?', @now, @now);
SET
@q16 = LAST_INSERT_ID();
INSERT INTO dating_exam_answer (question_id, content, personality_type, created_at, updated_at)
VALUES (@q16, '적당한 거리감을 유지하는 것', 'DECISIVE_INDEPENDENT', @now, @now),
       (@q16, '서로 이해하고 좋은 관계를 만들어가는 것', 'GROWING_RUNNING_MATE', @now, @now),
       (@q16, '명절과 경조사 등 기본적인 예의를 지키는 것', 'REALISTIC_SHELTER', @now, @now),
       (@q16, '배우자의 가족도 내 가족처럼 챙기는 것', 'DEVOTED_ROMANTIC', @now, @now);
