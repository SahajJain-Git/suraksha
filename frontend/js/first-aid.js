/**
 * Suraksha - First-Aid Learning Center
 * Protocol tab switcher and verified step-by-step emergency care guides.
 */

document.addEventListener("DOMContentLoaded", () => {
  const tabsContainer = document.getElementById("firstAidTabs");
  const protocolContent = document.getElementById("protocolContent");

  if (!tabsContainer || !protocolContent) return;

  let activeTopicId = "cpr";

  renderTabs();
  renderActiveTopic();

  function renderTabs() {
    tabsContainer.innerHTML = SAMPLE_FIRST_AID.map((topic) => {
      const isActive = topic.id === activeTopicId;
      return `
        <button
          type="button"
          class="first-aid-tab-btn ${isActive ? "active" : ""}"
          data-topic-id="${topic.id}"
          role="tab"
          aria-selected="${isActive}"
        >
          ${escapeHtml(topic.title)}
        </button>
      `;
    }).join("");

    tabsContainer.querySelectorAll(".first-aid-tab-btn").forEach((btn) => {
      btn.addEventListener("click", () => {
        activeTopicId = btn.getAttribute("data-topic-id");
        renderTabs();
        renderActiveTopic();
      });
    });
  }

  function renderActiveTopic() {
    const topic = SAMPLE_FIRST_AID.find((t) => t.id === activeTopicId) || SAMPLE_FIRST_AID[0];
    if (!topic) return;

    protocolContent.innerHTML = `
      <div style="margin-bottom: 2rem;">
        <div style="display: flex; gap: 0.75rem; align-items: center; margin-bottom: 0.5rem;">
          <span class="badge badge-danger">${escapeHtml(topic.category)}</span>
          <span style="font-size: 0.85rem; color: var(--color-text-muted);">Verified Emergency Guide</span>
        </div>
        <h2 style="font-size: 1.75rem; margin-bottom: 0.5rem;">${escapeHtml(topic.title)}</h2>
        <p style="font-size: 1.05rem; color: var(--color-text-muted);">${escapeHtml(topic.summary)}</p>
      </div>

      <!-- Critical Topic Specific Warning -->
      <div class="callout callout-danger" style="margin-bottom: 2rem;">
        <div class="callout-icon" aria-hidden="true">
          <svg viewBox="0 0 20 20" fill="currentColor">
            <path fill-rule="evenodd" d="M8.257 3.099c.765-1.36 2.722-1.36 3.486 0l5.58 9.92c.75 1.334-.213 2.98-1.742 2.98H4.42c-1.53 0-2.493-1.646-1.743-2.98l5.58-9.92zM11 13a1 1 0 11-2 0 1 1 0 012 0zm-1-8a1 1 0 00-1 1v3a1 1 0 002 0V6a1 1 0 00-1-1z" clip-rule="evenodd" />
          </svg>
        </div>
        <div>
          <div class="callout-title">Emergency Protocol Boundary</div>
          <div class="callout-body">${escapeHtml(topic.disclaimer)}</div>
        </div>
      </div>

      <!-- Step by Step Procedures -->
      <h3 style="font-size: 1.25rem; margin-bottom: 1.25rem;">Standard Action Steps:</h3>
      <div style="display: flex; flex-direction: column; gap: 1rem; margin-bottom: 2rem;">
        ${topic.steps
          .map(
            (step) => `
          <div class="protocol-step-card">
            <div class="step-number-badge">${step.num}</div>
            <div>
              <h4 style="font-size: 1.05rem; margin-bottom: 0.35rem; color: var(--color-primary);">${escapeHtml(step.title)}</h4>
              <div style="font-size: 0.95rem; line-height: 1.6; color: var(--color-text-muted);">${step.detail}</div>
            </div>
          </div>
        `
          )
          .join("")}
      </div>

      <!-- Emergency Hotline Reminder Callout -->
      <div class="callout callout-warning">
        <div class="callout-body" style="font-size: 0.85rem;">
          🚑 <strong>Remember:</strong> In any situation where the victim is unresponsive, struggling to breathe, or bleeding profusely, instruct a bystander to <strong>dial 112</strong> immediately while you perform these first-aid steps.
        </div>
      </div>
    `;
  }

  function escapeHtml(str) {
    const div = document.createElement("div");
    div.textContent = str;
    return div.innerHTML;
  }
});
