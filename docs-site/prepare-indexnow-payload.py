
import json
import os
from pathlib import Path
import xml.etree.ElementTree as ET
from datetime import datetime, timezone

INDEXNOW_KEY = os.environ.get("INDEXNOW_KEY")
SITE_MAP = Path(os.environ.get("SITE_MAP"))
OUTPUT_PATH = Path(os.environ.get("INDEXNOW_OUTPUT", "indexnow.json"))

sitemap = ET.parse(SITE_MAP)
root = sitemap.getroot()

namespace = {"sm": "http://www.sitemaps.org/schemas/sitemap/0.9"}

urls = [
	loc.text
	for loc in root.findall(".//sm:loc", namespace)
	if loc.text
]

payload = {
	"generatedAt": datetime.now(timezone.utc).isoformat(),
	"host": "naftah.daiitech.org",
	"key": INDEXNOW_KEY,
	"keyLocation": f"https://naftah.daiitech.org/{INDEXNOW_KEY}.txt",
	"urlList": urls
}

with OUTPUT_PATH.open("w", encoding="utf-8") as f:
	json.dump(payload, f, indent=2)

print(f"Generated {OUTPUT_PATH} with {len(urls)} URLs")