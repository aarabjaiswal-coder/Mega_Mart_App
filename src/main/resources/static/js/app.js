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
        || (activeCategory === 'drink' && category.includes('beverage'))
        || (activeCategory === 'meat' && (category.includes('seafood') || category.includes('fish')))
        || (activeCategory === 'household' && (category.includes('home') || category.includes('house')));
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

  const announceCartError = () => {
    const feedback = document.querySelector('.feedback-stack');
    if (!feedback) return;
    const message = document.createElement('p');
    message.className = 'feedback-message error-message';
    message.textContent = 'The basket could not be refreshed. Refresh the shop before trying again.';
    feedback.replaceChildren(message);
  };

  const postCartForm = async (form) => {
    const response = await fetch(form.action, {
      method: (form.method || 'POST').toUpperCase(),
      body: new URLSearchParams(new FormData(form)),
      credentials: 'same-origin',
      headers: { Accept: 'text/html', 'X-Requested-With': 'XMLHttpRequest' }
    });
    const responseHtml = await response.text();
    const responsePage = new DOMParser().parseFromString(responseHtml, 'text/html');
    const updatedCart = responsePage.querySelector('#cart-panel');
    const updatedCount = responsePage.querySelector('#basket-count');
    const currentCart = document.querySelector('#cart-panel');
    const currentCount = document.querySelector('#basket-count');
    if (!response.ok || !updatedCart || !updatedCount || !currentCart || !currentCount) {
      throw new Error('The shop did not return an updated basket.');
    }

    currentCart.replaceWith(document.importNode(updatedCart, true));
    currentCount.textContent = updatedCount.textContent;
    const currentFeedback = document.querySelector('.feedback-stack');
    const updatedFeedback = responsePage.querySelector('.feedback-stack');
    if (currentFeedback && updatedFeedback) {
      currentFeedback.replaceChildren(...Array.from(updatedFeedback.childNodes, (node) => document.importNode(node, true)));
    }
  };

  const submitCartForm = async (form, button) => {
    try {
      await postCartForm(form);
    } catch {
      announceCartError();
    } finally {
      form.dataset.cartPending = 'false';
      if (button) button.disabled = false;
    }
  };

  document.querySelectorAll('.add-product-form').forEach((form) => {
    form.addEventListener('submit', (event) => {
      if (form.dataset.cartPending === 'true') { event.preventDefault(); return; }
      event.preventDefault();
      const button = form.querySelector('button[type="submit"]');
      const basket = document.querySelector('#basket-count');
      if (!button) { void submitCartForm(form, button); return; }
      form.dataset.cartPending = 'true';
      button.disabled = true;
      if (!basket || typeof Element.prototype.animate !== 'function') {
        void submitCartForm(form, button);
        return;
      }
      const from = button.getBoundingClientRect();
      const to = basket.getBoundingClientRect();
      const startX = from.left + from.width / 2;
      const startY = from.top + from.height / 2;
      const endX = to.left + to.width / 2;
      const endY = to.top + to.height / 2;
      const butterfly = '<svg width="100%" height="100%" viewBox="0 0 100 100" aria-hidden="true"><path d="M49 48C32 13 5 14 9 42c2 15 17 21 38 15-31 2-39 29-21 36 14 5 23-13 25-37 2 24 11 42 25 37 18-7 10-34-21-36 21 6 36 0 38-15 4-28-23-29-40 6" fill="#ffbc48" stroke="#f08c48" stroke-width="3" stroke-linejoin="round"/><path d="M50 45v25m-4-24c-5-8-4-14 0-18m8 18c5-8 4-14 0-18" fill="none" stroke="#644c5a" stroke-width="3" stroke-linecap="round"/></svg>';
      let remaining = 3;
      let submitted = false;
      let fallbackTimer;
      const submitOrder = () => {
        if (submitted) return;
        submitted = true;
        window.clearTimeout(fallbackTimer);
        void submitCartForm(form, button);
      };
      const finishFlyer = (flyer) => {
        flyer.remove();
        remaining -= 1;
        if (remaining === 0) submitOrder();
      };
      fallbackTimer = window.setTimeout(submitOrder, 1250);
      for (let index = 0; index < 3; index += 1) {
        const flyer = document.createElement('span');
        flyer.className = 'butterfly-flyer';
        flyer.setAttribute('aria-hidden', 'true');
        flyer.innerHTML = butterfly;
        flyer.style.cssText = 'position:fixed;z-index:10000;pointer-events:none;width:' + (30 - index * 4) + 'px;height:' + (30 - index * 4) + 'px;left:' + (startX - 15) + 'px;top:' + (startY - 15) + 'px;';
        document.body.appendChild(flyer);
        const dx = endX - startX + (index - 1) * 7;
        const dy = endY - startY + (index - 1) * 7;
        const animation = flyer.animate([
          { transform: 'translate(0, 0) scale(.4) rotate(-18deg)', opacity: 0 },
          { transform: 'translate(' + (dx * .48) + 'px, ' + (dy * .35 - 55 - index * 5) + 'px) scale(1) rotate(13deg)', opacity: 1, offset: .42 },
          { transform: 'translate(' + dx + 'px, ' + dy + 'px) scale(.3) rotate(32deg)', opacity: 0 }
        ], { duration: 650 + index * 85, delay: index * 65, easing: 'cubic-bezier(.2,.75,.25,1)' });
        animation.onfinish = () => finishFlyer(flyer);
        animation.oncancel = () => finishFlyer(flyer);
      }
    });
  });

  document.addEventListener('submit', (event) => {
    const form = event.target;
    if (!(form instanceof HTMLFormElement) || !form.matches('#cart-panel form[action*="/shop/cart/remove"]')) return;
    event.preventDefault();
    if (form.dataset.cartPending === 'true') return;
    form.dataset.cartPending = 'true';
    const button = form.querySelector('button[type="submit"]');
    if (button) button.disabled = true;
    void submitCartForm(form, button);
  });
})();
