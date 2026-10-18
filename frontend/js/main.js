/**
 * Suraksha - Main Shared JavaScript
 * Handles responsive mobile navigation, accessibility attributes, active link states,
 * and emergency banner behavior across all pages.
 */

document.addEventListener("DOMContentLoaded", () => {
  initMobileNavigation();
  initEmergencyBanner();
  highlightActiveNavLink();
});

/**
 * Initializes the accessible mobile navigation toggle
 */
function initMobileNavigation() {
  const toggleBtn = document.querySelector(".nav-toggle");
  const navMenu = document.querySelector(".nav-menu");

  if (!toggleBtn || !navMenu) return;

  toggleBtn.addEventListener("click", () => {
    const isExpanded = toggleBtn.getAttribute("aria-expanded") === "true";
    toggleBtn.setAttribute("aria-expanded", String(!isExpanded));
    navMenu.classList.toggle("is-open");
  });

  // Close navigation menu if user presses Escape key
  document.addEventListener("keydown", (event) => {
    if (event.key === "Escape" && navMenu.classList.contains("is-open")) {
      navMenu.classList.remove("is-open");
      toggleBtn.setAttribute("aria-expanded", "false");
      toggleBtn.focus();
    }
  });

  // Close navigation menu if user clicks outside of header/menu
  document.addEventListener("click", (event) => {
    if (
      navMenu.classList.contains("is-open") &&
      !navMenu.contains(event.target) &&
      !toggleBtn.contains(event.target)
    ) {
      navMenu.classList.remove("is-open");
      toggleBtn.setAttribute("aria-expanded", "false");
    }
  });
}

/**
 * Handles the emergency top banner dismissal for the current session
 */
function initEmergencyBanner() {
  const banner = document.querySelector(".emergency-banner");
  const dismissBtn = document.querySelector(".banner-dismiss-btn");

  if (!banner || !dismissBtn) return;

  // Check if user previously dismissed banner during this session
  if (sessionStorage.getItem("suraksha_banner_dismissed") === "true") {
    banner.style.display = "none";
  }

  dismissBtn.addEventListener("click", () => {
    banner.style.display = "none";
    sessionStorage.setItem("suraksha_banner_dismissed", "true");
  });
}

/**
 * Automatically sets the .active class on the navigation link matching current URL
 */
function highlightActiveNavLink() {
  const currentPath = window.location.pathname.split("/").pop() || "index.html";
  const navLinks = document.querySelectorAll(".nav-menu .nav-link");

  navLinks.forEach((link) => {
    const href = link.getAttribute("href");
    if (href === currentPath || (currentPath === "" && href === "index.html")) {
      link.classList.add("active");
      link.setAttribute("aria-current", "page");
    } else {
      link.classList.remove("active");
      link.removeAttribute("aria-current");
    }
  });
}
