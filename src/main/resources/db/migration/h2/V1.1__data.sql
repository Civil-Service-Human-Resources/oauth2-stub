
INSERT INTO `client` (active, uid, password, redirect_uri)
VALUES (true, '9fbd4ae2-2db3-44c7-9544-88e80255b56e', '$2a$10$AbxhLGtIx7yv8jhF0BePiOxnb1mlHHq/Ge4R3PxCL2wIsoEov1VaS', 'http://lpg.local.cshr.digital:3001/authenticate'),
(true, 'f90a4080-e5e9-4a80-ace4-f738b4c9c30e', '$2a$10$AbxhLGtIx7yv8jhF0BePiOxnb1mlHHq/Ge4R3PxCL2wIsoEov1VaS', 'http://admin.local.cshr.digital:3003/authenticate');

INSERT INTO `role` (name) VALUES
('LEARNER'),
('COURSE_MANAGER'),
('IDENTITY_MANAGER'),
('ORGANISATION_REPORTER'),
('PROFESSION_REPORTER'),
('CSHR_REPORTER'),
('ORGANISATION_MANAGER'),
('PROFESSION_MANAGER')
;

INSERT INTO `identity` (active, locked, email, uid, password) VALUES
(true, false, 'learner@domain.com', '3c706a70-3fff-4e7b-ae7f-102c1d46f569', '$2a$10$sGfnyPnJ8a0b9R.vqIphKu5vjetS3.Bvi6ISv39bOphq5On0U2m36'),
(true, false, 'course-manager@domain.com', '8dc80f78-9a52-4c31-ac54-d280a70c18eb', '$2a$10$sGfnyPnJ8a0b9R.vqIphKu5vjetS3.Bvi6ISv39bOphq5On0U2m36'),
(true, false, 'profession-manager@domain.com', '1c66c980-8316-48e4-9358-eca1c2649964', '$2a$10$sGfnyPnJ8a0b9R.vqIphKu5vjetS3.Bvi6ISv39bOphq5On0U2m36'),
(true, false, 'identity-manager@domain.com', '65313ea4-59ea-4802-a521-71f9a92c85cd', '$2a$10$sGfnyPnJ8a0b9R.vqIphKu5vjetS3.Bvi6ISv39bOphq5On0U2m36'),
(true, false, 'organisation-reporter@domain.com', 'ef422d43-53f1-492a-a159-54b8c5348df8', '$2a$10$sGfnyPnJ8a0b9R.vqIphKu5vjetS3.Bvi6ISv39bOphq5On0U2m36'),
(true, false, 'profession-reporter@domain.com', '5b1a0e11-12f5-47a8-9fe2-e272184defc9', '$2a$10$sGfnyPnJ8a0b9R.vqIphKu5vjetS3.Bvi6ISv39bOphq5On0U2m36'),
(true, false, 'cshr-reporter@domain.com', 'c4cb1208-eca7-46a6-b496-0f6f354c6eac', '$2a$10$sGfnyPnJ8a0b9R.vqIphKu5vjetS3.Bvi6ISv39bOphq5On0U2m36')
;

INSERT INTO `identity_role` (identity_id, role_id) VALUES
((SELECT id FROM identity WHERE email = 'learner@domain.com'), (SELECT id FROM role WHERE name = 'LEARNER')),
((SELECT id FROM identity WHERE email = 'learner@domain.com'), (SELECT id FROM role WHERE name = 'COURSE_MANAGER')),
((SELECT id FROM identity WHERE email = 'learner@domain.com'), (SELECT id FROM role WHERE name = 'ORGANISATION_MANAGER')),
((SELECT id FROM identity WHERE email = 'learner@domain.com'), (SELECT id FROM role WHERE name = 'PROFESSION_MANAGER')),
((SELECT id FROM identity WHERE email = 'learner@domain.com'), (SELECT id FROM role WHERE name = 'IDENTITY_MANAGER')),
((SELECT id FROM identity WHERE email = 'course-manager@domain.com'), (SELECT id FROM role WHERE name = 'COURSE_MANAGER')),
((SELECT id FROM identity WHERE email = 'identity-manager@domain.com'), (SELECT id FROM role WHERE name = 'IDENTITY_MANAGER')),
((SELECT id FROM identity WHERE email = 'organisation-reporter@domain.com'), (SELECT id FROM role WHERE name = 'ORGANISATION_REPORTER')),
((SELECT id FROM identity WHERE email = 'profession-reporter@domain.com'), (SELECT id FROM role WHERE name = 'PROFESSION_REPORTER')),
((SELECT id FROM identity WHERE email = 'cshr-reporter@domain.com'), (SELECT id FROM role WHERE name = 'CSHR_REPORTER'))
;

INSERT INTO `client` (active, uid, password, redirect_uri)
VALUES (true, 'a5881544-6159-4d2f-9b51-8c47ce97454d', '$2a$10$AbxhLGtIx7yv8jhF0BePiOxnb1mlHHq/Ge4R3PxCL2wIsoEov1VaS', 'http://lpg.local.cshr.digital:3005/authenticate');

INSERT INTO `role` (name) VALUES ('DOWNLOAD_BOOKING_FEED');

INSERT INTO `identity` (active, locked, email, uid, password) VALUES
(true, false, 'booking-feed@domain.com', 'a4cb1208-eca7-46a6-b496-0f6f354c6eac', '$2a$10$sGfnyPnJ8a0b9R.vqIphKu5vjetS3.Bvi6ISv39bOphq5On0U2m36')
;

INSERT INTO `identity_role` (identity_id, role_id) VALUES
((SELECT id FROM identity WHERE email = 'booking-feed@domain.com'), (SELECT id FROM role WHERE name = 'DOWNLOAD_BOOKING_FEED'))
;

INSERT INTO `role` (name) VALUES ('MANAGE_CALL_OFF_PO');

INSERT INTO `identity` (active, locked, email, uid, password) VALUES
(true, false, 'manage-po@domain.com', 'f7cb1208-eca7-46a6-b496-0f6f354c6eac', '$2a$10$sGfnyPnJ8a0b9R.vqIphKu5vjetS3.Bvi6ISv39bOphq5On0U2m36')
;

INSERT INTO `identity_role` (identity_id, role_id) VALUES
((SELECT id FROM identity WHERE email = 'manage-po@domain.com'), (SELECT id FROM role WHERE name = 'MANAGE_CALL_OFF_PO'))
;
