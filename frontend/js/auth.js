/**
 * Suraksha - Frontend Authentication & Registration Controller (Milestone 8)
 *
 * Integrates real Spring Security REST endpoints:
 * - POST /api/auth/register (hashes password with BCrypt, assigns default student role)
 * - POST /api/auth/login (returns signed JWT Bearer token)
 * - GET /api/auth/me (fetches verified user profile)
 *
 * Security Rationale:
 * - Passwords are never persisted in browser storage.
 * - JWT tokens are maintained in sessionStorage for tab-scoped lifespan.
 * - Form submission buttons enter disabled loading states to prevent double submission.
 */

document.addEventListener("DOMContentLoaded", () => {
  initLoginForm();
  initRegisterForm();
  updateAuthNavDisplay();

  // Listen for centralized auth change events (e.g. token expired, login, logout)
  window.addEventListener("suraksha:auth-changed", () => {
    updateAuthNavDisplay();
  });
});

/**
 * Handles Login Page submission with real backend authentication
 */
function initLoginForm() {
  const loginForm = document.getElementById("loginForm");
  if (!loginForm) return;

  const emailInput = document.getElementById("loginEmail");
  const passwordInput = document.getElementById("loginPassword");
  const alertContainer = document.getElementById("loginAlertContainer");
  const submitBtn = loginForm.querySelector("button[type='submit']");

  loginForm.addEventListener("submit", async (e) => {
    e.preventDefault();

    const email = emailInput.value.trim();
    const password = passwordInput.value;

    let hasError = false;

    if (!email || !/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(email)) {
      setFieldError(emailInput, "Please enter a valid email address.");
      hasError = true;
    } else {
      clearFieldError(emailInput);
    }

    if (!password) {
      setFieldError(passwordInput, "Please enter your password.");
      hasError = true;
    } else {
      clearFieldError(passwordInput);
    }

    if (hasError) return;

    // Loading UI state
    setButtonLoading(submitBtn, true, "Signing In...");
    if (alertContainer) alertContainer.innerHTML = "";

    try {
      const response = await SurakshaApi.auth.login({ email, password });

      const userName = (response.user && response.user.fullName) || email.split("@")[0];
      const roles = (response.user && response.user.roles) || ["ROLE_STUDENT"];

      if (alertContainer) {
        alertContainer.innerHTML = `
          <div class="alert alert-success" role="alert" tabindex="-1">
            <svg style="width: 1.25rem; height: 1.25rem; flex-shrink: 0;" viewBox="0 0 20 20" fill="currentColor">
              <path fill-rule="evenodd" d="M10 18a8 8 0 100-16 8 8 0 000 16zm3.707-9.293a1 1 0 00-1.414-1.414L9 10.586 7.707 9.293a1 1 0 00-1.414 1.414l2 2a1 1 0 001.414 0l4-4z" clip-rule="evenodd"/>
            </svg>
            <div>
              <h4 style="font-size: 1rem; margin-bottom: 0.25rem; color: #065f46;">Login Successful</h4>
              <p style="font-size: 0.85rem; margin-bottom: 0.5rem; color: #047857;">
                Welcome back, <strong>${escapeHtml(userName)}</strong>! Authenticated with role <code>${escapeHtml(roles.join(", "))}</code>.
              </p>
              <p style="font-size: 0.75rem; color: #065f46; margin-bottom: 0;">
                Redirecting you to your learning workspace...
              </p>
            </div>
          </div>
        `;
        alertContainer.querySelector(".alert")?.focus();
      }

      // Check redirect URL parameter
      const urlParams = new URLSearchParams(window.location.search);
      const redirectUrl = urlParams.get("redirect") || "courses.html";

      setTimeout(() => {
        window.location.href = redirectUrl;
      }, 1200);

    } catch (err) {
      setButtonLoading(submitBtn, false, "Sign In &rarr;");

      let errorMsg = err.message || "Failed to sign in. Please verify your credentials.";
      if (err.isNetworkError) {
        errorMsg = "Unable to connect to the backend server (http://localhost:8080). Please ensure the Spring Boot application is running.";
      }

      if (alertContainer) {
        alertContainer.innerHTML = `
          <div class="alert alert-danger" role="alert" tabindex="-1">
            <svg style="width: 1.25rem; height: 1.25rem; flex-shrink: 0;" viewBox="0 0 20 20" fill="currentColor">
              <path fill-rule="evenodd" d="M18 10a8 8 0 11-16 0 8 8 0 0116 0zm-7 4a1 1 0 11-2 0 1 1 0 012 0zm-1-9a1 1 0 00-1 1v4a1 1 0 102 0V6a1 1 0 00-1-1z" clip-rule="evenodd"/>
            </svg>
            <div>
              <strong>Sign In Failed:</strong> ${escapeHtml(errorMsg)}
            </div>
          </div>
        `;
        alertContainer.querySelector(".alert")?.focus();
      }
    }
  });
}

/**
 * Handles Registration Page submission with real backend registration
 */
function initRegisterForm() {
  const registerForm = document.getElementById("registerForm");
  if (!registerForm) return;

  const nameInput = document.getElementById("regFullName");
  const emailInput = document.getElementById("regEmail");
  const passwordInput = document.getElementById("regPassword");
  const confirmPasswordInput = document.getElementById("regConfirmPassword");
  const alertContainer = document.getElementById("registerAlertContainer");
  const submitBtn = registerForm.querySelector("button[type='submit']");

  registerForm.addEventListener("submit", async (e) => {
    e.preventDefault();

    let hasError = false;

    // Validate Name
    if (!nameInput.value.trim() || nameInput.value.trim().length < 2) {
      setFieldError(nameInput, "Full name must be at least 2 characters.");
      hasError = true;
    } else {
      clearFieldError(nameInput);
    }

    // Validate Email
    if (!emailInput.value.trim() || !/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(emailInput.value.trim())) {
      setFieldError(emailInput, "Please enter a valid email address.");
      hasError = true;
    } else {
      clearFieldError(emailInput);
    }

    // Validate Password
    if (!passwordInput.value || passwordInput.value.length < 8) {
      setFieldError(passwordInput, "Password must be at least 8 characters long.");
      hasError = true;
    } else {
      clearFieldError(passwordInput);
    }

    // Validate Confirm Password
    if (confirmPasswordInput.value !== passwordInput.value) {
      setFieldError(confirmPasswordInput, "Passwords do not match.");
      hasError = true;
    } else {
      clearFieldError(confirmPasswordInput);
    }

    if (hasError) return;

    // Loading UI state
    setButtonLoading(submitBtn, true, "Registering Account...");
    if (alertContainer) alertContainer.innerHTML = "";

    const fullName = nameInput.value.trim();
    const email = emailInput.value.trim().toLowerCase();
    const password = passwordInput.value;

    try {
      const response = await SurakshaApi.auth.register({ fullName, email, password });

      if (alertContainer) {
        alertContainer.innerHTML = `
          <div class="alert alert-success" role="alert" tabindex="-1">
            <svg style="width: 1.25rem; height: 1.25rem; flex-shrink: 0;" viewBox="0 0 20 20" fill="currentColor">
              <path fill-rule="evenodd" d="M10 18a8 8 0 100-16 8 8 0 000 16zm3.707-9.293a1 1 0 00-1.414-1.414L9 10.586 7.707 9.293a1 1 0 00-1.414 1.414l2 2a1 1 0 001.414 0l4-4z" clip-rule="evenodd"/>
            </svg>
            <div>
              <h4 style="font-size: 1rem; margin-bottom: 0.25rem; color: #065f46;">Account Registered Successfully!</h4>
              <p style="font-size: 0.85rem; margin-bottom: 0.5rem; color: #047857;">
                Welcome, <strong>${escapeHtml(fullName)}</strong>! Your student account is now active with secure BCrypt hashing.
              </p>
              <p style="font-size: 0.75rem; color: #065f46; margin-bottom: 0;">
                Redirecting you to the course catalog...
              </p>
            </div>
          </div>
        `;
        alertContainer.querySelector(".alert")?.focus();
      }

      setTimeout(() => {
        window.location.href = "courses.html";
      }, 1500);

    } catch (err) {
      setButtonLoading(submitBtn, false, "Register Account &rarr;");

      let errorMsg = err.message || "Registration failed. Please review your details.";
      if (err.status === 409) {
        errorMsg = "An account with this email address already exists. Please sign in instead.";
      } else if (err.isNetworkError) {
        errorMsg = "Unable to connect to the backend server (http://localhost:8080). Please ensure the Spring Boot application is running.";
      }

      if (alertContainer) {
        alertContainer.innerHTML = `
          <div class="alert alert-danger" role="alert" tabindex="-1">
            <svg style="width: 1.25rem; height: 1.25rem; flex-shrink: 0;" viewBox="0 0 20 20" fill="currentColor">
              <path fill-rule="evenodd" d="M18 10a8 8 0 11-16 0 8 8 0 0116 0zm-7 4a1 1 0 11-2 0 1 1 0 012 0zm-1-9a1 1 0 00-1 1v4a1 1 0 102 0V6a1 1 0 00-1-1z" clip-rule="evenodd"/>
            </svg>
            <div>
              <strong>Registration Error:</strong> ${escapeHtml(errorMsg)}
            </div>
          </div>
        `;
        alertContainer.querySelector(".alert")?.focus();
      }
    }
  });
}

function setFieldError(inputEl, message) {
  inputEl.classList.add("is-invalid");
  inputEl.classList.remove("is-valid");
  const feedbackEl = inputEl.parentElement.querySelector(".form-feedback");
  if (feedbackEl) {
    feedbackEl.textContent = message;
    feedbackEl.classList.add("is-visible");
  }
}

function clearFieldError(inputEl) {
  inputEl.classList.remove("is-invalid");
  inputEl.classList.add("is-valid");
  const feedbackEl = inputEl.parentElement.querySelector(".form-feedback");
  if (feedbackEl) {
    feedbackEl.textContent = "";
    feedbackEl.classList.remove("is-visible");
  }
}

function setButtonLoading(button, isLoading, text) {
  if (!button) return;
  button.disabled = isLoading;
  button.innerHTML = text;
}

/**
 * Checks authentication state and renders responsive user controls in the navbar
 */
async function updateAuthNavDisplay() {
  const navActionsContainers = document.querySelectorAll(".nav-actions, .nav-actions-mobile");
  if (!navActionsContainers.length) return;

  const isAuth = typeof SurakshaApi !== "undefined" && SurakshaApi.auth.isAuthenticated();

  if (!isAuth) {
    navActionsContainers.forEach((container) => {
      container.innerHTML = `
        <a href="login.html" class="btn btn-secondary btn-sm">Log In</a>
        <a href="register.html" class="btn btn-primary btn-sm">Get Started</a>
      `;
    });
    return;
  }

  let user = SurakshaApi.auth.getUser();
  if (!user) {
    try {
      user = await SurakshaApi.auth.getMe();
    } catch {
      // If fetching profile fails, user remains null
    }
  }

  const displayName = (user && user.fullName) || (user && user.email) || "Learner";

  navActionsContainers.forEach((container) => {
    container.innerHTML = `
      <div style="display: flex; align-items: center; gap: 0.75rem; flex-wrap: wrap;">
        <a href="dashboard.html" class="btn btn-secondary btn-sm" title="View your learning dashboard">
          📊 Dashboard
        </a>
        <span style="font-size: 0.85rem; font-weight: 600; color: var(--color-primary);" title="Logged in as ${escapeHtml(displayName)}">
          👤 ${escapeHtml(displayName)}
        </span>
        <button type="button" class="btn btn-outline btn-sm logout-action-btn">Log Out</button>
      </div>
    `;

    container.querySelector(".logout-action-btn")?.addEventListener("click", () => {
      SurakshaApi.auth.logout();
      window.location.href = "index.html";
    });
  });
}

function escapeHtml(str) {
  if (str === null || str === undefined) return "";
  const div = document.createElement("div");
  div.textContent = String(str);
  return div.innerHTML;
}
