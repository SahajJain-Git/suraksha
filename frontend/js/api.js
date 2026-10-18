/**
 * Suraksha - Centralized API Client (Milestone 8)
 *
 * Provides a unified, resilient fetch layer connecting vanilla HTML/JS pages
 * to the Spring Boot REST backend at http://localhost:8080/api.
 *
 * Security & Storage Rationale:
 * - Uses sessionStorage for JWT storage to limit token lifetime to the browser session.
 * - Never stores plaintext passwords.
 * - Explains XSS risk: sessionStorage is accessible to any script executing on this origin.
 *   XSS protection is enforced by escaping dynamic HTML and avoiding innerHTML on untrusted inputs.
 * - Automatically detects 401 Unauthorized responses to clear expired credentials and prompt re-login.
 * - Provides graceful network error handling when the backend server is unavailable.
 */

(function (root, factory) {
  if (typeof module === "object" && module.exports) {
    module.exports = factory();
  } else {
    root.SurakshaApi = factory();
  }
})(typeof self !== "undefined" ? self : this, function () {
  "use strict";

  const STORAGE_KEYS = {
    TOKEN: "suraksha_jwt_token",
    USER: "suraksha_auth_user",
    API_BASE: "suraksha_api_base_url",
  };

  const DEFAULT_BASE_URL = "http://localhost:8080/api";

  function getBaseUrl() {
    if (typeof window !== "undefined" && window.SURAKSHA_API_BASE) {
      return window.SURAKSHA_API_BASE.replace(/\/+$/, "");
    }
    if (typeof sessionStorage !== "undefined") {
      const stored = sessionStorage.getItem(STORAGE_KEYS.API_BASE);
      if (stored) return stored.replace(/\/+$/, "");
    }
    return DEFAULT_BASE_URL;
  }

  function setBaseUrl(url) {
    if (!url) return;
    const cleanUrl = url.replace(/\/+$/, "");
    if (typeof sessionStorage !== "undefined") {
      sessionStorage.setItem(STORAGE_KEYS.API_BASE, cleanUrl);
    }
  }

  // --- Auth Token Management (sessionStorage) ---

  function getToken() {
    if (typeof sessionStorage === "undefined") return null;
    return sessionStorage.getItem(STORAGE_KEYS.TOKEN);
  }

  function setToken(token) {
    if (typeof sessionStorage === "undefined") return;
    if (token) {
      sessionStorage.setItem(STORAGE_KEYS.TOKEN, token);
    } else {
      sessionStorage.removeItem(STORAGE_KEYS.TOKEN);
    }
  }

  function getUser() {
    if (typeof sessionStorage === "undefined") return null;
    const userJson = sessionStorage.getItem(STORAGE_KEYS.USER);
    if (!userJson) return null;
    try {
      return JSON.parse(userJson);
    } catch {
      return null;
    }
  }

  function setUser(user) {
    if (typeof sessionStorage === "undefined") return;
    if (user) {
      sessionStorage.setItem(STORAGE_KEYS.USER, JSON.stringify(user));
    } else {
      sessionStorage.removeItem(STORAGE_KEYS.USER);
    }
  }

  function clearAuth() {
    if (typeof sessionStorage === "undefined") return;
    sessionStorage.removeItem(STORAGE_KEYS.TOKEN);
    sessionStorage.removeItem(STORAGE_KEYS.USER);
    // Also clean up any legacy demo keys
    sessionStorage.removeItem("suraksha_demo_user");
  }

  function isAuthenticated() {
    return !!getToken();
  }

  function handleUnauthorized() {
    clearAuth();
    if (typeof window !== "undefined") {
      window.dispatchEvent(new CustomEvent("suraksha:auth-changed", { detail: { authenticated: false } }));
    }
  }

  // --- Core Fetch Dispatcher ---

  async function request(endpoint, options = {}) {
    const baseUrl = getBaseUrl();
    const url = `${baseUrl}${endpoint.startsWith("/") ? endpoint : "/" + endpoint}`;

    const headers = {
      Accept: "application/json",
      ...options.headers,
    };

    // Attach Bearer JWT if available
    const token = getToken();
    if (token && !headers["Authorization"]) {
      headers["Authorization"] = `Bearer ${token}`;
    }

    // Set JSON Content-Type if payload provided and not FormData
    let body = options.body;
    if (body && typeof body === "object" && !(body instanceof FormData)) {
      headers["Content-Type"] = "application/json";
      body = JSON.stringify(body);
    }

    const fetchConfig = {
      ...options,
      headers,
      body,
    };

    let response;
    try {
      response = await fetch(url, fetchConfig);
    } catch (networkError) {
      const err = new Error(
        `Unable to reach Suraksha backend service at ${baseUrl}. ` +
        `Please ensure the Spring Boot server is running on port 8080.`
      );
      err.isNetworkError = true;
      err.status = 0;
      err.originalError = networkError;
      throw err;
    }

    // Attempt to parse JSON response
    let responseData = null;
    const contentType = response.headers.get("content-type") || "";
    if (contentType.includes("application/json")) {
      try {
        responseData = await response.json();
      } catch (jsonErr) {
        responseData = null;
      }
    } else {
      try {
        const text = await response.text();
        responseData = { message: text };
      } catch {
        responseData = null;
      }
    }

    // Check for HTTP errors
    if (!response.ok) {
      if (response.status === 401) {
        handleUnauthorized();
      }

      const errorMessage =
        (responseData && responseData.message) ||
        (responseData && responseData.error) ||
        `HTTP Error ${response.status}: ${response.statusText}`;

      const apiError = new Error(errorMessage);
      apiError.status = response.status;
      apiError.details = (responseData && responseData.details) || [];
      apiError.raw = responseData;
      throw apiError;
    }

    // Unwrap ApiResponse<T>
    if (responseData && typeof responseData === "object" && "success" in responseData) {
      if (responseData.success) {
        return responseData.data;
      } else {
        const customErr = new Error(responseData.message || "Operation failed.");
        customErr.status = response.status;
        throw customErr;
      }
    }

    return responseData;
  }

  // --- API Domain Services ---

  const auth = {
    getToken,
    setToken,
    getUser,
    setUser,
    isAuthenticated,
    clearAuth,

    async register({ fullName, email, password }) {
      const result = await request("/auth/register", {
        method: "POST",
        body: { fullName, email, password },
      });
      if (result && result.token) {
        setToken(result.token);
        if (result.user) setUser(result.user);
        if (typeof window !== "undefined") {
          window.dispatchEvent(new CustomEvent("suraksha:auth-changed", { detail: { authenticated: true, user: result.user } }));
        }
      }
      return result;
    },

    async login({ email, password }) {
      const result = await request("/auth/login", {
        method: "POST",
        body: { email, password },
      });
      if (result && result.token) {
        setToken(result.token);
        if (result.user) setUser(result.user);
        if (typeof window !== "undefined") {
          window.dispatchEvent(new CustomEvent("suraksha:auth-changed", { detail: { authenticated: true, user: result.user } }));
        }
      }
      return result;
    },

    async getMe() {
      const profile = await request("/auth/me", { method: "GET" });
      if (profile) setUser(profile);
      return profile;
    },

    logout() {
      clearAuth();
      if (typeof window !== "undefined") {
        window.dispatchEvent(new CustomEvent("suraksha:auth-changed", { detail: { authenticated: false } }));
      }
    },
  };

  const courses = {
    async getAll({ category, search } = {}) {
      const params = new URLSearchParams();
      if (category && category !== "ALL") params.append("category", category);
      if (search && search.trim()) params.append("search", search.trim());
      const query = params.toString() ? `?${params.toString()}` : "";
      return request(`/courses${query}`, { method: "GET" });
    },

    async getBySlug(slug) {
      return request(`/courses/${encodeURIComponent(slug)}`, { method: "GET" });
    },
  };

  const quizzes = {
    async getById(idOrSlug) {
      return request(`/quizzes/${encodeURIComponent(idOrSlug)}`, { method: "GET" });
    },

    async submit(idOrSlug, { answers }) {
      return request(`/quizzes/${encodeURIComponent(idOrSlug)}/submit`, {
        method: "POST",
        body: { answers },
      });
    },
  };

  const drills = {
    async getBySlug(slug) {
      return request(`/drills/${encodeURIComponent(slug)}`, { method: "GET" });
    },

    async submit(idOrSlug, { selectedChoiceIds }) {
      return request(`/drills/${encodeURIComponent(idOrSlug)}/submit`, {
        method: "POST",
        body: { selectedChoiceIds },
      });
    },
  };

  const contact = {
    async submit({ fullName, email, category, subject, message }) {
      return request("/contact", {
        method: "POST",
        body: { fullName, email, category, subject, message },
      });
    },
  };

  const progress = {
    async markLessonCompleted(lessonId) {
      return request(`/progress/lessons/${encodeURIComponent(lessonId)}`, {
        method: "POST",
      });
    },

    async getUserProgress() {
      return request("/progress", { method: "GET" });
    },
  };

  const health = {
    async check() {
      return request("/health", { method: "GET" });
    },
  };

  return {
    getBaseUrl,
    setBaseUrl,
    getToken,
    setToken,
    getUser,
    setUser,
    isAuthenticated,
    clearAuth,
    request,
    auth,
    courses,
    quizzes,
    drills,
    contact,
    progress,
    health,
  };
});
