/**
 * Suraksha - Virtual Mock Drill Scenario Engine (Milestone 8)
 *
 * Integrates:
 * - GET /api/drills/{slug} (loads simulation steps and branching decision trees)
 * - POST /api/drills/{id}/submit (server-side scoring and DrillAttempt persistence)
 *
 * Features:
 * - Immediate pedagogical feedback on safe vs dangerous emergency actions
 * - Dynamic readiness score calculation
 * - Authoritative rating label persistence in MySQL via Spring Data JPA
 */

document.addEventListener("DOMContentLoaded", async () => {
  const urlParams = new URLSearchParams(window.location.search);
  let activeDrillSlug = urlParams.get("drillId") || "drill-earthquake";

  const drillTitleEl = document.getElementById("drillTitle");
  const drillHazardTag = document.getElementById("drillHazardTag");
  const drillScenarioSelect = document.getElementById("drillScenarioSelect");
  const scoreCounterEl = document.getElementById("drillScoreCounter");
  const situationBox = document.getElementById("drillSituationBox");
  const questionBox = document.getElementById("drillQuestionBox");
  const choicesContainer = document.getElementById("drillChoicesContainer");
  const feedbackContainer = document.getElementById("drillFeedbackContainer");
  const nextStepBtn = document.getElementById("drillNextStepBtn");
  const drillContainer = document.getElementById("drillCardContainer");
  const debriefContainer = document.getElementById("drillDebriefContainer");

  let scenario = null;
  let currentStep = null;
  let readinessScore = 0;
  let selectedChoiceIds = [];
  let userChoiceHistory = [];

  // Dropdown switcher
  if (drillScenarioSelect) {
    drillScenarioSelect.value = activeDrillSlug;
    drillScenarioSelect.addEventListener("change", (e) => {
      activeDrillSlug = e.target.value;
      loadDrill();
    });
  }

  await loadDrill();

  async function loadDrill() {
    renderLoadingDrill();

    try {
      scenario = await SurakshaApi.drills.getBySlug(activeDrillSlug);
    } catch (err) {
      console.warn("Could not fetch drill from backend, using fallback data:", err);
      const fallback = (typeof SAMPLE_DRILLS !== "undefined" && SAMPLE_DRILLS[activeDrillSlug]) ||
                       (typeof SAMPLE_DRILLS !== "undefined" && SAMPLE_DRILLS["drill-earthquake"]);
      if (fallback) {
        scenario = convertFallbackToScenario(fallback);
      }
    }

    if (!scenario) {
      if (drillContainer) {
        drillContainer.innerHTML = `
          <div class="card-body text-center" style="padding: 3rem;">
            <h3 style="color: var(--color-danger);">Simulation Unavailable</h3>
            <p>Could not load scenario details.</p>
            <a href="courses.html" class="btn btn-primary">Return to Courses</a>
          </div>
        `;
      }
      return;
    }

    resetDrillState();
  }

  function resetDrillState() {
    readinessScore = 0;
    selectedChoiceIds = [];
    userChoiceHistory = [];

    if (drillTitleEl) drillTitleEl.textContent = scenario.title;
    if (drillHazardTag) drillHazardTag.textContent = `${scenario.hazardCategory || "Disaster"} Simulation`;
    if (drillContainer) drillContainer.style.display = "block";
    if (debriefContainer) debriefContainer.style.display = "none";

    updateScoreDisplay();

    // Determine initial step
    const steps = scenario.steps || [];
    currentStep = steps.find((s) => s.id === scenario.initialStepId) || steps[0];
    renderStep();
  }

  function renderLoadingDrill() {
    if (situationBox) {
      situationBox.innerHTML = `<em>Loading virtual scenario simulation...</em>`;
    }
    if (choicesContainer) {
      choicesContainer.innerHTML = `
        <div style="text-align: center; padding: 2rem;">
          <div class="loading-spinner" style="width: 2rem; height: 2rem; border: 3px solid #e2e8f0; border-top-color: var(--color-primary); border-radius: 50%; margin: 0 auto 0.75rem; animation: spin 0.8s linear infinite;"></div>
        </div>
      `;
    }
  }

  function updateScoreDisplay() {
    if (scoreCounterEl) {
      scoreCounterEl.textContent = `Readiness Score: ${readinessScore} pts`;
    }
  }

  function renderStep() {
    if (!currentStep) return;

    if (currentStep.isTerminal) {
      finishDrill();
      return;
    }

    if (situationBox) {
      situationBox.innerHTML = `<strong>Situation:</strong> ${escapeHtml(currentStep.situationText || currentStep.situation)}`;
    }
    if (questionBox) {
      questionBox.textContent = currentStep.questionText || currentStep.question || "What is your immediate action?";
    }

    if (feedbackContainer) {
      feedbackContainer.style.display = "none";
      feedbackContainer.innerHTML = "";
    }
    if (nextStepBtn) {
      nextStepBtn.style.display = "none";
    }

    // Render Choice Buttons
    if (choicesContainer) {
      const choices = currentStep.choices || [];
      choicesContainer.innerHTML = choices
        .map((choice, idx) => {
          return `
          <button type="button" class="drill-choice-btn" data-choice-id="${choice.id}">
            <span style="font-weight: bold; color: var(--color-primary);">${idx + 1}.</span>
            <span>${escapeHtml(choice.choiceText || choice.text)}</span>
          </button>
        `;
        })
        .join("");

      choicesContainer.querySelectorAll(".drill-choice-btn").forEach((btn) => {
        btn.addEventListener("click", () => {
          const choiceId = isNaN(btn.getAttribute("data-choice-id"))
            ? btn.getAttribute("data-choice-id")
            : Number(btn.getAttribute("data-choice-id"));

          const selectedChoice = choices.find((c) => c.id === choiceId);
          if (selectedChoice) {
            handleChoiceSelection(selectedChoice);
          }
        });
      });
    }
  }

  function handleChoiceSelection(choice) {
    // Disable all choice buttons to prevent duplicate clicks
    if (choicesContainer) {
      choicesContainer.querySelectorAll(".drill-choice-btn").forEach((b) => {
        b.disabled = true;
      });
    }

    // Record choice
    selectedChoiceIds.push(choice.id);
    readinessScore += (choice.scoreImpact !== undefined ? choice.scoreImpact : 0);
    updateScoreDisplay();

    const isSafe = choice.outcomeType === "SAFE";
    const impactColor = isSafe ? "#065f46" : "#991b1b";
    const impactBg = isSafe ? "#ecfdf5" : "#fef2f2";
    const impactBorder = isSafe ? "#10b981" : "#ef4444";
    const scoreText = (choice.scoreImpact > 0 ? `+${choice.scoreImpact}` : `${choice.scoreImpact}`) + " pts";

    userChoiceHistory.push({
      stepQuestion: currentStep.questionText || currentStep.question,
      choiceText: choice.choiceText || choice.text,
      outcomeType: choice.outcomeType,
      feedbackText: choice.feedbackText || choice.feedback,
      scoreImpact: choice.scoreImpact,
    });

    if (feedbackContainer) {
      feedbackContainer.style.display = "block";
      feedbackContainer.innerHTML = `
        <div style="background: ${impactBg}; border-left: 5px solid ${impactBorder}; padding: 1.25rem; border-radius: 6px; margin-top: 1rem;">
          <div style="display: flex; justify-content: space-between; align-items: center; margin-bottom: 0.5rem;">
            <strong style="color: ${impactColor}; font-size: 0.95rem;">
              ${isSafe ? "✓ SAFE DECISION" : "⚠ CRITICAL HAZARD RISK"}
            </strong>
            <span class="badge ${isSafe ? "badge-success" : "badge-danger"}">${scoreText}</span>
          </div>
          <p style="color: var(--color-text); font-size: 0.875rem; line-height: 1.6; margin: 0;">
            ${escapeHtml(choice.feedbackText || choice.feedback)}
          </p>
        </div>
      `;
    }

    // Determine Next Step
    const nextStepId = choice.nextStepId;
    const steps = scenario.steps || [];
    const nextStepObj = steps.find((s) => s.id === nextStepId || s.stepKey === nextStepId);

    if (nextStepBtn) {
      nextStepBtn.style.display = "inline-block";
      const isFinal = !nextStepObj || nextStepObj.isTerminal;
      nextStepBtn.textContent = isFinal ? "Complete Simulation & Debrief &rarr;" : "Proceed to Next Situation &rarr;";

      // Clean listener
      const newNextBtn = nextStepBtn.cloneNode(true);
      nextStepBtn.parentNode.replaceChild(newNextBtn, nextStepBtn);

      newNextBtn.addEventListener("click", () => {
        if (!nextStepObj || nextStepObj.isTerminal) {
          finishDrill();
        } else {
          currentStep = nextStepObj;
          renderStep();
        }
      });
    }
  }

  async function finishDrill() {
    if (drillContainer) drillContainer.style.display = "none";
    if (!debriefContainer) return;

    debriefContainer.style.display = "block";

    const isAuth = SurakshaApi.auth.isAuthenticated();
    let result = null;

    if (isAuth && scenario.id) {
      try {
        result = await SurakshaApi.drills.submit(scenario.id, { selectedChoiceIds });
      } catch (err) {
        console.warn("Could not submit drill attempt to backend:", err);
      }
    }

    const finalScore = (result && result.totalScore !== undefined) ? result.totalScore : readinessScore;
    const ratingLabel = (result && result.ratingLabel) ? result.ratingLabel : determineRatingLabel(finalScore);

    let authNotice = "";
    if (!isAuth) {
      authNotice = `
        <div class="alert alert-info" style="margin-bottom: 1.5rem;" role="status">
          <div>
            <strong>Guest Mode:</strong> You completed this simulation as a guest. <a href="login.html?redirect=${encodeURIComponent(window.location.href)}">Sign in</a> to save official drill scores to your transcript.
          </div>
        </div>
      `;
    }

    debriefContainer.innerHTML = `
      <div class="card" style="padding: 2.5rem 1.5rem; text-align: center;">
        <span class="badge badge-primary" style="font-size: 0.9rem; padding: 0.4rem 0.8rem; margin: 0 auto 0.75rem;">
          SIMULATION CONCLUDED
        </span>

        <h2 style="font-size: 1.75rem; margin-bottom: 0.5rem;">${escapeHtml(scenario.title)} Debrief</h2>
        <p style="color: var(--color-text-muted); font-size: 0.95rem; max-width: 600px; margin: 0 auto 1.5rem;">
          You navigated through all emergency branch points. Here is your evaluation:
        </p>

        ${authNotice}

        <!-- Readiness Score Badge -->
        <div style="background: #f8fafc; border: 2px dashed #cbd5e1; border-radius: 12px; padding: 1.5rem; max-width: 400px; margin: 0 auto 2rem;">
          <div style="font-size: 0.85rem; font-weight: 700; color: var(--color-text-muted); text-transform: uppercase;">
            Calculated Readiness Score
          </div>
          <div style="font-size: 2.5rem; font-weight: 800; color: var(--color-primary); margin: 0.25rem 0;">
            ${finalScore} pts
          </div>
          <div class="badge badge-accent" style="font-size: 0.95rem; padding: 0.35rem 0.75rem;">
            ${escapeHtml(ratingLabel)}
          </div>
        </div>

        <!-- Decision Log -->
        <h3 style="text-align: left; font-size: 1.25rem; margin: 1.5rem 0 1rem; border-bottom: 2px solid #e2e8f0; padding-bottom: 0.5rem;">
          Your Decision Trace & Expert Rationale
        </h3>

        <div style="text-align: left; display: flex; flex-direction: column; gap: 1rem;">
          ${userChoiceHistory.map((item, idx) => {
            const isSafe = item.outcomeType === "SAFE";
            return `
              <div class="card" style="padding: 1rem; border-left: 4px solid ${isSafe ? "#10b981" : "#ef4444"};">
                <div style="display: flex; justify-content: space-between; align-items: flex-start; gap: 0.5rem;">
                  <strong>Decision ${idx + 1}: ${escapeHtml(item.choiceText)}</strong>
                  <span class="badge ${isSafe ? "badge-success" : "badge-danger"}">${item.scoreImpact > 0 ? "+" + item.scoreImpact : item.scoreImpact} pts</span>
                </div>
                <p style="font-size: 0.85rem; color: var(--color-text-muted); margin-top: 0.5rem; line-height: 1.5;">
                  <strong>Rationale:</strong> ${escapeHtml(item.feedbackText)}
                </p>
              </div>
            `;
          }).join("")}
        </div>

        <div style="display: flex; justify-content: center; gap: 1rem; margin-top: 2rem; flex-wrap: wrap;">
          <button type="button" class="btn btn-primary" id="restartDrillBtn">
            Rerun This Simulation &rarr;
          </button>
          <a href="courses.html" class="btn btn-secondary">
            Return to Courses
          </a>
          <a href="dashboard.html" class="btn btn-outline">
            View My Progress Dashboard
          </a>
        </div>
      </div>
    `;

    debriefContainer.querySelector("#restartDrillBtn")?.addEventListener("click", () => {
      resetDrillState();
      window.scrollTo({ top: 300, behavior: "smooth" });
    });

    debriefContainer.scrollIntoView({ behavior: "smooth" });
  }

  function determineRatingLabel(score) {
    if (score >= 80) return "Preparedness Master";
    if (score >= 50) return "Safety Conscious";
    if (score >= 20) return "Developing Awareness";
    return "Needs Practice";
  }

  function convertFallbackToScenario(fallback) {
    const stepsArray = Object.keys(fallback.steps || {}).map((key) => {
      const step = fallback.steps[key];
      return {
        id: key,
        stepKey: key,
        situationText: step.situation,
        questionText: step.question,
        isTerminal: !!step.isFinal,
        choices: (step.choices || []).map((c, i) => ({
          id: c.id || `${key}_c${i}`,
          choiceText: c.text,
          outcomeType: c.outcomeType,
          feedbackText: c.feedback,
          scoreImpact: c.scoreImpact,
          nextStepId: c.nextStepId,
          sequenceOrder: i + 1,
        })),
      };
    });

    return {
      id: fallback.id,
      slug: fallback.id,
      title: fallback.title,
      hazardCategory: fallback.hazard,
      initialStepId: fallback.initialStepId,
      steps: stepsArray,
    };
  }

  function escapeHtml(str) {
    if (str === null || str === undefined) return "";
    const div = document.createElement("div");
    div.textContent = String(str);
    return div.innerHTML;
  }
});
