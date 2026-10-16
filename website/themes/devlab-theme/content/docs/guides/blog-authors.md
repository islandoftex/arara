+++
title = "Blog authors"
description = "Define an author once, credit one or more people on a post and give every writer a page of their own."
weight = 6
updated = 2026-10-06
+++

Register authors with Zola’s `authors` taxonomy and store their profiles in
`data/authors.toml`. Blog lists and article headings show the avatar and linked
name beside the date and reading time. Each author has a page listing their posts.

## Add your first author

{% <devlab.steps> %}
1. **Enable the `authors` taxonomy and create the shared profile.**

   In `zola.toml`, register the taxonomy next to `tags`:

   ```toml
   taxonomies = [
     { name = "tags", paginate_by = 5 },
     { name = "authors", feed = true },
   ]
   ```

   Then create `data/authors.toml` with one table per writer:

   ```toml
   [alex]
   name = "Alex Morgan"
   role = "Project maintainer"
   avatar = "/images/people/alex.webp"
   links = [
     { name = "Codeberg", path = "https://codeberg.org/alex", icon = "codeberg" },
   ]
   ```

   `alex` is the stable author key. The same string names the taxonomy term and profile table. Zola determines the URL using the site's taxonomy slug rules: `alex` becomes `/authors/alex/`, while `Alex Morgan` becomes `/authors/alex-morgan/`. Quote a TOML profile table name if it contains spaces.

2. **Credit that key in a Blog post.**

   ```toml
   +++
   title = "Acme 1.0"
   description = "What changed in our first stable release."
   date = 2026-09-08

   [taxonomies]
   tags = ["release"]
   authors = ["alex"]
   +++
   ```

   If the post already has a `[taxonomies]` table for `tags`, add `authors` inside that table. TOML does not allow the same table twice.

3. **Check all three presentations.**

   ```bash
   zola build
   ```

   Open the Blog list and the article to confirm the byline row, then `/authors/alex/` for the profile header and the post archive. When `feed = true` is set, Zola publishes the configured `feed_filenames` with only that writer's posts. The profile links to the first configured feed, `atom.xml` by default. `rss.xml` is also supported.
{% </devlab.steps> %}

## Where the byline and the author page lead

The Blog list and the article header share one byline design: a circular avatar (initials when no image is set), the author's name as a link, then the publication date and reading time. The same row is used on both surfaces.

The name links to the author page by default. The page contains the avatar, role, icon links and the writer's post archive. A profile `path` overrides the destination when a writer's identity lives elsewhere, such as a community page or an external profile.

DevLab also emits an `author` metadata entry and an author link in the document head for each credited person.

## Credit more than one person

List several keys in editorial order:

```toml
[taxonomies]
authors = ["alex", "mira"]
```

The first key appears first in the byline. There is no fixed author limit. On narrow screens the row wraps instead of being cut off. Removing `authors` or setting it to an empty array hides the byline for that post.

An unknown key uses a plain linked name. It has no authored profile, so check the byline before publishing. `zola check` does not render every template path, so do not use it as the only release check.

## Shape an author profile

Every declared profile requires a non-empty `name`. Empty or whitespace-only
names stop the build with an error naming the profile.

| Field | Default | Purpose |
| --- | --- | --- |
| `name` | Required | Visible author name and document metadata. |
| `role` | Empty | Short responsibility shown on the author page. |
| `avatar` | Empty | Local static image path or absolute HTTP(S) image URL. |
| `path` | Author page | Destination linked from the author's name in bylines and metadata. |
| `links` | Empty array | Icon links shown on the author page. |

These fields are escaped plain text. They do not render Markdown or HTML. Keep the role concise.

Each `links` item needs a non-empty `name` and `path`, with an optional `icon`. DevLab supports the same profile icons as [Community cards](@/docs/guides/community.md#choose-what-a-card-says-and-where-it-leads): `github`, `gitlab`, `codeberg`, `matrix`, `zulip`, `telegram`, `discord`, `rss`, `email`, `link` and `external-link`. An unknown icon uses the generic link mark.

## Use an avatar or initials

Put a square image in the site's static directory:

```toml
avatar = "/images/people/alex.webp"
```

Local avatar paths include the site's deployment prefix but are not language-prefixed. DevLab reserves the image dimensions and crops the image to a circle. The image is decorative because the visible name carries the identity. If the file is missing, the browser shows a broken image rather than switching to initials, so verify local assets before publishing.

When `avatar` is absent, DevLab derives the fallback from the first character of `name`, including Unicode names.

## Control reading time

Zola calculates `page.reading_time` from each post. DevLab shows it beside the publication date by default, which gives readers useful context without another front-matter field.

Disable it for the complete Blog in `zola.toml`:

```toml
[extra.devlab.blog]
show_reading_time = false
```

Or override one post inside its existing `[extra]` table:

```toml
[extra]
show_reading_time = false
```

The estimate remains available to template overrides even when DevLab does not render it.

## Connect profiles and destinations

`path` can lead to a personal site, a repository profile or a local Community page. Local paths and social destinations use the active-language route resolver, while HTTP(S), `mailto:`, `tel:`, fragments and query-only links are preserved. Omit `path` to keep the author page as the destination.

The author profile uses the same basic field names as `devlab.people`, so a maintainer can be represented consistently in both places. Blog and Community use different records: Blog authors live in `data/authors.toml` keyed for taxonomy front matter, while Community groups remain arrays that can be arranged under any page heading.

Global profiles are shared by every language. Use language-neutral names and destinations where possible. If roles must be translated, a site can provide language-specific template overrides. The language switcher selects existing translated pages and does not translate profile records.

## Translate the interface labels

Override the visible reading-time suffix in `zola.toml`:

```toml
[extra.devlab.i18n.translations.en]
reading_time = "мин чтения"
author_feed = "Публикации: {author}"
```

`author_feed` names the profile feed link for assistive technology and its tooltip. `{author}` is replaced with the display name. `reading_time` appears after Zola's numeric estimate. The profile's own `name`, `role` and link labels remain authored data.

For the exact configuration contract, see [Optional Blog](@/docs/reference/configuration/reading.md#optional-blog).
