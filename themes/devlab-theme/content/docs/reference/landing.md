+++
title = "Landing blocks"
description = "Ordered blocks for homepages and product pages."
weight = 5
updated = 2026-10-06
+++

Use `landing.html` for any page or section. The homepage already uses it.
Settings live in `[extra.devlab.home]` in site configuration or front matter.
Local fields override site fields. `blocks` replaces the complete list.

```toml
[extra.devlab.home]
layout = "wide"
blocks = [
  { type = "hero", eyebrow = "My product", actions = [{ label = "Read", path = "/manual/" }] },
  { type = "features", title = "Features", items = [{ title = "Fast", description = "Built with Zola." }] },
  { type = "content" },
  { type = "cta", title = "Start today", actions = [{ label = "Install", path = "/manual/install/" }] },
]
```

## Fields

| Field | Default | Meaning |
| --- | --- | --- |
| `layout` | `default` | `default` or `wide`; invalid values fail the build. |
| `blocks` | Hero, then content | Ordered array. Empty array leaves the page title. |
| Block `type` | Required | `hero`, `features`, `learn`, `workflow`, `cta`, `content`. |
| Block `id` | `landing-block-N` | Unique HTML id; use a simple fragment identifier. |
| `title`, `eyebrow`, `description` | Empty | Plain text. Hero title/description inherit the content item. |
| `actions` | Empty array | `{label, path}` records. Both fields required; first button is primary. |
| `items` | Empty array | Feature/learn/workflow records with required `title`; optional `description`, `kicker`, `icon`, `path`. |
| Hero `image`, `image_alt` | Empty | Asset URL and alt text. Wide mode places media beside the title. |
| Content `body` | Page/section content | Optional authored Markdown string replacing that block's content. |

There can be one hero, in the first position. Without a hero, the layout adds
the page's `h1`. Other block titles use `h2` and nested card titles use `h3`.
Repeat or reorder other blocks freely. Include one `content` block when the
page's Markdown body should appear. Explicitly repeating it repeats the body.
All built-in blocks render without JavaScript.

`features` uses large cards. In wide mode, a features block without a heading
directly after the hero overlaps its lower edge. `learn` places its introduction
beside a compact two-column card grid. `workflow` places its introduction beside
a numbered vertical plan. `cta` uses an accent panel with its actions alongside
the text. These split layouts stack on narrow screens.

An item's `path` makes its card or workflow step a link with a visible keyboard
focus. Omit `path` for an item that only presents information. Without a block
title, item headings use `h2`.

Links resolve through the active Zola language and respect `base_url` subpaths.
Escape literal Tera in Markdown examples with a raw block, as with other content.

## Custom block

Override `partials/landing-block.html` and handle the custom type before falling
back to `partials/devlab/landing-block.html`. The slot receives `block`,
`block_id`, `home`, `home_layout`, `context_item` and `lang`. Unknown built-in
types and repeated ids fail the build. This keeps additions local to one slot.
