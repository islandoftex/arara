(() => {
  const searchRoot = document.querySelector("[data-search]");
  const searchInput = document.querySelector("[data-search-input]");
  const searchResults = document.querySelector("[data-search-results]");
  const searchList = document.querySelector("[data-search-list]");
  const searchMessage = document.querySelector("[data-search-message]");
  const searchStatus = document.querySelector("[data-search-status]");
  const searchShortcut = document.querySelector("[data-search-shortcut]");
  const supportedIndexFormat = "elasticlunr_javascript";
  const searchIndexFormat =
    searchRoot?.dataset.searchIndexFormat || supportedIndexFormat;

  if (!searchRoot || searchIndexFormat !== supportedIndexFormat) {
    return;
  }

  const searchLabels = {
    start: searchRoot?.dataset.searchStartLabel || "Start typing to search.",
    noResults:
      searchRoot?.dataset.searchNoResultsLabel || "No results found.",
    unavailable:
      searchRoot?.dataset.searchUnavailableLabel ||
      "Search is currently unavailable.",
    resultsCount:
      searchRoot?.dataset.searchResultsCountLabel || "Results found: {count}.",
  };

  class SearchIndexAdapter {
    constructor() {
      this.index = null;
      this.unavailable = false;
    }

    loadIndex() {
      if (
        !window.elasticlunr ||
        !window.elasticlunr.Index ||
        typeof window.elasticlunr.Index.load !== "function" ||
        !window.searchIndex
      ) {
        return null;
      }

      return window.elasticlunr.Index.load(window.searchIndex);
    }

    getDocument(reference) {
      return this.index.documentStore.getDoc(reference);
    }

    isUnavailable() {
      return this.unavailable;
    }

    markUnavailable() {
      this.index = null;
      this.unavailable = true;
    }

    search(query) {
      if (!this.index) {
        this.index = this.loadIndex();
      }

      if (!this.index) {
        throw new Error("Search index is unavailable.");
      }

      return this.index.search(query, {
        bool: "OR",
        expand: true,
      });
    }
  }

  class SearchView {
    constructor(indexAdapter) {
      this.indexAdapter = indexAdapter;
      this.activeResultIndex = -1;
    }

    getSearchContainer() {
      if (!searchInput) {
        return null;
      }

      return searchInput.closest(
        "dialog, [data-search-modal], [data-search-drawer]"
      );
    }

    setExpanded(isExpanded) {
      searchInput?.setAttribute("aria-expanded", String(isExpanded));
    }

    announceStatus(message) {
      if (searchStatus) {
        searchStatus.textContent = message;
      }
    }

    getResultItems() {
      if (!searchResults || !searchList || searchResults.hidden) {
        return [];
      }

      return Array.from(
        searchList.querySelectorAll(".site-search-result")
      );
    }

    clearActiveResult() {
      this.getResultItems().forEach((item) => {
        item.classList.remove("is-active");
        item.setAttribute("aria-selected", "false");
      });

      this.activeResultIndex = -1;
      searchInput?.removeAttribute("aria-activedescendant");
    }

    createResultItem(result, index) {
      const doc = this.indexAdapter.getDocument(result.ref);

      const link = document.createElement("a");
      link.id = `site-search-result-${index}`;
      link.className = "site-search-result";
      link.href = result.ref;
      link.tabIndex = -1;
      link.setAttribute("role", "option");
      link.setAttribute("aria-selected", "false");

      const title = document.createElement("span");
      title.className = "site-search-result-title";
      title.textContent = doc.title || result.ref;

      const description = document.createElement("span");
      description.className = "site-search-result-description";
      description.textContent = doc.description || result.ref;

      link.appendChild(title);
      link.appendChild(description);

      return link;
    }

    renderMessage(message) {
      if (!searchResults || !searchList || !searchMessage) {
        return;
      }

      this.activeResultIndex = -1;
      searchInput?.removeAttribute("aria-activedescendant");
      searchList.replaceChildren();
      searchMessage.textContent = message;
      searchMessage.hidden = false;
      searchResults.hidden = false;
      this.setExpanded(true);
      this.announceStatus(message);
    }

    areResultsVisible() {
      return Boolean(searchResults && !searchResults.hidden);
    }

    clearResults() {
      if (!searchResults || !searchList || !searchMessage) {
        return;
      }

      this.activeResultIndex = -1;
      searchInput?.removeAttribute("aria-activedescendant");
      searchList.replaceChildren();
      searchMessage.textContent = "";
      searchMessage.hidden = true;
      searchResults.hidden = true;
      this.setExpanded(false);
      this.announceStatus("");
    }

    closeContainer() {
      const container = this.getSearchContainer();

      if (!container) {
        return;
      }

      if (container.tagName === "DIALOG" && container.open) {
        container.close();
      }

      container.hidden = true;
      container.classList.remove("is-open");
      container.setAttribute("aria-hidden", "true");
    }

    getActiveResultIndex() {
      return this.activeResultIndex;
    }

    isEventInsideRoot(event) {
      return searchRoot.contains(event.target);
    }

    openActiveResult() {
      const items = this.getResultItems();

      if (
        this.activeResultIndex < 0 ||
        !items[this.activeResultIndex]
      ) {
        return;
      }

      items[this.activeResultIndex].click();
    }

    openContainer() {
      const container = this.getSearchContainer();

      if (!container) {
        return;
      }

      if (container.tagName === "DIALOG" && !container.open) {
        if (typeof container.showModal === "function") {
          container.showModal();
        } else {
          container.setAttribute("open", "");
        }
      }

      container.hidden = false;
      container.classList.add("is-open");
      container.setAttribute("aria-hidden", "false");
    }

    renderResults(results) {
      if (!searchResults || !searchList || !searchMessage) {
        return;
      }

      this.activeResultIndex = -1;
      searchInput?.removeAttribute("aria-activedescendant");
      searchList.replaceChildren();

      if (results.length === 0) {
        this.renderMessage(searchLabels.noResults);
        return;
      }

      results.slice(0, 8).forEach((result, index) => {
        searchList.appendChild(this.createResultItem(result, index));
      });

      searchMessage.textContent = "";
      searchMessage.hidden = true;
      searchResults.hidden = false;
      this.setExpanded(true);
      this.announceStatus(
        searchLabels.resultsCount.replace(
          "{count}",
          String(results.length)
        )
      );
    }

    setActiveResult(index) {
      const items = this.getResultItems();

      if (items.length === 0) {
        this.clearActiveResult();
        return;
      }

      this.activeResultIndex = (index + items.length) % items.length;

      items.forEach((item, itemIndex) => {
        const isActive = itemIndex === this.activeResultIndex;

        item.classList.toggle("is-active", isActive);
        item.setAttribute("aria-selected", String(isActive));
      });

      const activeItem = items[this.activeResultIndex];

      searchInput?.setAttribute("aria-activedescendant", activeItem.id);
      activeItem.scrollIntoView({ block: "nearest" });
    }

    updateShortcutLabel() {
      if (!searchShortcut) {
        return;
      }

      const platform =
        navigator.userAgentData?.platform || navigator.platform || "";

      searchShortcut.textContent = /Mac|iPhone|iPad|iPod/.test(platform)
        ? "⌘ K"
        : "Ctrl+K";
    }
  }

  class SearchController {
    constructor(indexAdapter, view) {
      this.indexAdapter = indexAdapter;
      this.view = view;
    }

    runSearch(query) {
      if (query.length === 0) {
        this.view.renderMessage(searchLabels.start);
        return;
      }

      if (query.length < 2) {
        this.view.clearResults();
        return;
      }

      if (this.indexAdapter.isUnavailable()) {
        this.view.renderMessage(searchLabels.unavailable);
        return;
      }

      try {
        this.view.renderResults(this.indexAdapter.search(query));
      } catch (_) {
        this.indexAdapter.markUnavailable();
        this.view.renderMessage(searchLabels.unavailable);
      }
    }

    focusInput() {
      if (!searchInput) {
        return;
      }

      document.dispatchEvent(new Event("devlab:search-open"));
      this.view.openContainer();
      searchInput.focus();
      searchInput.select();
      this.runSearch(searchInput.value.trim());
    }

    moveActiveResult(direction) {
      const activeIndex = this.view.getActiveResultIndex();
      const resultCount = this.view.getResultItems().length;
      const nextIndex =
        activeIndex < 0
          ? direction > 0
            ? 0
            : resultCount - 1
          : activeIndex + direction;

      this.view.setActiveResult(nextIndex);
    }

    handleInputKeydown(event) {
      const hasResults = this.view.getResultItems().length > 0;

      if (event.key === "ArrowDown" && hasResults) {
        event.preventDefault();
        this.moveActiveResult(1);
        return;
      }

      if (event.key === "ArrowUp" && hasResults) {
        event.preventDefault();
        this.moveActiveResult(-1);
        return;
      }

      if (event.key === "Enter" && hasResults) {
        if (this.view.getActiveResultIndex() >= 0) {
          event.preventDefault();
          this.view.openActiveResult();
        }

        return;
      }

      if (event.key === "Tab") {
        this.view.clearResults();
        return;
      }

      if (event.key === "Escape") {
        event.preventDefault();
        if (this.view.areResultsVisible()) {
          this.view.clearResults();
          return;
        }

        searchInput.value = "";
        this.view.clearResults();
        searchInput.blur();
        this.view.closeContainer();
      }
    }

    handleDocumentKeydown(event) {
      if (!searchInput) {
        return;
      }

      if (
        (event.ctrlKey || event.metaKey) &&
        !event.altKey &&
        event.key.toLowerCase() === "k"
      ) {
        event.preventDefault();
        this.focusInput();
      }
    }

    handleDocumentClick(event) {
      if (!searchRoot || this.view.isEventInsideRoot(event)) {
        return;
      }

      this.view.clearResults();
    }

    init() {
      if (searchInput) {
        searchInput.addEventListener("focus", () => {
          this.runSearch(searchInput.value.trim());
        });

        searchInput.addEventListener("input", (event) => {
          this.runSearch(event.target.value.trim());
        });

        searchInput.addEventListener("keydown", (event) => {
          this.handleInputKeydown(event);
        });
      }

      this.view.updateShortcutLabel();

      document.addEventListener("keydown", (event) => {
        this.handleDocumentKeydown(event);
      });

      document.addEventListener("click", (event) => {
        this.handleDocumentClick(event);
      });

      // The bundle loads lazily: the visitor may have focused or typed in the
      // field before this script executed, so catch up with the current query.
      if (searchInput && document.activeElement === searchInput) {
        this.view.openContainer();
        this.runSearch(searchInput.value.trim());
      }
    }
  }

  const searchIndexAdapter = new SearchIndexAdapter();
  const searchView = new SearchView(searchIndexAdapter);
  const searchController = new SearchController(
    searchIndexAdapter,
    searchView
  );

  searchController.init();
})();
