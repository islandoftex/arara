+++
title = "DevLab Theme v0.3.1"
description = "A documentation-focused release with clearer section landings, curated navigation, stronger layout boundaries and a configurable footer."
date = 2026-07-23

[taxonomies]
tags = ["release", "documentation"]

[extra]
release = "v0.3.1"
+++

This article describes its original release. Configuration examples may use
retired settings. For v0.9, follow the [upgrade guide](@/docs/guides/upgrading-v0-8.md)
and the current reference.

DevLab Theme `v0.3.1` is a focused follow-up to `v0.3.0`. Most of the work happened inside Docs: section landings are easier to scan, navigation controls have one clear purpose and a project can choose which documentation sectors belong in the primary interface.

The release also tightens the surrounding layout and makes the footer a real customization surface without changing the Zola-native build.

## Documentation without synthetic waypoints

The global Docs link already opens the documentation landing page, and each section title already has an authored landing of its own. DevLab no longer inserts synthetic links named `Overview` between those destinations.

Section titles are now ordinary landing links. A separate chevron button expands or collapses descendants when they exist. The split keeps navigation concise and gives each control one job: the title navigates, while the chevron discloses.

Without JavaScript, every descendant remains visible and every landing remains reachable. After initialization, inactive branches collapse, saved open branches keep their state and the branch containing the current page opens so its active link stays in view.

## Curate the sectors, derive the tree

`extra.devlab.docs.navigation` is a new optional manifest for selecting and ordering the top-level sectors shared by the desktop sidebar, mobile Docs panel and root catalog:

```toml
[extra.devlab.docs]
section = "docs/_index.md"
path = "/docs/"
navigation = [
  { section = "docs/getting-started/_index.md" },
  { section = "docs/guides/_index.md" },
  { section = "docs/reference/_index.md" },
]
```

The manifest names only the top level. Titles, descriptions, links, pages and child sections still come from Zola content, so authors do not maintain a second copy of the complete tree. Omitting the manifest or leaving it empty preserves automatic top-level discovery.

Landing pages now reveal the hierarchy one level at a time. The Docs root presents the selected sectors, while a sector presents its direct pages and direct child sections as compact cards. Deeper entries stay on their own parent landing instead of flooding every higher-level page. The root introduction remains before its catalog, and authored text after a card group now has enough space to read as a new block.

See [Docs navigation](@/docs/guides/navigation.md) for the complete content and configuration contract.

## A stricter visual frame

The header, Docs sidebar and table of contents now use one quiet border color to define their areas consistently. The header separator is inset, so the line does not change sticky offsets or add a hidden pixel to the layout.

Overview entries use smaller responsive cards without decorative arrows. This keeps attention on titles and descriptions and brings dense section pages closer to the restrained visual language used by Hextra without copying its Hugo-specific structure.

The footer follows the same system. Its copyright stays geometrically centered, while an optional note and project links can occupy the side regions. Narrow screens and compact Docs contexts stack the same content into a centered column.

```toml
[extra.devlab.footer]
note = "Documentation for the Acme platform."
links = [
  { name = "Codeberg", path = "https://codeberg.org/acme/docs", icon = "codeberg" },
  { name = "Atom feed", path = "/atom.xml", icon = "rss" },
]
```

For markup beyond the structured fields, a site can replace only `partials/footer-content.html`. The theme keeps ownership of the separator, responsive layout and compact mode. The available fields and icon names are documented in [Brand and colors](@/docs/customization/branding.md).

## Smaller reliability improvements

- Search reports an unavailable state when its library or generated index cannot load, instead of leaving an unhandled failure.
- Generic taxonomy list and term routes now use complete theme layouts.
- Blog dates accept `extra.devlab.blog.date_format`, with the existing day-first display retained as the default.
- Docs and navigation scripts load only when their rendered interfaces need them.

## Upgrading from v0.3.0

Most sites can update without moving content or changing their existing `extra.devlab.*` configuration. The new Docs manifest, footer fields and Blog date format are opt-in.

One interface contract changed deliberately: `docs_overview_label` is no longer read because generated `Overview` links no longer exist. An older configuration can leave the field in place without breaking the build, but it has no effect. Create a real page or section titled `Overview` when that destination belongs in the information architecture.

Review site-owned CSS, copied templates or JavaScript that targets the previous Docs navigation or overview markup. A full `partials/footer.html` override still works and must continue to handle `footer_compact`. New customizations should prefer the smaller `partials/footer-content.html` boundary.

Before updating a pinned installation:

1. Read the changes above and review site-owned Docs or footer overrides.
2. Select `v0.3.1` using [Install and update](@/docs/getting-started/installation.md).
3. Run `zola check` and `zola build`.
4. Check the Docs root, one section landing and one deep page at desktop and mobile widths.
5. Verify search, the mobile menu, the footer and both color modes.

## Compatibility

DevLab Theme `v0.3.1` requires Zola `0.22.1` or newer. It remains Zola-native and does not require Node.js, npm or an external frontend runtime.
