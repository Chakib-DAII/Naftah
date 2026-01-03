#!/usr/bin/env bash

# Directory where original scripts live (bin/)
SRC_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"

# NAFTAH_HOME is parent of bin/
NAFTAH_HOME="$(dirname "$SRC_DIR")"

# Target for wrappers
BIN_DIR="/usr/local/bin"

# Scripts to install
SCRIPTS=(naftah-shell.sh naftah-repl.sh naftah-man.sh naftah-init.sh naftah.sh)

echo "Installing Naftah scripts..."

# Fix CRLFs and make originals executable
echo "Processing original scripts in $SRC_DIR ..."
for script in "${SCRIPTS[@]}"; do
    original="$SRC_DIR/$script"

    # Fix Windows CRLF endings
    sed -i 's/\r$//' "$original"

    # Make executable
    chmod +x "$original"

		# Ensure NAFTAH_HOME is exported in original
		# Only add if not already present
		if ! grep -q "export NAFTAH_HOME=" "$original"; then
				# Insert after shebang (line 1)
				sed -i "1a export NAFTAH_HOME=\"$NAFTAH_HOME\"" "$original"
		fi

done

# Install wrappers in /usr/local/bin
echo "Creating wrappers in $BIN_DIR ..."
for script in "${SCRIPTS[@]}"; do
    base_name="${script%.sh}"  # wrapper without .sh
    wrapper="$BIN_DIR/$base_name"

    echo "Installing wrapper $base_name ..."
    sudo tee "$wrapper" > /dev/null <<EOF
#!/usr/bin/env bash
export NAFTAH_HOME="$NAFTAH_HOME"
exec "\$NAFTAH_HOME/bin/$script" "\$@"
EOF

    sudo chmod +x "$wrapper"
done

echo
echo "Installation complete!"
echo "You can now run: ${SCRIPTS[*]/.sh/} from anywhere."
