+++
title = "Deploy a site"
description = "Build public output, upload it to a static host and verify the live site."
weight = 15
updated = 2026-10-06
+++

DevLab produces a static site. The host serves the generated `public/` directory.
It does not need Zola, Python, Node.js or the theme repository at runtime.

## 1. Set the final URL

In your site's `zola.toml`, set the complete deployment address:

```toml
base_url = "https://example.com/project/"
```

Include `/project/` when the site lives below that path. Template assets and
internal routes use this prefix. Keep site links built with `get_url` or the
DevLab route resolver. Hard-coded `/js/custom.js` would bypass the prefix.

Review the [Publishing](@/docs/guides/publishing.md) settings for metadata,
feeds, favicon, cache headers and CSP before building.

## 2. Restore the theme and build

For a site using a theme submodule:

```sh
git submodule update --init --recursive
zola check --skip-external-links
zola build
```

Use Zola 0.23.6 or newer. A deployment pipeline must restore the recorded
submodule revision instead of following its latest branch. `zola check`
validates Markdown links. It does not verify URLs stored in Download data or
site templates, so check published artifact and verification links separately.

`public/` is complete deployable output. Do not upload `content/`, theme source
or `zola.toml` as the website. Keep a previous successful output or commit for
rollback. A preview deployment can use `zola build --base-url` with its own URL.

## 3. Upload the output

Configure the static host's upload/publish directory as `public/`. For a
branch-based host, place the contents of that directory at the publishing
branch root. Do not add a second `public/` directory around the output.

Serve directory `index.html` files for clean routes such as `/blog/`. Configure
the host to use the generated `404.html` for missing paths. Host setup differs,
so follow its current instructions in Zola's
[deployment documentation](https://www.getzola.org/documentation/deployment/overview/).
The theme repository's Woodpecker workflow publishes its own demonstration.
It is not a required pipeline for your site.

## 4. Verify the published site

Check the root and one nested page, then a route that does not exist. Inspect
canonical URLs, `main.css`, `print.css`, favicon, custom assets and feeds.
Open search on each indexed language and download a real artifact. Its
checksum/signature links must match that file.

At a mobile width, open the menu and use Ctrl+K. Print an article containing
multiple tabs and confirm that every variant is included. Check a changed
stylesheet after redeploying to detect stale caches.

If the live site fails, restore the previous output or previous site commit,
rebuild with its recorded theme revision and deploy again.
