+++
title = "Logging"
description = "The log file's whole life — every entry, the session's trail, and the English it always speaks"
weight = 9
+++

The logging feature of arara, discussed earlier on, is activated through
either the `--log` command line option or the equivalent key in the
configuration file. This chapter covers the structure of a typical log
file, including the blocks that identify potential issues — and version 8
made the log grow: it now records the engine, the presentation, the
projects' order, and every mutation the session lived through.

{% <devlab.callout type="info" title="The log speaks English, always"> %}
Regardless of the language the console carries, log lines are always
English, hardcoded at their sites. Log files are shared diagnostic
artifacts: they read the same on every machine and every language, so they
can be pasted as-is into a report, a chat, or an issue. The console stays
localized — the log does not.
{% </devlab.callout> %}

# What the log records

A run behind `--log` writes `arara.log` — or the name the `logname` key
gave it — starting with the welcome and the environment block:

```text
Welcome to arara 8.0.0!
-------------------------------------------------------------------------------
```

The environment block follows: the tool's own deployment path, the Java
virtual machine, the operating system, the user home, and the configuration
file the run considered — `[none]` when there was none. This block helps
with tracking issues related to the underlying system and the tool
configuration itself:

```text
::: arara @ /opt/islandoftex/arara
::: Java 21.0.12, Eclipse Adoptium
::: Linux, amd64, 6.2.8-200.fc44.x86_64
::: user.home @ /home/islandoftex
::: CF @ /home/islandoftex/project/arararc.yaml
```

Next come the two provenance lines: the presentation the run rendered
through — `plain`, or `styled`, or the degraded form an undecorated
stream imposed — and the engine that read the rules:

```text
Console set to plain
Rule engine set to yaml
```

A run under the default engine also carries the deprecation warning, the
same one the console shows, so a shared log tells the whole story.

When a run carries projects, the log records the execution order once,
before anything starts — the run's own narrative:

```text
Project execution order resolved as: figures, extras, The Book Project
```

Then, for each project, the log names it and its working directory; and
for each file, the processing line with its size and modification stamp:

```text
Project "The Book Project" (working directory: /home/islandoftex/book)
Processing "main.tex" (size: 247 B, last modified: 2026-10-09 10:14:11), please wait.
```

# Directive extraction and normalization

The next entries refer to finding potential directive patterns in the
code, including multiline support — every matching pattern with its line
number. Mind that these numbers refer to the lines as extracted, so a
`--preamble` prepend shifts them:

```text
I found a potential pattern in line 1: pdflatex
```

When all patterns are collected, arara composes the directives,
validates the parameters against the rules, and lists the directives in
a dedicated block — identifier, parameters, conditional and the original
lines. This block is the debugging heart of the log: everything a
directive claimed to be, before interpretation:

```text
All directives were validated. We are good to go.
--------------------------------- DIRECTIVES ----------------------------------
Directive(identifier='pdflatex', parameters={reference=/home/islandoftex/main.tex},
conditional={ NONE }, lineNumbers=[1])
-------------------------------------------------------------------------------
```

# Interpretation and commands

For each rule, the log names the identifier and the location the rule was
found in — the first line to check when a rule misbehaves — and for each
task, the entry and the system command, with the directory it runs in:

```text
I am ready to interpret rule "pdflatex".
Rule location: "/opt/islandoftex/arara/rules"
Rule file: "/opt/islandoftex/arara/rules/pdflatex.lua"
I am ready to interpret task "PDFLaTeX engine" from rule "PDFLaTeX".
System command: [pdflatex, main.tex] @ /home/islandoftex/book
```

The command's outcome follows with its own line: the exit value and the
duration, in seconds:

```text
System command exited with value 0 in 0.9127 seconds
```

The tool's own output lands in an *output buffer* block, framed between
its begin and end markers — a run's full record even when the console
stayed silent:

```text
Command output
----------------------------- BEGIN OUTPUT BUFFER -----------------------------
This is pdfTeX, Version 3.141592653-2.6-1.40.26 (TeX Live 2025)
entering extended mode
...
------------------------------ END OUTPUT BUFFER ------------------------------
```

{% <devlab.callout type="info" title="Empty buffers"> %}
Not every command writes to the streams — removal commands and boolean
tasks leave the buffer empty on purpose. An empty block means the command
said nothing, not that the log missed it.
{% </devlab.callout> %}

Each task answers with the fixed, greppable result word, and the skipped
and halted directives carry their own records: a conditional skip is
logged with its reason, and a halt request with the status it asked for.

# The session's trail

Version 8 gave the session a witness: every mutation the session lived
through lands in the log as it happens. The run's opening records the
environment seeding in one line — the dozens of copied variables without
the noise — and every `put`, `remove` and `clear` a rule performs writes
its own line, the overwrites naming the value they displaced:

```text
Session environment refreshed (56 variables).
Session variable 'foo' set to 'bar'.
Session variable 'foo' overwritten with 'baz' (was 'bar').
Session variable 'foo' removed.
Session cleared (2 variables).
```

The reads stay silent — they fire on every conditional, and the log would
drown. The trail is the debugging answer to the question a complex
workflow eventually asks: *who set this, and when?*

# The closing entries

The log closes with the elapsed time and the verdict, the exit code the
process will carry — `Run completed with exit code 0` on a good day, `Run
failed with exit code 1` when a rule failed — and, when a run failed
before reaching them, the files it never processed are named, one line
each, the same report the console carries:

```text
File not processed: chapter2.tex
```

Whatever the console presentation rendered the run through, the log holds
the same entries: the display-side lines carry no console knowledge, so
the plain and the styled runs of the same project write identical logs —
the provenance line at the top is the only entry that tells them apart.

{% <devlab.callout type="warning" title="A privacy note"> %}
The environment block lists operating system details and paths, all for
debugging purposes, locally in your computer. From experience, these
entries greatly help tracking down errors — but be mindful when sharing
a log file: choose the blocks relevant to the discussion, and the
structure makes choosing easy.
{% </devlab.callout> %}

A fresh run truncates the log by default — each `--log` invocation starts
the file from scratch, the tidy choice for a log that records whole runs.
The logging feature provides a consistent framework for event recording:
it is highly recommended to include `--log` in your typical automation
workflow, as relevant information is gathered into a single consolidated
report. The `logname` key of [Configuration](Configuration.md) moves the
file, and a relative name follows the directory of the invocation.
