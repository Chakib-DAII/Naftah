/**
 * JVM Functions DataTable Module
 *
 * This module powers a server-side DataTables view for JVM
 * function metadata.
 *
 * Features:
 * - Chunked JSON loading (.json.gz)
 * - Full-text search index (token-based)
 * - Client-side caching layer
 * - Concurrency-limited fetch system
 * - Debounced UI interactions
 * - Smooth scroll UX for table navigation
 *
 * Data flow:
 * Meta + Index → Search/Pagination → Chunk Loader → DataTable
 *
 */

/** Metadata describing dataset structure (chunk size, totals, file name). */
let META = null;

/** Token → list of record IDs search index. */
let SEARCH_INDEX = {};

/** Cache of loaded JSON chunks (page → data). */
const CHUNK_CACHE = {};

/** Used to prevent race conditions between async requests. */
let currentRequestId = 0;

/** Indicates whether meta + index are fully loaded. */
let READY = false;

/**
 * Scrolls the DataTable into view after pagination/search events.
 *
 * Adjusts for:
 * - fixed site header
 * - DataTables fixed header overlay
 *
 * @returns {void}
 */
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

/**
 * Builds chunk file path.
 *
 * @param {string} base - dataset base name
 * @param {number} page - chunk page number
 * @returns {string}
 */
function getChunkFile(base, page) {
	return `/assets/data/${base}-page-${String(page).padStart(4, '0')}.json.gz`;
}

/**
 * Loads a compressed JSON chunk with caching.
 *
 * Steps:
 * - check memory cache
 * - fetch gzip file
 * - decompress via pako
 * - parse JSON
 *
 * @param {string} base
 * @param {number} page
 * @returns {Promise<Array<object>>}
 */
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

/**
 * Loads the precomputed search index.
 *
 * @returns {Promise<Record<string, number[]>>}
 */
function loadSearchIndex() {
	return fetch('/assets/data/jvm-functions-search-index.json.gz')
		.then(r => {
			if (!r.ok) throw new Error("Failed to load index");
			return r.arrayBuffer();
		})
		.then(buffer => {
			const decompressed = pako.inflate(new Uint8Array(buffer), {
				to: 'string'
			});

			return JSON.parse(decompressed);
		});
}

/**
 * Executes token-based search over SEARCH_INDEX.
 *
 * Matching strategy:
 * - exact match
 * - prefix match
 * - substring fallback
 * - AND intersection across tokens
 *
 * @param {string} query
 * @returns {number[]} matched record IDs
 */
function searchData(query) {
	if (!query) return [];

	const tokens = query.toLowerCase().trim().split(/\s+/);

	let resultSet = null;

	for (const token of tokens) {
		let ids = SEARCH_INDEX[token];

		// EXACT MATCH (fast path)
		if (!ids) {
			// PREFIX MATCH (fast-ish)
			const prefixMatches = [];

			for (const key in SEARCH_INDEX) {
				if (key.startsWith(token)) {
					prefixMatches.push(...SEARCH_INDEX[key]);
				}
			}

			// SUBSTRING MATCH (LIKE '%token%')
			if (prefixMatches.length === 0) {
				for (const key in SEARCH_INDEX) {
					if (key.includes(token)) {
						prefixMatches.push(...SEARCH_INDEX[key]);
					}
				}
			}

			ids = prefixMatches;
		}

		if (!ids || ids.length === 0) return [];

		// INTERSECTION logic (AND between tokens)
		resultSet = resultSet
			? new Set(ids.filter(id => resultSet.has(id)))
			: new Set(ids);
	}

	return Array.from(resultSet);
}

/**
 * Limits concurrency of async operations.
 *
 * @template T
 * @param {T[]} items
 * @param {number} limit
 * @param {(item: T) => Promise<any>} fn
 * @returns {Promise<any[]>}
 */
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

/**
 * Debounces function execution.
 *
 * @param {Function} fn
 * @param {number} delay
 * @returns {Function}
 */
function debounce(fn, delay) {
	let t;
	return function (...args) {
		clearTimeout(t);
		t = setTimeout(() => fn.apply(this, args), delay);
	};
}

/**
 * Loads metadata + search index before table initialization.
 */
Promise.all([
	fetch('/assets/data/jvm-functions-meta.json').then(r => r.json()),
	loadSearchIndex()
])
.then(([meta, index]) => {
	META = meta;
	SEARCH_INDEX = index;
	READY = true;
})
.catch(err => console.warn("Init failed", err));

// MAIN
$(document).ready(function () {

	// DataTable
	const table = $('#jvm-functions-table').DataTable({

		ajax: function (data, callback) {

			// wait for readiness instead of failing once
			if (!READY || !META || !META.chunk_size) {
				setTimeout(() => table.ajax.reload(null, false), 50);

				callback({
					draw: data.draw,
					data: [],
					recordsTotal: 0,
					recordsFiltered: 0
				});
				return;
			}

			const requestId = ++currentRequestId;

			const query = $('#jvm-functions-table_filter input').val() || '';
			const baseName = META.file.replace('.json', '');
			const chunkSize = META.chunk_size;

			// SEARCH MODE
			if (query && Object.keys(SEARCH_INDEX).length > 0) {

				const ids = searchData(query);

				if (requestId !== currentRequestId) return;

				if (!ids.length) {
					callback({
						draw: data.draw,
						data: [],
						recordsTotal: META.total_items,
						recordsFiltered: 0
					});
					return;
				}

				const pages = [...new Set(
					ids.slice(0, 100).map(id =>
						Math.floor((id - 1) / chunkSize) + 1
					)
				)];

				limitConcurrency(
					pages,
					4,
					p => loadChunk(baseName, p)
				)
				.then(chunks => {

						if (requestId !== currentRequestId) return;

						const all = chunks.flat();
						const idSet = new Set(ids);

						const filtered = all.filter(r => idSet.has(r.id));

						callback({
							draw: data.draw,
							data: filtered,
							recordsTotal: META.total_items,
							recordsFiltered: ids.length
						});
					})
					.catch(err => {
						console.error(err);
						callback({
							draw: data.draw,
							data: [],
							recordsTotal: META.total_items,
							recordsFiltered: 0
						});
					});

				return;
			}

			// PAGINATION MODE
			const start = data.start;
			const length = data.length;

			const firstChunk = Math.floor(start / chunkSize) + 1;
			const lastChunk = Math.floor((start + length - 1) / chunkSize) + 1;

			const pages = [];

			for (let p = firstChunk; p <= lastChunk; p++) {
				pages.push(p);
			}

			limitConcurrency(
				pages,
				4,
				p => loadChunk(baseName, p)
			)
			.then(chunks => {

					if (requestId !== currentRequestId) return;

					const merged = chunks.flat();

					const offset = start % chunkSize;
					const result = merged.slice(offset, offset + length);

					callback({
						draw: data.draw,
						data: result,
						recordsTotal: META.total_items,
						recordsFiltered: META.total_items
					});
				})
				.catch(err => {
					console.error(err);
					callback({
						draw: data.draw,
						data: [],
						recordsTotal: META.total_items,
						recordsFiltered: 0
					});
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

	// UI EVENTS

	// debounce search binding
	const debouncedSearch = debounce(scrollToJvmFunctionsTable, 200);

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