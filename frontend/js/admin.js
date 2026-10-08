/**
 * Government Scheme Recommendation System - Admin Command Center JS
 * Handles Admin Stats, Users Management, Scheme CRUD, and Rule Builder.
 */

document.addEventListener('DOMContentLoaded', () => {
    if (!isAdmin()) {
        showToast('Admin access required', 'error');
        setTimeout(() => { window.location.href = 'login.html'; }, 1000);
        return;
    }

    loadAdminStats();
    loadUsersTable();
    loadAdminSchemesTable();
    loadCategoriesDropdown();
    setupAdminFormListeners();
});

async function loadAdminStats() {
    try {
        const stats = await apiFetch('/admin/stats');
        document.getElementById('stat-total-users').innerText = stats.totalUsers || 0;
        document.getElementById('stat-active-schemes').innerText = stats.activeSchemes || 0;
        document.getElementById('stat-total-categories').innerText = stats.totalCategories || 0;
        document.getElementById('stat-total-rules').innerText = stats.totalEligibilityRules || 0;
    } catch (err) {
        console.error('Failed to load admin stats:', err);
    }
}

async function loadUsersTable() {
    const tbody = document.getElementById('admin-users-table-body');
    if (!tbody) return;

    try {
        const users = await apiFetch('/admin/users');
        if (!users || users.length === 0) {
            tbody.innerHTML = `<tr><td colspan="6" class="text-center py-4 text-muted">No users registered yet.</td></tr>`;
            return;
        }

        tbody.innerHTML = users.map(u => `
            <tr>
                <td class="fw-bold">#${u.id}</td>
                <td>
                    <div class="fw-bold">${u.name}</div>
                    <div class="small text-muted">${u.email}</div>
                </td>
                <td>${u.mobile}</td>
                <td><span class="badge ${u.role === 'ROLE_ADMIN' ? 'bg-purple' : 'bg-secondary'}">${u.role.replace('ROLE_', '')}</span></td>
                <td>
                    <span class="badge ${u.status === 'ACTIVE' ? 'bg-success' : 'bg-danger'}">${u.status}</span>
                </td>
                <td>
                    ${u.role !== 'ROLE_ADMIN' ? `
                        <button class="btn btn-sm ${u.status === 'ACTIVE' ? 'btn-outline-danger' : 'btn-outline-success'}" onclick="toggleUserStatus(${u.id}, '${u.status === 'ACTIVE' ? 'INACTIVE' : 'ACTIVE'}')">
                            ${u.status === 'ACTIVE' ? '<i class="bi bi-person-x me-1"></i> Deactivate' : '<i class="bi bi-person-check me-1"></i> Activate'}
                        </button>
                    ` : '<span class="text-muted small">System Admin</span>'}
                </td>
            </tr>
        `).join('');
    } catch (err) {
        tbody.innerHTML = `<tr><td colspan="6" class="text-center text-danger py-3">${err.message}</td></tr>`;
    }
}

async function toggleUserStatus(userId, newStatus) {
    try {
        await apiFetch(`/admin/users/${userId}/status?status=${newStatus}`, { method: 'PUT' });
        showToast(`User status changed to ${newStatus}`, 'success');
        loadUsersTable();
        loadAdminStats();
    } catch (err) {
        showToast(err.message || 'Status update failed', 'error');
    }
}

async function loadAdminSchemesTable() {
    const tbody = document.getElementById('admin-schemes-table-body');
    if (!tbody) return;

    try {
        const schemes = await apiFetch('/admin/schemes');
        if (!schemes || schemes.length === 0) {
            tbody.innerHTML = `<tr><td colspan="6" class="text-center py-4 text-muted">No schemes added yet. Click "Add New Scheme" to create one.</td></tr>`;
            return;
        }

        tbody.innerHTML = schemes.map(s => `
            <tr>
                <td class="fw-bold">#${s.id}</td>
                <td>
                    <div class="fw-bold text-primary">${s.schemeName}</div>
                    <div class="small text-muted">${s.department}</div>
                </td>
                <td><span class="badge-category">${s.categoryName}</span></td>
                <td><span class="${s.schemeType === 'STATE' ? 'badge-state' : 'badge-central'}">${s.schemeType}</span></td>
                <td>
                    ${s.eligibilityRule ? `
                        <span class="badge bg-success-subtle text-success border border-success px-2 py-1 small">
                            <i class="bi bi-check-circle me-1"></i> Rule Configured
                        </span>
                    ` : `
                        <span class="badge bg-warning-subtle text-warning border border-warning px-2 py-1 small">
                            <i class="bi bi-exclamation-circle me-1"></i> No Custom Rule
                        </span>
                    `}
                </td>
                <td>
                    <div class="btn-group btn-group-sm">
                        <button class="btn btn-outline-primary" onclick="openRuleBuilderModal(${s.id}, '${encodeURIComponent(s.schemeName)}')">
                            <i class="bi bi-gear-fill me-1"></i> Config Rule
                        </button>
                        <button class="btn btn-outline-danger" onclick="deleteScheme(${s.id})">
                            <i class="bi bi-trash-fill"></i>
                        </button>
                    </div>
                </td>
            </tr>
        `).join('');
    } catch (err) {
        tbody.innerHTML = `<tr><td colspan="6" class="text-center text-danger py-3">${err.message}</td></tr>`;
    }
}

async function loadCategoriesDropdown() {
    try {
        const categories = await apiFetch('/schemes/categories');
        const select = document.getElementById('schemeCategoryId');
        if (select) {
            select.innerHTML = `<option value="">Select Category</option>` +
                categories.map(c => `<option value="${c.id}">${c.name}</option>`).join('');
        }
    } catch (err) {
        console.error('Failed to load categories', err);
    }
}

function setupAdminFormListeners() {
    const schemeForm = document.getElementById('createSchemeForm');
    if (schemeForm) {
        schemeForm.addEventListener('submit', async (e) => {
            e.preventDefault();
            const categoryId = parseInt(document.getElementById('schemeCategoryId').value, 10);
            const schemeName = document.getElementById('schemeName').value.trim();
            const department = document.getElementById('schemeDepartment').value.trim();
            const schemeType = document.getElementById('schemeType').value;
            const shortDescription = document.getElementById('schemeShortDesc').value.trim();
            const benefits = document.getElementById('schemeBenefits').value.trim();
            const applicationUrl = document.getElementById('schemeAppUrl').value.trim();

            try {
                await apiFetch('/admin/schemes', {
                    method: 'POST',
                    body: JSON.stringify({
                        categoryId,
                        schemeName,
                        department,
                        schemeType,
                        shortDescription,
                        benefits,
                        applicationUrl,
                        description: shortDescription,
                        contactInformation: 'Contact Department helpline'
                    })
                });

                showToast('New Scheme Created Successfully!', 'success');
                const modal = bootstrap.Modal.getInstance(document.getElementById('addSchemeModal'));
                if (modal) modal.hide();
                schemeForm.reset();
                loadAdminSchemesTable();
                loadAdminStats();
            } catch (err) {
                showToast(err.message || 'Failed to create scheme', 'error');
            }
        });
    }

    const ruleForm = document.getElementById('ruleBuilderForm');
    if (ruleForm) {
        ruleForm.addEventListener('submit', async (e) => {
            e.preventDefault();
            const schemeId = document.getElementById('ruleSchemeId').value;
            const minAgeVal = document.getElementById('ruleMinAge').value;
            const maxAgeVal = document.getElementById('ruleMaxAge').value;
            const maxIncomeVal = document.getElementById('ruleMaxIncome').value;

            const payload = {
                minAge: minAgeVal ? parseInt(minAgeVal, 10) : null,
                maxAge: maxAgeVal ? parseInt(maxAgeVal, 10) : null,
                maxIncome: maxIncomeVal ? parseFloat(maxIncomeVal) : null,
                gender: document.getElementById('ruleGender').value || 'ALL',
                state: document.getElementById('ruleState').value || 'ALL',
                occupation: document.getElementById('ruleOccupation').value || 'ALL',
                category: document.getElementById('ruleCategory').value || 'ALL',
                studentStatus: document.getElementById('ruleStudentStatus').checked ? true : null,
                disabilityStatus: document.getElementById('ruleDisabilityStatus').checked ? true : null,
                farmerStatus: document.getElementById('ruleFarmerStatus').checked ? true : null,
                entrepreneurStatus: document.getElementById('ruleEntrepreneurStatus').checked ? true : null
            };

            try {
                await apiFetch(`/admin/schemes/${schemeId}/rule`, {
                    method: 'POST',
                    body: JSON.stringify(payload)
                });

                showToast('Eligibility Rule Configured Successfully!', 'success');
                const modal = bootstrap.Modal.getInstance(document.getElementById('ruleBuilderModal'));
                if (modal) modal.hide();
                loadAdminSchemesTable();
                loadAdminStats();
            } catch (err) {
                showToast(err.message || 'Failed to save rule', 'error');
            }
        });
    }
}

function openRuleBuilderModal(schemeId, schemeName) {
    document.getElementById('ruleSchemeId').value = schemeId;
    document.getElementById('ruleSchemeTitle').innerText = decodeURIComponent(schemeName);

    // Fetch existing scheme rule if any
    apiFetch(`/schemes/${schemeId}`).then(scheme => {
        if (scheme && scheme.eligibilityRule) {
            const r = scheme.eligibilityRule;
            document.getElementById('ruleMinAge').value = r.minAge || '';
            document.getElementById('ruleMaxAge').value = r.maxAge || '';
            document.getElementById('ruleMaxIncome').value = r.maxIncome || '';
            document.getElementById('ruleGender').value = r.gender || 'ALL';
            document.getElementById('ruleState').value = r.state || 'ALL';
            document.getElementById('ruleOccupation').value = r.occupation || 'ALL';
            document.getElementById('ruleCategory').value = r.category || 'ALL';
            document.getElementById('ruleStudentStatus').checked = Boolean(r.studentStatus);
            document.getElementById('ruleDisabilityStatus').checked = Boolean(r.disabilityStatus);
            document.getElementById('ruleFarmerStatus').checked = Boolean(r.farmerStatus);
            document.getElementById('ruleEntrepreneurStatus').checked = Boolean(r.entrepreneurStatus);
        } else {
            document.getElementById('ruleBuilderForm').reset();
            document.getElementById('ruleSchemeId').value = schemeId;
        }

        const modal = new bootstrap.Modal(document.getElementById('ruleBuilderModal'));
        modal.show();
    }).catch(err => {
        showToast('Error opening rule builder: ' + err.message, 'error');
    });
}

async function deleteScheme(id) {
    if (!confirm('Are you sure you want to delete this scheme?')) return;
    try {
        await apiFetch(`/admin/schemes/${id}`, { method: 'DELETE' });
        showToast('Scheme deleted successfully', 'success');
        loadAdminSchemesTable();
        loadAdminStats();
    } catch (err) {
        showToast(err.message || 'Delete failed', 'error');
    }
}
