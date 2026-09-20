# AGENTS.md

## Why this working copy exists (read first)

This tree is `arara`, the TeX automation tool (Kotlin, JVM, Gradle multi-project).
Upstream development happens on GitLab (GitHub is only a mirror). This particular
copy carries a large, externally generated change: the YAML/MVEL rule format was
deprecated in favour of a new Lua rule and configuration format, selectable at
runtime via `--engine yaml|lua` (default: YAML). That change was produced in an
isolated environment and is **untested**. The standing task in this repo:

1. verify everything that was done and fix breakage (see known issues below),
2. bump every library, Kotlin version, plugin, and Gradle to the latest release,
3. simplify the build files, and make everything work again.

A pristine pre-change checkout of the repository is available from the user on
request — diff against it when in doubt about a change.

## Known issues (verified 2026-09-19, fix and tick off)

- The build configures but **does not compile**: `MPPPath.writeText(text, append)`
  (`api/.../files/MPPPath.kt`) now takes two arguments, while call sites still
  pass one — `core/.../files/Database.kt:91` and
  `api/src/test/.../JVMMPPPathTest.kt:22,34` (fails `:core:compileKotlin` and
  `:api:compileTestKotlin`, blocking everything downstream).
- The KMP → plain-JVM migration moved sources to `src/main`/`src/test`, but
  stale `src/jvmTest` paths remain: cli's `ExecutionTest.kt`,
  `InterpreterTest.kt` and `PropertiesTest.kt` all reference
  `src/jvmTest/resources/...`, which does not exist (`src/test/resources/...`
  is the real location). Those tests break at runtime even once compilation is
  fixed.
- The full `./gradlew test` suite has never passed in this state; treat any
  green run as the milestone, and check the Lua engine (`--engine lua`)
  alongside the default YAML engine.

## Commands

Gradle 9.2.0 wrapper (downloads on first run). JVM toolchain is JDK 11
(`arara.jvm.target` in root `gradle.properties`; the foojay resolver in
`settings.gradle.kts` auto-provisions it, so any local JDK can drive the build).

- `./gradlew :cli:run` — run arara from sources (entry point
  `org.islandoftex.arara.cli.CLIKt`).
- `./gradlew :cli:shadowJar` — runnable fat jar at
  `cli/build/libs/arara-cli-with-deps-<version>.jar`; `:cli:build` includes it.
- `./gradlew test` — all tests (JUnit Platform; tests are written in Kotest).
  Single class:
  `./gradlew :cli:test --tests "org.islandoftex.arara.cli.utils.ExecutionTest"`.
- `./gradlew detekt` — root task linting every module's main sources
  (`detekt-config.yml`; test sources are excluded).
- `./gradlew spotlessCheck` / `spotlessApply` — enforced formatting: every
  Kotlin file needs the `// SPDX-License-Identifier: BSD-3-Clause` header,
  and files in `rules/` carry their own YAML/Lua headers. The YAML rule
  steps (`spotlessYaml*`) are disabled unless `-PvalidateYAML=true`.
- `./gradlew dependencyUpdates` — dependency upgrade report (pre-release
  versions filtered out).
- The project **version is derived from `CHANGELOG.md`** by the
  spotless-changelog plugin — do not hardcode it anywhere. `changelogPrint`
  shows the mapping; `changelogBump`/`changelogCheck`/`changelogPush` drive a
  release; the phrase `breaking change` in an entry bumps the major.
- `./gradlew roast` — verifies the shadow jar's highest bytecode version does
  not exceed `arara.jvm.target` (JRE-11 compatibility gate); rerun after
  touching build/dependency configuration.
- `./gradlew assembleCTAN` / `assembleTDSZip` — release packaging. These pull
  in the docs build, which shells out and requires `htmlq`, `weasyprint` and a
  pre-built `website/public/` (zola). Use `nix develop` (`flake.nix` provides
  all of these) before building docs or CTAN/TDS artifacts.

## Layout and architecture

Dependency chain: `api` → `core` → (`mvel`, `lua`) → `cli`.
`docs` holds the manual sources; `buildSrc` holds the publication logic.

- `api` — the public, stable interface. Builds with `explicitApi()`, so
  public members must be explicitly `public`. **`api/.../AraraAPI.kt` is
  generated** by the `createAraraAPIObject` task at compile time (it embeds
  the project version); never edit it by hand.
- `core` — implementations: linear executor/session, file handling, the
  yamlkt-serialized file database (`arara.yaml`), localization.
- `mvel` — legacy YAML rule + config parsing (kaml), plus MVEL **directive
  conditionals** which are *not* deprecated and must survive the YAML removal.
- `lua` — new Lua rule + config parsers (LuaJ fork `io.github.taoguan:luaj`).
  Depends on `mvel` temporarily for shared helper methods and the
  `LocalConfiguration` model — that edge is flagged for removal in `DEPREC.md`.
- `cli` — the application: directive parsing, rule dispatch by file extension
  (`.yaml` → mvel parser with a one-time deprecation warning, `.lua` →
  `LuaRuleParser`), configuration file lookup (YAML names first, then Lua),
  the clikt command line, and the shadow jar.
- `rules/` — every official rule ships in **both** formats
  (`arara-rule-*.yaml` + `.lua` twins). `scripts/yaml2lua.py` converts rules;
  `scripts/check_lua_rules.lua` smoke-tests all Lua rules with a plain `lua`
  interpreter.

## The YAML deprecation — traps that are easy to miss

`DEPREC.md` is the authoritative removal playbook (phases, keep/remove lists,
grep checklist). Read it before touching anything YAML/MVEL. Biggest trap:
**two YAML libraries with opposite fates.**

- `kaml` — parses deprecated YAML *rules* and *config files* (mvel module);
  it is removed together with them.
- `yamlkt` — parses *directive parameters* (`% arara: pdflatex: { shell: yes }`,
  in cli) and the *file database* (in core). It **stays**: directive syntax is
  YAML by specification and is not deprecated. Same for MVEL conditionals
  (`% arara: pdflatex if found('log', '!')`).

## Testing quirks

- The end-to-end suite is fixture-driven:
  `cli/src/test/resources/executiontests/<scenario>/` (YAML rules + config per
  scenario), driven by the Kotest `ExecutionTest` (`@Isolate`). Fixture paths
  in tests are relative to the `cli/` module directory.
- Root `gradle.properties` also drives the build: `arara.jvm.target` (JVM
  target/toolchain) and `arara.kotlin.api` / `arara.kotlin.language`
  (Kotlin api/language versions); the version catalog is
  `gradle/libs.versions.toml`.
- Kotest autoscan is disabled
  (`kotest.framework.classpath.scanning.autoscan.disable=true`).
- This directory has no `.git`, so git tooling and pre-commit hooks cannot be
  used here; rely on the Gradle checks above.
