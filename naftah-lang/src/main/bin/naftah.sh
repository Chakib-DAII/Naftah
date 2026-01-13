#!/usr/bin/env bash

# Resolve the directory of THIS wrapper script
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"

# Call the main script from the same folder, no matter where we are
"$SCRIPT_DIR/naftah-shell-wrapper.sh" run "$@"