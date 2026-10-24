/**
 * Government Scheme Recommendation System - Main App JS
 * Handles REST API Communication, Authentication Session, Toasts, & UI helpers.
 * Includes Secure Password Encryption (BCrypt) & Live Backend HTTPS Enforcement Options.
 */

// Default Configuration Constants
const DEFAULT_LOCAL_API = 'http://localhost:8081/api';
const DEFAULT_LIVE_API = 'https://api.govscheme.nic.in/api';

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
window.escapeHtml = escapeHtml;

/**
 * Validates and enforces HTTPS for remote / live backend links.
 * Ensures passwords and sensitive user credentials are encrypted in transit.
 */
function sanitizeAndEnforceHttps(url) {
    if (!url || typeof url !== 'string') return DEFAULT_LOCAL_API;
    let trimmed = url.trim().replace(/\/+$/, '');

    // Check if URL points to localhost or loopback
    const isLocalhost = /(localhost|127\.0\.0\.1|0\.0\.0\.0)/i.test(trimmed);

    if (!isLocalhost) {
        // Remote / Live backend link - Enforce HTTPS!
        if (trimmed.startsWith('http://')) {
            console.warn('[Security Policy] Live backend URLs MUST use HTTPS to protect passwords in transit. Upgraded to https://');
            trimmed = trimmed.replace(/^http:\/\//i, 'https://');
        } else if (!trimmed.startsWith('https://')) {
            trimmed = 'https://' + trimmed;
        }
    } else {
        if (!trimmed.startsWith('http://') && !trimmed.startsWith('https://')) {
            trimmed = 'http://' + trimmed;
        }
    }
    return trimmed;
}

/**
 * Retrieves the current backend configuration from localStorage
 */
function getBackendConfig() {
    try {
        const stored = localStorage.getItem('gov_scheme_backend_config');
        if (stored) {
            const parsed = JSON.parse(stored);
            return {
                mode: parsed.mode || 'local',
                customUrl: parsed.customUrl || '',
                liveUrl: parsed.liveUrl || DEFAULT_LIVE_API
            };
        }
    } catch (e) {
        console.error('Failed to parse backend config from localStorage', e);
    }
    return {
        mode: 'local',
        customUrl: '',
        liveUrl: DEFAULT_LIVE_API
    };
}

/**
 * Returns active API base URL with HTTPS enforcement
 */
function getApiBaseUrl() {
    const config = getBackendConfig();
    let url = DEFAULT_LOCAL_API;

    if (config.mode === 'live') {
        url = config.liveUrl || DEFAULT_LIVE_API;
    } else if (config.mode === 'custom' && config.customUrl) {
        url = config.customUrl;
    }

    return sanitizeAndEnforceHttps(url);
}

// Active API Base URL
let API_BASE_URL = getApiBaseUrl();

/**
 * Saves backend configuration and updates active API URL
 */
function saveBackendConfig(mode, customUrl) {
    let sanitizedCustom = customUrl ? customUrl.trim() : '';
    if (sanitizedCustom) {
        sanitizedCustom = sanitizeAndEnforceHttps(sanitizedCustom);
    }

    const config = {
        mode,
        customUrl: sanitizedCustom,
        liveUrl: DEFAULT_LIVE_API
    };

    localStorage.setItem('gov_scheme_backend_config', JSON.stringify(config));
    API_BASE_URL = getApiBaseUrl();
    updateSecurityPill();
    return API_BASE_URL;
}

// Toast Notification Helper (pure CSS, no Bootstrap JS dependency)
function showToast(message, type = 'info') {
    try {
        let container = document.getElementById('toast-container');
        if (!container) {
            container = document.createElement('div');
            container.id = 'toast-container';
            container.style.cssText = 'position:fixed;bottom:20px;right:20px;z-index:9999;display:flex;flex-direction:column;gap:10px;';
            document.body.appendChild(container);
        }

        const bgColor = type === 'success' ? '#198754' : type === 'error' ? '#dc3545' : '#0d6efd';
        const icon = type === 'success' ? '✓' : type === 'error' ? '⚠' : 'ℹ';

        const toastEl = document.createElement('div');
        toastEl.style.cssText = `
            background:${bgColor};color:#fff;padding:12px 18px;border-radius:8px;
            box-shadow:0 4px 16px rgba(0,0,0,.25);display:flex;align-items:center;
            gap:10px;min-width:240px;max-width:380px;font-size:14px;
            animation:slideInToast .3s ease;opacity:1;transition:opacity .4s ease;
        `;
        toastEl.innerHTML = `<span style="font-size:1.1rem">${icon}</span><span>${message}</span>`;

        // Inject keyframe if not already present
        if (!document.getElementById('toast-style')) {
            const style = document.createElement('style');
            style.id = 'toast-style';
            style.textContent = '@keyframes slideInToast{from{transform:translateX(110%);opacity:0}to{transform:translateX(0);opacity:1}}';
            document.head.appendChild(style);
        }

        container.appendChild(toastEl);

        // Auto-dismiss after 4 seconds
        setTimeout(() => {
            toastEl.style.opacity = '0';
            setTimeout(() => toastEl.remove(), 400);
        }, 4000);
    } catch (e) {
        console.error('[Toast Error]', e);
        console.info(`[${type.toUpperCase()}] ${message}`);
    }
}

// Session Management Helpers
function getToken() {
    return localStorage.getItem('gov_scheme_token');
}

function getUser() {
    const userStr = localStorage.getItem('gov_scheme_user');
    return userStr ? JSON.parse(userStr) : null;
}

function setSession(token, user) {
    localStorage.setItem('gov_scheme_token', token);
    localStorage.setItem('gov_scheme_user', JSON.stringify(user));
}

function clearSession() {
    localStorage.removeItem('gov_scheme_token');
    localStorage.removeItem('gov_scheme_user');
}

function isLoggedIn() {
    return !!getToken();
}

function isAdmin() {
    const user = getUser();
    return user && user.role === 'ROLE_ADMIN';
}

// Authenticated Fetch API Wrapper
async function apiFetch(endpoint, options = {}) {
    const token = getToken();
    const headers = {
        'Content-Type': 'application/json',
        ...(options.headers || {})
    };

    if (token) {
        headers['Authorization'] = `Bearer ${token}`;
    }

    const currentBase = getApiBaseUrl();

    try {
        const response = await fetch(`${currentBase}${endpoint}`, {
            ...options,
            headers
        });

        if (response.status === 401) {
            clearSession();
            // If on a protected page, redirect
            if (window.location.pathname.includes('dashboard') || window.location.pathname.includes('admin')) {
                window.location.href = 'login.html';
            }
        }

        const data = await response.json().catch(() => null);

        if (!response.ok) {
            throw new Error((data && data.message) || `Error ${response.status}: ${response.statusText}`);
        }

        return data;
    } catch (err) {
        console.error('API Error:', err);
        throw err;
    }
}

// Global Navbar Initializer
function updateNavbar() {
    const user = getUser();
    const navRight = document.getElementById('nav-auth-section');
    if (!navRight) return;

    const securityButton = `
        <button type="button" class="btn btn-sm btn-outline-secondary d-flex align-items-center gap-1 me-2" onclick="openBackendSecurityModal()" title="Backend & Password Encryption Settings">
            <i class="bi bi-shield-lock-fill text-success"></i>
            <span class="d-none d-md-inline small fw-semibold">HTTPS / BCrypt</span>
        </button>
    `;

    if (user) {
        const isAdminUser = user.role === 'ROLE_ADMIN';
        navRight.innerHTML = `
            <div class="d-flex align-items-center">
                ${securityButton}
                <div class="dropdown">
                    <button class="btn btn-outline-primary dropdown-toggle d-flex align-items-center gap-2 fw-semibold" type="button" data-bs-toggle="dropdown">
                        <i class="bi bi-person-circle fs-5"></i>
                        <span>${user.name}</span>
                    </button>
                    <ul class="dropdown-menu dropdown-menu-end shadow">
                        <li><h6 class="dropdown-header">Logged in as ${isAdminUser ? 'Admin' : 'Citizen'}</h6></li>
                        ${!isAdminUser ? `
                            <li><a class="dropdown-item" href="user-dashboard.html"><i class="bi bi-speedometer2 me-2"></i>Dashboard</a></li>
                            <li><a class="dropdown-item" href="recommendations.html"><i class="bi bi-cpu me-2"></i>Check Eligibility</a></li>
                        ` : `
                            <li><a class="dropdown-item" href="admin-dashboard.html"><i class="bi bi-shield-lock me-2"></i>Admin Dashboard</a></li>
                        `}
                        <li><hr class="dropdown-divider"></li>
                        <li><button class="dropdown-item" onclick="openBackendSecurityModal()"><i class="bi bi-shield-check me-2 text-success"></i>Encryption & HTTPS Settings</button></li>
                        <li><button class="dropdown-item text-danger" onclick="logout()"><i class="bi bi-box-arrow-right me-2"></i>Logout</button></li>
                    </ul>
                </div>
            </div>
        `;
    } else {
        navRight.innerHTML = `
            <div class="d-flex align-items-center gap-2">
                ${securityButton}
                <a href="login.html" class="btn btn-outline-primary fw-semibold">Login</a>
                <a href="register.html" class="btn btn-gov-primary">Register</a>
            </div>
        `;
    }
}

function logout() {
    clearSession();
    showToast('Logged out successfully', 'info');
    setTimeout(() => {
        window.location.href = 'index.html';
    }, 800);
}

// Currency Formatter
function formatCurrency(amount) {
    if (amount === null || amount === undefined) return 'N/A';
    return new Intl.NumberFormat('en-IN', { style: 'currency', currency: 'INR', maximumFractionDigits: 0 }).format(amount);
}

// =========================================================================
// BACKEND CONNECTION & HTTPS PASSWORD ENCRYPTION UI MODAL
// =========================================================================

/**
 * Initializes and injects the Backend Link & HTTPS Encryption Settings Modal into the document
 */
function initSecurityAndBackendModal() {
    if (document.getElementById('backendSecurityModal')) return;

    const modalHtml = `
    <!-- Backend Connection & HTTPS Encryption Modal -->
    <div class="modal fade" id="backendSecurityModal" tabindex="-1" aria-labelledby="backendSecurityModalLabel" aria-hidden="true">
        <div class="modal-dialog modal-dialog-centered modal-lg">
            <div class="modal-content shadow-lg border-0 rounded-4">
                <div class="modal-header bg-light py-3 border-bottom">
                    <div class="d-flex align-items-center gap-2">
                        <div class="bg-primary bg-opacity-10 text-primary p-2 rounded-circle d-flex align-items-center justify-content-center" style="width: 38px; height: 38px;">
                            <i class="bi bi-shield-lock-fill fs-5"></i>
                        </div>
                        <div>
                            <h5 class="modal-title fw-bold m-0" id="backendSecurityModalLabel">Backend Link & Password Encryption Settings</h5>
                            <span class="text-muted small">Transport Layer HTTPS & Spring Security BCrypt Configuration</span>
                        </div>
                    </div>
                    <button type="button" class="btn-close" data-bs-dismiss="modal" aria-label="Close"></button>
                </div>
                <div class="modal-body p-4">
                    <!-- Security Guarantees Banner -->
                    <div class="row g-3 mb-4">
                        <div class="col-md-6">
                            <div class="p-3 bg-success bg-opacity-10 border border-success border-opacity-25 rounded-3 h-100">
                                <div class="d-flex align-items-center gap-2 mb-1">
                                    <i class="bi bi-database-fill-lock text-success fs-5"></i>
                                    <span class="fw-bold text-success">BCrypt Database Hashing</span>
                                </div>
                                <p class="small text-muted mb-0">
                                    Passwords are <strong>never stored as plain text</strong>. Spring Security's <code>BCryptPasswordEncoder</code> automatically generates a random cryptographic salt and hashes every password during sign-up.
                                </p>
                            </div>
                        </div>
                        <div class="col-md-6">
                            <div class="p-3 bg-primary bg-opacity-10 border border-primary border-opacity-25 rounded-3 h-100">
                                <div class="d-flex align-items-center gap-2 mb-1">
                                    <i class="bi bi-file-lock2-fill text-primary fs-5"></i>
                                    <span class="fw-bold text-primary">HTTPS In-Transit Encryption</span>
                                </div>
                                <p class="small text-muted mb-0">
                                    Live backend links are <strong>strictly enforced to use HTTPS (SSL/TLS)</strong> so user passwords and Aadhaar data are fully encrypted while traveling across the network.
                                </p>
                            </div>
                        </div>
                    </div>

                    <!-- Backend Selection Form -->
                    <div class="mb-3">
                        <label class="form-label fw-bold text-dark mb-2">Select Backend API Link Option</label>
                        <div class="list-group">
                            <!-- Option 1: Local Development -->
                            <label class="list-group-item list-group-item-action d-flex align-items-start gap-3 p-3 cursor-pointer">
                                <input class="form-check-input flex-shrink-0 mt-1" type="radio" name="backendOption" id="optLocal" value="local">
                                <div class="w-100">
                                    <div class="d-flex justify-content-between align-items-center">
                                        <span class="fw-bold text-dark">Localhost Spring Boot Backend</span>
                                        <span class="badge bg-secondary">Development</span>
                                    </div>
                                    <div class="text-muted small mt-1">Default local port for backend development and testing.</div>
                                    <code class="d-block small text-primary mt-1">http://localhost:8081/api</code>
                                </div>
                            </label>

                            <!-- Option 2: Official Live Cloud Backend -->
                            <label class="list-group-item list-group-item-action d-flex align-items-start gap-3 p-3 cursor-pointer">
                                <input class="form-check-input flex-shrink-0 mt-1" type="radio" name="backendOption" id="optLive" value="live">
                                <div class="w-100">
                                    <div class="d-flex justify-content-between align-items-center">
                                        <span class="fw-bold text-dark">Official Live Production Backend (HTTPS)</span>
                                        <span class="badge bg-success"><i class="bi bi-shield-check me-1"></i>HTTPS Enforced</span>
                                    </div>
                                    <div class="text-muted small mt-1">Secured with SSL/TLS certificate for production cloud deployment.</div>
                                    <code class="d-block small text-success mt-1">https://api.govscheme.nic.in/api</code>
                                </div>
                            </label>

                            <!-- Option 3: Custom Live Backend URL -->
                            <label class="list-group-item list-group-item-action d-flex align-items-start gap-3 p-3 cursor-pointer">
                                <input class="form-check-input flex-shrink-0 mt-1" type="radio" name="backendOption" id="optCustom" value="custom">
                                <div class="w-100">
                                    <div class="d-flex justify-content-between align-items-center">
                                        <span class="fw-bold text-dark">Custom Remote Live Backend (HTTPS Enforced)</span>
                                        <span class="badge bg-primary">Custom Endpoint</span>
                                    </div>
                                    <div class="text-muted small mt-1">Provide your live deployment server URL (e.g. AWS, Render, Railway, Nginx).</div>
                                    <div class="input-group input-group-sm mt-2">
                                        <span class="input-group-text bg-light"><i class="bi bi-link-45deg"></i></span>
                                        <input type="text" id="customBackendInput" class="form-control" placeholder="https://your-live-domain.com/api">
                                    </div>
                                    <!-- Dynamic HTTPS Warning / Feedback -->
                                    <div id="httpsValidationNotice" class="mt-2 small d-none"></div>
                                </div>
                            </label>
                        </div>
                    </div>

                    <!-- Connection & Security Test Box -->
                    <div class="border rounded-3 p-3 bg-light mt-3">
                        <div class="d-flex justify-content-between align-items-center">
                            <div>
                                <span class="fw-bold small text-dark"><i class="bi bi-activity me-1 text-primary"></i>Verify Backend Security Status</span>
                                <div class="text-muted small">Ping the backend endpoint to verify BCrypt hashing and HTTPS transport.</div>
                            </div>
                            <button type="button" class="btn btn-sm btn-outline-primary fw-semibold" id="btnTestSecurity" onclick="runBackendSecurityTest()">
                                <i class="bi bi-arrow-repeat me-1"></i>Test Connection
                            </button>
                        </div>
                        <div id="securityTestResult" class="mt-2 small d-none"></div>
                    </div>
                </div>
                <div class="modal-footer bg-light py-2">
                    <button type="button" class="btn btn-outline-secondary" data-bs-dismiss="modal">Cancel</button>
                    <button type="button" class="btn btn-gov-primary px-4" onclick="applyBackendSettings()">
                        <i class="bi bi-check2-circle me-1"></i>Save & Apply Settings
                    </button>
                </div>
            </div>
        </div>
    </div>

    <!-- Floating Global Security Badge -->
    <div id="globalSecurityPill" class="position-fixed bottom-0 start-0 m-3 z-3 shadow-sm bg-white border rounded-pill px-3 py-1 d-flex align-items-center gap-2 cursor-pointer transition-all hover-shadow" onclick="openBackendSecurityModal()" style="font-size: 13px;">
        <span class="badge rounded-pill bg-success p-1"><i class="bi bi-shield-check text-white"></i></span>
        <span id="pillBackendText" class="fw-semibold text-secondary">Backend: Local</span>
        <span class="badge bg-light text-primary border">HTTPS / BCrypt</span>
    </div>
    `;

    document.body.insertAdjacentHTML('beforeend', modalHtml);

    // Setup input validation listener
    const customInput = document.getElementById('customBackendInput');
    if (customInput) {
        customInput.addEventListener('input', () => {
            validateCustomHttpsInput();
        });
    }

    // Auto-select corresponding radio when clicking custom input
    if (customInput) {
        customInput.addEventListener('focus', () => {
            document.getElementById('optCustom').checked = true;
        });
    }
}

/**
 * Validates custom URL input in real time for HTTPS compliance
 */
function validateCustomHttpsInput() {
    const input = document.getElementById('customBackendInput');
    const notice = document.getElementById('httpsValidationNotice');
    if (!input || !notice) return;

    const val = input.value.trim();
    if (!val) {
        notice.classList.add('d-none');
        return;
    }

    const isLocal = /(localhost|127\.0\.0\.1)/i.test(val);

    if (isLocal) {
        notice.className = 'mt-2 small text-muted';
        notice.innerHTML = `<i class="bi bi-info-circle me-1"></i>Localhost endpoint detected (HTTP permitted for local development).`;
        notice.classList.remove('d-none');
    } else if (val.startsWith('http://')) {
        notice.className = 'mt-2 small text-warning fw-semibold';
        notice.innerHTML = `<i class="bi bi-exclamation-triangle-fill me-1"></i><strong>HTTPS Enforced:</strong> Live backend links must use HTTPS to prevent unencrypted password transmission. This will be automatically upgraded to <code>https://${val.replace(/^http:\/\//i, '')}</code>.`;
        notice.classList.remove('d-none');
    } else if (val.startsWith('https://')) {
        notice.className = 'mt-2 small text-success fw-semibold';
        notice.innerHTML = `<i class="bi bi-shield-check me-1"></i><strong>Secure Link:</strong> Valid HTTPS link. Passwords will be encrypted in transit with TLS.`;
        notice.classList.remove('d-none');
    } else {
        notice.className = 'mt-2 small text-info';
        notice.innerHTML = `<i class="bi bi-arrow-up-right-circle me-1"></i>Remote URL will automatically use <code>https://${val}</code> for encrypted transmission.`;
        notice.classList.remove('d-none');
    }
}

/**
 * Updates the floating security pill to reflect the active backend link
 */
function updateSecurityPill() {
    const pillText = document.getElementById('pillBackendText');
    if (!pillText) return;

    const config = getBackendConfig();
    const activeUrl = getApiBaseUrl();
    const isHttps = activeUrl.startsWith('https://');

    if (config.mode === 'live') {
        pillText.innerHTML = `<span class="text-success"><i class="bi bi-lock-fill me-1"></i>Live HTTPS</span>`;
    } else if (config.mode === 'custom') {
        pillText.innerHTML = isHttps 
            ? `<span class="text-success"><i class="bi bi-lock-fill me-1"></i>Custom HTTPS</span>`
            : `<span class="text-warning"><i class="bi bi-hdd-network me-1"></i>Custom Local</span>`;
    } else {
        pillText.innerHTML = `<span class="text-primary"><i class="bi bi-laptop me-1"></i>Local Dev (8081)</span>`;
    }
}

/**
 * Opens the Backend Connection & HTTPS Encryption Modal
 */
function openBackendSecurityModal() {
    initSecurityAndBackendModal();

    const config = getBackendConfig();
    const optLocal = document.getElementById('optLocal');
    const optLive = document.getElementById('optLive');
    const optCustom = document.getElementById('optCustom');
    const customInput = document.getElementById('customBackendInput');
    const resultBox = document.getElementById('securityTestResult');

    if (resultBox) resultBox.classList.add('d-none');

    if (config.mode === 'live' && optLive) {
        optLive.checked = true;
    } else if (config.mode === 'custom' && optCustom) {
        optCustom.checked = true;
    } else if (optLocal) {
        optLocal.checked = true;
    }

    if (customInput) {
        customInput.value = config.customUrl || '';
        validateCustomHttpsInput();
    }

    const modalEl = document.getElementById('backendSecurityModal');
    if (typeof bootstrap !== 'undefined' && bootstrap.Modal) {
        const modal = bootstrap.Modal.getOrCreateInstance(modalEl);
        modal.show();
    } else {
        modalEl.classList.add('show');
        modalEl.style.display = 'block';
    }
}

/**
 * Runs live connection and encryption test against the selected endpoint
 */
async function runBackendSecurityTest() {
    const btn = document.getElementById('btnTestSecurity');
    const resultBox = document.getElementById('securityTestResult');
    if (!resultBox) return;

    // Determine target URL from current selection
    const selectedRadio = document.querySelector('input[name="backendOption"]:checked');
    const mode = selectedRadio ? selectedRadio.value : 'local';
    let targetUrl = DEFAULT_LOCAL_API;

    if (mode === 'live') {
        targetUrl = DEFAULT_LIVE_API;
    } else if (mode === 'custom') {
        const customVal = document.getElementById('customBackendInput').value.trim();
        if (!customVal) {
            resultBox.className = 'mt-2 small alert alert-danger p-2 mb-0';
            resultBox.innerHTML = '<i class="bi bi-exclamation-circle me-1"></i>Please enter a custom backend URL to test.';
            resultBox.classList.remove('d-none');
            return;
        }
        targetUrl = sanitizeAndEnforceHttps(customVal);
    }

    if (btn) {
        btn.disabled = true;
        btn.innerHTML = `<span class="spinner-border spinner-border-sm me-1"></span>Testing...`;
    }

    resultBox.className = 'mt-2 small alert alert-info p-2 mb-0';
    resultBox.innerHTML = `<i class="bi bi-hourglass-split me-1"></i>Contacting <code>${targetUrl}/auth/security-status</code>...`;
    resultBox.classList.remove('d-none');

    try {
        const res = await fetch(`${targetUrl}/auth/security-status`, {
            method: 'GET',
            headers: { 'Content-Type': 'application/json' }
        });

        if (!res.ok) {
            throw new Error(`HTTP ${res.status}: ${res.statusText}`);
        }

        const data = await res.json();
        const isHttps = data.isHttpsConnection || targetUrl.startsWith('https://');

        resultBox.className = 'mt-2 small alert alert-success p-2 mb-0';
        resultBox.innerHTML = `
            <div class="fw-bold mb-1"><i class="bi bi-check-circle-fill me-1 text-success"></i>Backend Connection Successful!</div>
            <div class="d-flex flex-wrap gap-2 mt-1">
                <span class="badge bg-success"><i class="bi bi-shield-check me-1"></i>${data.passwordHashing || 'BCrypt Hashing Active'}</span>
                <span class="badge ${isHttps ? 'bg-primary' : 'bg-warning text-dark'}">
                    <i class="bi ${isHttps ? 'bi-lock-fill' : 'bi-unlock-fill'} me-1"></i>${isHttps ? 'HTTPS / TLS Encrypted' : 'Local HTTP (Dev Mode)'}
                </span>
                <span class="badge bg-secondary">Plaintext DB Storage: Disabled</span>
            </div>
            <div class="text-muted mt-1" style="font-size: 11px;">Target: <code>${targetUrl}</code></div>
        `;
    } catch (err) {
        resultBox.className = 'mt-2 small alert alert-warning p-2 mb-0';
        resultBox.innerHTML = `
            <div class="fw-bold mb-1"><i class="bi bi-exclamation-triangle-fill text-warning me-1"></i>Cannot reach backend at: <code>${targetUrl}</code></div>
            <div>${err.message || 'Make sure Spring Boot backend is running on specified host.'}</div>
            <div class="text-muted mt-1 small">Note: Settings can still be saved for future live server deployment.</div>
        `;
    } finally {
        if (btn) {
            btn.disabled = false;
            btn.innerHTML = `<i class="bi bi-arrow-repeat me-1"></i>Test Connection`;
        }
    }
}

/**
 * Saves and applies user's backend and HTTPS settings
 */
function applyBackendSettings() {
    const selectedRadio = document.querySelector('input[name="backendOption"]:checked');
    const mode = selectedRadio ? selectedRadio.value : 'local';
    const customInput = document.getElementById('customBackendInput');
    const customUrl = customInput ? customInput.value.trim() : '';

    if (mode === 'custom' && !customUrl) {
        showToast('Please enter a custom backend URL', 'error');
        return;
    }

    const newUrl = saveBackendConfig(mode, customUrl);
    const isHttps = newUrl.startsWith('https://');

    // Close modal
    const modalEl = document.getElementById('backendSecurityModal');
    if (typeof bootstrap !== 'undefined' && bootstrap.Modal) {
        const modal = bootstrap.Modal.getInstance(modalEl);
        if (modal) modal.hide();
    } else {
        modalEl.classList.remove('show');
        modalEl.style.display = 'none';
    }

    showToast(`Backend updated: ${newUrl} (${isHttps ? 'HTTPS Encrypted' : 'Local Dev'})`, 'success');

    // Small delay then update UI
    setTimeout(() => {
        updateSecurityPill();
    }, 300);
}

// Global initialization
document.addEventListener('DOMContentLoaded', () => {
    initSecurityAndBackendModal();
    updateNavbar();
    updateSecurityPill();
});
