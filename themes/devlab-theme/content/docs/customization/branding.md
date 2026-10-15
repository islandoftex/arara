+++
title = "Brand and colors"
description = "Give DevLab your project's name, logo, footer, palette and typography."
weight = 1
updated = 2026-10-06
+++

Start with configuration. It covers the visible identity in the header and footer. Add a small site stylesheet only when the project needs its own colors, fonts or content widths.

## Change the name and logo

Set the site title first. DevLab uses it in metadata and as an accessible fallback when a logo mark has no visible text.

```toml
title = "Acme Docs"

[extra.devlab.brand]
logo_text = "Acme"
footer_text = "Acme Engineering"
show_logo_mark = true
logo_mark_path = "/brand/acme-mark.svg"
```

Put the image in the site, not in the installed theme:

```text
static/
└── brand/
    └── acme-mark.svg
```

Local asset paths pass through Zola, so they also work when the site is deployed below a path prefix. The header renders the mark at `32 × 32` pixels and treats it as decorative. `logo_text`, or `config.title` in a mark-only header, gives the home link its accessible name.

Choose the smallest identity that fits the project:

| Result | Configuration |
| --- | --- |
| Custom mark and project name | Set `logo_mark_path` and `logo_text` |
| Built-in DevLab mark and project name | Leave `logo_mark_path` empty and keep `show_logo_mark = true` |
| Text-only header | Set `show_logo_mark = false` |
| Mark-only header | Set `logo_text = ""`; keep a descriptive site `title` |

## Build the footer

Footer settings cover the usual project information without a template override:

```toml
[extra.devlab.brand]
footer_text = "Acme Engineering"

[extra.devlab.footer]
note = "Documentation for the Acme platform."
links_label = "Acme links"
links = [
  { name = "Codeberg", path = "https://codeberg.org/acme/docs", icon = "codeberg" },
  { name = "Contact", path = "mailto:docs@example.org", icon = "email" },
  { name = "Privacy", path = "/privacy/" },
]
```

Supported icons are `github`, `gitlab`, `codeberg`, `matrix`, `zulip`, `telegram`, `discord`, `rss`, `email` and `link`. A link without one of these icons keeps its visible name. The regular footer uses left, center and right regions. Narrow screens and the Docs footer stack the same content into one centered column.

If the footer needs arbitrary markup, override only `templates/partials/footer-content.html`. DevLab will still own the outer footer, separator and compact Docs layout. See [Override one partial](@/docs/customization/templates.md#override-one-partial) before taking that step.

## Add one site stylesheet

Load the site stylesheet after DevLab's `main.css` to override its tokens.

{% <devlab.steps> %}
1. **Create `static/custom.css` in the site.**

   Start with one small change so it is obvious that the file is loading:

   ```css
   :root {
     --color-accent: #6d28d9;
   }
   ```

2. **Create `templates/base.html` in the site.**

   {% raw %}
   ```html
   {% extends "devlab-theme/templates/base.html" %}

   {% block extra_styles %}
   <link rel="stylesheet" href="{{ get_url(path='custom.css') }}">
   {% endblock extra_styles %}
   ```
   {% endraw %}

3. **Check the result.**

   ```bash
   zola check
   zola serve
   ```

   Open a page and switch between light, dark and system modes before adding the rest of the palette.
{% </devlab.steps> %}

The resulting site-owned files are easy to see during an update:

```text
your-site/
├── static/
│   └── custom.css
└── templates/
    └── base.html
```

The `extra_styles` block appears after the theme stylesheet, so a token or selector with the same specificity wins without `!important`.

{% <devlab.callout type="warning" title="Do not shadow main.css"> %}
Do not create `sass/main.scss` or `static/main.css` in the site. Those paths replace DevLab's complete stylesheet. If you prefer Sass, use a distinct entrypoint such as `sass/site.scss`. Zola compiles it to `site.css`, which you can load through the same `extra_styles` block.
{% </devlab.callout> %}

## Change colors safely

Define a light palette, a system-dark fallback and an explicit dark palette. This preserves the right colors when JavaScript is disabled and when the visitor changes the theme manually.

```css
:root,
:root[data-theme="light"] {
  --color-accent: #6d28d9;
  --color-accent-contrast: #ffffff;
  --color-surface: #faf8ff;
  --color-border: #e7e0f3;
}

@media (prefers-color-scheme: dark) {
  :root {
    --color-accent: #c4b5fd;
    --color-accent-contrast: #1e1b4b;
    --color-surface: #171327;
    --color-border: #332b4d;
  }
}

:root[data-theme="dark"] {
  --color-accent: #c4b5fd;
  --color-accent-contrast: #1e1b4b;
  --color-surface: #171327;
  --color-border: #332b4d;
}
```

Filled buttons use `--color-accent-contrast` against `--color-accent`. Check that pair first, then read body text, links, code and callouts in both color modes.

## Change fonts and widths

DevLab does not fetch external fonts. Use a system stack, or keep local webfont files under `static/` and declare them in the site stylesheet.

```css
:root {
  --font-sans: Inter, ui-sans-serif, system-ui, sans-serif;
  --font-mono: "JetBrains Mono", ui-monospace, monospace;
  --site-width: 80rem;
  --content-width: 48rem;
}
```

The public site-level tokens are grouped by purpose:

| Group | Tokens |
| --- | --- |
| Core palette | `--color-bg`, `--color-surface`, `--color-fg`, `--color-muted`, `--color-border`, `--color-accent`, `--color-accent-contrast` |
| Code | `--color-code-bg`, `--color-code-fg`, `--color-pre-bg`, `--color-pre-fg`, `--color-pre-scheme` |
| Callouts | `--color-callout-{info,warning,error,tip}-{bg,border,fg}` |
| Typography | `--font-sans`, `--font-mono` |
| Widths | `--site-width`, `--content-width`, `--header-width`, `--wide-width`, `--wide-gutter`, `--wide-gutter-compact` |

Keep `--header-height` and `--sticky-offset` unchanged unless you test the custom header at every responsive breakpoint. Docs navigation and anchored headings depend on both values. Spacing, radii and breakpoints are currently component-level Sass values rather than public tokens.

Zola renders syntax tokens as `light-dark(<light_theme>, <dark_theme>)` and resolves them against `color-scheme`, so code blocks follow `--color-pre-scheme` (`light` or `dark`, default matches the active mode). Set it explicitly when `--color-pre-bg` does not match the mode: a dark code surface in the light theme needs `--color-pre-scheme: dark`, otherwise the tokens and the surface come from different schemes and the code loses contrast.

## Replace a static asset

To replace DevLab's favicon, add `static/favicon.svg` to the site. Zola gives it priority over the theme file at the same relative path.

Use a new path for unrelated assets and reference it from configuration or a template hook. Do not shadow DevLab's runtime files under `static/js/` unless the site deliberately takes ownership of that behavior. The replacement is silent and future theme versions will no longer be published at that path.

## Check the finished identity

- The logo link has a useful accessible name.
- Header and footer remain balanced on a narrow screen.
- Text and filled controls have sufficient contrast in light, dark and system modes.
- Long code blocks and Docs navigation still fit at the chosen content width.
- All custom files live outside `themes/devlab-theme/`.

The official Zola guide explains the same [site-over-theme file precedence](https://www.getzola.org/documentation/themes/installing-and-using-themes/#customizing-a-theme).
