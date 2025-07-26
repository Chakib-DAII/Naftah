const fs = require('fs');
const version = process.env.NEXT_RELEASE_VERSION;

if (!version) {
  console.error('NEXT_RELEASE_VERSION env variable missing');
  process.exit(1);
}

fs.writeFileSync('VERSION', version);
console.log(`Updated VERSION file to ${version}`);
