(() => {
  const welcome = document.getElementById('welcome-screen');
  if (welcome) {
    if (sessionStorage.getItem('megaMartWelcomeSeen') === '1') {
      welcome.remove();
    } else {
      sessionStorage.setItem('megaMartWelcomeSeen', '1');
      const delay = window.matchMedia('(prefers-reduced-motion: reduce)').matches ? 900 : 4200;
      window.setTimeout(() => welcome.classList.add('leaving'), delay);
      window.setTimeout(() => welcome.remove(), delay + 800);
    }
  }

  document.querySelectorAll('.add-to-order').forEach(button => {
    button.addEventListener('click', event => {
      event.preventDefault();
      const form = button.closest('form');
      if (!form || button.disabled) return;
      if (!form.reportValidity()) return;
      button.disabled = true;
      const flyer = document.createElement('span');
      flyer.className = 'butterfly-flight';
      flyer.setAttribute('aria-hidden', 'true');
      flyer.textContent = '🦋';
      const rect = button.getBoundingClientRect();
      flyer.style.left = `${rect.left + rect.width / 2}px`;
      flyer.style.top = `${rect.top}px`;
      document.body.appendChild(flyer);
      window.setTimeout(() => form.requestSubmit(), 850);
    });
  });

  if (document.querySelector('.customer-order-card')) {
    let refreshTimer;
    const scheduleRefresh = () => {
      window.clearTimeout(refreshTimer);
      const activeOrder = [...document.querySelectorAll('.status-pill')]
        .some(status => !['DELIVERED', 'CANCELLED'].includes(status.dataset.status));
      if (activeOrder && document.visibilityState === 'visible') {
        refreshTimer = window.setTimeout(() => window.location.reload(), 20000);
      }
    };
    document.addEventListener('visibilitychange', scheduleRefresh);
    scheduleRefresh();
  }
})();
