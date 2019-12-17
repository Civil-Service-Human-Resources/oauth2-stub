CREATE TABLE `whitelisted_domains` (
  `id` int NOT NULL AUTO_INCREMENT,
  `domain` varchar(100) NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `UK_domain` (`domain`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;