---
title: Naftah Releases
layout: default
permalink: /releases
---

# Naftah Releases - إصدارات نفطه

{% for release in site.data.releases %}

---

## [{{ release.name | default: release.tag_name }}]({{ release.html_url }})

**Published:** {{ release.published_at | date: "%Y-%m-%d" }}

{% if release.body %}
<div dir="rtl">
{{ release.body | markdownify 
		| replace: "<h1>", "<h2>"
		| replace: "</h1>", "</h2>"
 		| replace: "<li><p>", "<li>"
     	| replace: "</p></li>", "</li>"}}
{% else %}
_لا توجد إصدارات متوفرة._
{% endif %}
</div>

{% if release.assets and release.assets.size > 0 %}
**Downloads - التنزيلات:**
{% for asset in release.assets %}
- [{{ asset.name }}]({{ asset.browser_download_url }}) ({{ asset.size | divided_by: 1024 }} KB)
  {% endfor %}
{% endif %}

{% endfor %}