# Contributing to Naftah

First off, thanks for your interest in contributing to **Naftah**!
Your help improves the language and grows the community.
This guide will help you set up your development environment, understand the build system, and get started with
contributing code, grammar, or tooling.

We welcome all contributions — code, documentation, testing, discussions, and ideas!

---

## 🧰 Development Prerequisites

Before you begin, make sure you have the following installed:

| Tool         | Minimum Version   | Notes                                  |
|--------------|-------------------|----------------------------------------|
| **Java JDK** | 17                | Required. Preview features enabled.    |
| **Gradle**   | >= 7.5 (optional) | You can use the wrapper (`./gradlew`). |
| **Git**      | Any               | For cloning the repo.                  |
| **GPG**      | Optional          | Required only for publishing/signing.  |

---

## 🚀 Getting Started

### 1. Fork the Repository

Click the "Fork" button in the top right and clone your fork:

```bash
git clone https://github.com/Chakib-DAII/Naftah.git
cd naftah
```

### 2. Build the Project

You can build the project and generate all necessary artifacts (including the parser, dependencies, and REPL) using:

```bash
./gradlew build
```

Or on Windows:

```cmd
gradlew.bat build
```

---

## 🏗️ Key Gradle Tasks

Here's a breakdown of the most useful Gradle tasks available:

### 🔨 Build and Compilation

| Task          | Description                                                 |
|---------------|-------------------------------------------------------------|
| `build`       | Compiles source code, runs tests, checks code style         |
| `compileJava` | Compiles Java source including generated ANTLR code         |
| `clean`       | Cleans build directory (also deletes generated ANTLR files) |

---

### 🧪 Testing & Coverage

| Task                             | Description                                   |
|----------------------------------|-----------------------------------------------|
| `test`                           | Runs JUnit tests with vector preview features |
| `jacocoTestReport`               | Generates HTML and XML test coverage report   |
| `jacocoTestCoverageVerification` | Enforces minimum coverage (70%)               |

Test results and reports are available under `build/reports/tests/`.

---

### 🎯 Linting & Formatting

| Task                                                      | Description                                                |
|-----------------------------------------------------------|------------------------------------------------------------|
| `checkstyleMain`, `checkstyleTest`, `checkstyleBenchmark` | Run code style checks using Checkstyle                     |
| `spotlessApply`                                           | Auto-formats Java and Gradle files using Eclipse formatter |
| `lintAndFormat`                                           | Runs both Checkstyle and Spotless formatting               |

To fix formatting issues automatically:

```bash
./gradlew format
```

---

### 📜 ANTLR Grammar

ANTLR is used to define Naftah’s syntax. The grammar files live in:

```
src/main/antlr/
```

#### Key Task:

| Task                    | Description                                    |
|-------------------------|------------------------------------------------|
| `generateGrammarSource` | Regenerates parser/lexer code from `.g4` files |

This task runs automatically before `compileJava`.

> If you edit `NaftahLexer.g4` or `NaftahParser.g4`, always regenerate the sources.

---

### 📁 Build Distribution ZIPs

| Task             | Description                                                     |
|------------------|-----------------------------------------------------------------|
| `dist`           | Creates a minimal distribution ZIP under `build/distributions/` |
| `distStandalone` | Creates a fat/standalone JAR with all dependencies              |
| `fatJar`         | Builds a single runnable `.jar` with embedded dependencies      |

To create a full distribution (includes compiled classes, REPL scripts, lib JARs):

```bash
./gradlew dist
```

Or for the fat standalone JAR:

```bash
./gradlew distStandalone
```

---

## 🔬 Lexer Literal Extraction

Naftah uses custom tasks to extract tokens and symbols from the lexer:

| Task                   | Description                                                                                                         |
|------------------------|---------------------------------------------------------------------------------------------------------------------|
| `extractLexerLiterals` | Extracts all string literals and tokens from `NaftahLexer.g4` (reused in error listener and error messages)         |
| `writeResolvedJars`    | Writes resolved JAR names to `build/libs/original-dependencies` (jars to be ignored by the classpath class scanner) |

These are automatically included in the `dist` tasks.

---

## 🧪 Microbenchmarking with JMH

To run performance benchmarks:

```bash
./gradlew runJmh
```

This executes JMH tests defined in:

```
benchmark/java/
```

Results are written as JSON to:

```
build/reports/benchmarks/jmh-results.json
```

---

## 📦 Publishing to Maven Central (Optional)

Publishing is supported using:

```bash
./gradlew publish
```

Make sure to configure the following:

* [Create an account on Sonatype](https://central.sonatype.org/register/central-portal/)
* Set environment variables:

	* `OSSRH_USERNAME`
	* `OSSRH_PASSWORD`
* For signing (optional):

```bash
export GPG_PRIVATE_KEY=$(gpg --armor --export-secret-keys YOUR_KEY_ID)
export GPG_PASSPHRASE=yourPassphrase
```

Enable signing in `build.gradle` by uncommenting the `signing { ... }` section.

---

## 💡 Tips for Development

* Regenerate parser after grammar changes: `./gradlew generateGrammarSource`
* Clean builds: `./gradlew clean build`
* Check JavaDoc output: `build/docs/javadoc/index.html`
* Watch for preview feature use — Java 17+ with `--enable-preview` is required.
* Use `--info` or `--stacktrace` for debug builds if something fails.

---

## 🧱 Project Structure

```
├── build.gradle      			# Main Gradle build config
├── naftah-builtin-core         # Core builtin extensions dependency
	├── src/main/java         	# Core builtin annotations
	├── src/test/        	  	# JUnit tests
	├── build.gradle      		# Gradle build config
	└── build/                	# Gradle output (compiled classes, zips, etc.)
├── naftah-lang         		# Core language implementation
	├── src/main/java         	# Core language classes
	├── src/main/resources      # Core language resources
	├── src/main/antlr        	# Naftah grammar files (ANTLR)
	├── src/main/bin          	# CLI / REPL scripts (.sh / .bat)
	├── src/test/        	  	# JUnit tests
	├── src/benchmark/        	# JMH performance tests
	├── build.gradle      		# Gradle build config
	└── build/                	# Gradle output (compiled classes, zips, etc.)
├── learn-by-example/     		# Sample .naftah scripts
├── docs-site/     				# documentation site
└── config/               		# Code style and formatter config
```

---

## 🧑‍💻 IDE Support

If you use **IntelliJ IDEA**:

* Import as **Gradle project**.
* Enable preview features:
  File > Project Settings > Compiler > Java Compiler > Add `--enable-preview` as additional args.
* Enable ANTLR plugin (for syntax highlighting and navigation).
* Add `generated-src/antlr/main` as a generated source root.

---

## 👏 How to Contribute

1. Fork the repo and clone it locally.
2. Create a new branch:
   `git checkout -b feature/my-new-feature`
3. Make your changes.
4. Run build: `./gradlew build`
5. Run tests: `./gradlew test`
6. Open a PR on GitHub.

Please include:

* Clear description of your change
* Before/after behavior if applicable
* Tests or examples for any syntax/runtime changes

---

## 📬 Questions?

Open an issue or start a discussion on GitHub:
👉 [Naftah on GitHub](https://github.com/Chakib-DAII/Naftah/issues)

---
