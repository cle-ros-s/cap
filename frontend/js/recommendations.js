/**
 * Safely escapes HTML strings to prevent XSS and rendering errors
 */
function escapeHtml(text) {
    if (text === null || text === undefined) return '';
    return String(text)
        .replace(/&/g, '&amp;')
        .replace(/</g, '&lt;')
        .replace(/>/g, '&gt;')
        .replace(/"/g, '&quot;')
        .replace(/'/g, '&#039;');
}

document.addEventListener('DOMContentLoaded', async () => {
    const profileForm = document.getElementById('eligibilityForm');
    if (!profileForm) return;

    const urlParams = new URLSearchParams(window.location.search);
    const paramState = urlParams.get('state');
    if (paramState && document.getElementById('state')) {
        document.getElementById('state').value = paramState;
    }

    // Load saved user profile if logged in
    if (isLoggedIn()) {
        try {
            const profile = await apiFetch('/user/profile');
            populateFormFields(profile);
            if (paramState && document.getElementById('state')) {
                document.getElementById('state').value = paramState;
            }
            // Auto run recommendation on load if profile is complete
            if (profile.age || profile.annualIncome || profile.state) {
                runRecommendationEngine(getProfilePayloadFromForm());
            }
        } catch (err) {
            console.log('User profile fetch notice:', err.message);
        }
    } else if (paramState) {
        // If state is preselected from URL, pre-fill and run if user has minimal default
        // or wait for submit
    }

    profileForm.addEventListener('submit', (e) => {
        e.preventDefault();
        const payload = getProfilePayloadFromForm();
        runRecommendationEngine(payload);
    });
});

function populateFormFields(profile) {
    if (profile.age) document.getElementById('age').value = profile.age;
    if (profile.gender) document.getElementById('gender').value = profile.gender;
    if (profile.state) document.getElementById('state').value = profile.state;
    if (profile.district) document.getElementById('district').value = profile.district;
    if (profile.annualIncome) document.getElementById('annualIncome').value = profile.annualIncome;
    if (profile.occupation) document.getElementById('occupation').value = profile.occupation;
    if (profile.employmentStatus) document.getElementById('employmentStatus').value = profile.employmentStatus;
    if (profile.category) document.getElementById('category').value = profile.category;

    document.getElementById('studentStatus').checked = !!profile.studentStatus;
    document.getElementById('disabilityStatus').checked = !!profile.disabilityStatus;
    document.getElementById('farmerStatus').checked = !!profile.farmerStatus;
    document.getElementById('entrepreneurStatus').checked = !!profile.entrepreneurStatus;
}

function getProfilePayloadFromForm() {
    const ageVal = document.getElementById('age').value;
    const incomeVal = document.getElementById('annualIncome').value;

    return {
        age: ageVal ? parseInt(ageVal, 10) : null,
        gender: document.getElementById('gender').value || null,
        state: document.getElementById('state').value || null,
        district: document.getElementById('district').value.trim() || null,
        annualIncome: incomeVal ? parseFloat(incomeVal) : null,
        occupation: document.getElementById('occupation').value || null,
        employmentStatus: document.getElementById('employmentStatus').value || null,
        category: document.getElementById('category').value || null,
        studentStatus: document.getElementById('studentStatus').checked,
        disabilityStatus: document.getElementById('disabilityStatus').checked,
        farmerStatus: document.getElementById('farmerStatus').checked,
        entrepreneurStatus: document.getElementById('entrepreneurStatus').checked
    };
}

async function runRecommendationEngine(payload) {
    const resultsContainer = document.getElementById('recommendation-results');
    if (!resultsContainer) return;

    resultsContainer.innerHTML = `
        <div class="text-center py-5">
            <div class="spinner-border text-primary" style="width: 3rem; height: 3rem;" role="status">
                <span class="visually-hidden">Evaluating rules...</span>
            </div>
            <h5 class="mt-3 text-muted fw-semibold">Evaluating Predefined Rules Engine against your profile...</h5>
            <p class="text-secondary small">Transparent, deterministic rule-matching in progress.</p>
        </div>
    `;

    try {
        const results = await apiFetch('/recommendations', {
            method: 'POST',
            body: JSON.stringify(payload)
        });

        renderRecommendationResults(results);
    } catch (err) {
        resultsContainer.innerHTML = `
            <div class="alert alert-danger p-4 rounded-3 text-center">
                <i class="bi bi-exclamation-triangle-fill fs-2 d-block mb-2"></i>
                <h5 class="fw-bold">Evaluation Failed</h5>
                <p>${err.message || 'Unable to execute recommendation engine. Please check backend status.'}</p>
            </div>
        `;
    }
}

function renderRecommendationResults(results) {
    const container = document.getElementById('recommendation-results');
    if (!results || results.length === 0) {
        container.innerHTML = `
            <div class="alert alert-info text-center p-4 rounded-3">
                <i class="bi bi-info-circle fs-2 d-block mb-2"></i>
                <h5 class="fw-bold">No Active Schemes Found</h5>
                <p>No schemes are currently available in the database.</p>
            </div>
        `;
        return;
    }

    const eligibleSchemes = results.filter(r => r.eligible);
    const ineligibleSchemes = results.filter(r => !r.eligible);

    const selectedState = document.getElementById('state') ? document.getElementById('state').value : '';
    const stateScopeHtml = (selectedState && selectedState !== 'ALL') ? `
        <div class="alert alert-primary bg-primary bg-opacity-10 border-primary border-opacity-25 p-3 rounded-3 mb-4 d-flex align-items-center gap-3">
            <div class="bg-primary text-white p-2 rounded-circle"><i class="bi bi-geo-alt-fill fs-5"></i></div>
            <div>
                <h6 class="fw-bold mb-1 text-primary">State Scope: Central Government + ${escapeHtml(selectedState)} State Schemes</h6>
                <p class="mb-0 text-secondary small">Evaluated all national Central Government welfare programs plus schemes exclusive to residents of <strong>${escapeHtml(selectedState)}</strong>. Schemes from other states are excluded.</p>
            </div>
        </div>
    ` : '';

    let html = `
        ${stateScopeHtml}
        <div class="d-flex align-items-center justify-content-between mb-4 flex-wrap gap-2">
            <div>
                <h3 class="fw-bold m-0">Recommendation Engine Output</h3>
                <p class="text-muted m-0 small">Evaluated ${results.length} total schemes based on deterministic database eligibility rules.</p>
            </div>
            <div class="d-flex gap-2">
                <span class="badge bg-success fs-6 px-3 py-2 rounded-pill"><i class="bi bi-check-circle-fill me-1"></i> ${eligibleSchemes.length} Eligible</span>
                <span class="badge bg-secondary fs-6 px-3 py-2 rounded-pill"><i class="bi bi-x-circle-fill me-1"></i> ${ineligibleSchemes.length} Ineligible</span>
            </div>
        </div>
    `;

    // Render Eligible Schemes first
    if (eligibleSchemes.length > 0) {
        html += `<h4 class="fw-bold text-success mb-3"><i class="bi bi-award-fill me-2"></i>Fully Eligible Schemes (${eligibleSchemes.length})</h4>`;
        html += `<div class="row g-4 mb-5">`;
        eligibleSchemes.forEach(item => {
            html += renderSchemeEvaluationCard(item);
        });
        html += `</div>`;
    }

    // Render Ineligible / Partially Matching Schemes
    if (ineligibleSchemes.length > 0) {
        html += `<h4 class="fw-bold text-secondary mb-3"><i class="bi bi-slash-circle me-2"></i>Not Eligible / Partially Matching Schemes (${ineligibleSchemes.length})</h4>`;
        html += `<div class="row g-4 mb-4">`;
        ineligibleSchemes.forEach(item => {
            html += renderSchemeEvaluationCard(item);
        });
        html += `</div>`;
    }

    container.innerHTML = html;
}

function renderSchemeEvaluationCard(item) {
    const scheme = item.scheme;
    const isEligible = item.eligible;
    const cardClass = isEligible ? 'result-card-eligible' : 'result-card-ineligible';
    const badgeClass = isEligible ? 'match-score-eligible' : 'match-score-ineligible';

    const isStateScheme = scheme.schemeType === 'STATE';
    const stateLabel = scheme.state && scheme.state !== 'ALL' ? scheme.state : (isStateScheme ? 'State Govt' : 'Central Govt');
    const badgeHtml = isStateScheme ?
        `<span class="badge bg-warning text-dark px-2 py-1 ms-2"><i class="bi bi-geo-alt-fill me-1"></i>${escapeHtml(stateLabel)} State Govt</span>` :
        `<span class="badge bg-primary px-2 py-1 ms-2"><i class="bi bi-flag-fill me-1"></i>Central Govt Scheme</span>`;

    let criteriaHtml = '';
    item.criteriaBreakdown.forEach(c => {
        const passClass = c.satisfied ? 'criterion-pass' : 'criterion-fail';
        const iconClass = c.satisfied ? 'bi-check-circle-fill' : 'bi-x-circle-fill';

        criteriaHtml += `
            <div class="criterion-item ${passClass}">
                <i class="bi ${iconClass} criterion-icon"></i>
                <div class="flex-grow-1">
                    <div class="d-flex justify-content-between font-weight-bold fw-bold">
                        <span>${escapeHtml(c.criterionName)}</span>
                        <span class="small opacity-75">Required: ${escapeHtml(c.expectedValue)}</span>
                    </div>
                    <div class="mt-1 small">${escapeHtml(c.explanation)}</div>
                </div>
            </div>
        `;
    });

    const isSaved = scheme.isSaved;

    return `
        <div class="col-12">
            <div class="card gov-card ${cardClass} shadow-sm">
                <div class="card-body p-4">
                    <div class="d-flex justify-content-between align-items-start gap-3 mb-3 flex-wrap">
                        <div>
                            <span class="badge-category mb-2">${escapeHtml(scheme.categoryName)}</span>
                            ${badgeHtml}
                            <h4 class="fw-bold mt-2 mb-2">${escapeHtml(scheme.schemeName)}</h4>
                            <p class="text-muted small mb-0"><i class="bi bi-building me-1"></i> ${escapeHtml(scheme.department)}</p>
                        </div>
                        <div class="text-end">
                            <span class="match-score-badge ${badgeClass} d-inline-block">
                                ${isEligible ? '100% ELIGIBLE' : `${item.matchPercentage}% MATCH (${item.satisfiedCriteriaCount}/${item.totalEvaluatedCriteriaCount} Criteria)`}
                            </span>
                        </div>
                    </div>

                    <p class="text-dark mb-3">${scheme.shortDescription}</p>

                    <div class="bg-light p-3 rounded-3 mb-3 border">
                        <h6 class="fw-bold mb-2 text-primary"><i class="bi bi-gift-fill me-2"></i>Key Benefits</h6>
                        <p class="mb-0 text-dark small">${scheme.benefits}</p>
                    </div>

                    <div class="mb-3">
                        <h6 class="fw-bold mb-2"><i class="bi bi-card-checklist me-2"></i>Transparent Criteria Breakdown</h6>
                        ${criteriaHtml}
                    </div>

                    <div class="d-flex align-items-center justify-content-between pt-3 border-top flex-wrap gap-2">
                        <a href="scheme-detail.html?id=${scheme.id}" class="btn btn-gov-primary btn-sm px-3">
                            <i class="bi bi-eye-fill me-1"></i> View Full Details & Apply
                        </a>
                        ${isLoggedIn() ? `
                            <button class="btn ${isSaved ? 'btn-outline-danger' : 'btn-gov-outline'} btn-sm" onclick="toggleBookmark(${scheme.id}, this)">
                                <i class="bi ${isSaved ? 'bi-bookmark-dash-fill' : 'bi-bookmark-plus'} me-1"></i>
                                ${isSaved ? 'Remove Bookmark' : 'Save Scheme'}
                            </button>
                        ` : `
                            <a href="login.html" class="btn btn-gov-outline btn-sm">
                                <i class="bi bi-bookmark-plus me-1"></i> Login to Bookmark
                            </a>
                        `}
                    </div>
                </div>
            </div>
        </div>
    `;
}

async function toggleBookmark(schemeId, btn) {
    try {
        const isSaved = btn.classList.contains('btn-outline-danger');
        if (isSaved) {
            await apiFetch(`/saved-schemes/${schemeId}`, { method: 'DELETE' });
            btn.className = 'btn btn-gov-outline btn-sm';
            btn.innerHTML = `<i class="bi bi-bookmark-plus me-1"></i> Save Scheme`;
            showToast('Scheme removed from saved bookmarks', 'info');
        } else {
            await apiFetch(`/saved-schemes/${schemeId}`, { method: 'POST' });
            btn.className = 'btn btn-outline-danger btn-sm';
            btn.innerHTML = `<i class="bi bi-bookmark-dash-fill me-1"></i> Remove Bookmark`;
            showToast('Scheme saved to your bookmarks!', 'success');
        }
    } catch (err) {
        showToast(err.message || 'Bookmark update failed', 'error');
    }
}
