+++
title = "Template extensions"
description = "Extend template blocks or replace site partials."
weight = 2
updated = 2026-10-06
+++

Reach for a template only when configuration and CSS cannot express the change. Zola loads a site template before a theme template with the same path, so the site can extend a block or replace one partial without editing the installed DevLab copy.

## Choose an override

| The site needs... | Use... | What the site owns |
| --- | --- | --- |
| Metadata, CSS, JavaScript or an integration | A base-template hook | Only the added markup |
| Different content inside the built-in footer | `partials/footer-content.html` | Footer content, not its layout |
| A completely different shared component | One matching partial | That component and its behavior |
| A different structure for one page type | One layout block | That block's markup |

## Extend the base template

Create `templates/base.html` in the site. Keep only the blocks the project actually uses:

{% raw %}
```html
{% extends "devlab-theme/templates/base.html" %}

{% block head_extra %}
<meta name="application-name" content="Acme Docs">
{% endblock head_extra %}

{% block extra_styles %}
<link rel="stylesheet" href="{{ get_url(path='custom.css') }}">
{% endblock extra_styles %}

{% block extra_scripts %}
<script defer src="{{ get_url(path='js/custom.js') }}"></script>
{% endblock extra_scripts %}
```
{% endraw %}

Use `defer` for classic scripts that depend on theme controls. Deferred classic
scripts run in document order. Lazy search and diagram modules complete separately,
so integrations must wait for their own required controls. The slot controls tag
placement, not the completion of asynchronous work.

Put referenced files under the site's `static/` directory. `get_url` keeps their URLs correct when the site is deployed below a path prefix.

The base template exposes these focused hooks:

| Block | Position | Good fit |
| --- | --- | --- |
| `head_extra` | After DevLab metadata and `main.css` | Verification tags, metadata and preload hints |
| `extra_styles` | Last in the page head | Site stylesheets |
| `body_start` | After the skip link, before the site shell | Environment banners and accessibility integrations |
| `extra_scripts` | After DevLab scripts | Site JavaScript that uses rendered theme markup |
| `body_end` | Last before `</body>` | Integrations that must be the final body markup |

The structural `title`, `main_class` and `content` blocks belong to page layouts. Override them in a specific layout instead of changing the site-wide base wrapper.

## Override one partial

A site partial replaces the theme partial at the same relative path:

```text
your-site/
└── templates/
    └── partials/
        └── footer-content.html
```

`footer-content.html` is the smallest useful footer override. DevLab continues to own the separator, layout and compact Docs variant while the site controls the inner markup. The partial receives:

- `footer_text` and `footer_year`.
- `footer_note`.
- `footer_links` and `footer_links_label`.
- `footer_compact` for the Docs presentation.

`footer_compact` is true in the Docs sidebar and the mobile Docs panel, where the partial receives `footer_show_link_names = false`, so the note and copyright stay above a row of icon-only links regardless of the site setting.

Reuse `site-footer-note`, `site-footer-copyright`, `site-footer-links`, `site-footer-link` and `site-footer-link-mark` when you want to keep the built-in responsive styling.

You can also replace `partials/header.html` or `partials/footer.html`, but that is a complete component replacement. The site then owns its keyboard behavior, accessible names, responsive states and every runtime hook the component needs. A full footer override must handle `footer_compact`, because the same partial appears below regular pages, in the desktop Docs sidebar and in the mobile Docs panel.

### Bring a footer from another project

The built-in footer owns its wrapper, the `site-footer-*` classes and the variables listed above, so markup copied from another project cannot be dropped into `footer-content.html` unchanged. That partial is a convenience, not a requirement: replace `partials/footer.html` instead, which receives no required variable and lets the footer keep its own structure.

{% raw %}
```html
{% set footer_site = config.title %}
{% if footer_compact is defined and footer_compact %}
<p class="my-footer-min">{{ footer_site }}</p>
{% else %}
<footer class="my-footer">
  <p>&copy; {{ now() | date(format="%Y") }} {{ footer_site }}</p>
  <nav aria-label="Footer">
    <a href="{{ get_url(path='/') }}">Home</a>
    <a href="https://codeberg.org/you/project">Repository</a>
  </nav>
</footer>
{% endif %}
```
{% endraw %}

Every class in that file belongs to the site. Keep the compact branch when the site uses a Docs section, because the same file also fills the desktop Docs sidebar and the mobile Docs panel. Drop it when the site uses no reading layouts.

`devlab.footer` settings and `devlab.footer.show` only configure the built-in partial, so a replacement is not affected by them and does not need `footer_links`, `footer_note` or `footer_show_link_names`.

Header and navigation overrides must retain the `data-*` hooks used by `site-menu.js`, `search.js` and `theme.js`, or deliberately provide equivalent behavior. Use the documented [Extension API](@/docs/reference/extensions.md) for small slots
and runtime contracts. Undocumented internal markup can change between releases.

## Override one layout block

Suppose ordinary content pages need a project-specific header but should keep every other DevLab block. Create `templates/page.html`:

{% raw %}
```html
{% extends "devlab-theme/templates/page.html" %}

{% block content %}
<article class="content custom-page">
  <header class="page-header">
    <p class="hero-eyebrow">Acme</p>
    <h1>{{ page.title }}</h1>
  </header>

  {{ page.content | safe }}
</article>
{% endblock content %}
```
{% endraw %}

The same pattern works with `index.html`, `section.html`, `docs.html`, `doc-page.html`, `blog.html`, `blog-page.html`, `downloads.html`, `downloads-hub.html` and `downloads-product.html`. Extending the namespaced file keeps every untouched block connected to the installed theme.

Zola documents namespaced inheritance and site-template priority in [Customizing a theme](https://www.getzola.org/documentation/themes/extending-a-theme/).

## Theme updates

A copied template replaces the theme's file completely. When updating DevLab,
compare it with the new version to incorporate changes to markup, accessible
labels and script hooks. Extending a namespaced template limits this comparison
to the blocks your site replaces.

Use configuration for supported options and CSS for appearance. Keep template
overrides for changes to the page structure.
