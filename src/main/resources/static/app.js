let auth = sessionStorage.getItem("petmedtrack-auth");
let formHandlersAttached = false;

const escapeHtml = (value) => String(value ?? "")
    .replaceAll("&", "&amp;")
    .replaceAll("<", "&lt;")
    .replaceAll(">", "&gt;")
    .replaceAll('"', "&quot;")
    .replaceAll("'", "&#039;");

async function request(url, options = {}, token = auth) {
    const headers = {
        ...(options.headers || {}),
        Authorization: `Basic ${token}`
    };

    const response = await fetch(url, { ...options, headers });

    if (response.status === 401) {
        if (token === auth) {
            logout();
        }
        throw new Error("Kullanıcı adı veya şifre hatalı.");
    }

    if (!response.ok) {
        let message = "İşlem tamamlanamadı.";
        try {
            message = await response.text();
        } catch (ignored) {
            // Sunucu cevap vermediyse varsayılan mesajı kullan.
        }
        throw new Error(message || "İşlem tamamlanamadı.");
    }

    if (response.status === 204) {
        return null;
    }

    return response.json();
}

function showLogin() {
    if (document.getElementById("login-screen")) {
        return;
    }

    const loginScreen = document.createElement("div");
    loginScreen.id = "login-screen";
    loginScreen.innerHTML = `
        <div class="login-card">
            <div class="login-mark">✦</div>
            <p class="eyebrow">PETMEDTRACK</p>
            <h1>Hoş geldin</h1>
            <p class="login-description">Kliniğinin bakım paneline giriş yap.</p>
            <form id="login-form">
                <label for="login-username">Kullanıcı adı</label>
                <input id="login-username" autocomplete="username" required>
                <label for="login-password">Şifre</label>
                <input id="login-password" type="password" autocomplete="current-password" required>
                <button class="primary-button" type="submit">Giriş yap</button>
                <p id="login-message" class="login-message"></p>
            </form>
        </div>
    `;

    document.body.appendChild(loginScreen);

    document.getElementById("login-form").addEventListener("submit", async (event) => {
        event.preventDefault();

        const username = document.getElementById("login-username").value.trim();
        const password = document.getElementById("login-password").value;
        const candidate = btoa(`${username}:${password}`);
        const message = document.getElementById("login-message");

        try {
            await request("/api/owners", {}, candidate);
            auth = candidate;
            sessionStorage.setItem("petmedtrack-auth", auth);
            loginScreen.remove();
            await initDashboard();
        } catch (error) {
            message.textContent = error.message;
        }
    });
}

function logout() {
    auth = null;
    sessionStorage.removeItem("petmedtrack-auth");
    document.getElementById("login-screen")?.remove();
    showLogin();
}

async function loadDashboard() {
    const [owners, pets, medications, treatments] = await Promise.all([
        request("/api/owners"),
        request("/api/pets"),
        request("/api/medications"),
        request("/api/treatments")
    ]);

    document.getElementById("owner-count").textContent = owners.length;
    document.getElementById("pet-count").textContent = pets.length;
    document.getElementById("medication-count").textContent = medications.length;

    const today = new Date();
    const activeTreatments = treatments.filter(treatment =>
        !treatment.endDate || new Date(treatment.endDate) >= today
    );
    document.getElementById("treatment-count").textContent = activeTreatments.length;

    const treatmentsBody = document.getElementById("treatments-body");
    treatmentsBody.innerHTML = "";

    if (treatments.length === 0) {
        treatmentsBody.innerHTML = `
            <tr><td colspan="4" class="empty-state">Henüz tedavi kaydı yok.</td></tr>
        `;
        return;
    }

    treatments.forEach(treatment => {
        const isActive = !treatment.endDate || new Date(treatment.endDate) >= today;
        const statusText = isActive ? "Aktif" : "Tamamlandı";
        const statusClass = isActive ? "active-status" : "completed-status";
        const petName = treatment.pet?.name || "Bilinmeyen hasta";
        const species = treatment.pet?.species || "-";
        const medicationName = treatment.medication?.name || "-";

        const row = document.createElement("tr");
        row.innerHTML = `
            <td>
                <div class="patient">
                    <span class="patient-avatar sage">${escapeHtml(petName.charAt(0))}</span>
                    <div><strong>${escapeHtml(petName)}</strong><small>${escapeHtml(species)}</small></div>
                </div>
            </td>
            <td>${escapeHtml(medicationName)}</td>
            <td>${escapeHtml(treatment.dosage)} · ${escapeHtml(treatment.frequency)}</td>
            <td><span class="status ${statusClass}">${statusText}</span></td>
        `;
        treatmentsBody.appendChild(row);
    });
}

async function submitForm(form, url, data, messageId) {
    const message = document.getElementById(messageId);

    try {
        await request(url, {
            method: "POST",
            headers: { "Content-Type": "application/json" },
            body: JSON.stringify(data)
        });
        message.textContent = "Başarıyla kaydedildi.";
        form.reset();
        await loadDashboard();
    } catch (error) {
        message.textContent = error.message;
    }
}

function attachFormHandlers() {
    if (formHandlersAttached) {
        return;
    }
    formHandlersAttached = true;

    document.getElementById("create-owner-form")?.addEventListener("submit", event => {
        event.preventDefault();
        submitForm(event.target, "/api/owners", {
            firstName: document.getElementById("first-name").value,
            lastName: document.getElementById("last-name").value,
            email: document.getElementById("email").value,
            phoneNumber: document.getElementById("phone-number").value
        }, "owner-message");
    });

    document.getElementById("create-pet-form")?.addEventListener("submit", event => {
        event.preventDefault();
        submitForm(event.target, "/api/pets", {
            name: document.getElementById("pet-name").value,
            species: document.getElementById("species").value,
            breed: document.getElementById("breed").value,
            birthDate: document.getElementById("birth-date").value || null,
            ownerId: Number(document.getElementById("owner-id").value)
        }, "pet-message");
    });

    document.getElementById("create-medication-form")?.addEventListener("submit", event => {
        event.preventDefault();
        submitForm(event.target, "/api/medications", {
            name: document.getElementById("medication-name").value
        }, "medication-message");
    });

    document.getElementById("create-treatment-form")?.addEventListener("submit", event => {
        event.preventDefault();
        submitForm(event.target, "/api/treatments", {
            petId: Number(document.getElementById("treatment-pet-id").value),
            medicationId: Number(document.getElementById("treatment-medication-id").value),
            dosage: document.getElementById("dosage").value,
            frequency: document.getElementById("frequency").value,
            startDate: document.getElementById("start-date").value,
            endDate: document.getElementById("end-date").value || null
        }, "treatment-message");
    });

    document.getElementById("open-owner-form")?.addEventListener("click", () => {
        document.getElementById("owner-form")?.scrollIntoView({ behavior: "smooth" });
    });
}

async function initDashboard() {
    attachFormHandlers();
    try {
        await loadDashboard();
    } catch (error) {
        console.error(error);
    }
}

if (auth) {
    initDashboard().catch(() => showLogin());
} else {
    showLogin();
}
