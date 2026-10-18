# Suraksha - Frontend-Backend Integration Guide (Milestone 8)

> **Document Version**: 1.0.0  
> **Milestone**: Milestone 8 — Frontend-Backend Integration  
> **Target Audience**: Full-Stack Developers, Technical Interviewers, and Engineering Mentors

---

## 1. Architectural Overview

In Milestone 8, the static Vanilla HTML/CSS/JavaScript user interface was transformed into a dynamic, reactive client connected to the Spring Boot 3.3.5 / Spring Security REST backend.

```
+-------------------------------------------------------------+
|                      Browser Layer                          |
|  HTML5 Pages: index, courses, course-detail, quiz,         |
|               mock-drill, contact, login, register,         |
|               dashboard                                     |
|                                                             |
|  Page Controllers: courses.js, course-detail.js,            |
|                    quiz.js, mock-drill.js, auth.js,         |
|                    contact.js, dashboard.js                 |
|                            |                                |
|                            v                                |
|            SurakshaApi (frontend/js/api.js)                 |
|  - Configurable Base URL (http://localhost:8080/api)        |
|  - Automatic Bearer JWT attachment                          |
|  - Unified JSON serialization & deserialization             |
|  - Centralized 401 interception & auth-changed events       |
|  - Network error detection (isNetworkError: true)           |
+------------------------------|------------------------------+
                               | HTTP / REST (JSON)
                               v
+-------------------------------------------------------------+
|                   Spring Boot Backend                       |
|  - WebCorsConfig & SecurityConfig (CORS allowed origins)    |
|  - JwtAuthenticationFilter (Stateless Bearer validation)   |
|  - Controllers: Auth, Course, Quiz, Drill, Contact, Progress|
|  - Services -> JPA Repositories -> MySQL 8.0 / H2 Test DB   |
+-------------------------------------------------------------+
```

---

## 2. Centralized Fetch Client (`frontend/js/api.js`)

### 2.1 The Problem with Ad-Hoc `fetch()`
Directly calling `fetch()` across individual page scripts creates multiple liabilities:
1. **Duplicate Base URLs**: Hardcoding `http://localhost:8080/api` everywhere makes environment transitions error-prone.
2. **Boilerplate Headers**: Manually adding `Authorization: Bearer <token>` and `Content-Type: application/json` in every request is repetitive and prone to omissions.
3. **Inconsistent Error Parsing**: A 404, 401, or 500 status requires distinct handling (e.g. redirecting to login on 401, showing validation badges on 400).

### 2.2 The Solution: Unified `SurakshaApi`
`frontend/js/api.js` encapsulates all HTTP mechanics into a single module:

```javascript
SurakshaApi.courses.getAll({ category: 'EARTHQUAKE', search: 'Drop' });
SurakshaApi.courses.getBySlug('earthquake-101');
SurakshaApi.quizzes.getById('quiz-earthquake');
SurakshaApi.quizzes.submit('quiz-earthquake', { answers: [...] });
SurakshaApi.drills.getBySlug('earthquake-school-evacuation');
SurakshaApi.drills.submit('earthquake-school-evacuation', { selectedChoiceIds: [...] });
SurakshaApi.progress.markLessonCompleted(14);
SurakshaApi.progress.getUserProgress();
SurakshaApi.auth.login({ email, password });
SurakshaApi.auth.register({ fullName, email, password });
SurakshaApi.auth.getMe();
SurakshaApi.auth.logout();
```

---

## 3. Token Lifecycle & Browser Storage Rationale

### 3.1 Why `sessionStorage` over `localStorage`?
| Storage Type | Lifetime | Scope | Security Risk Profile |
| :--- | :--- | :--- | :--- |
| `localStorage` | Indefinite (survives tab/browser closure) | Shared across all tabs & windows on same origin | High vulnerability window if device is shared or XSS occurs. |
| `sessionStorage` | Ephemeral (cleared when browser tab closes) | Isolated to the specific browser tab session | **Preferred for learner sessions**: Mitigates persistent credential hijacking on shared computer lab machines. |
| `HttpOnly Cookie` | Controlled by backend `Set-Cookie` Max-Age | Sent automatically by browser | Immune to JavaScript XSS theft, but requires CSRF protection (`SameSite=Strict`, CSRF tokens) and strict CORS cookie credentials configuration. |

### 3.2 XSS (Cross-Site Scripting) Defense
Because JavaScript in `sessionStorage` can be read if arbitrary script execution occurs, Suraksha enforces strict XSS mitigations:
- **No `innerHTML` on Untrusted Data**: Text content is inserted via `textContent` or sanitized DOM node manipulation.
- **Backend Input Validation**: Jakarta Validation annotations (`@NotBlank`, `@Size`, `@Email`) sanitize and constrain inputs server-side.
- **Zero-Password Storage**: Plaintext passwords are never placed into `sessionStorage` or `localStorage`.

---

## 4. Educational Zero-Leak Quiz Architecture

A critical vulnerability in educational portals is transmitting the answer key (`isCorrect: true`) to the browser inside the quiz payload. Tech-savvy students can inspect `Network -> Response` or DOM state to find the answers.

### Suraksha Zero-Leak Enforcement:
1. `GET /api/quizzes/{id}`:
   - Returns question IDs, prompts, and choice IDs/text only.
   - The fields `isCorrect` and `explanation` are explicitly excluded from `QuizDetailDto` and `QuizQuestionDetailDto`.
2. `POST /api/quizzes/{id}/submit`:
   - The learner submits `{ answers: [{ questionId: 1, selectedOptionId: 2 }, ...] }`.
   - The backend validates student identity from Spring Security's authenticated JWT context.
   - The server queries `quiz_options` directly, calculates the authoritative score, records the `QuizAttempt` row, and returns the debrief along with correct answers and pedagogical explanations **only after submission**.

---

## 5. Branching Virtual Mock Drills

The virtual mock drill models real-time crisis decision making:
1. `GET /api/drills/{slug}`:
   - Loads the scenario metadata and decision nodes (`DrillStepDto`, `DrillChoiceDto`).
   - If an option leads to `nextStepKey`, the frontend navigates the user along the decision tree.
2. `POST /api/drills/{id}/submit`:
   - The frontend submits the sequence of decisions made: `{ selectedChoiceIds: [1, 4, 7] }`.
   - The backend computes the cumulative score:
     - Safe choices: Positive points (+10 to +25)
     - Hazardous choices: Penalty points (-5 to -20)
   - Evaluates a certified performance tier (`Exemplary Responders`, `Safety Conscious`, `Needs Preparedness Training`) and persists the `DrillAttempt`.

---

## 6. End-to-End Testing & Verification

Suraksha incorporates a full-fidelity end-to-end integration test suite:
- **Test File**: `backend/src/test/java/org/suraksha/FrontendBackendIntegrationTest.java`
- **Execution Mode**: `webEnvironment = RANDOM_PORT` on embedded Apache Tomcat.
- **HTTP Client**: `TestRestTemplate` performing genuine HTTP requests across network sockets.
- **Coverage**:
  1. System Health (`GET /api/health`)
  2. Public Course Catalogue (`GET /api/courses`, `GET /api/courses/{slug}`)
  3. Student Registration (`POST /api/auth/register`)
  4. Student Login & JWT Issuance (`POST /api/auth/login`)
  5. Authenticated Identity (`GET /api/auth/me`)
  6. Zero-Leak Quiz Retrieval (`GET /api/quizzes/{id}`)
  7. Quiz Submission & Server Evaluation (`POST /api/quizzes/{id}/submit`)
  8. Interactive Lesson Completion (`POST /api/progress/lessons/{id}`)
  9. Unified Learner Dashboard (`GET /api/progress`)
  10. Virtual Mock Drill Execution (`GET /api/drills/{slug}`, `POST /api/drills/{id}/submit`)
  11. Contact Form Submission (`POST /api/contact`)
  12. Token Expiration / Tamper Rejection (`HTTP 401 Unauthorized`)

### Test Suite Execution Result:
```text
[INFO] Tests run: 44, Failures: 0, Errors: 0, Skipped: 0
[INFO] BUILD SUCCESS
```

---

## 7. DevTools Network Tab Debugging Guide

When troubleshooting frontend-backend communications:
1. **Network Tab Filter**:
   - Filter by `Fetch/XHR` to isolate REST API exchanges.
2. **Headers Inspection**:
   - **Request Headers**: Verify `Authorization: Bearer eyJhbGci...` is attached.
   - **Response Headers**: Check `Content-Type: application/json` and `Access-Control-Allow-Origin`.
3. **Request Payload**:
   - Verify request body format matches backend DTO schema (e.g. check property casing like `fullName` vs `full_name`).
4. **Response Preview**:
   - Confirm payload follows `ApiResponse<T>` wrapper (`{ "success": true, "data": ... }`).
5. **Console Log Warnings**:
   - If an error displays `isNetworkError: true`, check if the backend application is running on port 8080 and CORS allows origin `http://localhost:*`.

---

## 8. Technical Interview Preparation (Q&A)

### Q1: Why use a centralized API client rather than calling `fetch()` directly in UI components?
> **Answer**: Centralizing API calls into `SurakshaApi` adheres to the Single Responsibility Principle (SRP) and DRY (Don't Repeat Yourself). It creates a single chokepoint for base URL configuration, Bearer token injection, uniform JSON parsing, standardized error unwrapping, and 401 unauthenticated session handling. If the authentication scheme changes from Bearer tokens to cookies, only `api.js` requires modification.

### Q2: What are the security trade-offs between `localStorage` and `sessionStorage` for storing JWTs?
> **Answer**: Both `localStorage` and `sessionStorage` are vulnerable to Cross-Site Scripting (XSS) if malicious scripts run on the origin. However, `localStorage` persists indefinitely across all tabs and windows, creating a wide exposure window if an attacker later accesses the machine or injects script. `sessionStorage` is strictly scoped to the active browser tab and is purged immediately when the tab closes, minimizing credential exposure on shared or educational lab computers.

### Q3: How does the application prevent answer leaking in online quizzes?
> **Answer**: The application implements a "Zero-Leak" architecture. When the frontend requests a quiz (`GET /api/quizzes/{id}`), the backend converts the JPA entities into a dedicated `QuizDetailDto` that completely omits `isCorrect` booleans and `explanation` text. Answers are graded purely server-side during `POST /api/quizzes/{id}/submit`, ensuring that inspecting browser memory, DOM, or network traffic reveals no answers beforehand.
