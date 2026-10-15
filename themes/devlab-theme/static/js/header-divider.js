(function () {
  const header = document.querySelector("[data-scroll-header]");
  if (!header) {
    return;
  }

  function updateDivider() {
    header.classList.toggle("site-header-at-top", window.scrollY <= 0);
  }

  updateDivider();
  window.addEventListener("scroll", updateDivider, { passive: true });
  window.addEventListener("pageshow", updateDivider);
})();
