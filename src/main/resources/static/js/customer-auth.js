const authButton = document.querySelector('#customer-auth-button');
const checkoutButton = document.querySelector('#checkout-google-sign-in');
const checkoutForm = document.querySelector('#checkout-form');
const tokenField = document.querySelector('#customer-access-token');
const authRequired = document.querySelector('#checkout-auth-required');
const ordersSection = document.querySelector('#customer-orders');
const ordersList = document.querySelector('#customer-orders-list');
const ordersHint = document.querySelector('#customer-orders-hint');
const welcomeAuthMessage = document.querySelector('#welcome-auth-message');
const customerAuthPage = document.body?.dataset.requireCustomerAuth === 'true';
let welcomeRedirectStarted = false;

if (welcomeAuthMessage && new URLSearchParams(window.location.search).get('signin') === 'required') {
  welcomeAuthMessage.textContent = 'Sign in with Google to enter the shop and place orders.';
} else if (welcomeAuthMessage && new URLSearchParams(window.location.search).get('signin') === 'unavailable') {
  welcomeAuthMessage.textContent = 'Sign-in could not be checked. Please try again or contact the store.';
}

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
      if (customerAuthPage && !session) {
        window.location.replace('/?signin=required');
        return;
      }
      if (customerAuthPage) document.body.dataset.authReady = 'true';

      if (session && window.location.pathname === '/' && !welcomeRedirectStarted) {
        welcomeRedirectStarted = true;
        const next = sessionStorage.getItem('megaMartAuthReturnTo');
        sessionStorage.removeItem('megaMartAuthReturnTo');
        window.location.replace(next === '/shop/checkout' ? next : '/shop');
        return;
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
    // event cannot be missed while this page is loading. Use getSession as the
    // primary value to avoid acting on an early INITIAL_SESSION callback race.
    let sessionReadComplete = false;
    let pendingSession;
    supabase.auth.onAuthStateChange((_event, nextSession) => {
      if (!sessionReadComplete) {
        pendingSession = nextSession;
        return;
      }
      queueMicrotask(() => { void syncSession(nextSession); });
    });
    const { data: { session } } = await supabase.auth.getSession();
    sessionReadComplete = true;
    await syncSession(session || pendingSession || null);
    authButton?.addEventListener('click', async () => {
      const { data: { session: current } } = await supabase.auth.getSession();
      if (current) await supabase.auth.signOut();
      else await startGoogleSignIn();
    });
    checkoutButton?.addEventListener('click', startGoogleSignIn);
    checkoutForm?.addEventListener('submit', (event) => {
      // Stop the browser immediately: waiting for getSession() before preventing
      // the default submit can send the order before its bearer token is filled.
      event.preventDefault();
      if (checkoutForm.dataset.submitting === 'true') return;
      checkoutForm.dataset.submitting = 'true';
      const submitButton = checkoutForm.querySelector('.place-order-button');
      if (submitButton) submitButton.disabled = true;

      void (async () => {
        try {
          const { data: { session: current }, error } = await supabase.auth.getSession();
          if (error) throw error;
          if (!current) {
            checkoutForm.dataset.submitting = 'false';
            if (authRequired) authRequired.hidden = false;
            return;
          }

          tokenField.value = current.access_token;
          HTMLFormElement.prototype.submit.call(checkoutForm);
        } catch (error) {
          checkoutForm.dataset.submitting = 'false';
          if (authRequired) {
            authRequired.hidden = false;
            const message = authRequired.querySelector('p');
            if (message) message.textContent = 'We could not confirm your Google sign-in. Please sign in again before placing the order.';
          }
        }
      })();
    });
    if (ordersSection) window.setInterval(async () => {
      const { data: { session: current } } = await supabase.auth.getSession();
      await loadCustomerOrders(current);
    }, 25000);
  } catch (error) {
    if (authButton) { authButton.disabled = true; authButton.title = error.message; }
    if (authRequired) { authRequired.hidden = false; authRequired.querySelector('p').textContent = error.message; }
    if (welcomeAuthMessage) welcomeAuthMessage.textContent = error.message;
    const submitButton = checkoutForm?.querySelector('.place-order-button');
    if (submitButton) submitButton.disabled = true;
    if (customerAuthPage) window.location.replace('/?signin=unavailable');
  }
}
