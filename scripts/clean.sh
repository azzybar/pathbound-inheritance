#!/usr/bin/env bash

set -euo pipefail
cd "$(dirname "$0")/.."

DIRS=("build/classes" "build/jar" "build/input" "build/dist")

for dir in "${DIRS[@]}"; do
    if [[ -d "${dir}" ]]; then
        rm -rf "${dir}"
        echo "Removed ${dir}"

        mkdir -p "${dir}"
        echo "Created ${dir}"
    fi
done
