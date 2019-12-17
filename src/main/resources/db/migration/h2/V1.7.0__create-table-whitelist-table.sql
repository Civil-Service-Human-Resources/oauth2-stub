CREATE TABLE `whitelisted_domains` (
  `id` mediumint(8) unsigned NOT NULL AUTO_INCREMENT,
  `domain` varchar(100) NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `UK_domain` (`domain`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;