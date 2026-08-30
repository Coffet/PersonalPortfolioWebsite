#!/bin/bash
# Runs on the Linode as the deploy user. Swaps the WAR and restarts systemd.
# Does not touch storage/database or storage/uploads.
set -euo pipefail

APP_DIR="${APP_DIR:-/home/deploy/portfolio-app}"
NEW_WAR="$APP_DIR/portfolio.war.new"
CUR_WAR="$APP_DIR/portfolio.war"
BAK_WAR="$APP_DIR/portfolio.war.bak"

if [ ! -f "$NEW_WAR" ]; then
  echo "Missing $NEW_WAR"
  exit 1
fi

mkdir -p \
  "$APP_DIR/storage/database" \
  "$APP_DIR/storage/uploads/projects" \
  "$APP_DIR/storage/uploads/gallery" \
  "$APP_DIR/storage/uploads/blog"

if [ -f "$CUR_WAR" ]; then
  cp -a "$CUR_WAR" "$BAK_WAR"
fi
mv -f "$NEW_WAR" "$CUR_WAR"

sudo systemctl restart portfolio.service

healthy=0
for _ in $(seq 1 30); do
  if curl -sf --max-time 3 http://127.0.0.1:8080/ >/dev/null; then
    healthy=1
    break
  fi
  sleep 2
done

if [ "$healthy" -eq 1 ]; then
  echo "Release healthy"
  exit 0
fi

echo "Release unhealthy; rolling back"
if [ -f "$BAK_WAR" ]; then
  mv -f "$BAK_WAR" "$CUR_WAR"
  sudo systemctl restart portfolio.service
fi
exit 1
