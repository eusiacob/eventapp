document.addEventListener("DOMContentLoaded", () => {
    const card = document.querySelector(".business-editor-card");
    if (!card) return;

    function reveal(target) {
        for (let parent = target.parentElement; parent; parent = parent.parentElement) {
            if (parent.tagName === "DETAILS") parent.open = true;
        }
    }

    function focusError(target) {
        reveal(target);
        if (!target.matches("input, select, textarea, button, a[href]")) {
            target.setAttribute("tabindex", "-1");
        }
        target.focus({preventScroll: true});
        target.scrollIntoView({
            behavior: window.matchMedia("(prefers-reduced-motion: reduce)").matches ? "auto" : "smooth",
            block: "center"
        });
    }

    // Server-side errors are rendered after submitting the form.
    const firstError = [...card.querySelectorAll(".alert-danger, .business-editor-form .text-danger")]
        .find(element => element.textContent.trim() && !element.closest("[hidden], .d-none"));
    if (firstError) {
        reveal(firstError);
        requestAnimationFrame(() => focusError(firstError));
    }

    // Native validation can report several invalid controls during one submission.
    // Reveal them immediately, but scroll/focus only the first one.
    let firstInvalid = null;
    card.addEventListener("invalid", event => {
        reveal(event.target);
        if (firstInvalid) return;
        firstInvalid = event.target;
        requestAnimationFrame(() => {
            focusError(firstInvalid);
            firstInvalid = null;
        });
    }, true);
});
