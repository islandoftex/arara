+++
title = "Release announcements"
description = "Publish a header notice and control its saved dismissal."
weight = 12
updated = 2026-10-06
+++

## Add a notice

Publish the destination page first, then add the announcement to `zola.toml`.
This example assumes a post exists at `/blog/acme-1-0/`. See
[Blog settings](@/docs/reference/configuration/reading.md#optional-blog) to create one.

```toml
[extra.devlab.announcement]
show = true
key = "acme-release-1.0"
label = "Release"
message = "Acme 1.0 is available"
path = "/blog/acme-1-0/"
dismissible = true
```

The banner appears across layouts using the base template. `message` and `label`
are plain text. Set `path = ""` for a notice without a link. Local paths include
the deployment prefix and active language. Create that destination in each
language that displays the notice.

The optional label disappears below 421px. Long messages are shortened, so put
release details in the destination page.

## Dismissal and updates

A close button requires `dismissible = true` and a non-empty `key`.
The browser stores the latest dismissed key. Keep it unchanged for wording
fixes. Change it with the message and destination for a new announcement.

`show = false` removes the notice without clearing the saved dismissal.
`dismissible = false` shows an enabled notice even if its key was dismissed.
An empty message removes the banner.

The saved preference belongs to the browser and origin. Languages and sites
under different subpaths of the same origin share it, so use a project-specific
key. Clearing site data resets it. Without storage, dismissal lasts for the
current page. Without JavaScript, the notice and link remain but closing is unavailable.

## Check the result

Run `zola check --skip-external-links` and `zola serve`. Follow the link, close
the notice and reload. For a previously dismissed notice, preview with a
temporary key and restore the publication key afterward.

If the close button is absent, check `dismissible` and `key`. If the notice
returns after navigation, check that the key is stable and browser storage is
available. A link to a missing page requires fixing its destination, including
translated routes.

Accessible names use `announcement` and `announcement_dismiss` in the
[interface catalog](@/docs/reference/languages.md#interface-catalog).
[Appearance settings](@/docs/reference/configuration/appearance.md#announcements)
lists the field types and defaults.
