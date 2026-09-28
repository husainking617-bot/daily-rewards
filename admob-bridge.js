(() => {
  if (window.__dailyRewardsAdMobBridge) return;
  window.__dailyRewardsAdMobBridge = true;

  const AdMob = window.Capacitor && window.Capacitor.registerPlugin
    ? window.Capacitor.registerPlugin('AdMob')
    : null;
  if (!AdMob) return;

  let busy = false;

  async function showRewarded(button) {
    if (busy) return;
    busy = true;
    try {
      await AdMob.initialize();
      await AdMob.prepareRewardVideoAd({
        adId: 'ca-app-pub-8752731937658252/9164542097'
      });
      await AdMob.showRewardVideoAd();

      button.setAttribute('data-dr-ad-allowed', '1');
      button.click();
    } catch (error) {
      console.error('Daily Rewards AdMob error', error);
    } finally {
      busy = false;
    }
  }

  document.addEventListener('click', (event) => {
    let node = event.target;
    while (node && node !== document.body) {
      const text = (node.innerText || node.textContent || '').replace(/\s+/g, ' ').trim();
      if (text.includes('Watch & Earn')) {
        if (node.getAttribute('data-dr-ad-allowed') === '1') {
          node.removeAttribute('data-dr-ad-allowed');
          return;
        }
        event.preventDefault();
        event.stopImmediatePropagation();
        showRewarded(node);
        return;
      }
      node = node.parentElement;
    }
  }, true);
})();