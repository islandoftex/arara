+++
title = "Content components"
description = "Compose card grids, status timelines and Docs overview icons from reusable Markdown components."
weight = 4
updated = 2026-10-06
+++

Use `devlab.*` components directly in Markdown. The examples below combine
card grids, timelines and overview icons. Arguments and defaults are listed
in the [component reference](@/docs/reference/components/_index.md).

## Card grids

`devlab.cards` arranges `devlab.card` children in a responsive grid that collapses to one column on narrow screens. Each card can link somewhere, carry an icon and a status tag, and hold Markdown.

{% <devlab.cards> %}
{% <devlab.card title="Install" icon="arrow-right" href="@/docs/getting-started/installation.md"> %}
Pin a release tag and enable the theme in `zola.toml`.
{% </devlab.card> %}
{% <devlab.card title="Tera components" icon="file" href="@/docs/reference/components/_index.md"> %}
Every component with its parameters and live examples.
{% </devlab.card> %}
{% <devlab.card title="Codeberg" icon="codeberg" tag="External" href="https://codeberg.org/RiPetitor/devlab-theme"> %}
Source code, issues and release tags.
{% </devlab.card> %}
{% </devlab.cards> %}

A linked card adds a trailing arrow, and an absolute HTTP(S) destination switches that arrow to the external-link marker:

{% raw %}
```jinja
{% <devlab.cards> %}
{% <devlab.card title="Tera components" icon="file" href="@/docs/reference/components/_index.md"> %}
Every component with its parameters and live examples.
{% </devlab.card> %}
{% <devlab.card title="Codeberg" icon="codeberg" tag="External" href="https://codeberg.org/RiPetitor/devlab-theme"> %}
Source code, issues and release tags.
{% </devlab.card> %}
{% </devlab.cards> %}
```
{% endraw %}

Omit `href` for a standalone card. The grid fits as many 16rem columns as the content width allows, so keep titles short: they become the card headings, and a long title wraps inside its card rather than widening the row.

## Status timelines

`devlab.timeline` presents a plan as ordered stages. Each `devlab.timeline_item` declares a `title` and a `status`, and can add a free-form `date` plus a custom `label`:

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

Supported statuses are `done`, `active`, `planned` and `blocked`. Any other value falls back to `planned`. A stage carries its state in both the badge text and the marker colour, so the plan still reads without colour. `label` replaces the badge text when a project uses its own wording, and `heading_level` matches the stage headings to a surrounding outline other than the default `h3`.

Use a timeline for stages with dates or status. For sequential instructions,
use [Steps](@/docs/reference/components/steps.md).

## Docs overview icons

Docs overview cards read an optional icon from a section or page in front matter:

```toml
[extra]
icon = "folder"
```

The same icon set powers the `icon` argument on `devlab.card`. Omit the field to keep a plain text card.

## Choose authored or generated lists

Cards and timelines belong in the page body and work in Docs, Blog and generic
layouts. Section templates generate lists from the content tree. Components
render the items you write explicitly.

## Related references

- [Tera components](@/docs/reference/components/_index.md): parameters, examples and the full icon list.
- [Content layouts](@/docs/guides/content-layouts.md): generated pages and sections.
- [Branding](@/docs/customization/branding.md): the color tokens the components consume.
