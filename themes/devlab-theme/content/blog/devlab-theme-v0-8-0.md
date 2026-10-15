+++
title = "DevLab Theme v0.8.0"
description = "A composition release with card grids, status timelines, Blog tables of contents and copy-safe code captions."
date = 2026-09-19

[taxonomies]
tags = ["release", "documentation", "components"]
authors = ["ripetitor"]

[extra]
release = "v0.8.0"
+++

This article describes its original release. Configuration examples may use
retired settings. For v0.9, follow the [upgrade guide](@/docs/guides/upgrading-v0-8.md)
and the current reference.

DevLab Theme `v0.8.0` is about composition. Authors can build landing sections and plans from reusable Markdown blocks, readers get a table of contents on long posts, and code examples can name the file they belong to. The release also settles the theme's own layout so sites stop patching it in Sass.

This release raises the minimum Zola version to `0.23.6` and explicitly escapes plain-text component fields when invoked from Markdown. Review overrides that put HTML in titles, labels or profile text: those fields now display it literally. Markdown bodies remain supported. Blog tables of contents are enabled by default. The theme still builds with Zola alone.

## Card grids

`devlab.cards` arranges `devlab.card` children in a responsive grid. A card can link somewhere, carry an icon and a status tag, and hold Markdown:

{% <devlab.cards> %}
{% <devlab.card title="Install" icon="arrow-right" href="@/docs/getting-started/installation.md"> %}
Pin a release tag and enable the theme in `zola.toml`.
{% </devlab.card> %}
{% <devlab.card title="Content components" icon="folder" href="@/docs/guides/content-components.md"> %}
Recipes for card grids, timelines and overview icons.
{% </devlab.card> %}
{% <devlab.card title="Codeberg" icon="codeberg" tag="External" href="https://codeberg.org/RiPetitor/devlab-theme"> %}
Source code, issues and release tags.
{% </devlab.card> %}
{% </devlab.cards> %}

A linked card adds a trailing arrow, an absolute HTTP(S) destination switches it to the external-link marker, and a static card omits the arrow entirely. Docs overview cards gained the same icon slot through an optional `[extra] icon` in section or page front matter, so a documentation landing page no longer needs hand-written markup.

## Status timelines

`devlab.timeline` presents a plan as ordered stages. Each stage declares a `status` and can add a date and a custom label:

{% <devlab.timeline> %}
{% <devlab.timeline_item title="Bring-up" status="done" date="2026-08"> %}
UART, bootloader, kernel, Device Tree and internal storage.
{% </devlab.timeline_item> %}
{% <devlab.timeline_item title="Hardware validation" status="active" date="2026-09"> %}
Network, USB, wireless, display, audio and sensors.
{% </devlab.timeline_item> %}
{% <devlab.timeline_item title="Preview" status="planned" date="Q4 2026"> %}
Test image with known limitations and pinned component versions.
{% </devlab.timeline_item> %}
{% <devlab.timeline_item title="Supported" status="planned" label="Release"> %}
Regression matrix on a release candidate before the stable release.
{% </devlab.timeline_item> %}
{% </devlab.timeline> %}

Supported statuses are `done`, `active`, `planned` and `blocked`. An unknown value falls back to `planned`. The state is carried by the badge text as well as the marker colour, so a plan still reads without colour.

## Blog tables of contents

Long posts now render a table of contents. On wide screens the article stays centered and the scrollable table of contents sits at the viewport edge. Below that width it collapses into a disclosure above the body. Both versions highlight the current section as the reader scrolls.

The Blog also uses a wider canvas than before, which puts that space to work without stretching the reading measure. Set `devlab.blog.show_toc = false` to publish posts without a table of contents. Posts with fewer than two headings stay unchanged.

## Code blocks that name their file

Zola already carries the `name` annotation of a fenced block into the rendered HTML. DevLab now shows it as the block title and demotes the language to a supporting chip:

```bash,name=build.sh,linenos
make -j"$(nproc)"
```

The toolbar reads the caption from Zola's `data-name` attribute and keeps it outside the copied source. The toolbar also prints readable language labels, which turns Zola's internal syntax names into `Shell`, `Text` and similar.

Syntax colors now follow the code block's own `--color-pre-scheme` token. A site can use dark code blocks on a light page without getting light-theme syntax colors on the dark background. Set this token alongside `--color-pre-bg` and `--color-pre-fg` in custom palettes.

## Navigation and feed fixes

Nested Docs navigation, breadcrumbs and previous/next links now preserve the current language. Blog author links use Zola's taxonomy URLs, including author names with spaces. Feed discovery follows `feed_filenames`, and author feed links use the first configured file with an accessible, configurable label.

## Footer links with names

Footer links used to collapse to an icon whenever one was configured. `devlab.footer.show_link_names = true` renders every link as its icon plus the visible name and shows the configured group label as a heading. The compact footer in the Docs sidebar and mobile panel follows the same setting, and icon-only links remain the default.

## Content media and narrow tables

Content images had no theme rules at all: an oversized image could leave the column. Images are now constrained to the content width, figure images and captions are styled, and table cells tighten on narrow screens next to the existing horizontal scroll. The theme also ships a `visually-hidden` utility.

## A quieter default

The release also removes rough edges that consumer sites were working around in their own Sass:

- A callout keeps the author's title casing instead of capitalizing every word, and an unknown `type` falls back to `info` with a readable label.
- Docs pages without headings no longer reserve an empty table of contents column.
- Docs overview cards use the full content column instead of stopping at a fixed width, and step lists keep their spacing against the following block.
- Blog article titles no longer break at a narrow character limit.

If your site patched any of these, the override can go.

## Previously on the development branch

Three features that were available only on `main` now ship in a tagged release: the dismissible [announcement banner](@/docs/guides/announcements.md), reusable [community cards](@/docs/guides/community.md) and [Blog author profiles](@/docs/guides/blog-authors.md). Sites that waited for a tag can adopt them without tracking the development branch.

## Upgrading from v0.7.0

1. Update the pinned theme tag to `v0.8.0` using [Install and update](@/docs/getting-started/installation.md) after the tag is published.
2. Upgrade Zola to `0.23.6` or newer. Review component overrides: titles, labels and profile text are plain text, while component bodies can still contain Markdown.
3. Posts with at least two headings now render a table of contents. Set `devlab.blog.show_toc = false` to opt out.
4. Review footer links. Icon-only links are unchanged. Enable `show_link_names` only when the labels should be visible.
5. Add `name=` to fenced blocks whose file or path matters at a glance. Existing blocks render as before.
6. Remove site-owned Sass that duplicates content images, figure captions, narrow tables, callout casing, the Docs table of contents or Blog title width.
7. Run `zola check` and `zola build`, then inspect the new components at a narrow viewport and with a keyboard.

The complete component contracts live in [Tera components](@/docs/reference/components/_index.md), the new recipes in [Content components](@/docs/guides/content-components.md), and every setting in the [configuration reference](@/docs/reference/configuration/_index.md).

## Compatibility

DevLab Theme `v0.8.0` requires Zola `0.23.6` or newer. Theme consumers still build with Zola alone: Node.js and npm are not runtime or site-build requirements, and the bundled diagram renderer does not depend on a remote service.
