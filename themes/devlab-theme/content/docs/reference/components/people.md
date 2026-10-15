+++
title = "People"
description = "Profile cards and contact links."
weight = 8
updated = 2026-10-06
+++

`devlab.people` renders an adaptive grid of community cards without JavaScript. Follow [Community cards](@/docs/guides/community.md) for page setup and examples.

{% raw %}
```jinja
{{<devlab.people members={page.extra.members} content_lang={lang} />}}
```
{% endraw %}

| Parameter | Required | Default | Description |
| --- | --- | --- | --- |
| `members` | Yes | - | Array of person records in display order. An empty array renders nothing. |
| `content_lang` | No | Empty | Active language for local profile and social links; pass `{lang}` on multilingual pages. |
| `heading_level` | No | `3` | HTML heading level for names, from 2 to 6. Other integers fall back to 3. |

Each record accepts these fields:

| Field | Required | Default | Description |
| --- | --- | --- | --- |
| `name` | Yes | - | Non-empty display name, rendered as escaped plain text. |
| `nickname` | No | Empty | Handle or identifier displayed below the name as plain text. |
| `support` | No | Empty | Footer action with non-empty `label` and `path`. |
| `role` | No | Empty | Short role below the name. |
| `bio` | No | Empty | Plain-text biography below the identity block. |
| `avatar` | No | Empty | Local static image path or absolute HTTP(S) image URL. Local image paths are not language-prefixed. |
| `initials` | No | First character of `name` | Text used when no avatar is supplied; use one or two letters. |
| `path` | No | Empty | Optional destination for the name link. Does not create a profile page. |
| `links` | No | Empty array | Icon links, each with a non-empty `name` and `path`, plus an optional `icon`. |

`name`, `nickname`, `role`, `bio`, `avatar`, `initials` and `path` are strings. The `support` object uses string fields `label` and `path`. It has the same route and escaping rules as profile and social links. A missing name or incomplete support link stops the build with an explanatory error. Each link's `name` is used in its tooltip and accessible label, prefixed by the person's name. Entries missing a non-empty link name or destination are omitted. Profile and social links use the same route resolver as `devlab.card`, stay in the current tab and support local routes, external HTTP(S) URLs, fragments, queries, `mailto:` and `tel:`.

Supported link icons: `github`, `gitlab`, `codeberg`, `matrix`, `zulip`, `telegram`, `discord`, `rss`, `email`, `link`, `external-link`. Unknown or missing icon names fall back to `link`. Arbitrary SVG markup is not accepted as an argument.

These records are authored data, not automatic theme configuration. Store them in page or section front matter, a shared configuration array or another array passed from a template. No people or routes are added to consumer sites by default. The component does not connect profiles to Blog metadata. Blog authorship is a separate capability.
