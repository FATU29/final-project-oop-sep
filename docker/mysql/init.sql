-- Initialize MySQL database for SEP Framework
-- This script runs automatically when MySQL container starts for the first time

-- Create database if not exists (already created by MYSQL_DATABASE env var)
-- USE sep_demo;

-- Set charset
SET NAMES utf8mb4;
SET CHARACTER SET utf8mb4;

-- You can add initial data or schema here if needed
-- Example:
-- CREATE TABLE IF NOT EXISTS sample_table (
--     id INT AUTO_INCREMENT PRIMARY KEY,
--     name VARCHAR(100) NOT NULL,
--     created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
-- );

