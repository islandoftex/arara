+++
title = "Icons"
description = "Built-in icons and custom SVG slots."
weight = 13
updated = 2026-10-06
+++

DevLab ships a small SVG library and exposes it through `devlab.icon`.
Interface icons use [Tabler Icons](https://tabler.io/icons). Service logos use
[Simple Icons](https://simpleicons.org). All SVGs are bundled with the theme,
inherit the surrounding text color, and work in light and dark mode.

GitHub {{<devlab.icon name="github" />}} Codeberg {{<devlab.icon name="codeberg" />}} Matrix {{<devlab.icon name="matrix" />}}

{% raw %}
```jinja
GitHub {{<devlab.icon name="github" />}}
```
{% endraw %}

Icons are decorative by default. Add `label` when an icon communicates meaning without adjacent text.

{{<devlab.icon name="check" size="sm" label="Small icon" />}}
{{<devlab.icon name="check" size="inline" label="Inline icon" />}}
{{<devlab.icon name="check" size="md" label="Medium icon" />}}
{{<devlab.icon name="check" size="lg" label="Large icon" />}}

| Parameter | Required | Default | Description |
| --- | --- | --- | --- |
| `name` | Yes | - | Icon name without `.html`. |
| `size` | No | `inline` | `sm`, `inline`, `md`, or `lg`; invalid values fall back to `inline`. |
| `label` | No | Empty | Accessible label for a meaningful standalone icon. |
| `extra_class` | No | Empty | Additional class on the wrapper. |

Available names:

- Theme: `sun`, `moon`, `desktop`
- Social and brand: `github`, `gitlab`, `codeberg`, `matrix`, `zulip`, `telegram`, `discord`, `rss`, `email`, `link`
- Interface: `search`, `chevron-right`, `external-link`, `copy`, `check`, `info`, `warning`, `menu`, `x`, `arrow-left`, `arrow-right`, `folder`, `file`, `terminal`, `heart`

### Icon catalog

<table>
  <thead><tr><th>Icon</th><th>Name</th><th>Icon</th><th>Name</th></tr></thead>
  <tbody>
  <tr>
    <td>{{<devlab.icon name="sun" size="md" />}}</td><td><code>sun</code></td>
    <td>{{<devlab.icon name="moon" size="md" />}}</td><td><code>moon</code></td>
  </tr>
  <tr>
    <td>{{<devlab.icon name="desktop" size="md" />}}</td><td><code>desktop</code></td>
    <td>{{<devlab.icon name="terminal" size="md" />}}</td><td><code>terminal</code></td>
  </tr>
  <tr>
    <td>{{<devlab.icon name="search" size="md" />}}</td><td><code>search</code></td>
    <td>{{<devlab.icon name="menu" size="md" />}}</td><td><code>menu</code></td>
  </tr>
  <tr>
    <td>{{<devlab.icon name="x" size="md" />}}</td><td><code>x</code></td>
    <td>{{<devlab.icon name="chevron-right" size="md" />}}</td><td><code>chevron-right</code></td>
  </tr>
  <tr>
    <td>{{<devlab.icon name="arrow-left" size="md" />}}</td><td><code>arrow-left</code></td>
    <td>{{<devlab.icon name="arrow-right" size="md" />}}</td><td><code>arrow-right</code></td>
  </tr>
  <tr>
    <td>{{<devlab.icon name="external-link" size="md" />}}</td><td><code>external-link</code></td>
    <td>{{<devlab.icon name="link" size="md" />}}</td><td><code>link</code></td>
  </tr>
  <tr>
    <td>{{<devlab.icon name="copy" size="md" />}}</td><td><code>copy</code></td>
    <td>{{<devlab.icon name="check" size="md" />}}</td><td><code>check</code></td>
  </tr>
  <tr>
    <td>{{<devlab.icon name="info" size="md" />}}</td><td><code>info</code></td>
    <td>{{<devlab.icon name="warning" size="md" />}}</td><td><code>warning</code></td>
  </tr>
  <tr>
    <td>{{<devlab.icon name="folder" size="md" />}}</td><td><code>folder</code></td>
    <td>{{<devlab.icon name="file" size="md" />}}</td><td><code>file</code></td>
  </tr>
  <tr>
    <td>{{<devlab.icon name="rss" size="md" />}}</td><td><code>rss</code></td>
    <td>{{<devlab.icon name="email" size="md" />}}</td><td><code>email</code></td>
  </tr>
  <tr>
    <td>{{<devlab.icon name="github" size="md" />}}</td><td><code>github</code></td>
    <td>{{<devlab.icon name="gitlab" size="md" />}}</td><td><code>gitlab</code></td>
  </tr>
  <tr>
    <td>{{<devlab.icon name="codeberg" size="md" />}}</td><td><code>codeberg</code></td>
    <td>{{<devlab.icon name="matrix" size="md" />}}</td><td><code>matrix</code></td>
  </tr>
  <tr>
    <td>{{<devlab.icon name="zulip" size="md" />}}</td><td><code>zulip</code></td>
    <td>{{<devlab.icon name="telegram" size="md" />}}</td><td><code>telegram</code></td>
  </tr>
  <tr>
    <td>{{<devlab.icon name="discord" size="md" />}}</td><td><code>discord</code></td>
    <td>{{<devlab.icon name="heart" size="md" />}}</td><td><code>heart</code></td>
  </tr>
  </tbody>
</table>

### Use an icon from a template

The lower-level SVG component is also global:

{% raw %}
```html
<a href="https://github.com/you" aria-label="GitHub">
  {{<devlab.svg_icon name="github" />}}
</a>
```
{% endraw %}

To extend the set, use the small `partials/icon.html` slot described in the [Extension API](@/docs/reference/extensions.md#add-an-icon). Keep filenames lowercase and add an accessible label when the icon is not decorative.
