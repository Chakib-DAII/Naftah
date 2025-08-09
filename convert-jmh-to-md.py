import json
import sys
from pathlib import Path

# Input/output paths
input_file = Path("build/reports/benchmarks/jmh-results.json")
output_file = Path("docs-site/benchmarks.md")

if not input_file.exists():
	print(f"Error: {input_file} does not exist.")
	sys.exit(1)

with input_file.open("r", encoding="utf-8") as f:
	data = json.load(f)

# Markdown header
lines = [
	"# 🧪 Benchmark Results",
	"",
	"| Benchmark | Mode | Score | Error | Units |",
	"|-----------|------|-------|-------|--------|"
]

# Loop through each benchmark result
for entry in data:
	benchmark = entry.get("benchmark", "N/A").split(".")[-1]
	mode = entry.get("mode", "N/A")
	score = f"{entry.get('primaryMetric', {}).get('score', 'N/A'):.10f}"
	error = f"{entry.get('primaryMetric', {}).get('scoreError', 'N/A'):.10f}"
	unit = entry.get("primaryMetric", {}).get("scoreUnit", "N/A")

	lines.append(f"| `{benchmark}` | {mode} | {score} | ±{error} | {unit} |")

# Write to file
output_file.parent.mkdir(parents=True, exist_ok=True)
output_file.write_text("\n".join(lines), encoding="utf-8")

print(f"✅ Markdown written to {output_file}")
