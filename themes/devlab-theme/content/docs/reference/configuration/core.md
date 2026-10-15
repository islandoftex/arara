+++
title = "Site settings and navigation"
description = "Zola configuration, header links and translated routes."
weight = 1
updated = 2026-10-06
+++

## Site settings

These fields belong in the site's `zola.toml`. Zola does not inherit them from
the theme's configuration. [Quick start](@/docs/getting-started/quick-start.md)
shows a complete initial file.

| Field | Purpose |
| --- | --- |
| `base_url` | Deployment address, including any path prefix; used for canonical and asset URLs. |
| `theme` | Set to `"devlab-theme"`. |
| `compile_sass` | Set to `true` to build the theme stylesheets. |
| `title` | Site name and default header/footer text. |
| `description` | Metadata fallback for content without its own description. |
| `default_language` | Language code for primary content; `"en"` in the minimal setup. |

Search settings are in [Search](@/docs/reference/configuration/search.md).
Feeds and code highlighting are in [Publishing](@/docs/guides/publishing.md).

## Theme settings

Optional theme settings live under `extra.devlab` and inherit defaults from
`theme.toml`. The navigation array replaces the default Home link.

After creating `content/docs/_index.md`, register its collection and add it to
navigation:

```toml
[extra.devlab.collections.manual]
section = "docs/_index.md"

[extra.devlab.navigation]
links = [
  { name = "Home", path = "/" },
  { collection = "manual" },
]
```

An ordinary link requires string `name` and `path`. A collection link requires
its registered ID. An optional `name` overrides the translated root title.
Unknown collection IDs and missing primary roots stop the build.

An empty `links` array removes global navigation. On Docs and Reader pages with
chapters, the mobile menu still opens the active collection's tree. Other pages
then have no menu button.

## Reading roots

`collections.<name>.section` is a source path relative to `content/`, not a
public URL. Zola supplies the root's permalink. Collection membership follows
source ancestry, even when a page has a custom URL.

Collections provide sidebar navigation and reading defaults. A page's selected
template determines its layout. See [Collections](@/docs/reference/collections.md)
for option inheritance and [Docs navigation](@/docs/guides/navigation.md) for
curated section order.

## Multilingual routes

Local navigation, landing actions, announcements and footer links include the
site's deployment prefix and active content language. Absolute HTTP(S) URLs,
protocol-relative URLs, `mailto:`, `tel:`, fragments and query-only references
are preserved. Download artifact URLs do not receive a language prefix.

Missing translated collection roots remove their navigation links. Ordinary
`path` links assume that the destination exists in the active language.

For local links inside content components, pass the language explicitly:

{% raw %}
```jinja
{% <devlab.card title="Documentation" href="@/docs/_index.md" content_lang={lang}> %}
Open the documentation.
{% </devlab.card> %}
```
{% endraw %}

Language codes, direction and interface labels are described in
[Languages](@/docs/reference/languages.md). Header and footer settings are in
[Appearance](@/docs/reference/configuration/appearance.md).
