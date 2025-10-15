import json
import matplotlib.pyplot as plt
import re
import sys
from collections import defaultdict
from pathlib import Path

# Input/output paths
input_file = Path("build/reports/benchmarks/jmh-results.json")
output_file = Path("docs-site/benchmarks.md")

# Create graph output directory
graphs_dir = Path("docs-site/static/benchmark-graphs")
graphs_dir.mkdir(parents=True, exist_ok=True)

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

# Collect data for grouped bar charts
benchmarks_by_name = defaultdict(list)

# Loop through each benchmark result
for entry in data:
	benchmark_full_name = entry.get("benchmark", "N/A")
	parts = benchmark_full_name.split(".")
	benchmark_full_class_name = ".".join(parts[:-1]) if len(parts) > 1 else "UnknownClass"
	benchmark_class_name = parts[-2] if len(parts) > 1 else "UnknownClass"
	benchmark_method_name = parts[-1]

	mode = entry.get("mode", "N/A")

	params_dict = entry.get("params", {})
	params = r', '.join(f"{k}={v}" for k, v in params_dict.items()).replace('$', r'\$') if params_dict else "-"

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
		f"| `{benchmark_class_name}.{benchmark_method_name}` | {params} | {mode} | {fmt(score)} | ±{fmt(error)} | {unit} | {percentiles_str} |"
	)

	if score is not None:
		benchmarks_by_name[benchmark_full_class_name].append({
			"method": benchmark_method_name,
			"params": params,
			"score": score,
			"error": error,
			"unit": unit
		})

# Plot per benchmark
for benchmark_full_class_name, results in benchmarks_by_name.items():
	method_list = [r["method"] for r in results]
	method = set(method_list)
	if len(method) == 1 and 'benchmarkInterpolation' in method:
		placeholder_pattern = re.compile(r'\$\{[^}]+\}')
		params = [f"param {i} -> {len(placeholder_pattern.findall(r["params"]))} variable(s)" for i, r in
				  enumerate(results)]
	else:
		params = method_list if all(p == '-' for p in [r["params"] for r in results]) else [r["params"] for r in
																							results]
	scores = [r["score"] for r in results]
	errors = [r["error"] for r in results]
	unit = results[0]["unit"]

	plt.figure(figsize=(15, 7.5))
	plt.bar(params, scores, yerr=errors, capsize=5, color='lightgreen', edgecolor='black')
	plt.title(f"Benchmark: {method}")
	plt.ylabel(unit)
	plt.xlabel("Params")
	# Build x-tick labels combining param + score
	xtick_labels = [f"{p}\n({score:.6f} {unit})" for p, score in zip(params, scores)]
	plt.xticks(ticks=range(len(params)), labels=xtick_labels, rotation=45, ha='right')
	plt.tight_layout()

	output_path = graphs_dir / f"{benchmark_full_class_name}_comparison.png"
	plt.savefig(output_path)
	plt.close()

	print(f"📈 Graph saved: {output_path}")

# Append images to the markdown
lines.append("\n---\n")
lines.append("## 📈 Benchmark Graphs\n")

# Find all PNGs
graph_paths = sorted(graphs_dir.glob("*.png"))

for img_path in graph_paths:
	title = img_path.stem.split(".")[-1].replace("_", " ")  # Restore class.method from filename
	lines.append(f"### {title}")
	lines.append(f"![{title}](static/benchmark-graphs/{img_path.name})")
	lines.append("")  # newline

lines.append("""

---

## 📁 ملفات مرتبطة

* [Home - الرئيسية](./index.md)

---
""")

# Write to file
output_file.parent.mkdir(parents=True, exist_ok=True)
output_file.write_text("\n".join(lines), encoding="utf-8")

print(f"✅ Markdown written to {output_file}")
