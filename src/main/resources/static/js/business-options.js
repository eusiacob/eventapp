document.addEventListener("DOMContentLoaded", () => {
    document.querySelectorAll(".business-options-picker").forEach(picker => {
        const category = picker.closest("form").querySelector('[name="category"]');
        if (!category) return;
        const serviceBoxes = [...picker.querySelectorAll(".service-type-checkbox")];
        const eventBoxes = [...picker.querySelectorAll(".event-type-checkbox")];
        const other = picker.querySelector(".other-service-details");
        const otherInput = other.querySelector("input");
        function update() {
            serviceBoxes.forEach(box => {
                const option = box.closest(".service-type-option");
                const available = option.dataset.category === category.value;
                option.hidden = !available;
                box.disabled = !available;
                if (!available) box.checked = false;
            });
            const selected = serviceBoxes.filter(box => box.checked && !box.disabled);
            const showOther = selected.some(box => box.dataset.other === "true");
            other.hidden = !showOther;
            otherInput.disabled = !showOther;
            otherInput.required = showOther;
            if (!showOther) otherInput.value = "";
            picker.querySelector(".service-type-count").textContent = "(" + selected.length + " selectate)";
            picker.querySelector(".event-type-count").textContent =
                "(" + eventBoxes.filter(box => box.checked).length + " selectate)";
        }
        category.addEventListener("change", update);
        picker.addEventListener("change", update);
        update();
    });
});

