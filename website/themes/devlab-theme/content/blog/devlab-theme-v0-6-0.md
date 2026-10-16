+++
title = "DevLab Theme v0.6.0"
description = "Shared CSS tokens, a homepage image, revised reading layouts and a five-post blog example."
date = 2026-08-31
updated = 2026-10-06

[taxonomies]
tags = ["release", "design", "documentation"]

[extra]
release = "v0.6.0"
+++

This article describes its original release. Configuration examples may use
retired settings. For v0.9, follow the [upgrade guide](@/docs/guides/upgrading-v0-8.md)
and the current reference.

DevLab Theme `v0.6.0` gives the homepage, Docs, Blog and Downloads shared CSS
tokens for typography, spacing and colors. It adds an optional homepage image
and revises the layouts at desktop and mobile widths.

The release keeps DevLab Zola-native and dependency-free. It does not change the `devlab.*` content component syntax or require a content migration from `v0.5.0`.

## One visual foundation

Typography, spacing, radii, shadows, motion and surface roles now share a compact set of CSS custom properties. Components use the same scale for hierarchy, focus states and interaction feedback in light and dark modes.

Borders and shadows distinguish raised surfaces. Accent colors identify actions,
current navigation and status.

For update-safe customization, continue to use the public CSS tokens documented in [Brand and colors](@/docs/customization/branding.md) before overriding individual component selectors.

## Optional homepage image

The wide homepage now has a composed hero, stronger responsive spacing and an optional product preview. Configure the preview in homepage front matter:

```toml
[extra]
home_layout = "wide"
home_hero_image = "/images/project-docs-preview.png"
home_hero_image_alt = "Project documentation with sidebar navigation and an on-page table of contents."
```

When the image is omitted, the hero returns to a centered text composition. Feature summaries, the learn-more section, workflow and final call to action use the same layout rhythm while remaining independently optional.

Homepage configuration still works globally or in `content/_index.md`. Local values take priority, and an explicitly empty feature array can hide its corresponding card or point collection. See the [homepage configuration reference](@/docs/reference/landing.md) for the complete inheritance rules.

## Documentation built around reading

The Docs overview, sidebar, article hierarchy, table of contents and previous or next navigation now have clearer separation without turning every region into a card. Section identity stays visible, long-form content keeps a comfortable measure and navigation remains useful on smaller screens.

Breadcrumbs and adjacent-page links continue to follow the same language-aware documentation tree introduced in `v0.5.0`. This release changes their presentation, not their routing contract.

## Blog layout and pagination

The Blog index places metadata in a separate column on larger screens. Titles,
summaries, release badges and article headers use the shared spacing and type
tokens.

The starter Blog configuration now displays five posts per page:

```toml
+++
title = "Blog"
sort_by = "date"
paginate_by = 5
template = "blog.html"
page_template = "blog-page.html"
+++
```

This is a documented default for new sites, not a forced theme setting. Existing sites keep their own `paginate_by` value, and omitting it still renders all posts on one page.

## Downloads with a visible release path

The Downloads page separates its introduction, status, channels and verification
guidance into successive sections. Each channel retains its own download action.

Status and verification blocks share the new surface and type scales, and the layout adapts from a broad release overview to a single-column mobile flow. The underlying Downloads configuration remains unchanged.

## Upgrading from v0.5.0

1. Update the pinned theme tag to `v0.6.0` using [Install and update](@/docs/getting-started/installation.md).
2. Keep Zola `0.23.4`. This release does not change the current Zola target.
3. Optionally add `home_hero_image` and a meaningful `home_hero_image_alt` to a wide homepage.
4. Review site-owned Sass and template overrides, especially rules that target homepage, Docs, Blog or Downloads internals.
5. Choose the Blog page size explicitly if the new-site example of five posts does not fit the publication.
6. Run `zola check` and `zola build`, then inspect the four primary surfaces in light, dark and narrow layouts.

No authored component syntax migration is required.

## Compatibility

DevLab Theme `v0.6.0` targets Zola `0.23.4`. It has no Node.js, npm or external frontend runtime dependency.
