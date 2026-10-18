-- ============================================================================
-- Suraksha Database DDL Schema Script
-- Target Database: MySQL 8.0+
-- Character Set: utf8mb4 (Full Unicode Support)
-- ============================================================================

-- Create Database if not exists
CREATE DATABASE IF NOT EXISTS suraksha_db
  CHARACTER SET utf8mb4
  COLLATE utf8mb4_unicode_ci;

USE suraksha_db;

-- ----------------------------------------------------------------------------
-- 1. Authentication & Role-Based Access Control (RBAC)
-- ----------------------------------------------------------------------------

CREATE TABLE IF NOT EXISTS roles (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(50) NOT NULL UNIQUE,
    description VARCHAR(255) NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS users (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    full_name VARCHAR(100) NOT NULL,
    email VARCHAR(150) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    enabled BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_user_email (email)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS user_roles (
    user_id BIGINT NOT NULL,
    role_id BIGINT NOT NULL,
    PRIMARY KEY (user_id, role_id),
    CONSTRAINT fk_user_roles_user FOREIGN KEY (user_id)
        REFERENCES users (id) ON DELETE CASCADE,
    CONSTRAINT fk_user_roles_role FOREIGN KEY (role_id)
        REFERENCES roles (id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ----------------------------------------------------------------------------
-- 2. Educational Courses & Lesson Modules
-- ----------------------------------------------------------------------------

CREATE TABLE IF NOT EXISTS courses (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    slug VARCHAR(100) NOT NULL UNIQUE,
    title VARCHAR(200) NOT NULL,
    category VARCHAR(50) NOT NULL,
    difficulty VARCHAR(30) NOT NULL DEFAULT 'BEGINNER',
    estimated_minutes INT NOT NULL DEFAULT 20,
    summary TEXT NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_courses_category (category)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS lessons (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    course_id BIGINT NOT NULL,
    slug VARCHAR(100) NOT NULL,
    title VARCHAR(200) NOT NULL,
    sequence_order INT NOT NULL DEFAULT 1,
    content MEDIUMTEXT NOT NULL,
    dos_guidelines TEXT NULL,
    donts_guidelines TEXT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_lessons_course FOREIGN KEY (course_id)
        REFERENCES courses (id) ON DELETE CASCADE,
    CONSTRAINT uk_course_lesson_order UNIQUE (course_id, sequence_order),
    CONSTRAINT uk_course_lesson_slug UNIQUE (course_id, slug)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ----------------------------------------------------------------------------
-- 3. Quizzes, Questions, Answer Options & Attempts
-- ----------------------------------------------------------------------------

CREATE TABLE IF NOT EXISTS quizzes (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    course_id BIGINT NOT NULL UNIQUE,
    slug VARCHAR(100) NOT NULL UNIQUE,
    title VARCHAR(200) NOT NULL,
    passing_score_percent INT NOT NULL DEFAULT 75,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_quizzes_course FOREIGN KEY (course_id)
        REFERENCES courses (id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS quiz_questions (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    quiz_id BIGINT NOT NULL,
    sequence_order INT NOT NULL DEFAULT 1,
    question_text TEXT NOT NULL,
    explanation TEXT NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_quiz_questions_quiz FOREIGN KEY (quiz_id)
        REFERENCES quizzes (id) ON DELETE CASCADE,
    CONSTRAINT uk_quiz_question_order UNIQUE (quiz_id, sequence_order)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS quiz_options (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    question_id BIGINT NOT NULL,
    option_text VARCHAR(500) NOT NULL,
    is_correct BOOLEAN NOT NULL DEFAULT FALSE,
    sequence_order INT NOT NULL DEFAULT 1,
    CONSTRAINT fk_quiz_options_question FOREIGN KEY (question_id)
        REFERENCES quiz_questions (id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS quiz_attempts (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    quiz_id BIGINT NOT NULL,
    score_percent INT NOT NULL,
    total_questions INT NOT NULL,
    correct_answers INT NOT NULL,
    passed BOOLEAN NOT NULL,
    attempted_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_quiz_attempts_user FOREIGN KEY (user_id)
        REFERENCES users (id) ON DELETE CASCADE,
    CONSTRAINT fk_quiz_attempts_quiz FOREIGN KEY (quiz_id)
        REFERENCES quizzes (id) ON DELETE CASCADE,
    INDEX idx_user_quiz_attempts (user_id, quiz_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ----------------------------------------------------------------------------
-- 4. Virtual Mock Drill Simulations
-- ----------------------------------------------------------------------------

CREATE TABLE IF NOT EXISTS drill_scenarios (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    course_id BIGINT NULL,
    slug VARCHAR(100) NOT NULL UNIQUE,
    title VARCHAR(200) NOT NULL,
    hazard_category VARCHAR(50) NOT NULL,
    description TEXT NOT NULL,
    initial_step_id BIGINT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_drill_scenarios_course FOREIGN KEY (course_id)
        REFERENCES courses (id) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS drill_steps (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    scenario_id BIGINT NOT NULL,
    step_key VARCHAR(50) NOT NULL,
    situation_text TEXT NOT NULL,
    question_text VARCHAR(500) NULL,
    is_terminal BOOLEAN NOT NULL DEFAULT FALSE,
    CONSTRAINT fk_drill_steps_scenario FOREIGN KEY (scenario_id)
        REFERENCES drill_scenarios (id) ON DELETE CASCADE,
    CONSTRAINT uk_scenario_step_key UNIQUE (scenario_id, step_key)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Add deferred foreign key for initial_step_id on drill_scenarios
ALTER TABLE drill_scenarios
    ADD CONSTRAINT fk_drill_scenarios_initial_step
    FOREIGN KEY (initial_step_id) REFERENCES drill_steps (id)
    ON DELETE SET NULL;

CREATE TABLE IF NOT EXISTS drill_choices (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    step_id BIGINT NOT NULL,
    choice_text VARCHAR(500) NOT NULL,
    outcome_type VARCHAR(20) NOT NULL,
    feedback_text TEXT NOT NULL,
    score_impact INT NOT NULL DEFAULT 0,
    next_step_id BIGINT NULL,
    sequence_order INT NOT NULL DEFAULT 1,
    CONSTRAINT fk_drill_choices_step FOREIGN KEY (step_id)
        REFERENCES drill_steps (id) ON DELETE CASCADE,
    CONSTRAINT fk_drill_choices_next_step FOREIGN KEY (next_step_id)
        REFERENCES drill_steps (id) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS drill_attempts (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    scenario_id BIGINT NOT NULL,
    total_score INT NOT NULL DEFAULT 0,
    rating_label VARCHAR(50) NOT NULL,
    completed_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_drill_attempts_user FOREIGN KEY (user_id)
        REFERENCES users (id) ON DELETE CASCADE,
    CONSTRAINT fk_drill_attempts_scenario FOREIGN KEY (scenario_id)
        REFERENCES drill_scenarios (id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ----------------------------------------------------------------------------
-- 5. User Learning Progress & Contact Messaging
-- ----------------------------------------------------------------------------

CREATE TABLE IF NOT EXISTS user_lesson_progress (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    lesson_id BIGINT NOT NULL,
    completed BOOLEAN NOT NULL DEFAULT TRUE,
    completed_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_progress_user FOREIGN KEY (user_id)
        REFERENCES users (id) ON DELETE CASCADE,
    CONSTRAINT fk_progress_lesson FOREIGN KEY (lesson_id)
        REFERENCES lessons (id) ON DELETE CASCADE,
    CONSTRAINT uk_user_lesson_progress UNIQUE (user_id, lesson_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS contact_messages (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    full_name VARCHAR(100) NOT NULL,
    email VARCHAR(150) NOT NULL,
    category VARCHAR(50) NOT NULL,
    subject VARCHAR(200) NOT NULL,
    message TEXT NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'NEW',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_contact_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
