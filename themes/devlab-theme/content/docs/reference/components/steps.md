+++
title = "Steps"
description = "Numbered instructions."
weight = 11
updated = 2026-10-06
+++

Steps turn a Markdown ordered list into a connected walkthrough.

{% <devlab.steps> %}
1. Install Zola 0.23.6.
2. Add `devlab-theme` under `themes/devlab-theme`.
3. Run `zola serve` and open the printed address.
{% </devlab.steps> %}

{% raw %}
```jinja
{% <devlab.steps> %}
1. Install Zola.
2. Add the theme.
3. Run `zola serve`.
{% </devlab.steps> %}
```
{% endraw %}

`devlab.steps` takes no arguments. Each top-level list item becomes one step. Nested Markdown remains available inside it.
