#!/bin/bash
set -euo pipefail

DATA_DIR="${DATA_DIR:-/mnt/data}"
DOWNLOADS_DIR="${DOWNLOADS_DIR:-/tmp/downloads}"

mkdir -p "$DATA_DIR/json"
mkdir -p "$DOWNLOADS_DIR"

java \
    -Djava.awt.headless=true \
    -Dwebdriver.chrome.driver=/opt/drivers/chromedriver \
    -Dwebdriver.chrome.logfile=/var/log/chromedriver.log \
    -DdataDir="$DATA_DIR" \
    -DdownloadsDir="$DOWNLOADS_DIR" \
    -jar /app/langdb-extract-1.0.0.jar
