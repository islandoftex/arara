(function () {
  const announcement = document.querySelector("[data-announcement]");
  const dismissButton = document.querySelector("[data-announcement-dismiss]");

  if (!announcement || !dismissButton) {
    return;
  }

  const root = document.documentElement;
  const storageKey = "devlab-announcement-dismissed";
  const announcementKey = announcement.dataset.announcementKey || "";

  function storeDismissedAnnouncement() {
    if (!announcementKey) {
      return;
    }

    try {
      localStorage.setItem(storageKey, announcementKey);
    } catch (_) {
      // The control still works for this page when persistent storage is unavailable.
    }
  }

  dismissButton.addEventListener("click", function () {
    storeDismissedAnnouncement();
    root.dataset.announcementDismissed = "true";
    announcement.hidden = true;
  });
})();
