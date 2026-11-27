ALTER TABLE `matches`
  ADD COLUMN `responded_at` datetime(6) DEFAULT NULL,
  ADD COLUMN `expired_at` datetime(6) DEFAULT NULL;

UPDATE `matches` SET `responded_at` = `updated_at`
WHERE `status` IN ('MATCHED', 'REJECTED', 'REJECT_CHECKED') AND `responded_at` IS NULL;

UPDATE `matches` SET `expired_at` = `updated_at`
WHERE `status` = 'EXPIRED' AND `expired_at` IS NULL;
