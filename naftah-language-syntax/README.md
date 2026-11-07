# Naftah Language Syntax

`naftah-language-syntax` is the versioned, dependency-safe language artifact shared by Naftah tools. It intentionally excludes the interpreter, REPL, classpath scanner, execution context, and user-code loading.

It provides:

- Generated Naftah and temporal ANTLR lexers/parsers under `org.daiitech.naftah.parser`.
- `ImplicitEndTokenSource` for runtime-compatible virtual `أنهي` tokens.
- The language-safe `ScriptUtils` API for canonical NFKC normalization, identifier splitting, diacritic removal, and the authoritative transliteration vocabulary.

Publish the current version to Maven Local for development:

```powershell
.\gradlew.bat :naftah-language-syntax:publishToMavenLocal
```

Consumers use `org.daiitech.naftah:naftah-language-syntax:<version>`. A released version should be published to the configured Maven registry before consumers update their dependency.
