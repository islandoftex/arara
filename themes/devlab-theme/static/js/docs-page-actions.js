(function () {
  // Looked up by attribute because document.currentScript is null in deferred scripts.
  var script = document.querySelector('script[data-link-copied-label]');
  var button = document.querySelector('[data-docs-copy-link]');

  if (!button || !navigator.clipboard || typeof navigator.clipboard.writeText !== 'function') return;

  var label = button.querySelector('[data-docs-copy-link-label]');
  var status = document.querySelector('[data-docs-copy-link-status]');
  var copyLabel = label ? label.textContent : 'Copy link';
  var copiedLabel = (script && script.dataset.linkCopiedLabel) || 'Link copied';
  var failedLabel = (script && script.dataset.copyLinkFailedLabel) || 'Failed';
  var successMessage = (script && script.dataset.copyLinkSuccessMessage) || 'Page link copied to clipboard.';
  var errorMessage = (script && script.dataset.copyLinkErrorMessage) || 'Unable to copy page link.';
  var resetTimer = null;
  var announcementTimer = null;
  var copyRequest = 0;

  function setButtonState(buttonLabel, state) {
    if (label) label.textContent = buttonLabel;
    button.dataset.copyState = state;
    button.setAttribute('aria-label', buttonLabel);
    button.title = buttonLabel;
  }

  function showFeedback(buttonLabel, announcement, state) {
    window.clearTimeout(resetTimer);
    window.clearTimeout(announcementTimer);

    setButtonState(buttonLabel, state);
    if (status) {
      status.textContent = '';
      announcementTimer = window.setTimeout(function () {
        status.textContent = announcement;
      }, 20);
    }

    resetTimer = window.setTimeout(function () {
      setButtonState(copyLabel, 'idle');
    }, 2000);
  }

  setButtonState(copyLabel, 'idle');
  button.hidden = false;

  button.addEventListener('click', function () {
    var request = ++copyRequest;

    navigator.clipboard.writeText(window.location.href).then(function () {
      if (request !== copyRequest) return;
      showFeedback(copiedLabel, successMessage, 'success');
    }).catch(function () {
      if (request !== copyRequest) return;
      showFeedback(failedLabel, errorMessage, 'error');
    });
  });
})();
