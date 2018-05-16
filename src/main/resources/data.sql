SET FOREIGN_KEY_CHECKS = 0;

TRUNCATE TABLE `client`;
TRUNCATE TABLE `role`;
TRUNCATE TABLE `identity`;
TRUNCATE TABLE `identity_role`;

INSERT INTO `client` (active, uid, password, redirect_uri)
VALUES (true, '9fbd4ae2-2db3-44c7-9544-88e80255b56e', '$2a$10$AbxhLGtIx7yv8jhF0BePiOxnb1mlHHq/Ge4R3PxCL2wIsoEov1VaS', 'http://lpg.local.cshr.digital/authenticate'),
(true, 'f90a4080-e5e9-4a80-ace4-f738b4c9c30e', '$2a$10$AbxhLGtIx7yv8jhF0BePiOxnb1mlHHq/Ge4R3PxCL2wIsoEov1VaS', 'http://admin.local.cshr.digital/authenticate');

INSERT INTO `role` (name) VALUES
('USER'),
('COURSE_MANAGER'),
('IDENTITY_MANAGER');

INSERT INTO `identity` (active, email, uid, password) VALUES
(true, 'user@domain.com', '3c706a70-3fff-4e7b-ae7f-102c1d46f569', '$2a$10$sGfnyPnJ8a0b9R.vqIphKu5vjetS3.Bvi6ISv39bOphq5On0U2m36'),
(true, 'identity-manager@domain.com', '65313ea4-59ea-4802-a521-71f9a92c85cd', '$2a$10$sGfnyPnJ8a0b9R.vqIphKu5vjetS3.Bvi6ISv39bOphq5On0U2m36'),
(true, 'course-manager@domain.com', '8dc80f78-9a52-4c31-ac54-d280a70c18eb', '$2a$10$sGfnyPnJ8a0b9R.vqIphKu5vjetS3.Bvi6ISv39bOphq5On0U2m36');

INSERT INTO `identity_role` (identity_id, role_id) VALUES
(SELECT id FROM identity WHERE email = 'user@domain.com', SELECT id FROM role WHERE name = 'USER'),
(SELECT id FROM identity WHERE email = 'course-manager@domain.com', SELECT id FROM role WHERE name = 'COURSE_MANAGER'),
(SELECT id FROM identity WHERE email = 'identity-manager@domain.com', SELECT id FROM role WHERE name = 'IDENTITY_MANAGER');

SET FOREIGN_KEY_CHECKS = 1;
