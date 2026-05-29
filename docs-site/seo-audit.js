import fs from "fs";
import path from "path";
import * as cheerio from "cheerio";

const dir = "./_site";

const ignore_dirs = [
  "_site/javadoc",
  "_site/assets",
  "_site/feed",
  "_site/sitemap",
];

function normalize(p) {
  return p.replace(/\\/g, "/");
}

function isIgnored(file) {
  const f = normalize(file);
  return ignore_dirs.some(dir => f.includes(dir));
}

function walk(dir) {
  return fs.readdirSync(dir).flatMap(file => {
    const full = path.join(dir, file);
    return fs.statSync(full).isDirectory()
      ? walk(full)
      : full;
  });
}

const files = walk(dir)
  .filter(f => f.endsWith(".html"))
  .filter(f => !isIgnored(f));

const report = [];

console.log(`📁 Files to scan (${files.length}):\n`);

for (const file of files) {
  console.log(`- Scanning ${file}`);
  const html = fs.readFileSync(file, "utf-8");
  const $ = cheerio.load(html);

  const title = $("title").text().trim();
  const desc = $('meta[name="description"]').attr("content")?.trim();
  const h1 = $("h1").length;

  let score = 100;
  const issues = [];

  // TITLE
  if (!title) {
    score -= 30;
    issues.push({
      type: "TITLE",
      severity: "critical",
      message: "Missing title",
    });
  } else if (title.length < 10 || title.length > 60) {
    score -= 10;
    issues.push({
      type: "TITLE",
      severity: "warning",
      message: `Invalid length (${title.length})`,
    });
  }

  // DESCRIPTION
  if (!desc) {
    score -= 30;
    issues.push({
      type: "DESCRIPTION",
      severity: "critical",
      message: "Missing meta description",
    });
  } else if (desc.length < 120 || desc.length > 160) {
    score -= 10;
    issues.push({
      type: "DESCRIPTION",
      severity: "warning",
      message: `Invalid length (${desc.length})`,
    });
  }

  // H1
  if (h1 === 0) {
    score -= 20;
    issues.push({
      type: "H1",
      severity: "critical",
      message: "Missing H1",
    });
  } else if (h1 > 1) {
    score -= 10;
    issues.push({
      type: "H1",
      severity: "warning",
      message: `Multiple H1 (${h1})`,
    });
  }

  score = Math.max(0, score);

  report.push({
    file,
    score,
    issues,
  });
}

console.log(`\n📊 Pages scanned: ${report.length}`);
console.log(`📊 Average score: ${
report.length
  ? (report.reduce((a, b) => a + b.score, 0) / report.length).toFixed(2)
  : 0
}`);

console.log("\n🔎 Scanning results:");

for (const page of report) {
console.log(`\n${page.file} → score: ${page.score}`);

for (const issue of page.issues) {
  console.log(`  ⚠️ [${issue.type}] ${issue.message}`);
}
}