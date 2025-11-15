// Scroll to the table top after each draw (paging/filtering)
function scrollToJvmFunctionsTable() {
	var $table = $('#jvm-functions-table');

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
    // JVM Functions
     var table = $('#jvm-functions-table').DataTable({
        ajax: {
            url: '/assets/data/jvm-functions.json',
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
            { data: 'methodName', render: val => `<code class="language-plaintext highlighter-rouge">${val}</code>` },
            { data: 'qualifiedCall', render: val => `<code class="language-plaintext highlighter-rouge">${val}</code>` },
            { data: 'methodParameterTypes', render: arr => arr && arr.length ? arr.join(', ') : '-' },
            { data: 'isStatic', render: val => val ? '✅' : '❌' },
            { data: 'isInvocable', render: val => val ? '✅' : '❌' }
        ],
        language: { url: "https://cdn.datatables.net/plug-ins/1.13.6/i18n/ar.json" }
    });

	// Track user interactions: pagination, length, search input
	$(document).on(
		"input",
		"#jvm-functions-table_filter > label > input[type=search]",
		scrollToJvmFunctionsTable);

    $(document).on(
        "change",
        "#jvm-functions-table_length > label > select",
    	scrollToJvmFunctionsTable);

	$(document).on(
		"click",
		"#jvm-functions-table_paginate > span > a.paginate_button:not(.current)",
		scrollToJvmFunctionsTable);

	$(document).on(
		"click",
		"#jvm-functions-table_previous",
		scrollToJvmFunctionsTable);

	$(document).on(
		"click",
		"#jvm-functions-table_next",
		scrollToJvmFunctionsTable);
});