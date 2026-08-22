/**
 * JVM Class Initializers DataTable Module
 *
 * This module renders a DataTable for JVM class constructors
 * (initializers) using a static JSON dataset.
 *
 * Features:
 * - DataTables integration (client-side JSON)
 * - FixedHeader support
 * - Responsive + scrollable layout
 * - Smooth scroll UX on interactions
 *
 * Dataset:
 * /assets/data/jvm-class-initializers.json
 *
 */

const TABLE_ID = '#jvm-class-initializers-table';
const DATA_URL = '/assets/data/jvm-class-initializers.json';

/**
 * Scrolls the JVM Class Initializers table into view.
 *
 * Adjusts for:
 * - site header height
 * - DataTables fixed header overlay
 *
 * Triggered after:
 * - pagination
 * - page size change
 * - search input
 *
 * @returns {void}
 */
function scrollToJvmClassInitializersTable() {
	const $table = $(TABLE_ID);

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
	}, 300);
}

/**
 * Initialize DataTable
 */
function initDataTable() {
	// JVM Class Initializers
  return $(TABLE_ID).DataTable({
    ajax: {
      url: DATA_URL,
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
      {
        data: 'className',
        render: val =>
          `<code class="language-plaintext highlighter-rouge">${val}</code>`
      },
      {
        data: 'qualifiedName',
        render: val =>
          `<code class="language-plaintext highlighter-rouge">${val}</code>`
      },
      {
        data: 'constructorParameterTypes',
        render: arr =>
          Array.isArray(arr) && arr.length ? arr.join(', ') : '-'
      },
      {
        data: 'isInvocable',
        render: val => (val ? '✅' : '❌')
      }
    ],

    language: {
      url:
        'https://cdn.datatables.net/plug-ins/1.13.6/i18n/ar.json'
    }
  });
}

/**
 * Bind UI events
 * Tracks user interactions: pagination, length, search input
 */
function bindEvents() {
	const scroll = () => scrollToJvmClassInitializersTable();

	$(document).on(
		'input',
		`${TABLE_ID}_filter > label > input[type=search]`,
		scroll
	);

	$(document).on(
		'change',
		`${TABLE_ID}_length > label > select`,
		scroll
	);

	$(document).on(
		'click',
		`${TABLE_ID}_paginate > span > a.paginate_button:not(.current)`,
		scroll
	);

	$(document).on(
		'click',
		`${TABLE_ID}_previous`,
		scroll
	);

	$(document).on(
		'click',
		`${TABLE_ID}_next`,
		scroll
	);
}

/**
 * Module entry point
 */
function initJvmClassInitializersTable() {
  $(document).ready(() => {
    initDataTable();
    bindEvents();
  });
}

export default initJvmClassInitializersTable;