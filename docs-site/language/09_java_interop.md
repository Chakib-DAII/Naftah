---
title: Java Interoperability
description: Guide to Naftah on the JVM with Java interoperability, including seamless use of Java classes and libraries.
layout: default
permalink: /language/java-interop
head_scripts: [
	{ src: "https://code.jquery.com/jquery-3.6.0.min.js", defer: true },
	{ src: "https://cdn.datatables.net/1.13.6/js/jquery.dataTables.min.js", defer: true },
	{ src: "https://cdn.jsdelivr.net/npm/pako@2.1.0/dist/pako.min.js", defer: true },
	{ src: "/assets/js/jvm-tables.js", type: "module", defer: true }
]
head_styles: [
	{ href: "https://cdn.datatables.net/1.13.6/css/jquery.dataTables.min.css" }
]
---

<div dir="rtl">

{% capture included_content %}
{% include language/09_java_interop.md %}
{% endcapture %}
{{ included_content | markdownify }}

{% if site.data.jvm-class-initializers.size > 0 %}
<h2 id="nla-jvm-class-initializers">📌 جميع مُهيئات الكائنات</h2>
<table id="jvm-class-initializers-table" class="display" style="width:100%">
<thead>
<tr>
<th>الكائن</th>
<th>الاسم المؤهل</th>
<th>أنواع المعاملات</th>
<th>قابل للاستدعاء؟</th>
</tr>
</thead>
</table>
<hr />
{% endif %}

{% if site.data.jvm-functions.size > 0 %}
<h2 id="nla-jvm-functions">📌 جميع استدعاءات الدوال</h2>
<table id="jvm-functions-table" class="display" style="width:100%">
<thead>
<tr>
<th>الكائن</th>
<th>الدالة</th>
<th>الاسم المؤهل</th>
<th>أنواع المعاملات</th>
<th>ثابتة؟</th>
<th>قابلة للاستدعاء؟</th>
</tr>
</thead>
</table>
<hr />
{% endif %}


<h2 id="nla-notes">🧠 ملاحظات</h2>

<ul>
  <li>يمكن استدعاء <strong>الدوال الثابتة والمثيلة</strong> من مكتبات جافا مباشرة داخل نفطه.</li>
  <li>يدعم إنشاء الكائنات مثل <strong>Object، UUID، Optional، ArrayList</strong> بطرق متعددة (فارغ، من قيمة، من سلسلة).</li>
  <li>التعامل مع الأنواع الرقمية مثل <strong>Integer، Long، Double، Boolean</strong> مع إمكانية التحويل من وإلى السلاسل النصية.</li>
  <li>يمكن استخدام <strong>الدوال على الكائنات</strong> مثل <code class="language-plaintext highlighter-rouge">length</code>، <code class="language-plaintext highlighter-rouge">substring</code>، <code class="language-plaintext highlighter-rouge">add</code>، <code class="language-plaintext highlighter-rouge">get</code>، <code class="language-plaintext highlighter-rouge">size</code>، و<code class="language-plaintext highlighter-rouge">toString</code>.</li>
  <li>يدعم <strong>Optional</strong> للتحقق من وجود القيم أو استخدام قيمة افتراضية (<code class="language-plaintext highlighter-rouge">orElse</code>).</li>
  <li>أسماء الدوال في نفطه عربية مع إمكانية التعامل مع <strong>الأرقام الصحيحة والعشرية</strong> و<strong>char/Character</strong>.</li>
  <li>التوافقية مع <strong>primitive ↔ wrapper</strong> تجعل استدعاءات الدوال مرنة وموثوقة.</li>
  <li>
    <p>الصياغة تعتمد على:</p>

    <ul>
      <li><code class="language-plaintext highlighter-rouge">::</code> لاستدعاء <strong>الدوال الثابتة (Static Methods)</strong> و<strong>البُنى (Constructors)</strong>.</li>
      <li><code class="language-plaintext highlighter-rouge">:::</code> لاستدعاء <strong>الدوال على الكائنات (Instance Methods)</strong>.</li>
    </ul>

  </li>
  <li>
    <p>يدعم <strong>الدوال المتسلسلة (Chained Calls)</strong> بحيث:</p>

    <ul>
      <li>عند استخدام <code class="language-plaintext highlighter-rouge">:::</code> يتم إعادة استخدام الاسم المؤهل من الاستدعاء السابق.</li>
      <li>عند استخدام <code class="language-plaintext highlighter-rouge">::</code> يتم استخدام الاسم المؤهل الخاص بالدالة فقط.</li>
    </ul>

  </li>
  <li>يسهل كتابة كود <strong>واضح وقابل للقراءة بالعربية</strong> مع دعم المعاملات الاختيارية والقيم الافتراضية.</li>
</ul>

<hr />

</div>