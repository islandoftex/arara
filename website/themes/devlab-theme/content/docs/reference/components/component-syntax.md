+++
title = "Component syntax"
description = "Component calls, Markdown bodies and Tera arguments."
weight = 1
+++

Self-closing components render inline:

{% raw %}
```jinja
{{<devlab.icon name="github" />}}
```
{% endraw %}

Components that accept Markdown use an opening and closing tag:

{% raw %}
```jinja
{% <devlab.callout type="info" title="Good to know"> %}
The body supports **Markdown**.
{% </devlab.callout> %}
```
{% endraw %}

Arguments written as strings use quotes. Pass variables and non-string literals as Tera expressions in braces, for example `item={page}` or `enabled={true}`.
