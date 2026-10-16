+++
title = "Cards"
description = "Linked cards, heading levels and Markdown bodies."
weight = 7
updated = 2026-10-06
+++

Cards can link to another resource or present standalone information.

{% <devlab.card title="Fast setup" href="@/docs/getting-started/_index.md" content_lang={lang}> %}
Start building your documentation site with Zola.
{% </devlab.card> %}

{% raw %}
```jinja
{% <devlab.card title="Fast setup" href="@/docs/getting-started/_index.md" content_lang={lang}> %}
Start building your documentation site with Zola.
{% </devlab.card> %}
```
{% endraw %}

Omit `href` for a static card:

{% <devlab.card title="Project status"> %}
DevLab Theme is under active development.
{% </devlab.card> %}

### Card grid

`devlab.cards` arranges cards in a responsive grid. Place `devlab.card` children directly inside it. The grid fits as many 16rem columns as the content width allows and collapses to one column on narrow screens without JavaScript.

{% <devlab.cards> %}
{% <devlab.card title="Documentation" icon="file" href="@/docs/_index.md"> %}
Guides, reference pages and configuration recipes.
{% </devlab.card> %}
{% <devlab.card title="Repository" icon="codeberg" tag="External" href="https://codeberg.org/RiPetitor/devlab-theme"> %}
Source code, issues and release tags.
{% </devlab.card> %}
{% </devlab.cards> %}

{% raw %}
```jinja
{% <devlab.cards> %}
{% <devlab.card title="Documentation" icon="file" href="@/docs/_index.md"> %}
Guides, reference pages and configuration recipes.
{% </devlab.card> %}
{% <devlab.card title="Repository" icon="codeberg" tag="External" href="https://codeberg.org/RiPetitor/devlab-theme"> %}
Source code, issues and release tags.
{% </devlab.card> %}
{% </devlab.cards> %}
```
{% endraw %}

`devlab.card` parameters:

| Parameter | Required | Default | Description |
| --- | --- | --- | --- |
| `title` | With `href` | Empty | Card heading and accessible name of its link. |
| `href` | No | Empty | Local path, `@/` content path, fragment, query, or external URL. A linked card adds a trailing arrow; an external HTTP(S) URL shows the external-link marker instead. |
| `content_lang` | No | Empty | Active Zola language for a language-aware local link; pass `{lang}` on multilingual content. |
| `icon` | No | Empty | Optional icon from the built-in set, shown in a rounded tile in the card header. |
| `tag` | No | Empty | Optional short badge in the card header, aligned opposite the icon. |
| `tag_tone` | No | `neutral` | Badge tone: `neutral`, `info`, `success`, `warning` or `danger`. |
| `heading_level` | No | `3` | Integer from 2 to 6; other integers fall back to 3. Pass numeric values as `{2}`. |

The heading link covers the card surface. Markdown links and controls in the
body remain independently usable. The card never nests one link inside another.
This structure works without JavaScript. Custom CSS targeting `a.card` should
target `.card--link` for the container or `.card-link` for the heading link.

`devlab.cards` takes no parameters.

Docs overview cards use the same icon slot. Add an optional icon to a section or page in front matter:

```toml
[extra]
icon = "folder"
```

Omit the field to keep the plain text-only card.
