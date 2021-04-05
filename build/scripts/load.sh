#!/bin/bash
set -euo pipefail

DATA_DIR="${DATA_DIR:-/mnt/data}"

java -DdataDir=$DATA_DIR -jar /app/langdb-load-1.0.0.jar
