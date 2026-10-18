/**
 * Suraksha - Course Catalogue Controller (Milestone 8)
 *
 * Fetches educational courses from the Spring Boot REST API:
 * GET /api/courses?category=...&search=...
 *
 * Features:
 * - Dynamic category filtering & debounced keyword search
 * - Loading indicator during network requests
 * - Graceful fallback to cached curriculum if backend is unreachable
 * - Accessible empty states and responsive grid layout
 */

document.addEventListener("DOMContentLoaded", () => {
  const coursesGrid = document.getElementById("coursesGrid");
  const filterButtons = document.querySelectorAll(".filter-btn");
  const searchInput = document.getElementById("courseSearchInput");
  const activeCountEl = document.getElementById("activeCoursesCount");

  if (!coursesGrid) return;

  let currentCategory = "ALL";
  let currentSearchQuery = "";
  let searchDebounceTimeout = null;

  // Category filter handlers
  filterButtons.forEach((btn) => {
    btn.addEventListener("click", () => {
      filterButtons.forEach((b) => b.classList.remove("active"));
      btn.classList.add("active");
      currentCategory = btn.getAttribute("data-category") || "ALL";
      loadCourses();
    });
  });

  // Debounced search input handler (300ms)
  if (searchInput) {
    searchInput.addEventListener("input", (e) => {
      currentSearchQuery = e.target.value.trim();
      clearTimeout(searchDebounceTimeout);
      searchDebounceTimeout = setTimeout(() => {
        loadCourses();
      }, 300);
    });
  }

  // Initial load
  loadCourses();

  /**
   * Fetches courses from the backend REST API with fallback to sample data
   */
  async function loadCourses() {
    renderLoadingState();

    try {
      const courses = await SurakshaApi.courses.getAll({
        category: currentCategory,
        search: currentSearchQuery,
      });

      renderCoursesList(courses, false);
    } catch (err) {
      console.warn("Backend unavailable, using cached educational courses:", err);
      // Filter local fallback data
      const fallbackFiltered = (typeof SAMPLE_COURSES !== "undefined" ? SAMPLE_COURSES : []).filter((course) => {
        const matchesCategory =
          currentCategory === "ALL" || course.category === currentCategory;
        const matchesSearch =
          !currentSearchQuery ||
          course.title.toLowerCase().includes(currentSearchQuery.toLowerCase()) ||
          course.summary.toLowerCase().includes(currentSearchQuery.toLowerCase());
        return matchesCategory && matchesSearch;
      });

      renderCoursesList(fallbackFiltered, true);
    }
  }

  function renderLoadingState() {
    coursesGrid.innerHTML = `
      <div style="grid-column: 1 / -1; text-align: center; padding: 4rem 1.5rem;" aria-live="polite">
        <div class="loading-spinner" style="width: 2.5rem; height: 2.5rem; border: 3px solid #e2e8f0; border-top-color: var(--color-primary); border-radius: 50%; margin: 0 auto 1rem; animation: spin 0.8s linear infinite;"></div>
        <p style="color: var(--color-text-muted); font-size: 0.95rem;">Loading verified disaster preparedness courses...</p>
      </div>
    `;
  }

  function renderCoursesList(courses, isOfflineFallback) {
    if (activeCountEl) {
      activeCountEl.textContent = `${courses.length} Course${courses.length === 1 ? "" : "s"} Available`;
    }

    if (!courses || courses.length === 0) {
      coursesGrid.innerHTML = `
        <div class="card" style="grid-column: 1 / -1; text-align: center; padding: 3rem 1.5rem;">
          <h3 style="color: var(--color-text-muted); margin-bottom: 0.5rem;">No disaster courses matched your filter</h3>
          <p style="color: var(--color-text-muted); font-size: 0.9rem;">
            Try searching for a different keyword or select "All Hazards".
          </p>
        </div>
      `;
      return;
    }

    let offlineNoticeHtml = "";
    if (isOfflineFallback) {
      offlineNoticeHtml = `
        <div class="alert alert-info" style="grid-column: 1 / -1; margin-bottom: 1.5rem;" role="status">
          <div>
            <strong>ℹ️ Offline / Development Notice:</strong> Showing cached courses. To access the live database, ensure the Spring Boot server is running on <code>http://localhost:8080</code>.
          </div>
        </div>
      `;
    }

    const cardsHtml = courses.map((course) => {
      const category = course.category || "GENERAL";
      const badgeClass = getBadgeClass(category);
      const categoryLabel = formatCategoryLabel(category);
      const slug = course.slug || course.id;
      const quizParam = course.quizSlug || course.quizId || `quiz-${slug.replace("-101", "")}`;
      const lessonsCount = course.lessonsCount || (course.lessons ? course.lessons.length : 4);

      return `
        <article class="card">
          <div style="display: flex; justify-content: space-between; align-items: flex-start; margin-bottom: 0.75rem;">
            <span class="badge ${badgeClass}">${escapeHtml(categoryLabel)}</span>
            <span class="text-muted" style="font-size: 0.8rem; font-weight: 500;">⏱ ${course.estimatedMinutes || 20} mins</span>
          </div>

          <h3 class="card-title">${escapeHtml(course.title)}</h3>
          <p class="card-body">${escapeHtml(course.summary)}</p>

          <div style="margin: 1rem 0; font-size: 0.8rem; color: var(--color-text-muted); display: flex; gap: 1rem;">
            <span>📚 ${lessonsCount} Modules</span>
            <span>🎯 Certified Quiz</span>
          </div>

          <div class="card-footer" style="gap: 0.5rem; flex-wrap: wrap;">
            <a href="course-detail.html?courseId=${encodeURIComponent(slug)}" class="btn btn-primary btn-sm" style="flex: 1;">
              Start Learning &rarr;
            </a>
            <a href="quiz.html?quizId=${encodeURIComponent(quizParam)}" class="btn btn-secondary btn-sm" title="Jump straight to quiz">
              Take Quiz
            </a>
          </div>
        </article>
      `;
    }).join("");

    coursesGrid.innerHTML = offlineNoticeHtml + cardsHtml;
  }

  function getBadgeClass(category) {
    switch (category.toUpperCase()) {
      case "EARTHQUAKE": return "badge-danger";
      case "FLOOD": return "badge-primary";
      case "FIRE": return "badge-accent";
      case "CYCLONE": return "badge-secondary";
      default: return "badge-primary";
    }
  }

  function formatCategoryLabel(category) {
    switch (category.toUpperCase()) {
      case "EARTHQUAKE": return "Earthquake";
      case "FLOOD": return "Flood";
      case "FIRE": return "Fire Safety";
      case "CYCLONE": return "Cyclone";
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
