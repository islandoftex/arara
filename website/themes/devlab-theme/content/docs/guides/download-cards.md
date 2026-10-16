+++
title = "Simple download cards"
description = "Publish release artifacts, unavailable channels and verification links."
weight = 10
updated = 2026-10-06
+++

The Downloads layout presents packages, installation images, source archives and verification details without adding a release API or client-side runtime. All release data stays explicit in the section front matter. Use this compact layout when each channel needs only one link. For a product with several files per channel, use [Single downloads](@/docs/guides/downloads.md).

## Set up the page

### Create the Downloads section

Downloads must be a Zola section because `downloads.html` reads section front matter. For locally hosted artifacts, create this structure:

```text
content/
  downloads/
    _index.md
static/
  releases/
    app-1.4.0-linux-x86_64.tar.gz
    SHA256SUMS
    SHA256SUMS.sig
```

The `static/releases/` directory is optional when every artifact is hosted externally. The `content/downloads/_index.md` section is always required.

Use `content/downloads/_index.md`, not `content/downloads.md`:

```toml
+++
title = "Downloads"
description = "Choose a build for your platform."
template = "downloads.html"

[extra.downloads]
eyebrow = "Downloads"
channels_label = "Download options"
unavailable_label = "Unavailable"
channels = [
  { name = "Linux x86_64", status = "Stable", description = "Portable release archive.", url = "/releases/app-1.4.0-linux-x86_64.tar.gz", button_label = "Download tarball" },
  { name = "Windows x86_64", status = "Planned", description = "This artifact has not been published yet." },
]

[extra.downloads.status]
label = "Current release"
title = "Version 1.4.0"
description = "The stable release for production installations."
items = [
  { label = "Published", value = "2026-07-15" },
  { label = "Architectures", value = "x86_64" },
  { label = "License", value = "MIT" },
]

[extra.downloads.verification]
label = "Verify"
title = "Checksums and signatures"
description = "Verify an artifact before installation."
links = [
  { label = "SHA256SUMS", url = "/releases/SHA256SUMS" },
  { label = "Signature", url = "/releases/SHA256SUMS.sig" },
]
+++

Choose a channel, then verify the downloaded file before installation.
```

The section `title` and `template = "downloads.html"` are required. Its `description` and Markdown body are optional. The layout reads `section.extra.downloads`. A global `config.extra.downloads` table in `zola.toml` is ignored.

Files under `static/releases/` are copied to `/releases/` during `zola build`. Replace the example filenames, release values and dates with real project data.

### Add the navigation link

Downloads is a regular navigation destination and does not need a special `kind`:

```toml
[extra.devlab.navigation]
links = [
  { name = "Home", path = "/" },
  { name = "Docs", collection = "manual" },
  { name = "Blog", path = "/blog/" },
  { name = "Downloads", path = "/downloads/" },
]
```

`links` defines the complete navigation array, so merge the Downloads entry into the site's existing list instead of discarding destinations you still need. The same entry appears in the desktop header and mobile menu. Local paths are resolved through Zola, including sites published below a URL subpath.

## Configure the layout

### Layout settings

All settings are section-scoped under `[extra.downloads]` in the Downloads section.

| Field | Type | Default | Purpose |
| --- | --- | --- | --- |
| `eyebrow` | String | `Downloads` | Short label above the page title. |
| `channels_label` | String | `Download options` | Visible and accessible heading for the channel grid. |
| `unavailable_label` | String | `Unavailable` | Disabled-button fallback for channels without a URL. |
| `channels` | Array of tables | Empty | Download cards rendered in the responsive grid. |

Omitting `extra.downloads` entirely leaves a clean hero containing the section title, description and Markdown body. Empty or omitted `channels` do not create an empty grid.

Empty `eyebrow`, `channels_label`, `unavailable_label`, `status.label` and `verification.label` values use their defaults. Empty strings do not hide those labels. Nested labels and descriptions are plain text. Markdown is rendered only from the section body below the front matter.

### Channel fields

Each entry in `channels` represents one artifact or release channel.

| Field | Required | Purpose |
| --- | --- | --- |
| `name` | Yes | Non-empty card heading and fallback label for an enabled button. |
| `status` | No | Compact label such as `Stable`, `Preview` or `Planned`. |
| `description` | No | Short explanation of the artifact or intended audience. |
| `url` | No | Local or remote artifact URL. Missing or empty means unavailable. |
| `button_label` | No | Custom action label. Falls back to `name` for enabled channels and `unavailable_label` for disabled channels. |

A channel with a non-empty `url` renders a primary link. A channel without one renders a native disabled button, which is not placed in the keyboard tab order:

```toml
channels = [
  { name = "Stable", url = "/releases/app.tar.gz", button_label = "Download" },
  { name = "Nightly", status = "Not published" },
]
```

Only `url` controls whether the action is enabled. A value such as `status = "Stable"` or `status = "Not published"` changes the visible label but never changes button behavior.

### URLs and files

Local paths, absolute HTTP(S) URLs and protocol-relative URLs use the same resolver as other theme links. The shared resolver also preserves `mailto:`, `tel:`, page fragments and query-only references, although download channels should normally use local static paths or HTTPS release storage.

Any non-empty channel URL renders a link even when the target does not exist. The theme does not check remote availability, add a `download` attribute or open links in a new tab. Whether the browser downloads or opens a file depends on the target and its server headers, including `Content-Disposition`.

Zola copies local `static/` files as-is. DevLab does not create archives, calculate sizes, derive versions or produce checksums and signatures.

### Release status

Add `[extra.downloads.status]` only when the page needs a release summary. The block is rendered when `title` is non-empty. Without it, the hero uses a single-column layout.

| Field | Required | Default | Purpose |
| --- | --- | --- | --- |
| `title` | Yes | — | Release name, version or availability statement. |
| `label` | No | `Release status` | Compact label above the title. |
| `description` | No | Empty | Short release summary. |
| `items` | No | Empty | Label/value metadata rows. |

Every `items` entry requires non-empty `label` and `value` strings:

```toml
items = [
  { label = "Version", value = "1.4.0" },
  { label = "Architectures", value = "x86_64, aarch64" },
]
```

### Verification links

The optional `[extra.downloads.verification]` block is rendered when `title` is non-empty. It can contain explanatory text, links, or only the heading.

| Field | Required | Default | Purpose |
| --- | --- | --- | --- |
| `title` | Yes | — | Verification heading. |
| `label` | No | `Verify` | Compact label above the heading. |
| `description` | No | Empty | Instructions for verifying artifacts. |
| `links` | No | Empty | Checksum, signature or verification-document links. |

Each verification link with a URL requires a non-empty `label`. An empty or
omitted URL is skipped. Empty entries do not create a verification body. The
title can stand alone when there is no description or usable link.

The theme displays links but does not generate hashes or signatures. Produce those files in the release workflow, place them beside the artifacts, and publish matching URLs in front matter.

## Troubleshooting

If Zola reports that `channel.name`, `item.label`, `item.value`, `link.label` or `link.url` is missing, check the corresponding table for a typo. Tera rejects missing required keys. Empty required values are also invalid configuration, but may instead produce incomplete UI, so remove or replace them before publishing.

If a channel displays `Unavailable`, add a non-empty `url` only after the artifact exists. If the entire Downloads page uses the generic section layout, confirm that `_index.md` contains:

```toml
template = "downloads.html"
```
