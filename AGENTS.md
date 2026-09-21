# AGENTS.md

## Why this working copy exists (read first)

This tree is `arara`, the TeX automation tool (Kotlin, JVM, Gradle multi-project).
Upstream development happens on GitLab (GitHub is only a mirror). This particular
copy carries a large, externally generated change: the YAML/MVEL rule format was
deprecated in favour of a new Lua rule and configuration format, selectable at
runtime via `--engine yaml|lua` (default: YAML). The change arrived untested and
was then repaired, modernized and simplified by the tagged sequence `oc0` through
`oc12`: the full suite is green, detekt and spotless pass with zero findings, the
tool is smoke-verified end-to-end on the provisioned JRE 11, and Kotlin, Gradle,
the plugins and the libraries are at their latest stable versions.

`oc0` is the exact pre-repair baseline and every later `oc<n>` tag is one
self-contained, verified step — diff or bisect against the tags when in doubt
about a change.

## Known issues (verified 2026-09-19; resolved during oc1..oc5)

- ~~The build configures but does not compile~~ — fixed in `oc3`: the
  KMP migration had dropped documented parameter defaults
  (`MPPPath.writeText`, `Session.updateEnvironmentVariables`); they are
  restored.
- ~~Stale `src/jvmTest` paths in tests~~ — fixed in `oc4`: ExecutionTest,
  InterpreterTest and PropertiesTest now point at `src/test/resources`.
- ~~The test suite has never passed~~ — fixed in `oc5`: the full suite is
  green (from a clean build). The single remaining failure was a
  machine-dependent assertion in `LuaLocalConfigurationTest` that assumed
  `MPPPath.toString()` keeps relative paths (it does not — see the
  `providedPath`/`referencePath` duality in `MPPPath`).

Remaining caveat: none — both engines and the Lua project pipeline are
smoke-verified end-to-end on the provisioned JRE 11 (see oc7): build
`:cli:shadowJar`, place the jar with a `rules/` sibling directory and run it
with `~/.gradle/jdks/eclipse_adoptium-11-*/bin/java`.

## Commands

Gradle 9.7.1 wrapper, Kotlin 2.4.20 (with `arara.kotlin.api`/`arara.kotlin.language`
pins one minor behind). JVM toolchain is JDK 11
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
- `./gradlew -PwarningsAsErrors=true test detekt spotlessCheck` — the strict
  gate proving the tree compiles without a single Kotlin warning.
- The project **version is derived from `CHANGELOG.md`** by the
  spotless-changelog plugin — do not hardcode it anywhere. `changelogPrint`
  shows the mapping; `changelogBump`/`changelogCheck`/`changelogPush` drive a
  release; the phrase `breaking change` in an entry bumps the major.
- The former packaging tasks (`roast`, `assembleCTAN*`, `assembleTDS*`, the
  `docs` Gradle module) were removed from the build: the doc/CTAN toolchain
  (zola, weasyprint, htmlq) is unavailable here and out of scope. The `docs/`
  directory still holds the manual sources (inert), and the removed tasks
  remain recoverable from the `oc1` tag.

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
  the clikt command line, and the shadow jar. **Directive conditionals
  (`if`/`while`/`until`/`unless`) and orb tags (`@{ … }`) in directive
  `options` follow the engine too**: `--engine lua` evaluates them as Lua
  (`LuaConditionalEvaluator` / `LuaOrbTags`, strict boolean, same helper
  registry as the MVEL dialect); the default engine keeps MVEL.
  Single-call conditions are valid in both dialects; compound ones differ
  (`&&`/`||`/`!` vs `and`/`or`/`not`).
- `MPPPath` (in `api`) is a sharp edge: it carries **two** paths —
  `providedPath` (exactly what the caller wrote; backs `isAbsolute`,
  `startsWith`, `resolve`) and `referencePath` (absolutized and normalized;
  backs `toString`, I/O, and **equality**). `MPPPath("a") ==
  MPPPath("/abs/cwd/a")` is true. Never assume identity semantics.
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
  in tests are relative to the `cli/` module directory. The `lua-engine`
  scenario plus `LuaExecutionTest` run the same pipeline with
  `RuleEngine.current` flipped to the Lua engine, so both engines have
  automated end-to-end coverage.
- Root `gradle.properties` also drives the build: `arara.jvm.target` (JVM
  target/toolchain) and `arara.kotlin.api` / `arara.kotlin.language`
  (Kotlin api/language versions); the version catalog is
  `gradle/libs.versions.toml`.
- Kotest autoscan is disabled
  (`kotest.framework.classpath.scanning.autoscan.disable=true`).
- Git history starts at the `oc0` tag (the pristine baseline); every later
  `oc<n>` tag marks one verified repair/modernization step, so regressions can
  be bisected by tags.
