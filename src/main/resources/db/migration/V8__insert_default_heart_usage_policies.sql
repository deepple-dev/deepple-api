-- Insert default heart usage policies
INSERT INTO heart_usage_policies (created_at, updated_at, gender, price, transaction_type)
VALUES (NOW(6), NOW(6), 'MALE', 10, 'INTRODUCTION'),
       (NOW(6), NOW(6), 'FEMALE', 4, 'INTRODUCTION'),
       (NOW(6), NOW(6), 'MALE', 20, 'MESSAGE'),
       (NOW(6), NOW(6), 'FEMALE', 10, 'MESSAGE'),
       (NOW(6), NOW(6), 'MALE', 3, 'PROFILE_EXCHANGE'),
       (NOW(6), NOW(6), 'FEMALE', 3, 'PROFILE_EXCHANGE'),
       (NOW(6), NOW(6), 'MALE', 20, 'MESSAGE_ACCEPTED'),
       (NOW(6), NOW(6), 'FEMALE', 10, 'MESSAGE_ACCEPTED');
