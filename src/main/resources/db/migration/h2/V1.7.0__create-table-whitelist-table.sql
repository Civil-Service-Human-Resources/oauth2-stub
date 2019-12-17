CREATE TABLE `whitelisted_domains` (
  `id` smallint(5) unsigned NOT NULL AUTO_INCREMENT,
  `domain` varchar(100) NOT NULL,
  `description` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `UK_domain` (`domain`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;