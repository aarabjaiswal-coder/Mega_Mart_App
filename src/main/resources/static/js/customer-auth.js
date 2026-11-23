const authButton = document.querySelector('#customer-auth-button');
const checkoutButton = document.querySelector('#checkout-google-sign-in');
const checkoutForm = document.querySelector('#checkout-form');
const tokenField = document.querySelector('#customer-access-token');
const authRequired = document.querySelector('#checkout-auth-required');
const ordersSection = document.querySelector('#customer-orders');
const ordersList = document.querySelector('#customer-orders-list');
const ordersHint = document.querySelector('#customer-orders-hint');

if (authButton || checkoutForm || ordersSection) {
  try {
    const configResponse = await fetch('/api/auth/config', { headers: { Accept: 'application/json' } });
    if (!configResponse.ok) throw new Error('Customer sign-in settings could not be loaded.');
    const config = await configResponse.json();
    if (!config.supabaseUrl || !config.publishableKey) throw new Error('Customer sign-in has not been configured yet.');
    const { createClient } = await import('https://esm.sh/@supabase/supabase-js@2');
    const supabase = createClient(config.supabaseUrl, config.publishableKey, {
      auth: { persistSession: true, autoRefreshToken: true, detectSessionInUrl: true }
    });

    const startGoogleSignIn = async () => {
      const returnTo = checkoutForm ? '/shop/checkout' : '';
      if (returnTo) sessionStorage.setItem('megaMartAuthReturnTo', returnTo);
      const { error } = await supabase.auth.signInWithOAuth({
        provider: 'google',
        options: { redirectTo: `${window.location.origin}/shop` }
      });
      if (error) window.alert(`Google sign-in could not start: ${error.message}`);
    };

    const make = (tag, className, text) => {
      const el = document.createElement(tag);
      if (className) el.className = className;
      if (text !== undefined) el.textContent = text;
      return el;
    };

    const loadCustomerOrders = async (session) => {
      if (!ordersSection || !ordersList) return;
      ordersSection.hidden = !session;
      if (!session) return;
      ordersList.replaceChildren(make('p', 'customer-orders-loading', 'Loading your orders…'));
      try {
        const response = await fetch('/api/customer/orders', {
          headers: { Authorization: `Bearer ${session.access_token}`, Accept: 'application/json' }
        });
        if (!response.ok) throw new Error('Could not load your orders. Please sign in again.');
        const orders = await response.json();
        ordersList.replaceChildren();
        if (!orders.length) {
          ordersHint.textContent = 'Your orders will appear here after you place one while signed in.';
          ordersList.append(make('p', 'customer-orders-empty', 'No orders on this account yet.'));
          return;
        }
        ordersHint.textContent = 'These orders are linked to your signed-in account.';
        for (const order of orders) {
          const card = make('article', 'customer-order-card');
          const head = make('div', 'customer-order-heading');
          const left = make('div');
          left.append(make('span', 'order-number', `Order #${order.orderId}`), make('small', '', 'Order status updates'));
          const statusValue = String(order.orderStatus || 'PLACED');
          const status = make('span', 'order-status', statusValue.replaceAll('_', ' '));
          status.dataset.status = statusValue;
          head.append(left, status);
          card.append(head);
          const progress = make('div', 'order-progress');
          for (const [label, states] of [['Placed', ['PLACED','PROCESSING','OUT_FOR_DELIVERY','DELIVERED']], ['Packing', ['PROCESSING','OUT_FOR_DELIVERY','DELIVERED']], ['On the way', ['OUT_FOR_DELIVERY','DELIVERED']], ['Delivered', ['DELIVERED']]]) {
            progress.append(make('span', states.includes(statusValue) ? 'step-done' : '', label));
          }
          card.append(progress);
          for (const item of (order.items || [])) card.append(make('p', 'order-items', `${item.quantity} × ${item.productName}`));
          const total = make('div', 'order-total');
          total.append(make('span', '', 'Cash on delivery'), make('b', '', `₹${order.totalAmount}`));
          card.append(total);
          ordersList.append(card);
        }
      } catch (error) {
        ordersList.replaceChildren(make('p', 'error-message', error.message));
      }
    };

    const syncSession = async (session) => {
      if (authButton) authButton.textContent = session ? 'Sign out' : 'Sign in with Google';
      if (tokenField) tokenField.value = session?.access_token || '';
      if (authRequired) authRequired.hidden = Boolean(session);
      if (checkoutForm) {
        const submitButton = checkoutForm.querySelector('.place-order-button');
        if (submitButton) submitButton.disabled = !session;
      }
      await loadCustomerOrders(session);
      if (session && window.location.pathname === '/shop') {
        const next = sessionStorage.getItem('megaMartAuthReturnTo');
        if (next === '/shop/checkout') {
          sessionStorage.removeItem('megaMartAuthReturnTo');
          window.location.replace(next);
        }
      }
    };

    // Subscribe before reading the session so the OAuth callback's initial session
    // event cannot be missed while this page is loading.
    supabase.auth.onAuthStateChange((_event, nextSession) => {
      queueMicrotask(() => { void syncSession(nextSession); });
    });
    const { data: { session } } = await supabase.auth.getSession();
    await syncSession(session);
    authButton?.addEventListener('click', async () => {
      const { data: { session: current } } = await supabase.auth.getSession();
      if (current) await supabase.auth.signOut();
      else await startGoogleSignIn();
    });
    checkoutButton?.addEventListener('click', startGoogleSignIn);
    checkoutForm?.addEventListener('submit', async (event) => {
      const { data: { session: current } } = await supabase.auth.getSession();
      if (!current) {
        event.preventDefault();
        authRequired.hidden = false;
      } else {
        tokenField.value = current.access_token;
      }
    });
    if (ordersSection) window.setInterval(async () => {
      const { data: { session: current } } = await supabase.auth.getSession();
      await loadCustomerOrders(current);
    }, 25000);
  } catch (error) {
    if (authButton) { authButton.disabled = true; authButton.title = error.message; }
    if (authRequired) { authRequired.hidden = false; authRequired.querySelector('p').textContent = error.message; }
    const submitButton = checkoutForm?.querySelector('.place-order-button');
    if (submitButton) submitButton.disabled = true;
  }
}
