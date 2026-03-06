/* ── AgendaCard HTML ── */
function agendaCard(item) {
  const tags = item.speakers.map(s =>
    `<span class="speaker-tag">${s}</span>`
  ).join('');
  return `
    <div class="agenda-card">
      <div class="agenda-meta">
        <span class="agenda-time">${item.time}</span>
        <span class="agenda-tag">${item.tag}</span>
      </div>
      <h3>${item.title}</h3>
      <p>${item.desc}</p>
      <div class="speaker-tags">${tags}</div>
    </div>`;
}

/* ── SpeakerCard HTML ── */
function speakerCard(s) {
  return `
    <div class="speaker-card">
      <div class="speaker-avatar">${s.initials}</div>
      <h3>${s.name}</h3>
      <p>${s.desc}</p>
      <a href="speaker-profile.html?id=${s.id}" class="btn-view-profile">View Profile</a>
    </div>`;
}

(function () {
  'use strict';

  // ── 1. Dismiss page skeleton once DOM is ready ─────────────────────────
  var skeleton = document.getElementById('page-skeleton');
  function dismissSkeleton() {
    if (!skeleton) return;
    skeleton.classList.add('skeleton-done');
    // Remove from DOM after transition so it doesn't trap focus
    setTimeout(function () {
      if (skeleton && skeleton.parentNode) {
        skeleton.parentNode.removeChild(skeleton);
      }
    }, 350);
  }

  if (document.readyState === 'loading') {
    document.addEventListener('DOMContentLoaded', dismissSkeleton);
  } else {
    // Already interactive — dismiss immediately (fast server response)
    dismissSkeleton();
  }

  // ── 2. Progress bar on every internal navigation click ─────────────────
  var bar = document.getElementById('nav-loader');

  function startBar() {
    if (!bar) return;
    bar.className = '';
    // Force reflow so transition restarts from 0
    bar.getBoundingClientRect();
    bar.classList.add('loading');
  }

  function finishBar() {
    if (!bar) return;
    bar.classList.remove('loading');
    bar.classList.add('done');
  }

  // Trigger bar on any same-origin anchor click
  document.addEventListener('click', function (e) {
    var anchor = e.target.closest('a[href]');
    if (!anchor) return;

    var href = anchor.getAttribute('href');
    // Skip: external, hash-only, javascript:, mailto:, target=_blank
    if (!href || href.startsWith('#') || href.startsWith('http')
        || href.startsWith('mailto') || href.startsWith('javascript')
        || anchor.target === '_blank') return;

    // Skip language switcher (no full navigation)
    if (href.includes('lang=')) return;

    startBar();
  });

  // Also start bar on form submits (POST navigations)
  document.addEventListener('submit', function (e) {
    if (e.target && e.target.method !== 'dialog') {
      startBar();
    }
  });

  // Finish bar when new page starts loading
  window.addEventListener('pageshow', finishBar);
  document.addEventListener('DOMContentLoaded', finishBar);
})();