# Naftah Documentation

The Naftah documentation website is built using Jekyll and Ruby. Node.js and npm are used for additional documentation tooling, including SEO auditing, asset optimization, and serving the generated static website locally.

## Requirements

- Ruby
- RubyGems
- Bundler
- Node.js
- npm

## Installation

Clone the repository:

bash

```bash
git clone https://github.com/Chakib-DAII/Naftah.git
cd Naftah/docs-site
```

Install Ruby dependencies:

bash

```bash
bundle install
```

Install Node.js dependencies:

bash

```bash
npm install
```

## Running locally

First, generate the documentation:

bash

```bash
bundle exec jekyll build
```

Then start the local static HTTP server:

bash

```bash
npm run serve
```

Alternatively, use `http-server` directly:

bash

```bash
npx http-server _site -p 4000
```

Open your browser:

[http://localhost:4000](<http://localhost:4000>)

The HTTP server serves the generated `_site/` directory. It does not rebuild documentation automatically when source files change.

To preview documentation updates:

1. Edit the documentation source files.
2. Rebuild the website using Jekyll.
3. Refresh your browser.

Stop the server with `Ctrl+C`.

## Building the documentation

Generate the static website:

bash

```bash
bundle exec jekyll build
```

The generated website is available in:

text

```text
_site/
```

## Testing and validation

Check the Jekyll configuration:

bash

```bash
bundle exec jekyll doctor
```

Validate the generated HTML:

bash

```bash
bundle exec htmlproofer _site --disable-external
```

## Cleaning generated files

Remove the generated website:

bash

```bash
bundle exec jekyll clean
```

## SEO auditing

Run the SEO audit:

bash

```bash
npm run seo
```

## Asset optimization

Minify CSS:

bash

```bash
npm run minify-css
```

Minify JavaScript:

bash

```bash
npm run minify-js
```

Obfuscate JavaScript:

bash

```bash
npm run obfuscate-js
```

Run all optimization tasks:

bash

```bash
npm run minify-and-obfuscate-files
```

## Development workflow

1. Edit documentation files in the source directories.
2. Build the website using `bundle exec jekyll build`.
3. Start the local HTTP server using `npm run serve`.
4. Open [http://localhost:4000](<http://localhost:4000>).
5. Refresh the browser after rebuilding the documentation.
6. Run `bundle exec jekyll doctor` to check the configuration.
7. Use HTMLProofer to validate generated pages.
8. Run the npm optimization scripts when needed.

## Useful commands

| Command | Purpose |
| --- | --- |
| `bundle install` | Install Ruby dependencies |
| `npm install` | Install Node dependencies |
| `bundle exec jekyll build` | Build documentation |
| `npm run serve` | Serve `_site/` on port 4000 |
| `npx http-server _site -p 4000` | Run HTTP server directly |
| `bundle exec jekyll doctor` | Check configuration |
| `bundle exec jekyll clean` | Clean generated files |
| `bundle exec htmlproofer _site --disable-external` | Validate HTML |
| `npm run seo` | Run SEO audit |
| `npm run minify-and-obfuscate-files` | Optimize assets |

## Resources

- [Naftah Repository](<https://github.com/Chakib-DAII/Naftah>)
- [Naftah Documentation](<https://naftah.daiitech.org/>)
- [Jekyll Documentation](<https://jekyllrb.com/docs/>)
- [http-server](<https://github.com/http-party/http-server>)