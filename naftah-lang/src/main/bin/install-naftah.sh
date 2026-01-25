#!/usr/bin/env bash

# SPDX-License-Identifier: Apache-2.0
# Copyright © The Naftah Project Authors

# ------------------------------------------------------------
# install_arabic_fonts
#
# Installs recommended Arabic fonts for the system.
#
# Behavior:
# - Detects the available package manager on the system.
# - Installs a curated set of fonts for Arabic display:
#     • Kacst fonts
#     • Amiri font
#     • Noto fonts (core, extra, UI, color emoji)
#     • DejaVu fonts
#     • IBM Plex fonts (for terminals and coding)
# - Rebuilds the font cache after installation.
# - Falls back gracefully if no supported package manager is found.
#
# Supported package managers:
# - apt     (Debian / Ubuntu)
# - dnf     (Fedora / RHEL)
# - yum     (Legacy RHEL / CentOS)
# - pacman  (Arch Linux)
# - zypper  (openSUSE)
# - apk     (Alpine Linux)
#
# Notes:
# - Requires sudo privileges.
# - Should work on most Linux distributions with GUI support.
# - If no supported package manager is detected, fonts must be installed manually.
#
# Usage:
#   install_arabic_fonts
#
# ------------------------------------------------------------
install_arabic_fonts() {
    echo "جاري تثبيت الخطوط العربية..."

    if command -v apt >/dev/null; then
        sudo apt update
        sudo apt install -y \
            fonts-kacst \
            fonts-kacst-one \
            fonts-amiri-core \
            fonts-noto-core \
            fonts-noto-extra \
            fonts-noto-ui-core \
            fonts-noto-color-emoji \
            fonts-dejavu-core \
            fonts-ibm-plex || true 

    elif command -v dnf >/dev/null; then
        sudo dnf install -y \
            kacst-fonts \
            amiri-fonts \
            google-noto-sans-arabic-fonts \
            dejavu-sans-fonts \
            ibm-plex-fonts --skip-unavailable

    elif command -v yum >/dev/null; then
        sudo yum install -y \
            kacst-fonts \
            amiri-fonts \
            google-noto-sans-arabic-fonts \
            dejavu-sans-fonts \
            ibm-plex-fonts --skip-broken

    elif command -v pacman >/dev/null; then
        sudo pacman -Sy --noconfirm --needed \
            ttf-kacst \
            ttf-amiri \
            noto-fonts \
            ttf-dejavu \
            ttf-ibm-plex || true 

    elif command -v zypper >/dev/null; then
        sudo zypper install -y \
            kacst-fonts \
            amiri-fonts \
            google-noto-sans-arabic-fonts \
            dejavu-sans-fonts \
            ibm-plex-fonts || true 

    elif command -v apk >/dev/null; then
        sudo apk add \
            ttf-kacst \
            ttf-amiri \
            noto-fonts \
            ttf-dejavu || true 

    else
        echo "لا يوجد مدير حزم مدعوم لتثبيت الخطوط تلقائياً."
        echo "الرجاء تثبيت الخطوط العربية يدوياً."
    fi


    echo "إعادة بناء ذاكرة الخطوط..."
    fc-cache -f -v
}



# ------------------------------------------------------------
# install_xterm
#
# Ensure that the `xterm` terminal emulator is installed.
#
# Behavior:
# - Checks whether `xterm` already exists in PATH.
# - If installed, prints a confirmation message and exits.
# - If not installed, attempts to install it using the
#   available package manager.
# - Falls back gracefully if no supported package manager
#   is found.
#
# Supported package managers:
# - apt     (Debian / Ubuntu)
# - dnf     (Fedora / RHEL)
# - yum     (Legacy RHEL / CentOS)
# - pacman 	(Arch Linux)
# - zypper 	(openSUSE)
# - apk     (Alpine Linux)
#
# Notes:
# - Requires sudo privileges.
# - Should not be used in WSL or systems without GUI support.
#
# ------------------------------------------------------------
install_xterm() {
    if command -v xterm >/dev/null; then
        echo "xterm مثبت بالفعل."
    fi

    echo "xterm غير مثبت. محاولة التثبيت..."

    if command -v apt >/dev/null; then
        sudo apt update && sudo apt install -y xterm

    elif command -v dnf >/dev/null; then
        sudo dnf install -y xterm

    elif command -v yum >/dev/null; then
        sudo yum install -y xterm

    elif command -v pacman >/dev/null; then
        sudo pacman -Sy --noconfirm xterm

    elif command -v zypper >/dev/null; then
        sudo zypper install -y xterm

    elif command -v apk >/dev/null; then
        sudo apk add xterm

    else
        echo "لا يوجد مدير حزم مدعوم لتثبيت xterm تلقائياً."
        echo "الرجاء تثبيت xterm يدوياً."
    fi
}

# ------------------------------------------------------------
# is_wsl
#
# Detect whether the script is running inside
# Windows Subsystem for Linux (WSL).
#
# Detection method:
# - Looks for the string "Microsoft" in /proc/version
#
# Compatible with:
# - WSL 1
# - WSL 2
#
# Usage:
#   if is_wsl; then
#       echo "Running inside WSL"
#   fi
#
# Returns:
# - 0 if running under WSL
# - 1 otherwise
# ------------------------------------------------------------
is_wsl() {
    grep -qi microsoft /proc/version 2>/dev/null
}


# Ensure UTF-8 is set for Arabic (only if not already in .bashrc)
if ! grep -q "export LANG=en_US.UTF-8" ~/.bashrc; then
    echo 'export LANG=en_US.UTF-8' >> ~/.bashrc
fi
if ! grep -q "export LC_ALL=en_US.UTF-8" ~/.bashrc; then
    echo 'export LC_ALL=en_US.UTF-8' >> ~/.bashrc
fi
source ~/.bashrc

# Directory where original scripts live (bin/)
SRC_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"

# NAFTAH_HOME is parent of bin/
NAFTAH_HOME="$(dirname "$SRC_DIR")"

# Target for wrappers
if is_wsl; then
    BIN_DIR="$HOME/.local/bin"
elif [[ "$EUID" -eq 0 ]]; then
    BIN_DIR="${PREFIX:-/usr/local}/bin"
else
    BIN_DIR="${XDG_BIN_HOME:-$HOME/.local/bin}"
fi

mkdir -p "$BIN_DIR"

# Scripts to install
SCRIPTS=(naftah-shell.sh naftah-shell-wrapper.sh naftah-repl.sh naftah-man.sh naftah-init.sh naftah.sh)

echo "جاري تثبيت سكريبتات نفطه..."


# Install recommended Arabic fonts
install_arabic_fonts

# Ensure xterm is installed
install_xterm

# Fix CRLFs and make originals executable
echo "معالجة السكريبتات الأصلية في $SRC_DIR ..."
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
echo "جاري إنشاء الملفات المُلّفَّفة في $BIN_DIR ..."
for script in "${SCRIPTS[@]}"; do
    base_name="${script%.sh}"  # wrapper without .sh
    wrapper="$BIN_DIR/$base_name"

    echo "تثبيت الملف المُلّفَّف $base_name ..."
    sudo tee "$wrapper" > /dev/null <<EOF
#!/usr/bin/env bash
export NAFTAH_HOME="$NAFTAH_HOME"
exec "\$NAFTAH_HOME/bin/$script" "\$@"
EOF

    sudo chmod +x "$wrapper"
done

echo
echo "تم اكتمال التثبيت!"
echo "يمكنك الآن تشغيل: ${SCRIPTS[*]/.sh/} من أي مكان."
