+++
title = "Content, layouts and overrides"
description = "How Zola content, DevLab collections and site overrides fit together."
weight = 1
updated = 2026-10-06
+++

Zola owns content, routes, sorting, taxonomies, feeds and search indexes.
DevLab renders that content with templates and styles, then adds browser
controls where interaction needs them.

## Site files

| Location | Responsibility |
| --- | --- |
| `zola.toml` | Zola settings and `extra.devlab` configuration. |
| `content/` | Markdown pages, section introductions and front matter. |
| `data/` | Site-owned records, such as author profiles and download catalogs. |
| `templates/` | Site template extensions and partial replacements. |
| `static/` | Site images, stylesheets and scripts. |
| `sass/` | Optional Sass entrypoints; use names distinct from the theme's files. |
| `themes/devlab-theme/` | The installed theme revision. |

Keep site changes outside the installed theme. An update replaces theme files.
It should not replace authored content or local overrides.

## Layout selection

A page's `template` selects its layout. Otherwise, it inherits the closest
ancestor section's `page_template`. That setting applies recursively. A
section's `template` selects its own landing layout and is not inherited by
child sections. These are [Zola's rules](https://www.getzola.org/documentation/content/section/).

Use `page.html` and `section.html` for ordinary content, `blog.html` and
`blog-page.html` for dated posts, `docs.html` and `doc-page.html` for documentation,
and `reader.html` for books. `landing.html` assembles ordered blocks.
The [layout guide](@/docs/guides/content-layouts.md) lists their behavior.

Layout controls the document shell and scripts. Naming a directory `docs`
does not select the Docs layout. An ordinary page within a Docs tree keeps the
layout explicitly assigned to it.

## Collection membership

A [collection](@/docs/reference/collections.md) registers a source root, such as
`manual/_index.md`. Membership follows the source file's ancestors. A page
under `content/manual/` can publish at `/reference/` with its own `path` and
still belong to the manual. Its collection link stays active, and its sidebar
branch opens in desktop and mobile navigation.

The most specific registered root wins when collections nest. Each collection
supplies its title, reading options and navigation scope. Curated navigation
must contain distinct, non-overlapping branches within that root.

Collections do not assign templates. Set the layout and register the root
separately, as shown in [Docs settings](@/docs/reference/configuration/reading.md#optional-docs).

## Option inheritance

Reading options resolve in this order:

1. Theme defaults from `theme.toml`.
2. Site defaults in `extra.devlab.docs`.
3. The active collection.
4. Ancestor sections within that collection, from root to child.
5. The current section or page.

A later scalar or array replaces an earlier value. `page_actions` merges
individual fields, so changing Copy link does not discard an inherited Edit URL.
Empty strings and `false` are valid overrides.

Landing settings resolve from site defaults and current content. Interface
labels resolve from English defaults, the active language and site overrides.
Product names and descriptions belong in authored content or data files.
Translating interface labels does not translate them.

## Template extensions

[Zola gives site templates priority](https://www.getzola.org/documentation/themes/extending-a-theme/)
over matching theme paths. Extend the namespaced theme template to change a
block without copying the whole file. Use a partial slot to replace a small
piece of markup. Use CSS tokens to change colors and widths.

The [extension reference](@/docs/reference/extensions.md) lists public blocks,
slots and their inputs. Files in `partials/devlab/` and `components/internal/`
implement those contracts. Site overrides should use the public slots.

## Build and browser behavior

A consumer build requires Zola alone. It compiles Sass, resolves templates and
writes static files. Theme development tests also use Python and Node.js.

The theme includes one main stylesheet and a print stylesheet. Browser scripts
handle color mode, menus and reading controls. Code Copy, tabs and diagrams
load on content that uses them. Download layouts also load their shared controls.
Search loads its index and language modules on first interaction.

Without JavaScript, authored content and generated links remain visible.
Tabs expose all panels and catalog pages list all products. Search, Copy,
color selection and dismissing notices need JavaScript. Browser printing
prints the current page. It does not assemble a whole book.
