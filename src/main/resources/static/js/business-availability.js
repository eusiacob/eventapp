document.addEventListener("DOMContentLoaded", () => {
    const form = document.getElementById("availabilityForm");
    if (!form) return;
    const calendarEl = document.getElementById("availabilityCalendar");
    const start = document.getElementById("availabilityStart");
    const end = document.getElementById("availabilityEnd");
    const message = document.getElementById("availabilityMessage");
    const list = document.getElementById("blockedDateList");
    let blocked = new Set(window.businessUnavailableDates || []);
    let busy = false;
    const today = new Intl.DateTimeFormat("sv-SE", {timeZone: "Europe/Bucharest"}).format(new Date());
    start.min = end.min = today;
    const dateLabel = value => value.split("-").reverse().join(".");
    function nextDay(value) {
        const date = new Date(value + "T12:00:00Z");
        date.setUTCDate(date.getUTCDate() + 1);
        return date.toISOString().slice(0, 10);
    }
    function paint() {
        calendarEl.querySelectorAll("[data-date]").forEach(cell => {
            const day = cell.dataset.date;
            cell.classList.toggle("availability-blocked", blocked.has(day));
            cell.classList.toggle("availability-past", day < today);
            cell.classList.toggle("availability-selected", day >= start.value && day <= end.value && !!start.value);
            cell.title = dateLabel(day) + (blocked.has(day) ? " — Indisponibil" : " — Disponibil pentru solicitări");
        });
    }
    function show(text, error = false) {
        message.className = "alert mt-3 " + (error ? "alert-danger" : "alert-success");
        message.textContent = text;
    }
    function renderList() {
        list.replaceChildren();
        const days = [...blocked].filter(day => day >= today).sort();
        const ranges = [];
        days.forEach(day => {
            const last = ranges[ranges.length - 1];
            if (last && nextDay(last.end) === day) last.end = day;
            else ranges.push({start: day, end: day});
        });
        if (!ranges.length) {
            list.textContent = "Nu ai date viitoare blocate.";
        }
        ranges.forEach(range => {
            const row = document.createElement("div");
            row.className = "d-flex align-items-center justify-content-between gap-2 py-2 border-bottom";
            const label = document.createElement("span");
            label.className = "small";
            label.textContent = dateLabel(range.start) + (range.end !== range.start ? " – " + dateLabel(range.end) : "");
            const button = document.createElement("button");
            button.type = "button";
            button.className = "btn btn-sm btn-outline-secondary";
            button.textContent = "Deblochează";
            button.disabled = busy;
            button.setAttribute("aria-label", "Deblochează " + label.textContent);
            // Deblocking a listed interval is explicit and does not toggle existing state.
            button.addEventListener("click", () => {
                start.value = range.start;
                end.value = range.end;
                paint();
                save(range.start, range.end, false);
            });
            row.append(label, button);
            list.append(row);
        });
    }
    async function save(from, to, unavailable) {
        if (busy) return;
        const length = (Date.parse(to) - Date.parse(from)) / 86400000 + 1;
        if (!from || !to || from < today || to < from || !Number.isFinite(length) || (unavailable && length > 366)) {
            show("Alege un interval valid, de maximum 366 de zile, începând de astăzi.", true);
            return;
        }
        busy = true;
        form.querySelectorAll("input, button").forEach(control => control.disabled = true);
        renderList();
        const headers = {"Content-Type": "application/x-www-form-urlencoded", "Accept": "application/json"};
        const csrfHeader = document.querySelector('meta[name="_csrf_header"]')?.content;
        const token = document.querySelector('meta[name="_csrf"]')?.content;
        if (csrfHeader && token) headers[csrfHeader] = token;
        try {
            const response = await axios.post(form.action,
                new URLSearchParams({start: from, end: to, unavailable: String(unavailable)}), {headers});
            if (!Array.isArray(response.data)) throw new Error("Răspuns invalid");
            blocked = new Set(response.data);
            show(unavailable ? "Intervalul a fost blocat." : "Intervalul este din nou disponibil pentru solicitări.");
        } catch (error) {
            const status = error.response?.status;
            show(status === 403 || status === 401 ? "Sesiunea a expirat sau nu ai permisiunea. Reîncarcă pagina."
                : status === 400 ? "Interval invalid. Verifică datele selectate."
                : "Modificarea nu a putut fi confirmată. Reîncarcă pagina pentru a verifica datele înainte de a reîncerca.", true);
        } finally {
            busy = false;
            form.querySelectorAll("input, button").forEach(control => control.disabled = false);
            paint();
            renderList();
        }
    }
    form.addEventListener("submit", event => {
        event.preventDefault();
        save(start.value, end.value, true);
    });
    start.addEventListener("change", () => {
        end.min = start.value || today;
        if (!end.value || end.value < start.value) end.value = start.value;
        paint();
    });
    end.addEventListener("change", paint);
    const calendar = new FullCalendar.Calendar(calendarEl, {
        initialView: "dayGridMonth",
        locale: "ro",
        firstDay: 1,
        height: "auto",
        fixedWeekCount: false,
        showNonCurrentDates: false,
        headerToolbar: {left: "prev,next", center: "title", right: "today"},
        buttonText: {today: "Astăzi"},
        datesSet: () => requestAnimationFrame(paint),
        dayCellDidMount: () => requestAnimationFrame(paint),
        dateClick: info => {
            if (busy || info.dateStr < today) return;
            start.value = end.value = info.dateStr;
            end.min = info.dateStr;
            paint();
        }
    });
    calendar.render();
    renderList();
});
