CREATE TABLE `admins` (
  `created_at` datetime(6) NOT NULL,
  `deleted_at` datetime(6) DEFAULT NULL,
  `id` bigint NOT NULL AUTO_INCREMENT,
  `updated_at` datetime(6) DEFAULT NULL,
  `comment` varchar(255) DEFAULT NULL,
  `email` varchar(255) DEFAULT NULL,
  `name` varchar(255) DEFAULT NULL,
  `password` varchar(255) DEFAULT NULL,
  `phone_number` varchar(255) DEFAULT NULL,
  `approval_status` varchar(50) DEFAULT NULL,
  `role` varchar(50) DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_admins_email` (`email`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE `blocks` (
  `blocked_id` bigint NOT NULL,
  `blocker_id` bigint NOT NULL,
  `created_at` datetime(6) NOT NULL,
  `id` bigint NOT NULL AUTO_INCREMENT,
  `updated_at` datetime(6) DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `unique_blocker_id_blocked_id` (`blocker_id`,`blocked_id`),
  UNIQUE KEY `uk_blocks_blocker_id_blocked_id` (`blocker_id`,`blocked_id`),
  KEY `idx_block_blocked_id` (`blocked_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE `dating_exam_answer` (
  `created_at` datetime(6) NOT NULL,
  `id` bigint NOT NULL AUTO_INCREMENT,
  `question_id` bigint NOT NULL,
  `updated_at` datetime(6) DEFAULT NULL,
  `content` varchar(255) NOT NULL,
  `personality_type` varchar(50) NOT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE `dating_exam_question` (
  `created_at` datetime(6) NOT NULL,
  `id` bigint NOT NULL AUTO_INCREMENT,
  `subject_id` bigint NOT NULL,
  `updated_at` datetime(6) DEFAULT NULL,
  `content` varchar(255) NOT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE `dating_exam_subject` (
  `is_public` bit(1) NOT NULL,
  `created_at` datetime(6) NOT NULL,
  `id` bigint NOT NULL AUTO_INCREMENT,
  `updated_at` datetime(6) DEFAULT NULL,
  `name` varchar(255) NOT NULL,
  `type` varchar(50) NOT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE `dating_exam_submit` (
  `created_at` datetime(6) NOT NULL,
  `id` bigint NOT NULL AUTO_INCREMENT,
  `member_id` bigint NOT NULL,
  `subject_id` bigint NOT NULL,
  `updated_at` datetime(6) DEFAULT NULL,
  `answers` varchar(255) NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_dating_exam_submit_member_subject` (`member_id`,`subject_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE `dating_exam_submit_result` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `member_id` bigint NOT NULL,
  `decisive_independent_count` int NOT NULL DEFAULT '0',
  `growing_running_mate_count` int NOT NULL DEFAULT '0',
  `devoted_romantic_count` int NOT NULL DEFAULT '0',
  `realistic_shelter_count` int NOT NULL DEFAULT '0',
  `dominant_personality_type` varchar(50) NOT NULL,
  `created_at` datetime(6) NOT NULL,
  `updated_at` datetime(6) DEFAULT NULL,
  `stimulating_adventurer_count` int NOT NULL DEFAULT '0',
  `rational_centrist_count` int NOT NULL DEFAULT '0',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_dating_exam_submit_result_member` (`member_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE `device_registrations` (
  `is_active` bit(1) NOT NULL,
  `id` bigint NOT NULL AUTO_INCREMENT,
  `member_id` bigint DEFAULT NULL,
  `device_id` varchar(255) DEFAULT NULL,
  `registration_token` varchar(255) DEFAULT NULL,
  `status_changed_at` datetime(6) DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_member_id` (`member_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE `event_publication` (
  `id` binary(16) NOT NULL,
  `listener_id` varchar(512) NOT NULL,
  `event_type` varchar(512) NOT NULL,
  `serialized_event` varchar(4000) NOT NULL,
  `publication_date` datetime(6) NOT NULL,
  `completion_date` datetime(6) DEFAULT NULL,
  `last_resubmission_date` datetime(6) DEFAULT NULL,
  `completion_attempts` int NOT NULL,
  `status` varchar(20) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `idx_event_publication_completion_date` (`completion_date`),
  KEY `idx_event_publication_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE `feed_authors` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `author_type` varchar(50) NOT NULL,
  `member_id` bigint DEFAULT NULL,
  `nickname` varchar(255) DEFAULT NULL,
  `gender` varchar(50) DEFAULT NULL,
  `year_of_birth` int DEFAULT NULL,
  `city` varchar(50) DEFAULT NULL,
  `mbti` varchar(50) DEFAULT NULL,
  `profile_image_url` varchar(255) DEFAULT NULL,
  `personality_type` varchar(50) DEFAULT NULL,
  `activity_status` varchar(50) DEFAULT NULL,
  `grade` varchar(50) DEFAULT NULL,
  `profile_updated_at` datetime(6) DEFAULT NULL,
  `profile_image_updated_at` datetime(6) DEFAULT NULL,
  `personality_type_updated_at` datetime(6) DEFAULT NULL,
  `activity_status_updated_at` datetime(6) DEFAULT NULL,
  `grade_updated_at` datetime(6) DEFAULT NULL,
  `created_at` datetime(6) NOT NULL,
  `updated_at` datetime(6) DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_feed_authors_member_id` (`member_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE `feed_backgrounds` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `image_key` varchar(255) NOT NULL,
  `thumbnail_key` varchar(255) NOT NULL,
  `display_order` int NOT NULL,
  `is_hidden` bit(1) NOT NULL,
  `created_at` datetime(6) NOT NULL,
  `updated_at` datetime(6) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE `feed_blocks` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `blocker_id` bigint NOT NULL,
  `blocked_id` bigint NOT NULL,
  `created_at` datetime(6) NOT NULL,
  `updated_at` datetime(6) DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_feed_blocks_blocker_id_blocked_id` (`blocker_id`,`blocked_id`),
  KEY `idx_feed_blocks_blocked_id_blocker_id` (`blocked_id`,`blocker_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE `feed_comments` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `feed_id` bigint NOT NULL,
  `parent_id` bigint DEFAULT NULL,
  `author_id` bigint NOT NULL,
  `content` varchar(255) NOT NULL,
  `status` varchar(50) NOT NULL,
  `deleted_at` datetime(6) DEFAULT NULL,
  `created_at` datetime(6) NOT NULL,
  `updated_at` datetime(6) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `idx_feed_comments_feed_id_parent_id_id` (`feed_id`,`parent_id`,`id`),
  KEY `idx_feed_comments_parent_id_id` (`parent_id`,`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE `feed_counts` (
  `feed_id` bigint NOT NULL,
  `like_count` bigint NOT NULL,
  `created_at` datetime(6) NOT NULL,
  `updated_at` datetime(6) DEFAULT NULL,
  `comment_count` bigint NOT NULL DEFAULT '0',
  PRIMARY KEY (`feed_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE `feed_images` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `feed_id` bigint NOT NULL,
  `image_id` bigint NOT NULL,
  `image_key` varchar(255) NOT NULL,
  `thumbnail_key` varchar(255) NOT NULL,
  `display_order` int NOT NULL,
  `created_at` datetime(6) NOT NULL,
  `updated_at` datetime(6) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `idx_feed_images_feed_id` (`feed_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE `feed_likes` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `feed_id` bigint NOT NULL,
  `member_id` bigint NOT NULL,
  `created_at` datetime(6) NOT NULL,
  `updated_at` datetime(6) DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_feed_likes_feed_id_member_id` (`feed_id`,`member_id`),
  KEY `idx_feed_likes_member_id_feed_id` (`member_id`,`feed_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE `feed_poll_options` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `feed_id` bigint NOT NULL,
  `content` varchar(255) NOT NULL,
  `display_order` int NOT NULL,
  `created_at` datetime(6) NOT NULL,
  `updated_at` datetime(6) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `idx_feed_poll_options_feed_id` (`feed_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE `feed_target_grades` (
  `feed_id` bigint NOT NULL,
  `grade` varchar(50) NOT NULL,
  PRIMARY KEY (`feed_id`,`grade`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE `feed_vote_counts` (
  `option_id` bigint NOT NULL,
  `feed_id` bigint NOT NULL,
  `vote_count` bigint NOT NULL,
  `created_at` datetime(6) NOT NULL,
  `updated_at` datetime(6) DEFAULT NULL,
  PRIMARY KEY (`option_id`),
  KEY `idx_feed_vote_counts_feed_id` (`feed_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE `feed_votes` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `feed_id` bigint NOT NULL,
  `member_id` bigint NOT NULL,
  `option_id` bigint NOT NULL,
  `created_at` datetime(6) NOT NULL,
  `updated_at` datetime(6) DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_feed_votes_feed_id_member_id` (`feed_id`,`member_id`),
  KEY `idx_feed_votes_member_id_feed_id` (`member_id`,`feed_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE `feeds` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `type` varchar(50) NOT NULL,
  `author_id` bigint NOT NULL,
  `status` varchar(50) NOT NULL,
  `content` varchar(255) NOT NULL,
  `published_at` datetime(6) DEFAULT NULL,
  `grade` varchar(50) DEFAULT NULL,
  `background_id` bigint DEFAULT NULL,
  `text_x` double DEFAULT NULL,
  `text_y` double DEFAULT NULL,
  `text_rotation` double DEFAULT NULL,
  `text_scale` double DEFAULT NULL,
  `title` varchar(255) DEFAULT NULL,
  `is_pinned` bit(1) DEFAULT NULL,
  `admin_id` bigint DEFAULT NULL,
  `idempotency_key` varchar(100) DEFAULT NULL,
  `deleted_at` datetime(6) DEFAULT NULL,
  `created_at` datetime(6) NOT NULL,
  `updated_at` datetime(6) DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_feeds_author_id_idempotency_key` (`author_id`,`idempotency_key`),
  KEY `idx_feeds_author_id_id` (`author_id`,`id`),
  KEY `idx_feeds_grade_status` (`grade`,`status`),
  KEY `idx_feeds_type_status_is_pinned_published_at` (`type`,`status`,`is_pinned`,`published_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE `heart_balances` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `member_id` bigint NOT NULL,
  `mission_balance` bigint NOT NULL,
  `purchase_balance` bigint NOT NULL,
  `created_at` datetime(6) NOT NULL,
  `updated_at` datetime(6) DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_heart_balances_member_id` (`member_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE `heart_members` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `member_id` bigint NOT NULL,
  `gender` varchar(50) DEFAULT NULL,
  `is_vip` bit(1) NOT NULL,
  `source_updated_at` datetime(6) NOT NULL,
  `created_at` datetime(6) NOT NULL,
  `updated_at` datetime(6) DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_heart_members_member_id` (`member_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE `heart_purchase_options` (
  `amount` bigint DEFAULT NULL,
  `created_at` datetime(6) NOT NULL,
  `deleted_at` datetime(6) DEFAULT NULL,
  `id` bigint NOT NULL AUTO_INCREMENT,
  `price` bigint DEFAULT NULL,
  `updated_at` datetime(6) DEFAULT NULL,
  `name` varchar(255) DEFAULT NULL,
  `product_id` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE `heart_transactions` (
  `amount` bigint DEFAULT NULL,
  `created_at` datetime(6) NOT NULL,
  `id` bigint NOT NULL AUTO_INCREMENT,
  `member_id` bigint DEFAULT NULL,
  `mission_balance` bigint DEFAULT NULL,
  `purchase_balance` bigint DEFAULT NULL,
  `updated_at` datetime(6) DEFAULT NULL,
  `content` varchar(255) DEFAULT NULL,
  `transaction_type` varchar(50) DEFAULT NULL,
  `idempotency_key` varchar(100) DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_heart_transactions_type_idempotency_key` (`transaction_type`,`idempotency_key`),
  KEY `idx_heart_transactions_member_id_id` (`member_id`,`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE `heart_usage_policies` (
  `created_at` datetime(6) NOT NULL,
  `id` bigint NOT NULL AUTO_INCREMENT,
  `price` bigint DEFAULT NULL,
  `updated_at` datetime(6) DEFAULT NULL,
  `gender` varchar(50) DEFAULT NULL,
  `transaction_type` varchar(50) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE `images` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `owner_type` varchar(50) NOT NULL,
  `owner_id` bigint NOT NULL,
  `content_type` varchar(50) NOT NULL,
  `status` varchar(50) NOT NULL,
  `source_key` varchar(255) NOT NULL,
  `image_key` varchar(255) NOT NULL,
  `thumbnail_key` varchar(255) NOT NULL,
  `failure_reason` varchar(255) DEFAULT NULL,
  `created_at` datetime(6) NOT NULL,
  `updated_at` datetime(6) DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_images_source_key` (`source_key`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE `interview_answers` (
  `created_at` datetime(6) NOT NULL,
  `id` bigint NOT NULL AUTO_INCREMENT,
  `member_id` bigint DEFAULT NULL,
  `question_id` bigint DEFAULT NULL,
  `updated_at` datetime(6) DEFAULT NULL,
  `content` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE `interview_questions` (
  `is_public` bit(1) NOT NULL,
  `created_at` datetime(6) NOT NULL,
  `id` bigint NOT NULL AUTO_INCREMENT,
  `updated_at` datetime(6) DEFAULT NULL,
  `content` varchar(255) DEFAULT NULL,
  `category` varchar(50) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE `likes` (
  `created_at` datetime(6) NOT NULL,
  `id` bigint NOT NULL AUTO_INCREMENT,
  `receiver_id` bigint DEFAULT NULL,
  `sender_id` bigint DEFAULT NULL,
  `updated_at` datetime(6) DEFAULT NULL,
  `level` varchar(50) DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_likes_sender_id_receiver_id` (`sender_id`,`receiver_id`),
  KEY `idx_receiver_id` (`receiver_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE `matches` (
  `created_at` datetime(6) NOT NULL,
  `id` bigint NOT NULL AUTO_INCREMENT,
  `read_by_responder_at` datetime(6) DEFAULT NULL,
  `requester_id` bigint DEFAULT NULL,
  `responder_id` bigint DEFAULT NULL,
  `updated_at` datetime(6) DEFAULT NULL,
  `request_message` varchar(255) DEFAULT NULL,
  `response_message` varchar(255) DEFAULT NULL,
  `requester_contact_type` varchar(50) DEFAULT NULL,
  `responder_contact_type` varchar(50) DEFAULT NULL,
  `status` varchar(50) DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_matches_active_member_pair` (((case when (`status` not in (_utf8mb4'EXPIRED',_utf8mb4'REJECT_CHECKED')) then least(`requester_id`,`responder_id`) end)),((case when (`status` not in (_utf8mb4'EXPIRED',_utf8mb4'REJECT_CHECKED')) then greatest(`requester_id`,`responder_id`) end))),
  KEY `idx_responder_id` (`responder_id`),
  KEY `idx_requester_id_responder_id` (`requester_id`,`responder_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE `member_accounts` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `phone_number` varchar(255) NOT NULL,
  `status` varchar(50) NOT NULL,
  `suspended_until` datetime(6) DEFAULT NULL,
  `created_at` datetime(6) NOT NULL,
  `updated_at` datetime(6) DEFAULT NULL,
  `deleted_at` datetime(6) DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_member_accounts_phone_number` (`phone_number`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE `member_hobbies` (
  `member_id` bigint NOT NULL,
  `name` varchar(50) DEFAULT NULL,
  KEY `idx_member_hobbies_member_id` (`member_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE `member_ideal_cities` (
  `member_ideal_id` bigint NOT NULL,
  `cities` varchar(50) DEFAULT NULL,
  KEY `idx_member_ideal_cities_member_ideal_id` (`member_ideal_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE `member_ideal_hobbies` (
  `member_ideal_id` bigint NOT NULL,
  `hobbies` varchar(50) DEFAULT NULL,
  KEY `idx_member_ideal_hobbies_member_ideal_id` (`member_ideal_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE `member_ideals` (
  `max_age` int DEFAULT NULL,
  `min_age` int DEFAULT NULL,
  `created_at` datetime(6) NOT NULL,
  `id` bigint NOT NULL AUTO_INCREMENT,
  `member_id` bigint DEFAULT NULL,
  `updated_at` datetime(6) DEFAULT NULL,
  `drinking_status` varchar(50) DEFAULT NULL,
  `religion` varchar(50) DEFAULT NULL,
  `smoking_status` varchar(50) DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_member_ideals_member_id` (`member_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE `member_introductions` (
  `created_at` datetime(6) NOT NULL,
  `id` bigint NOT NULL AUTO_INCREMENT,
  `introduced_member_id` bigint DEFAULT NULL,
  `member_id` bigint DEFAULT NULL,
  `updated_at` datetime(6) DEFAULT NULL,
  `type` varchar(50) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `idx_member_introductions_member_introduced` (`member_id`,`introduced_member_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE `member_mission` (
  `attempt_count` int NOT NULL,
  `is_completed` bit(1) NOT NULL,
  `success_count` int NOT NULL,
  `created_at` datetime(6) NOT NULL,
  `id` bigint NOT NULL AUTO_INCREMENT,
  `member_id` bigint NOT NULL,
  `mission_id` bigint NOT NULL,
  `updated_at` datetime(6) DEFAULT NULL,
  `period` varchar(20) NOT NULL DEFAULT 'ALL',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_member_mission_member_mission_period` (`member_id`,`mission_id`,`period`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE `member_notification_preferences` (
  `is_enabled` bit(1) DEFAULT NULL,
  `member_id` bigint NOT NULL,
  `notification_type` varchar(50) NOT NULL,
  PRIMARY KEY (`member_id`,`notification_type`),
  KEY `idx_member_notification_preferences_member_id` (`member_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE `members` (
  `height` int DEFAULT NULL,
  `is_dating_exam_submitted` bit(1) NOT NULL,
  `is_profile_public` bit(1) NOT NULL,
  `is_vip` bit(1) NOT NULL,
  `year_of_birth` int DEFAULT NULL,
  `created_at` datetime(6) NOT NULL,
  `deleted_at` datetime(6) DEFAULT NULL,
  `id` bigint NOT NULL AUTO_INCREMENT,
  `updated_at` datetime(6) DEFAULT NULL,
  `kakao_id` varchar(255) DEFAULT NULL,
  `nickname` varchar(255) DEFAULT NULL,
  `phone_number` varchar(255) DEFAULT NULL,
  `activity_status` varchar(50) DEFAULT NULL,
  `city` varchar(50) DEFAULT NULL,
  `district` varchar(50) DEFAULT NULL,
  `drinking_status` varchar(50) DEFAULT NULL,
  `gender` varchar(50) DEFAULT NULL,
  `grade` varchar(50) DEFAULT NULL,
  `highest_education` varchar(50) DEFAULT NULL,
  `job` varchar(50) DEFAULT NULL,
  `mbti` varchar(50) DEFAULT NULL,
  `primary_contact_type` varchar(50) DEFAULT NULL,
  `religion` varchar(50) DEFAULT NULL,
  `smoking_status` varchar(50) DEFAULT NULL,
  `last_accessed_at` datetime(6) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `idx_deleted_at` (`deleted_at`),
  KEY `idx_members_phone_number` (`phone_number`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE `missions` (
  `is_public` bit(1) NOT NULL,
  `repeatable_count` int NOT NULL,
  `required_attempt` int NOT NULL,
  `rewarded_heart` int NOT NULL,
  `created_at` datetime(6) NOT NULL,
  `id` bigint NOT NULL AUTO_INCREMENT,
  `updated_at` datetime(6) DEFAULT NULL,
  `action_type` varchar(50) DEFAULT NULL,
  `frequency_type` varchar(50) DEFAULT NULL,
  `target_gender` varchar(50) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE `notification_preferences` (
  `is_enabled_globally` bit(1) NOT NULL,
  `created_at` datetime(6) NOT NULL,
  `deleted_at` datetime(6) DEFAULT NULL,
  `id` bigint NOT NULL AUTO_INCREMENT,
  `member_id` bigint DEFAULT NULL,
  `updated_at` datetime(6) DEFAULT NULL,
  `global_setting_changed_at` datetime(6) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `idx_notification_preferences_member_id` (`member_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE `notification_templates` (
  `is_active` bit(1) NOT NULL,
  `id` bigint NOT NULL AUTO_INCREMENT,
  `body_template` varchar(255) DEFAULT NULL,
  `title_template` varchar(255) DEFAULT NULL,
  `type` varchar(50) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE `notifications` (
  `created_at` datetime(6) NOT NULL,
  `deleted_at` datetime(6) DEFAULT NULL,
  `id` bigint NOT NULL AUTO_INCREMENT,
  `read_at` datetime(6) DEFAULT NULL,
  `receiver_id` bigint DEFAULT NULL,
  `sender_id` bigint DEFAULT NULL,
  `updated_at` datetime(6) DEFAULT NULL,
  `body` varchar(255) DEFAULT NULL,
  `title` varchar(255) DEFAULT NULL,
  `sender_type` varchar(50) DEFAULT NULL,
  `status` varchar(50) DEFAULT NULL,
  `type` varchar(50) DEFAULT NULL,
  `idempotency_key` varchar(100) NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_notifications_idempotency_key` (`idempotency_key`),
  KEY `idx_notifications_receiver_deleted_id` (`receiver_id`,`deleted_at`,`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE `orders` (
  `created_at` datetime(6) NOT NULL,
  `id` bigint NOT NULL AUTO_INCREMENT,
  `member_id` bigint DEFAULT NULL,
  `updated_at` datetime(6) DEFAULT NULL,
  `transaction_id` varchar(255) DEFAULT NULL,
  `payment_method` varchar(50) DEFAULT NULL,
  `status` varchar(50) DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_orders_transaction_id_payment_method` (`transaction_id`,`payment_method`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE `profile_exchanges` (
  `created_at` datetime(6) NOT NULL,
  `id` bigint NOT NULL AUTO_INCREMENT,
  `requester_id` bigint NOT NULL,
  `responder_id` bigint NOT NULL,
  `updated_at` datetime(6) DEFAULT NULL,
  `status` varchar(50) DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_profile_exchanges_member_pair` ((least(`requester_id`,`responder_id`)),(greatest(`requester_id`,`responder_id`))),
  KEY `idx_responder_id` (`responder_id`),
  KEY `idx_requester_id_responder_id` (`requester_id`,`responder_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE `profile_images` (
  `is_primary` bit(1) NOT NULL,
  `profile_order` int DEFAULT NULL,
  `created_at` datetime(6) NOT NULL,
  `id` bigint NOT NULL AUTO_INCREMENT,
  `member_id` bigint DEFAULT NULL,
  `updated_at` datetime(6) DEFAULT NULL,
  `url` varchar(255) DEFAULT NULL,
  `image_id` bigint DEFAULT NULL,
  `image_key` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `idx_member_id_is_primary` (`member_id`,`is_primary`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE `refunds` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `order_id` bigint NOT NULL,
  `member_id` bigint NOT NULL,
  `transaction_id` varchar(255) NOT NULL,
  `product_id` varchar(255) NOT NULL,
  `quantity` int NOT NULL,
  `refund_amount` bigint NOT NULL,
  `payment_method` varchar(50) NOT NULL,
  `notification_type` varchar(50) NOT NULL,
  `refunded_at` datetime(6) NOT NULL,
  `created_at` datetime(6) NOT NULL,
  `updated_at` datetime(6) DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_refunds_transaction_id` (`transaction_id`),
  KEY `idx_refunds_member_id` (`member_id`),
  KEY `idx_refunds_order_id` (`order_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE `reports` (
  `admin_id` bigint DEFAULT NULL,
  `created_at` datetime(6) NOT NULL,
  `id` bigint NOT NULL AUTO_INCREMENT,
  `reportee_id` bigint DEFAULT NULL,
  `reporter_id` bigint DEFAULT NULL,
  `updated_at` datetime(6) DEFAULT NULL,
  `version` bigint DEFAULT NULL,
  `content` varchar(255) DEFAULT NULL,
  `reason` varchar(50) DEFAULT NULL,
  `result` varchar(50) DEFAULT NULL,
  `target_type` varchar(50) NOT NULL,
  `target_id` bigint NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_reports_target_type_target_id_reporter_id` (`target_type`,`target_id`,`reporter_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE `screenings` (
  `admin_id` bigint DEFAULT NULL,
  `created_at` datetime(6) NOT NULL,
  `id` bigint NOT NULL AUTO_INCREMENT,
  `member_id` bigint DEFAULT NULL,
  `updated_at` datetime(6) DEFAULT NULL,
  `version` bigint DEFAULT NULL,
  `rejection_reason` varchar(50) DEFAULT NULL,
  `status` varchar(50) DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_screenings_pending_member` (((case when (`status` = _utf8mb4'PENDING') then `member_id` end)))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE `self_introductions` (
  `is_opened` bit(1) NOT NULL,
  `created_at` datetime(6) NOT NULL,
  `deleted_at` datetime(6) DEFAULT NULL,
  `id` bigint NOT NULL AUTO_INCREMENT,
  `member_id` bigint DEFAULT NULL,
  `updated_at` datetime(6) DEFAULT NULL,
  `content` varchar(255) DEFAULT NULL,
  `title` varchar(255) DEFAULT NULL,
  `image_url` varchar(255) DEFAULT NULL,
  `image_id` bigint DEFAULT NULL,
  `image_key` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `idx_member_id` (`member_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE `suspensions` (
  `admin_id` bigint DEFAULT NULL,
  `created_at` datetime(6) NOT NULL,
  `expire_at` datetime(6) DEFAULT NULL,
  `id` bigint NOT NULL AUTO_INCREMENT,
  `member_id` bigint DEFAULT NULL,
  `updated_at` datetime(6) DEFAULT NULL,
  `status` varchar(50) DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_suspensions_member_id` (`member_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE `warning_reasons` (
  `warning_id` bigint NOT NULL,
  `reason_type` varchar(50) DEFAULT NULL,
  KEY `idx_warning_reasons_warning_id` (`warning_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE `warnings` (
  `is_critical` bit(1) NOT NULL,
  `admin_id` bigint DEFAULT NULL,
  `created_at` datetime(6) NOT NULL,
  `id` bigint NOT NULL AUTO_INCREMENT,
  `member_id` bigint DEFAULT NULL,
  `updated_at` datetime(6) DEFAULT NULL,
  `idempotency_key` varchar(100) DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_warnings_idempotency_key` (`idempotency_key`),
  KEY `idx_member_id` (`member_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
