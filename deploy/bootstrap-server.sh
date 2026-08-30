#!/bin/bash
# One-time Linode setup. Run as root from a checkout of this repo:
#   sudo bash deploy/bootstrap-server.sh
# After this, GitHub Actions copies only portfolio.war. It does not git-pull source.
set -euo pipefail

if [ "$(id -u)" -ne 0 ]; then
  echo "Run as root: sudo bash deploy/bootstrap-server.sh"
  exit 1
fi

SCRIPT_DIR="$(cd "$(dirname "$0")" && pwd)"
APP_DIR="/home/deploy/portfolio-app"

export DEBIAN_FRONTEND=noninteractive
apt-get update -y
apt-get install -y openjdk-17-jre-headless nginx curl ufw openssl

id deploy >/dev/null 2>&1 || useradd --create-home --shell /bin/bash deploy

mkdir -p \
  "$APP_DIR/storage/database" \
  "$APP_DIR/storage/uploads/projects" \
  "$APP_DIR/storage/uploads/gallery" \
  "$APP_DIR/storage/uploads/blog" \
  /home/deploy/.ssh \
  /etc/nginx/snippets \
  /etc/ssl/cf

chmod 700 /home/deploy/.ssh
touch /home/deploy/.ssh/authorized_keys
chmod 600 /home/deploy/.ssh/authorized_keys

if [ ! -f /etc/ssl/cf/coft.moe.pem ]; then
  openssl req -x509 -nodes -days 825 -newkey rsa:2048 \
    -keyout /etc/ssl/cf/coft.moe.key \
    -out /etc/ssl/cf/coft.moe.pem \
    -subj "/CN=coft.moe"
  chmod 600 /etc/ssl/cf/coft.moe.key
fi

install -m 644 "$SCRIPT_DIR/nginx-security-headers.conf" /etc/nginx/snippets/nginx-security-headers.conf
install -m 644 "$SCRIPT_DIR/nginx-site.example.conf" /etc/nginx/sites-available/portfolio
install -m 644 "$SCRIPT_DIR/portfolio.service" /etc/systemd/system/portfolio.service
install -m 440 "$SCRIPT_DIR/sudoers-deploy" /etc/sudoers.d/portfolio-deploy
visudo -c -f /etc/sudoers.d/portfolio-deploy

if [ ! -f /etc/portfolio.env ]; then
  install -m 600 "$SCRIPT_DIR/portfolio.env.example" /etc/portfolio.env
  echo "Edit /etc/portfolio.env and set CMS seed credentials before the first start."
fi

chown -R deploy:deploy /home/deploy
rm -f /etc/nginx/sites-enabled/default
ln -sfn /etc/nginx/sites-available/portfolio /etc/nginx/sites-enabled/portfolio
nginx -t

systemctl daemon-reload
systemctl enable nginx portfolio.service
systemctl reload nginx || systemctl start nginx

ufw allow OpenSSH
ufw allow 80/tcp
ufw allow 443/tcp
ufw --force enable

if [ ! -f /home/deploy/.ssh/github_actions_deploy.pub ]; then
  sudo -u deploy ssh-keygen -t ed25519 -N "" -C "github-actions-deploy" \
    -f /home/deploy/.ssh/github_actions_deploy
fi
PUB="$(cat /home/deploy/.ssh/github_actions_deploy.pub)"
grep -qxF "$PUB" /home/deploy/.ssh/authorized_keys || echo "$PUB" >> /home/deploy/.ssh/authorized_keys
chmod 600 /home/deploy/.ssh/authorized_keys
chown deploy:deploy /home/deploy/.ssh/authorized_keys

echo
echo "Bootstrap done. Add GitHub Actions secrets, then push to main:"
echo "  DEPLOY_HOST     this server IP"
echo "  DEPLOY_USER     deploy"
echo "  DEPLOY_SSH_KEY  output of: sudo cat /home/deploy/.ssh/github_actions_deploy"
echo
echo "First deploy starts Java when GitHub uploads portfolio.war."
echo "Until then, nginx is up but proxying to a process that is not running yet."
