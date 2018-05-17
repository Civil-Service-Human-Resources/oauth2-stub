/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET NAMES utf8 */;
/*!50503 SET NAMES utf8mb4 */;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;


-- Dumping database structure for identity
DROP DATABASE IF EXISTS `identity`;
CREATE DATABASE IF NOT EXISTS `identity` /*!40100 DEFAULT CHARACTER SET utf8 */;
USE `identity`;

-- Dumping structure for table identity.client
DROP TABLE IF EXISTS `client`;
CREATE TABLE IF NOT EXISTS `client` (
  `id` smallint(5) unsigned NOT NULL AUTO_INCREMENT,
  `active` bit(1) NOT NULL,
  `uid` char(36) DEFAULT NULL,
  `password` varchar(100) DEFAULT NULL,
  `redirect_uri` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `UK_uid` (`uid`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- Dumping data for table identity.client: ~0 rows (approximately)
/*!40000 ALTER TABLE `client` DISABLE KEYS */;
/*!40000 ALTER TABLE `client` ENABLE KEYS */;

-- Dumping structure for table identity.identity
DROP TABLE IF EXISTS `identity`;
CREATE TABLE IF NOT EXISTS `identity` (
  `id` mediumint(8) unsigned NOT NULL AUTO_INCREMENT,
  `active` bit(1) NOT NULL,
  `uid` char(36) NOT NULL,
  `email` varchar(150) NOT NULL,
  `password` varchar(100) NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `UK_email` (`email`),
  UNIQUE KEY `UK_uid` (`uid`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- Dumping data for table identity.identity: ~0 rows (approximately)
/*!40000 ALTER TABLE `identity` DISABLE KEYS */;
/*!40000 ALTER TABLE `identity` ENABLE KEYS */;

-- Dumping structure for table identity.identity_role
DROP TABLE IF EXISTS `identity_role`;
CREATE TABLE IF NOT EXISTS `identity_role` (
  `identity_id` mediumint(8) unsigned NOT NULL,
  `role_id` smallint(5) unsigned NOT NULL,
  PRIMARY KEY (`identity_id`,`role_id`),
  KEY `KEY_role_id` (`role_id`),
  CONSTRAINT `FK_identity_role_identity` FOREIGN KEY (`identity_id`) REFERENCES `identity` (`id`) ON DELETE CASCADE ON UPDATE CASCADE,
  CONSTRAINT `FK_identity_role_role` FOREIGN KEY (`role_id`) REFERENCES `role` (`id`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- Dumping data for table identity.identity_role: ~0 rows (approximately)
/*!40000 ALTER TABLE `identity_role` DISABLE KEYS */;
/*!40000 ALTER TABLE `identity_role` ENABLE KEYS */;

-- Dumping structure for table identity.invite
DROP TABLE IF EXISTS `invite`;
CREATE TABLE IF NOT EXISTS `invite` (
  `id` mediumint(8) unsigned NOT NULL AUTO_INCREMENT,
  `inviter_id` mediumint(8) unsigned NOT NULL,
  `code` char(40) NOT NULL,
  `status` varchar(10) NOT NULL,
  `for_email` varchar(150) NOT NULL,
  `invited_at` datetime NOT NULL,
  `accepted_at` datetime DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `code` (`code`),
  KEY `KEY_inviter_id` (`inviter_id`),
  CONSTRAINT `FK_invite_identity` FOREIGN KEY (`inviter_id`) REFERENCES `identity` (`id`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- Dumping data for table identity.invite: ~0 rows (approximately)
/*!40000 ALTER TABLE `invite` DISABLE KEYS */;
/*!40000 ALTER TABLE `invite` ENABLE KEYS */;

-- Dumping structure for table identity.invite_role
DROP TABLE IF EXISTS `invite_role`;
CREATE TABLE IF NOT EXISTS `invite_role` (
  `invite_id` mediumint(8) unsigned NOT NULL,
  `role_id` smallint(5) unsigned NOT NULL,
  PRIMARY KEY (`invite_id`,`role_id`),
  KEY `KEY_role_id` (`role_id`),
  CONSTRAINT `FK_invite_role_invite` FOREIGN KEY (`invite_id`) REFERENCES `invite` (`id`) ON DELETE CASCADE ON UPDATE CASCADE,
  CONSTRAINT `FK_invite_role_role` FOREIGN KEY (`role_id`) REFERENCES `role` (`id`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- Dumping data for table identity.invite_role: ~0 rows (approximately)
/*!40000 ALTER TABLE `invite_role` DISABLE KEYS */;
/*!40000 ALTER TABLE `invite_role` ENABLE KEYS */;

-- Dumping structure for table identity.reset
DROP TABLE IF EXISTS `reset`;
CREATE TABLE IF NOT EXISTS `reset` (
  `id` int(10) unsigned NOT NULL AUTO_INCREMENT,
  `code` char(40) NOT NULL,
  `email` varchar(150) NOT NULL,
  `reset_status` varchar(10) NOT NULL,
  `requested_at` datetime NOT NULL,
  `reset_at` datetime DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `UK_code` (`code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- Dumping data for table identity.reset: ~0 rows (approximately)
/*!40000 ALTER TABLE `reset` DISABLE KEYS */;
/*!40000 ALTER TABLE `reset` ENABLE KEYS */;

-- Dumping structure for table identity.role
DROP TABLE IF EXISTS `role`;
CREATE TABLE IF NOT EXISTS `role` (
  `id` smallint(5) unsigned NOT NULL AUTO_INCREMENT,
  `name` varchar(100) NOT NULL,
  `description` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `UK_name` (`name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- Dumping data for table identity.role: ~0 rows (approximately)
/*!40000 ALTER TABLE `role` DISABLE KEYS */;
/*!40000 ALTER TABLE `role` ENABLE KEYS */;

-- Dumping structure for table identity.token
DROP TABLE IF EXISTS `token`;
CREATE TABLE IF NOT EXISTS `token` (
  `id` bigint(20) unsigned NOT NULL AUTO_INCREMENT,
  `status` int(11) unsigned DEFAULT NULL,
  `token_id` varchar(255) DEFAULT NULL,
  `authentication` longblob,
  `authentication_id` varchar(255) DEFAULT NULL,
  `client_id` varchar(255) DEFAULT NULL,
  `refresh_token` varchar(255) DEFAULT NULL,
  `token` longblob,
  `user_name` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `UK_token_id` (`token_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- Dumping data for table identity.token: ~0 rows (approximately)
/*!40000 ALTER TABLE `token` DISABLE KEYS */;
/*!40000 ALTER TABLE `token` ENABLE KEYS */;

/*!40101 SET SQL_MODE=IFNULL(@OLD_SQL_MODE, '') */;
/*!40014 SET FOREIGN_KEY_CHECKS=IF(@OLD_FOREIGN_KEY_CHECKS IS NULL, 1, @OLD_FOREIGN_KEY_CHECKS) */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
