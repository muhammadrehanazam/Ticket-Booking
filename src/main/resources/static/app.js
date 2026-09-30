const state = { user: null, schedule: null, coachClass: null, seats: [], selected: [], lock: null, booking: null };
const $ = (id) => document.getElementById(id);
const savedUserKey = "ticketBooking.user";

async function api(url, options = {}) {
    const headers = {
        "Content-Type": "application/json",
        ...(state.user && state.user.token ? { Authorization: `Bearer ${state.user.token}` } : {})
    };
    const response = await fetch(url, { headers, ...options, headers: { ...headers, ...(options.headers || {}) } });
    const data = await response.json().catch(() => ({}));
    if (!response.ok) throw new Error(data.message || data.error || `Request failed (${response.status})`);
    return data;
}
function showMessage(text, type = "error") {
    const message = $("message");
    message.textContent = text;
    message.className = `message ${type}`;
    clearTimeout(showMessage.timeout);
    showMessage.timeout = setTimeout(() => message.classList.add("hidden"), 4500);
}
function formData(form) { return Object.fromEntries(new FormData(form).entries()); }
function setLoggedIn(user) {
    state.user = user;
    if (user) {
        localStorage.setItem(savedUserKey, JSON.stringify(user));
    } else {
        localStorage.removeItem(savedUserKey);
    }
    $("authPanel").classList.toggle("hidden", !!user);
    $("appPanel").classList.toggle("hidden", !user);
    $("userBox").classList.toggle("hidden", !user);
    $("adminPanel").classList.toggle("hidden", !user || user.role !== "ADMIN");
    if (user) { $("userBox").textContent = `${user.name} (${user.userId})`; loadHistory(); }
    if (user && user.role === "ADMIN") loadAdminData();
}
async function submitAuth(event, url) {
    event.preventDefault();
    try { const user = await api(url, { method: "POST", body: JSON.stringify(formData(event.target)) }); setLoggedIn(user); showMessage("Account action successful", "success"); }
    catch (error) { showMessage(error.message); }
}
$("loginForm").addEventListener("submit", (e) => submitAuth(e, "/api/users/login"));
$("registerForm").addEventListener("submit", (e) => submitAuth(e, "/api/users/register"));
$("logoutButton").addEventListener("click", () => { setLoggedIn(null); showMessage("Logged out", "success"); });

$("searchForm").addEventListener("submit", async (event) => {
    event.preventDefault();
    try {
        const values = formData(event.target);
        const trains = await api(`/api/schedules/search?from=${encodeURIComponent(values.from)}&to=${encodeURIComponent(values.to)}&date=${values.date}`);
        $("results").classList.remove("hidden"); $("seatPanel").classList.add("hidden");
        $("trainResults").innerHTML = trains.length ? trains.map(train => `<div class="train">
            <div class="train-top"><div><span class="train-badge">RAILWAY</span><strong>${train.trainName}</strong><span class="train-number">${train.trainNumber}</span></div><button data-schedule="${train.scheduleId}">Select train</button></div>
            <div class="route-line"><div><small>FROM</small><strong>${train.fromStation}</strong><span>${train.departureTime}</span></div><div class="route-arrow">→<small>${train.duration}</small></div><div><small>TO</small><strong>${train.toStation}</strong><span>${train.arrivalTime}</span></div></div>
            <div class="fare-strip">${train.classFares.map(fare => `<span><b>${fare.coachClass.replace("_", " ")}</b> ${fare.fare} PKR <small>${fare.availableSeats} seats</small></span>`).join("")}</div>
        </div>`).join("") : `<div class='no-results'><strong>No trains available</strong><span>No train is available for the selected route and date.</span><small>Try another date or search a different route.</small></div>`;
        document.querySelectorAll("[data-schedule]").forEach(button => button.addEventListener("click", () => chooseSchedule(trains.find(t => t.scheduleId == button.dataset.schedule))));
    } catch (error) { showMessage(error.message); }
});
async function chooseSchedule(schedule) {
    state.schedule = schedule; state.selected = []; $("seatPanel").classList.remove("hidden");
    $("classChoices").innerHTML = schedule.classFares.map(fare => `<button class="choice" data-class="${fare.coachClass}">${fare.coachClass} - ${fare.fare} (${fare.availableSeats} available)</button>`).join("");
    document.querySelectorAll("[data-class]").forEach(button => button.addEventListener("click", () => chooseClass(button.dataset.class)));
}
async function chooseClass(coachClass) {
    state.coachClass = coachClass; state.selected = [];
    document.querySelectorAll("[data-class]").forEach(button => button.classList.toggle("active", button.dataset.class === coachClass));
    try { state.seats = await api(`/api/schedules/${state.schedule.scheduleId}/seats?coachClass=${coachClass}`); renderSeats(); }
    catch (error) { showMessage(error.message); }
}
function renderSeats() {
    const coaches = state.seats.reduce((groups, seat) => {
        const coach = seat.coachNumber || "Coach";
        (groups[coach] ||= []).push(seat);
        return groups;
    }, {});
    $("seatGrid").innerHTML = Object.entries(coaches).map(([coach, seats]) => {
        const available = seats.filter(seat => seat.status === "AVAILABLE").length;
        const berthCards = seats.map(seat => {
            const position = seat.seatPosition ? seat.seatPosition.replaceAll("_", " ") : "Seat";
            return `<button class="seat ${seat.status.toLowerCase()}" data-seat="${seat.seatId}" ${seat.status !== "AVAILABLE" ? "disabled" : ""}>
                <strong>${seat.seatNumber}</strong><span class="seat-position">${position}</span><small>${seat.status}</small>
            </button>`;
        }).join("");
        return `<div class="coach-map">
            <div class="coach-header"><strong>Coach ${coach}</strong><span>${available} available</span></div>
            <div class="coach-seats">${berthCards}</div>
        </div>`;
    }).join("");
    document.querySelectorAll("[data-seat]").forEach(button => button.addEventListener("click", () => {
        const id = Number(button.dataset.seat);
        state.selected = state.selected.includes(id) ? state.selected.filter(x => x !== id) : [...state.selected, id];
        button.classList.toggle("active", state.selected.includes(id));
        button.querySelector("small").textContent = state.selected.includes(id) ? "SELECTED" : "AVAILABLE";
        $("lockButton").disabled = !state.selected.length;
    }));
}
$("lockButton").addEventListener("click", async () => {
    try {
        state.lock = await api("/api/seat-allocations/lock", { method: "POST", body: JSON.stringify({ scheduleId: state.schedule.scheduleId, seatIds: state.selected }) });
        $("bookingPanel").classList.remove("hidden"); $("lockMessage").textContent = `Seats locked until ${new Date(state.lock.lockExpiryTime).toLocaleTimeString()}.`;
        $("passengerFields").innerHTML = state.selected.map((id, index) => `<div class="card"><strong>Passenger ${index + 1} - Seat ${state.seats.find(s => s.seatId === id).seatNumber}</strong><label>Name<input name="passengerName-${id}" required></label><label>CNIC/B-Form<input name="cnicOrBForm-${id}" required></label><label>Age<input name="age-${id}" type="number" min="1" required></label><label>Gender<select name="gender-${id}" required><option value="MALE">MALE</option><option value="FEMALE">FEMALE</option></select></label></div>`).join("");
    } catch (error) { showMessage(error.message); }
});
$("bookingForm").addEventListener("submit", async (event) => {
    event.preventDefault(); const values = formData(event.target);
    try {
        const passengers = state.selected.map(id => ({ passengerName: values[`passengerName-${id}`], cnicOrBForm: values[`cnicOrBForm-${id}`], age: Number(values[`age-${id}`]), gender: values[`gender-${id}`] }));
        state.booking = await api("/api/bookings", { method: "POST", body: JSON.stringify({ userId: state.user.userId, scheduleId: state.schedule.scheduleId, lockToken: state.lock.lockToken, passengers }) });
        $("confirmationPanel").classList.remove("hidden"); $("confirmation").innerHTML = `<p><strong>PNR:</strong> ${state.booking.pnrNumber}</p><p><strong>Amount:</strong> ${state.booking.totalAmount}</p><p><strong>Status:</strong> ${state.booking.bookingStatus}</p><p><strong>Pay before:</strong> ${new Date(state.booking.paymentDeadline).toLocaleTimeString()}</p>`; showMessage("Booking confirmed. Complete payment within 10 minutes.", "success");
        loadHistory();
    } catch (error) { showMessage(error.message); }
});
$("payButton").addEventListener("click", async () => { try { const paid = await api(`/api/bookings/${state.booking.bookingId}/pay`, { method: "POST" }); $("confirmation").innerHTML += `<p><strong>Payment:</strong> ${paid.paymentStatus}</p>`; $("payButton").disabled = true; showMessage("Payment successful", "success"); loadHistory(); } catch (error) { showMessage(error.message); } });
$("historyButton").addEventListener("click", loadHistory);
async function adminApi(url, options = {}) {
    const headers = { "Content-Type": "application/json", "X-User-Id": state.user.userId };
    if (state.user && state.user.token) headers.Authorization = `Bearer ${state.user.token}`;
    return api(url, { ...options, headers });
}
async function loadAdminData() {
    try {
        const trains = await adminApi("/api/admin/trains");
        $("adminTrainSelect").innerHTML = `<option value="">Select train</option>` + trains.map(t => `<option value="${t.trainId}">${t.trainNumber} - ${t.name}</option>`).join("");
        const schedules = await adminApi("/api/admin/schedules");
        $("adminSchedules").innerHTML = schedules.length ? schedules.map(s => `<div class="booking-row"><span><strong>${s.trainName}</strong><br><span class="muted">${s.sourceCity} to ${s.destinationCity} | ${s.travelDate}</span></span><span>${s.scheduleId}</span></div>`).join("") : "<p class='muted'>No schedules yet.</p>";
    } catch (error) { $("adminMessage").textContent = error.message; }
}
$("adminRefreshButton").addEventListener("click", loadAdminData);
$("trainForm").addEventListener("submit", async event => {
    event.preventDefault();
    try { await adminApi("/api/admin/trains", { method: "POST", body: JSON.stringify(formData(event.target)) }); event.target.reset(); showMessage("Train created", "success"); loadAdminData(); }
    catch (error) { showMessage(error.message); }
});
$("scheduleForm").addEventListener("submit", async event => {
    event.preventDefault();
    try { await adminApi("/api/admin/schedules", { method: "POST", body: JSON.stringify(formData(event.target)) }); event.target.reset(); showMessage("Schedule created", "success"); loadAdminData(); }
    catch (error) { showMessage(error.message); }
});
function updateAdminDuration() {
    const departure = $("scheduleForm").departureTime.value;
    const arrival = $("scheduleForm").arrivalTime.value;
    if (!departure || !arrival) { $("adminDuration").value = ""; return; }
    const toMinutes = value => {
        const [hours, minutes] = value.split(":").map(Number);
        return hours * 60 + minutes;
    };
    let duration = toMinutes(arrival) - toMinutes(departure);
    if (duration <= 0) duration += 24 * 60;
    $("adminDuration").value = `${Math.floor(duration / 60)} h ${duration % 60} min`;
}
$("scheduleForm").departureTime.addEventListener("input", updateAdminDuration);
$("scheduleForm").arrivalTime.addEventListener("input", updateAdminDuration);
async function loadHistory() { if (!state.user) return; try { const bookings = await api(`/api/bookings/user/${state.user.userId}`); $("history").innerHTML = bookings.length ? bookings.map(b => `<div class="booking-row"><span><strong>${b.pnrNumber}</strong><br><span class="muted">${b.trainName} | ${b.bookingStatus} | ${b.paymentStatus}</span></span><span>${b.totalAmount}</span>${b.bookingStatus === "CONFIRMED" ? `<button data-cancel="${b.bookingId}" class="secondary">Cancel</button>` : ""}</div>`).join("") : "<p class='muted'>No bookings yet.</p>"; document.querySelectorAll("[data-cancel]").forEach(button => button.addEventListener("click", () => cancelBooking(button.dataset.cancel))); } catch (error) { showMessage(error.message); } }
async function cancelBooking(id) { try { await api(`/api/bookings/${id}/cancel`, { method: "POST" }); showMessage("Booking cancelled", "success"); loadHistory(); } catch (error) { showMessage(error.message); } }
const tomorrow = new Date(); tomorrow.setDate(tomorrow.getDate() + 1); $("searchForm").date.value = tomorrow.toISOString().slice(0, 10);

try {
    const savedUser = JSON.parse(localStorage.getItem(savedUserKey));
    if (savedUser && savedUser.userId && savedUser.name) setLoggedIn(savedUser);
} catch {
    localStorage.removeItem(savedUserKey);
}
