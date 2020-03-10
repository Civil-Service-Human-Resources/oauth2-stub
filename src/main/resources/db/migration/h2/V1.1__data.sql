
INSERT INTO `client` (active, uid, password, redirect_uri)
VALUES (true, '9fbd4ae2-2db3-44c7-9544-88e80255b56e', '$2a$10$AbxhLGtIx7yv8jhF0BePiOxnb1mlHHq/Ge4R3PxCL2wIsoEov1VaS', 'http://localhost:3001/authenticate'),
(true, 'f90a4080-e5e9-4a80-ace4-f738b4c9c30e', '$2a$10$AbxhLGtIx7yv8jhF0BePiOxnb1mlHHq/Ge4R3PxCL2wIsoEov1VaS', 'http://localhost:3003/authenticate'),
(true, 'management', '$2a$10$AbxhLGtIx7yv8jhF0BePiOxnb1mlHHq/Ge4R3PxCL2wIsoEov1VaS', 'http://localhost:8081/mgmt/login');

INSERT INTO `role` (name) VALUES
('LEARNER'),
('LEARNING_MANAGER'),
('IDENTITY_MANAGER'),
('ORGANISATION_REPORTER'),
('PROFESSION_REPORTER'),
('CSHR_REPORTER'),
('ORGANISATION_MANAGER'),
('PROFESSION_MANAGER'),
('LEARNING_CREATE'),
('LEARNING_PUBLISH'),
('LEARNING_EDIT'),
('LEARNING_ARCHIVE'),
('LEARNING_DELETE'),
('CSL_AUTHOR'),
('ORGANISATION_AUTHOR'),
('PROFESSION_AUTHOR'),
('KPMG_SUPPLIER_AUTHOR'),
('KORNFERRY_SUPPLIER_AUTHOR'),
('KNOWLEDGEPOOL_SUPPLIER_AUTHOR'),
('IDENTITY_DELETE')
;

INSERT INTO `identity` (active, locked, email, uid, password, last_logged_in, deletion_notification_sent) VALUES
(true, false, 'learner@domain.com', '3c706a70-3fff-4e7b-ae7f-102c1d46f569', '$2a$10$sGfnyPnJ8a0b9R.vqIphKu5vjetS3.Bvi6ISv39bOphq5On0U2m36', GETDATE(), false),
(true, false, 'course-manager@domain.com', '8dc80f78-9a52-4c31-ac54-d280a70c18eb', '$2a$10$sGfnyPnJ8a0b9R.vqIphKu5vjetS3.Bvi6ISv39bOphq5On0U2m36', GETDATE(), false),
(true, false, 'profession-manager@domain.com', '1c66c980-8316-48e4-9358-eca1c2649964', '$2a$10$sGfnyPnJ8a0b9R.vqIphKu5vjetS3.Bvi6ISv39bOphq5On0U2m36', GETDATE(), false),
(true, false, 'identity-manager@domain.com', '65313ea4-59ea-4802-a521-71f9a92c85cd', '$2a$10$sGfnyPnJ8a0b9R.vqIphKu5vjetS3.Bvi6ISv39bOphq5On0U2m36', GETDATE(), false),
(true, false, 'organisation-reporter@domain.com', 'ef422d43-53f1-492a-a159-54b8c5348df8', '$2a$10$sGfnyPnJ8a0b9R.vqIphKu5vjetS3.Bvi6ISv39bOphq5On0U2m36', GETDATE(), false),
(true, false, 'profession-reporter@domain.com', '5b1a0e11-12f5-47a8-9fe2-e272184defc9', '$2a$10$sGfnyPnJ8a0b9R.vqIphKu5vjetS3.Bvi6ISv39bOphq5On0U2m36', GETDATE(), false),
(true, false, 'cshr-reporter@domain.com', 'c4cb1208-eca7-46a6-b496-0f6f354c6eac', '$2a$10$sGfnyPnJ8a0b9R.vqIphKu5vjetS3.Bvi6ISv39bOphq5On0U2m36', GETDATE(), false)
;

INSERT INTO `identity_role` (identity_id, role_id) VALUES
((SELECT id FROM identity WHERE email = 'learner@domain.com'), (SELECT id FROM role WHERE name = 'LEARNER')),
((SELECT id FROM identity WHERE email = 'learner@domain.com'), (SELECT id FROM role WHERE name = 'LEARNING_MANAGER')),
((SELECT id FROM identity WHERE email = 'learner@domain.com'), (SELECT id FROM role WHERE name = 'ORGANISATION_MANAGER')),
((SELECT id FROM identity WHERE email = 'learner@domain.com'), (SELECT id FROM role WHERE name = 'PROFESSION_MANAGER')),
((SELECT id FROM identity WHERE email = 'learner@domain.com'), (SELECT id FROM role WHERE name = 'IDENTITY_MANAGER')),
((SELECT id FROM identity WHERE email = 'course-manager@domain.com'), (SELECT id FROM role WHERE name = 'LEARNING_MANAGER')),
((SELECT id FROM identity WHERE email = 'identity-manager@domain.com'), (SELECT id FROM role WHERE name = 'IDENTITY_MANAGER')),
((SELECT id FROM identity WHERE email = 'organisation-reporter@domain.com'), (SELECT id FROM role WHERE name = 'ORGANISATION_REPORTER')),
((SELECT id FROM identity WHERE email = 'profession-reporter@domain.com'), (SELECT id FROM role WHERE name = 'PROFESSION_REPORTER')),
((SELECT id FROM identity WHERE email = 'cshr-reporter@domain.com'), (SELECT id FROM role WHERE name = 'CSHR_REPORTER'))
;

INSERT INTO `client` (active, uid, password, redirect_uri)
VALUES (true, 'a5881544-6159-4d2f-9b51-8c47ce97454d', '$2a$10$AbxhLGtIx7yv8jhF0BePiOxnb1mlHHq/Ge4R3PxCL2wIsoEov1VaS', 'http://localhost:3005/authenticate');

INSERT INTO `role` (name) VALUES ('DOWNLOAD_BOOKING_FEED');

INSERT INTO `identity` (active, locked, email, uid, password, last_logged_in) VALUES
(true, false, 'booking-feed@domain.com', 'a4cb1208-eca7-46a6-b496-0f6f354c6eac', '$2a$10$sGfnyPnJ8a0b9R.vqIphKu5vjetS3.Bvi6ISv39bOphq5On0U2m36', '2019-02-01')
;

INSERT INTO `identity_role` (identity_id, role_id) VALUES
((SELECT id FROM identity WHERE email = 'booking-feed@domain.com'), (SELECT id FROM role WHERE name = 'DOWNLOAD_BOOKING_FEED'))
;

INSERT INTO `role` (name) VALUES ('MANAGE_CALL_OFF_PO');

INSERT INTO `identity` (active, locked, email, uid, password, last_logged_in) VALUES
(true, false, 'manage-po@domain.com', 'f7cb1208-eca7-46a6-b496-0f6f354c6eac', '$2a$10$sGfnyPnJ8a0b9R.vqIphKu5vjetS3.Bvi6ISv39bOphq5On0U2m36', '2019-02-01')
;

INSERT INTO `identity_role` (identity_id, role_id) VALUES
((SELECT id FROM identity WHERE email = 'manage-po@domain.com'), (SELECT id FROM role WHERE name = 'MANAGE_CALL_OFF_PO'))
;

ALTER TABLE `invite` ALTER COLUMN `inviter_id` mediumint(8) NULL;

INSERT INTO `role` (name) VALUES
('KORNFERRY_SUPPLIER_REPORTER')
;

CREATE TABLE `email_update` (
  `id` int(10) unsigned NOT NULL AUTO_INCREMENT,
  `code` char(40) UNIQUE NOT NULL,
  `email` varchar(255) NOT NULL,
  `identity_id` mediumint(8) unsigned NOT NULL,
  `timestamp` datetime NOT NULL,
  PRIMARY KEY (`id`),
  CONSTRAINT `fk_email_update_identity_id` FOREIGN KEY (`identity_id`) REFERENCES `identity` (`id`) ON DELETE CASCADE ON UPDATE CASCADE
);