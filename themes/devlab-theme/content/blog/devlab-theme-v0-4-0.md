+++
title = "DevLab Theme v0.4.0"
description = "A breaking Tera 2 release for Zola 0.23.1 with native content components, stricter templates and language-aware internals."
date = 2026-08-06

[taxonomies]
tags = ["release", "migration", "tera"]

[extra]
release = "v0.4.0"
+++

This article describes its original release. Configuration examples may use
retired settings. For v0.9, follow the [upgrade guide](@/docs/guides/upgrading-v0-8.md)
and the current reference.

DevLab Theme `v0.4.0` moves completely to Zola `0.23.1` and Tera 2. It deliberately drops compatibility with earlier Zola versions: the template engine was rewritten, macros disappeared, shortcodes were removed from Zola, and Markdown bodies are now Tera templates before they are rendered as Markdown.

This is a real breaking release rather than a compatibility shim. In return, the theme now has one component system for layouts and authored content, clearer build failures and less theme-specific syntax.

## One global component API

All reusable content now lives in `templates/components.html` and uses a namespaced Tera 2 API:

- `devlab.card`
- `devlab.callout`
- `devlab.details`
- `devlab.steps`
- `devlab.icon`

Components are registered globally and need no import. Block components accept Markdown bodies:

{% raw %}
```jinja
{% <devlab.callout type="tip" title="Tera 2"> %}
The component body supports **Markdown**.
{% </devlab.callout> %}
```
{% endraw %}

Inline components use a self-closing tag:

{% raw %}
```jinja
{{<devlab.icon name="github" label="GitHub" />}}
```
{% endraw %}

The complete API and live examples are in [Tera components](@/docs/reference/components/_index.md).

## Required content migration

Old shortcode calls do not build on Zola 0.23. Replace them before upgrading:

| Before | Now |
| --- | --- |
| `card(...)` / `end` | `devlab.card` block component |
| `callout(...)` / `end` | `devlab.callout` block component |
| `details(...)` / `end` | `devlab.details` block component |
| `steps()` / `end` | `devlab.steps` block component |
| `icon(...)` | self-closing `devlab.icon` component |

Zola 0.23 also evaluates Tera inside fenced code blocks. Wrap literal template examples in matching Tera raw/endraw blocks. Files that must never execute Tera can instead be listed under `skip_content_templating`, but pages using DevLab components must remain templated.

## Required template migration

Site-owned overrides must follow the Tera 2 migration rules:

- replace macro declarations, template imports, and `namespace::call()` invocations with components.
- pass named test arguments, such as `value is starting_with(pat="/docs/")`.
- replace recursive includes with recursive components.
- use square brackets for array indexes.
- review stricter undefined-value access and renamed or removed filters.

DevLab's own recursive Docs navigation, section catalogs, table of contents, pagination helpers, cards, links and icons are all Tera 2 components now. No compatibility macros remain in the theme.

## Language-aware internals

Content lookups now pass the active `lang` to `get_page` and `get_section`, using the canonical content paths required by Zola 0.23. Search loads `search_index.<lang>.js` automatically unless `extra.devlab.search.index` explicitly overrides the filename.

The active Zola language also supplies the document's `lang` attribute. Interface strings remain configurable through the existing label fields. This release does not claim complete translation coverage.

## Cleaner component assets

SVG fragments now live under `templates/components/icons/`, and every fragment is explicitly excluded from keyboard focus. The public icon component keeps decorative icons hidden from assistive technology and adds an accessible name only when `label` is present.

The CI build downloads the official Zola `0.23.1` musl archive and verifies its SHA-256 digest before extraction. Both the theme metadata and documentation now require 0.23.1.

## Upgrading from v0.3.1

1. Install Zola `0.23.1`.
2. Convert every DevLab shortcode call to the matching `devlab.*` component.
3. Protect literal Tera examples in Markdown with raw blocks.
4. Migrate any site-owned template overrides from macros and Tera 1 syntax.
5. Update the pinned theme tag to `v0.4.0` using [Install and update](@/docs/getting-started/installation.md).
6. Run `zola check` and a production `zola build` with the site's real base URL.
7. Review Docs navigation, component output, search, feeds and custom overrides.

The upstream breaking changes are documented in the [Zola 0.23 changelog](https://github.com/getzola/zola/blob/v0.23.1/CHANGELOG.md) and the [Tera 2 migration guide](https://github.com/Keats/tera/blob/master/MIGRATION.md).

## Compatibility

DevLab Theme `v0.4.0` requires Zola `0.23.1` or newer. It has no Node.js, npm or external frontend runtime dependency.
