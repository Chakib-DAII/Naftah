// Scroll to the table top after each draw (paging/filtering)
function scrollToJvmClassInitializersTable() {
	var $table = $('#jvm-class-initializers-table');

	// Get the table's top relative to the viewport
	var tableTop = $table[0].getBoundingClientRect().top + window.pageYOffset;

	// Consider the fixed site header height
	var siteHeaderHeight = $('.site-header').outerHeight() || 0;

	// DataTables FixedHeader is active, subtract its height as well
	var fixedHeaderHeight = 0;
	if ($.fn.DataTable.FixedHeader) {
		fixedHeaderHeight = $('.fixedHeader-floating').outerHeight() || 0;
	}

	// Scroll to the correct position
	$('html, body').animate({
		scrollTop: tableTop - siteHeaderHeight - fixedHeaderHeight - 100 // extra padding
	}, 300);
}

$(document).ready(function() {
	// JVM Class Initializers
	var table = $('#jvm-class-initializers-table').DataTable({
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

	// Track user interactions: pagination, length, search input
	$(document).on(
		"input",
		"#jvm-class-initializers-table_filter > label > input[type=search]",
		scrollToJvmClassInitializersTable);

    $(document).on(
        "change",
        "#jvm-class-initializers-table_length > label > select",
    	scrollToJvmClassInitializersTable);

	$(document).on(
		"click",
		"#jvm-class-initializers-table_paginate > span > a.paginate_button:not(.current)",
		scrollToJvmClassInitializersTable);

	$(document).on(
		"click",
		"#jvm-class-initializers-table_previous",
		scrollToJvmClassInitializersTable);

	$(document).on(
		"click",
		"#jvm-class-initializers-table_next",
		scrollToJvmClassInitializersTable);
});
