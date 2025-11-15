$(document).ready(function() {
	// JVM Class Initializers
	$('#jvm-class-initializers-table').DataTable({
		ajax: {
			url: '/assets/data/jvm-class-initializers.json',
			dataSrc: ''
		},
		deferRender: true,
		processing: true,
		pageLength: 50,
		scrollX: true,
		responsive: true,
		autoWidth: true,
		fixedHeader: true,
		columns: [
			{ data: 'className', render: val => `<code class="language-plaintext highlighter-rouge">${val}</code>` },
			{ data: 'qualifiedName', render: val => `<code class="language-plaintext highlighter-rouge">${val}</code>` },
			{ data: 'constructorParameterTypes', render: arr => arr && arr.length ? arr.join(', ') : '-' },
			{ data: 'isInvocable', render: val => val ? '✅' : '❌' }
		],
		language: { url: "https://cdn.datatables.net/plug-ins/1.13.6/i18n/ar.json" }
	});
});