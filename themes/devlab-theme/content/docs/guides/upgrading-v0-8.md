+++
title = "Upgrade from v0.8 to v0.9"
description = "Move retired homepage and reading settings to blocks and collections."
weight = 14
updated = 2026-10-06
+++

v0.9 changes the configuration API. Update the site files before switching the
installed theme. A v0.8 configuration is not compatible with v0.9.

## 1. Preserve the working site

Commit the site configuration, content and template overrides. Record the theme
revision with `git -C themes/devlab-theme rev-parse HEAD`. Check for uncommitted
theme edits and move those changes into site-owned files first.

## 2. Replace retired settings

Apply this map in `zola.toml` and homepage front matter:

| v0.8 setting | v0.9 replacement |
| --- | --- |
| `extra.home_layout` | `extra.devlab.home.layout` |
| `home_eyebrow` | `eyebrow` on the first `hero` block |
| `home_hero_image`, `home_hero_image_alt` | Hero `image`, `image_alt` |
| `home_primary_action_*`, `home_secondary_action_*` | Hero `actions = [{ label, path }]` |
| `home_features` | A `features` block with `items` |
| `home_learn.cards` | A `learn` block with `items` and its existing heading fields |
| `home_workflow.points` | A `workflow` block with `items` |
| `home_cta` | A `cta` block with `title`, `description` and `actions` |
| `devlab.docs.section` | `devlab.collections.<id>.section` |
| `devlab.docs.path`, `get_started_path` | Explicit navigation and landing action paths |
| Navigation `kind` | A `collection` link or ordinary `name` and `path` |
| `devlab.blog.path` | Ordinary navigation path and Blog templates |
| `extra.labels` | `extra.devlab.i18n.translations.<language>` |
| `extra.search_label`, `search_placeholder` | Translation keys `search`, `search_placeholder` |

Remove replaced keys. v0.9 stops the build when these retired keys remain, even
when their values are empty. Custom `extra` fields with other names are allowed.

For example, this v0.8 configuration:

```toml
[extra]
home_layout = "wide"
home_primary_action_label = "Install"
home_primary_action_path = "/manual/install/"

[extra.devlab.docs]
section = "manual/_index.md"

[extra.devlab.navigation]
links = [{ name = "Manual", path = "/manual/", kind = "docs" }]
```

becomes:

```toml
[extra.devlab.home]
layout = "wide"
blocks = [
  { type = "hero", actions = [{ label = "Install", path = "/manual/install/" }] },
  { type = "content" },
]

[extra.devlab.collections.manual]
section = "manual/_index.md"

[extra.devlab.navigation]
links = [{ collection = "manual" }]
```

Keep the `content` block to retain the homepage Markdown. Transfer existing
`docs.navigation` entries into the corresponding collection. Shared reading
settings and `docs.page_actions` can stay under `extra.devlab.docs`.

## 3. Review layouts and overrides

Keep `template = "docs.html"` and `page_template = "doc-page.html"` on manual
sections. Register each independent root as a collection. A URL under `/docs/`
does not select the reading layout by itself.

Compare copied templates with v0.9. Prefer the documented
[template slots](@/docs/reference/extensions.md) for small changes. Site scripts
should use `defer` when they depend on theme controls. Theme scripts now keep
private names out of the global scope.

Download cards using `downloads.html` keep their separate front-matter contract.
Single and Catalog use explicit `extra.data` files. See the
[data reference](@/docs/reference/downloads.md) before moving between those layouts.

## 4. Switch the theme and verify

Select a published revision using [Install and update](@/docs/getting-started/installation.md).
Then run:

```sh
zola check --skip-external-links
zola build
zola serve
```

Check the homepage actions, each manual's sidebar and Previous/Next links,
Blog tags and authors, Download files, translated routes and your overrides.
Open the mobile menu, use Ctrl+K and print a page with tabs. All tab variants
must appear in the print result.

Commit the updated site and theme pointer together. To roll back, restore that
site commit and run `git submodule update --init --recursive`.
