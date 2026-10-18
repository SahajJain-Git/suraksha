/**
 * Suraksha - Contact Form Validation & REST Handler (Milestone 8)
 *
 * Connects frontend inquiry form to Spring Boot REST API:
 * POST /api/contact
 *
 * Features:
 * - Client-side validation matching backend Jakarta bean constraints
 * - Loading spinner & button disabled states preventing duplicate submissions
 * - Real database persistence in MySQL contact_messages table
 * - Accessible error and confirmation alerts
 */

document.addEventListener("DOMContentLoaded", () => {
  const contactForm = document.getElementById("contactForm");
  if (!contactForm) return;

  const formFields = {
    fullName: {
      element: document.getElementById("fullName"),
      errorElement: document.getElementById("fullNameError"),
      validate: (value) => {
        if (!value.trim()) return "Full name is required.";
        if (value.trim().length < 2) return "Name must be at least 2 characters.";
        if (value.trim().length > 100) return "Name cannot exceed 100 characters.";
        return "";
      },
    },
    email: {
      element: document.getElementById("email"),
      errorElement: document.getElementById("emailError"),
      validate: (value) => {
        if (!value.trim()) return "Email address is required.";
        const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
        if (!emailRegex.test(value.trim())) return "Please enter a valid email address (e.g., name@example.com).";
        return "";
      },
    },
    category: {
      element: document.getElementById("category"),
      errorElement: document.getElementById("categoryError"),
      validate: (value) => {
        if (!value || value === "") return "Please select a message category.";
        return "";
      },
    },
    subject: {
      element: document.getElementById("subject"),
      errorElement: document.getElementById("subjectError"),
      validate: (value) => {
        if (!value.trim()) return "Subject line is required.";
        if (value.trim().length < 5) return "Subject must be at least 5 characters.";
        if (value.trim().length > 200) return "Subject cannot exceed 200 characters.";
        return "";
      },
    },
    message: {
      element: document.getElementById("message"),
      errorElement: document.getElementById("messageError"),
      validate: (value) => {
        if (!value.trim()) return "Message body cannot be empty.";
        if (value.trim().length < 15) return "Message must be at least 15 characters long to provide helpful detail.";
        return "";
      },
    },
  };

  const submitBtn = contactForm.querySelector("button[type='submit']");
  const alertContainer = document.getElementById("formAlertContainer");

  // Attach live validation on blur and input events
  Object.values(formFields).forEach(({ element, errorElement, validate }) => {
    if (!element) return;

    element.addEventListener("blur", () => {
      validateSingleField(element, errorElement, validate);
    });

    element.addEventListener("input", () => {
      if (element.classList.contains("is-invalid")) {
        validateSingleField(element, errorElement, validate);
      }
    });
  });

  // Handle Form Submission
  contactForm.addEventListener("submit", async (event) => {
    event.preventDefault();

    let isFormValid = true;
    let firstInvalidField = null;

    // Validate all fields
    Object.values(formFields).forEach(({ element, errorElement, validate }) => {
      const isValid = validateSingleField(element, errorElement, validate);
      if (!isValid) {
        isFormValid = false;
        if (!firstInvalidField) {
          firstInvalidField = element;
        }
      }
    });

    if (!isFormValid) {
      if (alertContainer) {
        alertContainer.innerHTML = `
          <div class="alert alert-danger" role="alert" tabindex="-1">
            <svg style="width: 1.25rem; height: 1.25rem; flex-shrink: 0;" viewBox="0 0 20 20" fill="currentColor">
              <path fill-rule="evenodd" d="M18 10a8 8 0 11-16 0 8 8 0 0116 0zm-7 4a1 1 0 11-2 0 1 1 0 012 0zm-1-9a1 1 0 00-1 1v4a1 1 0 102 0V6a1 1 0 00-1-1z" clip-rule="evenodd"/>
            </svg>
            <div>
              <strong>Please check the form:</strong> One or more fields require correction before submitting.
            </div>
          </div>
        `;
        alertContainer.querySelector(".alert")?.focus();
      }

      if (firstInvalidField) {
        firstInvalidField.focus();
      }
      return;
    }

    // Submit to Backend REST API
    setButtonLoading(submitBtn, true, "Submitting Inquiry...");
    if (alertContainer) alertContainer.innerHTML = "";

    const payload = {
      fullName: formFields.fullName.element.value.trim(),
      email: formFields.email.element.value.trim().toLowerCase(),
      category: formFields.category.element.value,
      subject: formFields.subject.element.value.trim(),
      message: formFields.message.element.value.trim(),
    };

    try {
      const response = await SurakshaApi.contact.submit(payload);

      setButtonLoading(submitBtn, false, "Send Message &rarr;");

      const senderName = (response && response.fullName) || payload.fullName;
      const refId = (response && response.id) ? `#${response.id}` : "";

      if (alertContainer) {
        alertContainer.innerHTML = `
          <div class="alert alert-success" role="alert" tabindex="-1">
            <svg style="width: 1.5rem; height: 1.5rem; flex-shrink: 0;" viewBox="0 0 20 20" fill="currentColor">
              <path fill-rule="evenodd" d="M10 18a8 8 0 100-16 8 8 0 000 16zm3.707-9.293a1 1 0 00-1.414-1.414L9 10.586 7.707 9.293a1 1 0 00-1.414 1.414l2 2a1 1 0 001.414 0l4-4z" clip-rule="evenodd"/>
            </svg>
            <div>
              <h4 style="font-size: 1rem; margin-bottom: 0.25rem; color: #065f46;">Inquiry Sent Successfully! ${refId}</h4>
              <p style="font-size: 0.875rem; margin-bottom: 0.5rem; color: #047857;">
                Thank you, <strong>${escapeHtml(senderName)}</strong>! Your message has been safely recorded in the Suraksha safety database. Our safety coordinators will review it promptly.
              </p>
              <p style="font-size: 0.75rem; color: #065f46; margin-bottom: 0;">
                Emergency reminder: In an immediate life-threatening crisis, call <strong>112</strong> or <strong>101</strong> directly.
              </p>
            </div>
          </div>
        `;
        alertContainer.querySelector(".alert")?.focus();
      }

      // Reset form and visual field states
      contactForm.reset();
      Object.values(formFields).forEach(({ element }) => {
        element.classList.remove("is-valid", "is-invalid");
      });

    } catch (err) {
      setButtonLoading(submitBtn, false, "Send Message &rarr;");

      let errorMsg = err.message || "Failed to submit inquiry.";
      if (err.isNetworkError) {
        errorMsg = "Unable to connect to the backend server (http://localhost:8080). Please ensure the Spring Boot server is running.";
      }

      if (alertContainer) {
        alertContainer.innerHTML = `
          <div class="alert alert-danger" role="alert" tabindex="-1">
            <svg style="width: 1.25rem; height: 1.25rem; flex-shrink: 0;" viewBox="0 0 20 20" fill="currentColor">
              <path fill-rule="evenodd" d="M18 10a8 8 0 11-16 0 8 8 0 0116 0zm-7 4a1 1 0 11-2 0 1 1 0 012 0zm-1-9a1 1 0 00-1 1v4a1 1 0 102 0V6a1 1 0 00-1-1z" clip-rule="evenodd"/>
            </svg>
            <div>
              <strong>Submission Error:</strong> ${escapeHtml(errorMsg)}
            </div>
          </div>
        `;
        alertContainer.querySelector(".alert")?.focus();
      }
    }
  });

  function validateSingleField(element, errorElement, validate) {
    const errorMessage = validate(element.value);

    if (errorMessage) {
      element.classList.add("is-invalid");
      element.classList.remove("is-valid");
      element.setAttribute("aria-invalid", "true");
      if (errorElement) {
        errorElement.textContent = errorMessage;
        errorElement.classList.add("is-visible");
      }
      return false;
    } else {
      element.classList.remove("is-invalid");
      element.classList.add("is-valid");
      element.setAttribute("aria-invalid", "false");
      if (errorElement) {
        errorElement.textContent = "";
        errorElement.classList.remove("is-visible");
      }
      return true;
    }
  }

  function setButtonLoading(button, isLoading, text) {
    if (!button) return;
    button.disabled = isLoading;
    button.innerHTML = text;
  }

  function escapeHtml(str) {
    if (str === null || str === undefined) return "";
    const div = document.createElement("div");
    div.textContent = String(str);
    return div.innerHTML;
  }
});
