+++
title = "Downloads"
description = "Publish a single product with release channels, files and verification instructions."
weight = 8
updated = 2026-10-06
+++

The [Downloads demo](@/downloads/_index.md) uses **Single**: one product, tabs for release channels, and a list of files with version and verification links. Choose **[Catalog](@/docs/guides/download-catalog.md)** when readers must first select a product. Both layouts use the same data format. For a few independent buttons, the existing [simple download cards](@/docs/guides/download-cards.md) layout remains available.

## Create the page

Create `content/downloads/_index.md`:

```toml
+++
title = "Download Example App"
description = "Choose a release for your platform."
template = "downloads-hub.html"

[extra]
data = "data/downloads.toml"
+++
```

The page is a **section**, so use `_index.md`. `extra.data` points to a TOML file in your site, relative to the site root. No global Downloads setting is required.

Create `data/downloads.toml`:

```toml
mode = "single"
eyebrow = "Downloads"
description = "Choose a release for your platform."

[[channel]]
id = "stable"
label = "Stable"
description = "Tested releases for everyday use."

[[channel]]
id = "preview"
label = "Preview"
description = "Upcoming builds for testing."

[[product]]
id = "example-app"
name = "Example App"
tags = ["Linux", "Open source"]

[[product.artifact]]
channel = "stable"
name = "Linux archive"
variant = "tar.gz"
arch = "x86_64"
status = "planned"

[[product.artifact]]
channel = "preview"
name = "Preview archive"
status = "development"
```

This is a working unpublished-release state: no fake URL is needed. Single requires exactly one `[[product]]`. Channel order controls tab order. Only channels with artifacts appear. Without JavaScript, all channel sections stay visible with their headings.

Add `{ name = "Downloads", path = "/downloads/" }` to the existing `[extra.devlab.navigation].links` array in `zola.toml`. Keep your other navigation entries.

## Publish a file

Put your release archive under `static/releases/` or use a real HTTPS download location. Then update its artifact entry:

```toml
[[product.artifact]]
channel = "stable"
name = "Linux archive"
variant = "tar.gz"
version = "1.0.0"
arch = "x86_64"
status = "ready"
recommended = true
url = "/releases/example-app-1.0.0-linux.tar.gz"
sha_url = "/releases/example-app-1.0.0-linux.tar.gz.sha256"
sig_url = "/releases/example-app-1.0.0-linux.tar.gz.asc"
```

Publish only the links whose files exist. An artifact without a URL displays **Not published**. `demo = true` also disables download and verification links, even if a URL was supplied. `status` describes maturity. It does not determine whether the file link exists. The **Available** badge requires `status = "ready"` and an enabled download.

A `.sha256` file and a signature must come from your release workflow. The theme never generates, validates or invents them. File sizes, dates and versions are optional explicit data.

## Add release information

Put `[product.status]` after the product definition to show compatibility requirements or project status:

```toml
[product.status]
title = "Before you install"
items = [
  { label = "System", value = "Linux x86_64" },
  { label = "Installation", value = "Extract the archive and run the executable" },
]
```

Use a root `[verification]` table for instructions shared by the product:

```toml
[verification]
title = "Verify the downloaded archive"
description = "Use the checksum and signature published alongside this release."
commands = "sha256sum -c example-app-1.0.0-linux.tar.gz.sha256\ngpg --verify example-app-1.0.0-linux.tar.gz.asc example-app-1.0.0-linux.tar.gz"
```

The command block has the theme's copy button. Replace these commands with the actual filenames and signing procedure for your project. Add `key_url` only when you publish a signing key and document how users can verify its fingerprint.

You can also add Markdown beneath the section front matter for installation notes. It appears after the release blocks. `intro` in the data file accepts Markdown. Other text fields are plain text.

## Translate and validate

Controls use the active language catalog. Each data file can override individual keys with `[labels]`. Use translated Zola sections with their own `extra.data` files when products or descriptions differ by language. See the [data reference](@/docs/reference/downloads.md#labels) for every label and default.

```toml
[labels]
download = "Herunterladen"
unavailable = "Noch nicht veröffentlicht"
channels = "Release-Kanäle"
```

Run `zola check` and `zola build`. Check the page at desktop and mobile widths, switch channels using the keyboard, and open every published file, checksum and signature. Local URLs include the site's `base_url` prefix. Download responses and filenames are controlled by the hosting server.

If the build reports an unknown channel, match each artifact's `channel` to a declared channel `id`. If Single reports the wrong product count, keep exactly one `[[product]]` or switch to [Catalog](@/docs/guides/download-catalog.md).

See the [full data reference](@/docs/reference/downloads.md) for product images, support badges, mirrors and optional fields.
