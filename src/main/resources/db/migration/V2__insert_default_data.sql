-- 인터뷰 질문
INSERT INTO interview_questions (created_at, updated_at, category, content, is_public)
VALUES (NOW(6), NOW(6), 'PERSONAL', '내가 생각하는 나의 장점과 단점', 1),
       (NOW(6), NOW(6), 'PERSONAL', '현재 하고 있는 일', 1),
       (NOW(6), NOW(6), 'PERSONAL', '나의 평일/주말 생활 패턴', 1),
       (NOW(6), NOW(6), 'PERSONAL', '일상에서 느끼는 나의 소소한 행복', 1),
       (NOW(6), NOW(6), 'PERSONAL', '내가 생각하는 나의 반전 매력', 1),
       (NOW(6), NOW(6), 'PERSONAL', '나만의 스트레스 해소 방법', 1),
       (NOW(6), NOW(6), 'PERSONAL', '좋아하는 음식/싫어하는 음식', 1),
       (NOW(6), NOW(6), 'PERSONAL', '배우고 싶은 분야/언어', 1),
       (NOW(6), NOW(6), 'PERSONAL', '10년 후 나의 모습', 1),
       (NOW(6), NOW(6), 'PERSONAL', '요즘 하고 있는 자기관리', 1),
       (NOW(6), NOW(6), 'PERSONAL', '내 삶의 우선 순위(나, 가족, 연인, 친구, 커리어 등)', 1),

       (NOW(6), NOW(6), 'SOCIAL', '남사친/여사친에 대한 나의 생각', 1),
       (NOW(6), NOW(6), 'SOCIAL', '나의 인간관계 스타일', 1),
       (NOW(6), NOW(6), 'SOCIAL', '호감을 느끼는 사람의 유형/타입', 1),
       (NOW(6), NOW(6), 'SOCIAL', '인간관계에서 가장 중요한 것', 1),
       (NOW(6), NOW(6), 'SOCIAL', '썸에 대한 나의 생각(기준, 적정 기간, 스킨십)', 1),
       (NOW(6), NOW(6), 'SOCIAL', '사랑하는 사람이 생겼을 때 결혼에 대한 고려', 1),
       (NOW(6), NOW(6), 'SOCIAL', '내 주변의 이성 친구들', 1),
       (NOW(6), NOW(6), 'SOCIAL', '나는 인싸?/아싸?', 1),
       (NOW(6), NOW(6), 'SOCIAL', '매일 연락하고 지내는 사람의 수', 1),
       (NOW(6), NOW(6), 'SOCIAL', '주변 사람들이 말하는 나의 모습', 1),

       (NOW(6), NOW(6), 'ROMANTIC', '추구하는 만남 횟수와 연락 빈도', 1),
       (NOW(6), NOW(6), 'ROMANTIC', '내가 연인을 사랑하는 방식', 1),
       (NOW(6), NOW(6), 'ROMANTIC', '연인에게 바라는 점', 1),
       (NOW(6), NOW(6), 'ROMANTIC', '연인 관계에서 가장 중요하다고 생각하는 것', 1),
       (NOW(6), NOW(6), 'ROMANTIC', '선호하는 데이트 스타일', 1),
       (NOW(6), NOW(6), 'ROMANTIC', '연인 사이의 갈등 해소 방법', 1),
       (NOW(6), NOW(6), 'ROMANTIC', '연인에게만 보여줄 수 있는 나의 모습', 1),
       (NOW(6), NOW(6), 'ROMANTIC', '연인을 위해 내가 할 수 있는 것', 1),
       (NOW(6), NOW(6), 'ROMANTIC', '연인에게 들려주고 싶은 노래', 1),
       (NOW(6), NOW(6), 'ROMANTIC', '연인과 함께 가보고 싶은 장소', 1);

-- 미션
INSERT INTO missions (created_at, updated_at, action_type, frequency_type, target_gender, required_attempt,
                      repeatable_count, rewarded_heart, is_public)
VALUES (NOW(6), NOW(6), 'LIKE', 'DAILY', 'MALE', 1, 2, 2, 1),
       (NOW(6), NOW(6), 'LIKE', 'DAILY', 'FEMALE', 1, 3, 2, 1),
       (NOW(6), NOW(6), 'INTERVIEW', 'CHALLENGE', 'ALL', 1, 1, 30, 1),
       (NOW(6), NOW(6), 'FIRST_DATE_EXAM', 'CHALLENGE', 'ALL', 1, 1, 15, 1),
       (NOW(6), NOW(6), 'SELF_INTRODUCTION', 'CHALLENGE', 'ALL', 1, 1, 100, 1);

-- 하트 사용 정책
INSERT INTO heart_usage_policies (created_at, updated_at, gender, price, transaction_type)
VALUES (NOW(6), NOW(6), 'MALE', 10, 'INTRODUCTION'),
       (NOW(6), NOW(6), 'FEMALE', 4, 'INTRODUCTION'),
       (NOW(6), NOW(6), 'MALE', 20, 'MESSAGE'),
       (NOW(6), NOW(6), 'FEMALE', 10, 'MESSAGE'),
       (NOW(6), NOW(6), 'MALE', 3, 'PROFILE_EXCHANGE'),
       (NOW(6), NOW(6), 'FEMALE', 3, 'PROFILE_EXCHANGE'),
       (NOW(6), NOW(6), 'MALE', 20, 'MESSAGE_ACCEPTED'),
       (NOW(6), NOW(6), 'FEMALE', 10, 'MESSAGE_ACCEPTED');

-- 하트 구매 옵션
INSERT INTO heart_purchase_options (created_at, updated_at, deleted_at, amount, name, price, product_id)
VALUES (NOW(6), NOW(6), NULL, 45, '45하트', 9900, 'APP_ITEM_HEART_45'),
       (NOW(6), NOW(6), NULL, 90, '90하트', 15000, 'APP_ITEM_HEART_90'),
       (NOW(6), NOW(6), NULL, 110, '110하트', 22000, 'APP_ITEM_HEART_110'),
       (NOW(6), NOW(6), NULL, 350, '350하트', 59000, 'APP_ITEM_HEART_350'),
       (NOW(6), NOW(6), NULL, 550, '550하트', 88000, 'APP_ITEM_HEART_550');

-- 알림 템플릿
INSERT INTO notification_templates (type, title_template, body_template, is_active)
VALUES ('MATCH_REQUEST', '매치 요청', '{senderName}님에게 메시지가 도착하였습니다.', 1),
       ('MATCH_ACCEPT', '매치 수락', '{senderName}님과 매칭되었습니다. 축하합니다!', 1),
       ('MATCH_REJECT', '매치 거절', '{senderName}님이 데이트 신청을 거절하셨습니다.', 1),
       ('PROFILE_EXCHANGE_REQUEST', '프로필 교환 요청', '{senderName}님이 프로필 교환을 요청하였습니다.', 1),
       ('PROFILE_EXCHANGE_ACCEPT', '프로필 교환 수락', '{senderName}님이 프로필 교환을 수락하셨습니다!', 1),
       ('PROFILE_EXCHANGE_REJECT', '프로필 교환 거절', '{senderName}님이 프로필 교환을 거절하셨습니다.', 1),
       ('LIKE', '좋아요', '{senderName}님이 회원님의 프로필을 좋아합니다.', 1),
       ('PROFILE_IMAGE_CHANGE_REQUEST', '프로필 이미지 변경 요청', '나를 더 잘 표현할 수 있는 프로필 이미지로 설정해보세요!', 1),
       ('INTERVIEW_WRITE_REQUEST', '인터뷰 작성 요청', '아직 인터뷰를 작성하지 않으셨어요! 인터뷰 작성하고 무료 하트 받아 가세요.', 1),
       ('INAPPROPRIATE_PROFILE', '부적절한 프로필 경고', '등록하신 프로필 정보를 변경해 주세요. 개인 정보 혹은 부적절한 내용 등록 시 서비스 이용이 제한될 수 있습니다.', 1),
       ('INAPPROPRIATE_PROFILE_IMAGE', '부적절한 프로필 사진 경고', '등록하신 프로필 사진을 변경해 주세요. 부적절한 사진 등록 시 서비스 이용이 제한될 수 있습니다.', 1),
       ('INAPPROPRIATE_INTERVIEW', '부적절한 인터뷰 경고', '등록하신 인터뷰를 변경해 주세요. 개인 정보 혹은 부적절한 내용 등록 시 서비스 이용이 제한될 수 있습니다.', 1),
       ('INAPPROPRIATE_SELF_INTRODUCTION', '부적절한 셀프소개 게시글 경고', '작성하신 게시글에 부적절한 내용이 포함되어 있습니다. 부적절한 내용 등록 시 서비스 이용이 제한될 수 있습니다.', 1),
       ('INACTIVITY_REMINDER', '다시 방문해보세요', '장기간 접속이 없으시네요! 다시 연애를 시작해 볼까요?', 1),
       ('SCREENING_APPROVED', '심사 승인', '프로필 심사가 완료되었어요!\n안전하고 진정성 있는 만남을 시작해 보세요.', 1),
       ('SCREENING_REJECTED', '심사 반려', '심사가 반려되었습니다. 등록하신 프로필을 변경해주세요. (반려 사유: {rejectionReason})', 1);

-- =============================================================================
-- 연애고사
-- =============================================================================
SET
@now = NOW(6);

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
