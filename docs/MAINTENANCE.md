# Maintenance mode on the Linode box

Nginx is the public door; Java only listens on `127.0.0.1:8080`. Whether visitors
get the app or a static page is decided by the location snippet that the site
config includes:

```
/etc/nginx/sites-available/portfolio
└── include /etc/nginx/snippets/portfolio-location.conf   (a symlink)
        ├── portfolio-proxy.conf      -> Java app          (mode: app)
        ├── portfolio-forefront.conf  -> /var/www/forefront (mode: forefront)
        └── portfolio-unpaid.conf     -> /var/www/unpaid    (mode: unpaid)
```

`portfolio-site-mode` rewrites the symlink, runs `nginx -t`, reloads, and rolls
the symlink back if either step fails. No request is dropped when the app is
already down.

## The two lines you asked about

```bash
sudo portfolio-site-mode forefront   # maintenance page on, app can be stopped
sudo portfolio-site-mode app         # back to the Java app
```

```bash
sudo systemctl stop portfolio.service     # stop the app (maintenance window)
sudo systemctl start portfolio.service    # start it again
```

`unpaid` is the same switch with the "Off for now" copy instead of
"Back in a moment".

## Planned maintenance (the sequence used on 2026-10-23)

```bash
# 1. Put the static page up first, so the site never shows a Cloudflare error.
sudo portfolio-site-mode forefront

# 2. Do the work. The app can be stopped, restarted, or replaced freely now.
sudo systemctl stop portfolio.service
sudo apt-get update
sudo apt-get install --only-upgrade openjdk-17-jre-headless   # or the WAR swap below
sudo systemctl start portfolio.service
curl -fsS http://127.0.0.1:8080/ >/dev/null && echo app-is-up

# 3. Put the proxy back.
sudo portfolio-site-mode app
```

For a WAR swap there is no need to stop the unit by hand:

```bash
scp target/portfolio.war root@SERVER:/home/deploy/portfolio-app/portfolio.war.new
ssh root@SERVER 'bash /home/deploy/remote-release.sh'
```

`remote-release.sh` keeps `portfolio.war.bak`, restarts the unit, polls
`127.0.0.1:8080` for up to 60 s, and rolls the previous WAR back if the new one
never answers. Run it while the forefront page is up so visitors never see the
restart.

## Verify from outside

```bash
curl -sI https://coft.moe/ | head -1                     # expect 200
ssh root@SERVER 'bash /tmp/ppw/probe.sh whatever'        # prints which page is live
```

The page markers are `Back in a moment` (forefront) and `Off for now` (unpaid);
the Java app has neither.

## When the box itself is gone (suspended / powered off)

Nothing on the server can help — a suspended Linode has no running nginx.
Cloudflare still answers for the domain, so the notice has to come from there:
`deploy/cloudflare/unpaid-fallback-worker.js` proxies to the origin and answers
with the "Off for now" page when the origin times out, returns 5xx, **or** when
the Linode account has an overdue open invoice (set `LINODE_TOKEN` to enable
that check). Deploy instructions are in the file header.

Until that Worker is deployed, an origin that is up but whose app is dead can
still be covered by `deploy/forefront/nginx-notice.conf` (flag file
`/etc/portfolio-down` + `error_page 502 503 504 =503 /notice.html`).

## Gotchas

- `nginx -t` + `reload` is not instantaneous: a request issued in the same
  second can still hit an old worker. Probe twice before believing a result.
- Never run `sed -i` over `/etc/nginx/snippets/portfolio-*.conf`: the glob
  includes the `portfolio-location.conf` symlink and `sed -i` replaces it with a
  regular file, which trips the mode script's symlink check. Re-create it with
  `ln -sfn /etc/nginx/snippets/portfolio-proxy.conf /etc/nginx/snippets/portfolio-location.conf`.
- These scripts ship with LF endings (see `.gitattributes`); a CRLF checkout
  breaks the shebang when copied to Ubuntu.
