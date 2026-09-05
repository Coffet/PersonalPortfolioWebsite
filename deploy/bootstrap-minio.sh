#!/bin/bash
# Optional. Run as root after deploy/bootstrap-server.sh if you want MinIO
# on this VPS. Binds to 127.0.0.1 only. Does not open 9000/9001 on ufw.
#   sudo bash deploy/bootstrap-minio.sh
set -euo pipefail

if [ "$(id -u)" -ne 0 ]; then
  echo "Run as root: sudo bash deploy/bootstrap-minio.sh"
  exit 1
fi

SCRIPT_DIR="$(cd "$(dirname "$0")" && pwd)"
DATA_DIR="/var/lib/minio"
BIN="/usr/local/bin/minio"
MC_BIN="/usr/local/bin/mc"

# Pinned community builds + hashes from dl.min.io (do not use the floating /minio URL).
MINIO_RELEASE="RELEASE.2025-09-07T16-13-09Z"
MINIO_URL="https://dl.min.io/server/minio/release/linux-amd64/archive/minio.${MINIO_RELEASE}"
MINIO_SHA256="7c5bd8512c6e966455b1d198209358b2d191c77a83ab377c4073281065fb855f"
MC_RELEASE="RELEASE.2025-08-13T08-35-41Z"
MC_URL="https://dl.min.io/client/mc/release/linux-amd64/archive/mc.${MC_RELEASE}"
MC_SHA256="01f866e9c5f9b87c2b09116fa5d7c06695b106242d829a8bb32990c00312e891"

# install_pinned_binary downloads a binary, verifies its SHA-256 checksum, and installs it with executable permissions.
install_pinned_binary() {
  local dest="$1"
  local url="$2"
  local expected_sha="$3"
  local tmp
  tmp="$(mktemp)"
  curl -fsSL "$url" -o "$tmp"
  local actual
  actual="$(sha256sum "$tmp" | awk '{print $1}')"
  if [ "$actual" != "$expected_sha" ]; then
    rm -f "$tmp"
    echo "Checksum mismatch for ${dest}. Aborting; existing binary (if any) was not replaced." >&2
    exit 1
  fi
  install -m 755 "$tmp" "$dest"
  rm -f "$tmp"
}

export DEBIAN_FRONTEND=noninteractive
apt-get update -y
apt-get install -y curl ca-certificates coreutils openssl

if [ ! -x "$BIN" ]; then
  install_pinned_binary "$BIN" "$MINIO_URL" "$MINIO_SHA256"
fi

if [ ! -x "$MC_BIN" ]; then
  install_pinned_binary "$MC_BIN" "$MC_URL" "$MC_SHA256"
fi

id minio-user >/dev/null 2>&1 || useradd --system --home "$DATA_DIR" --shell /usr/sbin/nologin minio-user
mkdir -p "$DATA_DIR" /etc/minio/certs
if [ ! -f /etc/minio/certs/public.crt ]; then
  openssl req -x509 -nodes -days 825 -newkey rsa:2048 \
    -keyout /etc/minio/certs/private.key \
    -out /etc/minio/certs/public.crt \
    -subj "/CN=127.0.0.1" \
    -addext "subjectAltName=IP:127.0.0.1,DNS:localhost"
  chmod 600 /etc/minio/certs/private.key
  chmod 644 /etc/minio/certs/public.crt
fi
chown -R minio-user:minio-user "$DATA_DIR" /etc/minio

if [ ! -f /etc/minio.env ]; then
  install -m 600 "$SCRIPT_DIR/minio.env.example" /etc/minio.env
  echo "Edit /etc/minio.env and replace the change-me MinIO root user/password before start."
fi

install -m 644 "$SCRIPT_DIR/minio.service" /etc/systemd/system/minio.service
systemctl daemon-reload
systemctl enable minio.service

echo
echo "MinIO unit installed. It listens on https://127.0.0.1:9000 (console 127.0.0.1:9001)."
echo "Set real root values in /etc/minio.env, then: systemctl start minio"
echo "Then provision the app bucket + scoped user: sudo bash deploy/provision-minio-app.sh"
echo "Put that app user in portfolio env — not MINIO_ROOT_USER. Do not ufw allow 9000."
echo "git push does not start MinIO or migrate files."
