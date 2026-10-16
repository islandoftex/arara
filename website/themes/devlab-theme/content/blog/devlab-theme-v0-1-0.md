+++
title = "DevLab Theme v0.1.0"
description = "The first public preview release of DevLab Theme."
date = 2026-06-30

[taxonomies]
tags = ["release"]

[extra]
release = "v0.1.0"
+++

This article describes its original release. Configuration examples may use
retired settings. For v0.9, follow the [upgrade guide](@/docs/guides/upgrading-v0-8.md)
and the current reference.

DevLab Theme `v0.1.0` is the first public preview release of a developer-focused Zola theme for documentation sites, technical blogs and project pages.

## Highlights

- Documentation layout with sidebar, table of contents, breadcrumbs and previous/next navigation
- Blog layout
- Client-side search
- Light, dark and system color modes
- SEO metadata
- Atom feed autodiscovery
- SVG favicon
- Custom 404 page
- Configurable homepage
- Callout and card shortcodes
- Code block headers with copy buttons
- Mobile navigation
- Optional docs and blog sections for clean external Zola sites

## Requirements

- Zola `0.22.0` or newer

## Installation

Install the theme as a Git submodule:

```sh
git submodule add https://codeberg.org/RiPetitor/devlab-theme themes/devlab-theme
git submodule update --init --recursive
```

Enable it in your Zola config:

```toml
theme = "devlab-theme"
```

## Notes

This is an early public release. The theme is suitable for testing, personal documentation sites and small technical projects.

Planned future work includes additional shortcodes, tags and taxonomies, Mermaid, KaTeX, multilingual support and CI checks.
