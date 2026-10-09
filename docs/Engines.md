+++
title = "Engines"
description = "The engine model, both dialects, interpolation, and the deprecation told honestly"
weight = 6
+++

Behind every rule arara interprets, and behind every conditional a
directive carries, there is an *engine*: the dialect that reads them. The
engine decides which rule format the tool understands, which expression
language the conditionals speak, and how the values inside directive
parameters are computed. This chapter tells the story of the two engines
arara carries — one old and deprecated, one young and future — and how to
live with both during the transition.

# The two engines

arara understands two engines, chosen with the `-e`/`--engine` flag:

- **`yaml`** — the default, and the historic one. Its rules are YAML files
  with MVEL snippets inside orb tags, its conditionals speak MVEL, and its
  directive parameters interpolate through `@{ … }` expressions. The engine
  is deprecated: it still works, it tells you so at every use, and it will
  be removed in a future version.

- **`lua`** — the modern one, the format all official rules also ship in.
  Its rules are Lua scripts, as taught in [Writing rules](Rules.md); its
  conditionals and parameter expressions speak plain Lua; and its helper
  surface is the typed, honest boundary covered in that chapter.

There is no configuration key for the engine: the choice belongs to the
command line, and the log file names the engine every run used — one more
reason to keep [Logging](Logging.md) at hand during a transition.

# The MVEL dialect

The default engine's expressions are written in MVEL, a hybrid, dynamic
expression language for the Java platform. This section covers the parts
that matter for arara; the official MVEL guide covers the rest.

## Property expressions

The bread and butter of MVEL inside arara is the property expression.
Directive parameters are converted into a map inside the rule scope, so a
parameter `foo` in a directive is read as `parameters.foo` in the rule:

```java
parameters.foo
```

Maps are accessed by property or by bracket, and lists by bracket or by
index call — `user.foobar`, `user["foobar"]` and `user.get("foobar")` are
equivalent. The property style reads cleanest, and the shipped YAML rules
favour it.

## Last value out

MVEL follows a *last value out* principle: the expression answers with the
value of its final statement, so an explicit `return` can be safely
omitted. It can also be written anyway — a visual indication of the exit
point — and the official rules do exactly that. Choose one habit and keep
your code consistent.

## Type coercion

MVEL coerces types that do not compare: `"123" == 123` holds true, because
the number coerces to the string. It is a convenience in computed rules —
and a reason to be deliberate in conditionals, where the next section
makes strictness a virtue.

## Orb tags

Inside the YAML rule format, the expressions live in *orb tags*, the
`@{ … }` markers:

```yaml
command: >
  @{
    return getCommand('pdflatex', file)
  }
```

Everything inside the tag is MVEL; everything outside is YAML. The same
tags interpolate values into directive parameters under this engine, as
seen in [Important concepts](Concepts.md).

# The Lua dialect

Under `--engine lua`, everything the MVEL snippets did speaks plain Lua —
the rules, as taught in [Writing rules](Rules.md), the helpers under their
camelCase and snake_case spellings, the conditionals, and the parameter
interpolation. Two doctrines define the dialect's character:

- **Strict booleans.** A conditional has to yield a proper boolean — `1`
  is not true, an empty string is not false. Lua's own truthiness
  deliberately does not apply, mirroring the MVEL dialect's strictness, so
  a condition that type-checks in one engine type-checks in the other.

- **The typed boundary.** Helper arguments have to match their declared
  types, and mismatches answer Lua's own `bad argument #<n> to
  '<helper>'` complaints, as covered in [Writing rules](Rules.md). Lua's
  implicit coercions apply to operators, not to arara's helpers.

## Interpolation

Under the Lua engine, directive parameters interpolate through Lua tags —
the `{{ … }}` markers, filled with the same expressions the conditionals
speak:

```tex
% arara: pdflatex: {
% arara: --> options: [ '--output-directory={{ session_path() }}' ]
% arara: --> }
```

The orb tags of the deprecated engine are rejected with a hint, so a
directive written for one engine fails honestly under the other instead of
silently producing half-evaluated values.

# Conditionals in both dialects

The four conditional operators — `if`, `until`, `unless` and `while` — are
the same in both engines, as taught in
[Important concepts](Concepts.md). What differs is the expression language
they carry: MVEL spells its logic `&&`, `||` and `!`, while Lua spells it
`and`, `or` and `not` — the table in that chapter holds the
correspondence. A single helper call translates verbatim between the
dialects; a compound expression does not.

{% <devlab.callout type="info" title="Helper names"> %}
Under the Lua engine, every helper answers both spellings — the historic
`getCommand` and the Lua-idiomatic `get_command` — so old habits survive
the transition. The official rules ship in the snake_case spelling, and it
is the one this manual teaches.
{% </devlab.callout> %}

# The deprecation, told honestly

The YAML engine is deprecated, and the tool says so at every use: a run
under the default engine logs the deprecation warning, on the console and
in the log file. The engine still works, all official rules ship in both
formats, and no workflow breaks today — but the future belongs to Lua,
and the sooner a project's rules migrate, the sooner the warning goes
quiet.

The migration itself is a chapter of its own: the dialect
correspondences and the timeline live in
[Migration](Migration.md). If you are starting a new project, start it in
Lua — there is no reason to write for yesterday's engine.
