const fs = require('fs');
const terser = require('terser');
const { walk } = require('./utils');

const dir = process.env.SITE_DIR || "_site";

(async () => {
  for (const file of walk(dir, '.js')) {
  	if (file.endsWith('.min.js')) continue;

    const code = fs.readFileSync(file, 'utf8');

    const result = await terser.minify(code, {
    	module: true,
      	compress: true,
      	mangle: true
    });

    fs.writeFileSync(file, result.code);
    console.log(`Minified JS: ${file}`);
  }
})();