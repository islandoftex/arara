+++
title = "Downloads data"
description = "Single and Catalog data fields, labels and validation."
weight = 6
updated = 2026-10-06
+++

Start with [Single downloads](@/docs/guides/downloads.md) or [Catalog](@/docs/guides/download-catalog.md). Both read an explicit TOML file selected by `extra.data` in section or page front matter. They do not read global `config.extra.downloads`.

| Template | Content | Required front matter |
| --- | --- | --- |
| `downloads-hub.html` | Section (`_index.md`) | `title`, `extra.data` |
| `downloads-product.html` | Page inside a catalog | `title`, `extra.data`, `extra.product` |
| `downloads.html` | Section with simple download cards | Separate [front-matter contract](@/docs/guides/download-cards.md) |

## Root fields

| Field | Default | Meaning |
| --- | --- | --- |
| `mode` | `single` | `single` requires exactly one product; `catalog` lists products and links to their pages. |
| `title` | Section title | Catalog heading. Single and detail headings use the product name. |
| `eyebrow` | Empty | Short text above the heading. |
| `description` | Section description in Single; empty in Catalog | Page lead. |
| `intro` | Empty | Markdown below the lead. |
| `actions` | Empty | Hero actions: required `label` and `url`, optional `style` (`primary` or `secondary`). |
| `channel` | Empty | Ordered release channels; each needs a unique non-empty `id` and `label`, with optional `description`. |
| `support` | Empty | Support levels with `id`, `label`, optional `tone`. Their order also controls support sorting, and each level becomes a filter pill with a build-time count. |
| `product` | Empty | Product records described below. |
| `featured` | Omitted | Optional Catalog shortcuts: `title`, `label`, `description`, `see_all_label`. Only products marked `featured = true` appear here. |
| `verification` | Omitted | Shared instructions, commands and signing-key link. |
| `mirror` | Empty | Shared download locations; each requires `name` and `url`. |
| `labels` | Active language catalog | Data-file overrides for interface labels. |

TOML placement matters: root values and tables such as `[verification]` are separate from `[product.status]`. A field written under the wrong table will not configure the intended block. Keep root scalar values before any tables.

## Products

Each `[[product]]` has these fields:

| Field | Required | Meaning |
| --- | --- | --- |
| `id` | Yes | Unique non-empty key matched by `extra.product`; not an inferred URL. |
| `name` | Yes | Product heading. |
| `url` | Catalog only | Product detail route; prefer a Zola `@/` content link. |
| `vendor` | No | Vendor label and Catalog filter value. |
| `support` | No | A declared support ID, used for the badge and Catalog filters. |
| `summary` | No | Catalog card summary and detail-page lead. |
| `tags` | No | Array of plain-text labels, also searched by Catalog. |
| `image` | No | Local or remote product image, shown as a wide preview beside the product heading. Without one, a neutral file icon keeps the frame. Images are decorative because the adjacent heading names the product. |
| `docs_url`, `docs_label` | No | Documentation link and optional label (defaults to `labels.docs`). |
| `featured` | No | Include in Catalog's featured shortcuts when `[featured]` exists. |
| `status` | No | Information block with optional `label`, `title`, `description`, and `items = [{ label, value }]`. It renders as a card section with one card per item, above verification and mirrors. |
| `verification` | No | Field overrides for root verification. Empty commands or key URL clear that inherited field. |
| `mirror` | No | Replaces root mirrors. An empty array disables shared mirrors for this product. |
| `artifact` | No | Ordered files, grouped by the declared channel order. The selected channel is remembered across product pages. |

IDs are data keys, not CSS classes. Do not use `all` as a support ID or vendor name: that value is reserved by the unrestricted Catalog filters. Product pages use their own front-matter title and description for metadata, even when the visible heading comes from the data file.

## Artifacts

Each `[[product.artifact]]` requires `channel` and a non-empty `name`. It describes a package, source archive or installation image.

| Field | Meaning |
| --- | --- |
| `channel` | Must match a declared channel `id`. Unknown channels stop the build instead of hiding files. |
| `name`, `variant` | Package heading and optional format/edition beside it. |
| `version`, `date`, `arch`, `size`, `kernel` | Optional plain-text metadata. `kernel` is useful for installation images. Values are displayed as supplied. |
| `status` | `ready`, `development` or `planned`; defaults to `planned`. |
| `url` | Download target. Missing/empty means unavailable. |
| `recommended` | Shows a recommendation only when the download is enabled. |
| `demo` | If true, suppresses download/checksum/signature/torrent links and adds an Example label. Defaults to false. |
| `sha_url`, `sig_url`, `torrent_url` | Optional companion links. Rendered only when the artifact's download is enabled. |
| `release_notes` | Optional release-notes route; can remain available for an unpublished artifact. |

A non-empty `url` enables the link unless `demo = true`. Status does not gate downloads. The Available badge additionally requires `status = "ready"`. Unsupported status values should be corrected rather than used for custom labels. Customize `[labels]` instead.

Links use DevLab's URL resolver: local paths include the deployment subpath, `@/` links resolve content, and absolute HTTPS links stay unchanged. The theme does not add a `download` attribute, fetch release APIs, verify remote files or calculate metadata.

## Verification and mirrors

`[verification]` accepts `label`, `title`, `description`, `commands`, `key_url`, `key_label`, `sha_label` and `sig_label`. Commands render as escaped code with a copy button. The other fields are plain text except the URLs. `sha_label` defaults to `SHA256`. `sig_label` defaults to `labels.signature`.

Each `[[mirror]]` needs `name` and `url`. Root mirrors and verification are defaults for products in the data file.
`[product.verification]` overrides individual verification fields.
`[[product.mirror]]` replaces the root mirror list. Use `mirror = []` in a product
record to remove shared mirrors. Omit them if there are no real instructions or published locations. Root instructions are not a substitute for matching each artifact with its own checksum and signature URL.

A verification table containing only checksum/signature labels does not render
an empty instructions card. A card requires description, commands or a key URL.
Catalog file counts include only enabled download URLs and exclude demo records.
Download and verification actions include architecture and version in their accessible names.

Both blocks render as full-width card sections below the product: verification keeps its heading and command block together, and mirrors wrap into a responsive grid of links with the visible URL beside each name. The command block stays inside a `.content` wrapper, which is what attaches the copy button.

## Labels

All labels below can be overridden under `[labels]` in the data file. Omitted keys use the active language’s `extra.devlab.i18n.translations.<language>.downloads` catalog, then English defaults. Product names, channel labels and descriptions are authored data. Translate them in the data file.

| Keys | English defaults |
| --- | --- |
| `downloads`, `artifacts`, `channels` | Downloads; Downloads; Release channels |
| `download`, `unavailable`, `no_artifacts` | Download; Not published; No files have been published for this product yet. |
| `ready`, `development`, `planned` | Available; In development; Planned |
| `recommended`, `demo`, `release_notes` | Recommended; Example; Release notes |
| `kernel`, `version`, `date`, `arch`, `size` | Kernel; Version; Published; Architecture; Size |
| `signature`, `docs`, `files` | Signature; Documentation; Files |
| `status`, `status_title` | Status; Project status |
| `verification`, `verification_title`, `key` | Verification; Verify your download; Public signing key |
| `mirrors`, `mirrors_title` | Mirrors; Other download locations |
| `catalog`, `featured`, `featured_title`, `all_products` | Product catalog; Featured; Selected products; All products |
| `search`, `search_placeholder` | Search; Name, vendor or tag |
| `support`, `vendor`, `all` | Support level; Vendor; All |
| `sort`, `sort_default`, `sort_name`, `sort_support` | Sort; Default order; Name; Support level |
| `reset` | Reset filters |
| `results` | Showing {count} of {total} products |
| `no_results` | No products match. Change the search or reset the filters. |

Preserve `{count}` and `{total}` in `results`. The filter script replaces them with the current count. Sorting uses the page language. The control labels do not imply translation of your data.

## Validation

The build rejects unknown modes, duplicate product/channel IDs, missing product IDs or names, missing Catalog product URLs, unknown artifact channels, and missing artifact names, and incomplete actions or mirrors. Required names and
labels cannot be whitespace-only. A detail page must match exactly one product. Single must contain exactly one product.

Zola's normal template errors report missing required keys or malformed data.
Artifact and verification URLs come from the site's data. DevLab does not
query a release service or generate those files.
