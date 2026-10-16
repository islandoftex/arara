+++
title = "DevLab Theme v0.3.0"
description = "Nested documentation navigation, regular page layouts, reusable download cards and documented template overrides."
date = 2026-07-18
updated = 2026-10-06

[taxonomies]
tags = ["release", "documentation"]

[extra]
release = "v0.3.0"
+++

This article describes its original release. Configuration examples may use
retired settings. For v0.9, follow the [upgrade guide](@/docs/guides/upgrading-v0-8.md)
and the current reference.

DevLab Theme `v0.3.0` replaces the demo's flat documentation list with nested
section navigation. It also adds regular page and section layouts, makes
download cards reusable, and documents how site overrides survive theme updates.

## Documentation that keeps its shape

The Demo documentation now uses the recursive Zola section tree already supported by DevLab instead of presenting its guides as one flat list. Nested groups start closed for a first-time visitor, remember the reader's choices locally and share that state between the desktop sidebar and mobile Docs panel. The current page keeps its active state without forcing a reader's closed group back open.

`On this page` follows the same idea. Third-level headings sit below their second-level parent, and scrolling opens the branch that contains the active heading without turning the parent into a non-clickable control.

The compact footer now belongs at the bottom of Docs navigation, where it remains available without interrupting the article. Breadcrumbs and previous/next links are still rendered by Zola, so essential reading paths do not depend on JavaScript. The complete behavior is described in [Docs navigation](@/docs/guides/navigation.md).

## Room for the rest of the project

Not every page is documentation or a release post. DevLab now includes regular `page.html` and `section.html` layouts for project pages, directories and small collections. Generic sections can show their Markdown introduction, child sections, direct pages and pagination without borrowing a Docs or Blog template. See [Content layouts](@/docs/guides/content-layouts.md) for the content model.

Downloads has also grown from a Demo-specific page into a reusable distribution layout. A project can publish any number of release channels, leave planned artifacts unavailable, add a release summary and link existing checksums or signatures. The front matter contract remains documented in [Download cards](@/docs/guides/download-cards.md).

The header mark is now owned by the site. A new installation starts with a text-only brand, while projects that want a mark can use the built-in triangle or provide their own image.

## Fewer hidden rules

The documentation is now grouped into Getting started, Guides, Customization and Reference. It explains pinned installation and updates, every supported configuration field, CSS tokens, template extension hooks and the maintenance cost of replacing a partial. The goal is to keep project-owned files outside `themes/devlab-theme/`, where an update cannot silently overwrite them. Start with the [Customization overview](@/docs/customization/_index.md).

README and the project screenshot now show the same interface that the documentation describes, rather than an earlier flat-navigation Demo.

## Upgrading from v0.2.1

The structured global configuration under `extra.devlab.*` remains valid. Most sites can keep their navigation, Docs, Blog, search, appearance, SEO and homepage settings unchanged.

There are three deliberate changes worth checking before switching the pinned tag.

### Move Downloads front matter

The earlier flat Downloads fields are no longer read. Release data now belongs to the section-scoped `extra.downloads` tables:

| `v0.2.1` section field | `v0.3.0` section field |
| --- | --- |
| `extra.status_label` | `extra.downloads.eyebrow` and `extra.downloads.status.label` |
| `extra.channels_label` | `extra.downloads.channels_label` |
| `extra.download_unavailable_label` | `extra.downloads.unavailable_label` |
| `extra.iso_channels` | `extra.downloads.channels` |
| `extra.status_title`, `extra.status_description`, `extra.status_items` | `extra.downloads.status.title`, `extra.downloads.status.description`, `extra.downloads.status.items` |
| `extra.checksum_label`, `extra.checksum_title`, `extra.checksum_description` | `extra.downloads.verification.label`, `extra.downloads.verification.title`, `extra.downloads.verification.description` |

Channel fields such as `name`, `status`, `description`, `url` and `button_label` keep their meaning. Verification links are new in `v0.3.0`. Status and verification blocks render only when they have a non-empty title, so incomplete data no longer leaves an empty placeholder.

No compatibility aliases are retained for the old Downloads fields. Move the values before updating the theme.

### Choose the header mark explicitly

The built-in triangle is no longer shown by default. Add this if the site should keep the previous DevLab mark:

```toml
[extra.devlab.brand]
show_logo_mark = true
logo_mark_path = ""
```

Set `logo_mark_path` to a site-owned image for a custom mark, or leave `show_logo_mark = false` for the new text-only default.

### Review site-owned overrides

On Docs routes, `partials/footer.html` is now rendered inside the desktop sidebar and mobile Docs panel with `footer_compact = true`, rather than below the article. A site-level footer replacement should support that compact context.

Also review copied layouts, partials, CSS or JavaScript that depend on the previous Downloads markup, Blog pagination classes, Docs navigation or TOC structure. These implementation details changed even though their public behavior remains familiar.

The Demo documentation itself moved from a flat list into nested sections without redirects. Saved links to Quick start, Guides or Reference pages may need updating. Consumer sites keep their own content routes and are not required to copy the Demo hierarchy.

## Before publishing the update

1. Read the changes above and keep project customizations outside the theme directory.
2. Update the pinned theme tag using [Install and update](@/docs/getting-started/installation.md).
3. Run `zola check` and `zola build`.
4. Check the routes the site actually enables: Home, Docs, Blog and Downloads.
5. On Docs, verify nested groups, remembered state, `On this page`, the mobile panel and the compact footer.
6. Check search, published and unavailable download actions, and both color modes.

Sites upgrading from a version older than `v0.2.1` should first follow the structured configuration migration in the [v0.2.1 release notes](@/blog/devlab-theme-v0-2-1.md).

## Compatibility

DevLab Theme `v0.3.0` requires Zola `0.22.1` or newer. Building a site requires no
Node.js pipeline or external content service.
