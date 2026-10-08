/**
 * Government Scheme Recommendation System - Main App JS
 * Handles REST API Communication, Authentication Session, Toasts, & UI helpers.
 */

const API_BASE_URL = 'http://localhost:8081/api';

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
            gap:10px;min-width:240px;max-width:360px;font-size:14px;
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

        // Also try Bootstrap toast as an enhancement if available
        if (typeof bootstrap !== 'undefined' && bootstrap.Toast) {
            // Already shown via CSS above — no need
        }
    } catch (e) {
        // Absolute fallback
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

    try {
        const response = await fetch(`${API_BASE_URL}${endpoint}`, {
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

    if (user) {
        const isAdminUser = user.role === 'ROLE_ADMIN';
        navRight.innerHTML = `
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
                    <li><button class="dropdown-item text-danger" onclick="logout()"><i class="bi bi-box-arrow-right me-2"></i>Logout</button></li>
                </ul>
            </div>
        `;
    } else {
        navRight.innerHTML = `
            <a href="login.html" class="btn btn-outline-primary fw-semibold">Login</a>
            <a href="register.html" class="btn btn-gov-primary">Register</a>
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

document.addEventListener('DOMContentLoaded', () => {
    updateNavbar();
});
