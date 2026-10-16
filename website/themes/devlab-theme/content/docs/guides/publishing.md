+++
title = "Publishing"
description = "Set up metadata, feeds, favicon, 404 and syntax highlighting."
weight = 13
updated = 2026-10-06
+++

Set the deployment URL and metadata before publishing. Configure feeds and host policies for the features your site uses.
Then follow [Deploy a site](@/docs/guides/deployment.md) to build and publish the output.

## SEO metadata

The theme writes the common tags for search and sharing:

- canonical URL
- page description
- Open Graph title, description, type and URL
- Twitter Card title and description
- theme color for light and dark mode

Titles and descriptions come from the current page or section first. If those are missing, the theme falls back to `config.title` and `config.description`.

Set an image only when you have one:

```toml
[extra.devlab.seo]
og_image = "/images/social-preview.png"
og_image_alt = "Preview of the project"
```

If `og_image` is empty, image tags are not rendered. Page and section overrides,
content types and language alternates are described in
[Languages and metadata](@/docs/reference/languages.md#metadata).

## Atom feed

DevLab uses Zola's built-in feed generator. Enable it in `zola.toml`:

```toml
author = "Your name"
generate_feeds = true
feed_filenames = ["atom.xml"]
```

When feeds are enabled, the theme adds an autodiscovery link for each configured filename. `atom.xml` uses the Atom MIME type and `rss.xml` uses the RSS MIME type. Custom filenames omit the optional type hint. Set `author` so generated entries do not fall back to `Unknown`. Most RSS readers can subscribe to Atom feeds without extra setup. There is no custom feed template in the theme.

To exclude a page from its section feed:

```toml
+++
title = "Internal release note"
include_in_feeds = false
+++
```

This is useful for utility pages or updates that should remain published without being delivered to subscribers.

## Favicon

The theme ships a small SVG favicon:

```text
static/favicon.svg
```

It is linked from the page head as an SVG icon. Add PNG or ICO variants only if your project needs them.

## 404 page

The theme includes `templates/404.html`. It uses the same header, footer and theme colors as the rest of the site.

If search is enabled, the page also points visitors back to the header search field.

## Syntax highlighting

Use Zola's dual theme highlighting so code blocks match the color mode:

```toml
[markdown]

[markdown.highlighting]
light_theme = "github-light"
dark_theme = "github-dark"
```

The theme adds the code block frame, language label and Copy button styling. Syntax colors still come from Zola.

## Cache policy

DevLab uses stable asset names such as `main.css`, `print.css` and `js/search.js`.
An updated theme can change their contents without changing those URLs. Configure
HTML and these assets to revalidate on the host, for example with:

```text
Cache-Control: no-cache
```

This permits storage while requiring validation before reuse. Reserve long-lived
`immutable` caching for content-hashed or versioned URLs that never change. If a
host cannot expose custom headers, verify its default cache policy and invalidate
its cache as part of deployment. An installed Git revision does not itself change
browser asset URLs.

## Content Security Policy

The built-in color initializer and lazy search loader contain inline scripts.
For a strict `script-src` policy, derive hashes from the **final rendered HTML**,
include those hashes alongside `'self'`, and regenerate them on every deployment.
Search loader contents vary by base URL and language. Account for any site-owned
integrations through the same process. A copied hash from another build is not a
stable configuration value.

Test search, color initialization and diagrams under the actual host policy.
Diagram SVG can contain inline styles, so script hashes alone do not define its
style policy. CSP headers and their build integration belong to the consuming
site/host. DevLab does not inject a universal policy into content pages.
