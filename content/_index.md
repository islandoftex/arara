+++
title = "arara"
description = "A TeX automation tool based on directives and rules."
+++

arara is a TeX automation tool built on a simple idea: you tell it what
to do with your files, in the files themselves, and it does exactly
that — no wizardry, no hidden state, just rules and directives you can
read.

```tex
% arara: pdflatex
% arara: bibtex
% arara: pdflatex
% arara: clean
\documentclass{article}
\begin{document}
Hello, world!
\end{document}
```

{% <devlab.callout type="info" title="The scaffold, live"> %}
This page exercises the identity: Karla carries the prose, Rosario
the headings, Expletus Sans the display voice, and Cascadia Code the
listings — while knuthify decorates LaTeX and TeX in prose and leaves
the code above untouched. Try both color modes; the navy side is the
book's cover.
{% </devlab.callout> %}
