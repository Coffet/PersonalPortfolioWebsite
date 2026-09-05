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

export DEBIAN_FRONTEND=noninteractive
apt-get update -y
apt-get install -y curl ca-certificates

if [ ! -x "$BIN" ]; then
  curl -fsSL https://dl.min.io/server/minio/release/linux-amd64/minio -o "$BIN"
  chmod 755 "$BIN"
fi

id minio-user >/dev/null 2>&1 || useradd --system --home "$DATA_DIR" --shell /usr/sbin/nologin minio-user
mkdir -p "$DATA_DIR"
chown -R minio-user:minio-user "$DATA_DIR"

if [ ! -f /etc/minio.env ]; then
  install -m 600 "$SCRIPT_DIR/minio.env.example" /etc/minio.env
  echo "Edit /etc/minio.env and replace the change-me MinIO user/password before start."
fi

install -m 644 "$SCRIPT_DIR/minio.service" /etc/systemd/system/minio.service
systemctl daemon-reload
systemctl enable minio.service

echo
echo "MinIO unit installed. It listens on 127.0.0.1:9000 (console 127.0.0.1:9001)."
echo "Set real values in /etc/minio.env, then: systemctl start minio"
echo "Point portfolio env at http://127.0.0.1:9000. Do not ufw allow 9000."
echo "git push does not start MinIO or migrate files."
