-- MySQL dump 10.13  Distrib 8.0.46, for Win64 (x86_64)
--
-- Host: localhost    Database: eventapp
-- ------------------------------------------------------
-- Server version	8.0.46

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
-- Table structure for table `business_category`
--

DROP TABLE IF EXISTS `business_category`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `business_category` (
  `business_category_id` bigint NOT NULL AUTO_INCREMENT,
  `business_category_name` varchar(255) NOT NULL,
  PRIMARY KEY (`business_category_id`)
) ENGINE=InnoDB AUTO_INCREMENT=10 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `business_category`
--

LOCK TABLES `business_category` WRITE;
/*!40000 ALTER TABLE `business_category` DISABLE KEYS */;
INSERT INTO `business_category` VALUES (1,'Planificator eveniment'),(2,'Fotograf'),(3,'DJ'),(4,'Decor'),(5,'Candy Bar'),(6,'Transport'),(7,'Entertainment'),(8,'Muzică live'),(9,'Altele');
/*!40000 ALTER TABLE `business_category` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `business_image`
--

DROP TABLE IF EXISTS `business_image`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `business_image` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `image_path` varchar(255) DEFAULT NULL,
  `business_id` bigint DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `FK87hrakbs1het1ll0a35edetgm` (`business_id`),
  CONSTRAINT `FK87hrakbs1het1ll0a35edetgm` FOREIGN KEY (`business_id`) REFERENCES `business_profile` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=14 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `business_image`
--

LOCK TABLES `business_image` WRITE;
/*!40000 ALTER TABLE `business_image` DISABLE KEYS */;
INSERT INTO `business_image` VALUES (1,'/images/1780859158635_img1.jpeg',32),(3,'/images/1780859158672_img3.jpg',32),(4,'/images/1780860308833_cover.jpg',32),(5,'/images/1780860308886_img1.jpeg',32),(6,'/images/1780860308896_img2.jpg',32),(7,'/images/1780860308910_img3.jpg',32),(10,'/images/1780865571707_img2.jpg',20);
/*!40000 ALTER TABLE `business_image` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `business_profile`
--

DROP TABLE IF EXISTS `business_profile`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `business_profile` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `category` varchar(255) NOT NULL,
  `city` varchar(255) NOT NULL,
  `description` varchar(700) NOT NULL,
  `name` varchar(50) NOT NULL,
  `phone` varchar(255) NOT NULL,
  `user_id` bigint DEFAULT NULL,
  `image_path` varchar(255) DEFAULT NULL,
  `email` varchar(255) DEFAULT NULL,
  `website` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `FKods55f5jne02efktw6da0sn7p` (`user_id`),
  CONSTRAINT `FKods55f5jne02efktw6da0sn7p` FOREIGN KEY (`user_id`) REFERENCES `user` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=40 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `business_profile`
--

LOCK TABLES `business_profile` WRITE;
/*!40000 ALTER TABLE `business_profile` DISABLE KEYS */;
INSERT INTO `business_profile` VALUES (20,'ORGANIZATOR','Bucharest','Luxury wedding planning services with complete event organization, decor, venues and entertainment.','Royal Wedding Events','0712345678',1,'https://images.unsplash.com/photo-1519741497674-611481863552?q=80&w=1200',NULL,NULL),(21,'FOTOGRAF','Cluj-Napoca','Professional wedding and event photography with cinematic editing and premium albums.','Golden Lens Photography','0723456789',1,'https://images.unsplash.com/photo-1520854221256-17451cc331bf?q=80&w=1200',NULL,NULL),(24,'DECOR','Bucharest','Servicii complete de decor pentru nunți, botezuri și petreceri private: aranjamente florale, photo corner, lumini ambientale și decor tematic.','Bloom Decor Studio','0751122334',1,'https://images.unsplash.com/photo-1523438885200-e635ba2c371e?q=80&w=1200','bloom@mail.com','https://railway.com/'),(25,'CANDY_BAR','Brasov','Candy bar personalizat pentru evenimente speciale, cu prăjituri, macarons, cupcakes și torturi tematice pregătite artizanal.','Sweet Moments Candy Bar','0752233445',1,'https://images.unsplash.com/photo-1488477181946-6428a0291777?q=80&w=1200',NULL,NULL),(26,'TRANSPORT','Cluj-Napoca','Închiriere mașini premium pentru nunți, botezuri și evenimente corporate. Oferim șofer profesionist și decor elegant pentru mașină.','Luxury Ride Events','0753344556',1,'https://images.unsplash.com/photo-1503376780353-7e6692767b70?q=80&w=1200',NULL,NULL),(27,'DIVERTISMENT','Iasi','Servicii de entertainment pentru copii: animatori, mascote, face painting, baloane modelabile și jocuri interactive pentru aniversări și botezuri.','Magic Kids Party','0754455667',1,'https://images.unsplash.com/photo-1530103862676-de8c9debad1d?q=80&w=1200',NULL,NULL),(28,'MUZICA_LIVE','Timisoara','Formație live pentru nunți, petreceri private și evenimente corporate. Repertoriu variat, sonorizare profesională și atmosferă premium.','Harmony Live Band','0755566778',1,'https://images.unsplash.com/photo-1516280440614-37939bbacd81?q=80&w=1200',NULL,NULL),(31,'CANDY_BAR','Bucharest','Lorem Ipsum is simply dummy text of the printing and typesetting industry. Lorem Ipsum has been the industry\'s standard dummy text ever since 1966, when designers at Letraset and James Mosley, the librarian at St Bride Printing Library, took a 1914 Cicero translation and scrambled it to make dummy text for Letraset\'s Body Type sheets. It has survived not only many decades, but also the leap into electronic typesetting, remaining essentially unchanged. It was popularised thanks to these sheets and more recently with desktop publishing software including versions of Lorem Ipsum.','Sweet Candy','0722876456',1,'https://i.ibb.co/tT448LTw/Stock-Cake-Sweet-Candy-Delight-397785-small.jpg',NULL,NULL),(32,'DJ','București','Junk descriptionn','DJ FURRY','0754111398',1,'/images/1780860909073_img1.jpeg',NULL,NULL);
/*!40000 ALTER TABLE `business_profile` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `business_unavailable_date`
--

DROP TABLE IF EXISTS `business_unavailable_date`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `business_unavailable_date` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `unavailable_date` date DEFAULT NULL,
  `business_id` bigint DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `FK5jn7y2gehyvltsghggyd3698d` (`business_id`),
  CONSTRAINT `FK5jn7y2gehyvltsghggyd3698d` FOREIGN KEY (`business_id`) REFERENCES `business_profile` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=35 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `business_unavailable_date`
--

LOCK TABLES `business_unavailable_date` WRITE;
/*!40000 ALTER TABLE `business_unavailable_date` DISABLE KEYS */;
INSERT INTO `business_unavailable_date` VALUES (10,'2026-06-04',20),(11,'2026-06-05',20),(12,'2026-06-06',20),(13,'2026-10-15',21);
/*!40000 ALTER TABLE `business_unavailable_date` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `favorite_businesses`
--

DROP TABLE IF EXISTS `favorite_businesses`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `favorite_businesses` (
  `user_id` bigint NOT NULL,
  `business_id` bigint NOT NULL,
  PRIMARY KEY (`user_id`,`business_id`),
  KEY `FKh5ysv7qfhubsnvwej21m2jake` (`business_id`),
  CONSTRAINT `FK4654l3xa07a01if6ropkn7tya` FOREIGN KEY (`user_id`) REFERENCES `user` (`id`),
  CONSTRAINT `FKh5ysv7qfhubsnvwej21m2jake` FOREIGN KEY (`business_id`) REFERENCES `business_profile` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `favorite_businesses`
--

LOCK TABLES `favorite_businesses` WRITE;
/*!40000 ALTER TABLE `favorite_businesses` DISABLE KEYS */;
/*!40000 ALTER TABLE `favorite_businesses` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `review`
--

DROP TABLE IF EXISTS `review`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `review` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `comment` varchar(1000) NOT NULL,
  `rating` int NOT NULL,
  `business_id` bigint DEFAULT NULL,
  `user_id` bigint DEFAULT NULL,
  `created_at` datetime(6) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `FK30tswoebhh221k7pxr5pmia9a` (`business_id`),
  KEY `FKiyf57dy48lyiftdrf7y87rnxi` (`user_id`),
  CONSTRAINT `FK30tswoebhh221k7pxr5pmia9a` FOREIGN KEY (`business_id`) REFERENCES `business_profile` (`id`),
  CONSTRAINT `FKiyf57dy48lyiftdrf7y87rnxi` FOREIGN KEY (`user_id`) REFERENCES `user` (`id`),
  CONSTRAINT `review_chk_1` CHECK (((`rating` >= 1) and (`rating` <= 5)))
) ENGINE=InnoDB AUTO_INCREMENT=8 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `review`
--

LOCK TABLES `review` WRITE;
/*!40000 ALTER TABLE `review` DISABLE KEYS */;
INSERT INTO `review` VALUES (4,'Another test rating!',3,20,3,NULL),(5,'Test!',3,31,1,'2026-06-07 20:10:01.712942'),(6,'Test review!!',5,21,1,'2026-06-07 21:02:38.399816'),(7,'testaa',5,20,1,'2026-06-07 21:14:57.370185');
/*!40000 ALTER TABLE `review` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `user`
--

DROP TABLE IF EXISTS `user`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `user` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `email` varchar(255) DEFAULT NULL,
  `first_name` varchar(10) NOT NULL,
  `last_name` varchar(10) NOT NULL,
  `password` varchar(255) NOT NULL,
  `phone` varchar(255) NOT NULL,
  `role` enum('BUSINESS','USER') DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `UKob8kqyqqgmefl0aco34akdtpe` (`email`)
) ENGINE=InnoDB AUTO_INCREMENT=6 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `user`
--

LOCK TABLES `user` WRITE;
/*!40000 ALTER TABLE `user` DISABLE KEYS */;
INSERT INTO `user` VALUES (1,'eus.iacob@gmail.com','Eusebiu','Iacob','$2a$10$erT1oeNG8vTNpbrEWdNW6.0iDfITU5f5uvdcWk9zgUN9/fEeF8Xae','0749311573','BUSINESS'),(3,'alan@mail.com','Alan','Walker','$2a$10$c/knmunB8EUKoRPzO8myzOmKbXlFYITzHY7fagenoctlOa1SfKhJS','0711456387','USER'),(4,'alan2@mail.com','alan','baker','$2a$10$ZJ/xFar/XGll76L39ys0h.5sAeX6hKZLasXsGuzCu5AVHWusA9bHa','0729765467','USER'),(5,'ionut.tanase@gmail.com','Ionut','Tanase','$2a$10$hldf8dxEJR1I4X9yWbf03uxcKsRjCKFGfuLJ4SXAxssFNpEfst3j2','0765998567','USER');
/*!40000 ALTER TABLE `user` ENABLE KEYS */;
UNLOCK TABLES;
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

-- Dump completed on 2026-06-22 19:11:03
