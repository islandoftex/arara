+++
title = "Appearance and announcements"
description = "Color modes, branding, footer links and announcements."
weight = 3
updated = 2026-10-06
+++

## Color mode and brand

```toml
[extra.devlab.appearance]
default_mode = "system"
show_toggle = true

[extra.devlab.brand]
logo_text = "Acme"
footer_text = "Acme"
show_logo_mark = false
logo_mark_path = ""
```

`default_mode` accepts `system`, `light` or `dark`. An unrecognized value uses
`system`. The reader's saved choice takes precedence. Set `show_toggle = false`
to remove the control. [Branding](@/docs/customization/branding.md) lists CSS
tokens and the stylesheet hook for palette changes.

## Footer

```toml
[extra.devlab.footer]
show = true
note = "Maintained by Acme."
links_label = "Project links"
show_link_names = false
links = [{ name = "Source", path = "https://codeberg.org/acme/project", icon = "codeberg" }]
```

`show = false` removes the built-in footer, including its compact reading variant.
`note` is escaped plain text. Each link requires `name` and `path`. Supported
`icon` values are `github`, `gitlab`, `codeberg`, `matrix`, `zulip`, `telegram`,
`discord`, `rss`, `email` and `link`. A supported icon supplies the accessible
name from `name`. Unknown or omitted icons render visible text.

`show_link_names = true` adds visible names and a `links_label` heading on the
regular footer. Compact footers in the reading sidebar/mobile panel always use
icons only, below note and copyright. `links_label` also names the link group
for assistive technology. Empty settings leave the copyright-only footer.

These options apply to the built-in footer. A site override owns its own markup.
See [Template extensions](@/docs/customization/templates.md).

## Announcements

`[extra.devlab.announcement]` is global site configuration. See [Release announcements](@/docs/guides/announcements.md) for the publishing workflow.

`show` and `dismissible` are booleans. `key`, `label`, `message` and `path` are strings. The banner renders only when `show = true` and `message` is non-empty. An empty `path` renders text without a link, and an empty `label` omits the prefix. Both text fields are escaped plain text, without Markdown or HTML rendering. These fields are not read from page or section front matter.

The close button requires `dismissible = true` and a non-empty `key`. A matching saved key hides the banner when those conditions hold. Changing only `message`, `label` or `path` does not reset dismissal. A new key identifies a new announcement. `show = false` removes the banner without clearing the saved key. Setting `dismissible = false` shows the enabled message even if its key was previously dismissed.

DevLab stores the latest dismissed key in `localStorage` as `devlab-announcement-dismissed`. This value is shared across paths and languages on the same origin, is not synchronized between devices, and can be lost when site data is cleared. Without storage, dismissal lasts for the current page. Without JavaScript, the banner and link render but closing does not work. Already open tabs apply a saved dismissal on their next load.

`path` uses the [active-language route resolver](@/docs/reference/configuration/core.md#multilingual-routes). Ensure that a local destination exists in each active language. The announcement has no missing-translation fallback. The label is hidden at widths of 420px or less, and long messages are truncated on desktop or limited to two lines on mobile. The optional close script is loaded only when the banner renders with a close button.
