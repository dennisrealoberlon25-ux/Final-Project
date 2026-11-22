-- =====================================================
-- Student Attendance Database
-- =====================================================

CREATE DATABASE IF NOT EXISTS student_attendance_db;
USE student_attendance_db;

-- -----------------------------------------------------
-- Tables
-- -----------------------------------------------------
CREATE TABLE student (
    student_id VARCHAR(15)  PRIMARY KEY,   -- e.g. 2025-01-06745 (13 chars)
    name       VARCHAR(100) NOT NULL,
    block      VARCHAR(20)  NOT NULL,
    course     VARCHAR(20)  NOT NULL,
    year       VARCHAR(20)  NOT NULL
);

CREATE TABLE attendance (
    attendance_id   INT AUTO_INCREMENT PRIMARY KEY,
    student_id      VARCHAR(15) NOT NULL,
    attendance_date DATE        NOT NULL,
    status          ENUM('Present','Absent') NOT NULL,
    FOREIGN KEY (student_id) REFERENCES student(student_id)
        ON DELETE CASCADE,
    UNIQUE (student_id, attendance_date)   -- one record per student per day
);

CREATE TABLE IF NOT EXISTS users (
    id            INT AUTO_INCREMENT PRIMARY KEY,
    username      VARCHAR(50)  NOT NULL UNIQUE,
    salt          VARBINARY(16) NOT NULL,
    password_hash VARBINARY(32) NOT NULL
);

-- -----------------------------------------------------
-- Students (must be inserted BEFORE attendance)
-- -----------------------------------------------------
INSERT INTO student (student_id, name, block, course, year)
VALUES
('2025-01-06477', 'ENTERO, KENNETH C.',         'BLOCK A', 'BSIT', '2ND YEAR'),
('2025-01-07408', 'RINON, ANGELA MARIE S.',     'BLOCK A', 'BSIT', '2ND YEAR'),
('2025-01-05678', 'DELA CRUZ, JUAN R.',         'BLOCK A', 'BSIT', '2ND YEAR'),
('2025-01-01245', 'FLORES, GILBERT S.',         'BLOCK A', 'BSIT', '2ND YEAR'),
('2025-01-06789', 'BANTOLINAO, ALDREN B.',      'BLOCK A', 'BSIT', '2ND YEAR'),
('2025-01-06745', 'CHAVEZ, ERICH B.',           'BLOCK A', 'BSIT', '2ND YEAR'),
('2025-01-06952', 'BERLON, DENNIS R.',          'BLOCK A', 'BSIT', '2ND YEAR'),
('2025-01-06387', 'SACAYAN, MARK CHRISMON R.',  'BLOCK A', 'BSIT', '2ND YEAR'),
('2025-01-07190', 'PRIEL, JOSCYN M.',           'BLOCK A', 'BSIT', '2ND YEAR'),
('2025-01-06372', 'OARDE, KENNETH STEVEN C.',   'BLOCK A', 'BSIT', '2ND YEAR');

-- -----------------------------------------------------
-- Attendance for 2026-11-17
-- -----------------------------------------------------
INSERT INTO attendance (student_id, attendance_date, status)
VALUES
('2025-01-06477', '2026-11-17', 'Present'),
('2025-01-07408', '2026-11-17', 'Present'),
('2025-01-05678', '2026-11-17', 'Absent'),
('2025-01-01245', '2026-11-17', 'Present'),
('2025-01-06789', '2026-11-17', 'Present'),
('2025-01-06745', '2026-11-17', 'Present'),
('2025-01-06952', '2026-11-17', 'Present'),
('2025-01-06387', '2026-11-17', 'Present'),
('2025-01-07190', '2026-11-17', 'Present'),
('2025-01-06372', '2026-11-17', 'Present');

-- -----------------------------------------------------
-- Quick check
-- -----------------------------------------------------
SELECT s.student_id, s.name, a.attendance_date, a.status
FROM attendance a
JOIN student s ON s.student_id = a.student_id
ORDER BY s.name;
