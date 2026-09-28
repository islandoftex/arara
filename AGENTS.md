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
smoke-verified end-to-end by the generated suite behind `./gradlew smoke`
(the codified successor of the oc7/oc45 scratch matrix and its script-era
form): one command that builds the runnable jar, renders the scenario
definitions of the helper build into runner files for bats, provisions the
scratch matrix in `/tmp/arara-smoke` from the repository fixtures and the
scenario content authored in the helper build's table, and runs the full
scenario set twice
— once on the underlying JVM and once on the floor runtime named by the
smoke properties, resolved through the toolchain service and skipped
loudly when absent. The scenarios cover both engines (the
simple directive pipeline, the engine happy paths, conditionals in both
dialects — including a wrongly typed conditional probe — sessions in
both engines, engine options with a staged application rule, the
project specification flow resolved through its Lua configuration,
the safe mode rejection of unsafe helper calls, the configured
preamble flow, multiple files as arguments and through the directive
file replication, whole-file extraction beyond the header, call
parameters reaching the rules, the dry run mode, the localized
labels, the loop cap through the command line, the halt
directive's error exit, dispatch by file extension including its
rejection path, and a file-database double run in both a TeX and a
TeX-free shape asserting the record-and-skip semantics of `changed`,
plus a run behind the log flag asserting the engine and the exit value
recorded in the log file, the rejection of the deprecated orb tag
syntax under the Lua engine, and a run configured through a Lua
configuration file whose paths are computed from the values in scope)
and the real TeX toolchain (the official `pdflatex` rule through both
dialects). The historical scenarios the compact matrix once dropped are
restored to the end-to-end set; the remaining fixture shapes stay with
the automated `ExecutionTest` suite, driven from the same fixtures.

The full suite is also verified on a real Windows machine. When
replicating there, clone with `git clone --config core.autocrlf=true`,
then set `git config core.autocrlf false` and `git config core.eol lf`
inside the clone before running anything — the formatter expects the
working copy exactly as committed — then run the same gates. The
functional suite runs through the bash of Git for Windows, which its
tool probes already require; bats-core must be visible to `which
bats` inside that shell (or `SMOKE_BATS` points at it), plus a TeX
distribution for the three typesetting scenarios. Absent tools skip
loudly. The runnable jar is
`arara-cli-with-deps-<version>.jar`; the bare module jar next to it
carries no dependencies.

## Commands

Gradle 9.8.0 wrapper, Kotlin 2.4.20. Compatibility targets live in root
`gradle.properties` (`arara.jvm.target=11`, `arara.kotlin.target=2.4.0`) and
are enforced by the tapmoc plugin — no toolchains are configured, so whatever
JVM drives the build compiles release-11 bytecode, with the JDK 11 API
surface compile-checked as well. The foojay resolver in `settings.gradle.kts`
remains solely to provision the smoke suite's floor runtime through the
`printSmokeJavaRuntime` task (driven by `smoke.jvm.version` and
`smoke.jvm.vendor`, with a loud notice whenever the smoke floor
differs from the build floor).

- `./gradlew :cli:run` — run arara from sources (entry point
  `org.islandoftex.arara.cli.CLIKt`).
- `./gradlew :cli:shadowJar` — runnable fat jar at
  `cli/build/libs/arara-cli-with-deps-<version>.jar`; `:cli:build` includes it.
- `./gradlew test` — all tests (JUnit Platform; tests are written in kotlin.test).
  Single class:
  `./gradlew :cli:test --tests "org.islandoftex.arara.cli.utils.ExecutionTest"`.
- `./gradlew detekt` — root task linting every module's main sources
  (`detekt-config.yml`; test sources are excluded). The lint tolerates zero
  findings: the three former informational ones were retired — two with real
  fixes in shape, one with a justified suppression at its declaration — and
  the configuration now fails the build on any new occurrence.
- `./gradlew spotlessCheck` / `spotlessApply` — enforced formatting: every
  Kotlin file needs the `// SPDX-License-Identifier: BSD-3-Clause` header,
  and files in `rules/` carry their own YAML/Lua headers. Both rule sets
  are converter-generated artifacts, so their formatting steps
  (`spotlessYamlRules*`, `spotlessLuaRules*`) are disabled unless
  `-PvalidateYamlRules=true` / `-PvalidateLuaRules=true`.
- `./gradlew dependencyUpdates` — dependency upgrade report (pre-release
  versions filtered out).
- `./gradlew smoke` — the end-to-end smoke suite: builds the runnable jar
  and the generated bats scenario files, provisions the scratch matrix from
  the repository fixtures, and runs every scenario on the underlying JVM and
  on the floor runtime named by the smoke properties. Requires bats-core and
  git on the path (or `SMOKE_BATS`); scenarios whose tools are absent skip
  loudly, and `-Psmoke.strict=true` escalates every skip into a failure.
  Per-leg TAP reports land in `build/reports/smoke/`.
- `./gradlew koverHtmlReport` — aggregated coverage report across all five
  modules (`build/reports/kover/html`; `koverXmlReport` for machine-readable
  numbers, `koverLog` for a console summary). `koverVerify` enforces the
  coverage floors in two tiers: the aggregate bound at the root guards
  the merged truth of all tests across all modules, and each module's
  own floor guards its own suite - the module reports see only their
  own tests, so the classes of `api` and `core` show most of their
  coverage through other modules' suites while their floors keep watch
  over their own. Every floor sits below its measured baseline so
  ordinary churn passes while a coverage regression fails the gate. The
  baseline is honest, not padded: what it excludes cannot be covered
  meaningfully — the interactive dialog family pending review, logging
  supplier bodies (no backend is configured in tests, by design),
  unreachable defensive arms, JVM bridge glue, and the `main` entry
  point, which is filtered out of the reports and exercised for real
  by the generated smoke suite through the runnable jar.
- `./gradlew updateHeaders` — refreshes the copyright year intervals in the
  license, the YAML rules and the Lua rules to the current year. Run it on
  demand when the year rolls over.
- `./gradlew -PwarningsAsErrors=true test detekt spotlessCheck koverVerify
  :dokkaGeneratePublicationHtml` —
  the strict gate proving the tree compiles without a single Kotlin warning
  and holds its coverage floors; the documentation generation joins the
  gate and fails on any documentation warning.
- The project **version is derived from `CHANGELOG.md`** by the
  spotless-changelog plugin — do not hardcode it anywhere. `changelogPrint`
  shows the mapping; `changelogBump`/`changelogCheck`/`changelogPush` drive a
  release; a breaking entry bumps the major — recognized by the phrase
  `breaking change` in any of its spellings or by the bold
  `**Breaking:**` marker of the current changelog convention.
- The former packaging tasks (`roast`, `assembleCTAN*`, `assembleTDS*`, the
  `docs` Gradle module) were removed from the build: the doc/CTAN toolchain
  (zola, weasyprint, htmlq) is unavailable here and out of scope. The `docs/`
  directory still holds the manual sources (inert), and the removed tasks
  remain recoverable from the `oc1` tag.

## Layout and architecture

Dependency chain: `api` → `core` → (`mvel`, `lua`) → `cli`.
`docs` holds the manual sources; `buildSrc` holds the publication
logic (the four library modules alone — the CLI application ships only
as the runnable jar).

- `api` — the public, stable interface. Builds with `explicitApi()`, so
  public members must be explicitly `public`. **`api/.../AraraAPI.kt` is
  generated** by the `createAraraAPIObject` task at compile time (it embeds
  the project version); never edit it by hand.
- `core` — implementations: linear executor/session, file handling, the
  kotaml-serialized file database (`arara.yaml`), localization (the
  `AraraMessages` interface: typed members per message, one
  implementation object per locale, compile-enforced completeness —
  a locale missing a message fails the build; `MPPLocale` is the only
  localization type left in `api`).
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
  `LuaRuleParser`), configuration file lookup (YAML names first, then Lua;
  Lua configuration scripts run with `user` — `home`, `name` — and `cwd`
  in scope, so they compute paths in plain Lua and no placeholder
  expansion applies to them),
  the clikt command line, and the shadow jar. **Directive conditionals
  (`if`/`while`/`until`/`unless`) and interpolation in directive
  `options` follow the engine too**: `--engine lua` evaluates conditionals
  and interpolates `{{ … }}` tags as Lua (`LuaConditionalEvaluator` /
  `LuaInterpolation`, strict boolean, same helper registry as the MVEL
  dialect; the `@{ … }` orb tags of the deprecated YAML engine are
  rejected with a hint); the default engine keeps MVEL orb tags.
  Single-call conditions are valid in both dialects; compound ones differ
  (`&&`/`||`/`!` vs `and`/`or`/`not`). The Lua helper boundary is
  typed: helper arguments must match the declared type and mismatches
  raise Lua-style `bad argument #<n> to '<helper>'` errors — Lua's
  implicit coercions apply to operators, not to arara's library
  functions (convert with `tostring`/`tonumber`; interpolation and flag
  results still stringify by design). Log lines are always English,
  hardcoded at their sites — the log file is a shared diagnostic
  artifact, so it reads the same in every language; the console stays
  localized.
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
  (`arara-rule-*.yaml` + `.lua` twins). A test in the `lua` module parses
  every shipped Lua rule through the real parser, so the whole ruleset is
  structure-validated in every gate.

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
- Root `gradle.properties` also drives the build: `arara.jvm.target` and
  `arara.kotlin.target` (the compatibility levels the tapmoc plugin
  enforces); the version catalog is `gradle/libs.versions.toml`.
- Git history starts at the `oc0` tag (the pristine baseline); every later
  `oc<n>` tag marks one verified repair/modernization step, so regressions can
  be bisected by tags.
