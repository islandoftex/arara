+++
title = "Literal Tera in Markdown"
description = "Show Tera code without executing it."
weight = 14
+++

Zola 0.23.6 templates all Markdown content, including fenced code blocks. Wrap examples that must be displayed rather than executed in a Tera raw block:

<pre><code>&#123;% raw %&#125;
```jinja
&#123;&#123;&lt;project.example value="literal" /&gt;&#125;&#125;
```
&#123;% endraw %&#125;</code></pre>

For files that should never execute Tera, add their paths to `skip_content_templating` in `zola.toml`. Do not disable templating on pages that use DevLab components.
