# AGENTS.md

## Project shape

- Kotlin Multiplatform (KMP) Gradle build, JVM-only targets. Minimum JRE 21.
  Kotlin 2.4, configured in `gradle.properties` (`arara.jvm.target`,
  `arara.kotlin.api`, `arara.kotlin.language`). Invalid compiler settings fail
  during configuration rather than falling back to another target.
- Development happens on GitLab (`gitlab.com/islandoftex/arara`); the GitHub
  repo is a mirror. The working branch is `development` (the default, per
  `origin/HEAD`); `master` receives releases. See README for the upstream note.
- Subprojects declared in `settings.gradle.kts`: `api`, `core`, `lua`, `mvel`,
  `cli`, `docs`, `smoke-tests`. `buildSrc/` holds custom Gradle tasks
  (CTAN/TDS packaging, publication) under `org.islandoftex.arara.build`.
- Dependency versions live in `gradle/libs.versions.toml` (the version catalog).
  Edit versions there, not in individual `build.gradle.kts` files.
  Modules apply Kotlin plugins through catalog aliases and declare their own
  production/test dependencies. The root reacts to KMP plugin application to
  configure shared compiler, test, documentation and publication conventions.

### Module roles (dependencies, not obvious from names)

- `api` — public KMP API surfaces (`MPPPath`, `FileType`, directives, host hooks, reporting, UI and localization contracts). Built
  with `explicitApi()`. Exposes Okio for scoped reads and normalized path exports.
  MPPPath keeps native JVM path algebra/identity and instance-bound filesystem I/O;
  derived paths retain their backend. Test injection is internal, without global state.
  `ForeignPath` is explicit POSIX/Windows lexical algebra with no filesystem access.
- `core` — runtime engine (configuration, file searching, rules, sessions,
  executor, rule lookup, diagnostic analysis and localized UI defaults).
  `api(project(":api"))`.
- `mvel` — MVEL expression interpreter used by rules. JVM-only; pulls
  `org.mvel:mvel2`, `kotaml` (the compatible Kaml fork). Interpreter-neutral rule and conditional helpers
  live in `core.rules`; MVEL's registry binds their JVM static methods.
  Shared configuration models live in `core.configuration`; MVEL retains the
  YAML loader (`YamlLocalConfiguration`).
  `MvelCommandEvaluator` supplies results and exit predicates to the CLI through
  the engine-neutral `api.rules.CommandEvaluator` interface.
- `lua` — Luak interpreter for project specifications and Lua rules. JVM helper
  environments, parser, conditionals and interpolation live in `lua.rules`; the
  CLI prefers Lua by default; `--preferred-engine` (`-e`) changes the format preference.
  Lookup falls back within each rule directory; the resolved rule determines expression syntax. Common-code rule value
  and session adapters live in `lua.rules`. Paths and commands cross as userdata.
  Lua configuration uses `lua.configuration.LuaLocalConfiguration`. Configuration
  and project scripts share fresh `user`/`cwd` globals; config lookup is local
  before global, Lua before YAML within a directory, independently of `--preferred-engine`.
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

Kotaml handles all YAML parsing: rules, configuration, file databases and directive
parameters. Directive conversion preserves quoted strings and uses conservative
boolean and decimal numeric conversion for plain scalars.

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
targets JRE 21.

Add `-PwarningsAsErrors=true` to reject Kotlin compiler warnings in application
and test compilations, including the smoke-test runner. CI enables this check.
Detekt parses sources against the configured `arara.jvm.target`.

- `./gradlew build` — compile + `jvmTest` for all subprojects.
- `./gradlew allTests` — run all KMP tests (this is what CI runs).
  Use `-PtestJavaVersion=25` to run test workers on Java 25. The Foojay
  resolver provisions missing toolchains outside Nix; GitLab jobs supply their
  selected runtime directly and cache the Java 21 compiler toolchain for Java 25 jobs.
- `./gradlew processSmoke` — test the runnable jar with a JDK 21+, without TeX.
  Select another runtime with `-PsmokeJava=/path/to/java`. JUnit output goes to
  `build/test-results/processSmoke/results.xml`; see `smoke-tests/README.md`.
- `nix develop .#texIntegration -c ./gradlew texIntegration` — test official rules
  against real TeX Live tools in both engines. Reports are under
  `build/test-results/texIntegration`; fixtures and tool output remain in
  `build/texIntegration`. Missing tools fail the suite.
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

`processSmoke` is a separate CI gate: GitLab tests Java 21/25; the GitHub
mirror tests Java 21 on Linux/macOS/Windows. Run it after the sequence above
when changing runtime behavior or packaging. It builds its own runnable jar.

Run them in that order; `detekt` and `spotlessCheck` are cheap and fail fast on
style issues that are noisy to fix after writing a lot of code.

`./gradlew checkRuntimeDependencies` checks the CLI's external runtime dependency
bytecode against `arara.jvm.target`, including JNA and the active multi-release JAR
entries. It runs in CI and `:cli:check`. This detects newer class-file requirements
and preview classes, not references to newer JDK APIs; runtime tests remain necessary.
The report is under `build/reports/runtime-dependencies`.

`./gradlew koverHtmlReport koverXmlReport` produces aggregate coverage reports
under `build/reports/kover`. Module tasks such as `:core:koverHtmlReport` measure
that module's own tests; the root report merges execution across module boundaries.
Packaged subprocess smoke checks are verified separately and do not contribute to
these reports. Reports include all production classes. `./gradlew koverVerify`
checks the aggregate floors (90% lines, 70% branches) and module line floors
(api/core 80%, lua 73%, mvel 79%, cli 90%). GitLab runs this separately on both
supported test runtimes and retains HTML/XML reports for one week.

### Lint / format (non-obvious)

- **Spotless** (`build.gradle.kts`, root): applies `licenseHeader
  ("// SPDX-License-Identifier: BSD-3-Clause")` to all `*.kt` in the listed
  source roots. Every new Kotlin file MUST start with that exact header, or
  `spotlessCheck` fails. Ktlint checks import ordering and unused imports, with a single
  alphabetical group; its other rules remain disabled. Spotless also trims trailing
  whitespace, converts leading tabs and enforces final newlines. Gradle Kotlin DSL
  files use the same import/whitespace rules (no license header there). Shipped YAML
  and Lua rules have standard headers and whitespace checks; existing years are preserved.
- **detekt** config is `detekt-config.yml`; `maxIssues: 0` rejects all findings.
  Complexity, control-flow and layout suppressions are forbidden. Compatibility
  facades retain narrowly documented function-count exceptions. Notable active rules:
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

`./gradlew dokkaGeneratePublicationHtml` builds the combined API reference in
`build/dokka/html`. Every application module and the combined publication reject
Dokka warnings, including undocumented public declarations and unresolved KDoc links. Maven publications carry Kotlin sources and
HTML API reference jars (the `javadoc` classifier).
`./gradlew checkPublications` checks staged Maven artifacts and uses Gradle TestKit
to compile API-only host implementations and build and run an independent Kotlin
consumer of core without CLI or interpreter dependencies
on Java 21. Artifacts are under `build/publication-check/repository`.

## Release / packaging

- Versioning is driven by `spotlessChangelog` from `CHANGELOG.md` (`tagPrefix
  "v"`, release branch `master`, commit message `Release v{{version}}`).
  `./gradlew` resolves `version = spotlessChangelog.versionNext`. Add a
  `breaking change` marker in the changelog to bump major; see
  `build.gradle.kts`.
- CTAN/TDS artifacts use native `Sync` and `Zip` tasks registered by
  `AraraPackaging` in `buildSrc`. `./gradlew assembleCTAN` builds the shaded jar,
  PDFs, source archives, TDS tree and CTAN tree. Existing archive names remain
  under `build/`. `-PpackagingDate=YYYY-MM-DD` fixes the man-page date for
  reproducible builds; the default is the current UTC date.
  Use `-Prelease=true` for a release build (CI sets `PRELEASE`).
- Publishing to the GitLab Maven registry uses `-PjobToken=...` or the
  `CI_JOB_TOKEN` environment variable. Local builds do not require credentials.

## Nix

`flake.nix` exposes `.#website` (the Zola site) and an independent default
development shell. `nix develop` supplies `corretto21`, `jdk25`,
`weasyprint`, `zola`, `zip`, and `nixpkgs-fmt` for development and documentation
builds. Gradle runs on JDK 21; all application modules compile with the JDK 21 toolchain.
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
  literal. The `AraraMessages` contracts live in `api` common code; locale objects live in
  `core` common code; parameterized messages use named, typed functions.


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
  (`ExperimentalTime`, `RequiresOptIn`,
  `ExperimentalUnsignedTypes`) — configured per-source-set in each module's
  `build.gradle.kts`; you usually do not need to add them at call sites.
- Tests use `kotlin.test` assertions and annotations, with JUnit Jupiter on the JVM.
  JUnit provides temporary directories, conditional tests and dynamic test cases.
