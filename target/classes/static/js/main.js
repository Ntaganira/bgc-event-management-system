/**
 * BGC Event Management System - Main JS
 * Author: NTAGANIRA Heritier | Date: 2026-02-27
 */

function toggleSidebar() {
    const sidebar = document.getElementById('sidebar');
    if (sidebar) sidebar.classList.toggle('collapsed');
}

// Auto-dismiss alerts after 4s
document.addEventListener('DOMContentLoaded', function () {
    document.querySelectorAll('.alert, .toast').forEach(function (el) {
        setTimeout(() => { el.style.transition = 'opacity 0.5s'; el.style.opacity = '0'; setTimeout(() => el.remove(), 500); }, 4000);
    });

    // Highlight active nav item
    const path = window.location.pathname;
    document.querySelectorAll('.nav-item').forEach(function (item) {
        const href = item.getAttribute('href') || '';
        if (href && path.startsWith(href) && href !== '/') {
            item.classList.add('active');
        }
    });
});
