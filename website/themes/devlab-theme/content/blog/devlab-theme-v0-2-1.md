+++
title = "DevLab Theme v0.2.1"
description = "A focused follow-up release for resilient navigation, documentation trees and a simpler configuration contract."
date = 2026-07-13T18:47:02+05:00

[taxonomies]
tags = ["release"]

[extra]
release = "v0.2.1"
+++

This article describes its original release. Configuration examples may use
retired settings. For v0.9, follow the [upgrade guide](@/docs/guides/upgrading-v0-8.md)
and the current reference.

DevLab Theme `v0.2.1` tightens the `v0.2` foundation with more predictable optional features, safer links and fewer compatibility branches.

## Breaking configuration change

DevLab Theme `v0.2.1` removes the flat configuration aliases retained from `0.1.x`. Sites already using `extra.devlab.*` require no changes. Sites still using flat fields under `[extra]` must update them before upgrading. Otherwise those values are ignored and the affected features use their defaults.

| Previous field                                  | Structured field                                                          |
| ----------------------------------------------- | ------------------------------------------------------------------------- |
| `logo_text`, `footer_text`                      | `devlab.brand.logo_text`, `devlab.brand.footer_text`                      |
| `nav_links`                                     | `devlab.navigation.links`                                                 |
| `docs_section`, `docs_path`, `get_started_path` | `devlab.docs.section`, `devlab.docs.path`, `devlab.docs.get_started_path` |
| `blog_path`                                     | `devlab.blog.path`                                                        |
| `theme_default`, `theme_toggle`                 | `devlab.appearance.default_mode`, `devlab.appearance.show_toggle`         |
| `search_index`                                  | `devlab.search.index`                                                     |
| `og_image`                                      | `devlab.seo.og_image`                                                     |

Existing `home_*` homepage fields, `extra.labels` and interface labels remain unchanged.

## Highlights

- Navigation can be empty or contain only the sections a site actually uses. Search and the color-mode toggle remain independent.
- Docs navigation, overview trees and previous or next links now pass through transparent sections without exposing structural placeholders.
- Mobile anchor targets stay visible below the sticky header, and drawer transitions no longer interfere with focus restoration.
- Internal paths still respect Zola's base URL, while absolute, protocol-relative, fragment, query, email and telephone links keep their original form.
- Code blocks preserve their layout without JavaScript, including long lines and copy-button spacing.
- Page and section title overrides once again flow into the final document title without duplicating SEO logic.
- Tagged installation examples are pinned to the release, and CI verifies that a release tag matches `theme.toml`.

## Compatibility

DevLab Theme `v0.2.1` requires Zola `0.22.1` or newer and remains fully Zola-native: no Node.js, npm or external frontend runtime is required.
