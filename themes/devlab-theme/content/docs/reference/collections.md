+++
title = "Collections and inheritance"
description = "Register reading roots and override their options."
weight = 3
updated = 2026-10-06
+++

## Register a collection

```toml
[extra.devlab.collections.manual]
section = "manual/_index.md"
title = "User manual"
show_toc = true
show_breadcrumbs = true
show_pagination = true

[extra.devlab.collections.api]
section = "api/_index.md"
title = "API reference"
show_toc = false

[extra.devlab.navigation]
links = [
  { name = "Home", path = "/" },
  { name = "Manual", collection = "manual" },
  { name = "API", collection = "api" },
]
```

Each root is an ordinary Zola section with `template = "docs.html"` and
`page_template = "doc-page.html"`. The theme derives its URL from that section.
Do not duplicate it in `path`. Source paths are relative to `content/`, without
the `@/` prefix. A missing primary-language root or unknown navigation collection
fails the build. A missing translated root removes that language's link.

The active root owns the sidebar, mobile tree, breadcrumbs, overview and
previous/next links. When collections nest, the most specific root wins.
Curated navigation entries must stay inside their collection’s source root.
Foreign, duplicate and overlapping entries fail the build. Add cross-collection links to global navigation
or authored content instead.
Generic `page.html` and `section.html` retain their own layout inside a collection.

Text dividers use `{ label = "Applications" }` records in `navigation`. An optional
`translations = { de = "Anwendungen" }` map localizes the label. These records
appear only in the sidebar and mobile Docs menu. They do not create pages or
change reading order. See [text dividers](@/docs/guides/navigation.md#add-text-dividers)
for a complete example.

## Override locally

The precedence is theme defaults → site `devlab.docs` defaults → collection →
ancestor sections inside that collection → current section or page.

In a section or page's front matter:

```toml
[extra.devlab]
show_toc = false
show_pagination = false

[extra.devlab.page_actions]
show_copy_link = true
```

Most fields replace earlier values. `page_actions` merges its individual fields,
so overriding `show_copy_link` preserves an inherited `edit_url`. Arrays such as
`navigation` replace the entire inherited array. An empty array returns to
automatic tree navigation. `false` and empty strings are intentional overrides.

## Configuration fields

| Field | Default | Meaning |
| --- | --- | --- |
| `section` | Required for named collections | Zola root section source. |
| `title` | Root section title for named collections | Sidebar name. |
| `show_toc`, `show_breadcrumbs`, `show_pagination` | `true` | Reading controls. |
| `navigation` | Automatic content tree | Ordered section records and non-linking text labels; see [navigation](@/docs/guides/navigation.md). |
| `page_actions` | Empty | Copy link, edit/report URLs, last updated and date format; see [configuration](@/docs/reference/configuration/reading.md#page-provenance-actions). |

`[extra.devlab.docs]` holds shared reading defaults only. Register a source root
under `devlab.collections.<name>` and use `collection = "name"` in navigation.
