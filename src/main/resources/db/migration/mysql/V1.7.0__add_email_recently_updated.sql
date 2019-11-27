ALTER TABLE `identity` ADD `email_recently_updated` bit(1) DEFAULT FALSE;

UPDATE `identity` SET `email_recently_updated` = FALSE WHERE `email_recently_updated` = null;