/**
 * Suraksha - Learner Progress & Certification Dashboard (Milestone 8)
 *
 * Connects to:
 * - GET /api/progress (retrieves isolated student progress, lesson completions, quiz attempts, and drill scores)
 * - GET /api/auth/me (retrieves verified student profile)
 *
 * Enforces strict user isolation: only the authenticated user's records are displayed.
 */

document.addEventListener("DOMContentLoaded", async () => {
  const container = document.getElementById("dashboardContentContainer");
  const heading = document.getElementById("dashboardWelcomeHeading");
  const subheading = document.getElementById("dashboardSubheading");

  if (!container) return;

  if (!SurakshaApi.auth.isAuthenticated()) {
    renderUnauthenticatedPrompt();
    return;
  }

  await loadDashboard();

  async function loadDashboard() {
    try {
      const progress = await SurakshaApi.progress.getUserProgress();
      renderDashboard(progress);
    } catch (err) {
      console.error("Failed to load user progress:", err);
      let errorMsg = err.message || "Could not retrieve your learning progress.";
      if (err.isNetworkError) {
        errorMsg = "Unable to connect to the backend server (http://localhost:8080). Please ensure the Spring Boot service is active.";
      }

      container.innerHTML = `
        <div class="card" style="padding: 2.5rem; text-align: center; border-top: 4px solid var(--color-danger);">
          <h3 style="color: var(--color-danger); margin-bottom: 0.5rem;">Dashboard Unavailable</h3>
          <p style="color: var(--color-text-muted); margin-bottom: 1.5rem;">${escapeHtml(errorMsg)}</p>
          <button type="button" class="btn btn-primary" onclick="window.location.reload()">Retry Loading</button>
        </div>
      `;
    }
  }

  function renderUnauthenticatedPrompt() {
    if (heading) heading.textContent = "Student Learning Dashboard";
    if (subheading) subheading.textContent = "Please sign in to view your learning records.";

    container.innerHTML = `
      <div class="card" style="padding: 3rem 1.5rem; text-align: center; max-width: 600px; margin: 0 auto;">
        <div class="brand-icon" style="margin: 0 auto 1.5rem; width: 64px; height: 64px;">
          <svg viewBox="0 0 24 24" aria-hidden="true" style="width: 36px; height: 36px;">
            <path d="M12 2L4 5v6.09c0 5.05 3.41 9.76 8 10.91 4.59-1.15 8-5.86 8-10.91V5l-8-3zm1 14h-2v-2h2v2zm0-4h-2V7h2v5z"/>
          </svg>
        </div>
        <h2 style="margin-bottom: 0.75rem;">Sign In Required</h2>
        <p style="color: var(--color-text-muted); margin-bottom: 2rem; font-size: 0.95rem;">
          To track your lesson completions, certified quiz scores, and virtual drill readiness ratings, sign in with your student account.
        </p>
        <div style="display: flex; justify-content: center; gap: 1rem; flex-wrap: wrap;">
          <a href="login.html?redirect=dashboard.html" class="btn btn-primary btn-lg">Sign In &rarr;</a>
          <a href="register.html" class="btn btn-secondary btn-lg">Create Account</a>
        </div>
      </div>
    `;
  }

  function renderDashboard(data) {
    const studentName = data.fullName || data.userEmail.split("@")[0];
    if (heading) heading.textContent = `Welcome, ${studentName}!`;
    if (subheading) {
      subheading.textContent = `Account: ${data.userEmail} • Student ID #${data.userId} • Verified Learner Profile`;
    }

    const completedLessons = data.totalCompletedLessonsCount || 0;
    const completedCourses = data.completedCoursesCount || 0;
    const totalCourses = data.totalCourses || 4;
    const quizzesPassed = data.totalQuizzesPassed || 0;
    const totalDrills = data.totalDrillAttempts || 0;

    container.innerHTML = `
      <!-- Top Metrics Row -->
      <div style="display: grid; grid-template-columns: repeat(auto-fit, minmax(200px, 1fr)); gap: 1.25rem; margin-bottom: 2.5rem;">
        <div class="card" style="padding: 1.5rem; border-top: 4px solid var(--color-primary); text-align: center;">
          <div style="font-size: 0.8rem; font-weight: 700; color: var(--color-text-muted); text-transform: uppercase;">Lessons Mastered</div>
          <div style="font-size: 2.25rem; font-weight: 800; color: var(--color-primary); margin: 0.25rem 0;">${completedLessons}</div>
          <span style="font-size: 0.8rem; color: #059669;">Verified by Backend</span>
        </div>

        <div class="card" style="padding: 1.5rem; border-top: 4px solid var(--color-accent); text-align: center;">
          <div style="font-size: 0.8rem; font-weight: 700; color: var(--color-text-muted); text-transform: uppercase;">Courses Completed</div>
          <div style="font-size: 2.25rem; font-weight: 800; color: var(--color-accent); margin: 0.25rem 0;">${completedCourses} / ${totalCourses}</div>
          <span style="font-size: 0.8rem; color: var(--color-text-muted);">Curriculum Progress</span>
        </div>

        <div class="card" style="padding: 1.5rem; border-top: 4px solid #10b981; text-align: center;">
          <div style="font-size: 0.8rem; font-weight: 700; color: var(--color-text-muted); text-transform: uppercase;">Quizzes Passed</div>
          <div style="font-size: 2.25rem; font-weight: 800; color: #047857; margin: 0.25rem 0;">${quizzesPassed}</div>
          <span style="font-size: 0.8rem; color: #047857;">Certified 75%+</span>
        </div>

        <div class="card" style="padding: 1.5rem; border-top: 4px solid var(--color-secondary); text-align: center;">
          <div style="font-size: 0.8rem; font-weight: 700; color: var(--color-text-muted); text-transform: uppercase;">Drills Executed</div>
          <div style="font-size: 2.25rem; font-weight: 800; color: var(--color-secondary); margin: 0.25rem 0;">${totalDrills}</div>
          <span style="font-size: 0.8rem; color: var(--color-text-muted);">Branching Simulations</span>
        </div>
      </div>

      <!-- Course Progress Section -->
      <div style="margin-bottom: 3rem;">
        <div style="display: flex; justify-content: space-between; align-items: center; margin-bottom: 1.25rem; flex-wrap: wrap; gap: 0.5rem;">
          <h2 style="font-size: 1.5rem; margin: 0;">Course Progression</h2>
          <a href="courses.html" class="btn btn-secondary btn-sm">Explore Courses &rarr;</a>
        </div>

        <div style="display: grid; grid-template-columns: repeat(auto-fit, minmax(280px, 1fr)); gap: 1.25rem;">
          ${(data.courseProgress || []).map((cp) => {
            const isDone = cp.isCompleted;
            const pct = cp.progressPercent || 0;
            return `
              <div class="card" style="padding: 1.5rem;">
                <div style="display: flex; justify-content: space-between; align-items: flex-start; margin-bottom: 0.5rem;">
                  <span class="badge ${isDone ? "badge-success" : "badge-primary"}">${escapeHtml(cp.category)}</span>
                  <span style="font-size: 0.85rem; font-weight: 700; color: ${isDone ? "#047857" : "var(--color-primary)"};">
                    ${pct}% Complete
                  </span>
                </div>

                <h3 style="font-size: 1.1rem; margin-bottom: 0.5rem;">${escapeHtml(cp.courseTitle)}</h3>

                <!-- Progress Bar -->
                <div style="width: 100%; height: 8px; background: #e2e8f0; border-radius: 4px; overflow: hidden; margin: 0.75rem 0;">
                  <div style="width: ${pct}%; height: 100%; background: ${isDone ? "#10b981" : "var(--color-primary)"}; transition: width 0.3s ease;"></div>
                </div>

                <div style="display: flex; justify-content: space-between; align-items: center; font-size: 0.8rem; color: var(--color-text-muted); margin-bottom: 1rem;">
                  <span>${cp.completedLessons} of ${cp.totalLessons} modules completed</span>
                  ${isDone ? '<span style="color: #047857; font-weight: bold;">Certified ✓</span>' : '<span>In Progress</span>'}
                </div>

                <a href="course-detail.html?courseId=${encodeURIComponent(cp.courseSlug)}" class="btn btn-primary btn-sm" style="width: 100%;">
                  ${isDone ? "Review Course" : "Continue Learning &rarr;"}
                </a>
              </div>
            `;
          }).join("")}
        </div>
      </div>

      <!-- Quiz Attempts History -->
      <div style="margin-bottom: 3rem;">
        <h2 style="font-size: 1.5rem; margin-bottom: 1.25rem;">Official Quiz Certification History</h2>
        ${(data.quizAttempts && data.quizAttempts.length > 0)
          ? `
            <div style="overflow-x: auto;">
              <table class="data-table" style="width: 100%; border-collapse: collapse; background: #fff; border-radius: 8px; overflow: hidden; box-shadow: var(--shadow-sm);">
                <thead>
                  <tr style="background: #f8fafc; border-bottom: 2px solid #e2e8f0; text-align: left; font-size: 0.85rem; color: var(--color-text-muted);">
                    <th style="padding: 0.75rem 1rem;">Quiz Title</th>
                    <th style="padding: 0.75rem 1rem;">Score</th>
                    <th style="padding: 0.75rem 1rem;">Correct</th>
                    <th style="padding: 0.75rem 1rem;">Certification Status</th>
                    <th style="padding: 0.75rem 1rem;">Attempted At</th>
                  </tr>
                </thead>
                <tbody>
                  ${data.quizAttempts.map((qa) => `
                    <tr style="border-bottom: 1px solid #f1f5f9; font-size: 0.9rem;">
                      <td style="padding: 0.75rem 1rem; font-weight: 600;">${escapeHtml(qa.quizTitle)}</td>
                      <td style="padding: 0.75rem 1rem; font-weight: 700; color: ${qa.passed ? "#047857" : "#b91c1c"};">${qa.scorePercent}%</td>
                      <td style="padding: 0.75rem 1rem;">${qa.correctAnswers} / ${qa.totalQuestions}</td>
                      <td style="padding: 0.75rem 1rem;">
                        <span class="badge ${qa.passed ? "badge-success" : "badge-danger"}">${qa.passed ? "PASS ✓" : "RETAKE NEEDED"}</span>
                      </td>
                      <td style="padding: 0.75rem 1rem; color: var(--color-text-muted); font-size: 0.85rem;">
                        ${formatDate(qa.attemptedAt)}
                      </td>
                    </tr>
                  `).join("")}
                </tbody>
              </table>
            </div>
          `
          : `
            <div class="card" style="padding: 2rem; text-align: center; color: var(--color-text-muted);">
              <p>No quiz attempts on record yet. Complete a course and take its certified quiz!</p>
              <a href="courses.html" class="btn btn-secondary btn-sm" style="margin-top: 0.5rem;">Browse Quizzes &rarr;</a>
            </div>
          `
        }
      </div>

      <!-- Virtual Mock Drill History -->
      <div>
        <h2 style="font-size: 1.5rem; margin-bottom: 1.25rem;">Virtual Mock Drill Transcripts</h2>
        ${(data.drillAttempts && data.drillAttempts.length > 0)
          ? `
            <div style="overflow-x: auto;">
              <table class="data-table" style="width: 100%; border-collapse: collapse; background: #fff; border-radius: 8px; overflow: hidden; box-shadow: var(--shadow-sm);">
                <thead>
                  <tr style="background: #f8fafc; border-bottom: 2px solid #e2e8f0; text-align: left; font-size: 0.85rem; color: var(--color-text-muted);">
                    <th style="padding: 0.75rem 1rem;">Simulation Scenario</th>
                    <th style="padding: 0.75rem 1rem;">Readiness Score</th>
                    <th style="padding: 0.75rem 1rem;">Performance Rating</th>
                    <th style="padding: 0.75rem 1rem;">Completed At</th>
                  </tr>
                </thead>
                <tbody>
                  ${data.drillAttempts.map((da) => `
                    <tr style="border-bottom: 1px solid #f1f5f9; font-size: 0.9rem;">
                      <td style="padding: 0.75rem 1rem; font-weight: 600;">${escapeHtml(da.scenarioTitle)}</td>
                      <td style="padding: 0.75rem 1rem; font-weight: 700; color: var(--color-primary);">${da.totalScore} pts</td>
                      <td style="padding: 0.75rem 1rem;">
                        <span class="badge badge-accent">${escapeHtml(da.ratingLabel)}</span>
                      </td>
                      <td style="padding: 0.75rem 1rem; color: var(--color-text-muted); font-size: 0.85rem;">
                        ${formatDate(da.completedAt)}
                      </td>
                    </tr>
                  `).join("")}
                </tbody>
              </table>
            </div>
          `
          : `
            <div class="card" style="padding: 2rem; text-align: center; color: var(--color-text-muted);">
              <p>No virtual mock drill attempts recorded yet. Test your split-second decisions in simulated hazards!</p>
              <a href="mock-drill.html" class="btn btn-secondary btn-sm" style="margin-top: 0.5rem;">Launch Virtual Drill &rarr;</a>
            </div>
          `
        }
      </div>
    `;
  }

  function formatDate(dateStr) {
    if (!dateStr) return "Just now";
    try {
      const d = new Date(dateStr);
      return d.toLocaleDateString(undefined, { year: "numeric", month: "short", day: "numeric", hour: "2-digit", minute: "2-digit" });
    } catch {
      return dateStr;
    }
  }

  function escapeHtml(str) {
    if (str === null || str === undefined) return "";
    const div = document.createElement("div");
    div.textContent = String(str);
    return div.innerHTML;
  }
});
