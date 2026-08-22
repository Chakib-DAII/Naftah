const fs = require('fs');
const path = require('path');

function walk(dir, extension) {
  let files = [];

  for (const entry of fs.readdirSync(dir, { withFileTypes: true })) {
    const fullPath = path.join(dir, entry.name);

    if (entry.isDirectory()) {
      files = files.concat(walk(fullPath, extension));
    } else if (!extension || entry.name.endsWith(extension)) {
      files.push(fullPath);
    }
  }

  return files;
}

module.exports = {
  walk
};