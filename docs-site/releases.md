---
title: Naftah Releases
description: Browse Naftah programming language releases, including version history, changelogs, and downloadable builds for the JVM.
layout: default
permalink: /releases
---

# Naftah Releases - إصدارات نفطه 

{% for release in site.data.releases %}

<h2 id="nla-release-{{ release.name | default: release.tag_name | slugify }}">
	<a href="{{ release.html_url }}" target="_blank">{{ release.name | default: release.tag_name }}</a>
</h2>

**Published:** {{ release.published_at | date: "%Y-%m-%d" }}

{% if release.body %}
{{ release.body | markdownify | shift_headings }}
{% else %}
_لا توجد إصدارات متوفرة._
{% endif %}

{% if release.assets and release.assets.size > 0 %}
**Downloads - التنزيلات:**
<ul>
  {% for asset in release.assets %}
  <li><a href="{{ asset.browser_download_url }}" target="_blank">{{ asset.name }}</a> ({{ asset.size | divided_by: 1024 }} KB)</li>
  {% endfor %}
</ul>
{% endif %}

---

{% endfor %}