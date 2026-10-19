# Website development

Zola 0.23.6 and DevLab v0.9.0 are pinned. Keep the theme submodule and
`devlab-theme` flake input at the same commit. Binary assets use Git LFS.

```sh
git submodule update --init --recursive
git lfs pull
nix build .#website
```

Preview staged sources:

```sh
nix develop -c ./gradlew :docs:prepareWebsite
nix run .#website-zola -- --root docs/build/website/source serve
```

Staging adapts legacy shortcodes in generated copies, leaving sources unchanged.

`nix develop -c ./gradlew :docs:buildDocs` builds the site and both PDFs;
`assembleCTAN` includes this step. Use `:docs:buildWebsite` for HTML alone.
Gradle and Nix share the Kotlin helpers in `buildSrc`; `./gradlew -p buildSrc check`
runs their regression checks, which also run in `nix build .#website`.
