-- MySQL dump 10.13  Distrib 8.0.44, for Linux (aarch64)
--
-- Host: localhost    Database: matchduo_db
-- ------------------------------------------------------
-- Server version	8.0.44

/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!50503 SET NAMES utf8mb4 */;
/*!40103 SET @OLD_TIME_ZONE=@@TIME_ZONE */;
/*!40103 SET TIME_ZONE='+00:00' */;
/*!40014 SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0 */;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;
/*!40111 SET @OLD_SQL_NOTES=@@SQL_NOTES, SQL_NOTES=0 */;

--
-- Table structure for table `chat_message`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE IF NOT EXISTS `chat_message` (
  `chat_message_id` bigint NOT NULL AUTO_INCREMENT,
  `content` text COLLATE utf8mb4_unicode_ci NOT NULL,
  `created_at` datetime(6) NOT NULL,
  `message_type` enum('SYSTEM','TEXT') COLLATE utf8mb4_unicode_ci NOT NULL,
  `session_no` int NOT NULL,
  `chat_room_id` bigint NOT NULL,
  `sender_id` bigint NOT NULL,
  PRIMARY KEY (`chat_message_id`),
  KEY `idx_chat_message_room_session_message_id` (`chat_room_id`,`session_no`,`chat_message_id`),
  KEY `idx_chat_message_room_session_created_at` (`chat_room_id`,`session_no`,`created_at`),
  KEY `FKm92rh2bmfw19xcn7nj5vrixsi` (`sender_id`),
  CONSTRAINT `FKj52yap2xrm9u0721dct0tjor9` FOREIGN KEY (`chat_room_id`) REFERENCES `chat_room` (`chat_room_id`),
  CONSTRAINT `FKm92rh2bmfw19xcn7nj5vrixsi` FOREIGN KEY (`sender_id`) REFERENCES `user` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=5 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `chat_message_read`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE IF NOT EXISTS `chat_message_read` (
  `message_read_id` bigint NOT NULL AUTO_INCREMENT,
  `last_read_at` datetime(6) DEFAULT NULL,
  `chat_room_id` bigint NOT NULL,
  `last_read_message_id` bigint DEFAULT NULL,
  `user_id` bigint NOT NULL,
  PRIMARY KEY (`message_read_id`),
  UNIQUE KEY `uk_message_read_room_user` (`chat_room_id`,`user_id`),
  KEY `idx_message_read_user` (`user_id`),
  KEY `FK1419qt2k9lb7mh4or9688rq4x` (`last_read_message_id`),
  CONSTRAINT `FK1419qt2k9lb7mh4or9688rq4x` FOREIGN KEY (`last_read_message_id`) REFERENCES `chat_message` (`chat_message_id`) ON DELETE SET NULL,
  CONSTRAINT `FKi7nq8vtewwwu8k8mwt0k00phm` FOREIGN KEY (`user_id`) REFERENCES `user` (`id`),
  CONSTRAINT `FKm2gbc2dx4vee3g2uj0e0te9yk` FOREIGN KEY (`chat_room_id`) REFERENCES `chat_room` (`chat_room_id`)
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `chat_room`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE IF NOT EXISTS `chat_room` (
  `chat_room_id` bigint NOT NULL AUTO_INCREMENT,
  `created_at` datetime(6) NOT NULL,
  `current_session_no` int NOT NULL,
  `receiver_left` bit(1) NOT NULL,
  `sender_left` bit(1) NOT NULL,
  `session_started_at` datetime(6) NOT NULL,
  `updated_at` datetime(6) DEFAULT NULL,
  `post_id` bigint NOT NULL,
  `receiver_id` bigint NOT NULL,
  `sender_id` bigint NOT NULL,
  PRIMARY KEY (`chat_room_id`),
  UNIQUE KEY `uk_chat_room_post_sender` (`post_id`,`sender_id`),
  KEY `idx_chat_room_post` (`post_id`),
  KEY `idx_chat_room_sender` (`sender_id`),
  KEY `idx_chat_room_receiver` (`receiver_id`),
  CONSTRAINT `FK5omnauewbg4ga8ey3w8u6d0dd` FOREIGN KEY (`sender_id`) REFERENCES `user` (`id`),
  CONSTRAINT `FKckfxqm5ulopxi03vfxeqh7k4m` FOREIGN KEY (`receiver_id`) REFERENCES `user` (`id`),
  CONSTRAINT `FKdedif34f1oocp49p9lxh3tglc` FOREIGN KEY (`post_id`) REFERENCES `post` (`post_id`)
) ENGINE=InnoDB AUTO_INCREMENT=2 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `favorite_champion`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE IF NOT EXISTS `favorite_champion` (
  `favorite_champion_id` bigint NOT NULL AUTO_INCREMENT,
  `created_at` datetime(6) NOT NULL,
  `deleted_at` datetime(6) DEFAULT NULL,
  `is_active` bit(1) NOT NULL,
  `updated_at` datetime(6) NOT NULL,
  `champion_id` int NOT NULL,
  `champion_name` varchar(255) COLLATE utf8mb4_unicode_ci NOT NULL,
  `losses` int NOT NULL,
  `champion_rank` int NOT NULL,
  `total_games` int NOT NULL,
  `win_rate` double NOT NULL,
  `wins` int NOT NULL,
  `game_account_id` bigint NOT NULL,
  PRIMARY KEY (`favorite_champion_id`),
  UNIQUE KEY `UKohpkkjs6y9aga121968dtm2a2` (`game_account_id`,`champion_rank`),
  CONSTRAINT `FKr22v5014bljhq1x9jbrfhvwkk` FOREIGN KEY (`game_account_id`) REFERENCES `game_account` (`game_account_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `game_account`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE IF NOT EXISTS `game_account` (
  `game_account_id` bigint NOT NULL AUTO_INCREMENT,
  `created_at` datetime(6) NOT NULL,
  `deleted_at` datetime(6) DEFAULT NULL,
  `is_active` bit(1) NOT NULL,
  `updated_at` datetime(6) NOT NULL,
  `game_nickname` varchar(255) COLLATE utf8mb4_unicode_ci NOT NULL,
  `game_tag` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `game_type` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `profile_icon_id` int DEFAULT NULL,
  `puuid` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `user_id` bigint NOT NULL,
  PRIMARY KEY (`game_account_id`),
  KEY `FK6cfiyosmkmtj2euuoe9t08h7w` (`user_id`),
  CONSTRAINT `FK6cfiyosmkmtj2euuoe9t08h7w` FOREIGN KEY (`user_id`) REFERENCES `user` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=2 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `game_mode`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE IF NOT EXISTS `game_mode` (
  `game_mode_id` bigint NOT NULL AUTO_INCREMENT,
  `created_at` datetime(6) NOT NULL,
  `is_active` bit(1) NOT NULL,
  `mode_code` varchar(20) COLLATE utf8mb4_unicode_ci NOT NULL,
  `name` varchar(50) COLLATE utf8mb4_unicode_ci NOT NULL,
  `updated_at` datetime(6) NOT NULL,
  PRIMARY KEY (`game_mode_id`),
  UNIQUE KEY `UKpt46av3rkfvpr146m0jsimv3s` (`mode_code`)
) ENGINE=InnoDB AUTO_INCREMENT=5 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `game_rank`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE IF NOT EXISTS `game_rank` (
  `rank_id` bigint NOT NULL AUTO_INCREMENT,
  `created_at` datetime(6) NOT NULL,
  `deleted_at` datetime(6) DEFAULT NULL,
  `is_active` bit(1) NOT NULL,
  `updated_at` datetime(6) NOT NULL,
  `losses` int NOT NULL,
  `queue_type` varchar(255) COLLATE utf8mb4_unicode_ci NOT NULL,
  `rank_division` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `tier` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `win_rate` double NOT NULL,
  `wins` int NOT NULL,
  `game_account_id` bigint NOT NULL,
  PRIMARY KEY (`rank_id`),
  KEY `FKmdxb87uag14j2q72gsrg5g3x8` (`game_account_id`),
  CONSTRAINT `FKmdxb87uag14j2q72gsrg5g3x8` FOREIGN KEY (`game_account_id`) REFERENCES `game_account` (`game_account_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `match_history`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE IF NOT EXISTS `match_history` (
  `match_id` bigint NOT NULL AUTO_INCREMENT,
  `created_at` datetime(6) NOT NULL,
  `deleted_at` datetime(6) DEFAULT NULL,
  `is_active` bit(1) NOT NULL,
  `updated_at` datetime(6) NOT NULL,
  `game_duration` int NOT NULL,
  `game_start_timestamp` bigint NOT NULL,
  `queue_id` int NOT NULL,
  `riot_match_id` varchar(255) COLLATE utf8mb4_unicode_ci NOT NULL,
  `win` bit(1) NOT NULL,
  `game_account_id` bigint NOT NULL,
  PRIMARY KEY (`match_id`),
  UNIQUE KEY `UKd4khpo6jdp33jgbm1y2ej534g` (`riot_match_id`,`game_account_id`),
  KEY `FK8m229t2rltthvqo7dhb5mv1ej` (`game_account_id`),
  CONSTRAINT `FK8m229t2rltthvqo7dhb5mv1ej` FOREIGN KEY (`game_account_id`) REFERENCES `game_account` (`game_account_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `match_participant`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE IF NOT EXISTS `match_participant` (
  `match_participant_id` bigint NOT NULL AUTO_INCREMENT,
  `created_at` datetime(6) NOT NULL,
  `deleted_at` datetime(6) DEFAULT NULL,
  `is_active` bit(1) NOT NULL,
  `updated_at` datetime(6) NOT NULL,
  `assists` int NOT NULL,
  `champion_id` int NOT NULL,
  `champion_name` varchar(255) COLLATE utf8mb4_unicode_ci NOT NULL,
  `cs` int NOT NULL,
  `deaths` int NOT NULL,
  `item0` int DEFAULT NULL,
  `item1` int DEFAULT NULL,
  `item2` int DEFAULT NULL,
  `item3` int DEFAULT NULL,
  `item4` int DEFAULT NULL,
  `item5` int DEFAULT NULL,
  `item6` int DEFAULT NULL,
  `kda` double NOT NULL,
  `kills` int NOT NULL,
  `level` int NOT NULL,
  `perks` text COLLATE utf8mb4_unicode_ci,
  `puuid` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `spell1_id` int NOT NULL,
  `spell2_id` int NOT NULL,
  `game_account_id` bigint NOT NULL,
  `match_id` bigint NOT NULL,
  PRIMARY KEY (`match_participant_id`),
  UNIQUE KEY `UKqdf51mw8f34ofyfd71vmdxi5y` (`match_id`,`game_account_id`),
  KEY `FKsohhepnawxki2re4mgkfpvbks` (`game_account_id`),
  CONSTRAINT `FKruxftmrsxiptqpog3cxl6wv5a` FOREIGN KEY (`match_id`) REFERENCES `match_history` (`match_id`),
  CONSTRAINT `FKsohhepnawxki2re4mgkfpvbks` FOREIGN KEY (`game_account_id`) REFERENCES `game_account` (`game_account_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `notification`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE IF NOT EXISTS `notification` (
  `notification_id` bigint NOT NULL AUTO_INCREMENT,
  `created_at` datetime(6) NOT NULL,
  `is_read` bit(1) NOT NULL,
  `message` varchar(255) COLLATE utf8mb4_unicode_ci NOT NULL,
  `read_at` datetime(6) DEFAULT NULL,
  `target_id` bigint NOT NULL,
  `title` varchar(100) COLLATE utf8mb4_unicode_ci NOT NULL,
  `type` enum('CHAT_EXIT','NEW_CHAT','RECRUITMENT_COMPLETED','REVIEW_REQUEST') COLLATE utf8mb4_unicode_ci NOT NULL,
  `user_id` bigint NOT NULL,
  PRIMARY KEY (`notification_id`),
  KEY `FKb0yvoep4h4k92ipon31wmdf7e` (`user_id`),
  CONSTRAINT `FKb0yvoep4h4k92ipon31wmdf7e` FOREIGN KEY (`user_id`) REFERENCES `user` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `party`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE IF NOT EXISTS `party` (
  `party_id` bigint NOT NULL AUTO_INCREMENT,
  `created_at` datetime(6) NOT NULL,
  `deleted_at` datetime(6) DEFAULT NULL,
  `is_active` bit(1) NOT NULL,
  `updated_at` datetime(6) NOT NULL,
  `closed_at` datetime(6) DEFAULT NULL,
  `expires_at` datetime(6) DEFAULT NULL,
  `leader_id` bigint NOT NULL,
  `post_id` bigint NOT NULL,
  `status` enum('ACTIVE','CLOSED','RECRUIT') COLLATE utf8mb4_unicode_ci NOT NULL,
  PRIMARY KEY (`party_id`)
) ENGINE=InnoDB AUTO_INCREMENT=2 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `party_member`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE IF NOT EXISTS `party_member` (
  `party_member_id` bigint NOT NULL AUTO_INCREMENT,
  `joined_at` datetime(6) NOT NULL,
  `left_at` datetime(6) DEFAULT NULL,
  `role` enum('LEADER','MEMBER') COLLATE utf8mb4_unicode_ci NOT NULL,
  `state` enum('JOINED','LEFT') COLLATE utf8mb4_unicode_ci NOT NULL,
  `party_id` bigint NOT NULL,
  `user_id` bigint NOT NULL,
  PRIMARY KEY (`party_member_id`),
  UNIQUE KEY `uk_party_member_user` (`party_id`,`user_id`),
  KEY `FKrheb2ixn81y9crg562uxq9gjb` (`user_id`),
  CONSTRAINT `FKctrpcp93h130dwe6j1jlhf960` FOREIGN KEY (`party_id`) REFERENCES `party` (`party_id`),
  CONSTRAINT `FKrheb2ixn81y9crg562uxq9gjb` FOREIGN KEY (`user_id`) REFERENCES `user` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=2 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `post`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE IF NOT EXISTS `post` (
  `post_id` bigint NOT NULL AUTO_INCREMENT,
  `created_at` datetime(6) NOT NULL,
  `deleted_at` datetime(6) DEFAULT NULL,
  `is_active` bit(1) NOT NULL,
  `updated_at` datetime(6) NOT NULL,
  `game_mode` enum('ARENA','HOWLING_ABYSS','SUMMONERS_RIFT') COLLATE utf8mb4_unicode_ci NOT NULL,
  `looking_positions` json NOT NULL,
  `memo` text COLLATE utf8mb4_unicode_ci,
  `mic` bit(1) NOT NULL,
  `my_position` enum('ADC','ANY','JUNGLE','MID','SUPPORT','TOP') COLLATE utf8mb4_unicode_ci NOT NULL,
  `queue_type` enum('DUO','FLEX','NORMAL') COLLATE utf8mb4_unicode_ci NOT NULL,
  `recruit_count` int NOT NULL,
  `status` enum('ACTIVE','CLOSED','RECRUIT') COLLATE utf8mb4_unicode_ci NOT NULL,
  `user_id` bigint NOT NULL,
  PRIMARY KEY (`post_id`),
  KEY `FK72mt33dhhs48hf9gcqrq4fxte` (`user_id`),
  CONSTRAINT `FK72mt33dhhs48hf9gcqrq4fxte` FOREIGN KEY (`user_id`) REFERENCES `user` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=2 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `refresh_token`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE IF NOT EXISTS `refresh_token` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `expires_at` datetime(6) NOT NULL,
  `token` tinytext COLLATE utf8mb4_unicode_ci NOT NULL,
  `user_id` bigint NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_refresh_token_user_id` (`user_id`)
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `review`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE IF NOT EXISTS `review` (
  `review_id` bigint NOT NULL AUTO_INCREMENT,
  `created_at` datetime(6) NOT NULL,
  `deleted_at` datetime(6) DEFAULT NULL,
  `is_active` bit(1) NOT NULL,
  `updated_at` datetime(6) NOT NULL,
  `content` varchar(100) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `emoji` enum('BAD','GOOD','NORMAL') COLLATE utf8mb4_unicode_ci NOT NULL,
  `post_id` bigint NOT NULL,
  `review_request_id` bigint DEFAULT NULL,
  `reviewee_id` bigint NOT NULL,
  `reviewer_id` bigint NOT NULL,
  PRIMARY KEY (`review_id`),
  UNIQUE KEY `uk_review_reviewer_reviewee_post` (`reviewer_id`,`reviewee_id`,`post_id`),
  KEY `FKrl7b0my7pmicpl5l591p7qdu7` (`post_id`),
  KEY `FKex04qrw6rnhqgcx6a7fhav492` (`review_request_id`),
  KEY `FKrxxkeo5xlq721tgwpnyfx326i` (`reviewee_id`),
  CONSTRAINT `FKex04qrw6rnhqgcx6a7fhav492` FOREIGN KEY (`review_request_id`) REFERENCES `review_request` (`review_request_id`),
  CONSTRAINT `FKrl7b0my7pmicpl5l591p7qdu7` FOREIGN KEY (`post_id`) REFERENCES `post` (`post_id`),
  CONSTRAINT `FKrxxkeo5xlq721tgwpnyfx326i` FOREIGN KEY (`reviewee_id`) REFERENCES `user` (`id`),
  CONSTRAINT `FKt58e9mdgxpl7j90ketlaosmx4` FOREIGN KEY (`reviewer_id`) REFERENCES `user` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `review_request`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE IF NOT EXISTS `review_request` (
  `review_request_id` bigint NOT NULL AUTO_INCREMENT,
  `created_at` datetime(6) NOT NULL,
  `deleted_at` datetime(6) DEFAULT NULL,
  `is_active` bit(1) NOT NULL,
  `updated_at` datetime(6) NOT NULL,
  `expires_at` datetime(6) NOT NULL,
  `status` enum('COMPLETED','PENDING') COLLATE utf8mb4_unicode_ci NOT NULL,
  `post_id` bigint NOT NULL,
  `request_user_id` bigint NOT NULL,
  PRIMARY KEY (`review_request_id`),
  UNIQUE KEY `uk_review_request_post_user` (`post_id`,`request_user_id`),
  KEY `FKi6fybm4ude799rrg8wj2i3abl` (`request_user_id`),
  CONSTRAINT `FK3ak0tik0pid86c0tfrixbbga4` FOREIGN KEY (`post_id`) REFERENCES `post` (`post_id`),
  CONSTRAINT `FKi6fybm4ude799rrg8wj2i3abl` FOREIGN KEY (`request_user_id`) REFERENCES `user` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `user`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE IF NOT EXISTS `user` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `created_at` datetime(6) NOT NULL,
  `deleted_at` datetime(6) DEFAULT NULL,
  `is_active` bit(1) NOT NULL,
  `updated_at` datetime(6) NOT NULL,
  `comment` varchar(100) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `email` varchar(40) COLLATE utf8mb4_unicode_ci NOT NULL,
  `nickname` varchar(20) COLLATE utf8mb4_unicode_ci NOT NULL,
  `password` varchar(100) COLLATE utf8mb4_unicode_ci NOT NULL,
  `profile_image` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `verification_code` varchar(100) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `UKob8kqyqqgmefl0aco34akdtpe` (`email`),
  UNIQUE KEY `UKn4swgcf30j6bmtb4l4cjryuym` (`nickname`)
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `user_bans`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE IF NOT EXISTS `user_bans` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `created_at` datetime(6) NOT NULL,
  `deleted_at` datetime(6) DEFAULT NULL,
  `is_active` bit(1) NOT NULL,
  `updated_at` datetime(6) NOT NULL,
  `from_user_id` bigint NOT NULL,
  `to_user_id` bigint NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `UKmnp4piipnh3wwmbbhlpxwrqpx` (`from_user_id`,`to_user_id`),
  KEY `FKrr3old8ryw4df15s0s1yfnciy` (`to_user_id`),
  CONSTRAINT `FKg6un4lwqegtnuey0iyhcnsupt` FOREIGN KEY (`from_user_id`) REFERENCES `user` (`id`),
  CONSTRAINT `FKrr3old8ryw4df15s0s1yfnciy` FOREIGN KEY (`to_user_id`) REFERENCES `user` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `verification`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE IF NOT EXISTS `verification` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `code` varchar(255) COLLATE utf8mb4_unicode_ci NOT NULL,
  `email` varchar(255) COLLATE utf8mb4_unicode_ci NOT NULL,
  `expires_at` datetime(6) NOT NULL,
  `verified` bit(1) NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `UK4w12b7ntrv8tuy104c1vkqqbk` (`email`)
) ENGINE=InnoDB AUTO_INCREMENT=8 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

-- Dump completed on 2025-12-26  2:57:20
