# The book assets

Everything the two books `\includegraphics` lands here, in two
populations with different owners.

**The static art** — the logo, the cover art, any hand-drawn figure —
is authored and committed by hand, and referenced from the skeletons in
`docs/tex/`.

**The diagram checkpoint** — `Concepts.mmd`/`Concepts.png` and friends —
is pipeline-maintained: the fence inside each chapter's Markdown is the
single source of truth, the conversion extracts the source, the diagram
pass renders the figure, and both sync back into this directory so the
books build from a fresh clone even without a reachable diagram service,
and a diagram change shows up as a reviewable diff. Never edit the
checkpoint by hand — the next build silently reverts it.
