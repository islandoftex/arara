+++
title = "Troubleshooting"
description = "Find missing settings, incorrect layouts and unavailable controls."
weight = 9
updated = 2026-10-06
+++

Run `zola check --skip-external-links` and `zola build` from the site root.
Use the failing source path in Zola's error to locate the configuration or
content file. A successful build can still contain incorrect settings.
The checks below cover common symptoms.

## Pages use the wrong layout

Set `template` on a section and `page_template` for its descendants:

```toml
+++
title = "Documentation"
template = "docs.html"
page_template = "doc-page.html"
+++
```

A nearer section's `page_template` or a page's own `template` takes precedence.
Blog sections use `blog.html` and `blog-page.html`. Books use `reader.html`
for both. See [layout selection](@/docs/guides/architecture.md#layout-selection).

## A sidebar or subsection is missing

Register the root in `zola.toml`:

```toml
[extra.devlab.collections.manual]
section = "docs/_index.md"
```

`content/docs/_index.md` must exist. Nested navigation groups also need an
`_index.md` file. A translated tree needs translated section files, such as
`_index.de.md`. Zola does not create them from the primary language.

Check a curated `navigation` list for omitted sections. Entries must stay
inside the collection and cannot repeat or overlap. With `sort_by = "weight"`,
every page needs a weight. With `sort_by = "date"`, every post needs a date.
Zola excludes pages that lack the selected sorting field.

The mobile tree requires a Docs or Reader layout inside a registered collection.
See [Docs navigation](@/docs/guides/navigation.md) for the complete setup.

## Search is missing or returns no results

Enable `build_search_index = true` and set
`search.index_format = "elasticlunr_javascript"`. Leave
`extra.devlab.search.enabled` enabled. Other index formats hide the control.

For each translated language, enable its own `build_search_index`. Verify that
`public/search_index.<language>.js` exists and its request succeeds in the
browser. The default modules cover English, German and Russian. Other languages
need matching modules. See [Search](@/docs/reference/configuration/search.md).

## Blog authors are missing

Register `{ name = "authors" }` in Zola's `taxonomies` and put author keys in
the post's `[taxonomies]` table. Each profile key must match a table in
`data/authors.toml`. A missing profile displays the key as a linked name.
Translated languages need their own author taxonomy declaration.
See [Blog authors](@/docs/guides/blog-authors.md).

## A notice is missing or keeps returning

The banner needs `show = true` and a non-empty `message` in
`extra.devlab.announcement`. Its close button also needs `dismissible = true`
and a non-empty `key`.

A saved dismissal hides the notice while its key remains the same. Change the
key to identify a new announcement. If a closed notice returns after navigation,
check that the key stays unchanged and persistent storage is permitted.
See [Release announcements](@/docs/guides/announcements.md).

## Downloads show “Not published”

Add an artifact `url` only when the file exists. A maturity `status` alone does
not enable a download. `demo = true` disables artifact and verification links.
See [Downloads data](@/docs/reference/downloads.md#artifacts).

## A feed or sharing image is missing

For feeds, enable `generate_feeds = true`, set `feed_filenames = ["atom.xml"]`
and add dates to feed entries. Check the output file after building.

For sharing images, set `extra.devlab.seo.og_image` to an existing static path
or absolute URL. An empty value omits image tags. Use the deployment `base_url`
when checking canonical URLs and social metadata. See [Publishing](@/docs/guides/publishing.md).

## Code colors or cached assets are wrong

Set `light_theme` and `dark_theme` under `[markdown.highlighting]`.
Check both palettes after changing styles. Override a token in a stylesheet
loaded after `main.css`. See [Brand and colors](@/docs/customization/branding.md).

If a deployment still uses previous CSS or scripts, inspect the response cache
headers. Theme asset names are stable across revisions. See
[cache policy](@/docs/guides/publishing.md#cache-policy).
