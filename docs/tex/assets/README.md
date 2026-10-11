# The book assets

Static art lives here — the logo, the cover, any hand-drawn figure —
authored and committed by hand, and referenced from the skeletons in
`docs/tex/`.

The rendered diagram figures are generated: the fences inside each
chapter's Markdown are the single source of truth, and the diagram pass
(`./gradlew renderDiagrams`) draws them into the build tree. They are
deliberately not committed; a figure that has not been rendered yet
compiles to a visible box naming the render command, so the books always
build, never silently missing art.
