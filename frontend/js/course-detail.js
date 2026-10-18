/**
 * Suraksha - Course Detail & Lesson Viewer (Milestone 8)
 *
 * Integrates:
 * - GET /api/courses/{slug} (loads course details and ordered lesson modules)
 * - POST /api/progress/lessons/{id} (persists lesson completions for authenticated learner)
 * - GET /api/progress (loads persisted completed lessons)
 *
 * Features:
 * - Smooth lesson pagination, DOs & DON'Ts presentation
 * - Progress tracking synchronized with backend database
 * - Offline fallback resilience to SAMPLE_COURSES
 */

document.addEventListener("DOMContentLoaded", async () => {
  const urlParams = new URLSearchParams(window.location.search);
  const courseSlug = urlParams.get("courseId") || "earthquake-101";

  // Header Elements
  const courseTitleEl = document.getElementById("courseTitle");
  const courseCategoryBadge = document.getElementById("courseCategoryBadge");
  const courseSummaryEl = document.getElementById("courseSummary");
  const quizLinkBtn = document.getElementById("quizLinkBtn");
  const drillLinkBtn = document.getElementById("drillLinkBtn");

  // Lesson Viewer Elements
  const lessonListContainer = document.getElementById("lessonList");
  const activeLessonTitle = document.getElementById("activeLessonTitle");
  const activeLessonContent = document.getElementById("activeLessonContent");
  const dosContainer = document.getElementById("dosContainer");
  const dontsContainer = document.getElementById("dontsContainer");
  const prevLessonBtn = document.getElementById("prevLessonBtn");
  const nextLessonBtn = document.getElementById("nextLessonBtn");
  const progressBadge = document.getElementById("courseProgressBadge");

  let course = null;
  let currentLessonIndex = 0;
  let completedLessonIds = new Set();

  // Load Course and User Progress
  await initCourseData();

  async function initCourseData() {
    try {
      course = await SurakshaApi.courses.getBySlug(courseSlug);
    } catch (err) {
      console.warn("Could not fetch course from backend, using fallback data:", err);
      course = (typeof SAMPLE_COURSES !== "undefined" ? SAMPLE_COURSES : []).find(
        (c) => c.id === courseSlug || c.slug === courseSlug
      ) || (SAMPLE_COURSES && SAMPLE_COURSES[0]);
    }

    if (!course) {
      if (activeLessonContent) {
        activeLessonContent.innerHTML = `<div class="alert alert-danger">Course not found. <a href="courses.html">Back to courses</a></div>`;
      }
      return;
    }

    // Populate Course Header
    if (courseTitleEl) courseTitleEl.textContent = course.title;
    if (courseCategoryBadge) {
      const category = course.category || "GENERAL";
      courseCategoryBadge.textContent = formatCategoryLabel(category);
      courseCategoryBadge.className = `badge ${getBadgeClass(category)}`;
    }
    if (courseSummaryEl) courseSummaryEl.textContent = course.summary;

    const quizParam = course.quizSlug || course.quizId || `quiz-${course.slug || courseSlug}`;
    const drillParam = course.drillSlug || course.drillId || `drill-${course.slug || courseSlug}`;

    if (quizLinkBtn) quizLinkBtn.href = `quiz.html?quizId=${encodeURIComponent(quizParam)}`;
    if (drillLinkBtn) drillLinkBtn.href = `mock-drill.html?drillId=${encodeURIComponent(drillParam)}`;

    // Sync Completed Lessons from Backend Progress API if Authenticated
    if (SurakshaApi.auth.isAuthenticated()) {
      try {
        const userProgress = await SurakshaApi.progress.getUserProgress();
        if (userProgress && userProgress.completedLessonIds) {
          userProgress.completedLessonIds.forEach((id) => completedLessonIds.add(id));
        }
      } catch (e) {
        console.warn("Could not fetch backend progress:", e);
      }
    } else {
      // Local fallback in sessionStorage
      const localCompleted = JSON.parse(
        sessionStorage.getItem(`completed_${course.slug || course.id}`) || "[]"
      );
      localCompleted.forEach((id) => completedLessonIds.add(id));
    }

    renderSidebar();
    renderCurrentLesson();
  }

  // Navigation handlers
  if (prevLessonBtn) {
    prevLessonBtn.addEventListener("click", () => {
      if (currentLessonIndex > 0) {
        currentLessonIndex--;
        renderSidebar();
        renderCurrentLesson();
        window.scrollTo({ top: 300, behavior: "smooth" });
      }
    });
  }

  if (nextLessonBtn) {
    nextLessonBtn.addEventListener("click", async () => {
      if (!course || !course.lessons || !course.lessons.length) return;

      const currentLesson = course.lessons[currentLessonIndex];

      // Mark current lesson completed
      completedLessonIds.add(currentLesson.id);

      // Save locally
      sessionStorage.setItem(
        `completed_${course.slug || course.id}`,
        JSON.stringify(Array.from(completedLessonIds))
      );

      // Persist to backend if authenticated
      if (SurakshaApi.auth.isAuthenticated()) {
        try {
          await SurakshaApi.progress.markLessonCompleted(currentLesson.id);
        } catch (e) {
          console.warn("Could not persist lesson progress to backend:", e);
        }
      }

      if (currentLessonIndex < course.lessons.length - 1) {
        currentLessonIndex++;
        renderSidebar();
        renderCurrentLesson();
        window.scrollTo({ top: 300, behavior: "smooth" });
      } else {
        renderSidebar();
        const quizParam = course.quizSlug || course.quizId || `quiz-${course.slug || courseSlug}`;
        alert("🎉 Congratulations! You have completed all lessons in this course. You can now take the certified quiz!");
        window.location.href = `quiz.html?quizId=${encodeURIComponent(quizParam)}`;
      }
    });
  }

  function renderSidebar() {
    if (!lessonListContainer || !course || !course.lessons) return;

    if (progressBadge) {
      const total = course.lessons.length;
      let completedInCourse = 0;
      course.lessons.forEach((l) => {
        if (completedLessonIds.has(l.id)) completedInCourse++;
      });
      const pct = total > 0 ? Math.round((completedInCourse / total) * 100) : 0;
      progressBadge.textContent = `${pct}% Complete`;
    }

    lessonListContainer.innerHTML = course.lessons
      .map((lesson, idx) => {
        const isActive = idx === currentLessonIndex;
        const isCompleted = completedLessonIds.has(lesson.id);
        return `
        <li>
          <button type="button" class="lesson-item-btn ${isActive ? "active" : ""} ${isCompleted ? "completed" : ""}" data-index="${idx}">
            <span>${idx + 1}. ${escapeHtml(lesson.title)}</span>
            <span class="lesson-badge">${isCompleted ? "✓" : idx + 1}</span>
          </button>
        </li>
      `;
      })
      .join("");

    lessonListContainer.querySelectorAll(".lesson-item-btn").forEach((btn) => {
      btn.addEventListener("click", () => {
        currentLessonIndex = parseInt(btn.getAttribute("data-index"), 10);
        renderSidebar();
        renderCurrentLesson();
        window.scrollTo({ top: 300, behavior: "smooth" });
      });
    });
  }

  function renderCurrentLesson() {
    if (!course || !course.lessons || !course.lessons.length) return;

    const lesson = course.lessons[currentLessonIndex];
    if (!lesson) return;

    if (activeLessonTitle) {
      activeLessonTitle.textContent = `${currentLessonIndex + 1}. ${lesson.title}`;
    }

    if (activeLessonContent) {
      // Content contains trusted educational HTML from backend seed
      activeLessonContent.innerHTML = lesson.content || "<p>Lesson content loading...</p>";
    }

    // Render DOs
    if (dosContainer) {
      const dosList = parseGuidelines(lesson.dosGuidelines || lesson.dos);
      if (dosList.length) {
        dosContainer.innerHTML = dosList.map((item) => `<li>${escapeHtml(item)}</li>`).join("");
      } else {
        dosContainer.innerHTML = `<li>Follow instructions from official emergency personnel.</li>`;
      }
    }

    // Render DON'Ts
    if (dontsContainer) {
      const dontsList = parseGuidelines(lesson.dontsGuidelines || lesson.donts);
      if (dontsList.length) {
        dontsContainer.innerHTML = dontsList.map((item) => `<li>${escapeHtml(item)}</li>`).join("");
      } else {
        dontsContainer.innerHTML = `<li>Do not spread unverified rumors or panic.</li>`;
      }
    }

    // Update Next/Previous Button states
    if (prevLessonBtn) {
      prevLessonBtn.disabled = currentLessonIndex === 0;
    }
    if (nextLessonBtn) {
      const isLast = currentLessonIndex === course.lessons.length - 1;
      nextLessonBtn.innerHTML = isLast
        ? "Finish & Go to Quiz &rarr;"
        : "Complete & Next Lesson &rarr;";
    }
  }

  function parseGuidelines(guidelines) {
    if (!guidelines) return [];
    if (Array.isArray(guidelines)) return guidelines;
    if (typeof guidelines === "string") {
      return guidelines
        .split("\n")
        .map((s) => s.trim())
        .filter((s) => s.length > 0);
    }
    return [];
  }

  function getBadgeClass(category) {
    switch (String(category).toUpperCase()) {
      case "EARTHQUAKE": return "badge-danger";
      case "FLOOD": return "badge-primary";
      case "FIRE": return "badge-accent";
      case "CYCLONE": return "badge-secondary";
      default: return "badge-primary";
    }
  }

  function formatCategoryLabel(category) {
    switch (String(category).toUpperCase()) {
      case "EARTHQUAKE": return "Earthquake Safety";
      case "FLOOD": return "Flood Mitigation";
      case "FIRE": return "Fire Safety";
      case "CYCLONE": return "Cyclone Mitigation";
      default: return category;
    }
  }

  function escapeHtml(str) {
    if (str === null || str === undefined) return "";
    const div = document.createElement("div");
    div.textContent = String(str);
    return div.innerHTML;
  }
});
