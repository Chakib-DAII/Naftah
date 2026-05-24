let META = null;
let SEARCH_INDEX = [];
const CHUNK_CACHE = {};

// Scroll helper, Scroll to the table top after each draw (paging/filtering)
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

// Chunk helpers
function getChunkFile(base, page) {
	return `/assets/data/${base}-page-${String(page).padStart(4, '0')}.json.gz`;
}

function loadChunk(base, page) {

	if (CHUNK_CACHE[page]) {
		return Promise.resolve(CHUNK_CACHE[page]);
	}

	return fetch(getChunkFile(base, page))
		.then(resp => {
			if (!resp.ok) throw new Error(`Missing chunk page ${page}`);
			return resp.arrayBuffer();
		})
		.then(buffer => {
			const json = JSON.parse(
				pako.inflate(new Uint8Array(buffer), { to: 'string' })
			);

			CHUNK_CACHE[page] = json;
			return json;
		});
}

// Search engine (FAST INDEX)
function searchData(query) {

	if (!query) return null;

	query = query.toLowerCase();

	return SEARCH_INDEX.filter(item => {
		return (
			item.className?.toLowerCase().includes(query) ||
			item.methodName?.toLowerCase().includes(query) ||
			item.qualifiedCall?.toLowerCase().includes(query)
		);
	});
}

// Main
$(document).ready(function() {

	// Load META
	fetch('/assets/data/jvm-functions-meta.json')
		.then(r => r.json())
		.then(meta => META = meta)
		.catch(() => {
			META = {
				file: "jvm-functions.json",
				total_items: 0,
				chunk_size: 100,
				pages: 0
			};
		});

	// Load SEARCH INDEX
	fetch('/assets/data/jvm-functions-search-index.json')
		.then(r => r.json())
		.then(data => SEARCH_INDEX = data)
		.catch(err => console.warn("Search index missing", err));

	// DataTable
	var table = $('#jvm-functions-table').DataTable({

		ajax: function(data, callback) {

			if (!META) {
				callback({ data: [] });
				return;
			}

			const query = $('#jvm-functions-table_filter input').val();
			const baseName = META.file.replace('.json', '');
			const chunkSize = META.chunk_size;

			//  SEARCH MODE (INDEX)
			if (query && SEARCH_INDEX.length > 0) {

				const results = searchData(query);

				if (!results || results.length === 0) {
					callback({
						data: [],
						recordsTotal: META.total_items,
						recordsFiltered: 0
					});
					return;
				}

				const pages = [...new Set(results.map(r => r.page))];

				Promise.all(pages.map(p => loadChunk(baseName, p)))
					.then(chunks => {

						const all = chunks.flat();
						const idSet = new Set(results.map(r => r.id));

						const filtered = all.filter(row => idSet.has(row.id));

						callback({
							data: filtered,
							recordsTotal: META.total_items,
							recordsFiltered: results.length
						});
					})
					.catch(err => {
						console.error("Search load failed:", err);
						callback({ data: [] });
					});

				return;
			}

			// NORMAL PAGINATION MODE
			const start = data.start;
			const length = data.length;

			const firstChunk = Math.floor(start / chunkSize) + 1;
			const lastChunk = Math.floor((start + length - 1) / chunkSize) + 1;

			const promises = [];

			for (let p = firstChunk; p <= lastChunk; p++) {
				promises.push(loadChunk(baseName, p));
			}

			Promise.all(promises)
				.then(chunks => {

					const merged = chunks.flat();

					const offset = start % chunkSize;
					const result = merged.slice(offset, offset + length);

					callback({
						data: result,
						recordsTotal: META.total_items,
						recordsFiltered: META.total_items
					});
				})
				.catch(err => {
					console.error("Chunk load failed:", err);
					callback({ data: [] });
				});
		},
		deferRender: true,
		processing: true,
		serverSide: true,
		pageLength: 10,
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