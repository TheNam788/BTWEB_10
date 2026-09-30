-- ========================================================
-- DATABASE SCRIPT CHO BÀI TẬP SPRING BOOT JWT + NIMBUS JOSE
-- Database: jwt_nimbus (hoặc jwt_springboot3)
-- ========================================================

CREATE DATABASE IF NOT EXISTS `jwt_nimbus` CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE `jwt_nimbus`;

-- Bảng users
DROP TABLE IF EXISTS `users`;

CREATE TABLE `users` (
    `id` INT AUTO_INCREMENT PRIMARY KEY,
    `username` VARCHAR(100) UNIQUE NULL,
    `email` VARCHAR(100) NOT NULL UNIQUE,
    `password` VARCHAR(255) NOT NULL,
    `full_name` VARCHAR(150) NULL,
    `role` VARCHAR(50) NOT NULL DEFAULT 'USER',
    `enabled` BOOLEAN NOT NULL DEFAULT TRUE,
    `images` VARCHAR(500) NULL,
    `created_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    `updated_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Mật khẩu mẫu mặc định là: 123456 (được hash bằng BCrypt: $2a$10$w09ZlM.Qe3eP1e740Jv0kOJq8tKz8k2l1V/U5WkS19H6E5Qd4Nl3a)
INSERT INTO `users` (`username`, `email`, `password`, `full_name`, `role`, `enabled`)
VALUES 
('admin', 'admin@gmail.com', '$2a$10$3/2pW7k7aJmQoJ1f8rE78.h0YQW2z7E4J7qH0vY1E8V5s1l0G6Y5K', 'Administrator', 'ADMIN', 1),
('user', 'user@gmail.com', '$2a$10$3/2pW7k7aJmQoJ1f8rE78.h0YQW2z7E4J7qH0vY1E8V5s1l0G6Y5K', 'Nguyen Van A', 'USER', 1);
