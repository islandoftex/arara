# AGENTS.md

## Why this working copy exists (read first)

This tree is `arara`, the TeX automation tool (Kotlin, JVM, Gradle multi-project).
Upstream development happens on GitLab (GitHub is only a mirror). This particular
copy carries a large, externally generated change: the YAML/MVEL rule format was
deprecated in favour of a new Lua rule and configuration format, selectable at
runtime via `--engine yaml|lua` (default: YAML). The change arrived untested and
was then repaired, modernized and simplified by the tagged `oc` sequence, which
continues past the repair: the full suite is green, the tool is smoke-verified
end-to-end on the provisioned JRE 11, and Kotlin, Gradle, the plugins and the
libraries are at their latest stable versions. The same sequence later re-based
the path model on a multiplatform path algebra behind a frozen public surface,
migrated the common-able file helpers to the same backend, and verified the
full suite on a real Windows machine.

`oc0` is the exact pre-repair baseline and every later `oc<n>` tag is one
self-contained, verified step — diff or bisect against the tags when in doubt
about a change.

Source code comments describe what the code does and why — they never
reference the `oc` tags, file names, or line numbers.

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
smoke-verified end-to-end on the provisioned JRE 11 (see oc7 and oc45):
build `:cli:shadowJar`, place the jar with a `rules/` sibling directory and
run it with `~/.gradle/jdks/eclipse_adoptium-11-*/bin/java`. The scratch
matrix in `/tmp/opencode/arara-smoke` covers both engines (execution-test
fixtures for MVEL conditionals, orb tags, sessions and halt; the Lua
engine scenarios including a wrongly typed conditional probe) and the
real TeX toolchain (an official `pdflatex` rule run through both
dialects, plus a file-database double run asserting the record-and-skip
semantics of `changed`).

The full suite is also verified on a real Windows machine. When
replicating there, clone with the checkout line endings pinned (or set
them in the clone before running — the formatter expects the host's
native endings in a working copy without a git repository), then run
the same gates. The runnable jar is
`arara-cli-with-deps-<version>.jar`; the bare module jar next to it
carries no dependencies.

## Commands

Gradle 9.7.1 wrapper, Kotlin 2.4.20 (with `arara.kotlin.api`/`arara.kotlin.language`
pins one minor behind). JVM toolchain is JDK 11
(`arara.jvm.target` in root `gradle.properties`; the foojay resolver in
`settings.gradle.kts` auto-provisions it, so any local JDK can drive the build).

- `./gradlew :cli:run` — run arara from sources (entry point
  `org.islandoftex.arara.cli.CLIKt`).
- `./gradlew :cli:shadowJar` — runnable fat jar at
  `cli/build/libs/arara-cli-with-deps-<version>.jar`; `:cli:build` includes it.
- `./gradlew test` — all tests (JUnit Platform; tests are written in kotlin.test).
  Single class:
  `./gradlew :cli:test --tests "org.islandoftex.arara.cli.utils.ExecutionTest"`.
- `./gradlew detekt` — root task linting every module's main sources
  (`detekt-config.yml`; test sources are excluded). Three informational
  findings are tolerated by configuration and print on every run: the
  `LuaConversion` function count, the deliberate else-throw in
  `Directives.kt` and the `LuaInterpreter` throw count — expected console
  noise, not failures.
- `./gradlew spotlessCheck` / `spotlessApply` — enforced formatting: every
  Kotlin file needs the `// SPDX-License-Identifier: BSD-3-Clause` header,
  and files in `rules/` carry their own YAML/Lua headers. Both rule sets
  are converter-generated artifacts, so their formatting steps
  (`spotlessYaml*`, `spotlessLuaRules*`) are disabled unless
  `-PvalidateYamlRules=true` / `-PvalidateLuaRules=true`.
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
  kotaml-serialized file database (`arara.yaml`), localization.
- `mvel` — legacy YAML rule + config parsing (kotaml, the supported fork of the
  archived kaml; the fork keeps the original `com.charleskorn.kaml` package),
  plus MVEL **directive
  conditionals** which are *not* deprecated and must survive the YAML removal.
- `lua` — new Lua rule + config parsers (LuaJ fork `io.github.taoguan:luaj`).
  The shared helpers live in `core` (`core/…/rules/RuleMethods`,
  `core/…/rules/ConditionalMethods`) and the configuration model in
  `core/…/configuration/`; the module no longer depends on `mvel` at all.
- `cli` — the application: directive parsing, rule dispatch by file extension
  (`.yaml` → mvel parser with a one-time deprecation warning, `.lua` →
  `LuaRuleParser`), configuration file lookup (YAML names first, then Lua),
  the clikt command line, and the shadow jar. **Directive conditionals
  (`if`/`while`/`until`/`unless`) and orb tags (`@{ … }`) in directive
  `options` follow the engine too**: `--engine lua` evaluates them as Lua
  (`LuaConditionalEvaluator` / `LuaOrbTags`, strict boolean, same helper
  registry as the MVEL dialect); the default engine keeps MVEL.
  Single-call conditions are valid in both dialects; compound ones differ
  (`&&`/`||`/`!` vs `and`/`or`/`not`). The Lua helper boundary is
  typed: helper arguments must match the declared type and mismatches
  raise Lua-style `bad argument #<n> to '<helper>'` errors — Lua's
  implicit coercions apply to operators, not to arara's library
  functions (convert with `tostring`/`tonumber`; interpolation and flag
  results still stringify by design).
- `MPPPath` (in `api`) is a sharp edge: it carries **two** paths —
  `providedPath` (exactly what the caller wrote; backs `isAbsolute`,
  `startsWith`, `resolve`) and `referencePath` (absolutized and normalized;
  backs `toString`, I/O, and **equality**). `MPPPath("a") ==
  MPPPath("/abs/cwd/a")` is true. Never assume identity semantics.
  Internally the model runs on a multiplatform path algebra, with the JVM
  bridges kept as the frozen escape hatch; metadata queries follow symlink
  chains (the backend reads them without following), and on Windows,
  drive-prefixed and UNC spellings written with forward slashes are
  re-spelled before parsing so they keep their Windows shape.
- `rules/` — every official rule ships in **both** formats
  (`arara-rule-*.yaml` + `.lua` twins). `scripts/yaml2lua.py` converts rules;
  `scripts/check_lua_rules.lua` smoke-tests all Lua rules with a plain `lua`
  interpreter.

## The YAML deprecation — traps that are easy to miss

`ROADMAP.md` is the authoritative removal playbook (phases, keep/remove lists,
grep checklist). Read it before touching anything YAML/MVEL. Note:
**one YAML library (kotaml) with surfaces of opposite fate** — rules/config
die with the YAML format removal; directive parameters and the file
database stay.

- `kotaml` (`io.heapy.kotaml`, imports read `com.charleskorn.kaml`) — the
  single YAML parser. In the mvel module it parses the deprecated *rules* and
  *config files* (removed together with them); in cli it parses *directive
  parameters* (`% arara: pdflatex: { shell: yes }`, via `parseToYamlNode` +
  `YamlNodeConversion`) and in core the *file database* — both **stay**:
  directive syntax is YAML by specification and is not deprecated. Same for
  MVEL conditionals
  (`% arara: pdflatex if found('log', '!')`).

## Testing quirks

- The end-to-end suite is fixture-driven:
  `cli/src/test/resources/executiontests/<scenario>/` (YAML rules + config per
  scenario), driven by the `ExecutionTest` suite. Fixture paths
  in tests are relative to the `cli/` module directory. The `lua-engine*`
  scenarios plus `LuaExecutionTest` run the same pipeline with
  `RuleEngine.current` flipped to the Lua engine (covering sessions and
  wrongly typed conditional helper calls as well), so both engines have
  automated end-to-end coverage.
- Root `gradle.properties` also drives the build: `arara.jvm.target` (JVM
  target/toolchain) and `arara.kotlin.api` / `arara.kotlin.language`
  (Kotlin api/language versions); the version catalog is
  `gradle/libs.versions.toml`.
- Git history starts at the `oc0` tag (the pristine baseline); every later
  `oc<n>` tag marks one verified repair/modernization step, so regressions can
  be bisected by tags.
