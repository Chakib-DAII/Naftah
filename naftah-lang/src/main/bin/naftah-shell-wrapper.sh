#!/usr/bin/env bash

# Resolve the directory of this wrapper script
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"

# Default: check environment variable
USE_XTERM="${NAFTAH_XTERM:-false}"

# Command-line flag takes precedence
ARGS=()

for arg in "$@"; do
    case "$arg" in
        --xterm)
            USE_XTERM=true
            ;;
        *)
            ARGS+=("$arg")
            ;;
    esac
done

# Define xterm options
XTERM_CMD="xterm"
XTERM_FONT="DejaVu Sans Mono"
XTERM_FONTSIZE="12"

XTERM_OPTS=(
  -u8
  -fa "$XTERM_FONT"
  -fs "$XTERM_FONTSIZE"
  -tn xterm-256color
  -T "Naftah Lang"
  -maximized
  -sp
  -sf
  -xrm "XTerm*metaSendsEscape: true"
  -xrm "XTerm*selectToClipboard: true"
  -xrm "XTerm*allowMouseOps: true"
)

# Run either directly or inside xterm
if [[ "$USE_XTERM" == "true" || "$USE_XTERM" == "1" ]]; then
    exec "$XTERM_CMD" "${XTERM_OPTS[@]}" -e "$SCRIPT_DIR/naftah-shell.sh" "${ARGS[@]}"
else
    exec "$SCRIPT_DIR/naftah-shell.sh" "${ARGS[@]}"
fi

