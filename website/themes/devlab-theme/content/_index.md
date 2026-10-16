+++
title = "DevLab Theme"
description = "A Zola theme for project pages, blogs, documentation and books."

[extra.devlab.home]
layout = "wide"

[[extra.devlab.home.blocks]]
type = "hero"
eyebrow = "Zola 0.23.6+"
image = "/images/devlab-docs-preview.jpg"
image_alt = "Documentation with a section sidebar, article column and heading navigation."
actions = [
  { label = "Quick start", path = "@/docs/getting-started/quick-start.md" },
  { label = "Blog", path = "@/blog/_index.md" },
]

[[extra.devlab.home.blocks]]
type = "features"
items = [
  { kicker = "Pages", title = "Pages and landing sections", description = "Ordinary Markdown pages or an ordered list of hero, feature, content and action blocks.", path = "@/docs/guides/content-layouts.md" },
  { kicker = "Blog", title = "Dated articles", description = "Posts, author profiles, heading navigation, feeds and pagination.", path = "@/docs/reference/configuration/reading.md#optional-blog" },
  { kicker = "Reading", title = "Documentation and books", description = "Named manuals and books with their own sidebar, breadcrumbs and reading sequence.", path = "@/docs/reference/collections.md" },
]

[[extra.devlab.home.blocks]]
type = "learn"
eyebrow = "Configuration"
title = "Configure and extend"
description = "Keep your site's content, configuration and overrides in its own files. Choose the layouts and controls each section needs."
items = [
  { kicker = "Content", title = "Content components", description = "Cards, callouts, tabs, file trees, diagrams and status timelines inside Markdown.", path = "@/docs/reference/components/_index.md" },
  { kicker = "Templates", title = "Template slots", description = "Replace a header item, page heading, sidebar addition, footer or icon in your site's files.", path = "@/docs/reference/extensions.md" },
  { kicker = "Navigation", title = "Collections", description = "Give each manual or book its own root, reading settings and navigation entries.", path = "@/docs/guides/navigation.md" },
  { kicker = "Translations", title = "Languages", description = "English, German and Russian interface catalogs, translated content routes and language-specific search.", path = "@/docs/reference/languages.md" },
]

[[extra.devlab.home.blocks]]
type = "workflow"
eyebrow = "Setup"
title = "Build the first page"
description = "Install the theme, create the site files and check the generated pages before adding more content."
items = [
  { title = "Install", description = "Link a local checkout or pin a published theme revision in themes/devlab-theme." },
  { title = "Create", description = "Write zola.toml and content/_index.md. Keep site changes outside the theme directory." },
  { title = "Check", description = "Run zola check, zola build and zola serve, then add the content and controls your site needs." },
]

[[extra.devlab.home.blocks]]
type = "cta"
title = "Start with the configuration and one page"
description = "Use the installation guide and Quick start to create your site, then add the layouts and components it needs."
actions = [
  { label = "Install DevLab", path = "@/docs/getting-started/installation.md" },
  { label = "Browse documentation", path = "@/docs/_index.md", style = "secondary" },
]
+++
