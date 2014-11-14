-- --------------------------------------------------------
-- Host:                         127.0.0.1
-- Server version:               6.0.2-alpha-community-nt-debug - MySQL Community Server (GPL)
-- Server OS:                    Win32
-- HeidiSQL version:             7.0.0.4053
-- Date/time:                    2012-09-06 21:08:40
-- --------------------------------------------------------

/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET NAMES utf8 */;
/*!40014 SET FOREIGN_KEY_CHECKS=0 */;

-- Dumping database structure for vacations
CREATE DATABASE IF NOT EXISTS `vacations` /*!40100 DEFAULT CHARACTER SET latin1 */;
USE `vacations`;


-- Dumping structure for table vacations.approval_steps
CREATE TABLE IF NOT EXISTS `approval_steps` (
  `ID` bigint(20) NOT NULL AUTO_INCREMENT,
  `STATE` varchar(50) NOT NULL,
  `VACATION_ID` bigint(20) NOT NULL,
  `ROLE_ID` bigint(20) DEFAULT NULL COMMENT 'For deputy this column will be empty',
  `APPROVER_ID` bigint(20) DEFAULT NULL COMMENT 'This column should be filled only for deputy',
  `ROW_NUMBER` int(10) NOT NULL,
  `COMMENTS` varchar(255) DEFAULT NULL,
  `STATUS` int(1) NOT NULL DEFAULT '1' COMMENT '1 - entity is used; 0 - entity was deleted.',
  `VERSION` int(10) NOT NULL DEFAULT '0',
  PRIMARY KEY (`ID`),
  KEY `VACATION_ID` (`VACATION_ID`),
  KEY `ROLE_ID` (`ROLE_ID`),
  KEY `APPROVER_ID` (`APPROVER_ID`),
  CONSTRAINT `APPROVERFK_NEW` FOREIGN KEY (`APPROVER_ID`) REFERENCES `users` (`ID`),
  CONSTRAINT `ROLEFK_NEW` FOREIGN KEY (`ROLE_ID`) REFERENCES `roles` (`ID`),
  CONSTRAINT `VACATIONFK_NEW` FOREIGN KEY (`VACATION_ID`) REFERENCES `vacations` (`ID`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- Data exporting was unselected.


-- Dumping structure for table vacations.approval_steps_log
CREATE TABLE IF NOT EXISTS `approval_steps_log` (
  `LOG_ID` bigint(20) NOT NULL AUTO_INCREMENT,
  `OPERATION` varchar(50) NOT NULL,
  `OPERATION_TIME` datetime NOT NULL,
  `OPERATION_USER` varchar(50) NOT NULL,
  `ID` bigint(20) NOT NULL DEFAULT '0',
  `STATE` varchar(50) CHARACTER SET utf8 NOT NULL,
  `VACATION_ID` bigint(20) NOT NULL,
  `ROLE_ID` bigint(20) DEFAULT NULL COMMENT 'For deputy this column will be empty',
  `APPROVER_ID` bigint(20) DEFAULT NULL COMMENT 'This column should be filled only for deputy',
  `ROW_NUMBER` int(10) NOT NULL,
  `COMMENTS` varchar(255) CHARACTER SET utf8 DEFAULT NULL,
  `STATUS` int(1) NOT NULL DEFAULT '1' COMMENT '1 - entity is used; 0 - entity was deleted.',
  `VERSION` int(10) NOT NULL DEFAULT '0',
  PRIMARY KEY (`LOG_ID`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

-- Data exporting was unselected.


-- Dumping structure for table vacations.holiday_days
CREATE TABLE IF NOT EXISTS `holiday_days` (
  `ID` bigint(20) NOT NULL AUTO_INCREMENT COMMENT 'unique id',
  `START_DATE` date NOT NULL COMMENT 'Start date of noworking interval',
  `END_DATE` date NOT NULL COMMENT 'End date of noworking interval',
  `DESCRIPTION` varchar(255) NOT NULL COMMENT 'Description why this days are noworking',
  `STATUS` int(1) NOT NULL DEFAULT '1' COMMENT '1 - is active; 0 - unused (was deleted);',
  `VERSION` int(10) NOT NULL DEFAULT '0',
  PRIMARY KEY (`ID`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1 COMMENT='Table with nonworking days';

-- Data exporting was unselected.


-- Dumping structure for table vacations.holiday_days_log
CREATE TABLE IF NOT EXISTS `holiday_days_log` (
  `LOG_ID` bigint(20) NOT NULL AUTO_INCREMENT COMMENT 'unique log id',
  `OPERATION` varchar(50) NOT NULL COMMENT 'operation description',
  `OPERATION_TIME` datetime NOT NULL,
  `OPERATION_USER` varchar(50) NOT NULL,
  `ID` bigint(20) NOT NULL DEFAULT '0' COMMENT 'unique id',
  `START_DATE` date NOT NULL COMMENT 'Start date of noworking interval',
  `END_DATE` date NOT NULL COMMENT 'End date of noworking interval',
  `DESCRIPTION` varchar(255) NOT NULL COMMENT 'Description why this days are noworking',
  `STATUS` int(1) NOT NULL DEFAULT '1' COMMENT '1 - is active; 0 - unused (was deleted);',
  `VERSION` int(10) NOT NULL DEFAULT '0',
  PRIMARY KEY (`LOG_ID`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

-- Data exporting was unselected.


-- Dumping structure for table vacations.remaining_vacation_days
CREATE TABLE IF NOT EXISTS `remaining_vacation_days` (
  `ID` bigint(20) NOT NULL COMMENT 'equals to related user''s id',
  `TWO_WEEKS_VACATIONS` int(8) NOT NULL COMMENT 'number of two week vacations',
  `ONE_WEEK_VACATIONS` int(8) NOT NULL COMMENT 'number of one week vacations',
  `DAY_VACATIONS` double(8,1) NOT NULL COMMENT 'number of day vacations',
  `STATUS` int(1) NOT NULL DEFAULT '1' COMMENT '1 - entity is used; 0 - entity was deleted',
  `VERSION` int(10) NOT NULL DEFAULT '0',
  PRIMARY KEY (`ID`),
  CONSTRAINT `ID_USERID_FK` FOREIGN KEY (`ID`) REFERENCES `users` (`ID`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='This table contains the number of vacation days for user';

-- Data exporting was unselected.


-- Dumping structure for table vacations.remaining_vacation_days_log
CREATE TABLE IF NOT EXISTS `remaining_vacation_days_log` (
  `LOG_ID` bigint(20) NOT NULL AUTO_INCREMENT,
  `OPERATION` varchar(50) NOT NULL,
  `OPERATION_TIME` datetime NOT NULL,
  `OPERATION_USER` varchar(50) NOT NULL,
  `ID` bigint(20) NOT NULL COMMENT 'equals to related user''s id',
  `TWO_WEEKS_VACATIONS` int(8) NOT NULL COMMENT 'number of two week vacations',
  `ONE_WEEK_VACATIONS` int(8) NOT NULL COMMENT 'number of one week vacations',
  `DAY_VACATIONS` double(8,1) NOT NULL COMMENT 'number of day vacations',
  `STATUS` int(1) NOT NULL DEFAULT '1' COMMENT '1 - entity is used; 0 - entity was deleted',
  `VERSION` int(10) NOT NULL DEFAULT '0',
  PRIMARY KEY (`LOG_ID`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

-- Data exporting was unselected.


-- Dumping structure for table vacations.roles
CREATE TABLE IF NOT EXISTS `roles` (
  `ID` bigint(20) NOT NULL AUTO_INCREMENT,
  `NAME` varchar(20) NOT NULL,
  `DESCRIPTION` varchar(100) NOT NULL,
  `PARENT_ROLE_ID` bigint(20) DEFAULT NULL,
  `PRIVILEGE` varchar(50) NOT NULL DEFAULT 'DEFAULT',
  `STATUS` int(1) NOT NULL DEFAULT '1' COMMENT '1 - entity is used; 0 - entity was deleted',
  `VERSION` int(10) NOT NULL DEFAULT '0',
  PRIMARY KEY (`ID`),
  KEY `FKAC8FF8F4A5540A6F` (`PARENT_ROLE_ID`),
  CONSTRAINT `PARENTROLEFK` FOREIGN KEY (`PARENT_ROLE_ID`) REFERENCES `roles` (`ID`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

-- Data exporting was unselected.


-- Dumping structure for table vacations.roles_log
CREATE TABLE IF NOT EXISTS `roles_log` (
  `LOG_ID` bigint(20) NOT NULL AUTO_INCREMENT,
  `OPERATION` varchar(50) NOT NULL,
  `OPERATION_TIME` datetime NOT NULL COMMENT 'Time when operation was done',
  `OPERATION_USER` varchar(50) NOT NULL COMMENT 'username of the user that made operation',
  `ID` bigint(20) NOT NULL DEFAULT '0',
  `NAME` varchar(20) NOT NULL,
  `DESCRIPTION` varchar(100) NOT NULL,
  `PARENT_ROLE_ID` bigint(20) DEFAULT NULL,
  `PRIVILEGE` varchar(50) NOT NULL DEFAULT '0',
  `VERSION` int(10) NOT NULL,
  PRIMARY KEY (`LOG_ID`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

-- Data exporting was unselected.


-- Dumping structure for table vacations.used_vacation_days
CREATE TABLE IF NOT EXISTS `used_vacation_days` (
  `ID` bigint(20) NOT NULL COMMENT 'equals to related vacation''s id',
  `TWO_WEEKS_VACATIONS` int(8) NOT NULL COMMENT 'number of two week vacations',
  `ONE_WEEK_VACATIONS` int(8) NOT NULL COMMENT 'number of one week vacations',
  `DAY_VACATIONS` double(8,1) NOT NULL COMMENT 'number of day vacations',
  `STATUS` int(1) NOT NULL DEFAULT '1' COMMENT '1 - entity is used; 0 - entity was deleted',
  `VERSION` int(10) NOT NULL DEFAULT '0',
  PRIMARY KEY (`ID`),
  CONSTRAINT `FK1_VACATIONS` FOREIGN KEY (`ID`) REFERENCES `vacations` (`ID`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1 COMMENT='used vacation days';

-- Data exporting was unselected.


-- Dumping structure for table vacations.used_vacation_days_log
CREATE TABLE IF NOT EXISTS `used_vacation_days_log` (
  `LOG_ID` bigint(20) NOT NULL AUTO_INCREMENT COMMENT 'unique log id',
  `OPERATION` varchar(50) NOT NULL,
  `OPERATION_TIME` datetime NOT NULL,
  `OPERATION_USER` varchar(50) NOT NULL,
  `ID` bigint(20) NOT NULL COMMENT 'equals to related vacation''s id',
  `TWO_WEEKS_VACATIONS` int(8) NOT NULL COMMENT 'number of two week vacations',
  `ONE_WEEK_VACATIONS` int(8) NOT NULL COMMENT 'number of one week vacations',
  `DAY_VACATIONS` double(8,1) NOT NULL COMMENT 'number of day vacations',
  `STATUS` int(1) NOT NULL DEFAULT '1' COMMENT '1 - entity is used; 0 - entity was deleted',
  `VERSION` int(10) NOT NULL DEFAULT '0',
  PRIMARY KEY (`LOG_ID`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

-- Data exporting was unselected.


-- Dumping structure for table vacations.users
CREATE TABLE IF NOT EXISTS `users` (
  `ID` bigint(20) NOT NULL AUTO_INCREMENT,
  `USERNAME` varchar(50) NOT NULL,
  `PASSWORD` char(40) NOT NULL,
  `FULLNAME` varchar(255) NOT NULL,
  `ROLE_ID` bigint(20) NOT NULL,
  `EMAIL` varchar(100) NOT NULL,
  `STATUS` int(1) NOT NULL DEFAULT '1',
  `VERSION` int(10) NOT NULL DEFAULT '0',
  PRIMARY KEY (`ID`),
  KEY `ROLE` (`ROLE_ID`),
  CONSTRAINT `ROLEFK` FOREIGN KEY (`ROLE_ID`) REFERENCES `roles` (`ID`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- Data exporting was unselected.


-- Dumping structure for table vacations.users_log
CREATE TABLE IF NOT EXISTS `users_log` (
  `LOG_ID` bigint(20) NOT NULL AUTO_INCREMENT,
  `OPERATION` varchar(50) NOT NULL,
  `OPERATION_TIME` datetime NOT NULL,
  `OPERATION_USER` varchar(50) NOT NULL,
  `ID` bigint(20) NOT NULL DEFAULT '0',
  `USERNAME` varchar(50) CHARACTER SET utf8 NOT NULL,
  `PASSWORD` char(40) CHARACTER SET utf8 NOT NULL,
  `FULLNAME` varchar(255) CHARACTER SET utf8 NOT NULL,
  `ROLE_ID` bigint(20) NOT NULL,
  `EMAIL` varchar(100) CHARACTER SET utf8 NOT NULL,
  `VERSION` int(10) NOT NULL DEFAULT '0',
  PRIMARY KEY (`LOG_ID`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

-- Data exporting was unselected.


-- Dumping structure for table vacations.vacations
CREATE TABLE IF NOT EXISTS `vacations` (
  `ID` bigint(20) NOT NULL AUTO_INCREMENT,
  `START_DATE` date NOT NULL,
  `END_DATE` date NOT NULL,
  `STATE` varchar(50) NOT NULL,
  `USER_ID` bigint(20) NOT NULL,
  `STATUS` int(1) NOT NULL DEFAULT '1' COMMENT '1 - entity is used; 0 - entity was deleted',
  `VERSION` int(10) NOT NULL DEFAULT '0',
  PRIMARY KEY (`ID`),
  KEY `USER_ID` (`USER_ID`),
  CONSTRAINT `USERFK` FOREIGN KEY (`USER_ID`) REFERENCES `users` (`ID`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- Data exporting was unselected.


-- Dumping structure for table vacations.vacations_deputies
CREATE TABLE IF NOT EXISTS `vacations_deputies` (
  `VACATION_ID` bigint(20) NOT NULL,
  `USER_ID` bigint(20) NOT NULL,
  `LIST_INDEX` int(10) NOT NULL DEFAULT '0' COMMENT 'This column holds list index',
  `STATUS` int(1) NOT NULL DEFAULT '1',
  KEY `USERFK_NEW` (`USER_ID`),
  KEY `VACATION_ID` (`VACATION_ID`),
  CONSTRAINT `USERFK_NEW` FOREIGN KEY (`USER_ID`) REFERENCES `users` (`ID`),
  CONSTRAINT `VACATIONFK` FOREIGN KEY (`VACATION_ID`) REFERENCES `vacations` (`ID`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1 COMMENT='Many-to-many relationship holder';

-- Data exporting was unselected.


-- Dumping structure for table vacations.vacations_deputies_log
CREATE TABLE IF NOT EXISTS `vacations_deputies_log` (
  `LOG_ID` bigint(20) NOT NULL AUTO_INCREMENT,
  `OPERATION` varchar(50) NOT NULL,
  `OPERATION_TIME` datetime NOT NULL,
  `OPERATION_USER` varchar(50) NOT NULL,
  `VACATION_ID` bigint(20) NOT NULL,
  `USER_ID` bigint(20) NOT NULL,
  `LIST_INDEX` int(10) NOT NULL DEFAULT '0' COMMENT 'This column holds list index',
  `STATUS` int(1) NOT NULL DEFAULT '1',
  PRIMARY KEY (`LOG_ID`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

-- Data exporting was unselected.


-- Dumping structure for table vacations.vacations_log
CREATE TABLE IF NOT EXISTS `vacations_log` (
  `LOG_ID` bigint(20) NOT NULL AUTO_INCREMENT,
  `OPERATION` varchar(50) NOT NULL,
  `OPERATION_TIME` datetime NOT NULL,
  `OPERATION_USER` varchar(50) NOT NULL,
  `ID` bigint(20) NOT NULL DEFAULT '0',
  `START_DATE` date NOT NULL,
  `END_DATE` date NOT NULL,
  `STATE` varchar(50) CHARACTER SET utf8 NOT NULL,
  `USER_ID` bigint(20) NOT NULL,
  `STATUS` int(1) NOT NULL DEFAULT '1' COMMENT '1 - entity is used; 0 - entity was deleted',
  `VERSION` int(10) NOT NULL DEFAULT '0',
  PRIMARY KEY (`LOG_ID`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

-- Data exporting was unselected.
/*!40014 SET FOREIGN_KEY_CHECKS=1 */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
