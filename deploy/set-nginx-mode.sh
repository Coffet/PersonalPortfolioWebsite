#!/usr/bin/env bash
set -euo pipefail

if [[ "$(id -u)" -ne 0 ]]; then
  echo "Run as root: sudo bash deploy/set-nginx-mode.sh app|forefront|unpaid"
  exit 1
fi

if [[ "$#" -ne 1 || ( "$1" != "app" && "$1" != "forefront" && "$1" != "unpaid" ) ]]; then
  echo "Usage: sudo bash deploy/set-nginx-mode.sh app|forefront|unpaid"
  exit 2
fi

SNIPPET_DIR="/etc/nginx/snippets"
ACTIVE="$SNIPPET_DIR/portfolio-location.conf"

# "app" is the plain reverse proxy; the snippet is named after its role, not the mode.
case "$1" in
  app)       MODE_FILE="proxy" ;;
  forefront) MODE_FILE="forefront" ;;
  unpaid)    MODE_FILE="unpaid" ;;
esac

TARGET="$SNIPPET_DIR/portfolio-$MODE_FILE.conf"
TEMP="$ACTIVE.new.$$"

if [[ ! -L "$ACTIVE" ]]; then
  echo "$ACTIVE must be a symlink to portfolio-proxy.conf, portfolio-forefront.conf or portfolio-unpaid.conf"
  exit 1
fi

if [[ ! -f "$TARGET" ]]; then
  echo "Missing Nginx location snippet: $TARGET"
  exit 1
fi

PREVIOUS="$(readlink "$ACTIVE")"
cleanup() {
  rm -f "$TEMP"
}
trap cleanup EXIT

ln -s "$TARGET" "$TEMP"
mv -Tf "$TEMP" "$ACTIVE"

rollback() {
  ln -s "$PREVIOUS" "$TEMP"
  mv -Tf "$TEMP" "$ACTIVE"
}

if ! nginx -t; then
  rollback
  echo "Nginx config test failed; previous mode restored."
  exit 1
fi

if ! systemctl reload nginx; then
  rollback
  nginx -t && systemctl reload nginx || true
  echo "Nginx reload failed; previous mode restored."
  exit 1
fi

echo "Nginx mode set to $1."
