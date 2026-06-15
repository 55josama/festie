#!/usr/bin/env bash

set -euo pipefail

apt-get update
DEBIAN_FRONTEND=noninteractive apt-get install -y docker.io docker-compose

systemctl enable --now docker
mkdir -p /opt/festie-monitoring
