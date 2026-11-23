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
    message.textContent = 'We could not confirm that basket change. Check the basket before trying again.';
    feedback.replaceChildren(message);
  };

  const makeElement = (tag, className, text) => {
    const element = document.createElement(tag);
    if (className) element.className = className;
    if (text !== undefined) element.textContent = text;
    return element;
  };

  const money = (amount) => new Intl.NumberFormat('en-IN', {
    minimumFractionDigits: 2,
    maximumFractionDigits: 2
  }).format(Number(amount) || 0);

  const renderCartSummary = (summary) => {
    const currentCart = document.querySelector('#cart-panel');
    const currentCount = document.querySelector('#basket-count');
    if (!currentCart || !currentCount || !Array.isArray(summary.items)) throw new Error('Basket elements are unavailable.');

    const cart = document.createElement('aside');
    cart.className = 'cart-panel';
    cart.id = 'cart-panel';
    cart.setAttribute('aria-labelledby', 'cart-title');

    const title = makeElement('div', 'cart-title-row');
    title.append(makeElement('span', 'cart-symbol', '♧'));
    const titleCopy = makeElement('div');
    titleCopy.append(makeElement('p', 'eyebrow', 'YOUR GOOD STUFF'), makeElement('h2', '', 'Your basket'));
    titleCopy.querySelector('h2').id = 'cart-title';
    title.append(titleCopy, makeElement('span', 'cart-count', String(summary.itemCount)));
    cart.append(title);

    if (summary.items.length === 0) {
      cart.append(makeElement('p', 'cart-empty', 'Your basket is waiting for something lovely.'));
    } else {
      const csrfToken = document.querySelector('#cart-panel input[name="_csrf"]')?.value
        || document.querySelector('.add-product-form input[name="_csrf"]')?.value || '';
      summary.items.forEach((item) => {
        const line = makeElement('div', 'cart-line');
        const art = makeElement('span', 'cart-line-art');
        art.setAttribute('aria-hidden', 'true');
        const svg = document.createElementNS('http://www.w3.org/2000/svg', 'svg');
        svg.setAttribute('viewBox', '0 0 220 160');
        const use = document.createElementNS('http://www.w3.org/2000/svg', 'use');
        use.setAttribute('href', '/images/mega-mart-illustrations.svg#pantry');
        svg.append(use);
        art.append(svg);

        const copy = makeElement('span', 'cart-line-copy');
        copy.append(makeElement('b', '', item.productName), makeElement('small', '', `${item.quantity} × ₹${money(item.unitPrice)}`));
        const price = makeElement('strong', '', `₹${money(item.lineTotal)}`);
        const removeForm = document.createElement('form');
        removeForm.action = '/shop/cart/remove';
        removeForm.method = 'post';
        const csrf = makeElement('input');
        csrf.type = 'hidden'; csrf.name = '_csrf'; csrf.value = csrfToken;
        const productId = makeElement('input');
        productId.type = 'hidden'; productId.name = 'productId'; productId.value = String(item.productId);
        const removeButton = makeElement('button', 'remove-line', '×');
        removeButton.type = 'submit';
        removeButton.setAttribute('aria-label', `Remove ${item.productName}`);
        removeForm.append(csrf, productId, removeButton);
        line.append(art, copy, price, removeForm);
        cart.append(line);
      });

      const subtotal = makeElement('div', 'cart-subtotal');
      subtotal.append(makeElement('span', '', 'Subtotal'), makeElement('b', '', `₹${money(summary.subtotal)}`));
      cart.append(subtotal);
    }

    const payment = makeElement('p', 'cart-payment');
    payment.append(makeElement('span', '', '✓'), document.createTextNode(' Cash on delivery'));
    cart.append(payment);
    const checkout = makeElement('a', `checkout-button${summary.itemCount ? '' : ' checkout-disabled'}`);
    checkout.href = summary.itemCount ? '/shop/checkout' : '/shop#products';
    checkout.append(makeElement('span', '', summary.itemCount ? 'Continue to checkout' : 'Find something tasty'), makeElement('span', '', '↗'));
    cart.append(checkout, makeElement('p', 'cart-footnote', 'Your address and contact details are entered at checkout.'));

    currentCart.replaceWith(cart);
    currentCount.textContent = String(summary.itemCount);
    const feedback = document.querySelector('.feedback-stack');
    if (feedback) {
      feedback.replaceChildren();
      if (summary.message) {
        feedback.append(makeElement('p', `feedback-message ${summary.success ? 'success-message' : 'error-message'}`, summary.message));
      }
    }
  };

  const postCartForm = async (form) => {
    const response = await fetch(form.action, {
      method: (form.method || 'POST').toUpperCase(),
      body: new URLSearchParams(new FormData(form)),
      credentials: 'same-origin',
      headers: { Accept: 'application/json', 'X-Requested-With': 'XMLHttpRequest' }
    });
    const summary = await response.json();
    renderCartSummary(summary);
    if (!response.ok && summary.success) throw new Error('The basket update was not accepted.');
  };

  const submitCartForm = async (form, onComplete) => {
    try {
      await postCartForm(form);
    } catch {
      announceCartError();
    } finally {
      onComplete();
    }
  };

  const bindAddProductForms = () => document.querySelectorAll('.add-product-form').forEach((form) => {
    form.addEventListener('submit', (event) => {
      if (form.dataset.cartPending === 'true') { event.preventDefault(); return; }
      event.preventDefault();
      const button = form.querySelector('button[type="submit"]');
      const basket = document.querySelector('#basket-count');
      form.dataset.cartPending = 'true';
      if (button) button.disabled = true;
      let requestFinished = false;
      let animationFinished = false;
      const releaseButton = () => {
        if (!requestFinished || !animationFinished) return;
        form.dataset.cartPending = 'false';
        if (button) button.disabled = false;
      };
      void submitCartForm(form, () => { requestFinished = true; releaseButton(); });
      if (!button || !basket || typeof Element.prototype.animate !== 'function') {
        animationFinished = true;
        releaseButton();
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
      let animationTimer;
      const finishFlyer = (flyer) => {
        if (flyer.dataset.finished === 'true') return;
        flyer.dataset.finished = 'true';
        flyer.remove();
        remaining -= 1;
        if (remaining === 0) {
          window.clearTimeout(animationTimer);
          animationFinished = true;
          releaseButton();
        }
      };
      animationTimer = window.setTimeout(() => { animationFinished = true; releaseButton(); }, 1200);
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
  bindAddProductForms();

  document.addEventListener('submit', (event) => {
    const form = event.target;
    if (!(form instanceof HTMLFormElement) || !form.matches('#cart-panel form[action*="/shop/cart/remove"]')) return;
    event.preventDefault();
    if (form.dataset.cartPending === 'true') return;
    form.dataset.cartPending = 'true';
    const button = form.querySelector('button[type="submit"]');
    if (button) button.disabled = true;
    void submitCartForm(form, () => {
      form.dataset.cartPending = 'false';
      if (button) button.disabled = false;
    });
  });
})();
