-- MySQL Schema for Government Scheme Recommendation System
CREATE DATABASE IF NOT EXISTS gov_scheme_db;
USE gov_scheme_db;

-- Users Table
CREATE TABLE IF NOT EXISTS users (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    email VARCHAR(100) NOT NULL UNIQUE,
    mobile VARCHAR(15) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    role VARCHAR(20) NOT NULL DEFAULT 'USER',
    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

-- User Profiles Table
CREATE TABLE IF NOT EXISTS user_profiles (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL UNIQUE,
    date_of_birth DATE,
    age INT,
    gender VARCHAR(20),
    state VARCHAR(100),
    district VARCHAR(100),
    annual_income DOUBLE,
    occupation VARCHAR(100),
    employment_status VARCHAR(50),
    category VARCHAR(50),
    student_status BOOLEAN DEFAULT FALSE,
    disability_status BOOLEAN DEFAULT FALSE,
    farmer_status BOOLEAN DEFAULT FALSE,
    entrepreneur_status BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

-- Scheme Categories Table
CREATE TABLE IF NOT EXISTS scheme_categories (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL UNIQUE,
    description TEXT,
    icon_name VARCHAR(50) DEFAULT 'bi-folder',
    status VARCHAR(20) DEFAULT 'ACTIVE'
);

-- Schemes Table
CREATE TABLE IF NOT EXISTS schemes (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    category_id BIGINT NOT NULL,
    scheme_name VARCHAR(255) NOT NULL UNIQUE,
    short_description TEXT NOT NULL,
    description LONGTEXT,
    department VARCHAR(150) NOT NULL,
    scheme_type VARCHAR(50) NOT NULL DEFAULT 'CENTRAL', -- CENTRAL / STATE
    benefits LONGTEXT NOT NULL,
    application_url VARCHAR(500),
    contact_information TEXT,
    status VARCHAR(20) DEFAULT 'ACTIVE',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (category_id) REFERENCES scheme_categories(id) ON DELETE CASCADE
);

-- Eligibility Rules Table
CREATE TABLE IF NOT EXISTS eligibility_rules (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    scheme_id BIGINT NOT NULL UNIQUE,
    min_age INT,
    max_age INT,
    min_income DOUBLE,
    max_income DOUBLE,
    gender VARCHAR(20) DEFAULT 'ALL',
    state VARCHAR(100) DEFAULT 'ALL',
    district VARCHAR(100) DEFAULT 'ALL',
    category VARCHAR(50) DEFAULT 'ALL',
    occupation VARCHAR(100) DEFAULT 'ALL',
    employment_status VARCHAR(50) DEFAULT 'ALL',
    student_status BOOLEAN DEFAULT NULL,
    disability_status BOOLEAN DEFAULT NULL,
    farmer_status BOOLEAN DEFAULT NULL,
    entrepreneur_status BOOLEAN DEFAULT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (scheme_id) REFERENCES schemes(id) ON DELETE CASCADE
);

-- Required Documents Table
CREATE TABLE IF NOT EXISTS required_documents (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    scheme_id BIGINT NOT NULL,
    document_name VARCHAR(150) NOT NULL,
    description VARCHAR(255),
    mandatory BOOLEAN DEFAULT TRUE,
    FOREIGN KEY (scheme_id) REFERENCES schemes(id) ON DELETE CASCADE
);

-- Application Procedures Table
CREATE TABLE IF NOT EXISTS application_procedures (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    scheme_id BIGINT NOT NULL,
    step_number INT NOT NULL,
    instruction TEXT NOT NULL,
    FOREIGN KEY (scheme_id) REFERENCES schemes(id) ON DELETE CASCADE
);

-- Saved Schemes Table
CREATE TABLE IF NOT EXISTS saved_schemes (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    scheme_id BIGINT NOT NULL,
    saved_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    UNIQUE KEY unique_user_scheme (user_id, scheme_id),
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (scheme_id) REFERENCES schemes(id) ON DELETE CASCADE
);
