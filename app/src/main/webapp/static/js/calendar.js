(() => {
    "use strict";
    const allDay = document.getElementById("allDay");
    const frequency = document.getElementById("frequency");
    const endMode = document.getElementById("endMode");
    function updateFields() {
        document.querySelectorAll(".calendar-time").forEach(input => {
            input.disabled = Boolean(allDay && allDay.checked);
            input.required = !input.disabled;
            input.closest(".calendar-field").hidden = input.disabled;
        });
        ["endMode", "count", "until"].forEach(name => {
            const input = document.getElementById(name);
            if (!input) {
                return;
            }
            input.disabled = !frequency || frequency.value === "NONE"
                || (name === "count" && endMode.value !== "count")
                || (name === "until" && endMode.value !== "until");
            input.required = !input.disabled;
        });
    }
    [allDay, frequency, endMode].filter(Boolean).forEach(input => input.addEventListener("change", updateFields));
    updateFields();
})();