-- UniCanteen database schema (MySQL 8.0+)
-- Run this script in MySQL Workbench or the VS Code SQL extension.
CREATE DATABASE IF NOT EXISTS unicanteen
  CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE unicanteen;

SET FOREIGN_KEY_CHECKS = 0;
DROP VIEW IF EXISTS v_order_summary;
DROP TABLE IF EXISTS payments;
DROP TABLE IF EXISTS order_details;
DROP TABLE IF EXISTS orders;
DROP TABLE IF EXISTS users;
DROP TABLE IF EXISTS food_items;
DROP TABLE IF EXISTS categories;
DROP TABLE IF EXISTS students;
SET FOREIGN_KEY_CHECKS = 1;

CREATE TABLE students (
    student_id INT AUTO_INCREMENT PRIMARY KEY,
    roll_no VARCHAR(20) NOT NULL,
    full_name VARCHAR(80) NOT NULL,
    email VARCHAR(100) NOT NULL,
    phone VARCHAR(20),
    department VARCHAR(60) NOT NULL DEFAULT 'General',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uq_students_roll UNIQUE (roll_no),
    CONSTRAINT uq_students_email UNIQUE (email),
    CONSTRAINT ck_students_email CHECK (email LIKE '%@%')
) ENGINE=InnoDB;

CREATE TABLE categories (
    category_id INT AUTO_INCREMENT PRIMARY KEY,
    category_name VARCHAR(50) NOT NULL,
    description VARCHAR(200),
    CONSTRAINT uq_categories_name UNIQUE (category_name)
) ENGINE=InnoDB;

CREATE TABLE food_items (
    food_id INT AUTO_INCREMENT PRIMARY KEY,
    food_name VARCHAR(80) NOT NULL,
    category_id INT NOT NULL,
    price DECIMAL(8,2) NOT NULL,
    availability CHAR(1) NOT NULL DEFAULT 'Y',
    image_path VARCHAR(255),
    description VARCHAR(200),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uq_food_name UNIQUE (food_name),
    CONSTRAINT ck_food_price CHECK (price > 0),
    CONSTRAINT ck_food_avail CHECK (availability IN ('Y','N')),
    CONSTRAINT fk_food_cat FOREIGN KEY (category_id) REFERENCES categories(category_id),
    INDEX ix_food_cat (category_id)
) ENGINE=InnoDB;

CREATE TABLE orders (
    order_id INT AUTO_INCREMENT PRIMARY KEY,
    student_id INT NOT NULL,
    order_date TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    status VARCHAR(15) NOT NULL DEFAULT 'PENDING',
    total_amount DECIMAL(10,2) NOT NULL DEFAULT 0,
    notes VARCHAR(200),
    CONSTRAINT ck_orders_status CHECK (status IN
      ('PENDING','ACCEPTED','PREPARING','READY','COMPLETED','REJECTED','CANCELLED')),
    CONSTRAINT ck_orders_total CHECK (total_amount >= 0),
    CONSTRAINT fk_orders_student FOREIGN KEY (student_id) REFERENCES students(student_id),
    INDEX ix_orders_stu (student_id),
    INDEX ix_orders_status (status)
) ENGINE=InnoDB;

CREATE TABLE order_details (
    detail_id INT AUTO_INCREMENT PRIMARY KEY,
    order_id INT NOT NULL,
    food_id INT NOT NULL,
    quantity SMALLINT NOT NULL,
    unit_price DECIMAL(8,2) NOT NULL,
    line_total DECIMAL(10,2) NOT NULL,
    CONSTRAINT ck_detail_qty CHECK (quantity > 0),
    CONSTRAINT ck_detail_price CHECK (unit_price > 0),
    CONSTRAINT uq_detail_item UNIQUE (order_id, food_id),
    CONSTRAINT fk_detail_order FOREIGN KEY (order_id) REFERENCES orders(order_id) ON DELETE CASCADE,
    CONSTRAINT fk_detail_food FOREIGN KEY (food_id) REFERENCES food_items(food_id),
    INDEX ix_detail_food (food_id)
) ENGINE=InnoDB;

CREATE TABLE payments (
    payment_id INT AUTO_INCREMENT PRIMARY KEY,
    order_id INT NOT NULL,
    amount DECIMAL(10,2) NOT NULL,
    method VARCHAR(10) NOT NULL DEFAULT 'CASH',
    status VARCHAR(10) NOT NULL DEFAULT 'PENDING',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    paid_at TIMESTAMP NULL,
    CONSTRAINT uq_payment_order UNIQUE (order_id),
    CONSTRAINT ck_pay_amount CHECK (amount >= 0),
    CONSTRAINT ck_pay_method CHECK (method IN ('CASH','ONLINE')),
    CONSTRAINT ck_pay_status CHECK (status IN ('PENDING','PAID','FAILED','REFUNDED')),
    CONSTRAINT fk_pay_order FOREIGN KEY (order_id) REFERENCES orders(order_id) ON DELETE CASCADE
) ENGINE=InnoDB;

CREATE TABLE users (
    user_id INT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(40) NOT NULL,
    password_hash VARCHAR(64) NOT NULL,
    role VARCHAR(10) NOT NULL,
    student_id INT NULL,
    full_name VARCHAR(80) NOT NULL,
    is_active CHAR(1) NOT NULL DEFAULT 'Y',
    CONSTRAINT uq_users_username UNIQUE (username),
    CONSTRAINT ck_users_role CHECK (role IN ('OWNER','CUSTOMER')),
    CONSTRAINT ck_users_active CHECK (is_active IN ('Y','N')),
    CONSTRAINT fk_users_student FOREIGN KEY (student_id) REFERENCES students(student_id) ON DELETE CASCADE
) ENGINE=InnoDB;

CREATE OR REPLACE VIEW v_order_summary AS
SELECT o.order_id, s.full_name AS customer, o.order_date,
       o.status AS order_status, o.total_amount,
       p.method AS pay_method, p.status AS pay_status
FROM orders o
JOIN students s ON s.student_id = o.student_id
LEFT JOIN payments p ON p.order_id = o.order_id;
