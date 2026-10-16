(function () {
  // Looked up by attribute because document.currentScript is null in deferred scripts.
  var script = document.querySelector('script[data-copy-label]');
  var copyLabel = (script && script.dataset.copyLabel) || 'Copy';
  var copiedLabel = (script && script.dataset.copiedLabel) || 'Copied';
  var copyFailedLabel = (script && script.dataset.copyFailedLabel) || 'Failed';
  var copySuccessMessage = (script && script.dataset.copySuccessMessage) || 'Code copied to clipboard.';
  var copyErrorMessage = (script && script.dataset.copyErrorMessage) || 'Unable to copy code.';
  var overflowObserver = typeof window.ResizeObserver === 'function'
    ? new window.ResizeObserver(function (entries) {
      entries.forEach(function (entry) {
        updateOverflowTabStop(entry.target);
      });
    })
    : null;

  function updateOverflowTabStop(pre) {
    if (pre.scrollWidth > pre.clientWidth + 1) {
      pre.tabIndex = 0;
      return;
    }

    pre.removeAttribute('tabindex');
  }

  function observeOverflow(pre) {
    window.requestAnimationFrame(function () {
      updateOverflowTabStop(pre);
    });

    if (overflowObserver) {
      overflowObserver.observe(pre);
    }
  }

  function extractLanguage(value, allowRaw) {
    if (!value) return '';

    var source = String(value).trim();
    var match = source.match(/language-([A-Za-z0-9_+-]+)/);

    if (match) return match[1].toLowerCase();

    return allowRaw ? source.toLowerCase() : '';
  }

  function getCodeLanguage(pre, code) {
    var dataLanguage = extractLanguage(pre.getAttribute('data-lang') || code.getAttribute('data-lang'), true);

    if (dataLanguage) return dataLanguage;

    var classLanguage = extractLanguage(code.className || pre.className, false);

    if (classLanguage) return classLanguage;

    return 'text';
  }

  // Zola reports the giallo syntax name, so ``bash`` arrives as ``shellscript``
  // and ``text`` as ``plain``. Map the common names to readable labels and
  // title-case anything unmapped instead of shouting the raw value.
  var languageLabels = {
    bash: 'Bash',
    c: 'C',
    console: 'Console',
    cpp: 'C++',
    csharp: 'C#',
    css: 'CSS',
    diff: 'Diff',
    dockerfile: 'Dockerfile',
    go: 'Go',
    golang: 'Go',
    html: 'HTML',
    ini: 'INI',
    java: 'Java',
    javascript: 'JavaScript',
    jinja: 'Jinja',
    jinja2: 'Jinja',
    json: 'JSON',
    js: 'JavaScript',
    kotlin: 'Kotlin',
    lua: 'Lua',
    makefile: 'Makefile',
    markdown: 'Markdown',
    md: 'Markdown',
    nginx: 'Nginx',
    php: 'PHP',
    plain: 'Text',
    powershell: 'PowerShell',
    ps1: 'PowerShell',
    py: 'Python',
    python: 'Python',
    ruby: 'Ruby',
    rust: 'Rust',
    scss: 'SCSS',
    sh: 'Shell',
    shell: 'Shell',
    shellscript: 'Shell',
    sql: 'SQL',
    text: 'Text',
    toml: 'TOML',
    ts: 'TypeScript',
    typescript: 'TypeScript',
    vim: 'Vim',
    xml: 'XML',
    yaml: 'YAML',
    yml: 'YAML'
  };

  function getLanguageLabel(languageName) {
    var key = String(languageName || '').toLowerCase();

    if (Object.prototype.hasOwnProperty.call(languageLabels, key)) {
      return languageLabels[key];
    }

    return key ? key.charAt(0).toUpperCase() + key.slice(1) : 'Text';
  }

  function getCodeName(pre, code) {
    var value = pre.getAttribute('data-name') || code.getAttribute('data-name') || '';

    return String(value).trim();
  }

  function createCodeBlock(pre, code) {
    var wrapper = document.createElement('div');
    wrapper.className = 'code-block';

    var header = document.createElement('div');
    header.className = 'code-block-header';

    var meta = document.createElement('span');
    meta.className = 'code-block-meta';

    // Zola puts the fenced block's ``name=`` annotation on the code element.
    // Show it as the block title and demote the language to a small chip, so a
    // captioned block reads like ``board.env · Shell`` instead of only ``Shell``.
    var codeName = getCodeName(pre, code);
    if (codeName) {
      var title = document.createElement('span');
      title.className = 'code-block-title';
      title.setAttribute('dir', 'ltr');
      title.setAttribute('title', codeName);
      title.textContent = codeName;
      meta.appendChild(title);
      meta.classList.add('is-titled');
      wrapper.dataset.name = codeName;
    }

    var language = document.createElement('span');
    language.className = 'code-block-language';
    var languageName = getCodeLanguage(pre, code);
    language.textContent = getLanguageLabel(languageName);
    wrapper.dataset.language = languageName;
    observeOverflow(pre);

    meta.appendChild(language);
    header.appendChild(meta);

    pre.parentNode.insertBefore(wrapper, pre);
    wrapper.appendChild(header);
    wrapper.appendChild(pre);

    return header;
  }

  function createFeedbackController(button, label, status) {
    var resetTimer = null;
    var announcementTimer = null;

    function setButtonLabel(value) {
      label.textContent = value;
      button.setAttribute('aria-label', value);
      button.title = value;
    }

    return {
      clearReset: function () {
        window.clearTimeout(resetTimer);
        setButtonLabel(copyLabel);
        button.dataset.copyState = 'idle';
      },

      show: function (buttonLabel, announcement, state) {
        window.clearTimeout(resetTimer);
        window.clearTimeout(announcementTimer);

        setButtonLabel(buttonLabel);
        button.dataset.copyState = state;
        status.textContent = '';

        announcementTimer = window.setTimeout(function () {
          status.textContent = announcement;
        }, 20);

        resetTimer = window.setTimeout(function () {
          setButtonLabel(copyLabel);
          button.dataset.copyState = 'idle';
        }, 2400);
      }
    };
  }

  function getCopyText(code) {
    var clone = code.cloneNode(true);
    clone.querySelectorAll('.giallo-ln').forEach(function (node) {
      node.remove();
    });
    return clone.textContent;
  }

  function createCopyControl(header, code) {
    if (!navigator.clipboard || typeof navigator.clipboard.writeText !== 'function') return;

    var button = document.createElement('button');
    button.className = 'code-copy-button';
    button.type = 'button';
    button.dataset.copyState = 'idle';

    var icon = document.createElement('span');
    icon.className = 'code-copy-icon';
    icon.setAttribute('aria-hidden', 'true');
    button.appendChild(icon);

    var label = document.createElement('span');
    label.className = 'code-copy-label';
    label.textContent = copyLabel;
    button.appendChild(label);
    button.setAttribute('aria-label', copyLabel);
    button.title = copyLabel;
    header.appendChild(button);

    var status = document.createElement('span');
    status.className = 'code-copy-status';
    status.setAttribute('role', 'status');
    status.setAttribute('aria-live', 'polite');
    status.setAttribute('aria-atomic', 'true');
    header.appendChild(status);

    var feedback = createFeedbackController(button, label, status);
    var copyRequest = 0;

    button.addEventListener('click', function () {
      var request = ++copyRequest;
      feedback.clearReset();

      navigator.clipboard.writeText(getCopyText(code)).then(function () {
        if (request !== copyRequest) return;
        feedback.show(copiedLabel, copySuccessMessage, 'success');
      }).catch(function () {
        if (request !== copyRequest) return;
        feedback.show(copyFailedLabel, copyErrorMessage, 'error');
      });
    });
  }

  document.querySelectorAll('.content pre').forEach(function (pre) {
    if (pre.parentNode && pre.parentNode.classList && pre.parentNode.classList.contains('code-block')) return;

    var code = pre.querySelector('code');
    if (!code) return;

    var header = createCodeBlock(pre, code);
    createCopyControl(header, code);
  });
})();
