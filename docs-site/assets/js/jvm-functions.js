let META = null;
let SEARCH_INDEX = {};

const CHUNK_CACHE = {};
let currentRequestId = 0;

// Scroll helper, Scroll to the table top after each draw (paging/filtering)
function scrollToJvmFunctionsTable() {
	const $table = $('#jvm-functions-table');

	// Get the table's top relative to the viewport
	const tableTop = $table[0].getBoundingClientRect().top + window.pageYOffset;

	// Consider the fixed site header height
	const siteHeaderHeight = $('.site-header').outerHeight() || 0;

	// DataTables FixedHeader is active, subtract its height as well
	let fixedHeaderHeight = 0;
	if ($.fn.DataTable.FixedHeader) {
		fixedHeaderHeight = $('.fixedHeader-floating').outerHeight() || 0;
	}

	// Scroll to the correct position
	$('html, body').animate({
		scrollTop: tableTop - siteHeaderHeight - fixedHeaderHeight - 100 // extra padding
	}, 250);
}

// Chunk loader
function getChunkFile(base, page) {
	return `/assets/data/${base}-page-${String(page).padStart(4, '0')}.json.gz`;
}

function loadChunk(base, page) {
	if (CHUNK_CACHE[page]) {
		return Promise.resolve(CHUNK_CACHE[page]);
	}

	return fetch(getChunkFile(base, page))
		.then(resp => {
			if (!resp.ok) throw new Error(`Missing chunk ${page}`);
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

// FAST SEARCH ENGINE
function loadSearchIndex() {
	return fetch('/assets/data/jvm-functions-search-index.json.gz')
		.then(async r => {
			if (!r.ok) throw new Error("Failed to load index");
			return r.arrayBuffer();
		})
		.then(buffer => {
			try {
				const decompressed = pako.inflate(new Uint8Array(buffer), {
					to: 'string'
				});

				return JSON.parse(decompressed);
			} catch (e) {
				console.error("Decompression/parse failed");
				throw e;
			}
		});
}

function searchData(query) {
	if (!query) return [];

	const tokens = query.toLowerCase().split(/\s+/);

	let resultSet = null;

	for (const token of tokens) {
		const ids = SEARCH_INDEX[token];

		if (!ids) return [];

		if (!resultSet) {
			resultSet = new Set(ids);
		} else {
			resultSet = new Set(ids.filter(id => resultSet.has(id)));
		}
	}

	return Array.from(resultSet || []);
}

function limitConcurrency(items, limit, fn) {
	let index = 0;
	let active = 0;
	let results = [];

	return new Promise(resolve => {
		function next() {
			if (index === items.length && active === 0) {
				resolve(results);
				return;
			}

			while (active < limit && index < items.length) {
				const i = index++;
				active++;

				Promise.resolve(fn(items[i]))
					.then(res => results[i] = res)
					.finally(() => {
						active--;
						next();
					});
			}
		}

		next();
	});
}

// debounce helper (CRITICAL for performance)
function debounce(fn, delay) {
	let t;
	return function (...args) {
		clearTimeout(t);
		t = setTimeout(() => fn.apply(this, args), delay);
	};
}

// MAIN
$(document).ready(function () {

	// META
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

	// INVERTED INDEX
	loadSearchIndex()
		.then(data => SEARCH_INDEX = data)
		.catch(err => console.warn("Search index missing", err));

	// DataTable
	const table = $('#jvm-functions-table').DataTable({

		ajax: function (data, callback) {

			if (!META) {
				callback({ data: [] });
				return;
			}

			const requestId = ++currentRequestId;

			const query = $('#jvm-functions-table_filter input').val();
			const baseName = META.file.replace('.json', '');
			const chunkSize = META.chunk_size;

			// SEARCH MODE
			if (query && Object.keys(SEARCH_INDEX).length > 0) {

				const ids = searchData(query);

				if (requestId !== currentRequestId) return;

				if (!ids.length) {
					callback({
						data: [],
						recordsTotal: META.total_items,
						recordsFiltered: 0
					});
					return;
				}

				const uniqueIds = Array.from(new Set(ids));

                // map id → chunk (from META logic)
                const pageMap = new Map();

                for (const id of uniqueIds) {
                	const page = Math.floor((id - 1) / chunkSize) + 1;

                	if (!pageMap.has(page)) {
                		pageMap.set(page, []);
                	}

                	pageMap.get(page).push(id);
                }

                // convert to sorted, capped page list
                const pages = Array.from(pageMap.keys())
                	.sort((a, b) => a - b)
                	.slice(0, 20); // HARD SAFETY LIMIT

				limitConcurrency(
					[...pages],
					4, // max parallel requests
					p => loadChunk(baseName, p)
				).then(chunks => {

						if (requestId !== currentRequestId) return;

						const all = chunks.flat();
						const idSet = new Set(ids);

						const filtered = all.filter(row => idSet.has(row.id));

						callback({
							data: filtered,
							recordsTotal: META.total_items,
							recordsFiltered: ids.length
						});
					})
					.catch(err => {
						console.error("Search failed:", err);
						callback({ data: [] });
					});

				return;
			}

			// PAGINATION MODE
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

					if (requestId !== currentRequestId) return;

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
		serverSide: true,
		processing: true,
		deferRender: true,
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

	// debounce search binding
	const debouncedSearch = debounce(scrollToJvmFunctionsTable, 300);

	// Track user interactions: pagination, length, search input
	$(document).on(
		"input",
		"#jvm-functions-table_filter > label > input[type=search]",
		function () {
			debouncedSearch();
			table.ajax.reload();
		});

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