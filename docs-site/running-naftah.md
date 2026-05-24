---
title: Running Naftah
layout: default
permalink: /running-naftah
---

# 🚀 Running Naftah: Scripts, REPL, Initialization and Manual

Naftah comes with cross-platform CLI tools that allow you to run scripts, start an interactive shell, or initialize the
system to work with existing Java/Kotlin/Groovy libraries.

---

## 🔧 Prerequisites {#nla-prerequisites}

* **Java 17 or higher** (ensure `JAVA_HOME` is set correctly)
* Naftah distribution folder with:
	* `lib/` – containing core and optional `.jar` files
	* Shell/BAT scripts: `naftah.sh`, `naftah-repl.sh`, `naftah-init.sh`, etc.

---

## 🗂️ Available Scripts {#nla-available-scripts}

| Script Name               | Description                                    |
|---------------------------|------------------------------------------------|
| `naftah.sh` / `.bat`      | Default entry point (runs a script by default) |
| `naftah-repl.sh` / `.bat` | Starts REPL (interactive shell)                |
| `naftah-init.sh` / `.bat` | Initializes Java classpath libraries           |
| `naftah-man.sh` / `.bat`  | Naftah language manual                         |

> 🖥️ Use `.bat` files on **Windows** and `.sh` scripts on **Linux/macOS**.

---

## ✅ 1. Run a Naftah Script {#nla-run}

To execute a `.naftah` file:

### Linux / macOS {#nla-run-linux-macos}

```bash
./naftah-shell.sh run hello.naftah
```

Or directly (default subcommand is `run`):

```bash
./naftah.sh hello.naftah
```

### Windows {#nla-run-windows}

```cmd
naftah-shell.bat run hello.naftah
```

Or simply:

```cmd
naftah.bat hello.naftah
```

---

## 💬 2. Start the Interactive Shell (REPL) {#nla-repl}

The REPL allows you to experiment with Naftah code line-by-line in a terminal.

### Linux / macOS {#nla-repl-linux-macos}

```bash
./naftah-repl.sh
```

Or:

```bash
./naftah-shell.sh shell
```

### Windows {#nla-repl-windows}

```cmd
naftah-repl.bat
```

Or:

```cmd
naftah.bat shell
```

### Features: {#nla-features}

* Multi-line code support
* Auto history and arrow key navigation
* Live feedback with Arabic-style syntax

---

## ⚙️ 3. Initialize Java Libraries for Use in Naftah {#nla-init}

The `init` command processes all `.jar` files inside the `lib/` folder and adds them to Naftah's reflection system.
This enables:

* Using Java/Kotlin/Groovy classes inside `.naftah` scripts.
* Interacting with them via Arabic transliterated syntax.
* Seamless interop without additional build steps.

### Linux / macOS {#nla-init-linux-macos}

```bash
./naftah-init.sh
```

Or:

```bash
./naftah.sh init
```

### Windows {#nla-init-windows}

```cmd
naftah-init.bat
```

Or:

```cmd
naftah.bat init
```

> 🔍 By default, `init` **scans all `.jar` files under `lib/`** and builds type metadata for use in Naftah scripts.

---

## 📚 4. Use the Naftah Manual (Dynamic CLI Help) {#nla-manual}

The `man` command launches **Naftah's interactive manual system**, which supports exploring all built-in commands,
functions, and Java interop metadata — in both **Arabic and English**.

It behaves like a smart REPL-based help system, not just a static manual:

### 🔍 Features:

* ⌨️ Type `usage` / `مساعدة` to view all supported commands
* 📁 Use `list` / `المواضيع` to browse available help topics
* 📚 Type any topic name to open its Markdown-based guide
* 🏷️ Type any Java class name (fully-qualified) to view its **Arabic-qualified alias**
* 📦 Explore Java interop using:
	* `classes` / `الأصناف` — All known classes
	* `accessible-classes` / `الأصناف-المتاحة` — Public classes only
	* `instantiable-classes` / `الأصناف-القابلة-للتهيئة` — Instantiable classes
* ⚙️ View runtime and built-in functions using:
	* `builtin-functions` / `الدوال-المدمجة`
	* `jvm-functions` / `دوال-جافا`
* ❌ Type `exit` / `خروج` to quit the manual session at any time

---

### Linux / macOS {#nla-manual-linux-macos}

```bash
./naftah-man.sh
```

Or:

```bash
./naftah.sh man
```

### Windows {#nla-manual-windows}

```cmd
naftah-man.bat
```

Or:

```cmd
naftah.bat man
```

> 💡 You can use this command as a lightweight alternative to browsing the docs online — it’s fully terminal-friendly and
> supports Arabic output with pagination.

---

## 🧪 Example Workflow {#nla-example-workflow}

1. Add your Java/Kotlin/Groovy `.jar` files under `lib/`
2. Run `naftah-init` to scan and index classes
3. Start REPL or run a `.naftah` file that uses those classes

```bash
# Add your JAR
cp mylib.jar lib/

# Initialize classpath
./naftah-init.sh

# Run a script
./naftah.sh myscript.naftah

# Start REPL
./naftah-repl.sh
```

---

## 📄 CLI Usage Summary {#nla-cli-usage-summary}

### Available Subcommands: {#nla-cli-usage-subcommands-summary}

| Command | Purpose                                       |
|---------|-----------------------------------------------|
| `run`   | Runs a `.naftah` script or inline code        |
| `shell` | Starts interactive REPL                       |
| `init`  | Scans Java/Kotlin/Groovy `.jar`s under `lib/` |
| `man`   | launches Naftah's interactive manual system   |

### Global Options: {#nla-cli-usage-global-options-summary}

| Option                      | Description                                                                 |
|-----------------------------|-----------------------------------------------------------------------------|
| `-e "<script>"`             | Run inline Naftah code (instead of providing a filename)                    |
| `-cp, --classpath <path>`   | Add custom entries to the Java classpath                                    |
| `-D <key=value>`            | Define system properties, available via `System.getProperty(...)`           |
| `--scan-classpath` (`-scp`) | Enable reuse of Java types from classpath in your Naftah script             |
| `--force-scan-classpath`    | Force classpath re-scan even if already initialized                         |
| `--enable-cache=M,I`        | Enable performance caches: `M` for multiline strings, `I` for interpolation |
| `--arabic_formatting`       | Use Arabic-style number formatting (e.g., decimal separator)                |
| `--arabic_indic`            | Use Arabic-Indic digit shapes (٠١٢٣٤٥٦٧٨٩)                                  |
| `-d, --debug`               | Enable debug mode (prints stack traces)                                     |
| `-c, --encoding <charset>`  | Specify character encoding (e.g., `UTF-8`, `windows-1256`)                  |
| `--vector`                  | Enable experimental Vector API optimizations (JDK incubator)                |
| `-h, --help`                | Show help message and exit                                                  |
| `-v, --version`             | Show version and exit                                                       |

---

### 🧠 Examples {#nla-examples}

```bash
# Run a Naftah script file
naftah run myscript.naftah

# Run Naftah inline code
naftah run -e "اطبع(\"السلام عليكم\")"

# Start interactive shell
naftah shell

# Initialize classpath types for reuse
naftah init -cp lib/my.jar --scan-classpath

# Use Arabic-Indic numerals
naftah run script.naftah --arabic_indic

# Enable both caches (multiline + interpolation)
naftah run script.naftah --enable-cache=M,I
```

---


