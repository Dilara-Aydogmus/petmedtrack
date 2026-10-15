let auth = sessionStorage.getItem("petmedtrack-auth");
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

function actionButton(action, id, label) {
    return `<button type="button" class="table-action" data-action="${action}" data-id="${id}">${label}</button>`;
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

function renderManagementTables(owners, pets, medications, clinics, veterinarians) {
    const ownersBody = document.getElementById("owners-body");
    if (ownersBody) {
        ownersBody.innerHTML = owners.map(owner => `
            <tr>
                <td>${escapeHtml(owner.firstName)} ${escapeHtml(owner.lastName)}</td>
                <td>${escapeHtml(owner.email)}</td>
                <td>${escapeHtml(owner.phoneNumber)}</td>
                <td>${escapeHtml(owner.clinic?.name || "-")}</td>
                <td>${owner.active ? "Aktif" : "Pasif"}</td>
                <td>${actionButton("edit-owner", owner.id, "Düzenle")}
                    ${owner.active ? actionButton("deactivate-owner", owner.id, "Pasifleştir") : actionButton("activate-owner", owner.id, "Aktifleştir")}</td>
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
                <td>${escapeHtml(pet.owner?.firstName || "-")} ${escapeHtml(pet.owner?.lastName || "")}</td>
                <td>${actionButton("edit-pet", pet.id, "Düzenle")} ${actionButton("delete-pet", pet.id, "Sil")}</td>
            </tr>
        `).join("") || `<tr><td colspan="5" class="empty-state">Kayıt yok.</td></tr>`;
    }

    const medicationsBody = document.getElementById("medications-body");
    if (medicationsBody) {
        medicationsBody.innerHTML = medications.map(medication => `
            <tr>
                <td>${escapeHtml(medication.name)}</td>
                <td>${escapeHtml(medication.createdAt || "-")}</td>
                <td>${actionButton("edit-medication", medication.id, "Düzenle")} ${actionButton("delete-medication", medication.id, "Sil")}</td>
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
                ${actionButton("edit-treatment", treatment.id, "Düzenle")}
                ${isActive ? actionButton("complete-treatment", treatment.id, "Bitir") : actionButton("reactivate-treatment", treatment.id, "Aktifleştir")}
                ${actionButton("cancel-treatment", treatment.id, "İptal")}
                ${actionButton("delete-treatment", treatment.id, "Sil")}
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
