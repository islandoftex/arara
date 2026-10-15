+++
title = "Content layouts"
description = "Use generic pages and sections outside the specialized Docs, Blog and Downloads layouts."
weight = 2
updated = 2026-10-06
+++

DevLab includes generic templates for project pages, directories, portfolios and other content that does not belong in Docs, Blog or Downloads. They use Zola's normal content model and do not require additional theme configuration.

## Choose the generic templates

Zola uses `page.html` for a regular page and `section.html` for a non-root section when a more specific template is not configured. The root `content/_index.md` uses the theme's `index.html` homepage by default. You can name the generic templates explicitly in section front matter:

```toml
+++
title = "Projects"
description = "Tools, libraries and experiments."
sort_by = "weight"
template = "section.html"
page_template = "page.html"
+++
```

The generic layouts are intentionally smaller than the specialized layouts:

| Content | Template | Included behavior |
| --- | --- | --- |
| Regular page | `page.html` | Title, optional description and Markdown body. |
| Regular section | `section.html` | Title, optional description and body, child sections, pages and optional pagination. |
| Documentation | `docs.html` and `doc-page.html` | Recursive sidebar, breadcrumbs, page TOC and previous/next links. |
| Blog / Updates | `blog.html` and `blog-page.html` | Dates, release metadata, post list, breadcrumbs and pagination. |
| Single / Catalog downloads | `downloads-hub.html` and `downloads-product.html` | Product releases, channel tabs, optional catalog filters and product detail pages. |
| Simple download cards | `downloads.html` | Compact channel cards, status and verification links. |

Use a specialized layout only when the content needs its additional navigation or metadata contract.

Section `page_template` values are inherited by descendant pages until a closer section overrides them. When a generic section lives below Docs or Blog, set both `template = "section.html"` and `page_template = "page.html"` explicitly. An individual page can still override the inherited layout with its own `template` value.

## Create a regular page

Create `content/about.md`:

```md
+++
title = "About"
description = "Background and project goals."
+++

This page explains why the project exists and how it is maintained.
```

With the default slug, Zola publishes the page at `/about/`. The generic page layout renders the title, description and Markdown body without adding Docs navigation, a page TOC or Blog metadata.

## Create a regular section

Create `content/projects/_index.md`:

```toml
+++
title = "Projects"
description = "Tools, libraries and experiments."
sort_by = "weight"
template = "section.html"
page_template = "page.html"
+++

Browse current work and completed experiments.
```

Then add `content/projects/cli-tool.md`:

```md
+++
title = "CLI tool"
description = "A small command-line utility."
weight = 1
+++

Installation notes, examples and project links belong here.
```

The section body is rendered before its content list. Each direct child page becomes a card with its title and optional description. `sort_by = "weight"` and page `weight` values control the page order through Zola.

## Add child sections

Directories with their own `_index.md` become child sections:

```text
content/projects/
├── _index.md
├── cli-tool.md
└── libraries/
    ├── _index.md
    └── parser.md
```

The parent `/projects/` page lists the `Libraries` section before its direct pages. The `/projects/libraries/` page renders its own body and direct pages. Generic sections do not flatten the complete tree into one list.

Give child sections a `description` so their cards explain what readers will find there. Assign a unique `weight` to each child section when their order matters. When the parent uses `sort_by = "weight"`, give every direct page a weight as well.

These lists come from Zola's content model. To choose the exact items instead, compose [Content components](@/docs/guides/content-components.md) by hand.

## Publish without listing

Set `hidden = true` when a route should remain published and directly linkable
but should not appear in generated lists, search, feeds or the sitemap:

```toml
+++
title = "Private preview"
hidden = true
+++
```

DevLab reads `section.pages` and `section.subsections` directly, so Zola's hidden-content behavior applies consistently to generic lists, Docs navigation and section catalogs without a second theme setting.

## Paginate section pages

Add `paginate_by` when a section should show a limited number of direct pages at a time:

```toml
+++
title = "Projects"
sort_by = "weight"
paginate_by = 12
template = "section.html"
page_template = "page.html"
+++
```

The generic section layout switches from `section.pages` to Zola's `paginator.pages` and renders previous/next controls when more than one page exists. Pagination applies only to direct pages. Child-section cards and the section body remain visible on every paginated page.

Set `paginate_path` in the section front matter only when the default pagination path does not fit the site's URL design.
