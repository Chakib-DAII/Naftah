---
title: ساحة التجارب
description: تشغيل وتجربة لغة نفطه مباشرة داخل المتصفح.
layout: default
permalink: /playground
rtl: true
head_scripts: [
	{ src: "https://cjrtnc.leaningtech.com/4.3/loader.js", defer: true },
	{ src: "/assets/js/minimal-class-scanning-index.js", defer: true },
	{ src: "/assets/js/playground.js", type: "module", defer: true }
]
head_styles: [
	{ href: "/assets/css/playground.css" }
]
---

# ساحة تجارب نفطه (Naftah Playground)

<div class="playground">

	<!-- TOP BAR -->
	<div class="topbar">

	<div class="examples">
	  <label for="examples-select">أمثلة لغة نفطه</label>
	  <select id="examples-select" aria-label="أمثلة لغة نفطه"></select>
	</div>

	<button id="run-btn">تشغيل</button>

	</div>

	<div id="runtime-banner" class="runtime-banner"></div>

	<!-- PANES -->
	<div class="panes">

	<div class="editor-pane">
	  <div id="line-numbers" aria-hidden="true"></div>
	  <textarea id="input" wrap="off" placeholder="اكتب كود نفطه هنا..." aria-label="Editor"></textarea>
	</div>


	<div class="console-pane">
	  <pre id="console" aria-label="Console"></pre>
	</div>

	</div>

	<div id="spinner-overlay" class="spinner-overlay">
	  <div class="spinner"></div>
	 	<p class="spinner-text">
			جاري تحميل بيئة نفطه… شكرًا على انتظارك.
		</p>
	</div>

</div>