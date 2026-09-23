# Pulling the plug on YAML rules and configuration files

This document is the removal playbook for the deprecated YAML *rule* and
*configuration* formats. It lists, file by file, what has to happen when we
finally remove them in favor of the Lua formats introduced in version 8.0.

> **Scope.** Only rules (`arara-rule-*.yaml`, user rules) and configuration
> files (`.araraconfig.yaml`, `araraconfig.yaml`, `.arararc.yaml`,
> `arararc.yaml`) are removed. YAML itself does **not** leave the code base:
> directives embed YAML maps as their parameter syntax, e.g.
>
> ```tex
> % arara: pdflatex: { shell: yes }
> ```
>
> and that syntax is stable, documented user interface. See
> [What stays](#what-stays) before deleting anything.

## What stays

These components look YAML- or MVEL-related but are **out of scope**; do not
remove them as part of this effort:

> **One YAML library.** Since yamlkt was replaced, the code base uses a
> single YAML parser:
>
> | Library | Coordinates | Used by | Fate |
> | --- | --- | --- | --- |
> | **kotaml** | `io.heapy.kotaml` (a supported fork of the archived kaml; the fork keeps the original `com.charleskorn.kaml` package) | YAML rules (`mvel/…/rules/Rule.kt`), YAML config (`core/…/configuration/LocalConfiguration.kt`), directive parameters (`cli/…/DirectiveUtils.kt`), file database (`core/…/Database.kt`) | rules and config are removed with them; directive parameters and the database **stay** |
>
> Whenever this document says the YAML *library for rules and
> configuration* is removed, it means the kotaml dependency **of the
> mvel module only**. Directives keep being parsed as YAML, and the file
> database keeps being serialized as YAML, both via kotaml in the cli
> and core modules.

1. **Directive parameter parsing.** `cli/…/ruleset/DirectiveUtils.kt` parses
   the `{ key: value }` part of a directive with **kotaml**
   (`Yaml.default.parseToYamlNode` plus the `YamlNodeConversion` object).
   Directives are YAML by specification, independently of the rule format
   the directive eventually resolves to. Keep `libs.kotaml` in
   `cli/build.gradle.kts`.
2. **The file database.** `core/…/files/Database.kt` serializes arara's
   hash database with kotaml. Unrelated to rules/config; keep `libs.kotaml`
   in `core/build.gradle.kts`. (If the database format should ever change,
   that is a separate decision.)
3. **MVEL directive conditionals.** The `if`/`while`/`until`/`unless`
   conditionals of a directive (`% arara: pdflatex if found('log', '!')`)
   are MVEL expressions and are *not* deprecated. (A Lua-based
   counterpart tied to the `--engine` flag exists, and conditionals turn
   Lua with the phase 1 default flip; see the version 9 outlook at the
   end of this document.) Consequently the
   following must survive the removal:
   - `mvel/…/rules/DirectiveConditionalEvaluator.kt`
   - `core/…/rules/ConditionalMethods.kt`, `core/…/utils/MethodUtils.kt`
     (already moved out of the mvel module)
   - `mvel/…/utils/MvelState.kt` (conditional methods, preambles)
   - `api/…/AraraExceptionWithHeader.kt` (already moved out of the
     mvel module)
   - the `org.mvel:mvel2` dependency
4. **Preambles.** Preambles are plain strings of directives; they are
   stored on `MvelState` for historic reasons only. They keep working with
   both engines.
5. **`RuleMethods`.** The helper methods are engine-agnostic (the Lua
   bridges in `lua/…/rules/LuaRuleEnvironment.kt` call them); they have
   been **moved to `core/…/rules/` ahead of the removal** (see step 2.4),
   so they survive the deletion untouched.

## Prerequisites for removal

Do not start the removal before all of the following hold:

- [ ] At least one stable release has shipped with the Lua engine
      available (`--engine lua`), the deprecation warning for YAML rules
      (`cli/…/ruleset/RuleUtils.kt`) and the CHANGELOG/manual notices.
- [ ] A release has shipped with **Lua as the default engine** (see
      phase 1) so users had a cycle to notice breakage while YAML was
      still available as an opt-in fallback.
- [ ] The official rules in the Lua format have reached TeX Live/CTAN
      (they ship automatically, `buildSrc/…/TDSTreeBuilderTask.kt` copies
      the whole `rules/` directory).
- [ ] The migration path is documented for users: `scripts/yaml2lua.py`
      converts rule files; the manual's Configuration chapter documents the
      Lua configuration format.

## Phase 1 — flip the default engine

Small, reversible release:

1. `cli/…/ruleset/RuleEngine.kt`: change the companion default from
   `var current: RuleEngine = YAML` to `LUA`.
2. `cli/…/CLI.kt`: change `.default(RuleEngine.YAML)` to
   `.default(RuleEngine.LUA)` on the `--engine` option and update its help
   text ("'yaml' (deprecated) or 'lua' (default)").
3. Update the usage block and the `--engine` bullet in
   `website/content/manual/CLI.md`, plus a CHANGELOG entry.
4. Consider upgrading the one-time `logger.warn` in `RuleUtils` to a
   visible `DisplayUtils` warning when the YAML engine is selected
   explicitly.

## Phase 2 — remove YAML rules

### 2.1 CLI module

- `cli/…/ruleset/RuleFormat.kt`: delete the `MVEL("yaml")` constant
  (and the now-unneeded `@Deprecated` annotation and
  `@Suppress("DEPRECATION")` sites that reference it).
- `cli/…/ruleset/RuleEngine.kt`: delete the `YAML` constant; at this point
  the enum only distinguishes `LUA` (and, one day, the Kotlin DSL). Either
  keep it as the extension point for the DSL or delete the enum together
  with the `--engine` option (phase 4).
- `cli/…/ruleset/RuleUtils.kt`: remove the `RuleFormat.MVEL.extension`
  branch, `warnAboutYamlDeprecation()` and the
  `org.islandoftex.arara.mvel.rules.Rule` import. Replace the branch with a
  friendly error for leftover `.yaml` rules, e.g. "YAML rules are no
  longer supported since version X; convert the rule with
  scripts/yaml2lua.py", so users with custom rules get guidance instead of
  a rule-not-found error.
- `cli/…/interpreter/Interpreter.kt`:
  - delete `evaluateMvelCommand(…)`, `mvelExitCheck(…)` and the
    `is SerialRuleCommand ->` branch of `executeCommand` (keep the
    `LuaRuleCommand` branch and the `else` error);
  - delete `resultToList(…)` if nothing else uses it;
  - remove `parameters.plus(MvelState.ruleMethods)` from the flag
    evaluation `context` in `parseArguments` — Lua argument processors
    ignore the method map, it was only consumed by MVEL flags;
  - drop the imports of `SerialRuleCommand`, `TemplateRuntime` and — if
    the preamble handling is the only remaining consumer — narrow the
    `MvelState` usage accordingly.
  - **Keep** `DirectiveConditionalEvaluator` and everything conditional-
    related (see [What stays](#what-stays)).

### 2.2 MVEL module

- Delete `mvel/…/rules/Rule.kt`, `mvel/…/rules/RuleArgument.kt` and
  `mvel/…/rules/SerialRuleCommand.kt`.
- Remove `libs.kotaml` from `mvel/build.gradle.kts` and drop the
  `kotaml` version/library entries from `gradle/libs.versions.toml`
  (kotaml's only two consumers are the YAML rule and configuration
  parsers — verify with `grep -rn "com.charleskorn" --include="*.kt" .`
  before deleting; the fork kept the original package, so the grep
  below is unaffected by the coordinate change). Do **not** touch `libs.yamlkt`: it parses directives
  and the database (see [What stays](#what-stays)). Remove the
  `kotlin("plugin.serialization")` plugin from the mvel module if no
  `@Serializable` class remains there.
- The module keeps its name (MVEL conditionals still justify it) unless
  you want to fold the survivors into `core` — in that case move
  `DirectiveConditionalEvaluator`, `ConditionalMethods`, `MethodUtils`,
  `MvelState`, `AraraExceptionWithHeader` and the `mvel2` dependency, then
  delete the module from `settings.gradle.kts` (module list) and from the
  root `build.gradle.kts` (spotless targets for `mvel/build.gradle.kts`
  and `mvel/src/**/*.kt`, detekt source list), and adjust the publication.

### 2.3 Configuration model

- `LocalConfiguration` and `SerialFileType` already live in
  `core/…/configuration/` (moved ahead of the removal; the Lua parser
  constructs the class directly). At removal time:
  - delete the kotaml-based `companion object { fun load(…) }` and the
    `@Serializable` annotations;
  - drop the `!config` header check, which was YAML-specific.
- `cli/…/configuration/ConfigurationUtils.kt`:
  - shrink `configFileForProject` to a single Lua search:
    `configFileWithExtension(project, "lua")` — the YAML-first indirection
    (`listOf("yaml", "lua").firstNotNullOfOrNull { … }`) goes away;
  - in `loadLocalConfiguration`, remove the `"yaml" -> …` branch; as with
    rules, prefer a helpful error over silence when a
    `.araraconfig.yaml`-style file is still present: detect the YAML names
    and tell the user to migrate (the manual's Configuration chapter has
    the key-by-key mapping; the formats are close enough that no converter
    script is needed, but writing a `yamlconfig2lua` sibling of
    `scripts/yaml2lua.py` would be a nice touch);
  - update the class KDoc describing the lookup order.

### 2.4 Rule helper methods

`RuleMethods` must outlive MVEL rules because the Lua bridges delegate
to it. It already lives in `core/…/rules/`, moved ahead of the removal
together with `ConditionalMethods`, `MethodUtils` and
`AraraExceptionWithHeader`, and the `lua` module no longer depends on
the mvel module at all. At removal time:

- remove the entries that `MvelState.ruleMethods` reflected into the
  MVEL evaluation context, and — once no consumer is left — the
  `ruleMethods` map in `MvelState` itself (`conditionalMethods` stays).

### 2.5 Rule files and build glue

- Delete `rules/arara-rule-*.yaml` (74 files); the `.lua` twins remain.
- Root `build.gradle.kts`: delete the `yaml { target("rules/*.yaml") … }`
  spotless block (the `format("luaRules")` block stays). Keep the Kotlin
  spotless block untouched.
- `scripts/yaml2lua.py` and `scripts/check_lua_rules.lua`: keep for at
  least one more release as the user-facing migration tool, referenced
  from the removal error message and the CHANGELOG; delete later.
- CTAN/TDS: nothing to do — `TDSTreeBuilderTask` copies `rules/`
  wholesale; verify the generated tree once after deletion.
- CI: `.gitlab-ci.yml` needs no change, but run the full pipeline —
  `spotlessCheck` in particular — after removing the yaml block.

### 2.6 Tests

- The end-to-end fixtures under `cli/src/test/resources/executiontests/`
  consist of YAML rules (`echoone.yaml`, …) and YAML configuration files
  (`arararc.yaml`) per scenario — port them to the Lua formats
  (`scripts/yaml2lua.py` handles the rules; the configs are one-liners).
  The `directive-with-mvel-options` and `conditionals` scenarios stay:
  directive options and conditionals are out of scope.
- Note that the `arara.yaml` file handled by the "track changes" case in
  `cli/src/test/…/ExecutionTest.kt` is the *database* (see
  [What stays](#what-stays)), not a rule or configuration file — leave it
  alone.
- Delete the YAML rule/configuration parser tests in the mvel module (if
  present) and make sure `lua/src/test/…/rules/LuaRuleParserTest.kt` and
  `lua/src/test/…/configuration/LuaLocalConfigurationTest.kt` cover any
  behavior the deleted tests asserted.
- Grep the test sources for `!config` and `arara-rule-` + `.yaml`.

### 2.7 Documentation

- `website/content/manual/YAML.md`: delete the chapter (it documents the
  YAML rule/config syntax); check `_index.md`/menu weights.
- `website/content/manual/MVEL.md`: **keep**, but rewrite the framing:
  MVEL is now only the directive-conditional language. Move rule-specific
  MVEL content out or drop it.
- `website/content/manual/Concepts.md`: make the Lua format the primary
  description of a rule; remove the YAML explanation and the "Lua rule
  format" message box (it becomes the main text).
- `website/content/manual/Configuration.md`: same for the configuration —
  the Lua section becomes the main text, the YAML keys/`!config` parts go,
  the lookup-order paragraph shrinks to the four `.lua` names.
- `website/content/manual/CLI.md`: update/remove the `--engine`
  description depending on phase 4.
- `website/content/manual/Methods.md`: still accurate (the helpers exist
  as Lua globals); adjust examples from MVEL to Lua syntax.
- Add a prominent CHANGELOG entry under a new major version and a news
  post with the migration guide.

## Phase 3 — grep checklist

Before tagging the release, all of these should come back empty (or only
match this file and the CHANGELOG/news):

```sh
grep -rn "kaml"                    --include="*.kt" --include="*.kts" .
grep -rn "SerialRuleCommand"       --include="*.kt" .
grep -rn "RuleFormat.MVEL"         --include="*.kt" .
grep -rn "RuleEngine.YAML"         --include="*.kt" .
grep -rn "mvel.rules.Rule"         --include="*.kt" .
grep -rn "MvelState.ruleMethods"   --include="*.kt" .
grep -rn "!config"                 --include="*.kt" --include="*.md" .
ls rules/*.yaml 2>/dev/null
```

And these must still match (guard rails — if any comes back empty, scope
was exceeded):

```sh
grep -rn "yamlkt" cli/src core/src               # directive YAML + database
grep -rn "yamlkt" cli/build.gradle.kts core/build.gradle.kts
grep -rn "DirectiveConditionalEvaluator" cli/src # MVEL conditionals
grep -rn "TemplateRuntime\|MVEL" mvel/src        # conditional evaluation
```

In particular, `% arara: pdflatex: { shell: yes }` must still parse after
the removal — the end-to-end tests in
`cli/src/test/resources/executiontests/directive-with-options/` cover
exactly this and must keep passing.

## Phase 4 — optional cleanup (separate release)

- Retire the `--engine` option (`cli/…/CLI.kt`, `RuleEngine`) once only one
  engine exists. (An earlier revision of this document suggested repurposing
  the option for the Kotlin DSL; that module was removed from the tree in this
  fork as it was never wired into the application, so the option has no third
  engine to select.) Removing a CLI
  option is a user-visible break: deprecate it for one release (accept
  and warn) before dropping it.
- Delete `scripts/yaml2lua.py` and the migration error messages.
- Revisit the module layout: with rules and configuration gone, the mvel
  module is a small "directive conditionals" library — folding it into
  core removes a publication artifact (coordinate `arara-mvel` consumers,
  if any, via the CHANGELOG).

## Version note

Removing a supported file format is a breaking change: schedule phase 2
for the next **major** version after the phase 1 release, per the semantic
versioning policy stated in `CHANGELOG.md`.

## Version 9 outlook — replacing MVEL conditionals

Directive conditionals follow the rule engine: with `--engine lua` they
are evaluated as Lua expressions (`LuaConditionalEvaluator`), with the
default YAML engine they keep the MVEL dialect. The Lua dialect offers
the same helpers, requires a proper boolean result like MVEL, and
inverts `unless`/`until` identically. Simple, single-call conditions
such as `if exists('log')` work in both dialects unchanged; compound
conditions use Lua's `and`, `or` and `not` instead of `&&`, `||` and
`!`.

The Lua helper boundary is typed and will stay so in version 9: helper
arguments must match the declared type, mismatches raise Lua-style
`bad argument #<n> to '<helper>'` errors, and results keep their types
(conditions and `exit` closures require proper booleans). Lua's
implicit coercions apply to operators, not to arara's library
functions — this mirrors how Lua's own standard library type-checks
its arguments, keeps `'true'` a string and `'1'` out of numeric slots,
and matches the strict scalar contract the directive parameter parser
already follows. Rule and project file parsing follows the same
contract: absent optional entries keep their documented defaults,
wrongly typed entries fail at parse time, and nothing is silently
coerced, defaulted or dropped.

Rule evaluation failures surface through the same localized runtime
errors the MVEL dialect uses (command, flag and `exit` closures), with
arara-generated helper errors available as the diagnostic detail;
author-authored `throwError` exceptions keep flowing raw in both
dialects.

Known quirk to decide before version 9: `toLua` renders file objects
as plain path strings, so a file stored via `session.put` comes back
as a string which conditional helpers then interpret as an *extension*
(the MVEL dialect keeps the object and dispatches to the file
overload). Either map files to userdata in `toLua` or document the
string round-trip as the contract.

Consequences for the plan above:

- the phase 1 default flip turns conditionals Lua as well, so the
  release notes of that release must tell users to port compound
  conditionals (`&&`/`||`/`!` become `and`/`or`/`not`);
- the helpers moved to a neutral home ahead of schedule:
  `ConditionalMethods` and `RuleMethods` live in `core/…/rules/`,
  `MethodUtils` in `core/…/utils/`, the shared configuration model in
  `core/…/configuration/` and `AraraExceptionWithHeader` in the `api`
  module — the Lua engine no longer depends on the mvel module at all
  (the former transitional `lua` -> `mvel` edge is gone);
- the remaining MVEL surface after that move is empty: the orb tag
  interpolation of directive `options` also has a Lua counterpart tied
  to the engine (`LuaOrbTags`), including the safe mode passthrough.
  Once the conditional and rule helpers have moved to a neutral home,
  the `mvel` module can be deleted entirely.
