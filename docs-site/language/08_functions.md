---
title: Naftah Functions
description: Guide to defining and calling functions in Naftah, including typed parameters, default values, void functions, and complex function structures.
layout: default
permalink: /language/functions
---

<div dir="rtl">

{% capture included_content %}
{% include language/08_functions.md %}
{% endcapture %}
{{ included_content | markdownify }}

{% if site.data.builtin-functions.size > 0 %}
<hr />
<h2>جميع الدوال المضمنة مع الوصف 📋</h2>

	{% for provider in site.data.builtin-functions %}
	<h3>{{ provider.provider }}</h3>
	  <p>{{ provider.description | newline_to_br }}</p>
	
	  <table>
		<thead>
		  <tr>
			<th>الوظيفة</th>
			<th>الوصف</th>
			<th>الاستخدام</th>
			<th>الأسماء البديلة</th>
			<th>أنواع المعاملات</th>
			<th>نوع الإرجاع</th>
		  </tr>
		</thead>
		<tbody>
		  {% for fn in provider.functions %}
		  <tr>
			<td><code class="language-plaintext highlighter-rouge">{{ fn.name }}</code></td>
			<td>{{ fn.description | newline_to_br }}</td>
			<td><code class="language-plaintext highlighter-rouge">{{ fn.usage | newline_to_br }}</code></td>
			<td>
				{% if fn.aliases.size > 0 %}
				  {% for alias in fn.aliases %}
					<code class="language-plaintext highlighter-rouge">{{ alias }}</code>{% if forloop.last == false %}, {% endif %}
				  {% endfor %}
				{% else %}
				-
				{% endif %}
			</td>
			<td>
				{% if fn.parameterTypes.size > 0 %}
				{{ fn.parameterTypes | join: ", " }}
				{% else %}
				-
				{% endif %}
			</td>
			<td>{{ fn.returnType }}</td>
		  </tr>
		  {% endfor %}
		</tbody>
	  </table>
	{% endfor %}

{% endif %}

<h2 id="1-ملاحظات">🧠 ملاحظات</h2>

<ul>
  <li>أسماء الدوال في نفطه عربية مع إمكانيات استخدامها مع الأرقام الصحيحة والعشرية.</li>
  <li>العمليات الحسابية والمنطقية تدعم الأعداد بمختلف أحجامها (8-بت، 16-بت، 32-بت، 64-بت، والأعداد العشرية).</li>
  <li>الدوال المضمنة تسهل كتابة كود واضح وقابل للقراءة بالعربية.</li>
  <li>الدوال تبدأ بكلمة <code class="language-plaintext highlighter-rouge">دالة</code> وتنتهي بـ <code class="language-plaintext highlighter-rouge">نهاية</code>.</li>
  <li>يمكن تحديد أنواع المعاملات باستخدام <code class="language-plaintext highlighter-rouge">: النوع</code>.</li>
  <li>يمكن تحديد نوع القيمة المرجعة بعد القوسين باستخدام <code class="language-plaintext highlighter-rouge">: نوع_القيمة</code>.</li>
  <li>المعاملات يمكن أن تكون اختيارية مع قيم افتراضية.</li>
  <li>الدوال التي لا ترجع قيمة لا تحتاج إلى تحديد نوع إرجاع أو يمكن اعتبارها <code class="language-plaintext highlighter-rouge">void</code>.</li>
</ul>

<hr />

</div>