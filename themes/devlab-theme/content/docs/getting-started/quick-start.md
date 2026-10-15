+++
title = "Quick start"
description = "The configuration and content needed for a first build."
weight = 2
updated = 2026-10-06
+++

[Install DevLab](@/docs/getting-started/installation.md) first. Run the commands
below from the new site's directory, not from the theme repository.

## Site configuration

Create `zola.toml` in the site root:

```toml
base_url = "https://example.com"
title = "My site"
description = "Project notes."
default_language = "en"
theme = "devlab-theme"
compile_sass = true
build_search_index = false
generate_feeds = false

[markdown.highlighting]
light_theme = "github-light"
dark_theme = "github-dark"
```

Set `base_url` to the deployment address before publishing. `title` supplies
the header and footer name. `description` is the metadata fallback for pages
without their own description. Set `default_language` to the language of your content.

Zola settings belong in this file. Optional `extra.devlab` settings inherit from
`theme.toml`. Omit fields whose defaults fit the site. Search and feeds are
disabled in this configuration.

## First page

Create the content directory:

```sh
mkdir -p content
```

Create `content/_index.md`:

```md
+++
title = "Home"
+++

## Project notes

This site uses DevLab and Zola.
```

The default homepage renders the front-matter title and the Markdown body.
Change them independently of the site's title in `zola.toml`.

## Check and preview

```sh
zola check --skip-external-links
zola build
zola serve
```

`zola build` writes the deployable site to `public/`. Open the local address
reported by `zola serve`. If another preview is running, choose a free port,
for example `zola serve --port 1112`.

## Add content

| Content | Read next |
| --- | --- |
| Ordinary pages and directories | [Content layouts](@/docs/guides/content-layouts.md) |
| Documentation with a sidebar | [Docs navigation](@/docs/guides/navigation.md) |
| Dated articles | [Create a blog](@/docs/guides/blog.md) |
| Book chapters | [Reader](@/docs/reference/reader.md) |
| Landing sections | [Landing blocks](@/docs/reference/landing.md) |
| Site search | [Search configuration](@/docs/reference/configuration/search.md) |
| Publish the site | [Deployment](@/docs/guides/deployment.md) |
