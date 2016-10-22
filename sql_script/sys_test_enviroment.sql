CREATE TABLE `sys_test_enviroment` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `original_id` varchar(255) DEFAULT NULL,
  `test_id` varchar(255) DEFAULT NULL,
  `created_date` datetime DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=8 DEFAULT CHARSET=utf8
