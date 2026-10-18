# Website development

Zola 0.23.6 and DevLab v0.9.0 are pinned. Keep the theme submodule and
`devlab-theme` flake input at the same commit. Binary assets use Git LFS.

```sh
git submodule update --init --recursive
git lfs pull
nix build .#website
```

Preview using a fresh staging directory:

```sh
python3 scripts/prepare-website.py --output /tmp/arara-site
nix run .#website-zola -- --root /tmp/arara-site serve
```

Staging adapts legacy shortcodes in generated copies, leaving sources unchanged.

For PDFs, make `result/public` available at `website/public`, then run
`nix develop -c ./gradlew :docs:buildDocs` (also included in `assembleCTAN`).
`nix build .#website` runs the staging and PDF extraction checks.
