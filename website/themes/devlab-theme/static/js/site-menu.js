(function () {
  const menu = document.querySelector("[data-site-menu]");
  const toggle = document.querySelector("[data-site-menu-toggle]");
  const backdrop = document.querySelector("[data-site-menu-backdrop]");

  if (!menu || !toggle || !backdrop) {
    return;
  }

  const closeButtons = menu.querySelectorAll("[data-site-menu-close]");
  const panelButtons = menu.querySelectorAll("[data-site-menu-panel-target]");
  const panels = menu.querySelectorAll("[data-site-menu-panel]");
  const initialPanel = menu.dataset.siteMenuInitialPanel || "main";
  const mobileQuery = window.matchMedia("(max-width: 760px)");
  const backgroundElements = Array.from(
    document.querySelectorAll(
      ".skip-link, .site-announcement, .site-header, .site-main, .site-footer"
    )
  ).filter(function (element) {
    return !menu.contains(element);
  });
  const focusableSelector = [
    "a[href]",
    "button:not([disabled])",
    "input:not([disabled])",
    "select:not([disabled])",
    "textarea:not([disabled])",
    "[tabindex]:not([tabindex='-1'])"
  ].join(",");

  document.body.classList.add("site-menu-is-ready");

  function createFocusManager() {
    let returnFocus = toggle;

    function getFocusableElements(container) {
      return Array.from(container.querySelectorAll(focusableSelector)).filter(
        function (element) {
          return element.getClientRects().length > 0;
        }
      );
    }

    function rememberReturnFocus() {
      returnFocus = document.activeElement || toggle;
    }

    function setBackgroundInert(isInert) {
      backgroundElements.forEach(function (element) {
        element.inert = isInert;
      });
    }

    function focusFirst(container) {
      window.requestAnimationFrame(function () {
        const firstControl = getFocusableElements(container)[0];

        if (firstControl) {
          firstControl.focus();
        }
      });
    }

    function restoreReturnFocus() {
      const focusTarget = returnFocus && returnFocus.isConnected
        ? returnFocus
        : toggle;

      focusTarget.focus();
    }

    function trapFocus(event, container) {
      const focusableElements = getFocusableElements(container);

      if (!focusableElements.length) {
        event.preventDefault();
        return;
      }

      const firstElement = focusableElements[0];
      const lastElement = focusableElements[focusableElements.length - 1];
      const focusIsInside = container.contains(document.activeElement);

      if (
        event.shiftKey &&
        (!focusIsInside || document.activeElement === firstElement)
      ) {
        event.preventDefault();
        lastElement.focus();
      } else if (
        !event.shiftKey &&
        (!focusIsInside || document.activeElement === lastElement)
      ) {
        event.preventDefault();
        firstElement.focus();
      }
    }

    return {
      focusFirst: focusFirst,
      rememberReturnFocus: rememberReturnFocus,
      restoreReturnFocus: restoreReturnFocus,
      setBackgroundInert: setBackgroundInert,
      trapFocus: trapFocus
    };
  }

  function createPanelController(focusManager) {
    function setPanel(panelName) {
      panels.forEach(function (panel) {
        const isActive = panel.dataset.siteMenuPanel === panelName;

        panel.hidden = !isActive;
        panel.classList.toggle("is-active", isActive);
      });

      panelButtons.forEach(function (button) {
        if (button.hasAttribute("aria-expanded")) {
          button.setAttribute(
            "aria-expanded",
            String(button.dataset.siteMenuPanelTarget === panelName)
          );
        }
      });
    }

    function focusPanel(panelName) {
      const panel = Array.from(panels).find(function (candidate) {
        return candidate.dataset.siteMenuPanel === panelName;
      });

      if (!panel) {
        return;
      }

      focusManager.focusFirst(panel);
    }

    return {
      focus: focusPanel,
      set: setPanel
    };
  }

  function createMenuController(focusManager, panelController) {
    function isOpen() {
      return menu.classList.contains("is-open");
    }

    function setOpen(open, shouldRestoreFocus) {
      const wasOpen = isOpen();

      if (open) {
        focusManager.rememberReturnFocus();
        panelController.set(initialPanel);
      }

      menu.inert = !open;
      menu.classList.toggle("is-open", open);
      menu.setAttribute("aria-hidden", String(!open));
      toggle.setAttribute("aria-expanded", String(open));
      backdrop.hidden = !open;
      document.body.classList.toggle("site-menu-is-open", open);
      focusManager.setBackgroundInert(open);

      if (open) {
        focusManager.focusFirst(menu);
      } else if (wasOpen && shouldRestoreFocus !== false) {
        focusManager.restoreReturnFocus();
      }
    }

    function handleKeydown(event) {
      if (!isOpen()) {
        return;
      }

      if (event.key === "Escape") {
        setOpen(false);
        return;
      }

      if (event.key !== "Tab") {
        return;
      }

      focusManager.trapFocus(event, menu);
    }

    function closeOnDesktop(event) {
      if (!event.matches) {
        setOpen(false, menu.contains(document.activeElement));
      }
    }

    return {
      closeOnDesktop: closeOnDesktop,
      handleKeydown: handleKeydown,
      isOpen: isOpen,
      setOpen: setOpen
    };
  }

  const focusManager = createFocusManager();
  const panelController = createPanelController(focusManager);
  const menuController = createMenuController(focusManager, panelController);

  document.addEventListener("devlab:search-open", function () {
    if (menuController.isOpen()) menuController.setOpen(false, false);
  });

  toggle.addEventListener("click", function () {
    menuController.setOpen(!menuController.isOpen());
  });

  closeButtons.forEach(function (button) {
    button.addEventListener("click", function () {
      menuController.setOpen(false);
    });
  });

  panelButtons.forEach(function (button) {
    button.addEventListener("click", function () {
      const panelName = button.dataset.siteMenuPanelTarget;

      panelController.set(panelName);
      panelController.focus(panelName);
    });
  });

  backdrop.addEventListener("click", function () {
    menuController.setOpen(false);
  });

  menu.querySelectorAll("a").forEach(function (link) {
    link.addEventListener("click", function () {
      menuController.setOpen(false, false);
    });
  });

  document.addEventListener("keydown", menuController.handleKeydown);

  if (mobileQuery.addEventListener) {
    mobileQuery.addEventListener("change", menuController.closeOnDesktop);
  } else {
    mobileQuery.addListener(menuController.closeOnDesktop);
  }
})();
