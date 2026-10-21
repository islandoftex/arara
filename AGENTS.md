# AGENTS.md

## Project shape

- Kotlin Multiplatform (KMP) Gradle build, JVM-only targets. Minimum JRE 11.
  Kotlin 2.4, configured in `gradle.properties` (`arara.jvm.target`,
  `arara.kotlin.api`, `arara.kotlin.language`).
- Development happens on GitLab (`gitlab.com/islandoftex/arara`); the GitHub
  repo is a mirror. The working branch is `development` (the default, per
  `origin/HEAD`); `master` receives releases. See README for the upstream note.
- Subprojects declared in `settings.gradle.kts`: `api`, `core`, `lua`, `mvel`,
  `cli`, `docs`, `smoke-tests`. `buildSrc/` holds custom Gradle tasks
  (CTAN/TDS packaging, publication) under `org.islandoftex.arara.build`.
- Dependency versions live in `gradle/libs.versions.toml` (the version catalog).
  Edit versions there, not in individual `build.gradle.kts` files.

### Module roles (dependencies, not obvious from names)

- `api` — public KMP API surfaces (`MPPPath`, `FileType`, directives,etc.). Built
  with `explicitApi()`. Depends on nothing.
- `core` — runtime engine (configuration, file searching, rules, sessions,
  executor). `api(project(":api"))`.
- `mvel` — MVEL expression interpreter used by rules. JVM-only; pulls
  `org.mvel:mvel2`, `kotaml` (the compatible Kaml fork). Interpreter-neutral rule and conditional helpers
  live in `core.rules`; MVEL's registry binds their JVM static methods.
  Shared configuration models live in `core.configuration`; MVEL retains the
  YAML loader (`YamlLocalConfiguration`).
  `MvelCommandEvaluator` supplies results and exit predicates to the CLI through
  the engine-neutral `core.rules.CommandEvaluator` interface.
- `lua` — Luak interpreter for project specifications and Lua rules. JVM helper
  environments, parser, conditionals and interpolation live in `lua.rules`; the
  CLI selects them with `--engine lua` (default: YAML/MVEL). Common-code rule value
  and session adapters live in `lua.rules`. Paths and commands cross as userdata.
  Lua configuration uses `lua.configuration.LuaLocalConfiguration`. Configuration
  and project scripts share fresh `user`/`cwd` globals; config lookup is local
  before global, Lua before YAML within a directory, independently of `--engine`.
  Integer helper arguments require 32-bit values; Longs must be exactly
  representable in Luak.
- `cli` — the actual application; entrypoint is
  `org.islandoftex.arara.cli.CLIKt` (`cli/src/jvmMain/.../CLI.kt`). Produces the
  shaded `arara-cli-with-deps-*.jar` via the `shadowJar` task. Depends on
  `core`, `lua`, `mvel`, `clikt`, `log4j`.
- `docs` — builds the manual/quickstart PDFs from the rendered website HTML
  (see "Documentation" below).
- `smoke-tests` — standalone Kotlin runner for packaged-application checks;
  uses `api` for paths and is not published.
- `rules/` — built-in YAML rule definitions shipped with the distribution
  (74 files like `arara-rule-pdflatex.yaml`). These are data, not Kotlin.

## Generated / gitignored files — do not edit by hand

- `api/src/jvmMain/kotlin/org/islandoftex/arara/api/AraraAPI.kt` is **generated**
  by the `:api:createAraraAPIObject` Gradle task (writes the current
  `project.version` into `AraraAPI.version`). It is matched by `.gitignore`
  (`api/src/**/AraraAPI.kt`). Never edit or commit it; run the task instead.
- `docs/**/version.txt` is written by `:docs:writeVersionFile`, also gitignored.
- `website/public/`, `result/` (Nix), and `**/build/*` are gitignored; the only
  `build/` directories tracked are under `buildSrc/src/**/build/*`.
- `website/themes/devlab-theme` is a pinned submodule; CI checks it out recursively.
  Keep its commit in sync with the `devlab-theme` flake input.
  `:docs:prepareWebsite` stages legacy shortcodes without editing sources.

## Common developer commands

Run from the repo root with `./gradlew`. Current Gradle plugins require
JDK 17+ to build; the Nix development shell supplies JDK 21. The application
still targets JRE 11.

- `./gradlew build` — compile + `jvmTest` for all subprojects.
- `./gradlew allTests` — run all KMP tests (this is what CI runs).
  Use `-PtestJavaVersion=11` to run test workers on Java 11 while Gradle
  continues to run on JDK 17+. The existing Foojay resolver provisions a
  missing toolchain; GitLab caches `.gradle/jdks/` separately across pipelines.
  The first run or a cache miss downloads it; subsequent runs reuse it.
- `./gradlew processSmoke` — test the runnable jar with a JDK 11+, without TeX.
  Select another runtime with `-PsmokeJava=/path/to/java`. JUnit output goes to
  `build/test-results/processSmoke/results.xml`; see `smoke-tests/README.md`.
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

`processSmoke` is a separate CI gate: GitLab tests Java 11/21; the GitHub
mirror tests Java 21 on Linux/macOS/Windows. Run it after the sequence above
when changing runtime behavior or packaging. It builds its own runnable jar.

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

## Documentation build

Run `nix develop -c ./gradlew :docs:buildDocs` to build the website and PDFs;
`assembleCTAN` includes this step. Zola and WeasyPrint come from the dev shell.
`:docs:buildWebsite` writes `website/public`; staging and book HTML go under
`docs/build`. Remove any old Nix-store symlink at `website/public` first.

The Kotlin helpers in `buildSrc` extract `data-arara-document-*` regions and
validate local assets. `./gradlew -p buildSrc check` runs their regression
checks. `nix build .#website` compiles the same helpers and runs the same checks
without downloading Gradle dependencies.

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

`flake.nix` exposes `.#website` (the Zola site) and an independent default
development shell. `nix develop` supplies `corretto21`, `jdk11`,
`weasyprint`, `zola`, `zip`, and `nixpkgs-fmt` for development and documentation
builds. Gradle runs on JDK 21; the CLI compiler uses the JDK 11 toolchain.
The shell registers both Nix JDK homes explicitly and disables automatic
JDK discovery/downloads, since generic Linux JDKs cannot run in the Nix CI
image. Existing `GRADLE_OPTS` settings are preserved.
Run application builds with `nix develop -c ./gradlew :cli:shadowJar`.
There is no Nix application package or gradle2nix artifact lock to maintain.

Keep `flake.lock` committed to pin the Nix tools; update it deliberately with
`nix flake update`. Application dependencies remain in the Gradle version
catalog and build scripts. `nixpkgs-fmt` formats `.nix` files (also enforced
by pre-commit).

## Conventions that differ from defaults

- Application-owned Lua validation and helper diagnostics use the selected
  message catalog. Keep field names, Lua type names and script-supplied errors
  literal; use `formatString` for catalog placeholders without rescanning values.


- Prefer `MPPPath` for IO. Use `java.nio.file.Path` when `MPPPath` lacks the
  required operation, and `java.io.File` only when neither is suitable.
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
- Tests use Kotest (`kotest-runner-junit5-jvm`, 6.2.6) in `core`, `mvel`,
  `cli`; `lua` uses plain `kotlin("test-common")`.
