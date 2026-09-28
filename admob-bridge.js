(() => {
  const install = () => {
    if (window.__dailyRewardsNativeAdBridge) return;
    if (!window.DailyRewardsAd || !window.DailyRewardsAd.showRewarded) {
      setTimeout(install, 500);
      return;
    }

    window.__dailyRewardsNativeAdBridge = true;
    let busy = false;
    let pendingButton = null;

    window.__dailyRewardsAdRewarded = () => {
      const button = pendingButton;
      pendingButton = null;
      busy = false;

      if (!button) return;
      button.setAttribute('data-dr-ad-allowed', '1');
      button.click();
    };

    window.__dailyRewardsAdFailed = (message) => {
      pendingButton = null;
      busy = false;
      console.error('Daily Rewards AdMob error:', message);
    };

    document.addEventListener('click', (event) => {
      let node = event.target;

      while (node && node !== document.body) {
        const text = (node.innerText || node.textContent || '')
          .replace(/\s+/g, ' ')
          .trim();

        if (text.includes('Watch & Earn')) {
          if (node.getAttribute('data-dr-ad-allowed') === '1') {
            node.removeAttribute('data-dr-ad-allowed');
            return;
          }

          event.preventDefault();
          event.stopImmediatePropagation();

          if (busy) return;

          busy = true;
          pendingButton = node;
          window.DailyRewardsAd.showRewarded();
          return;
        }

        node = node.parentElement;
      }
    }, true);
  };

  install();
})();
