CREATE TABLE `upload_audit_log` (
   `id` bigint(20) NOT NULL AUTO_INCREMENT,
   `upload_type` varchar(45) NOT NULL,
   `file_name` varchar(45) DEFAULT NULL,
   `uploaded_by` varchar(45) DEFAULT NULL,
   `uploaded_at` datetime DEFAULT NULL,
   `status` varchar(45) DEFAULT NULL,
   PRIMARY KEY (`id`)
 ) ENGINE=InnoDB AUTO_INCREMENT=34 DEFAULT CHARSET=latin1 COLLATE=latin1_swedish_ci