(() => {
  const install = () => {
    if (!window.Capacitor || !window.Capacitor.registerPlugin) {
      setTimeout(install, 1000);
      return;
    }

    if (window.__dailyRewardsAdMobBridge) return;
    const AdMob = window.Capacitor.registerPlugin('AdMob');
    if (!AdMob) {
      setTimeout(install, 1000);
      return;
    }

    window.__dailyRewardsAdMobBridge = true;
    let busy = false;

    async function showRewarded(button) {
      if (busy) return;
      busy = true;
      try {
        await AdMob.initialize();

        // Development/test build: Google's guaranteed rewarded test ad.
        await AdMob.prepareRewardVideoAd({
          adId: 'ca-app-pub-3940256099942544/5224354917',
          isTesting: true
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
  };

  install();
})();