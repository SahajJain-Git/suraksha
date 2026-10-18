-- ============================================================================
-- Suraksha Database Seed Data Script
-- Target Database: MySQL 8.0+
-- Pre-requisite: Execute database/schema.sql prior to running this script.
-- ============================================================================

USE suraksha_db;

-- ----------------------------------------------------------------------------
-- 1. Seed Roles & Users
-- Passwords:
--   student@suraksha.org -> StudentPass123!
--   admin@suraksha.org   -> AdminPass123!
-- (Plaintext passwords are never stored. BCrypt strength factor: 10)
-- ----------------------------------------------------------------------------

INSERT INTO roles (id, name, description) VALUES
(1, 'ROLE_STUDENT', 'Standard student learner with access to courses, quizzes, and drills'),
(2, 'ROLE_TEACHER', 'Educator or safety coordinator with class monitoring privileges'),
(3, 'ROLE_ADMIN', 'System administrator with curriculum management and inquiry access')
ON DUPLICATE KEY UPDATE description = VALUES(description);

INSERT INTO users (id, full_name, email, password_hash, enabled) VALUES
(1, 'Aarav Sharma', 'student@suraksha.org', '$2a$10$uJq/yLG8ZpQ.z0tBkqJVNeAqBIJY9LZlyapQdL2/b5yetKuaHl/cC', TRUE),
(2, 'Dr. Neha Patel', 'admin@suraksha.org', '$2a$10$tzBZ7u.EBI6yZmnC.8VMAOUosAzc7ytz3j3jxOEVbJVAsHsUr5G5i', TRUE)
ON DUPLICATE KEY UPDATE full_name = VALUES(full_name);

INSERT INTO user_roles (user_id, role_id) VALUES
(1, 1), -- Aarav is ROLE_STUDENT
(2, 3)  -- Dr. Neha is ROLE_ADMIN
ON DUPLICATE KEY UPDATE role_id = VALUES(role_id);

-- ----------------------------------------------------------------------------
-- 2. Seed Courses
-- ----------------------------------------------------------------------------

INSERT INTO courses (id, slug, title, category, difficulty, estimated_minutes, summary) VALUES
(1, 'earthquake-101', 'Earthquake Preparedness & Response', 'EARTHQUAKE', 'BEGINNER', 25,
 'Master the Drop, Cover, and Hold On technique, identify classroom hazards, and prepare a 72-hour family survival kit.'),
(2, 'flood-101', 'Flood & Flash Flood Safety', 'FLOOD', 'BEGINNER', 20,
 'Learn the danger of moving water, evacuation elevation tactics, water purification rules, and avoiding electrical hazards.'),
(3, 'fire-101', 'Structural Fire Prevention & Evacuation', 'FIRE', 'INTERMEDIATE', 25,
 'Understand combustion dynamics, master the PASS extinguisher technique, learn smoke-crawl egress, and prevent laboratory fires.'),
(4, 'cyclone-101', 'Cyclone & Severe Storm Mitigation', 'CYCLONE', 'BEGINNER', 20,
 'Learn cyclone classification, storm surge risks, shelter securing methods, and surviving the deceptive calm eye of the storm.')
ON DUPLICATE KEY UPDATE title = VALUES(title);

-- ----------------------------------------------------------------------------
-- 3. Seed Lessons (Earthquake Preparedness)
-- ----------------------------------------------------------------------------

INSERT INTO lessons (id, course_id, slug, title, sequence_order, content, dos_guidelines, donts_guidelines) VALUES
(1, 1, 'eq-lesson-1', 'The Science of Shaking & Early Warnings', 1,
 '<p>Earthquakes occur when tectonic plates grinding against each other suddenly slip along a geological fault line. The energy released travels through the earth in seismic waves.</p><p>Unlike weather hazards like cyclones, earthquakes strike without advance meteorological notice. However, modern seismic sensors can detect early P-waves seconds before destructive S-waves arrive.</p><h4>Key Hazard Takeaway:</h4><p>Most earthquake casualties are not caused by the ground opening up; they are caused by collapsing non-structural elements: unanchored bookcases, heavy light fixtures, falling ceiling tiles, and shattering window glass.</p>',
 'Secure tall furniture and lab shelves to wall studs.\nIdentify safe shelter spots in every room: under sturdy desks or interior walls.\nKeep an emergency radio and flashlight near classroom doors.',
 'Do not place heavy textbooks or glass trophies on high open shelves.\nDo not rely on doorways for protection in modern buildings.\nDo not panic or rush blindly toward exits when shaking starts.'),

(2, 1, 'eq-lesson-2', 'During Shaking: Drop, Cover, and Hold On', 2,
 '<p>When the ground begins to violently shake, human balance is compromised within seconds. Attempting to run while floors are buckling frequently leads to broken limbs or head trauma from flying debris.</p><h4>The Three Essential Steps:</h4><ol><li><strong>DROP:</strong> Drop down immediately onto your hands and knees.</li><li><strong>COVER:</strong> Cover your head and neck underneath a sturdy desk or table.</li><li><strong>HOLD ON:</strong> Hold on to your shelter with one hand; be prepared to move with it.</li></ol>',
 'Drop, Cover, and Hold On until shaking completely stops.\nTurn your face away from windows to protect against shattering glass.\nIf outdoors, move into an open area away from power lines and brick parapets.',
 'DO NOT run outside while shaking is in progress. Falling masonry from building facades is deadly.\nDO NOT use elevators under any circumstances.\nDO NOT push or crowd against classroom doors.'),

(3, 1, 'eq-lesson-3', 'Post-Shaking Evacuation & Utility Safety', 3,
 '<p>Once tremors subside, the immediate danger shifts to secondary hazards: gas leaks, electrical short circuits, fires, and aftershocks.</p><p>Evacuate in a disciplined, single-file line following school evacuation routes to open assembly zones.</p><h4>Detecting Secondary Hazards:</h4><p>If you smell natural gas (rotten-egg sulfur odor) or hear a hissing pipe: <strong>DO NOT turn on or off any electrical switches</strong>, light matches, or create sparks.</p>',
 'Check yourself and nearby peers for injuries and administer basic first aid if trained.\nListen to official updates via battery-operated emergency transceivers.\nWear sturdy closed-toe shoes to protect against shards of broken glass.',
 'Do not re-enter damaged buildings until certified safe by structural engineers.\nDo not operate electrical appliances or light switches if a gas leak is suspected.\nDo not spread unverified rumors on social media regarding aftershock predictions.'),

(4, 1, 'eq-lesson-4', 'The 72-Hour Emergency Survival Kit', 4,
 '<p>Following a major disaster, emergency responders may take up to 72 hours to reach all isolated neighborhoods. Every household and classroom should maintain a designated Go-Bag.</p><h4>Core Kit Checklist:</h4><ul><li>Drinking water: 3 to 4 liters per person per day</li><li>Non-perishable food rations</li><li>LED flashlight and spare batteries</li><li>First-aid kit with sterile bandages</li><li>Loud whistle for signaling beneath rubble</li></ul>',
 'Inspect and refresh stored water and food supplies every 6 months.\nStore copies of identification and prescriptions in a waterproof bag.\nKeep the emergency kit in an easily accessible location known to all family members.',
 'Do not pack salty snacks that accelerate dehydration.\nDo not lock away the kit in inaccessible attics or basements.\nDo not rely solely on smartphone battery flashlights.')
ON DUPLICATE KEY UPDATE title = VALUES(title);

-- ----------------------------------------------------------------------------
-- 4. Seed Quizzes & Questions
-- ----------------------------------------------------------------------------

INSERT INTO quizzes (id, course_id, slug, title, passing_score_percent) VALUES
(1, 1, 'quiz-earthquake', 'Earthquake Safety Certification Quiz', 75),
(2, 2, 'quiz-flood', 'Flood & Flash Flood Safety Quiz', 75),
(3, 3, 'quiz-fire', 'Fire Prevention & PASS Protocol Quiz', 75),
(4, 4, 'quiz-cyclone', 'Cyclone & Severe Storm Safety Quiz', 75)
ON DUPLICATE KEY UPDATE title = VALUES(title);

-- Questions for Earthquake Quiz
INSERT INTO quiz_questions (id, quiz_id, sequence_order, question_text, explanation) VALUES
(1, 1, 1, 'When an earthquake strikes while you are inside a multi-story school building, what is the safest immediate action?',
 'Running during active shaking frequently causes falls and severe injuries from falling debris. Dropping under sturdy furniture shields your vital organs and head.'),
(2, 1, 2, 'After earthquake shaking stops, you smell a strong odor of gas in the hallway. What should you do?',
 'Flipping an electrical switch or lighting a flame creates microscopic electrical sparks that can ignite leaking gas and trigger an explosion.'),
(3, 1, 3, 'How much drinking water per person per day should be stored in a family 72-hour disaster survival kit?',
 'Emergency disaster guidelines (NDMA & Red Cross) recommend 3 to 4 liters per person per day to cover hydration, food preparation, and minimal hygiene.'),
(4, 1, 4, 'Why should you NOT stand under doorways for protection in modern buildings?',
 'In modern architecture, interior doorframes are no stronger than standard walls, and unsecured swinging doors frequently injure hands and fingers.')
ON DUPLICATE KEY UPDATE question_text = VALUES(question_text);

-- Options for Earthquake Questions (Correct answers marked with is_correct = TRUE)
INSERT INTO quiz_options (question_id, option_text, is_correct, sequence_order) VALUES
-- Question 1 Options
(1, 'Run down the stairs as fast as possible to reach the street', FALSE, 1),
(1, 'Drop, Cover under a sturdy desk or table, and Hold On', TRUE, 2),
(1, 'Take the nearest elevator to reach ground level', FALSE, 3),
(1, 'Stand directly in the center of the room with arms raised', FALSE, 4),

-- Question 2 Options
(2, 'Turn on the light switches to inspect the pipe', FALSE, 1),
(2, 'Light a match to find the leak', FALSE, 2),
(2, 'Evacuate immediately without operating any electrical switches', TRUE, 3),
(2, 'Use your smartphone to record a video while standing near the pipe', FALSE, 4),

-- Question 3 Options
(3, '0.5 liters per person', FALSE, 1),
(3, '1 to 2 cups total', FALSE, 2),
(3, '3 to 4 liters per person', TRUE, 3),
(3, 'Water is unnecessary if you store soda', FALSE, 4),

-- Question 4 Options
(4, 'Doorways are reserved exclusively for teachers', FALSE, 1),
(4, 'Modern interior doorways are not reinforced and swinging doors can cause severe crush injuries', TRUE, 2),
(4, 'Doorways are always electrified during earthquakes', FALSE, 3),
(4, 'Doorways attract lightning', FALSE, 4);

-- ----------------------------------------------------------------------------
-- 5. Seed Virtual Mock Drill (Earthquake Scenario)
-- ----------------------------------------------------------------------------

INSERT INTO drill_scenarios (id, course_id, slug, title, hazard_category, description, initial_step_id) VALUES
(1, 1, 'drill-earthquake', 'High-Rise School Earthquake Simulation', 'EARTHQUAKE',
 'Experience simulated tremors in a 3rd floor classroom. Make split-second decisions regarding shelter, secondary gas leaks, and safe egress.', NULL)
ON DUPLICATE KEY UPDATE title = VALUES(title);

-- Seed Steps
INSERT INTO drill_steps (id, scenario_id, step_key, situation_text, question_text, is_terminal) VALUES
(1, 1, 'step-1',
 'You are seated on the 3rd floor of your school during afternoon classes. Suddenly, the building violently rumbles. Books tumble off shelves, lights flicker, and floor tiles shudder.',
 'What is your immediate action?', FALSE),
(2, 1, 'step-2',
 'The main shaking ceases after 45 seconds. Dust fills the air, fire alarms are ringing, and a strong rotten-egg (mercaptan gas) odor is detected near the hallway.',
 'What is your evacuation decision?', FALSE),
(3, 1, 'step-3',
 'You reach the ground floor exit, but notice an elevator door slightly ajar with someone shouting for help inside. Tremors rumble again (an aftershock).',
 'How do you respond to the trapped person?', FALSE),
(4, 1, 'step-finish',
 'You have arrived safely at the open football field assembly zone. Roll call is underway, medical aid is arriving, and structural engineers are inspecting the building.',
 NULL, TRUE)
ON DUPLICATE KEY UPDATE situation_text = VALUES(situation_text);

-- Link initial_step_id back to scenario
UPDATE drill_scenarios SET initial_step_id = 1 WHERE id = 1;

-- Seed Choices with branch targets and scoring
INSERT INTO drill_choices (step_id, choice_text, outcome_type, feedback_text, score_impact, next_step_id, sequence_order) VALUES
(1, 'Sprint immediately for the hallway staircase to reach the ground floor.', 'DANGER',
 'CRITICAL MISTAKE! Running while the building is shaking causes severe falls. Stairwells experience intense shear stress, and flying facade bricks outside the exit are lethal.',
 -30, 2, 1),
(1, 'Drop to your hands and knees under your sturdy wooden desk, Cover your head/neck, and Hold On.', 'SAFE',
 'EXCELLENT DECISION! Dropping prevents falls. The sturdy desk shields your skull, spine, and eyes from falling fluorescent lights and shattered window fragments.',
 50, 2, 2),
(1, 'Run toward the window to see what is happening in the courtyard.', 'DANGER',
 'FATAL RISK! Windows shatter under seismic shear tension. Standing near glass exposes your body to high-speed flying glass shards.',
 -40, 2, 3),

(2, 'Switch on the hallway light switch so everyone can see through the dust.', 'DANGER',
 'DANGEROUS! Never touch electrical switches when gas is suspected. A tiny internal contact spark can ignite gas vapors and cause an explosion.',
 -30, 3, 1),
(2, 'Alert your teacher/peers of the gas odor, refrain from using any switches, and proceed calmly along the emergency stairwell.', 'SAFE',
 'SPOT ON! You protected everyone from an ignition spark and initiated prompt, disciplined evacuation via stairs.',
 30, 3, 2),

(3, 'Attempt to pry open the heavy elevator doors with your bare hands alone while standing under the shaft.', 'DANGER',
 'UNSAFE! Elevator shafts can collapse or drop during aftershocks. Risking your life without specialized tools creates two victims instead of one.',
 -20, 4, 1),
(3, 'Reassure the person loudly, tell them not to jump, exit immediately to the assembly zone, and alert the trained emergency response team.', 'SAFE',
 'HEROIC & WISE! Emergency crews equipped with hydraulic spreaders can safely extricate trapped victims while you avoid being crushed during the aftershock.',
 20, 4, 2);

-- ----------------------------------------------------------------------------
-- 6. Seed Sample Progress, Attempts & Contact Messages
-- ----------------------------------------------------------------------------

INSERT INTO user_lesson_progress (user_id, lesson_id, completed) VALUES
(1, 1, TRUE),
(1, 2, TRUE)
ON DUPLICATE KEY UPDATE completed = VALUES(completed);

INSERT INTO quiz_attempts (user_id, quiz_id, score_percent, total_questions, correct_answers, passed) VALUES
(1, 1, 100, 4, 4, TRUE);

INSERT INTO drill_attempts (user_id, scenario_id, total_score, rating_label) VALUES
(1, 1, 100, 'Preparedness Master');

INSERT INTO contact_messages (full_name, email, category, subject, message, status) VALUES
('Priya Sharma', 'priya.sharma@school.edu', 'SCHOOL', 'Request for School Safety Workshop',
 'We would like to integrate the Suraksha virtual mock drills into our upcoming annual campus safety week for 400 high school students.', 'NEW');
