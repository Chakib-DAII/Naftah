document.addEventListener("DOMContentLoaded", function () {
  // Select all <pre><code> blocks for Naftah language
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
});
