document.addEventListener('DOMContentLoaded', () => {
    const form = document.getElementById('loginForm');
    if (!form) return;
    form.addEventListener('submit', (e) => {
        e.preventDefault();
        // minimal behavior: redirect to main panel
        window.location.href = 'index.html';
    });
});
