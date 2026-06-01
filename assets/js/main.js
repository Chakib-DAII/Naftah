/**
 * Naftah Documentation UI Enhancements
 *
 * This script enhances the documentation site UX by adding:
 * - Copy-to-clipboard buttons for Naftah code blocks
 * - Mobile navigation toggle behavior
 * - Automatic table wrapping for horizontal scrolling
 * - URL hash tracking based on scroll position
 *
 * It runs after DOM content is fully loaded.
 */

document.addEventListener("DOMContentLoaded", function () {
	/**
	* CODE BLOCK: COPY BUTTON FOR NAFTAH SNIPPETS
	*
	* Enhances all <pre><code class="language-naftah"> blocks by:
	* - Wrapping them in a styled container
	* - Adding a "copy" button
	* - Copying code to clipboard on click
	* - Providing temporary visual feedback
	*/
	document.querySelectorAll("pre > code.language-naftah").forEach(function(codeBlock) {

	const pre = codeBlock.parentNode;

	// Wrap <pre> in a container div
	const wrapper = document.createElement("div");
	wrapper.className = "code-block";
	pre.parentNode.insertBefore(wrapper, pre);
	wrapper.appendChild(pre);

	// Create copy button
	const button = document.createElement("button");
	button.className = "copy-button";
	button.textContent = "نسخ";
	wrapper.appendChild(button);

	// Copy functionality
	button.addEventListener("click", () => {
	  navigator.clipboard.writeText(codeBlock.innerText).then(() => {
		button.textContent = "تم النسخ";
		setTimeout(() => (button.textContent = "نسخ"), 1500);
	  }).catch(err => {
		console.error("Copy failed", err);
	  });
	});
	});


	/**
	* MOBILE NAVIGATION TOGGLE
	*
	* Enables hamburger menu behavior on small screens.
	*/
	const toggle = document.querySelector('.menu-toggle');
	const nav = document.querySelector('nav');

	if (toggle && nav) {

	// Toggle menu on click
	toggle.addEventListener('click', () => {
	  if (window.innerWidth < 600) {
		nav.classList.toggle('active');
	  }
	});

	// Reset menu on window resize
	window.addEventListener('resize', () => {
	  if (window.innerWidth > 600) {
		nav.classList.remove('active');
	  }
	});
	}

	/**
	 * TABLE WRAPPER (HORIZONTAL SCROLL SUPPORT)
	 *
	 * Wraps all <table> elements in a container to enable:
	 * - Horizontal scrolling on small screens
	 * - Better layout stability
	 */
	document.querySelectorAll('table').forEach(table => {

	// Skip if already wrapped
	if (table.parentElement.classList.contains('table-wrapper')) return;

	const wrapper = document.createElement('div');
	wrapper.className = 'table-wrapper';

	table.parentNode.insertBefore(wrapper, table);
	wrapper.appendChild(table);
	});

	/**
	 * SCROLL-BASED URL HASH TRACKING
	 *
	 * Updates browser URL hash based on visible headings.
	 * Useful for:
	 * - Deep linking
	 * - Navigation state persistence
	 */
	const headings = document.querySelectorAll('[id^="nla-"]');

	const observer = new IntersectionObserver(
	(entries) => {
	  entries.forEach((entry) => {
		if (entry.isIntersecting) {
		  history.replaceState(null, null, `#${entry.target.id}`);
		}
	  });
	},
	{
	  rootMargin: '-120px 0px -70% 0px',
	  threshold: 0
	}
	);

	headings.forEach((heading) => observer.observe(heading));
});