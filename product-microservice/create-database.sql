CREATE DATABASE IF NOT EXISTS product_db;
USE product_db;

CREATE TABLE IF NOT EXISTS products (
    id           BIGINT AUTO_INCREMENT PRIMARY KEY,
    name         VARCHAR(100)   NOT NULL,
    description  VARCHAR(500),
    price        DECIMAL(12, 2) NOT NULL,
    currency     VARCHAR(3)     NOT NULL DEFAULT 'INR',
    stock        INT            NOT NULL DEFAULT 0,
    category     VARCHAR(100)   NOT NULL,
    vendor_id    BIGINT         NOT NULL,
    status       ENUM('PENDING','APPROVED','REJECTED') NOT NULL DEFAULT 'PENDING',
    created_at   DATETIME       NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at   DATETIME       NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS vendor_requests (
    id                   BIGINT AUTO_INCREMENT PRIMARY KEY,
    vendor_id            BIGINT        NOT NULL,
    business_name        VARCHAR(200)  NOT NULL,
    contact_email        VARCHAR(200)  NOT NULL,
    business_description VARCHAR(1000),
    status               ENUM('PENDING','APPROVED','REJECTED') NOT NULL DEFAULT 'PENDING',
    admin_remarks        VARCHAR(500),
    requested_at         DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP,
    reviewed_at          DATETIME
);
