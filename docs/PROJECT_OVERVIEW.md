# Suraksha: Project Overview & Functional Requirements Specification

## 1. Problem Statement & Mission

Disasters—both natural (earthquakes, flash floods, cyclones, landslides) and human-induced (structural fires, industrial gas leaks)—often strike with little warning. In educational institutions and communities, the primary cause of casualties is panic, lack of situational awareness, and misinformation regarding basic safety steps (e.g., attempting to use elevators during a fire or running outside during active earthquake shaking).

**Suraksha** (सुरक्षा - *Safety & Protection*) is an educational disaster preparedness platform built specifically for students, educators, and schools. Its core mission is to replace panic with informed preparation by providing:
1. Structured, categorized disaster education courses.
2. Verified first-aid guidance and emergency safety rules.
3. Interactive virtual mock drills that test decision-making under simulated hazard conditions.
4. Objective assessments (quizzes) with server-side validation to measure learner readiness.

---

## 2. Ethical and Safety Disclaimers

> **CRITICAL DISCLAIMER**:
> Suraksha is solely an **educational and awareness tool**.
> 1. It is **not** an emergency response, dispatch, or SOS system.
> 2. It does not replace real emergency telephone services (such as 112 in India, 911 in North America, or local municipal emergency hotlines).
> 3. Its first-aid content provides foundational awareness and does not substitute for certified clinical first-responder training (e.g., Red Cross or St. John Ambulance certification).
> 4. Virtual mock drills are simulated educational scenarios and must never trigger real-world alarms, sirens, or automated calls to emergency personnel.

---

## 3. User Personas

| Persona | Role & Needs | Key Platform Activities |
| :--- | :--- | :--- |
| **Student / Learner** | School or university student wanting to learn life-saving safety skills. | Browses courses, studies disaster safety checklists, takes quizzes, participates in interactive virtual mock drills, tracks personal readiness score. |
| **Educator / Instructor** | Teacher organizing disaster management curriculum or school safety week. | Assigns courses to students, reviews student progress, conducts classroom walkthroughs of virtual drills. |
| **Administrator** | Institutional safety coordinator or system administrator. | Manages courses, lessons, quizzes, monitors contact inquiries, manages user accounts. |
| **Guest / Public Citizen** | Any visitor seeking emergency preparation guidelines. | Accesses public safety guides, emergency helpline numbers, first-aid references, and course previews. |

---

## 4. Core Functional Modules

### Module A: Public Website & Navigation
* **Homepage (`index.html`)**:
  * Hero section with call-to-action ("Start Learning Preparedness", "Try a Mock Drill").
  * Disaster Categories Quick-Grid (Earthquakes, Floods, Fires, Cyclones, Industrial Hazards).
  * Prominent Emergency Numbers quick reference card (National Emergency 112, Fire 101, Ambulance 102, Disaster Management 108).
  * Real-time search or filter preview of available modules.
* **About Page (`about.html`)**:
  * Educational mission, alignment with disaster management authorities (NDMA/SDMA principles), and safety disclaimers.
* **Contact & Feedback Page (`contact.html`)**:
  * Clean form (Name, Email, Subject, Category, Message).
  * Client-side validation for valid email format and message lengths.
  * Server-side persistence of user feedback into MySQL with clear submission feedback.

### Module B: User Accounts & Authentication
* **Registration (`register.html`)**:
  * Fields: Full Name, Email Address, Password, Confirm Password, Role selection (defaults to `STUDENT`).
  * Strong client-side validation (password length, matching passwords, required fields).
  * Backend BCrypt hashing (`$2a$10$...`) with duplicate email checks.
* **Login (`login.html`)**:
  * Email and Password authentication via Spring Security.
  * Token or Session-based security with secure client storage.
  * Meaningful error responses for invalid credentials without leaking specific user existence.
* **Logout & Protected Routes**:
  * Invalidation of active token/session.
  * Redirects unauthenticated users trying to access `/dashboard.html` or `/quiz.html`.

### Module C: Disaster Education Courses
* Organized by hazard categories:
  * **Earthquake Preparedness**: Drop, Cover, and Hold On; structural hazard identification; emergency kit checklist.
  * **Flood & Flash Flood Safety**: Evacuation routes, 30cm moving water danger, electrical hazard disconnection, post-flood sanitation.
  * **Fire Safety & Prevention**: PASS technique (Pull, Aim, Squeeze, Sweep) for fire extinguishers, smoke evacuation (crawl low), prevention of electrical overloads.
  * **Cyclone & Storm Protocols**: Securing shelter, interpreting storm warning color codes, surviving eye-of-the-storm calm phases.
  * **Industrial & Chemical Hazards**: Hazardous gas leak response, windward movement, shelter-in-place techniques.
* Each course contains:
  * Course metadata: Title, Hazard Category, Difficulty Level (Beginner, Intermediate, Advanced), Estimated Duration.
  * Lessons / Modules: Step-by-step reading with visual infographics, DOs and DON'Ts tables, and key takeaways.

### Module D: First-Aid Learning Center (`first-aid.html`)
* Essential first-aid topics:
  * CPR (Cardiopulmonary Resuscitation) awareness (Chest compressions basics, hands-only guidance).
  * Severe Bleeding control (Direct pressure, elevation, clean dressings).
  * Burn Management (Cool running water for 20 mins, never apply ice or butter).
  * Choking response (Heimlich maneuver / abdominal thrusts).
  * Heat Exhaustion & Heatstroke relief.
* Clear educational disclaimer prominently displayed at the top and bottom of every first-aid card.

### Module E: Virtual Interactive Mock Drills (`mock-drill.html`)
* Branching simulation scenarios:
  * *Scenario 1: Earthquake in a Multi-Story School Building.*
    * Step 1: Tremors begin. Options: (A) Rush down the staircase immediately, (B) Drop under a sturdy desk and hold on, (C) Run to the elevator.
    * Consequence & educational explanation immediately revealed.
    * Step 2: Tremors stop. Options: Evacuation procedure, checking for gas leaks, assembly area protocol.
  * *Scenario 2: Chemistry Lab Fire Emergency.*
    * Decision steps on extinguisher selection (Class A vs B vs Electrical), alarm activation, and safe low-crawl egress.
* Scoring based on safety choices and readiness score calculation.

### Module F: Objective Quizzes (`quiz.html`)
* Course-specific quizzes containing multiple-choice questions.
* **Server-side Validation Rule**: The browser never scores the quiz directly. The frontend submits an array of selected option IDs to `POST /api/quizzes/{id}/submit`. The backend evaluates against the database, computes the score, logs the attempt, and returns the verified result.

### Module G: Student Dashboard (`dashboard.html`)
* Summary statistics: Courses Enrolled, Lessons Completed, Average Quiz Score, Drills Practiced.
* Course progress bars.
* Recent quiz attempt history with pass/fail badges.
* Actionable recommendations ("Take the Flood Safety Quiz next!").

### Module H: Administration Portal
* Course & Lesson authoring (Create, Edit, Delete).
* Quiz Question & Option management.
* Contact message review table.
* Backend authorization strictly enforcing `ROLE_ADMIN` on mutating endpoints.

---

## 5. Non-Functional Requirements

1. **Simplicity and Maintainability**:
   * Clean separation of concerns (HTML structure, CSS presentation, JS logic).
   * Java Spring Boot standard layering without over-engineering or unnecessary external libraries.
2. **Security**:
   * No hardcoded credentials or API secrets in code repositories.
   * Passwords hashed using Spring Security's `BCryptPasswordEncoder`.
   * Input validation both on client (HTML5/JS) and server (Jakarta Validation annotations like `@NotBlank`, `@Email`, `@Size`).
   * Explicit Cross-Origin Resource Sharing (CORS) configuration restricted to local dev origins.
3. **Accessibility & Responsive Design**:
   * Semantic HTML5 elements (`<header>`, `<nav>`, `<main>`, `<article>`, `<section>`, `<footer>`).
   * High-contrast color palette adhering to WCAG 2.1 AA standards.
   * Responsive CSS Grid and Flexbox layouts functional from mobile screens (360px) up to high-resolution desktop displays (1920px).
4. **Performance & Reliability**:
   * Lightweight vanilla JavaScript with zero third-party framework overhead.
   * Efficient JPA queries with proper indexing and relationship fetching.
   * Graceful error handling with descriptive user-friendly alerts.
