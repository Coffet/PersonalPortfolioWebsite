// Cloudflare Worker: answer for coft.moe when the Linode box cannot.
//
// Why this lives in Cloudflare: a suspended (unpaid) Linode is powered off and
// unreachable, so /var/www/forefront can never be served from the box itself.
// Cloudflare still answers for the domain, so the notice has to be produced here.
//
// What it does, in order:
//   1. Optional billing check (needs LINODE_TOKEN): if the Linode account has an
//      open invoice past its due date, show the notice even though the origin is up.
//   2. Otherwise proxy the request to the origin (the Worker route itself is
//      bypassed for subrequests, so this reaches nginx on the Linode).
//   3. If the origin times out or answers 5xx, show the notice instead.
//
// Deploy (dashboard):
//   Workers & Pages -> Create -> Worker -> paste this file -> Deploy
//   Settings -> Variables:
//     LINODE_TOKEN  (secret, optional)  Linode API token with account read scope
//   Settings -> Triggers -> Add route:
//     coft.moe/*        zone: coft.moe
//     www.coft.moe/*    zone: coft.moe
//
// Deploy (wrangler):
//   npx wrangler deploy deploy/cloudflare/unpaid-fallback-worker.js \
//     --name coft-fallback --route "coft.moe/*" --route "www.coft.moe/*"
//   npx wrangler secret put LINODE_TOKEN     # optional
//
// Test after deploying: stop the box (or `portfolio-site-mode unpaid` is NOT
// enough - that answers 200 from nginx). Use Linode's "Power Off" once and
// confirm the notice appears.

const ORIGIN_TIMEOUT_MS = 4000;
const BILLING_TTL_MS = 5 * 60 * 1000;

let billingCache = { checkedAt: 0, unpaid: false };

async function linodeLooksUnpaid(env) {
  if (!env || !env.LINODE_TOKEN) return false;

  const now = Date.now();
  if (now - billingCache.checkedAt < BILLING_TTL_MS) return billingCache.unpaid;

  let unpaid = false;
  try {
    const res = await fetch(
      "https://api.linode.com/v4/account/invoices?page_size=100&status=open",
      {
        headers: {
          Authorization: `Bearer ${env.LINODE_TOKEN}`,
          "X-Filter": JSON.stringify({ status: "open" }),
        },
        signal: AbortSignal.timeout(ORIGIN_TIMEOUT_MS),
      },
    );
    if (res.ok) {
      const body = await res.json();
      const today = new Date().toISOString().slice(0, 10);
      unpaid = (body.data || []).some(
        (invoice) => typeof invoice.due === "string" && invoice.due.slice(0, 10) < today,
      );
    }
  } catch {
    // API unreachable: fall through to the origin probe instead of guessing.
  }

  billingCache = { checkedAt: now, unpaid };
  return unpaid;
}

function noticeResponse(status = 503, retryAfter = 60) {
  return new Response(NOTICE_HTML, {
    status,
    headers: {
      "Content-Type": "text/html; charset=utf-8",
      "Cache-Control": "no-store",
      "Retry-After": String(retryAfter),
    },
  });
}

export default {
  async fetch(request, env) {
    if (await linodeLooksUnpaid(env)) {
      return noticeResponse(503);
    }

    let response;
    try {
      // Subrequests from a Worker to its own zone go to the origin, not back here.
      response = await fetch(request, { signal: AbortSignal.timeout(ORIGIN_TIMEOUT_MS * 3) });
    } catch {
      return noticeResponse(503);
    }

    if (response.status >= 500) {
      return noticeResponse(503);
    }

    return response;
  },
};

const NOTICE_HTML = `<!DOCTYPE html>
<html lang="en">
<head>
<meta charset="utf-8">
<meta name="viewport" content="width=device-width, initial-scale=1, viewport-fit=cover">
<meta name="color-scheme" content="dark">
<meta name="robots" content="noindex, nofollow">
<meta name="theme-color" content="#000000">
<meta http-equiv="refresh" content="60">
<title>coft</title>
<link rel="preconnect" href="https://fonts.googleapis.com">
<link rel="preconnect" href="https://fonts.gstatic.com" crossorigin>
<link href="https://fonts.googleapis.com/css2?family=Instrument+Sans:wght@400;600&display=swap" rel="stylesheet">
<link rel="icon" type="image/png" href="data:image/png;base64,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">
<style>
:root{--bg:#000;--text:#f2f2f2;--soft:#9b9b9b;--accent:#3eb297;
--font:"Instrument Sans","Segoe UI Variable Text","Segoe UI",system-ui,-apple-system,sans-serif}
*{box-sizing:border-box}
body{margin:0;min-height:100dvh;display:grid;place-items:center;
padding:clamp(26px,4vw,52px) clamp(24px,6vw,56px);background:var(--bg);color:var(--text);
font-family:var(--font);font-size:16px;line-height:1.6;-webkit-font-smoothing:antialiased}
.page{width:min(100%,42rem);display:grid;justify-items:center;text-align:center;gap:20px}
h1{margin:0;font-size:clamp(2.4rem,1.5rem+4vw,3.6rem);font-weight:600;line-height:1.1;
letter-spacing:-.03em;text-wrap:balance}
p{margin:0;max-width:28rem;color:var(--soft);font-size:.95rem}
a{color:var(--text);text-underline-offset:4px;text-decoration-color:var(--accent)}
.links{display:flex;gap:22px;justify-content:center;flex-wrap:wrap;font-size:.9rem}
</style>
</head>
<body>
<main class="page">
<h1>Off for now</h1>
<p>The site is temporarily unavailable. It comes back on its own &mdash; this page retries every minute.</p>
<p class="links"><a href="/">Check again</a><a href="mailto:blaze459077@gmail.com">Email</a></p>
</main>
</body>
</html>`;
