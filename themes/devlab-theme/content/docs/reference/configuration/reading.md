+++
title = "Docs and Blog settings"
description = "Documentation roots, page actions and dated articles."
weight = 2
updated = 2026-10-06
+++

## Optional Docs

Create `content/docs/_index.md`:

```toml
+++
title = "Documentation"
sort_by = "weight"
template = "docs.html"
page_template = "doc-page.html"
+++
```

Register the root and add a header link in `zola.toml`:

```toml
[extra.devlab.collections.manual]
section = "docs/_index.md"

[extra.devlab.navigation]
links = [{ name = "Home", path = "/" }, { collection = "manual" }]
```

Create `content/docs/install.md` with a title and weight:

```md
+++
title = "Installation"
weight = 1
+++

## Requirements

List the tools needed to install the project.
```

`page_template` applies to all descendant pages. A closer section or the
page itself can override it. A nested section selects its own `template`.
Pages follow `weight` order. Directories with
`_index.md` form navigation groups. [Docs navigation](@/docs/guides/navigation.md)
describes nested and curated trees.

| Reading option | Default | Effect when false |
| --- | --- | --- |
| `show_breadcrumbs` | `true` | Removes the path above the article. |
| `show_toc` | `true` | Removes heading navigation. |
| `show_pagination` | `true` | Removes Previous/Next links. |

Set shared defaults in `extra.devlab.docs`, or set them on an individual
collection. [Collections](@/docs/reference/collections.md) explains local overrides.

## Page provenance actions

```toml
[extra.devlab.docs.page_actions]
show_last_updated = true
show_copy_link = true
edit_url = "https://codeberg.org/acme/project/_edit/main/content/"
report_url = "https://codeberg.org/acme/project/issues/new"
date_format = "%Y-%m-%d"
```

| Field | Default | Behavior |
| --- | --- | --- |
| `show_last_updated` | `false` | Shows the page's native `updated` date when present. |
| `show_copy_link` | `false` | Adds Copy link when the browser Clipboard API is available. |
| `edit_url` | Empty | Prefix to which the source `relative_path` is appended, including a translated filename suffix. |
| `report_url` | Empty | Complete feedback URL, used unchanged. |
| `date_format` | `%Y-%m-%d` | Format of the update date. |

Use absolute HTTP(S) contribution URLs. Keep a trailing slash on `edit_url`
and include the repository's content directory. Empty URLs hide the links.
Copy link includes the current heading fragment. Edit appears beside the article
on wide screens and below it on narrow screens, including when TOC is disabled.

## Optional Blog

For the complete authoring and publication sequence, follow
[Create and publish a blog](@/docs/guides/blog.md).

Create `content/blog/_index.md`:

```toml
+++
title = "Blog"
sort_by = "date"
paginate_by = 5
template = "blog.html"
page_template = "blog-page.html"
+++
```

Create a dated post, for example `content/blog/first-post.md`:

```md
+++
title = "First post"
date = 2026-10-03
+++

Write the post here.
```

Add `{ name = "Blog", path = "/blog/" }` to the existing navigation array.
Omit `paginate_by` to list all posts on one page. A custom section path changes
the route. Update the header link to match.

| Field under `extra.devlab.blog` | Default | Purpose |
| --- | --- | --- |
| `date_format` | `%d.%m.%Y` | Published and updated date format. |
| `show_reading_time` | `true` | Shows Zola's estimate; a post can override it with `extra.show_reading_time`. |
| `show_toc` | `true` | Shows heading navigation beside or above the article. |
| `show_pagination` | `true` | Shows Older/Newer links for posts in a sorted section. |

Posts use native `date` and `updated` fields. `extra.release` adds a release
badge. Author profiles use the `authors` taxonomy described in
[Blog authors](@/docs/guides/blog-authors.md).
Feeds are configured separately in [Publishing](@/docs/guides/publishing.md#atom-feed).
