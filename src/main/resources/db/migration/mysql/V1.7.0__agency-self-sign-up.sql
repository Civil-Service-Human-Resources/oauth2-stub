ALTER TABLE `invite` ADD COLUMN `is_authorised_invite` bit(1) DEFAULT TRUE;

ALTER TABLE `identity` ADD COLUMN `email_recently_updated` bit(1) DEFAULT FALSE;
