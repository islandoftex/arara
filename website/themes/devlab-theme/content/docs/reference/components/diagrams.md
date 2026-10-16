+++
title = "Diagrams"
description = "Mermaid diagrams and loading behavior."
weight = 4
updated = 2026-10-06
+++

Diagrams turn a compact Mermaid-compatible source into a responsive SVG. The renderer is bundled with the theme, loaded only on pages that use `devlab.diagram`, and follows DevLab's live color tokens without a remote font or CDN request.

{% <devlab.diagram id="site-build-architecture" label="Site build" description="Markdown, site configuration and the installed theme produce static files for hosting."> %}
flowchart TD
  content["Markdown content"]
  config["Site configuration"]
  theme["Installed DevLab theme"]
  build["Zola build"]
  files["HTML, CSS and JavaScript"]
  host["Static hosting"]

  content --> build
  config --> build
  theme --> build
  build --> files
  files --> host
{% </devlab.diagram> %}

{% raw %}
```jinja
{% <devlab.diagram id="site-build-architecture" label="Site build" description="How a project becomes a static site."> %}
flowchart TD
  content["Markdown content"]
  config["Site configuration"]
  theme["Installed DevLab theme"]
  build["Zola build"]
  files["HTML, CSS and JavaScript"]
  host["Static hosting"]

  content --> build
  config --> build
  theme --> build
  build --> files
  files --> host
{% </devlab.diagram> %}
```
{% endraw %}

| Parameter | Required | Default | Description |
| --- | --- | --- | --- |
| `id` | Yes | - | Unique, HTML-safe identifier used by the accessible caption. |
| `label` | Yes | - | Visible diagram title and accessible name. |
| `description` | No | Empty | Visible explanation associated with the generated graphic. |
| `error_message` | No | `Diagram could not be rendered.` | Fallback message for disabled JavaScript or a rendering error. |

The current renderer supports flowcharts, state, sequence, class, ER and XY diagrams. Treat diagram source like the rest of the site's authored content: it is rendered from files in the repository, not from visitor input. Keep the description meaningful because it remains available when the visual cannot be rendered.

The self-contained renderer is about 1.56 MB minified (469 KB gzip). Neither the component loader nor the renderer is requested on pages without a diagram. Its entrypoint, build settings and licenses are documented in the repository's `THIRD_PARTY_NOTICES.md`.
