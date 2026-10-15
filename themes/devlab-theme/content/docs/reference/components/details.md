+++
title = "Details"
description = "Collapsible Markdown content."
weight = 10
updated = 2026-10-06
+++

Details hide supplementary Markdown behind the browser's native, no-JavaScript disclosure widget.

{% <devlab.details summary="What Zola version do I need?"> %}
DevLab Theme requires Zola `0.23.6`. Check the installed version with `zola --version`.
{% </devlab.details> %}

{% raw %}
```jinja
{% <devlab.details summary="What Zola version do I need?"> %}
DevLab Theme requires Zola `0.23.6`.
{% </devlab.details> %}
```
{% endraw %}

The required `summary` argument is the always-visible label.
