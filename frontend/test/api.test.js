/**
 * Suraksha - Frontend API Client Automated Unit Tests (Milestone 9)
 *
 * Can be run in Node.js (node frontend/test/api.test.js) or via frontend/test/runner.html.
 * Tests client URL resolution, token storage, bearer-token injection, envelope unwrapping,
 * centralized 401 handling, network error resilience, and REST DTO contracts.
 */

// Setup mock browser globals for Node.js environment
if (typeof globalThis.sessionStorage === "undefined") {
  const store = new Map();
  globalThis.sessionStorage = {
    getItem: (key) => (store.has(key) ? store.get(key) : null),
    setItem: (key, val) => store.set(key, String(val)),
    removeItem: (key) => store.delete(key),
    clear: () => store.clear(),
  };
}

if (typeof globalThis.window === "undefined") {
  const listeners = new Map();
  globalThis.window = {
    sessionStorage: globalThis.sessionStorage,
    dispatchEvent: (event) => {
      const handlers = listeners.get(event.type) || [];
      handlers.forEach((h) => h(event));
      return true;
    },
    addEventListener: (type, handler) => {
      if (!listeners.has(type)) listeners.set(type, []);
      listeners.get(type).push(handler);
    },
  };
}

if (typeof globalThis.CustomEvent === "undefined") {
  globalThis.CustomEvent = class CustomEvent {
    constructor(type, params = {}) {
      this.type = type;
      this.detail = params.detail || null;
    }
  };
}

// Helper to create mock fetch responses
function createMockResponse(data, status = 200, ok = true) {
  return {
    ok,
    status,
    headers: {
      get: (h) => (h.toLowerCase() === "content-type" ? "application/json" : null),
    },
    json: async () => data,
    text: async () => (typeof data === "string" ? data : JSON.stringify(data)),
  };
}

// Load SurakshaApi module
const SurakshaApi = typeof require !== "undefined"
  ? require("../js/api.js")
  : window.SurakshaApi;

// Lightweight Assertion and Runner Framework
let passed = 0;
let failed = 0;
const results = [];

function assert(condition, message) {
  if (!condition) {
    throw new Error(message || "Assertion failed");
  }
}

function assertEquals(actual, expected, message) {
  if (actual !== expected) {
    throw new Error(`${message || "Values differ"}: expected [${expected}], got [${actual}]`);
  }
}

async function test(name, fn) {
  try {
    sessionStorage.clear();
    delete window.SURAKSHA_API_BASE;
    await fn();
    passed++;
    results.push({ name, status: "PASS" });
    console.log(`  [PASS] ${name}`);
  } catch (err) {
    failed++;
    results.push({ name, status: "FAIL", error: err.message });
    console.error(`  [FAIL] ${name}: ${err.message}`);
  }
}

// Global mock fetch helper
let mockFetchHandler = null;
globalThis.fetch = async function (url, options) {
  if (mockFetchHandler) {
    return mockFetchHandler(url, options);
  }
  throw new Error("No mock fetch handler registered");
};

// ==========================================
// TEST SUITE DEFINITIONS
// ==========================================

async function runAllTests() {
  console.log("=== Suraksha Frontend API Client Tests ===");

  await test("Base URL defaults to http://localhost:8080/api", () => {
    assertEquals(SurakshaApi.getBaseUrl(), "http://localhost:8080/api");
  });

  await test("setBaseUrl overrides URL and strips trailing slashes", () => {
    SurakshaApi.setBaseUrl("http://custom-host:9090/api///");
    assertEquals(SurakshaApi.getBaseUrl(), "http://custom-host:9090/api");
  });

  await test("window.SURAKSHA_API_BASE takes precedence over storage", () => {
    SurakshaApi.setBaseUrl("http://storage-url:8080/api");
    window.SURAKSHA_API_BASE = "http://window-override:3000/api/";
    assertEquals(SurakshaApi.getBaseUrl(), "http://window-override:3000/api");
    delete window.SURAKSHA_API_BASE;
  });

  await test("Token management via SurakshaApi and SurakshaApi.auth", () => {
    assertEquals(SurakshaApi.getToken(), null);
    assertEquals(SurakshaApi.isAuthenticated(), false);
    assertEquals(SurakshaApi.auth.isAuthenticated(), false);

    SurakshaApi.setToken("mock.jwt.token.value");
    assertEquals(SurakshaApi.getToken(), "mock.jwt.token.value");
    assertEquals(SurakshaApi.auth.getToken(), "mock.jwt.token.value");
    assertEquals(SurakshaApi.isAuthenticated(), true);

    SurakshaApi.clearAuth();
    assertEquals(SurakshaApi.getToken(), null);
    assertEquals(SurakshaApi.isAuthenticated(), false);
  });

  await test("User management: setUser stores JSON and getUser parses correctly", () => {
    assertEquals(SurakshaApi.getUser(), null);

    const testUser = { id: 42, fullName: "Dr. APJ Abdul Kalam", email: "kalam@suraksha.org", roles: ["ROLE_STUDENT"] };
    SurakshaApi.setUser(testUser);

    const retrieved = SurakshaApi.getUser();
    assert(retrieved !== null, "User should be retrieved");
    assertEquals(retrieved.id, 42);
    assertEquals(retrieved.email, "kalam@suraksha.org");
    assertEquals(retrieved.roles[0], "ROLE_STUDENT");

    SurakshaApi.setUser(null);
    assertEquals(SurakshaApi.getUser(), null);
  });

  await test("getUser handles corrupted JSON safely without throwing", () => {
    sessionStorage.setItem("suraksha_auth_user", "{ corrupted-json ... ");
    assertEquals(SurakshaApi.getUser(), null);
  });

  await test("request() attaches Authorization Bearer header when token is present", async () => {
    SurakshaApi.setToken("my-secret-bearer-jwt");
    let capturedHeaders = null;

    mockFetchHandler = async (url, options) => {
      capturedHeaders = options.headers;
      return createMockResponse({ success: true, data: { courses: [] } });
    };

    await SurakshaApi.request("/courses");
    assert(capturedHeaders !== null, "Headers should be captured");
    assertEquals(capturedHeaders["Authorization"], "Bearer my-secret-bearer-jwt");
  });

  await test("request() does NOT attach Authorization header when token is absent", async () => {
    SurakshaApi.clearAuth();
    let capturedHeaders = null;

    mockFetchHandler = async (url, options) => {
      capturedHeaders = options.headers;
      return createMockResponse({ success: true, data: { courses: [] } });
    };

    await SurakshaApi.request("/courses");
    assert(capturedHeaders !== null, "Headers should be captured");
    assertEquals(capturedHeaders["Authorization"], undefined);
  });

  await test("request() unwrap envelope: returns 'data' from { success: true, data: ... }", async () => {
    mockFetchHandler = async () => createMockResponse({
      success: true,
      message: "Course retrieved",
      data: { id: 10, slug: "flood-safety-101", title: "Flood Safety" },
    });

    const result = await SurakshaApi.request("/courses/flood-safety-101");
    assertEquals(result.id, 10);
    assertEquals(result.slug, "flood-safety-101");
    assertEquals(result.title, "Flood Safety");
  });

  await test("request() throws error on HTTP 400 Bad Request with backend message and details", async () => {
    mockFetchHandler = async () => createMockResponse(
      {
        success: false,
        message: "Validation failed",
        details: ["Full name is required", "Password must be at least 6 characters"],
      },
      400,
      false
    );

    let caughtError = null;
    try {
      await SurakshaApi.request("/auth/register", { method: "POST", body: {} });
    } catch (err) {
      caughtError = err;
    }

    assert(caughtError !== null, "Should have thrown an error on 400");
    assertEquals(caughtError.status, 400);
    assertEquals(caughtError.message, "Validation failed");
    assert(Array.isArray(caughtError.details), "Details should be an array");
    assertEquals(caughtError.details.length, 2);
  });

  await test("request() on 401 Unauthorized clears token and fires auth-changed event", async () => {
    SurakshaApi.setToken("expired-token-xyz");
    let eventFired = false;

    const handler = (evt) => {
      if (evt.detail && evt.detail.authenticated === false) {
        eventFired = true;
      }
    };
    window.addEventListener("suraksha:auth-changed", handler);

    mockFetchHandler = async () => createMockResponse(
      {
        success: false,
        message: "Full authentication is required to access this resource",
      },
      401,
      false
    );

    try {
      await SurakshaApi.request("/auth/me");
    } catch (err) {
      assertEquals(err.status, 401);
    }

    assertEquals(SurakshaApi.getToken(), null, "Token must be cleared on 401");
    assertEquals(eventFired, true, "suraksha:auth-changed event must be fired on 401");
  });

  await test("request() converts network failures into clean error with isNetworkError flag", async () => {
    mockFetchHandler = async () => {
      throw new TypeError("Failed to fetch");
    };

    let caughtError = null;
    try {
      await SurakshaApi.request("/courses");
    } catch (err) {
      caughtError = err;
    }

    assert(caughtError !== null, "Should catch network error");
    assertEquals(caughtError.isNetworkError, true);
    assertEquals(caughtError.status, 0);
    assert(caughtError.message.includes("Unable to reach Suraksha backend"), "Helpful message provided");
  });

  await test("DTO verification: submitQuiz sends { answers: [...] } structure", async () => {
    let capturedBody = null;
    mockFetchHandler = async (url, options) => {
      capturedBody = JSON.parse(options.body);
      return createMockResponse({
        success: true,
        data: { quizId: 1, scorePercent: 100, passed: true },
      });
    };

    await SurakshaApi.quizzes.submit(1, {
      answers: [
        { questionId: 101, selectedOptionId: 501 },
        { questionId: 102, selectedOptionId: 504 },
      ],
    });

    assert(capturedBody !== null, "Body must be captured");
    assert(Array.isArray(capturedBody.answers), "answers field must be an array");
    assertEquals(capturedBody.answers.length, 2);
    assertEquals(capturedBody.answers[0].questionId, 101);
    assertEquals(capturedBody.answers[0].selectedOptionId, 501);
  });

  await test("DTO verification: submitDrill sends { selectedChoiceIds: [...] } structure", async () => {
    let capturedBody = null;
    mockFetchHandler = async (url, options) => {
      capturedBody = JSON.parse(options.body);
      return createMockResponse({
        success: true,
        data: { scenarioId: 2, totalScore: 85, ratingLabel: "Preparedness Master" },
      });
    };

    await SurakshaApi.drills.submit(2, { selectedChoiceIds: [10, 25, 40] });

    assert(capturedBody !== null, "Body must be captured");
    assert(Array.isArray(capturedBody.selectedChoiceIds), "selectedChoiceIds field must be an array");
    assertEquals(capturedBody.selectedChoiceIds.length, 3);
    assertEquals(capturedBody.selectedChoiceIds[1], 25);
  });

  console.log("\n==========================================");
  console.log(`Frontend Test Summary: ${passed} passed, ${failed} failed`);
  console.log("==========================================");

  if (typeof module !== "undefined" && module.exports) {
    if (failed > 0) {
      process.exit(1);
    }
  }

  return { passed, failed, results };
}

// Auto-run if executed directly in Node
if (typeof require !== "undefined" && require.main === module) {
  runAllTests();
}

// Export for browser test runner
if (typeof module !== "undefined" && module.exports) {
  module.exports = { runAllTests };
} else if (typeof window !== "undefined") {
  window.runFrontendTests = runAllTests;
}
