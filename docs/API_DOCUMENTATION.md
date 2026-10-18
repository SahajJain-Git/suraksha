# Suraksha REST API Specification & Reference Manual

> **Version**: 1.0.0 (Milestone 7 — Core REST APIs)  
> **Base URL**: `http://localhost:8080/api`  
> **Security Protocol**: Stateless JWT Bearer Token (`Authorization: Bearer <token>`)

---

## 1. Architectural Design & Security Principles

Suraksha REST APIs adhere to enterprise RESTful conventions:
* **Uniform Enveloping**: All successful responses are wrapped in a standard `ApiResponse<T>` payload (`success`, `message`, `data`, `timestamp`).
* **Predictable Error Contracts**: All error responses return structured `ErrorResponseDto` payloads (`timestamp`, `status`, `error`, `message`, `path`, optional `details` array).
* **Strict Answer Privacy (Zero-Leak Policy)**: Educational quizzes served via `GET /api/quizzes/{id}` strictly strip `isCorrect` flags and `explanation` text from response payloads. Answers are evaluated exclusively server-side upon authenticated submission.
* **Idempotent Operations**: Progress marking (`POST /api/progress/lessons/{id}`) is idempotent, preventing duplicate progress entries.
* **Strict User Isolation**: All personal progress endpoints (`GET /api/progress`, `POST /api/progress/**`, `POST /api/quizzes/**/submit`, `POST /api/drills/**/submit`) derive identity directly from the validated Spring Security `Authentication.getName()` context and never trust client-supplied user IDs.

---

## 2. API Endpoint Matrix

| Method | Endpoint | Access Policy | Description |
| :--- | :--- | :--- | :--- |
| **GET** | `/api/health` | Public | System health and database connectivity probe |
| **POST** | `/api/auth/register` | Public | Register student learner account |
| **POST** | `/api/auth/login` | Public | Authenticate credentials and receive JWT |
| **GET** | `/api/auth/me` | Authenticated | Fetch current user's profile and roles |
| **GET** | `/api/courses` | Public | List courses with optional category & keyword search |
| **GET** | `/api/courses/{slug}` | Public | Retrieve course details with ordered lessons |
| **GET** | `/api/quizzes/{id}` | Public | Get quiz questions & choices (secret answers stripped) |
| **POST** | `/api/quizzes/{id}/submit` | Authenticated | Submit answers for server-side grading & record attempt |
| **GET** | `/api/drills/{slug}` | Public | Retrieve mock drill scenario and decision steps |
| **POST** | `/api/drills/{id}/submit` | Authenticated | Submit decision choices, calculate score & record attempt |
| **POST** | `/api/contact` | Public | Submit contact / institutional partnership inquiry |
| **POST** | `/api/progress/lessons/{id}` | Authenticated | Mark lesson completed (idempotent) |
| **GET** | `/api/progress` | Authenticated | Retrieve learner dashboard & completion statistics |

---

## 3. Course APIs

### 3.1 List Courses
* **Endpoint**: `GET /api/courses`
* **Query Parameters**:
  * `category` *(optional, string)*: Filter by hazard category (e.g., `EARTHQUAKE`, `FLOOD`, `FIRE`, `CYCLONE`).
  * `search` *(optional, string)*: Case-insensitive search keyword in title or summary.
* **Response `200 OK`**:
```json
{
  "success": true,
  "message": "Courses retrieved successfully",
  "data": [
    {
      "id": 1,
      "slug": "earthquake-101",
      "title": "Earthquake Preparedness & Response",
      "category": "EARTHQUAKE",
      "difficulty": "BEGINNER",
      "estimatedMinutes": 25,
      "summary": "Master the Drop, Cover, and Hold On technique...",
      "lessonsCount": 4
    }
  ],
  "timestamp": "2026-10-18T10:00:00Z"
}
```

### 3.2 Get Course Details by Slug
* **Endpoint**: `GET /api/courses/{slug}`
* **Response `200 OK`**:
```json
{
  "success": true,
  "message": "Course details retrieved successfully",
  "data": {
    "id": 1,
    "slug": "earthquake-101",
    "title": "Earthquake Preparedness & Response",
    "category": "EARTHQUAKE",
    "difficulty": "BEGINNER",
    "estimatedMinutes": 25,
    "summary": "Master the Drop, Cover, and Hold On technique...",
    "quizId": 1,
    "quizSlug": "quiz-earthquake",
    "drillId": 1,
    "drillSlug": "drill-earthquake",
    "lessons": [
      {
        "id": 1,
        "slug": "eq-lesson-1",
        "title": "The Science of Shaking & Early Warnings",
        "sequenceOrder": 1,
        "content": "<p>Earthquakes occur when tectonic plates...</p>",
        "dosGuidelines": "Secure tall furniture and lab shelves...",
        "dontsGuidelines": "Do not place heavy textbooks on high open shelves..."
      }
    ]
  },
  "timestamp": "2026-10-18T10:00:00Z"
}
```
* **Error `404 Not Found`**:
```json
{
  "timestamp": "2026-10-18T10:00:00Z",
  "status": 404,
  "error": "Not Found",
  "message": "Course not found with slug: invalid-slug",
  "path": "/api/courses/invalid-slug"
}
```

---

## 4. Quiz APIs

### 4.1 Preview / Take Quiz
* **Endpoint**: `GET /api/quizzes/{id}` (`{id}` accepts numeric database ID or string slug e.g. `quiz-earthquake`)
* **Security**: Correct answers (`isCorrect`) and rationale (`explanation`) are strictly withheld.
* **Response `200 OK`**:
```json
{
  "success": true,
  "message": "Quiz retrieved successfully",
  "data": {
    "id": 1,
    "courseId": 1,
    "courseTitle": "Earthquake Preparedness & Response",
    "slug": "quiz-earthquake",
    "title": "Earthquake Safety Certification Quiz",
    "passingScorePercent": 75,
    "totalQuestions": 4,
    "questions": [
      {
        "id": 1,
        "sequenceOrder": 1,
        "questionText": "When an earthquake strikes while inside a multi-story building, what is the safest immediate action?",
        "options": [
          { "id": 1, "optionText": "Run down the stairs as fast as possible", "sequenceOrder": 1 },
          { "id": 2, "optionText": "Drop, Cover under a sturdy desk, and Hold On", "sequenceOrder": 2 },
          { "id": 3, "optionText": "Take the nearest elevator to ground level", "sequenceOrder": 3 },
          { "id": 4, "optionText": "Stand directly in the center of the room", "sequenceOrder": 4 }
        ]
      }
    ]
  },
  "timestamp": "2026-10-18T10:00:00Z"
}
```

### 4.2 Submit Quiz Answers
* **Endpoint**: `POST /api/quizzes/{id}/submit`
* **Headers**: `Authorization: Bearer <jwt_token>`
* **Request Body**:
```json
{
  "answers": [
    { "questionId": 1, "selectedOptionId": 2 },
    { "questionId": 2, "selectedOptionId": 7 },
    { "questionId": 3, "selectedOptionId": 11 },
    { "questionId": 4, "selectedOptionId": 14 }
  ]
}
```
* **Response `200 OK`**:
```json
{
  "success": true,
  "message": "Quiz submitted and evaluated successfully",
  "data": {
    "attemptId": 42,
    "quizId": 1,
    "quizSlug": "quiz-earthquake",
    "quizTitle": "Earthquake Safety Certification Quiz",
    "scorePercent": 100,
    "passingScorePercent": 75,
    "totalQuestions": 4,
    "correctAnswers": 4,
    "passed": true,
    "attemptedAt": "2026-10-18T10:05:00",
    "questionResults": [
      {
        "questionId": 1,
        "sequenceOrder": 1,
        "questionText": "When an earthquake strikes...",
        "selectedOptionId": 2,
        "correctOptionId": 2,
        "isCorrect": true,
        "explanation": "Running during active shaking frequently causes falls and severe injuries. Dropping under sturdy furniture shields your vital organs.",
        "options": [
          { "id": 1, "optionText": "Run down stairs...", "isCorrect": false, "sequenceOrder": 1 },
          { "id": 2, "optionText": "Drop, Cover, and Hold On...", "isCorrect": true, "sequenceOrder": 2 }
        ]
      }
    ]
  },
  "timestamp": "2026-10-18T10:05:00Z"
}
```

---

## 5. Virtual Mock Drill APIs

### 5.1 Get Drill Scenario
* **Endpoint**: `GET /api/drills/{slug}`
* **Response `200 OK`**:
```json
{
  "success": true,
  "message": "Drill scenario retrieved successfully",
  "data": {
    "id": 1,
    "courseId": 1,
    "courseTitle": "Earthquake Preparedness & Response",
    "slug": "drill-earthquake",
    "title": "High-Rise School Earthquake Simulation",
    "hazardCategory": "EARTHQUAKE",
    "description": "Experience simulated tremors in a 3rd floor classroom...",
    "initialStepId": 1,
    "steps": [
      {
        "id": 1,
        "stepKey": "step-1",
        "situationText": "You are seated on the 3rd floor during afternoon classes...",
        "questionText": "What is your immediate action?",
        "isTerminal": false,
        "choices": [
          {
            "id": 2,
            "choiceText": "Drop to your hands and knees under your sturdy wooden desk, Cover, and Hold On.",
            "outcomeType": "SAFE",
            "feedbackText": "EXCELLENT DECISION! Dropping prevents falls...",
            "scoreImpact": 50,
            "nextStepId": 2,
            "sequenceOrder": 2
          }
        ]
      }
    ]
  },
  "timestamp": "2026-10-18T10:00:00Z"
}
```

### 5.2 Submit Drill Decision Path
* **Endpoint**: `POST /api/drills/{id}/submit`
* **Headers**: `Authorization: Bearer <jwt_token>`
* **Request Body**:
```json
{
  "selectedChoiceIds": [2, 5, 7]
}
```
* **Response `200 OK`**:
```json
{
  "success": true,
  "message": "Drill decisions evaluated successfully",
  "data": {
    "attemptId": 18,
    "scenarioId": 1,
    "scenarioSlug": "drill-earthquake",
    "scenarioTitle": "High-Rise School Earthquake Simulation",
    "totalScore": 100,
    "ratingLabel": "Preparedness Master",
    "completedAt": "2026-10-18T10:12:00",
    "choiceFeedbacks": [
      {
        "choiceId": 2,
        "choiceText": "Drop to hands and knees under sturdy desk...",
        "outcomeType": "SAFE",
        "feedbackText": "EXCELLENT DECISION! Dropping prevents falls...",
        "scoreImpact": 50
      }
    ]
  },
  "timestamp": "2026-10-18T10:12:00Z"
}
```

---

## 6. Contact & Partnership API

### 6.1 Submit Contact Inquiry
* **Endpoint**: `POST /api/contact`
* **Access**: Public
* **Request Body**:
```json
{
  "fullName": "Priya Sharma",
  "email": "priya.sharma@school.edu",
  "category": "SCHOOL",
  "subject": "Request for School Safety Workshop",
  "message": "We would like to integrate the Suraksha virtual mock drills into our upcoming annual campus safety week for 400 high school students."
}
```
* **Response `201 Created`**:
```json
{
  "success": true,
  "message": "Thank you for reaching out. Your message has been received.",
  "data": {
    "id": 1,
    "fullName": "Priya Sharma",
    "email": "priya.sharma@school.edu",
    "category": "SCHOOL",
    "subject": "Request for School Safety Workshop",
    "status": "NEW",
    "createdAt": "2026-10-18T10:15:00"
  },
  "timestamp": "2026-10-18T10:15:00Z"
}
```

---

## 7. Learner Progress APIs

### 7.1 Mark Lesson Completed
* **Endpoint**: `POST /api/progress/lessons/{id}`
* **Headers**: `Authorization: Bearer <jwt_token>`
* **Behavior**: Idempotent. Subsequent calls return the existing completion record.
* **Response `200 OK`**:
```json
{
  "success": true,
  "message": "Lesson marked as completed successfully",
  "data": {
    "lessonId": 1,
    "lessonSlug": "eq-lesson-1",
    "lessonTitle": "The Science of Shaking & Early Warnings",
    "courseId": 1,
    "courseSlug": "earthquake-101",
    "courseTitle": "Earthquake Preparedness & Response",
    "completed": true,
    "completedAt": "2026-10-18T10:18:00"
  },
  "timestamp": "2026-10-18T10:18:00Z"
}
```

### 7.2 Get Learner Progress Dashboard
* **Endpoint**: `GET /api/progress`
* **Headers**: `Authorization: Bearer <jwt_token>`
* **Isolation**: Strictly scoped to the authenticated user.
* **Response `200 OK`**:
```json
{
  "success": true,
  "message": "User progress retrieved successfully",
  "data": {
    "userId": 1,
    "userEmail": "student@suraksha.org",
    "fullName": "Aarav Sharma",
    "totalCourses": 4,
    "completedCoursesCount": 1,
    "totalCompletedLessonsCount": 4,
    "completedLessonIds": [1, 2, 3, 4],
    "courseProgress": [
      {
        "courseId": 1,
        "courseSlug": "earthquake-101",
        "courseTitle": "Earthquake Preparedness & Response",
        "category": "EARTHQUAKE",
        "totalLessons": 4,
        "completedLessons": 4,
        "isCompleted": true,
        "progressPercent": 100
      }
    ],
    "totalQuizAttempts": 2,
    "totalQuizzesPassed": 1,
    "quizAttempts": [
      {
        "id": 1,
        "quizId": 1,
        "quizSlug": "quiz-earthquake",
        "quizTitle": "Earthquake Safety Certification Quiz",
        "scorePercent": 100,
        "totalQuestions": 4,
        "correctAnswers": 4,
        "passed": true,
        "attemptedAt": "2026-10-18T10:05:00"
      }
    ],
    "totalDrillAttempts": 1,
    "drillAttempts": [
      {
        "id": 1,
        "scenarioId": 1,
        "scenarioSlug": "drill-earthquake",
        "scenarioTitle": "High-Rise School Earthquake Simulation",
        "totalScore": 100,
        "ratingLabel": "Preparedness Master",
        "completedAt": "2026-10-18T10:12:00"
      }
    ]
  },
  "timestamp": "2026-10-18T10:20:00Z"
}
```
