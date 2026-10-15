+++
title = "Languages and metadata"
description = "Content translations, interface labels and sharing metadata."
weight = 7
updated = 2026-10-06
+++

## Content languages

Declare languages through Zola and create translated Markdown files such as
`page.de.md` and `_index.de.md`. Collection source paths use the primary source.
The theme resolves the current translation and its actual permalink.

```toml
title = "Acme"
default_language = "en"
[languages.de]
title = "Acme"
description = "Handbücher für das Acme-Projekt."
build_search_index = true

[extra.devlab.i18n]
show_switcher = true
rtl_languages = []

[extra.devlab.i18n.language_names]
en = "English"
de = "Deutsch"
```

Zola provides language-specific `config.title` and `config.description`. Set
those fields in every language table. Zola does not inherit their primary-language
values. They supply the header brand and site metadata when no brand override
is configured.

The header switcher and `hreflang` metadata list existing translations of the
current content. An untranslated page does not link to a guessed URL. Disable
`show_switcher` to hide the control. Alternate metadata remains available.
Languages in `rtl_languages` use `dir="rtl"`.

## Interface catalog

English, German and Russian defaults live in `theme.toml`. Override individual keys:

```toml
[extra.devlab.i18n.translations.de]
search = "Durchsuchen"
toc = "Inhalt"
copy = "Code kopieren"
theme_dark = "Dunkles Design"
```

For another language, create its table. Missing keys fall back to the English
catalog. Plain text remains escaped in HTML and attributes. JavaScript controls
receive the same catalog through their rendered `data-*` attributes.

| Group | Keys |
| --- | --- |
| Navigation | `skip`, `main_navigation`, `mobile_navigation`, `main_mobile_navigation`, `docs_mobile_navigation`, `docs_sidebar`, `docs_section_toggle`, `breadcrumb`, `toc`, `previous`, `next`, `pagination`, `page`, `page_count`, `language_switcher` |
| Menu | `menu`, `menu_open`, `menu_close`, `menu_back`, `menu_docs` |
| Search | `search`, `search_placeholder`, `search_start`, `search_no_results`, `search_unavailable`, `search_results`, `search_results_count` |
| Appearance | `theme_light`, `theme_dark`, `theme_system` |
| Reading | `published`, `updated`, `post_authors`, `reading_time`, `author_feed`, `post_tags`, `post_navigation`, `newer_post`, `older_post`, `page_actions`, `last_updated`, `copy_link`, `link_copied`, `copy_link_failed`, `copy_link_success`, `copy_link_error`, `edit_page`, `feedback_prompt`, `report_issue` |
| Code | `copy`, `copied`, `copy_failed`, `copy_success`, `copy_error` |
| Other | `announcement`, `announcement_dismiss`, `footer_links`, `not_found`, `not_found_description`, `go_home`, `not_found_hint`, `docs_sections`, `docs_pages`, `docs_pages_sections`, `docs_pagination` |

Keep `{title}` in `docs_section_toggle`, `{author}` in `author_feed`, `{count}`
in search counts, and `{current}` / `{total}` in `page_count`. Download labels
use the nested `translations.<language>.downloads` table. Local data-file
`[labels]` overrides it. Its complete keys and placeholders are in `theme.toml`.
Product names, navigation names, authored messages and component bodies belong
to content. Translate them in the corresponding Markdown or data file.

## Metadata

Site defaults live in `[extra.devlab.seo]`. Page or section front matter can
override fields through `[extra.devlab.seo]`:

```toml
[extra.devlab.seo]
og_image = "/images/share.webp"
og_image_alt = "Preview of the product"
og_type = "website"
```

`og_image` defaults to empty. `og_image_alt` describes an existing sharing image.
Local paths respect `base_url`. Article layouts default to `article`. Ordinary
pages, landings, Docs and Reader default to `website`. `og_type` can explicitly
override the default. Title and description continue to come from content, with
site fallbacks. Canonical URLs use the generated route.
