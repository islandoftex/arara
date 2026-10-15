+++
title = "DevLab v0.9.0: layouts, collections and extension slots"
description = "Layouts, collections, sidebar labels, community cards, icons and setup in DevLab 0.9.0."
date = 2026-10-02
updated = 2026-10-06

[taxonomies]
tags = ["release", "architecture", "documentation"]
authors = ["ripetitor"]

[extra]
release = "v0.9.0"
+++

DevLab v0.9.0 lets a project combine a landing page, blog, several manuals and
a book in one Zola site. Layouts are selected in content front matter, reading
roots are registered in configuration, and template slots let a site replace
individual parts of the interface.

## Layouts follow content settings

The theme provides ordinary page and section layouts, dated articles,
documentation, Reader, landing pages and downloads. Zola's `template` selects
a layout. A section's `page_template` applies recursively to its descendants.
A closer section or a page can override it.

A URL no longer implies a layout. A page under `content/manual/` can use
`page.html` without receiving a documentation shell. A post outside the blog
directory can use `blog-page.html` and receive article metadata and a TOC.
Custom layouts declare their context in the `layout_context` block.

[Site architecture](@/docs/guides/architecture.md) explains the content model,
layout selection and ownership of site files.

## Landing pages use ordered blocks

A landing page now reads an array of blocks. Authors choose their order and
can repeat feature or content sections. The built-in types are `hero`,
`features`, `learn`, `workflow`, `cta` and `content`.

For example, in `content/product.md`:

```toml
+++
title = "Acme"
description = "Tools for inspecting build logs."
template = "landing.html"

[extra.devlab.home]
blocks = [
  { type = "hero", actions = [{ label = "Read the manual", path = "/manual/" }] },
  { type = "features", title = "Tools", items = [{ title = "Log search", description = "Find failed jobs and related output." }] },
  { type = "content" },
]
+++

## Installation

Add the package and configure the log directory.
```

The hero uses the page title and description. The `content` block renders its
Markdown body. A hero must come first and can appear only once. Duplicate block
IDs and unknown block types stop the build. A site can add a block type through
`partials/landing-block.html`.

See [Landing blocks](@/docs/reference/landing.md) for fields and inheritance.

## Manuals and books have named collections

A collection identifies a reading root by its content source. For a site with
`content/manual/_index.md` and `content/api/_index.md`, configuration can contain:

```toml
[extra.devlab.collections.manual]
section = "manual/_index.md"

[extra.devlab.collections.api]
section = "api/_index.md"
show_toc = false

[extra.devlab.navigation]
links = [
  { name = "Home", path = "/" },
  { collection = "manual" },
  { collection = "api" },
]
```

The theme gets each root's URL and title from Zola. Sidebar, mobile tree,
breadcrumbs and reading links use the active collection. Source ancestry
keeps a page in that collection when the page sets a custom public `path`.
The corresponding header link stays active and its sidebar branch stays open.

Reading options inherit from site defaults, then the collection, ancestor
sections and current content. `page_actions` merges individual fields, so a
local Copy link setting preserves the inherited Edit URL. Arrays replace the
previous array.

Curated navigation selects distinct branches within the collection. Foreign,
duplicate and overlapping entries fail the build. Catalog pages show one
level of content in both automatic and curated modes: direct pages and links
to child sections.

Navigation also accepts text dividers such as `{ label = "Applications" }`.
An optional `translations` map supplies labels for other languages. Dividers
appear in the desktop sidebar and mobile Docs menu, without links, toggles or
keyboard focus. They do not create pages or affect Previous/Next links. A group
with no sections in the active language is omitted.

[Collections](@/docs/reference/collections.md) and
[Docs navigation](@/docs/guides/navigation.md) describe the configuration.

## Reading navigation and page widths

Active entries in the Docs sidebar and page TOC use text highlighting. Blog
and author post lists use the same centered width as articles. Headings,
descriptions and separators align within that width.

On the homepage, Blog and Downloads, the header separator appears after
scrolling. Docs and Reader show it immediately. Without JavaScript, the
separator remains visible on every page.

## Reader keeps one chapter sequence

`reader.html` renders chapter pages and section introductions. One sequence
controls the chapter list and Previous/Next links. By default, it contains the
root introduction, direct pages in Zola order, then subsection introductions
and descendants. Transparent sections contribute their content without adding
an introduction of their own.

An explicit `reading_order` can place introductions and chapters in another
order. Entries must be unique and belong to the book. Missing primary-language
files fail the build. Untranslated entries are omitted from that language's
sequence. Filenames containing `_index` are treated as ordinary chapter files
unless the filename itself starts with `_index.`.

Reader and Docs include a compact TOC on narrower screens. Edit links remain
available below the content when the desktop panel is hidden. The print
stylesheet removes navigation, announcements and controls, wraps code, and
includes every tab panel with its label. Printing covers
the current chapter. Whole-book PDF and EPUB export are outside this release.

See [Reader](@/docs/reference/reader.md) for a root section, first chapter and
explicit ordering example.

## Small template changes use slots

Sites can replace header brand, navigation or tools separately. Shared page
slots cover headings, extra heading content, metadata and actions. Sidebar,
footer and icon slots cover the remaining common extension points.

A custom icon added through `partials/icon.html` works in content and footer
links. A missing footer icon falls back to the link name. Linked cards use a
heading link, allowing ordinary links in the Markdown body without nested
anchors.

Base blocks also expose additions to the head, main landmark and body.
`layout_scripts` belongs to a layout. `extra_scripts` adds site script tags after
theme and layout tags. Use `defer` on dependent classic scripts. Theme templates keep ordinary inheritance paths,
so site overrides continue to participate in Zola's lookup rules.

CSS tokens control colors, fonts and widths. Overrides loaded after the theme
stylesheet apply in light, dark and system mode. Public slots and component
arguments are listed in [Extension API](@/docs/reference/extensions.md).
`partials/devlab/` and `components/internal/` contain their implementation.

## Community cards

`devlab.people` renders a grid of centered profiles with avatars or initials,
names, optional nicknames and social links. Each profile can include a role,
biography and a support action below the card body. Cards use authored records
in the site's front matter or configuration and require no JavaScript.

Profile, social and support destinations use the shared route resolver,
including the deployment prefix and active language for local links. A missing
name or incomplete support action stops the build with an explanatory error.

The [Community cards guide](@/docs/guides/community.md) includes a complete
page example. The [People reference](@/docs/reference/components/people.md)
lists its fields.

## Bundled SVG icons

Interface icons use Tabler Icons 3.48.0. Service logos use Simple Icons 16.34.0.
The default header mark and favicon use a terminal symbol. SVG files are
bundled with the theme and inherit the surrounding text color. Their licenses
and source mappings are included in the repository.

The [icon catalog](@/docs/reference/components/icons.md#icon-catalog) shows
every built-in icon. Sites can add their own icons through the existing
`partials/icon.html` slot.

## Translations use one interface catalog

English, German and Russian interface labels live in `theme.toml`. Sites can
override individual keys or add another language. Controls and their accessible
names use the same resolved catalog. Downloads use its nested `downloads`
table, with optional overrides from the selected data file.

Content still needs its own translations. The switcher and `hreflang` metadata
link to existing translated pages and sections. Missing translated collection
roots remove that language's collection link. Zola language configuration also
sets the translated site title, description, taxonomies and search index.

Search loads the active language's generated index on first interaction.
German and Russian load the matching stemmer support and language modules
before the index. Other indexed languages can supply ordered plugin paths.
See [Languages](@/docs/reference/languages.md) and
[Search configuration](@/docs/reference/configuration/search.md).

## Downloads share a data format

Single downloads lists one product and its files by release channel. Catalog
lists several products with detail pages, search, support and vendor filters,
sorting and result counts. Both layouts read an explicit TOML data file.

Product, channel and support IDs are validated during the build. A file becomes
downloadable when its URL is present. Its maturity status does not enable the
link. Preview and planned artifacts can therefore appear before files exist.
Checksum and signature links are optional and require an enabled download.
Each product can replace shared mirrors and override verification fields.
Catalog counts include only enabled files, and download labels distinguish
architecture and version.
The theme does not query release APIs or invent verification files.

[Single downloads](@/docs/guides/downloads.md),
[Catalog](@/docs/guides/download-catalog.md) and the
[data reference](@/docs/reference/downloads.md) cover the two layouts.

## Setup starts with two site files

The installation guide links a local theme checkout or pins a published
revision. [Quick start](@/docs/getting-started/quick-start.md) then creates
`zola.toml` and `content/_index.md`. Search, feeds and specialized layouts are
added when the site needs them. Standalone starter projects are removed.
Examples sit beside the settings or components they explain.

The supported API is documented directly. Retired configuration keys stop the
build with an upgrade message. The [v0.8 migration guide](@/docs/guides/upgrading-v0-8.md)
maps the old homepage and reading settings to blocks and collections. Parallel
label tables and fixed homepage rendering are removed. Current configuration uses ordered landing blocks,
named collections, explicit navigation destinations and the interface catalog.

A site build requires **Zola 0.23.6 or newer**. There is no frontend build step.
Copy, tabs and diagrams load on pages that use them. Download layouts load
their shared controls. Static navigation and content remain readable without
JavaScript.

The [publishing guide](@/docs/guides/publishing.md) covers metadata, feeds,
cache settings and host policies.

## Publication and release checks

Blog lists and articles link to tag archives. Description text takes precedence
in lists, with a plain-text summary fallback. Dated articles link to their
adjacent posts. Author profiles require non-empty names and supply their display
name and role to page metadata.

The [Blog guide](@/docs/guides/blog.md) walks through the first post, authors,
tags and feeds. [Deployment](@/docs/guides/deployment.md) covers uploading
`public/` and checking live routes. The release tag is published separately.
Until it exists, the installation guide and Download page provide a repository
checkout. The demo build enables versioned archive URLs only after verifying
that the local and published tag point to the same commit and both archives
are available.
