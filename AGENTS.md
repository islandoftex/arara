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
plus a run behind the log flag asserting the engine, the exit value
and the YAML deprecation warning recorded in the log file, the
rejection of the deprecated orb tag syntax under the Lua engine, a
run configured through a Lua configuration file whose paths are
computed from the values in scope, the missing-rule error naming the
file in the engine's own format, the snake_case helper spellings
through a local Lua rule, the configuration precedence when both
formats are present with the YAML one winning, and the Lua
configuration honored under the default engine, the console mode
surface - the flag's explicit plain run, the configuration key
selecting the console with the log stating a silent tool, the
unoffered-mode errors of both the configuration and the command
line, the styled presentation under a captured stream asserting the
degraded structure with its closing verdict and summary, the styled
dry run verdict without a table, the failed task quoting its own
output with the log keeping the full record, and the log parity
between the plain and the styled presentations)
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
surface compile-checked as well. Driving the build with a
bleeding-edge JDK the Kotlin plugin does not support yet makes it
fall back to the newest supported target in `buildSrc` — the only
compilation surface tapmoc does not govern — with a harmless
Java/Kotlin target mismatch warning; switch the shell back to a
supported JVM and the noise disappears. The foojay resolver in `settings.gradle.kts`
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
  `./gradlew :cli:test --tests "org.islandoftex.arara.cli.execution.ExecutionTest"`.
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
  meaningfully — the interactive dialog surface, which lives with
  the CLI's dialog renderer, no longer needs an exclusion: its
  blocks render and its choices re-prompt under redirected streams
  in the CLI suite, so the dialogs are tested whole, leaving only
  the human typing itself uncovered; logging
  supplier bodies (no backend is configured in tests, by design),
  unreachable defensive arms, JVM bridge glue, and the `main` entry
  point, which is filtered out of the reports and exercised for real
  by the generated smoke suite through the runnable jar. Kover rides
  0.9.11: upstream closed its configuration-cache zeroing (the api
  floor once reported zero lines) by reverting the change that caused
  it, not by fixing it, so any future release that lands
  task-outcome logic again must be re-verified on both a fresh and a
  reused configuration cache entry before adoption. The floors are
  deliberately hard-coded in the build files rather than read from
  `gradle.properties`: a gate moves only through a commit, never
  through a command line `-P` override of a property.
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
  shows the mapping and `changelogCheck` gates it against the changelog
  conventions; a breaking entry bumps the major — recognized by the phrase
  `breaking change` in any of its spellings or by the bold
  `**Breaking:**` marker of the current changelog convention.
- Two spotless-changelog properties carry the release flow: without
  `-Prelease=true` every build appends `-SNAPSHOT` to the derived version,
  and an empty `[Unreleased]` section collapses the derived version onto
  the changelog's own latest release instead of computing a bump. A
  release is therefore declared by the promoted changelog in the
  repository: the promotion — a fresh `[Unreleased]` heading above the
  new release section — and the tag on the promoted master are human
  acts, while CI never touches the changelog and only builds, running
  `publish -Prelease=true` at the tag to ship the stable version the
  promoted section names; plain pushes produce the `-SNAPSHOT` artifact.
  The solstice plugin provides the promotion step: run its
  `promoteChangelog` by hand to promote the unreleased section and
  rewrite the diff-comparison links the promotion leaves behind.
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
  Every helper is bound under both spellings: the historic camelCase name
  of the shared vocabulary and its mechanically derived snake_case alias,
  the Lua-idiomatic form the official rules ship in — each spelling
  reports its own name in argument errors, the camelCase forms retire
  together with the deprecated engine (the removal playbook schedules
  it), and a completeness test walks each environment's globals so a
  helper registered without its alias fails the gate.
- `cli` — the application: directive parsing, rule dispatch by file extension
  (`.yaml` → mvel parser with a one-time deprecation warning, `.lua` →
  `LuaRuleParser`), configuration file lookup (YAML names first, then Lua;
  Lua configuration scripts run with `user` — `home`, `name` — and `cwd`
  in scope, so they compute paths in plain Lua and no placeholder
  expansion applies to them),
  the console renderer seam (the display facade routes every call
  site to the renderer resolved at startup and to the run log that
  writes the display-side log entries; the plain renderer keeps
  the historic silhouette, refreshed in place - the banner names
  the run with the shared version line, the error block titles
  its section on the dashed rule and the run closes with the
  verdict word - and the run log's
  display-side entries hold no console knowledge, so they record
  the same lines whichever console rendered the run, while the
  provenance line at the mode application names the presentation
  - plain, styled, or styled degraded by an undecorated stream - the
  way the engine line names the engine),
  the dialog contract (core defines the semantics and ships the
  headless default that assumes no interactive surface; the
  application wires its implementation - the CLI wires the classic
  terminal dialogs for the plain presentation and the type-tinted
  dialog boxes for the styled one, both sharing the reading
  semantics, and they stay available in every execution mode,
  since a dialog is a conversation with the person who runs the
  tool),
  the console mode surface (the `-c`/`--console` flag and the
  `console`
  configuration key, in both formats, resolved as command line over
  configuration over the plain default; `plain` (the default, the
  historic silhouette with its banner naming the run, its error
  sections titled and its closing verdict) and `styled` (the modern
  presentation on the
  terminal library the help output already renders through,
  degrading to the ASCII structure when the stream cannot carry
  colors or glyphs, and closing a run with a verdict line and a
  summary table naming each file, its task count, its time and its
  status; a verbose styled run shows the command it executes, frames
  the tool's own output with dotted rules and names the
  conditionally skipped directives; a failed task quotes the tail
  of its own output in the dim bar panel, with the full record in
  the log) are the offered modes, and an
  unoffered name is an error rather than a silent fallback.
  On Windows, the console landscape has two color systems: the
  legacy console API every host accepts, and VT sequences, which
  a program must opt into per process (`SetConsoleMode` with
  `ENABLE_VIRTUAL_TERMINAL_PROCESSING`) before the console agrees
  to interpret them. arara does not flip that bit: mordant probes
  the console honestly, so a legacy conhost gets the degraded
  form by design, ConPTY hosts (Windows Terminal, mintty) render
  the full presentation, and a terminal that claims colors it
  cannot process - bash inside a legacy console inherits a
  lying `TERM` - garbles every ANSI byte it receives, arara's
  own help included. The live feedback is gated the same way: it
  requires the user's blessing, the escape vocabulary and a
  live terminal, so a legacy console never sees a frame. The
  glyph vocabulary wants a modern font - Consolas lacks the
  check, the cross and the braille frames, Cascadia Mono and
  DejaVu Sans Mono carry them all - and the
  `VirtualTerminalLevel` registry key is the user-side escape
  hatch for making conhost process VT for every program.
  The clikt command line, and the shadow jar. **Directive conditionals
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
  localized. Exception messages are the one accepted deviation: they
  log as thrown, in the session language, and giving them English
  twins is future work.
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
