(() => {
  const refreshInterval = 15000;
  let timer;
  const scheduleRefresh = () => {
    window.clearTimeout(timer);
    if (document.visibilityState === 'visible') {
      timer = window.setTimeout(() => window.location.reload(), refreshInterval);
    }
  };
  document.addEventListener('visibilitychange', scheduleRefresh);
  scheduleRefresh();
})();
