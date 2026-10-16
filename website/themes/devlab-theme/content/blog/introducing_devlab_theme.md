+++
title = "DevLab Theme 0.0.1 release candidate"
description = "A first release candidate for a Zola theme focused on documentation, technical blogs and maintainable developer sites."
date = 2026-06-06

[taxonomies]
tags = ["release"]

[extra]
release = "v0.0.1-rc"
+++

DevLab Theme is a modern Zola theme for developers, documentation sites and technical blogs.

The `0.0.1` release candidate turns the project from a local demo into a small theme package that can be installed in a separate Zola site.

{% <devlab.callout type="info" title="Release candidate"> %}
This version is ready for final validation. Before tagging `v0.0.1`, the theme should be checked in this repository and installed once in a clean external Zola site.
{% </devlab.callout> %}

## What is included

The release candidate focuses on the workflows a documentation theme needs on day one:

- documentation layout with desktop sidebar and table of contents
- single mobile menu with a documentation panel
- grouped and collapsible docs navigation
- docs breadcrumbs and previous/next links
- client-side search
- light and dark color modes
- callout and card shortcodes
- code block headers with language labels and copy buttons
- theme metadata, MIT license and fallback `[extra]` defaults

## Install preview

Install the theme under a Zola site's `themes/` directory:

```bash
git clone https://codeberg.org/RiPetitor/devlab-theme themes/devlab-theme
```

Enable the theme and the optional search index:

```toml
theme = "devlab-theme"
compile_sass = true
build_search_index = true
```

The theme is designed to work without a frontend framework or build tool beyond Zola's built-in Sass and search index support.

## Validation before tagging

The final `0.0.1` tag should be cut after these checks:

- run `zola check`
- run `zola build`
- review light and dark modes
- review desktop and mobile documentation navigation
- review mobile menu open, close, backdrop click, Escape and link-click states
- review search results and empty search state
- review code block language labels and Copy button states
- install the repository as `themes/devlab-theme` in a clean external Zola site

## Next direction

After `0.0.1`, the theme can move toward more polished documentation ergonomics: richer code block metadata, more content components, better search presentation and a small set of real-world example pages.
