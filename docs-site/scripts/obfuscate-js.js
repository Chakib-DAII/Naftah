const JavaScriptObfuscator = require('javascript-obfuscator');
const glob = require('glob');
const fs = require('fs');
const { walk } = require('./utils');

const dir = process.env.SITE_DIR || "_site";

glob.sync(`${dir}/assets/js/**/*.js`).forEach(file => {
	const code = fs.readFileSync(file, 'utf8');

	const result = JavaScriptObfuscator.obfuscate(code, {
		compact: true,
		stringArray: true,
		stringArrayEncoding: ['base64'],
		wrapGlobalFunctions: true,
	});

	fs.writeFileSync(file, result.getObfuscatedCode());
    console.log(`Obfuscated JS: ${file}`);
});