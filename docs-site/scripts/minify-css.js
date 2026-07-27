const fs = require('fs');
const CleanCSS = require('clean-css');
const { walk } = require('./utils');

const dir = process.env.SITE_DIR || "_site";

for (const file of walk(dir, '.css')) {
  if (file.endsWith('.min.css')) continue;

  const css = fs.readFileSync(file, 'utf8');

  const output = new CleanCSS({
    level: 2
  }).minify(css);

  fs.writeFileSync(file, output.styles);
  console.log(`Minified CSS: ${file}`);
}