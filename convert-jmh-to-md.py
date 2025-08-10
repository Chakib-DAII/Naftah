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


def fmt(val):
	try:
		return f"{float(val):.10f}"
	except (ValueError, TypeError):
		return str(val)


# Markdown header
lines = [
	"# 🧪 Benchmark Results",
	"",
	"| Benchmark | Params | Mode | Score | Error | Units | Percentiles |",
	"|-----------|--------|------|-------|-------|--------|--------------|"
]

# Loop through each benchmark result
for entry in data:
	benchmark = entry.get("benchmark", "N/A").split(".")[-1]
	mode = entry.get("mode", "N/A")

	params_dict = entry.get("params", {})
	params = ", ".join(f"{k}={v}" for k, v in params_dict.items()) if params_dict else "-"

	score = entry.get('primaryMetric', {}).get('score', 'N/A')
	error = entry.get('primaryMetric', {}).get('scoreError', 'N/A')
	unit = entry.get('primaryMetric', {}).get('scoreUnit', 'N/A')

	percentiles = entry.get('primaryMetric', {}).get('scorePercentiles', {})
	# Sort and format all percentiles
	formatted_percentiles = []
	for key in sorted(percentiles, key=lambda x: float(x)):
		try:
			formatted_value = f"{float(percentiles[key]):.6f}"
		except (ValueError, TypeError):
			formatted_value = str(percentiles[key])
		formatted_percentiles.append(f"P{key}={formatted_value}")

	percentiles_str = ", ".join(formatted_percentiles)

	lines.append(
		f"| `{benchmark}` | {params} | {mode} | {fmt(score)} | ±{fmt(error)} | {unit} | {percentiles_str} |"
	)
# Write to file
output_file.parent.mkdir(parents=True, exist_ok=True)
output_file.write_text("\n".join(lines), encoding="utf-8")

print(f"✅ Markdown written to {output_file}")
