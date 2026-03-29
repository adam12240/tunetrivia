import { useEffect, useRef, useState } from 'react';

export default function AdSense({ adClient, adSlot, style = { display: 'block' }, test }) {
  const ref = useRef(null);
  const [loaded, setLoaded] = useState(false);

  // if `test` is not explicitly provided, read from Vite env VITE_ADS_TEST
  const isTest = typeof test === 'boolean' ? test : (import.meta.env.VITE_ADS_TEST === 'true');

  useEffect(() => {
    if (typeof window === 'undefined') return;
    const w = window;
    try {
      const existing = document.querySelector('script[data-adsbygoogle-client]') || document.querySelector('script[src*="pagead2.googlesyndication.com/pagead/js/adsbygoogle.js"]');
      if (!existing) {
        const script = document.createElement('script');
        script.async = true;
        const clientQuery = adClient ? `?client=${adClient}` : '';
        script.src = `https://pagead2.googlesyndication.com/pagead/js/adsbygoogle.js${clientQuery}`;
        script.setAttribute('crossorigin', 'anonymous');
        script.setAttribute('data-adsbygoogle-client', adClient || '');
        document.head.appendChild(script);
        script.onload = () => {
          try { (w.adsbygoogle = w.adsbygoogle || []).push({}); } catch (e) { void e; }
          setLoaded(true);
        };
        script.onerror = () => setLoaded(false);
      } else {
        try { (w.adsbygoogle = w.adsbygoogle || []).push({}); } catch (e) { void e; }
        setLoaded(true);
      }

      try { (w.adsbygoogle = w.adsbygoogle || []).push({}); } catch (e) { void e; }
    } catch (e) { void e; }
  }, [adClient]);

  const dataAttrs = {};
  if (isTest) dataAttrs['data-adtest'] = 'on';

  // If required config missing or script didn't load, render a harmless visual placeholder to help layout testing
  const isPlaceholderClient = adClient && String(adClient).includes('PLACEHOLDER');
  const showPlaceholder = isTest && (isPlaceholderClient || !adClient || !adSlot || (!loaded && isTest));
  if (showPlaceholder) {
    return (
      <div className="ad-placeholder" style={{ width: style.width || 160, height: style.height || 600 }}>
        {isTest ? 'Ad placeholder (test mode)' : 'Ad placeholder'}
      </div>
    );
  }

  return (
    <ins
      className="adsbygoogle"
      ref={ref}
      style={style}
      data-ad-client={adClient}
      data-ad-slot={adSlot}
      data-ad-format="auto"
      data-full-width-responsive="true"
      {...dataAttrs}
    />
  );
}
