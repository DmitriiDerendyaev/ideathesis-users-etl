-- =============================
-- Таблица пользователей
-- =============================
CREATE TABLE users (
    guid UUID PRIMARY KEY,
    full_name VARCHAR(255),
    first_name VARCHAR(255),
    last_name VARCHAR(255),
    middle_name VARCHAR(255),
    email VARCHAR(255) UNIQUE,
    phone VARCHAR(20),
    user_type VARCHAR(20) CHECK (user_type IN ('student', 'employee')) NOT NULL,
    version BIGINT,
    created_at TIMESTAMP,
    updated_at TIMESTAMP
);

-- =============================
-- Справочники для студентов
-- =============================

CREATE TABLE student_groups (
    id SERIAL PRIMARY KEY,
    name VARCHAR(255) UNIQUE NOT NULL
);

CREATE TABLE departments (
    id SERIAL PRIMARY KEY,
    name VARCHAR(255) UNIQUE NOT NULL
);

CREATE TABLE degree_levels (
    id SERIAL PRIMARY KEY,
    name VARCHAR(100) UNIQUE NOT NULL
);

CREATE TABLE degree_forms (
    id SERIAL PRIMARY KEY,
    name VARCHAR(100) UNIQUE NOT NULL
);

-- =============================
-- Таблица студентов
-- =============================

CREATE TABLE students (
    guid UUID PRIMARY KEY REFERENCES users(guid) ON DELETE CASCADE,
    group_id INT REFERENCES student_groups(id),
    course INT,
    start_year INT,
    department_id INT REFERENCES departments(id),
    degree_level_id INT REFERENCES degree_levels(id),
    degree_form_id INT REFERENCES degree_forms(id),
    version BIGINT
);

-- =============================
-- Справочники для сотрудников
-- =============================

CREATE TABLE job_titles (
    id SERIAL PRIMARY KEY,
    name VARCHAR(255) UNIQUE NOT NULL
);

CREATE TABLE staff_categories (
    id SERIAL PRIMARY KEY,
    name VARCHAR(255) UNIQUE NOT NULL
);

CREATE TABLE employment_types (
    id SERIAL PRIMARY KEY,
    name VARCHAR(255) UNIQUE NOT NULL
);

-- =============================
-- Подразделения
-- =============================

CREATE TABLE subdivisions (
    id SERIAL PRIMARY KEY,
    name VARCHAR(255) UNIQUE NOT NULL,
    guid UUID UNIQUE NOT NULL
);

-- =============================
-- Таблица сотрудников
-- =============================

CREATE TABLE employees (
    guid UUID PRIMARY KEY REFERENCES users(guid) ON DELETE CASCADE,
    full_name VARCHAR(255),
    surname VARCHAR(255),
    email VARCHAR(255) UNIQUE,
    date_of_birth DATE,
    version BIGINT
);

-- =============================
-- Таблица трудоустройства сотрудников
-- =============================

CREATE TABLE employee_employments (
    id SERIAL PRIMARY KEY,
    employee_guid UUID REFERENCES employees(guid) ON DELETE CASCADE,
    job_title_id INT REFERENCES job_titles(id),
    staff_category_id INT REFERENCES staff_categories(id),
    employment_type_id INT REFERENCES employment_types(id),
    subdivision_id INT REFERENCES subdivisions(id),
    job_state VARCHAR(255)
);