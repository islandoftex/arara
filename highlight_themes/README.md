# Highlight themes

Zola loads additional syntax themes through the `extra_themes`
setting under `[markdown.highlighting]` in `config.toml` — a list of
JSON TextMate theme files.

- `github-dark-high-contrast.json` comes from the
  [shikijs/textmate-grammars-themes](https://github.com/shikijs/textmate-grammars-themes)
  collection (MIT license) — the same curated set Zola's docs point
  to. It is the github-dark face with every token brightened; its
  comments read at roughly 8.7:1 on the site's navy code surface,
  against the bundled github-dark's 3.1:1 — and arara directives
  are comments. The file is shipped exactly as the collection
  provides it.
