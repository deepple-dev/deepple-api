INSERT INTO missions (created_at, updated_at, action_type, frequency_type, target_gender, required_attempt,
                      repeatable_count, rewarded_heart, is_public)
VALUES (NOW(6), NOW(6), 'LIKE', 'DAILY', 'MALE', 1, 2, 2, 1),
       (NOW(6), NOW(6), 'LIKE', 'DAILY', 'FEMALE', 1, 3, 2, 1),
       (NOW(6), NOW(6), 'INTERVIEW', 'CHALLENGE', 'ALL', 1, 1, 30, 1),
       (NOW(6), NOW(6), 'FIRST_DATE_EXAM', 'CHALLENGE', 'ALL', 1, 1, 15, 1)
