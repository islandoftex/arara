# Website development

The site stays in `website/` on the normal development and release branches.
Zola 0.23.6 comes from the regular nixpkgs input updated with `nix flake update`.
DevLab v0.9.0 is a pinned upstream Git submodule, not vendored source.
The Nix theme input and submodule must name the same commit when updating.

```sh
git submodule update --init --recursive
git lfs pull
nix build .#website
```

To preview with the pinned Zola:

```sh
python3 scripts/prepare-website.py --output /tmp/arara-site
nix run .#website-zola -- --root /tmp/arara-site serve
```

Choose a fresh output directory on each run. The preparation script appends
the existing README/changelog content as the old Nix build did and adapts
legacy shortcodes in generated copies only. Repository Markdown and front
matter remain unchanged. Literal template examples are protected from Tera 2.

The adapter is temporary compatibility code: Tera 2 no longer accepts the old
shortcode syntax. Named arguments, nested callouts and indentation within lists
need parsing; plain substitutions would break existing examples. Staging also
makes Nix store assets writable so site overrides work. Keep these checks until
a later content migration lets us remove the adapter.

Existing URLs, news, organizational attribution, logos and recording are
retained. Binary assets belong in Git LFS from their first commit; the root
`.gitattributes` covers the existing image and font formats. Theme assets
remain owned by the upstream submodule. No v8 content or author taxonomy is
introduced.
