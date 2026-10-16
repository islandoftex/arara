+++
title = "Community cards"
description = "Introduce maintainers, contributors and community members wherever their work belongs."
weight = 11
updated = 2026-10-06
+++

Use people cards to show who maintains a project, writes its documentation or helps other users. A card centers an avatar, name and optional nickname, followed by a role, short biography and icon links. An optional support link occupies the bottom of the card. Only the person's name is required.

## Rendered example

These fictional profiles use a shared list from the demo configuration. The [standalone community page](@/community.md) renders the same data in a wider layout.

{{<devlab.people members={config.extra.community_members} content_lang={lang} />}}

The grid adapts to its available space, including the narrower Docs column. Links remain ordinary links without JavaScript, and each icon has an accessible name that identifies both the person and destination.

## Create your own page

Create `content/community.md`. Start with one person and add another `[[extra.members]]` table for each additional card:

{% raw %}
```jinja
+++
title = "Our community"
description = "The people behind Acme."
template = "page.html"

[[extra.members]]
name = "Alex Morgan"
nickname = "alexm"
support = { label = "Support Alex", path = "https://example.com/support/alex" }
initials = "AM"
role = "Maintainer"
bio = "Looks after releases and helps new contributors get started."
links = [
  { name = "Codeberg", path = "https://codeberg.org/your-account", icon = "codeberg" },
  { name = "Email", path = "mailto:hello@example.com", icon = "email" },
]

[[extra.members]]
name = "Mira Chen"
initials = "MC"
role = "Documentation"
bio = "Makes the project easier to learn and use."
+++

## Maintainers

{{<devlab.people members={page.extra.members} content_lang={lang} />}}
```
{% endraw %}

Replace the example identities and destinations before publishing. The card order follows the array order. Removing a person or a link removes it from the rendered page. An empty members array renders nothing. There is no global switch to enable: the component appears only where you call it.

The same call works in a Docs page or Blog post. For a section body, put the data in its `_index.md` and pass `section.extra.members` instead. To make a standalone page discoverable, add `/community/` to your [navigation or footer links](@/docs/reference/configuration/core.md#theme-settings).

## Add an avatar or keep initials

Place your image at `static/images/people/alex.webp`, then add this field inside Alex's table:

```toml
avatar = "/images/people/alex.webp"
```

Use a square image with the face or mark near the center. DevLab crops it into a circle and reserves its size to keep the layout steady while it loads. Local image paths respect the deployment prefix but do not receive a language prefix. An absolute HTTPS image URL works too, but makes a request to that host. Local files avoid that dependency.

When `avatar` is absent or empty, the card shows `initials`. If you omit those too, it uses the first character of the name. A broken image URL does not switch to initials automatically, so check the image when previewing. Avatars are decorative for assistive technology because the name is already written below them.

## Choose what a card says and where it leads

Use `nickname` for a handle or short identifier. It is plain text and does not create a link. Keep `role` short and `bio` to one or two sentences. Both are plain text, as is `name`. Markdown and HTML are not rendered inside these fields. Omit empty fields rather than filling them with placeholder text.

Set the optional `path` to make the person's name a link to their profile or another existing page. Each item in `links` needs its own `name` and `path`. `icon` selects a bundled SVG. The icon stays compact while its accessible label and tooltip identify the destination. Unsupported or missing icons use the generic link symbol.

Pass `content_lang={lang}` for language-aware local profile and social links. Make sure those destinations exist in each language that uses the cards. External URLs and email links are preserved. Cards do not create profile pages, translate biographies or add Blog authorship automatically.

## Add a support link

Add a `support` table to a person record:

```toml
support = { label = "Support Alex", path = "https://example.com/support/alex" }
```

Both `label` and `path` must be non-empty. An incomplete table stops the build with a configuration error. The label is authored text, so choose the wording for each language. The destination can be a donation page, a sponsor profile or a contribution guide. It uses the same route rules as the other links and opens in the current tab. No payment widget or remote script is loaded.

Omit `support` to render the card without a footer action. The demo uses a contribution link to the project repository.

## Reuse one list across pages

For a group that appears in several places, move the records to `zola.toml` and name their tables `[[extra.community_members]]`. Keep the same fields, then use:

{% raw %}
```jinja
{{<devlab.people members={config.extra.community_members} content_lang={lang} />}}
```
{% endraw %}

`community_members` is your own data key, not a mandatory DevLab setting. You can keep separate arrays for maintainers, translators and alumni, rendering each below a normal Markdown heading. For localized copy, use page or section data in each translated content file instead of sharing one global list.

## Adjust the presentation

Cards follow the current light or dark palette, spacing and radius tokens. Social links wrap when space runs out. Names and biographies are not cut off. Text and layout support both LTR and RTL pages.

Names are `h3` headings by default, suitable under an `h2` such as `## Maintainers`. Pass `heading_level={2}` when cards sit directly below a page title, or use another level from 2 to 6 to fit the surrounding document.

For site-specific styling, use the existing [stylesheet override workflow](@/docs/customization/templates.md) and target `.people-grid`, `.person-card` or `.person-avatar`. For example, this limits a group to two columns while keeping small screens in a single column:

```css
.people-grid {
  grid-template-columns: repeat(2, minmax(0, 1fr));
}

@media (max-width: 640px) {
  .people-grid {
    grid-template-columns: 1fr;
  }
}
```

See the [component reference](@/docs/reference/components/people.md) for all fields and supported icons.
