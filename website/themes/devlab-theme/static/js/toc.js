(function () {
  const content =
    document.querySelector("[data-devlab-toc-content]") ||
    document.querySelector(".docs-content");
  const tocRoots = Array.from(document.querySelectorAll("[data-devlab-toc], .docs-toc"));

  if (!content || tocRoots.length === 0) {
    return;
  }

  function normalizePath(path) {
    return path.replace(/\/index\.html$/, "/");
  }

  function decodeHash(hash) {
    if (!hash || hash === "#") {
      return "";
    }

    try {
      return decodeURIComponent(hash.slice(1));
    } catch (_) {
      return hash.slice(1);
    }
  }

  function getSamePageHashId(link) {
    try {
      const url = new URL(link.getAttribute("href"), window.location.href);

      if (normalizePath(url.pathname) !== normalizePath(window.location.pathname)) {
        return "";
      }

      return decodeHash(url.hash);
    } catch (_) {
      return "";
    }
  }

  // A page can render more than one table of contents (for example a sticky
  // sidebar and a collapsed one for narrow screens); keep them in sync.
  const roots = tocRoots
    .map(function (toc) {
      const links = Array.from(toc.querySelectorAll("a[href]"));
      const linkById = new Map();

      links.forEach(function (link) {
        const id = getSamePageHashId(link);

        if (!id) {
          return;
        }

        link.classList.add("toc-link");

        if (!linkById.has(id)) {
          linkById.set(id, link);
        }
      });

      return {
        links: links,
        groups: Array.from(toc.querySelectorAll("[data-docs-toc-group]")),
        linkById: linkById
      };
    })
    .filter(function (root) {
      return root.linkById.size > 0;
    });

  if (roots.length === 0) {
    return;
  }

  const headings = Array.from(
    content.querySelectorAll("h1[id], h2[id], h3[id], h4[id], h5[id], h6[id]")
  ).filter(function (heading) {
    return roots.some(function (root) {
      return root.linkById.has(heading.id);
    });
  });

  if (headings.length === 0) {
    return;
  }

  let activeHeadingId = "";

  function setActiveHeading(id) {
    if (activeHeadingId === id) {
      return;
    }

    activeHeadingId = id;

    roots.forEach(function (root) {
      root.links.forEach(function (link) {
        link.classList.remove("is-active");
        link.removeAttribute("aria-current");
      });

      root.groups.forEach(function (group) {
        group.open = false;
      });

      const activeLink = root.linkById.get(id);

      if (!activeLink) {
        return;
      }

      activeLink.classList.add("is-active");
      activeLink.setAttribute("aria-current", "location");

      let parentGroup = activeLink.closest("[data-docs-toc-group]");

      while (parentGroup) {
        parentGroup.open = true;
        parentGroup = parentGroup.parentElement.closest("[data-docs-toc-group]");
      }
    });
  }

  function getHeaderOffset() {
    const header = document.querySelector(".site-header");

    if (!header) {
      return 96;
    }

    return Math.ceil(header.getBoundingClientRect().height + 16);
  }

  function getCurrentHeading() {
    const offset = getHeaderOffset();
    const visibleHeadings = headings.filter(function (heading) {
      return heading.getClientRects().length > 0;
    });
    let currentHeading = visibleHeadings[0];

    visibleHeadings.forEach(function (heading) {
      if (heading.getBoundingClientRect().top - offset <= 1) {
        currentHeading = heading;
      }
    });

    return currentHeading;
  }

  function updateActiveHeading() {
    const currentHeading = getCurrentHeading();
    setActiveHeading(currentHeading ? currentHeading.id : "");
  }

  setActiveHeading(headings[0].id);
  updateActiveHeading();

  roots.forEach(function (root) {
    root.links.forEach(function (link) {
      link.addEventListener("click", function () {
        const id = getSamePageHashId(link);

        if (id && root.linkById.has(id)) {
          setActiveHeading(id);
          window.setTimeout(updateActiveHeading, 120);
        }
      });
    });
  });

  window.addEventListener("hashchange", function () {
    window.requestAnimationFrame(updateActiveHeading);
  });

  content.addEventListener("toggle", updateActiveHeading, true);
  const visibilityObserver = new MutationObserver(function () {
    window.requestAnimationFrame(updateActiveHeading);
  });
  visibilityObserver.observe(content, { attributes: true, subtree: true, attributeFilter: ["hidden"] });

  if (!("IntersectionObserver" in window)) {
    window.addEventListener("scroll", updateActiveHeading, { passive: true });
    return;
  }

  const observer = new IntersectionObserver(
    function () {
      window.requestAnimationFrame(updateActiveHeading);
    },
    {
      rootMargin: `-${getHeaderOffset()}px 0px -70% 0px`,
      threshold: 0
    }
  );

  headings.forEach(function (heading) {
    observer.observe(heading);
  });
})();
