+++
title = "DevLab Theme v0.5.0"
description = "A multilingual and RTL-ready release with consistent Docs navigation, an explicit search contract and an exact Zola 0.23.4 target."
date = 2026-08-21

[taxonomies]
tags = ["release", "documentation", "internationalization"]

[extra]
release = "v0.5.0"
+++

This article describes its original release. Configuration examples may use
retired settings. For v0.9, follow the [upgrade guide](@/docs/guides/upgrading-v0-8.md)
and the current reference.

DevLab Theme `v0.5.0` brings the documentation interface onto one language-aware routing model and targets Zola `0.23.4`. Docs navigation, local links, search and layout direction now follow explicit contracts instead of relying on assumptions that only hold for a single-language site.

This release does not provide a translation catalog for authored content or every theme-owned string. It supplies the routing, partial-translation and RTL foundations that a multilingual site needs while keeping interface labels configurable through the existing settings.

## One language-aware route model

Local header, mobile-menu, homepage, CTA, footer, feed and Docs links now resolve for the active Zola language. External URLs, protocol-relative URLs, email and telephone links, fragments and query-only references remain unchanged.

Docs also handle partial translations deliberately. A missing translated Docs root hides that language's Docs destinations. When the translated root exists, untranslated curated sectors are skipped, and a Docs destination without any renderable navigation items stays a direct link instead of opening an empty mobile panel. The default-language configuration remains strict, so a missing configured Docs section still stops the build.

Content components cannot infer a page's language by themselves. Pass the active language when a card contains a local link that should follow translated routes:

{% raw %}
```jinja
{% <devlab.card title="Fast setup" href="@/docs/getting-started/_index.md" content_lang={lang}> %}
Open the translated guide.
{% </devlab.card> %}
```
{% endraw %}

## Explicit right-to-left languages

Sites can list the active language codes that should render right-to-left:

```toml
[extra.devlab.i18n]
rtl_languages = ["ar", "he"]
```

DevLab sets the document direction from this list. The drawer, header, Docs sidebar, table of contents, overview cards, pagination and content components use logical layout properties and mirror their directional indicators in RTL. Inline and fenced code remain isolated as left-to-right content so technical punctuation keeps its intended order.

Unlisted languages continue to use `ltr`. Codes may name the default language as well as languages configured under Zola's `[languages]` table.

## Docs navigation follows one scope

When `extra.devlab.docs.navigation` curates the top-level documentation sectors, previous and next links now use that same scope and order. Pages below omitted sectors remain published but no longer reappear through pagination.

Within each selected sector, navigation follows the content tree depth first: direct pages come before subsection pages. Duplicate page paths are removed in first-occurrence order, preventing repeated or overlapping manifest entries from creating pagination loops. Keep manifest entries unique and non-overlapping anyway, because the sidebar and root catalog render each configured top-level sector. Breadcrumbs continue to represent the real content hierarchy independently of the curated top level.

Translated Docs roots, nested sections, breadcrumbs, catalogs and pagination now resolve Zola's language-specific source paths without appending a language suffix twice.

## An explicit search index contract

DevLab's bundled search adapter consumes Zola's JavaScript Elasticlunr index. New and upgraded sites should state that format explicitly:

```toml
build_search_index = true

[search]
index_format = "elasticlunr_javascript"
include_title = true
include_description = true
include_content = true
```

An explicitly configured incompatible format now hides the search control, omits its scripts and removes search-specific guidance from the 404 page. If the field is absent, search continues to work only while Zola's active default produces the supported format.

## Current Zola target

DevLab `v0.5.0` targets Zola `0.23.4`. The CI build downloads the official GNU archive, verifies its SHA-256 checksum and checks the reported version before building the site. The theme follows the current Zola release rather than promising a compatibility range across older or newer versions.

No authored component syntax migration is required from `v0.4.0`, but the Zola update is required.

## Upgrading from v0.4.0

1. Install Zola `0.23.4` and confirm it with `zola --version`.
2. Add `search.index_format = "elasticlunr_javascript"` when search is enabled.
3. List every active RTL language under `extra.devlab.i18n.rtl_languages`. Leave the array empty for an LTR-only site.
4. Pass `content_lang={lang}` to cards whose local links should follow translated content routes.
5. Review curated previous and next links, and keep Docs manifest entries unique and non-overlapping.
6. Review site-owned template and Sass overrides that assume physical left or right positioning.
7. Update the pinned theme tag to `v0.5.0` using [Install and update](@/docs/getting-started/installation.md).
8. Run `zola check` and `zola build`, then inspect Docs navigation, search and local links in every configured language.

Sites that remain single-language and left-to-right need no new i18n configuration.

## Compatibility

DevLab Theme `v0.5.0` targets Zola `0.23.4`. It has no Node.js, npm or external frontend runtime dependency.
