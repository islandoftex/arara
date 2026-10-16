(function () {
  var containers = Array.from(document.querySelectorAll('[data-content-tabs]'));

  if (!containers.length) return;

  var storagePrefix = 'devlab-tabs-selection-v1:';
  var states = [];

  function readSelection(sync) {
    if (!sync) return '';

    try {
      return localStorage.getItem(storagePrefix + sync) || '';
    } catch (error) {
      return '';
    }
  }

  function saveSelection(sync, value) {
    if (!sync) return;

    try {
      localStorage.setItem(storagePrefix + sync, value);
    } catch (error) {
      // Storage can be unavailable without affecting the current tab group.
    }
  }

  function panelValue(panel) {
    return panel.dataset.tabValue || panel.dataset.tabName;
  }

  function selectTab(state, index, moveFocus) {
    state.tabs.forEach(function (tab, tabIndex) {
      var isActive = tabIndex === index;

      tab.setAttribute('aria-selected', String(isActive));
      tab.tabIndex = isActive ? 0 : -1;
      state.panels[tabIndex].hidden = !isActive;
    });

    state.activeIndex = index;

    if (moveFocus) state.tabs[index].focus();
  }

  function matchingIndex(state, value) {
    return state.panels.findIndex(function (panel) {
      return panelValue(panel) === value;
    });
  }

  function activateTab(state, index, moveFocus) {
    var value = panelValue(state.panels[index]);

    selectTab(state, index, moveFocus);

    if (!state.sync) return;

    saveSelection(state.sync, value);

    states.forEach(function (otherState) {
      if (otherState === state || otherState.sync !== state.sync) return;

      var otherIndex = matchingIndex(otherState, value);
      if (otherIndex >= 0) selectTab(otherState, otherIndex, false);
    });
  }

  containers.forEach(function (container, groupIndex) {
    var panels = Array.from(container.children).filter(function (child) {
      return child.matches('[data-content-tab]');
    });

    if (panels.length < 2) return;

    var tablist = document.createElement('div');
    var tabs = [];
    var sync = container.dataset.tabsSync || '';
    var tablistLabel = container.dataset.tabsLabel || 'Tabs';

    tablist.className = 'content-tab-list';
    tablist.setAttribute('role', 'tablist');
    tablist.setAttribute('aria-label', tablistLabel);

    panels.forEach(function (panel, tabIndex) {
      var tab = document.createElement('button');
      var tabId = 'devlab-tabs-' + groupIndex + '-tab-' + tabIndex;
      var panelId = 'devlab-tabs-' + groupIndex + '-panel-' + tabIndex;
      var fallbackLabel = panel.querySelector('[data-tab-fallback-label]');

      tab.className = 'content-tab';
      tab.type = 'button';
      tab.id = tabId;
      tab.textContent = panel.dataset.tabName;
      tab.setAttribute('role', 'tab');
      tab.setAttribute('aria-controls', panelId);

      panel.id = panelId;
      panel.setAttribute('role', 'tabpanel');
      panel.setAttribute('aria-labelledby', tabId);
      panel.tabIndex = 0;

      if (fallbackLabel) fallbackLabel.hidden = true;

      tablist.appendChild(tab);
      tabs.push(tab);
    });

    container.insertBefore(tablist, panels[0]);
    container.classList.add('is-ready');

    var state = {
      activeIndex: 0,
      container: container,
      panels: panels,
      sync: sync,
      tabs: tabs
    };
    var storedSelection = readSelection(sync);
    var initialIndex = storedSelection ? matchingIndex(state, storedSelection) : -1;

    if (initialIndex < 0 && sync) {
      var synchronizedState = states.find(function (otherState) {
        return otherState.sync === sync;
      });

      if (synchronizedState) {
        initialIndex = matchingIndex(
          state,
          panelValue(synchronizedState.panels[synchronizedState.activeIndex])
        );
      }
    }

    if (initialIndex < 0) {
      initialIndex = panels.findIndex(function (panel) {
        return panel.dataset.tabSelected === 'true';
      });
    }

    if (initialIndex < 0) initialIndex = 0;

    states.push(state);
    selectTab(state, initialIndex, false);

    tabs.forEach(function (tab, tabIndex) {
      tab.addEventListener('click', function () {
        activateTab(state, tabIndex, false);
      });

      tab.addEventListener('keydown', function (event) {
        var nextIndex = state.activeIndex;
        var isRtl = getComputedStyle(container).direction === 'rtl';

        if (event.key === 'Home') {
          nextIndex = 0;
        } else if (event.key === 'End') {
          nextIndex = tabs.length - 1;
        } else if (event.key === 'ArrowRight') {
          nextIndex = (state.activeIndex + (isRtl ? -1 : 1) + tabs.length) % tabs.length;
        } else if (event.key === 'ArrowLeft') {
          nextIndex = (state.activeIndex + (isRtl ? 1 : -1) + tabs.length) % tabs.length;
        } else {
          return;
        }

        event.preventDefault();
        activateTab(state, nextIndex, true);
      });
    });
  });
})();
