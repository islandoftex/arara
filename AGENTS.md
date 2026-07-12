# AGENTS.md

## Project shape

- Kotlin Multiplatform (KMP) Gradle build, JVM-only targets. Minimum JRE 11.
  Kotlin 2.2, configured in `gradle.properties` (`arara.jvm.target`,
  `arara.kotlin.api`, `arara.kotlin.language`).
- Development happens on GitLab (`gitlab.com/islandoftex/arara`); the GitHub
  repo is a mirror. The working branch is `development` (the default, per
  `origin/HEAD`); `master` receives releases. See README for the upstream note.
- Subprojects declared in `settings.gradle.kts`: `api`, `core`, `lua`, `mvel`,
  `kotlin-dsl`, `cli`, `docs`. `buildSrc/` holds custom Gradle tasks
  (CTAN/TDS packaging, publication) under `org.islandoftex.arara.build`.
- Dependency versions live in `gradle/libs.versions.toml` (the version catalog).
  Edit versions there, not in individual `build.gradle.kts` files.

### Module roles (dependencies, not obvious from names)

- `api` — public KMP API surfaces (`MPPPath`, `FileType`, directives,etc.). Built
  with `explicitApi()`. Depends on nothing.
- `core` — runtime engine (configuration, file searching, rules, sessions,
  executor). `api(project(":api"))`.
- `mvel` — MVEL expression interpreter used by rules. JVM-only; pulls
  `org.mvel:mvel2`, `kaml`.
- `lua` — Lua interpreter (`luak`) for Lua-based rules. KMP common.
- `kotlin-dsl` — optional Kotlin scripting DSL for rules; pulls
  `kotlin-scripting-*` and a `runtimeOnly` `compiler-embeddable` (heavy).
- `cli` — the actual application; entrypoint is
  `org.islandoftex.arara.cli.CLIKt` (`cli/src/jvmMain/.../CLI.kt`). Produces the
  shaded `arara-cli-with-deps-*.jar` via the `shadowJar` task. Depends on
  `core`, `lua`, `mvel`, `clikt`, `log4j`.
- `docs` — builds the manual/quickstart PDFs from the rendered website HTML
  (see "Documentation" below).
- `rules/` — built-in YAML rule definitions shipped with the distribution
  (74 files like `arara-rule-pdflatex.yaml`). These are data, not Kotlin.

## Generated / gitignored files — do not edit by hand

- `api/src/jvmMain/kotlin/org/islandoftex/arara/api/AraraAPI.kt` is **generated**
  by the `:api:createAraraAPIObject` Gradle task (writes the current
  `project.version` into `AraraAPI.version`). It is matched by `.gitignore`
  (`api/src/**/AraraAPI.kt`). Never edit or commit it; run the task instead.
- `docs/**/version.txt` is written by `:docs:writeVersionFile` on every build
  (`outputs.upToDateWhen { false }`), also gitignored.
- `website/public/`, `result/` (Nix), and `**/build/*` are gitignored; the only
  `build/` directories tracked are under `buildSrc/src/**/build/*`.
- `website/themes/juice` is a git submodule (`.gitmodules`). Use
  `GIT_SUBMODULE_STRATEGY=recursive` semantics; CI checks it out automatically.

## Common developer commands

Run from the repo root with `./gradlew`. JDK 11+ required.

- `./gradlew build` — compile + `jvmTest` for all subprojects.
- `./gradlew allTests` — run all KMP tests (this is what CI runs).
- `./gradlew :cli:test` or `./gradlew :core:jvmTest` — tests for one subproject.
  Single test class: `./gradlew :core:jvmTest --tests "fully.qualified.ClassName"
  --tests "*PartialName*"`.
- `./gradlew :cli:shadowJar` — produces `cli/build/libs/arara-cli-with-deps-*.jar`,
  the runnable distribution artifact. `:cli:assembleDist` depends on it and also
  emits the `cli/build/distributions/*.zip`.
- `./gradlew :cli:jvmJar` / `:cli:installDist` — application distribution.

### Required verification order (matches CI)

CI (`testtemplate` in `.gitlab-ci.yml`) runs `./gradlew allTests` **then**
`./gradlew detekt` as separate jobs, and `spotlessCheck` is its own job. The
full local pre-push sequence is:

```
./gradlew spotlessCheck detekt allTests
```

Run them in that order; `detekt` and `spotlessCheck` are cheap and fail fast on
style issues that are noisy to fix after writing a lot of code.

### Lint / format (non-obvious)

- **Spotless** (`build.gradle.kts`, root): applies `licenseHeader
  ("// SPDX-License-Identifier: BSD-3-Clause")` to all `*.kt` in the listed
  source roots. Every new Kotlin file MUST start with that exact header, or
  `spotlessCheck` fails. `ktlint` is intentionally disabled (see the comment in
  `build.gradle.kts`); only `trimTrailingWhitespace`,
  `leadingTabsToSpaces`, `endWithNewline`, and the license header are enforced.
  Gradle Kotlin DSL files (`*.kts`) are formatted too (no license header there).
- **detekt** config is `detekt-config.yml`; `maxIssues: 20` is allowed (not
  zero), so a green build does not mean zero findings. Notable active rules:
  `MaxLineLength` 120, `MagicNumber` (ignored in tests), `ForbiddenComment`
  rejects `TODO:`, `FIXME:`, `STOPSHIP:`, `WildcardImport` (except
  `java.util.*`). Tests are excluded from many rules.
- `.editorconfig`: 4-space indent, LF, UTF-8; Kotlin files end with newline.
  YAML/shell use 2-space indent.

### pre-commit

`.pre-commit-config.yaml` wires standard hooks plus `forbid-tabs`/`forbid-crlf`
and `nixpkgs-fmt` for `.nix` files. The `gradle-task` hook for `spotlessCheck`
is configured with `types: [rust]` (so it does not actually trigger on Kotlin
by default) — do not rely on it; run `./gradlew spotlessCheck` manually.

## Documentation build (has external prerequisites)

`docs/buildDocs` → `buildManual` + `buildQuickstartGuide` run shell scripts
(`docs/htmlmanualtopdf.sh`, `docs/htmlquickstarttopdf.sh`) that:

1. Require `htmlq`, `sed`, and `weasyprint` on PATH. These are provided by the
   Nix dev shell (`nix develop`), not by a plain JDK checkout.
2. Read the already-rendered website HTML from `website/public/` — i.e. the
   website (`zola build`) must be built first. CI builds the website in the
   `pages` job before the CTAN/PDF step.
3. Write `/tmp/arara-manual/...` (hardcoded `/tmp` path; non-Linux systems need
   adjustment).

The `:docs:buildDocs` task is a dependency of the CTAN packaging tasks
(`assembleTDSTree`, `assembleDocumentationSourceZip`). You cannot produce a
CTAN/TDS zip without first having the rendered website and the PDF tools.

## Release / packaging

- Versioning is driven by `spotlessChangelog` from `CHANGELOG.md` (`tagPrefix
  "v"`, release branch `master`, commit message `Release v{{version}}`).
  `./gradlew` resolves `version = spotlessChangelog.versionNext`. Add a
  `breaking change` marker in the changelog to bump major; see
  `build.gradle.kts`.
- CTAN/TDS artifacts are built by custom tasks in `buildSrc/`
  (`TDSTreeBuilderTask`, `TDSZipBuilderTask`, `CTANTreeBuilderTask`,
  `CTANZipBuilderTask`, `SourceZipBuilderTask`, `DocumentationSourceZipBuilderTask`).
  The chained entry point is `./gradlew assembleCTAN`, which transitively runs
  `:cli:shadowJar`, `:docs:buildDocs`, and the source/docs zips. Use
  `-Prelease=true` for a real release build (CI sets `PRELEASE`).
- Publishing to the GitLab Maven registry requires `-PjobToken=...` (CI job
  token). Without `JobToken` the root build prints a warning but still
  configures publications; local builds can ignore it.

## Nix

`flake.nix` exposes `.#default` (gradle2nix build using `gradle.lock`) and
`.#website` (zola site). `nix develop` gives a shell with `corretto11`,
`htmlq`, `weasyprint`, `zola`, `zip`, `nixpkgs-fmt`, and `gradle2nix` — the
recommended way to get the docs-build prerequisites. `nixpkgs-fmt` formats
`.nix` files (also enforced by pre-commit). CI verifies `gradle.lock` stays in
sync with `build.gradle.kts`/`libs.versions.toml` via `gradle2nix` in the
`test:flake-dependencies` job — when you change versions, regenerate
`gradle.lock` or that job will fail.

## Conventions that differ from defaults

- License header `// SPDX-License-Identifier: BSD-3-Clause` is mandatory on
  every `.kt` (enforced by Spotless). Missing it is the most common failure for
  new files.
- Code uses tabs → spaces conversion at the Spotless stage; write with either
  but the canonical form is 4 spaces (`.editorconfig`).
- KMP `expect`/`actual` classes are enabled via `-Xexpect-actual-classes` in
  `api` and `core`.
- Several `@OptIn` annotations are project-wide
  (`AraraMessages`, `ExperimentalTime`, `RequiresOptIn`,
  `ExperimentalUnsignedTypes`) — configured per-source-set in each module's
  `build.gradle.kts`; you usually do not need to add them at call sites.
- Tests use Kotest (`kotest-runner-junit5-jvm`, 6.0.0.M12 milestone — note the
  pre-stable version) in `core`, `mvel`, `kotlin-dsl`, `cli`; `lua` uses plain
  `kotlin("test-common")`.
