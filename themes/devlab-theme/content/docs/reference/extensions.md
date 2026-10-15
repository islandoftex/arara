+++
title = "Extension API"
description = "Small template slots and stable boundaries for consumer sites."
weight = 8
updated = 2026-10-06
+++

## Blocks

Extend `devlab-theme/templates/base.html` in the site's `templates/base.html`.
Override a block without copying the document shell.

| Block | Contract |
| --- | --- |
| `head_extra`, `extra_styles` | Metadata and CSS after the theme's head. |
| `body_start`, `body_end` | Content outside the site shell. |
| `main_before`, `main_after` | Content inside the main landmark, around the layout. |
| `extra_scripts` | Site script tags after theme and layout tags. Use `defer` for dependent classic scripts. |
| `layout_context` | Declare `devlab_layout`: `page`, `blog`, `article`, `docs`, `reader` or `downloads` in a custom layout. |
| `layout_scripts` | Assets owned by a custom layout. Keep site scripts in `extra_scripts`. |

`content`, `title` and `main_class` remain layout blocks. A custom layout must
declare its capabilities explicitly. Placing a generic page below `/docs/` does
not turn it into a documentation layout.

## Partial slots

Create just the matching file in the site's `templates/partials/` directory.

| Partial | Input and responsibility |
| --- | --- |
| `header-brand.html` | `config`, `lang`; brand link inside the theme's header wrapper. |
| `header-navigation.html` | `nav_links`, `active_path`, `active_url`, `lang`; desktop links. |
| `header-tools.html` | `config`, `lang`, `has_site_search`; search, appearance and optional site tools. |
| `page-heading.html` | `context_item`, `devlab_layout`, `ui`; the shared page heading. Optional `heading_id` and `heading_header_class`. |
| `page-heading-extra.html` | `context_item`, `devlab_layout`, `ui`; additions below the description. |
| `page-meta.html` | The same context; article metadata, empty on other layouts by default. |
| `page-actions.html` | The same context; reading actions, empty on generic/article layouts by default. |
| `sidebar-before.html`, `sidebar-after.html` | `docs_section`, `docs_options`; additions inside the sidebar. |
| `footer-content.html` | Footer data and compact mode; see [template customization](@/docs/customization/templates.md). |
| `icon.html` | `name`; selects an SVG fragment for both Markdown and templates. |

Empty sidebar slots render nothing. Replacing an entire header, sidebar or
footer makes its wrapper and behavior the site's responsibility.

## Add an icon

Create `templates/partials/icon.html`:

{% raw %}
```jinja
{% if name == "rocket" %}
  {% include "components/icons/rocket.html" %}
{% else %}
  {% include "partials/devlab/icons.html" %}
{% endif %}
```
{% endraw %}

Put the SVG fragment in `templates/components/icons/rocket.html`. Use the same
SVG wrapper convention as a built-in fragment. Render it with
`devlab.icon name="rocket"`. Pass `label` when it conveys meaning. The fallback
keeps built-in icons intact. Footer links use the same slot. An unknown icon falls back to the visible link
name, so a missing fragment does not leave an unnamed control.

## CSS and runtime hooks

Public CSS tokens are listed in [Branding](@/docs/customization/branding.md).
Load a site stylesheet through `extra_styles`. A later `:root` token declaration
overrides theme defaults in light, dark and system mode. Use explicit
`data-theme` selectors only when the two palettes intentionally differ.

The existing JS controls use `data-site-menu-*`, `data-search-*`,
`data-docs-nav-*`, `data-devlab-toc`, `data-devlab-toc-content`, `data-tabs-*`,
`data-theme-toggle`, and `data-docs-copy-link*`. These hooks belong to the
corresponding built-in controls. Keep the complete built-in control partial
when composing slots. A hook by itself does not implement the control.

Public component names and documented arguments form the component API. Their
definitions are organized under `components/core.html` and
`components/content.html`. Definitions under `components/internal/` and
partials under `partials/devlab/` are internal. Do not replace definition files
to change one icon or one header item.
