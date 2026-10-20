(() => {
  const search = document.querySelector('#product-search');
  const cards = Array.from(document.querySelectorAll('.product-card'));
  const filters = Array.from(document.querySelectorAll('[data-category-filter]'));
  const emptySearch = document.querySelector('#search-empty-state');
  let activeCategory = 'all';

  function applyFilters() {
    const query = (search?.value || '').trim().toLowerCase();
    let visibleCount = 0;
    cards.forEach((card) => {
      const name = (card.dataset.search || '').toLowerCase();
      const category = (card.dataset.category || '').toLowerCase();
      const matchesSearch = !query || name.includes(query);
      const matchesCategory = activeCategory === 'all'
        || category.includes(activeCategory)
        || (activeCategory === 'fruit' && category.includes('produce'))
        || (activeCategory === 'vegetable' && category.includes('produce'))
        || (activeCategory === 'drink' && category.includes('beverage'));
      const show = matchesSearch && matchesCategory;
      card.hidden = !show;
      if (show) visibleCount += 1;
    });
    if (emptySearch) emptySearch.hidden = !cards.length || visibleCount > 0;
  }

  search?.addEventListener('input', applyFilters);
  filters.forEach((filter) => {
    filter.addEventListener('click', () => {
      activeCategory = (filter.dataset.categoryFilter || 'all').toLowerCase();
      document.querySelectorAll('.filter-chip').forEach((chip) => {
        chip.classList.toggle('is-selected', chip.dataset.categoryFilter === activeCategory);
      });
      applyFilters();
      if (filter.classList.contains('aisle-card')) {
        document.querySelector('#products')?.scrollIntoView({ behavior: 'smooth', block: 'start' });
      }
    });
  });

  const menuButton = document.querySelector('.menu-toggle');
  const navigation = document.querySelector('.main-nav');
  menuButton?.addEventListener('click', () => {
    const open = menuButton.getAttribute('aria-expanded') !== 'true';
    menuButton.setAttribute('aria-expanded', String(open));
    navigation?.classList.toggle('is-open', open);
  });
  navigation?.querySelectorAll('a').forEach((link) => {
    link.addEventListener('click', () => {
      navigation.classList.remove('is-open');
      menuButton?.setAttribute('aria-expanded', 'false');
    });
  });

  document.querySelectorAll('.add-product-form').forEach((form) => {
    form.addEventListener('submit', (event) => {
      if (form.dataset.animationDone === 'true') return;
      const button = form.querySelector('button[type="submit"]');
      const basket = document.querySelector('#basket-count');
      if (!button || !basket || window.matchMedia('(prefers-reduced-motion: reduce)').matches
          || typeof button.animate !== 'function') return;

      event.preventDefault();
      const from = button.getBoundingClientRect();
      const to = basket.getBoundingClientRect();
      const flyer = document.createElement('span');
      flyer.className = 'butterfly-flyer';
      flyer.setAttribute('aria-hidden', 'true');
      flyer.innerHTML = '<svg viewBox="0 0 100 100"><use href="/images/mega-mart-illustrations.svg#butterfly"></use></svg>';
      document.body.appendChild(flyer);
      const startX = from.left + from.width / 2;
      const startY = from.top + from.height / 2;
      const endX = to.left + to.width / 2;
      const endY = to.top + to.height / 2;
      flyer.style.left = (startX - 17) + 'px';
      flyer.style.top = (startY - 17) + 'px';
      const animation = flyer.animate([
        { transform: 'translate(0, 0) scale(.55) rotate(-15deg)', opacity: 0 },
        { transform: 'translate(' + ((endX - startX) * .5) + 'px, ' + ((endY - startY) * .35 - 48) + 'px) scale(1) rotate(14deg)', opacity: 1, offset: .42 },
        { transform: 'translate(' + (endX - startX) + 'px, ' + (endY - startY) + 'px) scale(.25) rotate(30deg)', opacity: 0 }
      ], { duration: 620, easing: 'cubic-bezier(.2,.75,.25,1)' });
      animation.onfinish = () => {
        flyer.remove();
        form.dataset.animationDone = 'true';
        form.requestSubmit();
      };
      animation.oncancel = () => {
        flyer.remove();
        form.dataset.animationDone = 'true';
        form.requestSubmit();
      };
    });
  });

  if (document.querySelector('.customer-order-card')) {
    let refreshTimer;
    const scheduleRefresh = () => {
      window.clearTimeout(refreshTimer);
      const activeOrder = Array.from(document.querySelectorAll('.order-status'))
        .some((status) => !['DELIVERED', 'CANCELLED'].includes(status.dataset.status));
      if (activeOrder && document.visibilityState === 'visible') {
        refreshTimer = window.setTimeout(() => window.location.reload(), 25000);
      }
    };
    document.addEventListener('visibilitychange', scheduleRefresh);
    scheduleRefresh();
  }
})();
