CREATE DATABASE  IF NOT EXISTS `infoSys` /*!40100 DEFAULT CHARACTER SET utf8 */;
USE `infoSys`;
-- MySQL dump 10.13  Distrib 5.7.12, for Win64 (x86_64)
--
-- Host: grad-cs936-sis2.c9uoo2m4mynu.us-west-1.rds.amazonaws.com    Database: infoSys
-- ------------------------------------------------------
-- Server version	5.7.11-log

/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!40101 SET NAMES utf8 */;
/*!40103 SET @OLD_TIME_ZONE=@@TIME_ZONE */;
/*!40103 SET TIME_ZONE='+00:00' */;
/*!40014 SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0 */;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;
/*!40111 SET @OLD_SQL_NOTES=@@SQL_NOTES, SQL_NOTES=0 */;

--
-- Table structure for table `class`
--

DROP TABLE IF EXISTS `class`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `class` (
  `course_id` smallint(4) unsigned NOT NULL,
  `professor_id` smallint(3) unsigned NOT NULL,
  `semester` enum('spring','summer','fall','winter') NOT NULL,
  `school_year` decimal(4,0) unsigned NOT NULL,
  `building_abbr` char(5) NOT NULL,
  `room_number` decimal(3,0) unsigned NOT NULL,
  `start_at` time NOT NULL,
  `end_at` time NOT NULL,
  `M` bit(1) NOT NULL,
  `Tu` bit(1) NOT NULL,
  `W` bit(1) NOT NULL,
  `Th` bit(1) NOT NULL,
  `F` bit(1) NOT NULL,
  `Sa` bit(1) NOT NULL,
  `Su` bit(1) NOT NULL,
  `id` smallint(5) unsigned NOT NULL AUTO_INCREMENT,
  PRIMARY KEY (`id`),
  KEY `course_id` (`course_id`),
  KEY `professor_id` (`professor_id`),
  CONSTRAINT `class_ibfk_6` FOREIGN KEY (`course_id`) REFERENCES `course` (`id`) ON DELETE CASCADE,
  CONSTRAINT `class_ibfk_7` FOREIGN KEY (`professor_id`) REFERENCES `professor` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=5 DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `class`
--

LOCK TABLES `class` WRITE;
/*!40000 ALTER TABLE `class` DISABLE KEYS */;
INSERT INTO `class` VALUES (1,1,'winter',2015,'MEB',200,'09:15:00','10:10:00','\0','\0','\0','\0','\0','\0','\0',1),(1,1,'winter',2015,'MBE',20,'02:00:00','03:00:00','','\0','','\0','','\0','\0',2),(1,2,'fall',2016,'a',1,'10:20:00','11:20:00','','\0','\0','\0','\0','\0','\0',3),(1,1,'fall',2015,'MBE',202,'04:18:20','05:02:00','','\0','\0','','','\0','\0',4);
/*!40000 ALTER TABLE `class` ENABLE KEYS */;
UNLOCK TABLES;
/*!50003 SET @saved_cs_client      = @@character_set_client */ ;
/*!50003 SET @saved_cs_results     = @@character_set_results */ ;
/*!50003 SET @saved_col_connection = @@collation_connection */ ;
/*!50003 SET character_set_client  = cp850 */ ;
/*!50003 SET character_set_results = cp850 */ ;
/*!50003 SET collation_connection  = cp850_general_ci */ ;
/*!50003 SET @saved_sql_mode       = @@sql_mode */ ;
/*!50003 SET sql_mode              = 'NO_ENGINE_SUBSTITUTION' */ ;
DELIMITER ;;
/*!50003 CREATE*/ /*!50017 DEFINER=`SIS_Admin`@`%`*/ /*!50003 TRIGGER no_course_schedule_overlap
BEFORE INSERT ON class
FOR EACH ROW
BEGIN
	IF(new.start_at > new.end_at) THEN
   SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'Start time is after end time.';
 END IF;
 IF EXISTS(SELECT * FROM class
		  WHERE building_abbr = new.building_abbr
           AND room_number = new.room_number
           AND start_at <= new.end_at
           AND end_at >= new.start_at) THEN
   SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'Class time overlaps with another class in the same room and building.';
 END IF;
END */;;
DELIMITER ;
/*!50003 SET sql_mode              = @saved_sql_mode */ ;
/*!50003 SET character_set_client  = @saved_cs_client */ ;
/*!50003 SET character_set_results = @saved_cs_results */ ;
/*!50003 SET collation_connection  = @saved_col_connection */ ;
/*!50003 SET @saved_cs_client      = @@character_set_client */ ;
/*!50003 SET @saved_cs_results     = @@character_set_results */ ;
/*!50003 SET @saved_col_connection = @@collation_connection */ ;
/*!50003 SET character_set_client  = cp850 */ ;
/*!50003 SET character_set_results = cp850 */ ;
/*!50003 SET collation_connection  = cp850_general_ci */ ;
/*!50003 SET @saved_sql_mode       = @@sql_mode */ ;
/*!50003 SET sql_mode              = 'NO_ENGINE_SUBSTITUTION' */ ;
DELIMITER ;;
/*!50003 CREATE*/ /*!50017 DEFINER=`SIS_Admin`@`%`*/ /*!50003 TRIGGER no_course_schedule_overlap_on_update
BEFORE INSERT ON class
FOR EACH ROW
BEGIN
	IF(new.start_at > new.end_at) THEN
   SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'Start time is after end time.';
 END IF;
 IF EXISTS(SELECT * FROM class
		  WHERE building_abbr = new.building_abbr
           AND room_number = new.room_number
           AND start_at <= new.end_at
           AND end_at >= new.start_at) THEN
   SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'Class time overlaps with another class in the same room and building.';
 END IF;
END */;;
DELIMITER ;
/*!50003 SET sql_mode              = @saved_sql_mode */ ;
/*!50003 SET character_set_client  = @saved_cs_client */ ;
/*!50003 SET character_set_results = @saved_cs_results */ ;
/*!50003 SET collation_connection  = @saved_col_connection */ ;
/*!50003 SET @saved_cs_client      = @@character_set_client */ ;
/*!50003 SET @saved_cs_results     = @@character_set_results */ ;
/*!50003 SET @saved_col_connection = @@collation_connection */ ;
/*!50003 SET character_set_client  = cp850 */ ;
/*!50003 SET character_set_results = cp850 */ ;
/*!50003 SET collation_connection  = cp850_general_ci */ ;
/*!50003 SET @saved_sql_mode       = @@sql_mode */ ;
/*!50003 SET sql_mode              = 'NO_ENGINE_SUBSTITUTION' */ ;
DELIMITER ;;
/*!50003 CREATE*/ /*!50017 DEFINER=`SIS_Admin`@`%`*/ /*!50003 TRIGGER check_school_year
BEFORE INSERT ON class
FOR EACH ROW
BEGIN
  IF(new.school_year > YEAR(CURDATE())) THEN
    SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'Cannot enroll in a class from the future';
  END IF;
END */;;
DELIMITER ;
/*!50003 SET sql_mode              = @saved_sql_mode */ ;
/*!50003 SET character_set_client  = @saved_cs_client */ ;
/*!50003 SET character_set_results = @saved_cs_results */ ;
/*!50003 SET collation_connection  = @saved_col_connection */ ;
/*!50003 SET @saved_cs_client      = @@character_set_client */ ;
/*!50003 SET @saved_cs_results     = @@character_set_results */ ;
/*!50003 SET @saved_col_connection = @@collation_connection */ ;
/*!50003 SET character_set_client  = cp850 */ ;
/*!50003 SET character_set_results = cp850 */ ;
/*!50003 SET collation_connection  = cp850_general_ci */ ;
/*!50003 SET @saved_sql_mode       = @@sql_mode */ ;
/*!50003 SET sql_mode              = 'NO_ENGINE_SUBSTITUTION' */ ;
DELIMITER ;;
/*!50003 CREATE*/ /*!50017 DEFINER=`SIS_Admin`@`%`*/ /*!50003 TRIGGER prevent_class_id_change
BEFORE UPDATE ON class
FOR EACH ROW
BEGIN
	IF(OLD.id != NEW.id) THEN
   SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT='Cannot change class id';
 END IF;
END */;;
DELIMITER ;
/*!50003 SET sql_mode              = @saved_sql_mode */ ;
/*!50003 SET character_set_client  = @saved_cs_client */ ;
/*!50003 SET character_set_results = @saved_cs_results */ ;
/*!50003 SET collation_connection  = @saved_col_connection */ ;
/*!50003 SET @saved_cs_client      = @@character_set_client */ ;
/*!50003 SET @saved_cs_results     = @@character_set_results */ ;
/*!50003 SET @saved_col_connection = @@collation_connection */ ;
/*!50003 SET character_set_client  = cp850 */ ;
/*!50003 SET character_set_results = cp850 */ ;
/*!50003 SET collation_connection  = cp850_general_ci */ ;
/*!50003 SET @saved_sql_mode       = @@sql_mode */ ;
/*!50003 SET sql_mode              = 'NO_ENGINE_SUBSTITUTION' */ ;
DELIMITER ;;
/*!50003 CREATE*/ /*!50017 DEFINER=`SIS_Admin`@`%`*/ /*!50003 TRIGGER check_school_year_on_update
BEFORE UPDATE ON class
FOR EACH ROW
BEGIN
  IF(new.school_year > YEAR(CURDATE())) THEN
    SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'Cannot enroll in a class from the future';
  END IF;
END */;;
DELIMITER ;
/*!50003 SET sql_mode              = @saved_sql_mode */ ;
/*!50003 SET character_set_client  = @saved_cs_client */ ;
/*!50003 SET character_set_results = @saved_cs_results */ ;
/*!50003 SET collation_connection  = @saved_col_connection */ ;

--
-- Table structure for table `course`
--

DROP TABLE IF EXISTS `course`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `course` (
  `name` char(40) NOT NULL,
  `description` varchar(200) NOT NULL,
  `units` decimal(1,0) unsigned NOT NULL,
  `department_id` smallint(3) unsigned NOT NULL,
  `id` smallint(4) unsigned NOT NULL AUTO_INCREMENT,
  PRIMARY KEY (`id`),
  KEY `department_id` (`department_id`),
  CONSTRAINT `course_ibfk_1` FOREIGN KEY (`department_id`) REFERENCES `department` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=16 DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `course`
--

LOCK TABLES `course` WRITE;
/*!40000 ALTER TABLE `course` DISABLE KEYS */;
INSERT INTO `course` VALUES ('Mechanics of Robotics','The mechanics of robot action',5,1,1),('a','b',5,1,2),('c','a',4,1,8),('CS201 Algorithms','Theory of Algorithms',5,2,9),('c','b',3,1,10),('aha','aj',1,1,11),('fhf','dd',3,2,12),('Data Structures','Learn data structures using Java',3,2,13),('A','B',3,1,14),('Solid Mechanics','Mechanics of solids',4,3,15);
/*!40000 ALTER TABLE `course` ENABLE KEYS */;
UNLOCK TABLES;
/*!50003 SET @saved_cs_client      = @@character_set_client */ ;
/*!50003 SET @saved_cs_results     = @@character_set_results */ ;
/*!50003 SET @saved_col_connection = @@collation_connection */ ;
/*!50003 SET character_set_client  = cp850 */ ;
/*!50003 SET character_set_results = cp850 */ ;
/*!50003 SET collation_connection  = cp850_general_ci */ ;
/*!50003 SET @saved_sql_mode       = @@sql_mode */ ;
/*!50003 SET sql_mode              = 'NO_ENGINE_SUBSTITUTION' */ ;
DELIMITER ;;
/*!50003 CREATE*/ /*!50017 DEFINER=`SIS_Admin`@`%`*/ /*!50003 TRIGGER check_units_in_proper_range
BEFORE INSERT ON course
FOR EACH ROW
BEGIN
IF ( new.units > 5 OR new.units < 1 ) THEN
SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'A course must be within 1 to 5 units.';
END IF;
END */;;
DELIMITER ;
/*!50003 SET sql_mode              = @saved_sql_mode */ ;
/*!50003 SET character_set_client  = @saved_cs_client */ ;
/*!50003 SET character_set_results = @saved_cs_results */ ;
/*!50003 SET collation_connection  = @saved_col_connection */ ;
/*!50003 SET @saved_cs_client      = @@character_set_client */ ;
/*!50003 SET @saved_cs_results     = @@character_set_results */ ;
/*!50003 SET @saved_col_connection = @@collation_connection */ ;
/*!50003 SET character_set_client  = cp850 */ ;
/*!50003 SET character_set_results = cp850 */ ;
/*!50003 SET collation_connection  = cp850_general_ci */ ;
/*!50003 SET @saved_sql_mode       = @@sql_mode */ ;
/*!50003 SET sql_mode              = 'NO_ENGINE_SUBSTITUTION' */ ;
DELIMITER ;;
/*!50003 CREATE*/ /*!50017 DEFINER=`SIS_Admin`@`%`*/ /*!50003 TRIGGER prevent_course_id_change
BEFORE UPDATE ON course
FOR EACH ROW
BEGIN
	IF (OLD.id != NEW.id) THEN
	  SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = "Cannot change course id.";
 END IF;
END */;;
DELIMITER ;
/*!50003 SET sql_mode              = @saved_sql_mode */ ;
/*!50003 SET character_set_client  = @saved_cs_client */ ;
/*!50003 SET character_set_results = @saved_cs_results */ ;
/*!50003 SET collation_connection  = @saved_col_connection */ ;
/*!50003 SET @saved_cs_client      = @@character_set_client */ ;
/*!50003 SET @saved_cs_results     = @@character_set_results */ ;
/*!50003 SET @saved_col_connection = @@collation_connection */ ;
/*!50003 SET character_set_client  = cp850 */ ;
/*!50003 SET character_set_results = cp850 */ ;
/*!50003 SET collation_connection  = cp850_general_ci */ ;
/*!50003 SET @saved_sql_mode       = @@sql_mode */ ;
/*!50003 SET sql_mode              = 'NO_ENGINE_SUBSTITUTION' */ ;
DELIMITER ;;
/*!50003 CREATE*/ /*!50017 DEFINER=`SIS_Admin`@`%`*/ /*!50003 TRIGGER no_update_on_units
BEFORE UPDATE ON course
FOR EACH ROW
BEGIN
	IF ( OLD.units != NEW.units ) THEN
	  SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'Cannot change course units.';
	END IF;
END */;;
DELIMITER ;
/*!50003 SET sql_mode              = @saved_sql_mode */ ;
/*!50003 SET character_set_client  = @saved_cs_client */ ;
/*!50003 SET character_set_results = @saved_cs_results */ ;
/*!50003 SET collation_connection  = @saved_col_connection */ ;

--
-- Table structure for table `department`
--

DROP TABLE IF EXISTS `department`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `department` (
  `name` varchar(100) NOT NULL,
  `head_name` char(30) NOT NULL,
  `id` smallint(3) unsigned NOT NULL AUTO_INCREMENT,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=4 DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `department`
--

LOCK TABLES `department` WRITE;
/*!40000 ALTER TABLE `department` DISABLE KEYS */;
INSERT INTO `department` VALUES ('Bell Department of Mechanical Engineering','Yamamoto Kurakami1',1),('ICS','Donald Brenn',2),('Department of Physics','Feynman',3);
/*!40000 ALTER TABLE `department` ENABLE KEYS */;
UNLOCK TABLES;
/*!50003 SET @saved_cs_client      = @@character_set_client */ ;
/*!50003 SET @saved_cs_results     = @@character_set_results */ ;
/*!50003 SET @saved_col_connection = @@collation_connection */ ;
/*!50003 SET character_set_client  = cp850 */ ;
/*!50003 SET character_set_results = cp850 */ ;
/*!50003 SET collation_connection  = cp850_general_ci */ ;
/*!50003 SET @saved_sql_mode       = @@sql_mode */ ;
/*!50003 SET sql_mode              = 'NO_ENGINE_SUBSTITUTION' */ ;
DELIMITER ;;
/*!50003 CREATE*/ /*!50017 DEFINER=`SIS_Admin`@`%`*/ /*!50003 TRIGGER prevent_department_id_change
BEFORE UPDATE ON department
FOR EACH ROW
BEGIN
	IF (OLD.id != NEW.id) THEN
	  SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = "Cannot change department id.";
 END IF;
END */;;
DELIMITER ;
/*!50003 SET sql_mode              = @saved_sql_mode */ ;
/*!50003 SET character_set_client  = @saved_cs_client */ ;
/*!50003 SET character_set_results = @saved_cs_results */ ;
/*!50003 SET collation_connection  = @saved_col_connection */ ;

--
-- Table structure for table `enrollment`
--

DROP TABLE IF EXISTS `enrollment`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `enrollment` (
  `student_id` mediumint(7) unsigned NOT NULL,
  `class_id` smallint(5) unsigned NOT NULL,
  `pass_fail` bit(1) NOT NULL,
  `grade` enum('A','B','C','D','F','P','W','IP') NOT NULL DEFAULT 'IP',
  `id` mediumint(8) unsigned NOT NULL AUTO_INCREMENT,
  PRIMARY KEY (`id`),
  KEY `student_id` (`student_id`),
  KEY `course_schedule_id` (`class_id`),
  CONSTRAINT `enrollment_ibfk_4` FOREIGN KEY (`class_id`) REFERENCES `class` (`id`) ON DELETE CASCADE,
  CONSTRAINT `enrollment_ibfk_5` FOREIGN KEY (`student_id`) REFERENCES `student` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=20 DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `enrollment`
--

LOCK TABLES `enrollment` WRITE;
/*!40000 ALTER TABLE `enrollment` DISABLE KEYS */;
INSERT INTO `enrollment` VALUES (3,2,'','P',17),(3,1,'\0','IP',18),(6,1,'\0','IP',19);
/*!40000 ALTER TABLE `enrollment` ENABLE KEYS */;
UNLOCK TABLES;
/*!50003 SET @saved_cs_client      = @@character_set_client */ ;
/*!50003 SET @saved_cs_results     = @@character_set_results */ ;
/*!50003 SET @saved_col_connection = @@collation_connection */ ;
/*!50003 SET character_set_client  = cp850 */ ;
/*!50003 SET character_set_results = cp850 */ ;
/*!50003 SET collation_connection  = cp850_general_ci */ ;
/*!50003 SET @saved_sql_mode       = @@sql_mode */ ;
/*!50003 SET sql_mode              = 'NO_ENGINE_SUBSTITUTION' */ ;
DELIMITER ;;
/*!50003 CREATE*/ /*!50017 DEFINER=`SIS_Admin`@`%`*/ /*!50003 TRIGGER check_initially_IP
BEFORE INSERT ON enrollment 
FOR EACH ROW
BEGIN
IF (new.grade != 'IP') THEN
SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'You cannot give a newly enrolled student a grade other than IP!';
END IF;
END */;;
DELIMITER ;
/*!50003 SET sql_mode              = @saved_sql_mode */ ;
/*!50003 SET character_set_client  = @saved_cs_client */ ;
/*!50003 SET character_set_results = @saved_cs_results */ ;
/*!50003 SET collation_connection  = @saved_col_connection */ ;
/*!50003 SET @saved_cs_client      = @@character_set_client */ ;
/*!50003 SET @saved_cs_results     = @@character_set_results */ ;
/*!50003 SET @saved_col_connection = @@collation_connection */ ;
/*!50003 SET character_set_client  = cp850 */ ;
/*!50003 SET character_set_results = cp850 */ ;
/*!50003 SET collation_connection  = cp850_general_ci */ ;
/*!50003 SET @saved_sql_mode       = @@sql_mode */ ;
/*!50003 SET sql_mode              = 'NO_ENGINE_SUBSTITUTION' */ ;
DELIMITER ;;
/*!50003 CREATE*/ /*!50017 DEFINER=`SIS_Admin`@`%`*/ /*!50003 TRIGGER prevent_enrollment_id_change
BEFORE UPDATE ON enrollment
FOR EACH ROW
BEGIN
	IF (OLD.id != NEW.id) THEN
	  SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = "Cannot change enrollment id.";
 END IF;
END */;;
DELIMITER ;
/*!50003 SET sql_mode              = @saved_sql_mode */ ;
/*!50003 SET character_set_client  = @saved_cs_client */ ;
/*!50003 SET character_set_results = @saved_cs_results */ ;
/*!50003 SET collation_connection  = @saved_col_connection */ ;
/*!50003 SET @saved_cs_client      = @@character_set_client */ ;
/*!50003 SET @saved_cs_results     = @@character_set_results */ ;
/*!50003 SET @saved_col_connection = @@collation_connection */ ;
/*!50003 SET character_set_client  = cp850 */ ;
/*!50003 SET character_set_results = cp850 */ ;
/*!50003 SET collation_connection  = cp850_general_ci */ ;
/*!50003 SET @saved_sql_mode       = @@sql_mode */ ;
/*!50003 SET sql_mode              = 'NO_ENGINE_SUBSTITUTION' */ ;
DELIMITER ;;
/*!50003 CREATE*/ /*!50017 DEFINER=`SIS_Admin`@`%`*/ /*!50003 TRIGGER update_gpa_and_units_on_grade_assignment
BEFORE UPDATE ON enrollment
FOR EACH ROW
BEGIN
	IF (OLD.grade != 'IP') THEN
	  SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'Cannot change grade on enrollment record once one is assigned.';
 ELSEIF (OLD.grade != NEW.grade) THEN
   IF (OLD.pass_fail AND NEW.grade != 'P' AND NEW.grade != 'F' AND NEW.grade != 'W') THEN
     SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'The grade for a pass-fail must be P, F, or W.';
   ELSEIF (NOT OLD.pass_fail AND NEW.grade = 'P') THEN
     SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT='If the enrollment is not pass-fail the grade cannot be P';
   ELSE
     CALL add_units_and_change_gpa(NEW.grade, OLD.class_id, OLD.student_id);
   END IF;
 END IF;
END */;;
DELIMITER ;
/*!50003 SET sql_mode              = @saved_sql_mode */ ;
/*!50003 SET character_set_client  = @saved_cs_client */ ;
/*!50003 SET character_set_results = @saved_cs_results */ ;
/*!50003 SET collation_connection  = @saved_col_connection */ ;

--
-- Table structure for table `major`
--

DROP TABLE IF EXISTS `major`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `major` (
  `name` char(50) NOT NULL,
  `id` tinyint(3) unsigned NOT NULL AUTO_INCREMENT,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=4 DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `major`
--

LOCK TABLES `major` WRITE;
/*!40000 ALTER TABLE `major` DISABLE KEYS */;
INSERT INTO `major` VALUES ('Computer Science',1),('Mathematics',2),('Zoology',3);
/*!40000 ALTER TABLE `major` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `professor`
--

DROP TABLE IF EXISTS `professor`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `professor` (
  `name` char(30) NOT NULL,
  `academic_rank` char(40) NOT NULL,
  `department_id` smallint(3) unsigned NOT NULL,
  `id` smallint(3) unsigned NOT NULL AUTO_INCREMENT,
  PRIMARY KEY (`id`),
  KEY `department_id` (`department_id`),
  CONSTRAINT `professor_ibfk_1` FOREIGN KEY (`department_id`) REFERENCES `department` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=6 DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `professor`
--

LOCK TABLES `professor` WRITE;
/*!40000 ALTER TABLE `professor` DISABLE KEYS */;
INSERT INTO `professor` VALUES ('Alwarez Morovsky','Associate Professor',1,1),('Ashok Patil','Associate',2,2),('Ashok Patil','Associate',2,3),('Muhammed Ali','Senior',3,4),('Buddha','Emeritus',2,5);
/*!40000 ALTER TABLE `professor` ENABLE KEYS */;
UNLOCK TABLES;
/*!50003 SET @saved_cs_client      = @@character_set_client */ ;
/*!50003 SET @saved_cs_results     = @@character_set_results */ ;
/*!50003 SET @saved_col_connection = @@collation_connection */ ;
/*!50003 SET character_set_client  = cp850 */ ;
/*!50003 SET character_set_results = cp850 */ ;
/*!50003 SET collation_connection  = cp850_general_ci */ ;
/*!50003 SET @saved_sql_mode       = @@sql_mode */ ;
/*!50003 SET sql_mode              = 'NO_ENGINE_SUBSTITUTION' */ ;
DELIMITER ;;
/*!50003 CREATE*/ /*!50017 DEFINER=`SIS_Admin`@`%`*/ /*!50003 TRIGGER prevent_professor_id_change
BEFORE UPDATE ON professor
FOR EACH ROW
BEGIN
	IF (OLD.id != NEW.id) THEN
	  SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = "Cannot change professor id.";
 END IF;
END */;;
DELIMITER ;
/*!50003 SET sql_mode              = @saved_sql_mode */ ;
/*!50003 SET character_set_client  = @saved_cs_client */ ;
/*!50003 SET character_set_results = @saved_cs_results */ ;
/*!50003 SET collation_connection  = @saved_col_connection */ ;

--
-- Table structure for table `state`
--

DROP TABLE IF EXISTS `state`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `state` (
  `abbr` char(2) NOT NULL,
  `name` char(20) NOT NULL,
  PRIMARY KEY (`abbr`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `state`
--

LOCK TABLES `state` WRITE;
/*!40000 ALTER TABLE `state` DISABLE KEYS */;
INSERT INTO `state` VALUES ('AK','Alaska'),('AL','Alabama'),('AR','Arkansas'),('AZ','Arizona'),('CA','California'),('CO','Colorado'),('CT','Connecticut'),('DC','District of Columbia'),('DE','Delaware'),('FL','Florida'),('GA','Georgia'),('HI','Hawaii'),('IA','Iowa'),('ID','Idaho'),('IL','Illinois'),('IN','Indiana'),('KS','Kansas'),('KY','Kentucky'),('LA','Louisiana'),('MA','Massachusetts'),('MD','Maryland'),('ME','Maine'),('MI','Michigan'),('MN','Minnesota'),('MO','Missouri'),('MS','Mississippi'),('MT','Montana'),('NC','North Carolina'),('ND','North Dakota'),('NE','Nebraska'),('NH','New Hampshire'),('NJ','New Jersey'),('NM','New Mexico'),('NV','Nevada'),('NY','New York'),('OH','Ohio'),('OK','Oklahoma'),('OR','Oregon'),('PA','Pennsylvania'),('RI','Rhode Island'),('SC','South Carolina'),('SD','South Dakota'),('TN','Tennessee'),('TX','Texas'),('UT','Utah'),('VA','Virginia'),('VT','Vermont'),('WA','Washington'),('WI','Wisconsin'),('WV','West Virginia'),('WY','Wyoming');
/*!40000 ALTER TABLE `state` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `student`
--

DROP TABLE IF EXISTS `student`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `student` (
  `name` char(40) NOT NULL,
  `major_id` tinyint(3) unsigned NOT NULL,
  `total_units` tinyint(3) unsigned NOT NULL DEFAULT '0',
  `gpa` decimal(3,2) unsigned NOT NULL DEFAULT '0.00',
  `email` char(25) NOT NULL,
  `street` char(25) NOT NULL,
  `city` char(30) NOT NULL,
  `state` char(2) NOT NULL DEFAULT 'PA',
  `zip` decimal(5,0) unsigned NOT NULL,
  `phone` decimal(10,0) unsigned NOT NULL,
  `birth_date` date NOT NULL,
  `age` tinyint(3) unsigned NOT NULL,
  `sex` enum('M','F') NOT NULL,
  `id` mediumint(7) unsigned NOT NULL AUTO_INCREMENT,
  PRIMARY KEY (`id`),
  KEY `major_id` (`major_id`),
  KEY `state` (`state`),
  CONSTRAINT `student_ibfk_1` FOREIGN KEY (`major_id`) REFERENCES `major` (`id`) ON DELETE CASCADE,
  CONSTRAINT `student_ibfk_2` FOREIGN KEY (`state`) REFERENCES `state` (`abbr`)
) ENGINE=InnoDB AUTO_INCREMENT=11 DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `student`
--

LOCK TABLES `student` WRITE;
/*!40000 ALTER TABLE `student` DISABLE KEYS */;
INSERT INTO `student` VALUES ('Bill',1,10,3.00,'bill@g.com','4213','Denvera','CA',90034,31,'1997-11-12',18,'M',3),('jim',3,5,4.00,'jim@jim.com','4','LA','PA',19000,432,'2015-01-02',21,'M',5),('Elmo',1,0,0.00,'elmo@elmo.com','Sesame St.','Kidtown','CA',90032,393,'0011-11-21',46,'M',6),('Bob',3,0,0.00,'bob@bob.com','Bob St.','Bob Angeles','CA',90031,949,'0018-02-16',21,'M',7),('Paul',1,0,0.00,'a','b','e','CA',90034,2,'0095-02-19',21,'M',8),('A',1,0,0.00,'B','E','F','CA',90034,6,'1995-06-04',21,'M',9),('Paul',1,0,0.00,'paulg@grad.com','3965 AZ Way','Los Angeles','CA',90034,39191919,'1992-07-01',23,'M',10);
/*!40000 ALTER TABLE `student` ENABLE KEYS */;
UNLOCK TABLES;
/*!50003 SET @saved_cs_client      = @@character_set_client */ ;
/*!50003 SET @saved_cs_results     = @@character_set_results */ ;
/*!50003 SET @saved_col_connection = @@collation_connection */ ;
/*!50003 SET character_set_client  = cp850 */ ;
/*!50003 SET character_set_results = cp850 */ ;
/*!50003 SET collation_connection  = cp850_general_ci */ ;
/*!50003 SET @saved_sql_mode       = @@sql_mode */ ;
/*!50003 SET sql_mode              = 'NO_ENGINE_SUBSTITUTION' */ ;
DELIMITER ;;
/*!50003 CREATE*/ /*!50017 DEFINER=`SIS_Admin`@`%`*/ /*!50003 TRIGGER zipInStateInsert
BEFORE INSERT ON student
FOR EACH ROW
BEGIN
	IF NOT EXISTS(SELECT * 
		      FROM zipCodeRangesByState
		      WHERE state = new.state
		      AND zipCodeLowBound <= new.zip
		      AND zipCodeUpperBound >= new.zip) OR new.zip = 0 THEN
   SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = "Zip not present in state";
 END IF;
END */;;
DELIMITER ;
/*!50003 SET sql_mode              = @saved_sql_mode */ ;
/*!50003 SET character_set_client  = @saved_cs_client */ ;
/*!50003 SET character_set_results = @saved_cs_results */ ;
/*!50003 SET collation_connection  = @saved_col_connection */ ;
/*!50003 SET @saved_cs_client      = @@character_set_client */ ;
/*!50003 SET @saved_cs_results     = @@character_set_results */ ;
/*!50003 SET @saved_col_connection = @@collation_connection */ ;
/*!50003 SET character_set_client  = cp850 */ ;
/*!50003 SET character_set_results = cp850 */ ;
/*!50003 SET collation_connection  = cp850_general_ci */ ;
/*!50003 SET @saved_sql_mode       = @@sql_mode */ ;
/*!50003 SET sql_mode              = 'NO_ENGINE_SUBSTITUTION' */ ;
DELIMITER ;;
/*!50003 CREATE*/ /*!50017 DEFINER=`SIS_Admin`@`%`*/ /*!50003 TRIGGER check_age_and_birth_date_before_insert
BEFORE INSERT ON student
FOR EACH ROW
BEGIN
	DECLARE todaySubAge DATE;
 DECLARE dayDiff SMALLINT(5);
 DECLARE curYear SMALLINT(5);
	SET todaySubAge = SUBDATE(CURDATE(), INTERVAL new.age YEAR);
 SET dayDiff = DATEDIFF(todaySubAge, new.birth_date);
 SET curYear = YEAR(CURDATE());
 IF(dayDiff < 0 OR (dayDiff > 366 AND MOD(curYear,4) = 0) OR (dayDiff > 365 AND MOD(curYear,4) != 0)) THEN
	  SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT='Birth date and age do not match';
 END IF;
END */;;
DELIMITER ;
/*!50003 SET sql_mode              = @saved_sql_mode */ ;
/*!50003 SET character_set_client  = @saved_cs_client */ ;
/*!50003 SET character_set_results = @saved_cs_results */ ;
/*!50003 SET collation_connection  = @saved_col_connection */ ;
/*!50003 SET @saved_cs_client      = @@character_set_client */ ;
/*!50003 SET @saved_cs_results     = @@character_set_results */ ;
/*!50003 SET @saved_col_connection = @@collation_connection */ ;
/*!50003 SET character_set_client  = cp850 */ ;
/*!50003 SET character_set_results = cp850 */ ;
/*!50003 SET collation_connection  = cp850_general_ci */ ;
/*!50003 SET @saved_sql_mode       = @@sql_mode */ ;
/*!50003 SET sql_mode              = 'NO_ENGINE_SUBSTITUTION' */ ;
DELIMITER ;;
/*!50003 CREATE*/ /*!50017 DEFINER=`SIS_Admin`@`%`*/ /*!50003 TRIGGER prevent_student_id_change
BEFORE UPDATE ON student
FOR EACH ROW
BEGIN
	IF (OLD.id != NEW.id) THEN
	  SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = "Cannot change student id.";
 END IF;
END */;;
DELIMITER ;
/*!50003 SET sql_mode              = @saved_sql_mode */ ;
/*!50003 SET character_set_client  = @saved_cs_client */ ;
/*!50003 SET character_set_results = @saved_cs_results */ ;
/*!50003 SET collation_connection  = @saved_col_connection */ ;
/*!50003 SET @saved_cs_client      = @@character_set_client */ ;
/*!50003 SET @saved_cs_results     = @@character_set_results */ ;
/*!50003 SET @saved_col_connection = @@collation_connection */ ;
/*!50003 SET character_set_client  = cp850 */ ;
/*!50003 SET character_set_results = cp850 */ ;
/*!50003 SET collation_connection  = cp850_general_ci */ ;
/*!50003 SET @saved_sql_mode       = @@sql_mode */ ;
/*!50003 SET sql_mode              = 'NO_ENGINE_SUBSTITUTION' */ ;
DELIMITER ;;
/*!50003 CREATE*/ /*!50017 DEFINER=`SIS_Admin`@`%`*/ /*!50003 TRIGGER zipInStateUpdate
BEFORE UPDATE ON student
FOR EACH ROW
BEGIN
	IF NOT EXISTS(SELECT * 
		      FROM zipCodeRangesByState
		      WHERE state = new.state
		      AND zipCodeLowBound <= new.zip
		      AND zipCodeUpperBound >= new.zip) OR new.zip = 0 THEN
   SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = "Zip not present in state";
 END IF;
END */;;
DELIMITER ;
/*!50003 SET sql_mode              = @saved_sql_mode */ ;
/*!50003 SET character_set_client  = @saved_cs_client */ ;
/*!50003 SET character_set_results = @saved_cs_results */ ;
/*!50003 SET collation_connection  = @saved_col_connection */ ;
/*!50003 SET @saved_cs_client      = @@character_set_client */ ;
/*!50003 SET @saved_cs_results     = @@character_set_results */ ;
/*!50003 SET @saved_col_connection = @@collation_connection */ ;
/*!50003 SET character_set_client  = cp850 */ ;
/*!50003 SET character_set_results = cp850 */ ;
/*!50003 SET collation_connection  = cp850_general_ci */ ;
/*!50003 SET @saved_sql_mode       = @@sql_mode */ ;
/*!50003 SET sql_mode              = 'NO_ENGINE_SUBSTITUTION' */ ;
DELIMITER ;;
/*!50003 CREATE*/ /*!50017 DEFINER=`SIS_Admin`@`%`*/ /*!50003 TRIGGER check_age_and_birth_date_before_update
BEFORE UPDATE ON student
FOR EACH ROW
BEGIN
	DECLARE todaySubAge DATE;
 DECLARE dayDiff SMALLINT(5);
 DECLARE curYear SMALLINT(5);
	SET todaySubAge = SUBDATE(CURDATE(), INTERVAL new.age YEAR);
 SET dayDiff = DATEDIFF(todaySubAge, new.birth_date);
 SET curYear = YEAR(CURDATE());
 IF(dayDiff < 0 OR (dayDiff > 366 AND MOD(curYear,4) = 0) OR (dayDiff > 365 AND MOD(curYear,4) != 0)) THEN
	  SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT='Birth date and age do not match';
 END IF;
END */;;
DELIMITER ;
/*!50003 SET sql_mode              = @saved_sql_mode */ ;
/*!50003 SET character_set_client  = @saved_cs_client */ ;
/*!50003 SET character_set_results = @saved_cs_results */ ;
/*!50003 SET collation_connection  = @saved_col_connection */ ;

--
-- Table structure for table `zipCodeRangesByState`
--

DROP TABLE IF EXISTS `zipCodeRangesByState`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `zipCodeRangesByState` (
  `state` char(2) NOT NULL,
  `zipCodeLowBound` decimal(5,0) unsigned NOT NULL,
  `zipCodeUpperBound` decimal(5,0) unsigned NOT NULL,
  PRIMARY KEY (`zipCodeLowBound`,`zipCodeUpperBound`),
  KEY `state` (`state`),
  CONSTRAINT `zipCodeRangesByState_ibfk_1` FOREIGN KEY (`state`) REFERENCES `state` (`abbr`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `zipCodeRangesByState`
--

LOCK TABLES `zipCodeRangesByState` WRITE;
/*!40000 ALTER TABLE `zipCodeRangesByState` DISABLE KEYS */;
INSERT INTO `zipCodeRangesByState` VALUES ('AK',99500,99999),('AL',35000,35299),('AL',35400,36999),('AR',71600,72999),('AZ',85000,85399),('AZ',85500,85799),('AZ',85900,86099),('AZ',86300,86599),('CA',90000,90899),('CA',91000,92899),('CA',93000,96199),('CO',80000,81699),('CT',6000,6389),('CT',6391,6999),('DE',19700,19999),('FL',32000,33999),('FL',34100,34199),('FL',34200,34299),('FL',34400,34499),('FL',34600,34699),('FL',34700,34799),('FL',34900,34999),('GA',30000,31999),('GA',39800,39999),('HI',96701,96798),('HI',96800,96899),('IA',50000,51699),('IA',52000,52899),('ID',83200,83413),('ID',83415,83899),('IL',60000,62099),('IL',62200,62999),('IN',46000,47999),('KS',66000,66299),('KS',66400,67999),('KY',40000,41899),('KY',42000,42799),('LA',70000,70199),('LA',70300,70899),('LA',71000,71499),('MA',1000,2799),('MA',5500,5599),('MD',20588,0),('MD',20600,21299),('MD',21400,21999),('ME',3900,4999),('MI',48000,49999),('MN',55000,55199),('MN',55300,56799),('MO',63000,63199),('MO',63300,64199),('MO',64400,65899),('MS',38600,39799),('MT',59000,59999),('NC',27000,28999),('ND',58000,58899),('NE',68000,68199),('NE',68300,69399),('NH',3000,3899),('NJ',7000,8999),('NM',87000,87199),('NM',87300,88499),('NV',88900,89199),('NV',89300,89599),('NV',89700,89899),('NY',500,599),('NY',6390,0),('NY',10000,14999),('OH',43000,45999),('OK',73000,73199),('OK',73400,73959),('OK',73961,74199),('OK',74300,74999),('OR',97000,97999),('PA',15000,19699),('RI',2800,2899),('RI',2900,2999),('SC',29000,29999),('SD',57000,57799),('TN',37000,38599),('TX',73300,73399),('TX',73960,0),('TX',75000,77099),('TX',77200,79999),('TX',88500,88599),('UT',84000,84799),('VA',20100,20199),('VA',20598,0),('VA',22000,24699),('VT',5000,5499),('VT',5600,5999),('WA',98000,98699),('WA',98800,99499),('WI',53000,53299),('WI',53400,53599),('WI',53700,54999),('WV',24700,26899),('WY',82000,83199),('WY',83414,0);
/*!40000 ALTER TABLE `zipCodeRangesByState` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Dumping events for database 'infoSys'
--

--
-- Dumping routines for database 'infoSys'
--
/*!50003 DROP PROCEDURE IF EXISTS `add_units_and_change_gpa` */;
/*!50003 SET @saved_cs_client      = @@character_set_client */ ;
/*!50003 SET @saved_cs_results     = @@character_set_results */ ;
/*!50003 SET @saved_col_connection = @@collation_connection */ ;
/*!50003 SET character_set_client  = cp850 */ ;
/*!50003 SET character_set_results = cp850 */ ;
/*!50003 SET collation_connection  = cp850_general_ci */ ;
/*!50003 SET @saved_sql_mode       = @@sql_mode */ ;
/*!50003 SET sql_mode              = 'NO_ENGINE_SUBSTITUTION' */ ;
DELIMITER ;;
CREATE DEFINER=`SIS_Admin`@`%` PROCEDURE `add_units_and_change_gpa`(grade ENUM('F','D','C','B','A','P','W'), class_id SMALLINT UNSIGNED, student_id MEDIUMINT UNSIGNED)
BEGIN
	DECLARE gradeVal TINYINT;
 DECLARE course_units TINYINT;
 IF(grade != 'W') THEN
   SET gradeVal = (CASE
		              WHEN grade = 'P' THEN 2
                       ELSE grade - 1
                   END);
   SELECT course.units
   INTO course_units
   FROM course
   WHERE course.id = (SELECT course_id
                      FROM class
                      WHERE class.id = class_id);
   UPDATE student
   SET gpa = ( (total_units * gpa) + (course_units * gradeVal) ) / (total_units + course_units),
       total_units = total_units + course_units
   WHERE student.id = student_id;
 END IF;
END ;;
DELIMITER ;
/*!50003 SET sql_mode              = @saved_sql_mode */ ;
/*!50003 SET character_set_client  = @saved_cs_client */ ;
/*!50003 SET character_set_results = @saved_cs_results */ ;
/*!50003 SET collation_connection  = @saved_col_connection */ ;
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

-- Dump completed on 2016-06-04 22:42:21
