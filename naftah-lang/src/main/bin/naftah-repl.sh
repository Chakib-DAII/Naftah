#!/usr/bin/env bash

# SPDX-License-Identifier: Apache-2.0
# Copyright © The Naftah Project Authors

# Resolve the directory of THIS wrapper script
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"

# Call the main script from the same folder, no matter where we are
"$SCRIPT_DIR/naftah-shell-wrapper.sh" shell "$@"