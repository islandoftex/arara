// Catalog filtering progressively enhances the server-rendered links.
(function () {
  var root = document.querySelector('[data-dlh-catalog]');

  if (!root) return;

  var grid = root.querySelector('[data-dlh-grid]');

  if (!grid) return;

  var toolbar = root.querySelector('[data-dlh-toolbar]');
  var result = root.querySelector('[data-dlh-result]');
  var empty = root.querySelector('[data-dlh-empty]');
  var searchInput = root.querySelector('[data-dlh-search]');
  var vendorSelect = root.querySelector('[data-dlh-vendor]');
  var sortSelect = root.querySelector('[data-dlh-sort]');
  var resetButton = root.querySelector('[data-dlh-reset]');
  var seeAll = root.querySelector('[data-dlh-see-all]');
  var supportButtons = Array.prototype.slice.call(root.querySelectorAll('[data-dlh-support]'));
  var countNodes = Array.prototype.slice.call(root.querySelectorAll('[data-dlh-count]'));
  var cards = Array.prototype.slice.call(grid.querySelectorAll('[data-dlh-card]'));

  var state = {
    query: '',
    support: 'all',
    vendor: 'all',
    sort: 'default'
  };

  var supportOrder = supportButtons
    .map(function (button) {
      return button.getAttribute('data-dlh-support');
    })
    .filter(function (value) {
      return value && value !== 'all';
    });

  cards.forEach(function (card, index) {
    card.dlhIndex = index;
  });

  // Reveal the build-time controls once filtering is available.
  if (toolbar) toolbar.hidden = false;
  if (resetButton) resetButton.hidden = false;

  function normalize(value) {
    return String(value || '')
      .toLowerCase()
      .replace(/\s+/g, ' ')
      .trim();
  }

  function attribute(element, name) {
    return element.getAttribute(name) || '';
  }

  function matches(card, skipSupport) {
    if (!skipSupport && state.support !== 'all' && attribute(card, 'data-support') !== state.support) {
      return false;
    }

    if (state.vendor !== 'all' && attribute(card, 'data-vendor') !== state.vendor) {
      return false;
    }

    if (state.query) {
      return normalize(attribute(card, 'data-search')).indexOf(state.query) !== -1;
    }

    return true;
  }

  function refreshCounts() {
    countNodes.forEach(function (node) {
      if (seeAll && seeAll.contains(node)) {
        node.textContent = String(cards.length);
        return;
      }
      var value = attribute(node, 'data-dlh-count');
      var total = 0;

      cards.forEach(function (card) {
        if (value !== 'all' && attribute(card, 'data-support') !== value) return;
        if (matches(card, true)) total += 1;
      });

      node.textContent = String(total);
    });
  }

  function apply() {
    var visible = 0;

    cards.forEach(function (card) {
      var isVisible = matches(card, false);

      card.hidden = !isVisible;

      if (isVisible) visible += 1;
    });

    if (empty) empty.hidden = visible !== 0;

    if (result) {
      result.hidden = false;
      result.textContent = (root.getAttribute('data-result-label') || 'Showing {count} of {total} products')
        .replace('{count}', String(visible)).replace('{total}', String(cards.length));
    }

    refreshCounts();
  }

  function supportRank(value) {
    var index = supportOrder.indexOf(value);

    return index === -1 ? supportOrder.length : index;
  }

  function compare(a, b) {
    if (state.sort === 'support') {
      var rank = supportRank(attribute(a, 'data-support')) - supportRank(attribute(b, 'data-support'));

      if (rank !== 0) return rank;
    }

    if (state.sort === 'name' || state.sort === 'support') {
      return normalize(attribute(a, 'data-name')).localeCompare(normalize(attribute(b, 'data-name')), document.documentElement.lang || undefined);
    }

    return a.dlhIndex - b.dlhIndex;
  }

  function sortCards() {
    cards
      .slice()
      .sort(compare)
      .forEach(function (card) {
        grid.appendChild(card);
      });
  }

  function setSupport(value) {
    state.support = value;

    supportButtons.forEach(function (button) {
      button.setAttribute('aria-pressed', attribute(button, 'data-dlh-support') === value ? 'true' : 'false');
    });
  }

  function resetFilters() {
    state.query = '';
    state.vendor = 'all';
    state.sort = 'default';

    setSupport('all');

    if (searchInput) searchInput.value = '';
    if (vendorSelect) vendorSelect.value = 'all';
    if (sortSelect) sortSelect.value = 'default';

    apply();
    sortCards();

    if (searchInput) searchInput.focus();
  }

  if (searchInput) {
    searchInput.addEventListener('input', function () {
      state.query = normalize(searchInput.value);
      apply();
    });

    searchInput.addEventListener('keydown', function (event) {
      if (event.key !== 'Escape' || !searchInput.value) return;

      searchInput.value = '';
      state.query = '';
      apply();
    });
  }

  if (vendorSelect) {
    vendorSelect.addEventListener('change', function () {
      state.vendor = vendorSelect.value;
      apply();
    });
  }

  if (sortSelect) {
    sortSelect.addEventListener('change', function () {
      state.sort = sortSelect.value;
      sortCards();
    });
  }

  supportButtons.forEach(function (button) {
    button.addEventListener('click', function () {
      setSupport(attribute(button, 'data-dlh-support') || 'all');
      apply();
    });
  });

  if (resetButton) {
    resetButton.addEventListener('click', resetFilters);
  }

  // The featured shortcut resets every filter, then brings the full grid into view.
  if (seeAll) {
    seeAll.addEventListener('click', function (event) {
      event.preventDefault();
      resetFilters();

      if (grid.scrollIntoView) grid.scrollIntoView({ behavior: 'smooth', block: 'start' });
    });
  }

  apply();
})();
