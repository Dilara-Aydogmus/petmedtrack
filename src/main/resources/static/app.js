let auth = sessionStorage.getItem("petmedtrack-auth");
let currentUser = null;
let formHandlersAttached = false;
let dashboardData = {
    owners: [], pets: [], medications: [], diseases: [], treatments: [], clinics: [], veterinarians: []
};

const escapeHtml = (value) => String(value ?? "")
    .replaceAll("&", "&amp;")
    .replaceAll("<", "&lt;")
    .replaceAll(">", "&gt;")
    .replaceAll('"', "&quot;")
    .replaceAll("'", "&#039;");

async function request(url, options = {}, token = auth) {
    const headers = {
        ...(options.headers || {}),
        Authorization: `Bearer ${token}`
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
            <div id="login-view" class="auth-view">
                <h1>Hoş geldin</h1>
                <p class="login-description">Kliniğinin bakım paneline güvenle giriş yap.</p>
                <form id="login-form">
                    <label for="login-username">Kullanıcı adı</label>
                    <input id="login-username" autocomplete="username" required>
                    <label for="login-password">Şifre</label>
                    <input id="login-password" type="password" autocomplete="current-password" required>
                    <button class="primary-button" type="submit">Giriş yap</button>
                    <p id="login-message" class="login-message"></p>
                </form>
                <div class="auth-switch">
                    <span>Hesabın yok mu?</span>
                    <button id="show-register" class="text-button" type="button">Yeni kullanıcı oluştur</button>
                </div>
            </div>
            <div id="register-view" class="auth-view hidden-form">
                <button id="show-login" class="back-button" type="button">← Giriş ekranına dön</button>
                <h1>Hesap oluştur</h1>
                <p class="login-description">PetMedTrack’i kullanmak için bilgilerini gir.</p>
                <div class="account-type-choice" aria-label="Hesap türü">
                    <button type="button" class="account-type" data-role="USER">
                        <strong>Normal kullanıcı</strong>
                        <small>Hayvan sahibi olarak kayıt ol</small>
                    </button>
                    <button type="button" class="account-type" data-role="VET">
                        <strong>Veteriner hesabı</strong>
                        <small>Klinik ve veteriner bilgilerini ekle</small>
                    </button>
                </div>
                <p id="register-choice-message" class="register-choice-message">Devam etmek için hesap türünü seç.</p>
                <form id="register-form" class="hidden-form">
                    <input id="register-role" type="hidden" value="USER">
                    <p id="register-form-title" class="register-section-title">Kullanıcı bilgileri</p>
                    <label for="register-username">Kullanıcı adı</label>
                    <input id="register-username" autocomplete="username" required>
                    <label for="register-password">Şifre</label>
                    <input id="register-password" type="password" autocomplete="new-password" required>
                    <label for="register-first-name">Ad</label>
                    <input id="register-first-name" autocomplete="given-name" required>
                    <label for="register-last-name">Soyad</label>
                    <input id="register-last-name" autocomplete="family-name" required>
                    <label for="register-email">E-posta</label>
                    <input id="register-email" type="email" autocomplete="email" required>
                    <label for="register-phone-number">Telefon</label>
                    <input id="register-phone-number" autocomplete="tel" required>
                    <div id="register-user-clinic-field" class="register-role-field">
                        <label for="register-user-clinic-name">Bağlanılacak klinik adı</label>
                        <input id="register-user-clinic-name" autocomplete="organization" required>
                    </div>
                    <div id="register-clinic-fields" class="hidden-form">
                        <p class="register-section-title">Klinik bilgileri</p>
                        <label for="register-clinic-name">Klinik adı</label>
                        <input id="register-clinic-name">
                        <label for="register-clinic-address">Klinik adresi</label>
                        <input id="register-clinic-address">
                        <label for="register-clinic-phone-number">Klinik telefonu</label>
                        <input id="register-clinic-phone-number">
                    </div>
                    <button class="primary-button" type="submit">Hesap oluştur</button>
                    <p id="register-message" class="login-message"></p>
                </form>
            </div>
        </div>
    `;

    document.body.appendChild(loginScreen);

    document.getElementById("show-register").addEventListener("click", () => {
        document.getElementById("login-view").classList.add("hidden-form");
        document.getElementById("register-view").classList.remove("hidden-form");
        document.getElementById("register-username").focus();
    });

    document.getElementById("show-login").addEventListener("click", () => {
        document.getElementById("register-view").classList.add("hidden-form");
        document.getElementById("login-view").classList.remove("hidden-form");
        document.getElementById("login-username").focus();
    });

    document.querySelectorAll(".account-type").forEach(button => {
        button.addEventListener("click", event => {
            const role = event.currentTarget.dataset.role;
            document.getElementById("register-role").value = role;
            document.querySelectorAll(".account-type").forEach(item => {
                item.classList.toggle("active", item === event.currentTarget);
            });
            document.getElementById("register-form").classList.remove("hidden-form");
            document.getElementById("register-choice-message").classList.add("hidden-form");
            document.getElementById("register-form-title").textContent = role === "VET"
                ? "Veteriner ve klinik bilgileri"
                : "Kullanıcı bilgileri";
            document.getElementById("register-clinic-fields")
                .classList.toggle("hidden-form", role !== "VET");
            document.getElementById("register-user-clinic-field")
                .classList.toggle("hidden-form", role === "VET");
            document.getElementById("register-user-clinic-name").required = role === "USER";
            document.getElementById("register-clinic-name").required = role === "VET";
            document.getElementById("register-clinic-phone-number").required = role === "VET";
            document.getElementById("register-form").scrollIntoView({ behavior: "smooth", block: "start" });
        });
    });

    document.getElementById("register-form").addEventListener("submit", async event => {
        event.preventDefault();

        const role = document.getElementById("register-role").value;
        const message = document.getElementById("register-message");
        const requestBody = {
            username: document.getElementById("register-username").value.trim(),
            password: document.getElementById("register-password").value,
            role: role,
            firstName: document.getElementById("register-first-name").value.trim(),
            lastName: document.getElementById("register-last-name").value.trim(),
            email: document.getElementById("register-email").value.trim(),
            phoneNumber: document.getElementById("register-phone-number").value.trim(),
            clinicName: role === "VET"
                ? document.getElementById("register-clinic-name").value.trim()
                : document.getElementById("register-user-clinic-name").value.trim(),
            clinicAddress: role === "VET" ? document.getElementById("register-clinic-address").value.trim() : null,
            clinicPhoneNumber: role === "VET" ? document.getElementById("register-clinic-phone-number").value.trim() : null
        };

        try {
            const response = await fetch("/api/auth/register", {
                method: "POST",
                headers: { "Content-Type": "application/json" },
                body: JSON.stringify(requestBody)
            });

            if (!response.ok) {
                throw new Error(await response.text() || "Kayıt oluşturulamadı.");
            }

            message.textContent = "Kayıt başarılı. Şimdi giriş yapabilirsiniz.";
            document.getElementById("login-username").value = requestBody.username;
            document.getElementById("register-form").reset();
            document.getElementById("register-form").classList.add("hidden-form");
            document.getElementById("register-choice-message").classList.remove("hidden-form");
            document.querySelectorAll(".account-type").forEach(item => item.classList.remove("active"));
            document.getElementById("register-view").classList.add("hidden-form");
            document.getElementById("login-view").classList.remove("hidden-form");
        } catch (error) {
            message.textContent = error.message;
        }
    });

    document.getElementById("login-form").addEventListener("submit", async (event) => {
        event.preventDefault();

        const username = document.getElementById("login-username").value.trim();
        const password = document.getElementById("login-password").value;
        const message = document.getElementById("login-message");

        try {
            const response = await fetch("/api/auth/login", {
                method: "POST",
                headers: {
                    "Content-Type": "application/json"
                },
                body: JSON.stringify({
                    username: username,
                    password: password
                })
            });

            if (!response.ok) {
                throw new Error("Kullanıcı adı veya şifre hatalı.");
            }

            auth = await response.text();
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

const pageDefinitions = {
    dashboard: {
        label: "Genel Bakış",
        selectors: [".welcome-row", ".stats-grid", "#search", ".content-grid", ".bottom-grid"]
    },
    pets: {
        label: "Dostlarım",
        selectors: ["#pets-view", "#pet-form"]
    },
    treatments: {
        label: "Tedaviler",
        selectors: [".content-grid", "#treatment-form", "#treatment-plan-form", "#treatment-edit-form"]
    },
    medications: {
        label: "İlaçlar",
        selectors: ["#medications-view", "#medication-form"]
    },
    owners: {
        label: "Sahipler",
        selectors: ["#owners-view", "#owner-form", "#clinic-form"]
    }
};

function pageNameFromHash(hash) {
    const value = (hash || "").replace("#", "");
    if (pageDefinitions[value]) {
        return value;
    }

    const targetToPage = {
        "pet-form": "pets",
        "new-medication": "medications",
        "new-treatment": "treatments",
        "new-treatment-plan": "treatments",
        "all-treatments": "treatments",
        "edit-treatment-anchor": "treatments",
        "owner-form": "owners"
    };
    return targetToPage[value] || "dashboard";
}

function showPage(pageName, updateUrl = true) {
    const page = pageDefinitions[pageName] || pageDefinitions.dashboard;
    const mainContent = document.querySelector(".main-content");
    if (!mainContent) {
        return;
    }

    mainContent.querySelectorAll(":scope > section").forEach(section => {
        section.classList.add("page-hidden");
        section.classList.remove("page-visible");
    });

    page.selectors.forEach(selector => {
        mainContent.querySelectorAll(selector).forEach(element => {
            element.classList.remove("page-hidden");
            element.classList.add("page-visible");
        });
    });

    document.querySelectorAll(".nav-item").forEach(item => {
        item.classList.toggle("active", item.getAttribute("href") === `#${pageName}`);
    });

    const breadcrumb = document.querySelector(".breadcrumb");
    if (breadcrumb) {
        breadcrumb.innerHTML = `<span>PetMedTrack</span><b>/</b> ${page.label}`;
    }

    if (updateUrl && window.location.hash !== `#${pageName}`) {
        history.pushState(null, "", `#${pageName}`);
    }
}

function setupPageNavigation() {
    document.querySelectorAll(".nav-item, .quick-actions a, .panel-heading a").forEach(link => {
        link.addEventListener("click", event => {
            const target = link.getAttribute("href");
            if (!target || !target.startsWith("#")) {
                return;
            }

            event.preventDefault();
            showPage(pageNameFromHash(target));
        });
    });

    window.addEventListener("popstate", () => {
        showPage(pageNameFromHash(window.location.hash), false);
    });

    showPage(pageNameFromHash(window.location.hash), false);
}

function fillClinicSelect(selectId, clinics) {
    const select = document.getElementById(selectId);
    if (!select) {
        return;
    }

    const selectedValue = select.value;
    select.innerHTML = '<option value="">Klinik seçin</option>';

    clinics.forEach(clinic => {
        const option = document.createElement("option");
        option.value = clinic.id;
        option.textContent = clinic.name;
        select.appendChild(option);
    });

    if (selectedValue) {
        select.value = selectedValue;
    }
}

async function loadReferenceData() {
    const clinics = await request("/api/clinics");
    fillClinicSelect("owner-clinic-id", clinics);
    fillClinicSelect("veterinarian-clinic-id", clinics);
}

async function loadCurrentUser() {
    currentUser = await request("/api/auth/me");
    const userName = currentUser.veterinarianName || currentUser.username || "Kullanıcı";
    const clinicName = currentUser.role === "VET" && currentUser.clinicName
        ? currentUser.clinicName
        : "Klinik bilgisi yok";
    const initials = userName
        .split(" ")
        .filter(Boolean)
        .map(part => part.charAt(0))
        .join("")
        .slice(0, 2)
        .toUpperCase();

    document.getElementById("current-user-name").textContent = userName;
    document.getElementById("current-user-welcome").textContent = userName;
    document.getElementById("current-clinic-name").textContent = clinicName;
    document.getElementById("current-user-avatar").textContent = initials || "U";
}

function actionButton(action, id, label) {
    return `<button type="button" class="table-action" data-action="${action}" data-id="${id}">${label}</button>`;
}

function applyRoleBasedInterface() {
    const role = String(currentUser?.role || "")
        .trim()
        .toUpperCase()
        .replace(/^ROLE_/, "");
    const isVet = role === "VET";
    const vetOnlyElements = [
        'a.nav-item[href="#owners"]',
        "#open-owner-form",
        "#owner-search-panel",
        "#owners-view",
        ".quick-panel",
        "#owner-form",
        "#veterinarian-form",
        "#pet-form",
        "#medication-form",
        "#treatment-form",
        "#disease-form",
        "#treatment-plan-form",
        "#treatment-edit-form"
    ];

    vetOnlyElements.forEach(selector => {
        document.querySelectorAll(selector).forEach(element => {
            element.classList.toggle("role-hidden", !isVet);
            if (isVet) {
                element.style.removeProperty("display");
            } else {
                element.style.setProperty("display", "none", "important");
            }
        });
    });

    const intro = document.querySelector(".welcome-row .intro");
    if (intro && !isVet) {
        intro.textContent = "Kliniğin veteriner ekibi tarafından eklenen kayıtları görüntülüyorsun.";
    }
}

function createTreatmentMedicationRow(medication = {}) {
    const row = document.createElement("div");
    row.className = "treatment-medication-row edit-treatment-medication-row";
    row.innerHTML = `
        <input class="edit-medication-id" type="number" placeholder="İlaç ID" value="${medication.medication?.id || medication.medicationId || ""}" required>
        <input class="edit-dosage" placeholder="Doz" value="${escapeHtml(medication.dosage || "")}" required>
        <input class="edit-frequency" placeholder="Sıklık" value="${escapeHtml(medication.frequency || "")}" required>
        <button type="button" class="table-action remove-edit-medication">Sil</button>
    `;
    return row;
}

function openTreatmentEdit(treatment) {
    document.getElementById("edit-treatment-id").value = treatment.id;
    document.getElementById("edit-treatment-pet-id").value = treatment.pet?.id || "";
    document.getElementById("edit-treatment-disease-ids").value = (treatment.diseases || [])
        .map(disease => disease.id)
        .join(",");
    document.getElementById("edit-treatment-start-date").value = treatment.startDate || "";
    document.getElementById("edit-treatment-end-date").value = treatment.endDate || "";
    document.getElementById("edit-treatment-next-checkup").value = treatment.nextCheckupAt
        ? treatment.nextCheckupAt.slice(0, 16)
        : "";

    const medicationsContainer = document.getElementById("edit-treatment-medications");
    medicationsContainer.innerHTML = "";
    (treatment.treatmentMedications || []).forEach(medication => {
        medicationsContainer.appendChild(createTreatmentMedicationRow(medication));
    });

    document.getElementById("treatment-edit-form").classList.add("visible-form");
    document.getElementById("treatment-edit-form").scrollIntoView({ behavior: "smooth" });
}

function getEditedTreatmentMedications() {
    return Array.from(document.querySelectorAll("#edit-treatment-medications .edit-treatment-medication-row"))
        .map(row => ({
            medicationId: Number(row.querySelector(".edit-medication-id").value),
            dosage: row.querySelector(".edit-dosage").value,
            frequency: row.querySelector(".edit-frequency").value
        }));
}

function renderCalendar(treatments) {
    const today = new Date();
    const day = document.getElementById("calendar-day");
    const month = document.getElementById("calendar-month");
    const weekday = document.getElementById("calendar-weekday");
    const reminders = document.getElementById("calendar-reminders");

    if (!day || !month || !weekday || !reminders) {
        return;
    }

    day.textContent = today.getDate();
    month.textContent = today.toLocaleDateString("tr-TR", { month: "long" });
    weekday.textContent = today.toLocaleDateString("tr-TR", { weekday: "long" });

    const upcomingTreatments = treatments
        .filter(treatment => treatment.nextCheckupAt)
        .map(treatment => ({
            treatment,
            date: new Date(treatment.nextCheckupAt)
        }))
        .filter(item => item.date >= today)
        .sort((first, second) => first.date - second.date)
        .slice(0, 5);

    if (upcomingTreatments.length === 0) {
        reminders.innerHTML = '<p class="empty-state">Yaklaşan kontrol yok.</p>';
        return;
    }

    reminders.innerHTML = upcomingTreatments.map(({ treatment, date }, index) => {
        const petName = treatment.pet?.name || "Bilinmeyen hasta";
        const time = date.toLocaleTimeString("tr-TR", {
            hour: "2-digit",
            minute: "2-digit"
        });
        const dateText = date.toLocaleDateString("tr-TR", {
            day: "2-digit",
            month: "2-digit"
        });
        const appointmentClass = index % 2 === 0 ? "" : "green-appointment";

        return `
            <div class="calendar-line">
                <span class="time">${dateText}<br>${time}</span>
                <div class="appointment ${appointmentClass}">
                    <strong>${escapeHtml(petName)} kontrolü</strong>
                    <span>Tedavi kontrolü</span>
                </div>
            </div>
        `;
    }).join("");
}

function renderManagementTables(owners, pets, medications, clinics, veterinarians) {
    const canManage = currentUser?.role === "VET" || currentUser?.role === "ADMIN";
    const ownersBody = document.getElementById("owners-body");
    if (ownersBody) {
        ownersBody.innerHTML = owners.map(owner => `
            <tr>
                <td>${escapeHtml(owner.firstName)} ${escapeHtml(owner.lastName)}</td>
                <td>${escapeHtml(owner.email)}</td>
                <td>${escapeHtml(owner.phoneNumber)}</td>
                <td>${escapeHtml(owner.clinic?.name || "-")}</td>
                <td>${owner.active ? "Aktif" : "Pasif"}</td>
                <td>${canManage ? `${actionButton("edit-owner", owner.id, "Düzenle")}
                    ${owner.active ? actionButton("deactivate-owner", owner.id, "Pasifleştir") : actionButton("activate-owner", owner.id, "Aktifleştir")}` : "Görüntüleme"}</td>
            </tr>
        `).join("") || `<tr><td colspan="6" class="empty-state">Kayıt yok.</td></tr>`;
    }

    const petsBody = document.getElementById("pets-body");
    if (petsBody) {
        petsBody.innerHTML = pets.map(pet => `
            <tr>
                <td>${escapeHtml(pet.name)}</td>
                <td>${escapeHtml(pet.species)} / ${escapeHtml(pet.breed || "-")}</td>
                <td>${pet.ageYears ?? 0} yıl ${pet.ageMonths ?? 0} ay</td>
                <td>${pet.weight ?? "-"} kg</td>
                <td>${pet.height ?? "-"} cm</td>
                <td>${pet.neutered ? "Evet" : "Hayır"}</td>
                <td>${escapeHtml(pet.owner?.firstName || "-")} ${escapeHtml(pet.owner?.lastName || "")}</td>
                <td>${canManage ? `${actionButton("edit-pet", pet.id, "Düzenle")} ${actionButton("delete-pet", pet.id, "Sil")}` : "Görüntüleme"}</td>
            </tr>
        `).join("") || `<tr><td colspan="8" class="empty-state">Kayıt yok.</td></tr>`;
    }

    const medicationsBody = document.getElementById("medications-body");
    if (medicationsBody) {
        medicationsBody.innerHTML = medications.map(medication => `
            <tr>
                <td>${escapeHtml(medication.name)}</td>
                <td>${escapeHtml(medication.createdAt || "-")}</td>
                <td>${canManage ? `${actionButton("edit-medication", medication.id, "Düzenle")} ${actionButton("delete-medication", medication.id, "Sil")}` : "Görüntüleme"}</td>
            </tr>
        `).join("") || `<tr><td colspan="3" class="empty-state">Kayıt yok.</td></tr>`;
    }

    const diseasesBody = document.getElementById("diseases-body");
    if (diseasesBody) {
        diseasesBody.innerHTML = (dashboardData.diseases || []).map(disease => `
            <tr>
                <td>${escapeHtml(disease.name)}</td>
                <td>${actionButton("edit-disease", disease.id, "Düzenle")} ${actionButton("delete-disease", disease.id, "Sil")}</td>
            </tr>
        `).join("") || `<tr><td colspan="2" class="empty-state">Kayıt yok.</td></tr>`;
    }

    const clinicsBody = document.getElementById("clinics-body");
    if (clinicsBody) {
        clinicsBody.innerHTML = clinics.map(clinic => `
            <tr>
                <td>${escapeHtml(clinic.name)}</td>
                <td>${escapeHtml(clinic.address || "-")}</td>
                <td>${escapeHtml(clinic.phoneNumber)}</td>
                <td>${actionButton("edit-clinic", clinic.id, "Düzenle")} ${actionButton("delete-clinic", clinic.id, "Sil")}</td>
            </tr>
        `).join("") || `<tr><td colspan="4" class="empty-state">Kayıt yok.</td></tr>`;
    }

    const veterinariansBody = document.getElementById("veterinarians-body");
    if (veterinariansBody) {
        veterinariansBody.innerHTML = veterinarians.map(veterinarian => `
            <tr>
                <td>${escapeHtml(veterinarian.firstName)} ${escapeHtml(veterinarian.lastName)}</td>
                <td>${escapeHtml(veterinarian.email)}</td>
                <td>${escapeHtml(veterinarian.clinic?.name || "-")}</td>
                <td>${actionButton("edit-veterinarian", veterinarian.id, "Düzenle")} ${actionButton("delete-veterinarian", veterinarian.id, "Sil")}</td>
            </tr>
        `).join("") || `<tr><td colspan="4" class="empty-state">Kayıt yok.</td></tr>`;
    }
}

async function updateOwner(owner) {
    const firstName = prompt("Ad", owner.firstName);
    const lastName = prompt("Soyad", owner.lastName);
    const email = prompt("E-posta", owner.email);
    const phoneNumber = prompt("Telefon", owner.phoneNumber);
    if (!firstName || !lastName || !email || !phoneNumber) return;

    await request(`/api/owners/${owner.id}`, {
        method: "PUT",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify({
            firstName, lastName, email, phoneNumber,
            clinicId: owner.clinic.id
        })
    });
}

async function updatePet(pet) {
    const name = prompt("Hayvan adı", pet.name);
    const species = prompt("Tür", pet.species);
    const breed = prompt("Irk", pet.breed || "");
    if (!name || !species) return;

    await request(`/api/pets/${pet.id}`, {
        method: "PUT",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify({
            name, species, breed,
            ageYears: pet.ageYears,
            ageMonths: pet.ageMonths,
            weight: pet.weight,
            height: pet.height,
            neutered: pet.neutered,
            ownerId: pet.owner.id
        })
    });
}

async function updateMedication(medication) {
    const name = prompt("İlaç adı", medication.name);
    if (!name) return;

    await request(`/api/medications/${medication.id}`, {
        method: "PUT",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify({ name })
    });
}

async function updateDisease(disease) {
    const name = prompt("Hastalık adı", disease.name);
    if (!name) return;

    await request(`/api/diseases/${disease.id}`, {
        method: "PUT",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify({ name })
    });
}

async function updateClinic(clinic) {
    const name = prompt("Klinik adı", clinic.name);
    const address = prompt("Adres", clinic.address || "");
    const phoneNumber = prompt("Telefon", clinic.phoneNumber);
    if (!name || !phoneNumber) return;

    await request(`/api/clinics/${clinic.id}`, {
        method: "PUT",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify({ name, address, phoneNumber })
    });
}

async function updateVeterinarian(veterinarian) {
    const firstName = prompt("Ad", veterinarian.firstName);
    const lastName = prompt("Soyad", veterinarian.lastName);
    const email = prompt("E-posta", veterinarian.email);
    const phoneNumber = prompt("Telefon", veterinarian.phoneNumber);
    if (!firstName || !lastName || !email || !phoneNumber) return;

    await request(`/api/veterinarians/${veterinarian.id}`, {
        method: "PUT",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify({
            firstName, lastName, email, phoneNumber,
            clinicId: veterinarian.clinic.id
        })
    });
}

async function handleManagementAction(action, id, data) {
    const item = data.find(value => value.id === Number(id));
    if (!item) return;

    if (action === "edit-owner") await updateOwner(item);
    if (action === "edit-pet") await updatePet(item);
    if (action === "edit-medication") await updateMedication(item);
    if (action === "edit-disease") await updateDisease(item);
    if (action === "edit-clinic") await updateClinic(item);
    if (action === "edit-veterinarian") await updateVeterinarian(item);
    if (action === "edit-treatment") {
        openTreatmentEdit(item);
        return;
    }

    if (action === "deactivate-owner" || action === "activate-owner") {
        const endpoint = action === "deactivate-owner" ? "" : "/activate";
        await request(`/api/owners/${id}${endpoint}`, { method: action === "deactivate-owner" ? "DELETE" : "PATCH" });
    }

    if (["delete-pet", "delete-medication", "delete-disease", "delete-clinic", "delete-veterinarian"].includes(action)) {
        const endpoint = {
            "delete-pet": "pets", "delete-medication": "medications",
            "delete-disease": "diseases",
            "delete-clinic": "clinics", "delete-veterinarian": "veterinarians"
        }[action];
        if (confirm("Bu kaydı silmek istediğinize emin misiniz?")) {
            await request(`/api/${endpoint}/${id}`, { method: "DELETE" });
        }
    }

    if (["complete-treatment", "cancel-treatment", "reactivate-treatment", "delete-treatment"].includes(action)) {
        const actionName = action.replace("-treatment", "");
        const method = actionName === "delete" ? "DELETE" : "PATCH";
        const suffix = method === "PATCH" ? `/${actionName}` : "";
        await request(`/api/treatments/${id}${suffix}`, { method });
    }
}

async function loadDashboard() {
    const [owners, pets, medications, treatments, clinics, veterinarians, diseases] = await Promise.all([
        request("/api/owners"),
        request("/api/pets"),
        request("/api/medications"),
        request("/api/treatments"),
        request("/api/clinics"),
        request("/api/veterinarians"),
        request("/api/diseases")
    ]);

    fillClinicSelect("owner-clinic-id", clinics);
    fillClinicSelect("veterinarian-clinic-id", clinics);
    dashboardData = { owners, pets, medications, diseases, treatments, clinics, veterinarians };
    renderManagementTables(owners, pets, medications, clinics, veterinarians);
    renderCalendar(treatments);

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
        const medications = treatment.treatmentMedications || [];
        const medicationName = medications.length === 0
            ? "-"
            : medications.map(item => item.medication?.name || "-").join(", ");
        const dosage = medications.length === 0
            ? "-"
            : medications.map(item => `${item.dosage} · ${item.frequency}`).join(", ");

        const row = document.createElement("tr");
        const treatmentActions = currentUser?.role === "VET" || currentUser?.role === "ADMIN"
            ? `${actionButton("edit-treatment", treatment.id, "Düzenle")}
                ${isActive ? actionButton("complete-treatment", treatment.id, "Bitir") : actionButton("reactivate-treatment", treatment.id, "Aktifleştir")}
                ${actionButton("cancel-treatment", treatment.id, "İptal")}
                ${actionButton("delete-treatment", treatment.id, "Sil")}`
            : "Görüntüleme";
        row.innerHTML = `
            <td>
                <div class="patient">
                    <span class="patient-avatar sage">${escapeHtml(petName.charAt(0))}</span>
                    <div><strong>${escapeHtml(petName)}</strong><small>${escapeHtml(species)}</small></div>
                </div>
            </td>
            <td>${escapeHtml(medicationName)}</td>
            <td>${escapeHtml(dosage)}</td>
            <td><span class="status ${statusClass}">${statusText}</span></td>
            <td>
                ${treatmentActions}
            </td>
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

function renderOwnerSearchResults(owners) {
    const results = document.getElementById("owner-search-results");

    if (owners.length === 0) {
        results.textContent = "Eslesen sahip bulunamadi.";
        return;
    }

    results.innerHTML = owners.map(owner => `
        <div class="search-result">
            <strong>${escapeHtml(owner.firstName)} ${escapeHtml(owner.lastName)}</strong>
            <small>${escapeHtml(owner.email)} · ${owner.active ? "Aktif" : "Pasif"}</small>
        </div>
    `).join("");
}

function renderPetSearchResults(pets) {
    const results = document.getElementById("pet-search-results");

    if (pets.length === 0) {
        results.textContent = "Eslesen hayvan bulunamadi.";
        return;
    }

    results.innerHTML = pets.map(pet => `
        <div class="search-result">
            <strong>${escapeHtml(pet.name)}</strong>
            <small>${escapeHtml(pet.species)} · ${escapeHtml(pet.breed || "Irk bilgisi yok")}</small>
        </div>
    `).join("");
}

async function searchOwners() {
    const params = new URLSearchParams();
    const name = document.getElementById("owner-search-name").value.trim();
    const active = document.getElementById("owner-search-active").value;

    if (name) {
        params.set("name", name);
    }

    if (active) {
        params.set("active", active);
    }

    const owners = await request(`/api/owners/search?${params}`);
    renderOwnerSearchResults(owners);
}

async function searchPets() {
    const params = new URLSearchParams();
    const name = document.getElementById("pet-search-name").value.trim();
    const species = document.getElementById("pet-search-species").value.trim();

    if (name) {
        params.set("name", name);
    }

    if (species) {
        params.set("species", species);
    }

    const pets = await request(`/api/pets/search?${params}`);
    renderPetSearchResults(pets);
}

function attachFormHandlers() {
    if (formHandlersAttached) {
        return;
    }
    formHandlersAttached = true;

    document.getElementById("logout-button")?.addEventListener("click", logout);

    document.getElementById("create-owner-form")?.addEventListener("submit", event => {
        event.preventDefault();
        submitForm(event.target, "/api/owners", {
            firstName: document.getElementById("first-name").value,
            lastName: document.getElementById("last-name").value,
            email: document.getElementById("email").value,
            phoneNumber: document.getElementById("phone-number").value,
            clinicId: Number(document.getElementById("owner-clinic-id").value)
        }, "owner-message");
    });

    document.getElementById("create-clinic-form")?.addEventListener("submit", event => {
        event.preventDefault();
        submitForm(event.target, "/api/clinics", {
            name: document.getElementById("clinic-name").value,
            address: document.getElementById("clinic-address").value,
            phoneNumber: document.getElementById("clinic-phone-number").value
        }, "clinic-message");
    });

    document.getElementById("create-veterinarian-form")?.addEventListener("submit", event => {
        event.preventDefault();
        submitForm(event.target, "/api/veterinarians", {
            firstName: document.getElementById("veterinarian-first-name").value,
            lastName: document.getElementById("veterinarian-last-name").value,
            email: document.getElementById("veterinarian-email").value,
            phoneNumber: document.getElementById("veterinarian-phone-number").value,
            clinicId: Number(document.getElementById("veterinarian-clinic-id").value)
        }, "veterinarian-message");
    });

    document.getElementById("create-pet-form")?.addEventListener("submit", event => {
        event.preventDefault();
        submitForm(event.target, "/api/pets", {
            name: document.getElementById("pet-name").value,
            species: document.getElementById("species").value,
            breed: document.getElementById("breed").value,
            ageYears: document.getElementById("age-years").value
                ? Number(document.getElementById("age-years").value)
                : null,
            ageMonths: document.getElementById("age-months").value
                ? Number(document.getElementById("age-months").value)
                : null,
            weight: document.getElementById("weight").value
                ? Number(document.getElementById("weight").value)
                : null,
            height: document.getElementById("height").value
                ? Number(document.getElementById("height").value)
                : null,
            neutered: document.getElementById("neutered").checked,
            ownerId: Number(document.getElementById("owner-id").value)
        }, "pet-message");
    });

    document.getElementById("create-medication-form")?.addEventListener("submit", event => {
        event.preventDefault();
        submitForm(event.target, "/api/medications", {
            name: document.getElementById("medication-name").value
        }, "medication-message");
    });

    document.getElementById("create-disease-form")?.addEventListener("submit", event => {
        event.preventDefault();
        submitForm(event.target, "/api/diseases", {
            name: document.getElementById("disease-name").value
        }, "disease-message");
    });

    document.getElementById("add-plan-medication")?.addEventListener("click", () => {
        const container = document.getElementById("plan-medications");
        const row = document.createElement("div");
        row.className = "treatment-medication-row";
        row.innerHTML = `
            <input class="plan-medication-id" type="number" placeholder="Ilac ID" required>
            <input class="plan-dosage" placeholder="Doz" required>
            <input class="plan-frequency" placeholder="Siklik" required>
        `;
        container.appendChild(row);
    });

    document.getElementById("create-treatment-plan-form")?.addEventListener("submit", event => {
        event.preventDefault();

        const diseaseIds = document.getElementById("plan-disease-ids")
            .value
            .split(",")
            .map(value => Number(value.trim()))
            .filter(value => Number.isInteger(value) && value > 0);

        const medicationRows = document.querySelectorAll(
            "#plan-medications .treatment-medication-row"
        );

        const medications = Array.from(medicationRows).map(row => ({
            medicationId: Number(row.querySelector(".plan-medication-id").value),
            dosage: row.querySelector(".plan-dosage").value,
            frequency: row.querySelector(".plan-frequency").value
        }));

        submitForm(event.target, "/api/treatments", {
            petId: Number(document.getElementById("plan-pet-id").value),
            diseaseIds: diseaseIds,
            medications: medications,
            startDate: document.getElementById("plan-start-date").value,
            endDate: document.getElementById("plan-end-date").value || null,
            nextCheckupAt: document.getElementById("plan-next-checkup").value
                ? document.getElementById("plan-next-checkup").value + ":00"
                : null
        }, "treatment-plan-message");
    });

    document.getElementById("add-edit-treatment-medication")?.addEventListener("click", () => {
        document.getElementById("edit-treatment-medications")
            .appendChild(createTreatmentMedicationRow());
    });

    document.getElementById("edit-treatment-medications")?.addEventListener("click", event => {
        if (event.target.classList.contains("remove-edit-medication")) {
            event.target.closest(".edit-treatment-medication-row").remove();
        }
    });

    document.getElementById("cancel-treatment-edit")?.addEventListener("click", () => {
        document.getElementById("treatment-edit-form").classList.remove("visible-form");
    });

    document.getElementById("update-treatment-form")?.addEventListener("submit", async event => {
        event.preventDefault();
        const message = document.getElementById("treatment-edit-message");
        const treatmentId = document.getElementById("edit-treatment-id").value;
        const diseaseIds = document.getElementById("edit-treatment-disease-ids").value
            .split(",")
            .map(value => Number(value.trim()))
            .filter(value => Number.isInteger(value) && value > 0);

        try {
            await request(`/api/treatments/${treatmentId}`, {
                method: "PATCH",
                headers: { "Content-Type": "application/json" },
                body: JSON.stringify({
                    petId: Number(document.getElementById("edit-treatment-pet-id").value),
                    diseaseIds,
                    medications: getEditedTreatmentMedications(),
                    startDate: document.getElementById("edit-treatment-start-date").value,
                    endDate: document.getElementById("edit-treatment-end-date").value || null,
                    nextCheckupAt: document.getElementById("edit-treatment-next-checkup").value
                        ? document.getElementById("edit-treatment-next-checkup").value + ":00"
                        : null
                })
            });

            message.textContent = "Tedavi güncellendi.";
            document.getElementById("treatment-edit-form").classList.remove("visible-form");
            await loadDashboard();
        } catch (error) {
            message.textContent = error.message;
        }
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

    document.getElementById("owner-search-form")?.addEventListener("submit", async event => {
        event.preventDefault();
        const message = document.getElementById("owner-search-message");

        try {
            await searchOwners();
            message.textContent = "Arama tamamlandi.";
        } catch (error) {
            message.textContent = error.message;
        }
    });

    document.getElementById("pet-search-form")?.addEventListener("submit", async event => {
        event.preventDefault();
        const message = document.getElementById("pet-search-message");

        try {
            await searchPets();
            message.textContent = "Arama tamamlandi.";
        } catch (error) {
            message.textContent = error.message;
        }
    });

    document.addEventListener("click", async event => {
        const button = event.target.closest("[data-action]");
        if (!button) {
            return;
        }

        const action = button.dataset.action;
        const dataByAction = {
            "edit-owner": dashboardData.owners,
            "deactivate-owner": dashboardData.owners,
            "activate-owner": dashboardData.owners,
            "edit-pet": dashboardData.pets,
            "delete-pet": dashboardData.pets,
            "edit-medication": dashboardData.medications,
            "delete-medication": dashboardData.medications,
            "edit-disease": dashboardData.diseases,
            "delete-disease": dashboardData.diseases,
            "edit-clinic": dashboardData.clinics,
            "delete-clinic": dashboardData.clinics,
            "edit-veterinarian": dashboardData.veterinarians,
            "delete-veterinarian": dashboardData.veterinarians,
            "edit-treatment": dashboardData.treatments,
            "complete-treatment": dashboardData.treatments,
            "cancel-treatment": dashboardData.treatments,
            "reactivate-treatment": dashboardData.treatments,
            "delete-treatment": dashboardData.treatments
        };

        try {
            await handleManagementAction(action, button.dataset.id, dataByAction[action] || []);
            await loadDashboard();
        } catch (error) {
            alert(error.message);
        }
    });

    document.getElementById("open-owner-form")?.addEventListener("click", () => {
        showPage("owners");
    });
}

async function initDashboard() {
    setupPageNavigation();
    attachFormHandlers();
    try {
        await loadCurrentUser();
        applyRoleBasedInterface();
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
