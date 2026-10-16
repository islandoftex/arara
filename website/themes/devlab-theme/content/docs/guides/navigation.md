+++
title = "Docs navigation"
description = "Select top-level documentation sections and derive their descendants from the Zola content tree."
weight = 3
updated = 2026-10-06
+++

DevLab connects documentation to the section configured by `extra.devlab.collections.<name>.section`. An optional `extra.devlab.collections.<name>.navigation` manifest orders top-level sections and text labels. The selected sections are shared by the desktop sidebar, mobile Docs panel and root Docs catalog. Descendants inside each selected section continue to come from the Zola content tree.

Breadcrumbs keep following the real content hierarchy. Previous and next links use the curated manifest's scope and top-level order when it is present, or the complete Docs tree when it is omitted. The theme demo uses `content/docs/`. Your site can use any content directory.

## Build the content tree

Use pages for regular links and directories with `_index.md` for expandable groups:

```txt
content/docs/
├── _index.md
├── getting-started/
│   ├── _index.md
│   ├── installation.md
│   └── quick-start.md
├── guides/
│   ├── _index.md
│   └── navigation.md
├── customization/
│   ├── _index.md
│   └── branding.md
└── reference/
    ├── _index.md
    └── configuration/
        └── _index.md
```

Each subsection title is a link to that section's authored landing page. When the subsection has children, a separate chevron button expands them in the sidebar and mobile Docs menu. Child subsections are nested automatically.

DevLab does not generate synthetic `Overview` items. The global Docs link opens the root landing page, and every subsection link opens its own landing page. If an information architecture needs a visible item named `Overview`, add it as a normal page or section with that title.

## Configure the Docs root

Connect the source section and navigation entry in `zola.toml`:

```toml
[extra.devlab.collections.manual]
section = "docs/_index.md"
navigation = [
  { section = "docs/getting-started/_index.md" },
  { section = "docs/guides/_index.md" },
  { section = "docs/customization/_index.md" },
  { section = "docs/reference/_index.md" },
]

[extra.devlab.navigation]
links = [
  { name = "Docs", collection = "manual" },
]
```

Merge the Docs entry into the site's complete `links` array. The settings have different roles:

- `section` identifies the source root relative to `content/`.
- `collection` references that root from global navigation.
- `navigation` selects and orders its top-level documentation sections.

The public URL comes from the root's actual permalink. On Docs/Reader layouts,
the mobile menu initially opens the active collection. Other layouts start at
global navigation. Landing actions have explicit `label` and `path` fields.

## Select the top-level sections

A section entry in `navigation` requires one `section` source path relative to `content/`. The path must identify a section `_index.md`, not its public URL:

```toml
[extra.devlab.collections.manual]
section = "docs/_index.md"
navigation = [
  { section = "docs/users/_index.md" },
  { section = "docs/packaging/_index.md" },
  { section = "docs/developers/_index.md" },
]
```

The manifest order is the rendered top-level order and the top-level order used by previous and next links. DevLab takes each entry's title, description and link from the target section. Its pages and child sections are discovered automatically, so the manifest does not repeat the complete tree.

Manifest entries must belong to the collection. Missing source paths, foreign
sections, duplicate entries and overlapping branches stop the build. Select a
branch or its descendants. Listing both would repeat their content.

If `navigation` is omitted or empty, DevLab discovers top-level navigation and previous or next targets automatically from the configured Docs root. Omitting one section from a non-empty manifest hides that generated top-level entry and excludes its pages from previous and next links. It does not unpublish the section or its pages.

`extra.devlab.collections.<name>.navigation` is separate from `extra.devlab.navigation.links`: the first structures documentation, while the second adds global header and mobile-menu destinations.

## Add text dividers

Insert a `label` record before the sections it introduces. This gives one sidebar separate areas such as documentation and applications without creating extra content pages:

```toml
[extra.devlab.collections.manual]
section = "docs/_index.md"
navigation = [
  { section = "docs/getting-started/_index.md" },
  { section = "docs/guides/_index.md" },
  { label = "Applications", translations = { de = "Anwendungen", ru = "Приложения" } },
  { section = "docs/apps/_index.md" },
  { label = "Methods", translations = { de = "Arbeitsweisen", ru = "Методы работы" } },
  { section = "docs/methods/_index.md" },
]
```

Create the listed sections in your site before using these source paths. A label is plain text, styled like the sidebar's collection title. It has no link, toggle or keyboard focus and appears in both the desktop sidebar and mobile Docs menu. The collection title remains at the top.

`label` must be a non-empty string. It cannot share a record with `section` or `path`. The optional `translations` map selects the text for the active language and falls back to `label` when that language has no entry. Author these labels alongside the navigation. They are not interface catalog keys.

A label introduces subsequent section entries until the next label. It is shown only when at least one of those sections exists in the active language. Empty groups, consecutive unused labels and a trailing label render nothing.

Labels do not become cards on the root Docs page or targets for previous and next links. The selected source sections still control the catalog and reading order. Reader layouts use their separate `reading_order` setting.

## Control nesting and order

Set `transparent = true` in a subsection when its pages should belong to the parent navigation level. DevLab follows Zola's transparent-section behavior: the subsection wrapper is hidden, its pages appear once under the effective parent, and nested non-transparent sections remain visible as groups.

The explicit manifest controls only the top-level section order. Set `sort_by = "weight"` in each section's `_index.md` and add `weight` to its pages when you need a custom descendant order. Give sibling subsections unique `weight` values as well, because Zola uses section weight to order them.

The root Docs catalog renders one card for each selected top-level section, without copying its descendants onto the root page. A section landing renders its direct pages and direct child sections as cards. A child section exposes its own direct entries on its landing page. This one-level-at-a-time structure keeps deep pages out of higher-level catalogs.

The card grid chooses the number of columns from the available content width and returns to one column on small screens. Source order remains the keyboard and screen-reader order. On the root Docs page, the authored introduction is rendered before the catalog so readers get context before choosing a section.

The generated HTML keeps every child link visible and hides disclosure buttons, so the complete navigation remains usable without JavaScript. When JavaScript loads, inactive branches collapse and the buttons become available. The branch containing the current page remains open, including when that page
sets a custom `path`. Branch selection follows source ancestry.

Opening or closing a branch saves that choice under `devlab-docs-nav-state-v1` in the browser's `localStorage`, and the same state is reused by the desktop sidebar and mobile Docs menu. Saved open groups remain open across page changes. The active branch always reopens on a direct page load, even when an earlier saved state closed it. Clearing site data resets non-active groups to closed after JavaScript initializes.

## Follow the same tree

Breadcrumbs always use the real section hierarchy, for example `Documentation / Guides / Docs navigation`, independently of the curated manifest.

With a non-empty curated manifest, previous and next links include only pages below the listed sections. They follow manifest order between top-level sections and depth-first content-tree order inside each section: direct pages first, then subsection pages. Without a manifest, they use the complete configured Docs root in the same depth-first order.
They are rendered by Zola, so this essential navigation remains available when JavaScript is disabled.

The `On this page` panel groups third-level headings below their second-level parent with native disclosure controls. Groups start closed in the generated HTML. As the reader moves through the page, scroll tracking closes inactive groups and opens the branch that contains the active heading. The parent title remains a regular anchor link.

On Docs pages, the compact site footer sits at the bottom of the desktop sidebar and the mobile Docs panel. Other layouts keep the footer below the main page content.
