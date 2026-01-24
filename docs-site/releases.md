---
title: Naftah Releases
layout: default
permalink: /releases
---

# Naftah Releases - إصدارات نفطه 

{% for release in site.data.releases %}

## [{{ release.name | default: release.tag_name }}]({{ release.html_url }})

**Published:** {{ release.published_at | date: "%Y-%m-%d" }}

{% if release.body %}
{{ release.body | markdownify }}
{% else %}
_لا توجد إصدارات متوفرة._
{% endif %}

{% if release.assets and release.assets.size > 0 %}
**Downloads - التنزيلات:**
<ul>
  {% for asset in release.assets %}
  <li><a href="{{ asset.browser_download_url }}">{{ asset.name }}</a> ({{ asset.size | divided_by: 1024 }} KB)</li>
  {% endfor %}
</ul>
{% endif %}

---

{% endfor %}