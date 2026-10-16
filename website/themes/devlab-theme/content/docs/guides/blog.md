+++
title = "Create and publish a blog"
description = "Create a section, write a post, connect tags and authors, then publish."
weight = 5
updated = 2026-10-06
+++

Start from the [Quick start](@/docs/getting-started/quick-start.md) site. Run the
commands from the site root and keep the blog outside the theme directory.

## 1. Create the section

Create `content/blog/_index.md`:

```toml
+++
title = "Blog"
description = "Project notes and tutorials."
sort_by = "date"
paginate_by = 5
template = "blog.html"
page_template = "blog-page.html"
+++
```

Add `{ name = "Blog", path = "/blog/" }` to your existing navigation links.
Every post needs `date` because the section sorts by date. Posts without sorting
data are excluded from the sorted list. Omit `paginate_by` to list all posts.

## 2. Register tags and authors

Add these settings to `zola.toml` before any tables. Extend an existing
`taxonomies` array rather than declaring it twice:

```toml
author = "Alex Morgan"
generate_feeds = true
feed_filenames = ["atom.xml"]
taxonomies = [
  { name = "tags", paginate_by = 10 },
  { name = "authors", feed = true },
]
```

Create `data/authors.toml`:

```toml
[alex]
name = "Alex Morgan"
role = "Project maintainer"
```

The key `alex` connects a post to this record and its `/authors/alex/` page.
Known profiles require a non-empty name. Unknown keys fall back to their plain
name. [Blog authors](@/docs/guides/blog-authors.md) covers avatars, links,
multiple writers and profile feeds.

## 3. Write the first post

Create `content/blog/first-post.md`:

```md
+++
title = "First project release"
description = "Install the first release and check its configuration."
date = 2026-10-06

[taxonomies]
tags = ["release", "installation"]
authors = ["alex"]
+++

## Install

Explain the command and its expected result.

## Check

Show how readers can verify the installation.
```

Use `updated` when you change the article after publication. `extra.release`
adds an optional release badge. Prefer a short `description` for article lists
and metadata. Without a description, a post's `<!-- more -->` summary supplies
a plain-text list excerpt, limited to 280 characters. The full article keeps
its Markdown formatting.

Tag links appear in lists and articles and lead to generated tag archives.
Older/Newer links follow the containing section's date order. Set
`extra.devlab.blog.show_pagination = false` in `zola.toml` to hide those links. Standalone
articles outside a sorted section have no adjacent-post links.

## 4. Preview a draft

Set `draft = true` in the post front matter while writing. Run:

```sh
zola serve --drafts
```

Normal builds omit drafts. Remove the draft flag before publication. For a
renamed route, use Zola's native `aliases` field to preserve old incoming links.
Keep dated filenames and aliases site-owned. Zola's
[page front matter reference](https://www.getzola.org/documentation/content/page/)
explains these native settings.

## 5. Verify and publish

```sh
zola check --skip-external-links
zola build
zola serve
```

Open `/blog/`, the article, `/tags/release/`, `/authors/alex/`, `atom.xml` and the
author feed. Check dates, bylines, excerpts, tag links and Older/Newer links.
Test the menu and search with a keyboard. A site under a path prefix must keep
that prefix in article and asset links.

Follow [Deploy a site](@/docs/guides/deployment.md) to upload `public/`, then
subscribe to the published feed to verify its URLs. Publication metadata and
cache policy are covered in [Publishing](@/docs/guides/publishing.md).
