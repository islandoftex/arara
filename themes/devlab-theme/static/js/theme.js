(function () {
  const root = document.documentElement;
  const toggle = document.querySelector("[data-theme-toggle]");
  const storageKey = "devlab-theme-color-mode";
  const modes = ["light", "dark", "system"];
  const systemThemeQuery = window.matchMedia
    ? window.matchMedia("(prefers-color-scheme: dark)")
    : null;

  function isValidMode(mode) {
    return modes.includes(mode);
  }

  function getDefaultMode() {
    return isValidMode(root.dataset.themeDefault) ? root.dataset.themeDefault : "system";
  }

  function getStoredMode() {
    try {
      const storedMode = localStorage.getItem(storageKey);
      return isValidMode(storedMode) ? storedMode : null;
    } catch (_) {
      return null;
    }
  }

  function storeMode(mode) {
    try {
      localStorage.setItem(storageKey, mode);
    } catch (_) {
      // Ignore storage errors and keep the in-page theme state.
    }
  }

  function getSystemTheme() {
    return systemThemeQuery && systemThemeQuery.matches ? "dark" : "light";
  }

  function resolveTheme(mode) {
    return mode === "system" ? getSystemTheme() : mode;
  }

  function getCurrentMode() {
    return isValidMode(root.dataset.themeMode) ? root.dataset.themeMode : getDefaultMode();
  }

  function getNextMode(mode) {
    if (mode === "system") {
      return "light";
    }

    if (mode === "light") {
      return "dark";
    }

    return "system";
  }

  function getModeLabel(mode) {
    if (mode === "light") {
      return toggle?.dataset.themeLightLabel || "Light theme";
    }

    if (mode === "dark") {
      return toggle?.dataset.themeDarkLabel || "Dark theme";
    }

    return toggle?.dataset.themeSystemLabel || "System theme";
  }

  function setTheme(mode, persist = false) {
    const selectedMode = isValidMode(mode) ? mode : getDefaultMode();
    const resolvedTheme = resolveTheme(selectedMode);

    root.dataset.themeMode = selectedMode;
    root.dataset.theme = resolvedTheme;

    if (persist) {
      storeMode(selectedMode);
    }

    if (toggle) {
      const modeLabel = getModeLabel(selectedMode);
      toggle.setAttribute("aria-label", modeLabel);
      toggle.setAttribute("title", modeLabel);
    }
  }

  const initialMode = getStoredMode() || getDefaultMode();
  setTheme(initialMode);

  if (toggle) {
    toggle.addEventListener("click", () => {
      const nextMode = getNextMode(getCurrentMode());

      setTheme(nextMode, true);
    });
  }

  if (systemThemeQuery) {
    const handleSystemThemeChange = () => {
      if (getCurrentMode() === "system") {
        setTheme("system");
      }
    };

    if (systemThemeQuery.addEventListener) {
      systemThemeQuery.addEventListener("change", handleSystemThemeChange);
    } else if (systemThemeQuery.addListener) {
      systemThemeQuery.addListener(handleSystemThemeChange);
    }
  }
})();
