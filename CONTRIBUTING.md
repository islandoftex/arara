# Contributing

Code, rules, translations, documentation and bug reports are welcome. Development
happens on [GitLab](https://gitlab.com/islandoftex/arara); GitHub is a mirror.
Open issues and merge requests on GitLab, and keep discussions constructive.

## Issues and merge requests

Search existing issues before reporting a problem. Include a minimal example,
expected and actual behavior, arara and Java versions, operating system and TeX
distribution where relevant. Discuss substantial design changes before
implementing them.

Target development changes at `development`. Keep merge requests focused, with
reviewable commits and a short description of the problem, resulting behavior
and verification. Retain license notices and credit reused work; contributions
use the project's [BSD-3-Clause license](LICENSE).

## Development and verification

Use JDK 21 and the Gradle wrapper (`./gradlew`); `nix develop` supplies the build
and documentation tools. Initialize submodules recursively and fetch Git LFS
assets before building the website. See the [build instructions](website/content/manual/Building.md)
for details.

Follow surrounding code conventions, keep abstractions in the lowest suitable
module, prefer `MPPPath` for I/O, and document public APIs with KDoc. Put dependency
versions in `gradle/libs.versions.toml`. New Kotlin files need the
`// SPDX-License-Identifier: BSD-3-Clause` header.

Add regression coverage for behavior changes. Before submitting code, run:

```sh
./gradlew spotlessCheck detekt allTests -PwarningsAsErrors=true
```

Also run `./gradlew processSmoke` for runtime or packaging changes, and
`./gradlew -p buildSrc check` for build-tool changes. Rule changes should keep
Lua and legacy YAML counterparts consistent and pass
`nix develop .#texIntegration -c ./gradlew texIntegration`.

Update documentation alongside behavior changes and add an Unreleased changelog
entry for user-visible changes, clearly marking breaking changes. Internal
cleanup, tests and CI changes do not need changelog entries. Use the existing
message catalogs for application diagnostics and translations.

## Website and documentation

Website and documentation fixes for the current release may target `master`
and be published without an arara release; forward these fixes to `development`.
Keep the `master` documentation consistent with released behavior and leave
published release packages and PDFs unchanged.

The website and PDF guides share Markdown sources and the Island of TeX visual
identity. Check relevant changes with `nix develop -c ./gradlew :docs:buildDocs`.
Content-only corrections need documentation checks; changes to documentation
tooling or CI require broader verification.
