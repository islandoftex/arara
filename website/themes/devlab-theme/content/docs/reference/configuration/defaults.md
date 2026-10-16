+++
title = "Default values"
description = "Default values of theme settings."
weight = 5
updated = 2026-10-06
+++

## Defaults

Collection-specific fields and inheritance: [Collections](@/docs/reference/collections.md).
Landing block fields: [Landing](@/docs/reference/landing.md). UI catalog: [Languages](@/docs/reference/languages.md).

| Setting                       | Default                       | Purpose                                                                |
| ----------------------------- | ----------------------------- | ---------------------------------------------------------------------- |
| `devlab.brand.logo_text`      | `config.title`                | Header logo text.                                                      |
| `devlab.brand.footer_text`    | `config.title`                | Footer copyright label.                                                |
| `devlab.brand.show_logo_mark` | `false`                       | Whether to render a compact image or the built-in theme mark.          |
| `devlab.brand.logo_mark_path` | Empty                         | Optional local or remote image that replaces the built-in mark.        |
| `devlab.announcement.show`    | `false`                       | Render the site-wide banner when its message is non-empty.             |
| `devlab.announcement.key`     | Empty                         | Announcement identity; a non-empty key is required for dismissal.      |
| `devlab.announcement.label`   | Empty                         | Optional plain-text prefix; hidden at widths of 420px or less.         |
| `devlab.announcement.message` | Empty                         | Plain-text announcement; an empty message hides the banner.           |
| `devlab.announcement.path`    | Empty                         | Optional link resolved with the active language; empty renders text.  |
| `devlab.announcement.dismissible` | `true`                    | Show a close button when the banner is enabled and has a key.         |
| `devlab.footer.show`           | `true`                        | Whether to render regular and compact footers.                         |
| `devlab.footer.note`           | Empty                         | Optional plain-text footer information.                                |
| `devlab.footer.links`          | Empty array                   | Optional project, social or utility links.                             |
| `devlab.footer.links_label`    | `ui.footer_links`                | Accessible name for the footer link group.                             |
| `devlab.footer.show_link_names` | `false`                      | Show names and a `links_label` heading in the regular footer. Compact reading footers always omit link names. |
| `devlab.seo.og_image`         | Empty                         | Optional Open Graph and Twitter preview image path or absolute URL.    |
| `devlab.appearance.default_mode` | `system`                   | Initial color mode before a visitor choice is saved.                   |
| `devlab.appearance.show_toggle`  | `true`                     | Whether to render the color mode toggle button.                        |
| `devlab.i18n.rtl_languages`    | Empty array                   | Active Zola language codes rendered right-to-left; unlisted languages use `ltr`. |
| `devlab.collections.<name>.navigation`      | Not set                       | Optional ordered top-level section manifest for the Docs sidebar, mobile panel and root catalog; omission or an empty array enables automatic discovery. |
| `devlab.docs.show_breadcrumbs` | `true`                       | Whether Docs articles render their breadcrumb path.                    |
| `devlab.docs.show_toc`         | `true`                       | Whether Docs pages and sections render heading navigation.             |
| `devlab.docs.show_pagination`  | `true`                       | Whether Docs articles render Previous and Next cards.                  |
| `devlab.docs.page_actions.show_last_updated` | `false`          | Show `page.updated` below a Docs page when the field is present.        |
| `devlab.docs.page_actions.show_copy_link` | `false`               | Add a progressively enhanced control that copies the current page URL. |
| `devlab.docs.page_actions.edit_url` | Empty                       | Repository edit-URL prefix; `page.relative_path` is appended.           |
| `devlab.docs.page_actions.report_url` | Empty                     | Complete issue or feedback URL used without modification.               |
| `devlab.docs.page_actions.date_format` | `%Y-%m-%d`               | Display format for the Docs `updated` value.                             |
| `devlab.blog.date_format`     | `%d.%m.%Y`                    | Display format for published and updated dates.                        |
| `devlab.blog.show_reading_time` | `true`                      | Show Zola's reading-time estimate; a post can override it through `extra.show_reading_time`. |
| `devlab.blog.show_pagination` | `true` | Show Older/Newer links in a sorted article section. |
| `devlab.blog.show_toc`        | `true`                        | Render a post table of contents: sticky beside the article on wide screens, collapsed above it on narrow ones. |
| `devlab.search.enabled`       | `true`                        | Whether DevLab renders its search interface and loads its adapter.     |
| `devlab.search.language_scripts.<lang>` | German/Russian modules for `de`/`ru`; empty for others | Ordered lazy language plugin paths, replacing that language’s defaults. |
| `devlab.search.index`         | Empty                         | Optional generated-index filename override; empty selects `search_index.<lang>.js`. |
| `devlab.navigation.links`     | Home | Header and mobile links using `name`/`path` or `collection`. |
