document.addEventListener("DOMContentLoaded", () => {
    const toggles = Array.from(document.querySelectorAll(".business-visibility-toggle"));
    const notificationModalElement = document.getElementById("visibilityNotificationModal");
    const notificationTitle = document.getElementById("visibilityNotificationTitle");
    const notificationMessage = document.getElementById("visibilityNotificationMessage");
    const notificationIcon = document.getElementById("visibilityNotificationIcon");
    const csrfToken = document.querySelector('meta[name="_csrf"]')?.content;
    const csrfHeader = document.querySelector('meta[name="_csrf_header"]')?.content;

    const setUiState = (uuid, active) => {
        const toggle = document.getElementById(`visibility-toggle-${uuid}`);
        const label = document.getElementById(`visibility-label-${uuid}`);
        const badge = document.getElementById(`visibility-badge-${uuid}`);

        if (toggle) toggle.checked = active;
        if (label) label.textContent = active ? "Public" : "Inactiv";
        if (badge) {
            badge.textContent = active ? "Vizibil public" : "Inactiv";
            badge.classList.toggle("bg-success-subtle", active);
            badge.classList.toggle("text-success", active);
            badge.classList.toggle("bg-danger-subtle", !active);
            badge.classList.toggle("text-dark", !active);
        }
    };

    const showFeedback = (message, success) => {
        if (!notificationModalElement || !window.bootstrap) return;

        notificationTitle.textContent = success
            ? "Vizibilitate actualizată"
            : "Actualizarea a eșuat";
        notificationMessage.textContent = message;
        notificationIcon.className = success
            ? "bi bi-check-circle-fill text-success d-block mb-3"
            : "bi bi-exclamation-triangle-fill text-danger d-block mb-3";

        bootstrap.Modal.getOrCreateInstance(notificationModalElement).show();
    };

    const setInteractive = (interactive) => {
        toggles.forEach(toggle => {
            if (toggle.dataset.permanentlyDisabled !== "true") {
                toggle.disabled = !interactive;
            }
        });
    };

    toggles.forEach(toggle => {
        if (toggle.disabled) toggle.dataset.permanentlyDisabled = "true";

        toggle.addEventListener("change", async () => {
            const uuid = toggle.dataset.uuid;
            const requestedActive = toggle.checked;
            setInteractive(false);

            try {
                const headers = {
                    "Accept": "application/json",
                    "Content-Type": "application/json",
                    "X-Requested-With": "XMLHttpRequest"
                };
                if (csrfHeader && csrfToken) headers[csrfHeader] = csrfToken;

                const response = await fetch(toggle.dataset.url, {
                    method: "POST",
                    headers,
                    body: JSON.stringify({active: requestedActive})
                });
                const payload = await response.json().catch(() => ({}));

                if (!response.ok || !payload.success) {
                    throw new Error(payload.message || "Vizibilitatea nu a putut fi actualizată.");
                }

                setUiState(uuid, payload.active);
                (payload.deactivatedUuids || []).forEach(otherUuid => setUiState(otherUuid, false));
                showFeedback(payload.message, true);
            } catch (error) {
                setUiState(uuid, !requestedActive);
                showFeedback(error.message || "Vizibilitatea nu a putut fi actualizată.", false);
            } finally {
                setInteractive(true);
            }
        });
    });
});
