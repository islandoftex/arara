+++
title = "Download catalog"
description = "Offer an optional product catalog with filters and individual release pages."
weight = 9
updated = 2026-10-06
+++

Use Catalog for a family of applications, devices or installation targets. Visitors first choose a product, then download its files from a dedicated page. A single product usually needs only [Single downloads](@/docs/guides/downloads.md).

The [working catalog example](@/examples/downloads-catalog/_index.md) lives outside the main demo navigation. It demonstrates search, support filters, vendor selection, sorting, an unavailable artifact and a product with no files.

## Create the catalog and product pages

```text
content/
  downloads/
    _index.md
    example-app.md
    example-device.md
data/
  downloads.toml
```

The catalog `_index.md` selects `downloads-hub.html` and the data file:

```toml
+++
title = "Product downloads"
description = "Choose your product."
template = "downloads-hub.html"

[extra]
data = "data/downloads.toml"
+++
```

Each product is a regular page. Create `content/downloads/example-app.md`:

```toml
+++
title = "Example App"
template = "downloads-product.html"

[extra]
data = "data/downloads.toml"
product = "example-app"
+++

Installation notes for this product.
```

`extra.product` must match exactly one product `id` in the same data file. The detail page uses its parent section for the breadcrumb. Put product pages directly inside the catalog section. The page title and description also provide search and social metadata. Keep them consistent with the product.

## Define the catalog data

```toml
mode = "catalog"
title = "Product downloads"
description = "Choose your product to see available releases."

[[channel]]
id = "stable"
label = "Stable"

[[support]]
id = "maintained"
label = "Maintained"
tone = "success"

[[product]]
id = "example-app"
name = "Example App"
vendor = "Example Project"
support = "maintained"
summary = "A command-line application."
tags = ["CLI", "Linux"]
url = "@/downloads/example-app.md"

[[product.artifact]]
channel = "stable"
name = "Linux archive"
status = "planned"
```

Add another product by repeating `[[product]]`, its optional artifacts, and the corresponding content page. Product `url` is explicit: use `@/` links so Zola detects missing content instead of silently constructing an incorrect route. Translated catalogs should point to their translated product files.

All channel, verification and artifact fields work the same as Single. Root verification instructions and mirrors are defaults. Use them only when
they are shared. Override a product's verification fields and mirror list for
independent release servers or signing keys.


For example, put this after the matching `[[product]]` record and before the
next product:

```toml
[product.verification]
commands = "sha256sum -c toolkit.sha256"
key_url = "https://example.com/toolkit/key.asc"

[[product.mirror]]
name = "Toolkit releases"
url = "https://example.com/toolkit/releases/"
```

The product mirror list replaces shared mirrors. `mirror = []` directly inside
`[[product]]` disables them for an unpublished product. Verification fields
merge with root defaults. Set an inherited `commands` or `key_url` to an empty
string to clear it. Catalog cards count only files with enabled download URLs.

## Feature selected products

Add a root `[featured]` table **before the product tables**, then mark individual products with `featured = true`:

```toml
[featured]
title = "Start here"
description = "Recommended products for new users."
```

Featured cards are shortcuts to the same product pages. The main catalog below remains complete. Omit `[featured]` for a simpler page.

## Filtering and accessibility

The catalog downloads no remote index and needs no API. Search matches product names, vendors, tags and summaries. Support and vendor filters combine with the search. Sorting can follow the data file, name or the order of the declared support levels. The result count is announced to assistive technology.

The vendor options and the support counts are rendered while the page builds, so the toolbar already describes the catalog before scripts load. Product pages list download locations as a mirror grid and keep their verification instructions in the same card layout.

**Reset filters** clears all controls and focuses the search field, and **All products** in the featured strip does the same before it moves to the grid. Escape in the search field clears only the query. Filters appear only when JavaScript initializes. Without JavaScript, all product links remain visible and usable. Release channel tabs follow the same progressive enhancement rule and remember the selected channel across product pages.

Keep support labels meaningful and consistent. Products can omit a support level or vendor, but those products will only match the unrestricted filter for that field.

Use the [Downloads data reference](@/docs/reference/downloads.md) for required fields, defaults and build-time validation. Keep the [simple cards layout](@/docs/guides/download-cards.md) if the site does not need this structure.
