(function () {
  const groups = Array.from(document.querySelectorAll("[data-docs-nav-group]"));
  const storageKey = "devlab-docs-nav-state-v1";

  function getStoredState() {
    try {
      const value = localStorage.getItem(storageKey);

      if (!value) {
        return {};
      }

      const state = JSON.parse(value);

      if (!state || typeof state !== "object" || Array.isArray(state)) {
        return {};
      }

      return state;
    } catch (_) {
      return {};
    }
  }

  function storeState(state) {
    try {
      localStorage.setItem(storageKey, JSON.stringify(state));
    } catch (_) {
      // Keep native details behavior when storage is unavailable.
    }
  }

  const state = getStoredState();
  const groupsByKey = new Map();

  function setGroupOpen(group, isOpen) {
    const toggle = group.querySelector("[data-docs-nav-toggle]");
    const children = group.querySelector("[data-docs-nav-children]");

    if (!toggle || !children) {
      return;
    }

    group.classList.toggle("is-open", isOpen);
    toggle.setAttribute("aria-expanded", String(isOpen));
    children.hidden = !isOpen;
  }

  function setMatchingGroupsOpen(groupKey, isOpen) {
    groupsByKey.get(groupKey)?.forEach(function (group) {
      setGroupOpen(group, isOpen);
    });
  }

  groups.forEach(function (group) {
    const groupKey = group.dataset.docsNavGroup;

    if (!groupKey) {
      return;
    }

    if (!groupsByKey.has(groupKey)) {
      groupsByKey.set(groupKey, []);
    }

    groupsByKey.get(groupKey).push(group);
  });

  groups.forEach(function (group) {
    const groupKey = group.dataset.docsNavGroup;
    const toggle = group.querySelector("[data-docs-nav-toggle]");
    const isOpenFromServer = group.hasAttribute("data-docs-nav-active-branch");

    if (!groupKey || !toggle) {
      return;
    }

    setGroupOpen(group, isOpenFromServer || state[groupKey] === true);

    toggle.addEventListener("click", function () {
      const isOpen = toggle.getAttribute("aria-expanded") !== "true";

      state[groupKey] = isOpen;
      setMatchingGroupsOpen(groupKey, isOpen);
      storeState(state);
    });

    toggle.hidden = false;
  });

  function scrollCurrentLinks() {
    window.requestAnimationFrame(function () {
      document
        .querySelectorAll(".docs-sidebar-nav, .site-menu-docs-nav")
        .forEach(function (navigation) {
          const currentLink = navigation.querySelector(
            ".docs-nav-link[aria-current='page']"
          );

          if (!currentLink || currentLink.getClientRects().length === 0) {
            return;
          }

          currentLink.scrollIntoView({
            block: "nearest",
            inline: "nearest",
          });
        });
    });
  }

  scrollCurrentLinks();

  document
    .querySelectorAll(
      "[data-site-menu-toggle], [data-site-menu-panel-target='docs']"
    )
    .forEach(function (control) {
      control.addEventListener("click", scrollCurrentLinks);
    });
})();
