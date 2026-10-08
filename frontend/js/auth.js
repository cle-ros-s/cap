/**
 * Government Scheme Recommendation System - Auth JS
 * Handles user registration & login forms.
 */

document.addEventListener('DOMContentLoaded', () => {
    // Registration Form Handler
    const registerForm = document.getElementById('registerForm');
    if (registerForm) {
        registerForm.addEventListener('submit', async (e) => {
            e.preventDefault();

            const name = document.getElementById('name').value.trim();
            const email = document.getElementById('email').value.trim();
            const mobile = document.getElementById('mobile').value.trim();
            const password = document.getElementById('password').value;
            const confirmPassword = document.getElementById('confirmPassword').value;

            if (password !== confirmPassword) {
                showToast('Passwords do not match!', 'error');
                return;
            }

            const btn = registerForm.querySelector('button[type="submit"]');
            btn.disabled = true;
            btn.innerHTML = `<span class="spinner-border spinner-border-sm me-2"></span>Creating Account...`;

            try {
                const res = await apiFetch('/auth/register', {
                    method: 'POST',
                    body: JSON.stringify({ name, email, mobile, password })
                });

                setSession(res.token, {
                    id: res.id,
                    name: res.name,
                    email: res.email,
                    mobile: res.mobile,
                    role: res.role
                });

                showToast('Registration successful! Redirecting...', 'success');
                setTimeout(() => {
                    window.location.href = 'user-dashboard.html';
                }, 1000);
            } catch (err) {
                showToast(err.message || 'Registration failed', 'error');
                btn.disabled = false;
                btn.innerText = 'Register Account';
            }
        });
    }

    // Login Form Handler
    const loginForm = document.getElementById('loginForm');
    if (loginForm) {
        loginForm.addEventListener('submit', async (e) => {
            e.preventDefault();

            const username = document.getElementById('username').value.trim();
            const password = document.getElementById('password').value;

            const btn = loginForm.querySelector('button[type="submit"]');
            btn.disabled = true;
            btn.innerHTML = `<span class="spinner-border spinner-border-sm me-2"></span>Authenticating...`;

            // Clear previous inline error
            const errDiv = document.getElementById('login-error');
            if (errDiv) errDiv.classList.add('d-none');

            try {
                const res = await apiFetch('/auth/login', {
                    method: 'POST',
                    body: JSON.stringify({ username, password })
                });

                setSession(res.token, {
                    id: res.id,
                    name: res.name,
                    email: res.email,
                    mobile: res.mobile,
                    role: res.role
                });

                showToast('Login successful!', 'success');
                setTimeout(() => {
                    if (res.role === 'ROLE_ADMIN') {
                        window.location.href = 'admin-dashboard.html';
                    } else {
                        window.location.href = 'user-dashboard.html';
                    }
                }, 800);
            } catch (err) {
                const errMsg = err.message || 'Invalid credentials. Please try again.';
                showToast(errMsg, 'error');
                // Also show inline error for guaranteed visibility
                const errDiv = document.getElementById('login-error');
                if (errDiv) {
                    errDiv.textContent = '⚠ ' + errMsg;
                    errDiv.classList.remove('d-none');
                }
                btn.disabled = false;
                btn.innerText = 'Sign In';
            }
        });
    }
});
