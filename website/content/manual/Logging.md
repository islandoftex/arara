+++
title = "Logging"
description = "Logging"
weight = 5
+++

The logging feature of arara, as discussed earlier on, is activated through
either the `--log` command line option or the equivalent key in the
configuration file. This chapter covers the basic structure of a typical log
file provided by our tool, including the important blocks that can be used to
identify potential issues. The following example is used to illustrate this
feature:

```tex
% arara: pdftex
% arara: clean: { extensions: [ log ] }
Hello world.
\bye
```

When running our tool on the previous example with the `--log` command line
option (otherwise, the logging framework will not provide a file at all), we
will obtain the expected `arara.log` log file containing the most significant
events that happened during this particular execution, as well as details
regarding the underlying operating system. The contents of this file are
discussed below. Note that timestamps were deliberated removed from the log
entries in order to declutter the output, and line breaks were included in order
to easily spot each entry.

# System information

The very first entry to appear in the log file is the current version of arara.

```text
Welcome to arara 8.0.0!
```

The following entries in the log file are the absolute path of the current
deployment of arara (line 1), details about the current Java virtual machine
(namely, vendor and absolute path, in lines 2 and 3, respectively), the
underlying operating system information (namely, system name, architecture and
eventually the kernel version, in line 4), the home directory (line 5), and
the absolute path of the applied configuration file, if any (line 6). This block
is very important to help with tracking possible issues related to the underlying
operating system and the tool configuration
itself.

```text
::: arara @ /opt/islandoftex/arara
::: Java 24.0.1, Red Hat, Inc.
::: /usr/lib/jvm/java-24-openjdk
::: Linux, amd64, 6.15.3-200.fc42.x86_64
::: user.home @ /home/paulo
::: CF @ [none]
```

{% messagebox(title="A privacy note") %}
We understand that the previous entries containing information about the
underlying operating system might pose as a privacy threat to some
users. However, it is worth noting that arara does not share any sensitive
information about your system, as entries are listed in the log file for
debugging purposes only, locally in your computer.

From experience, these entries greatly help our users to track down errors in
the execution, as well as learning more about the underlying operating
system. However, be mindful of sharing your log file! Since the log file
contains structured blocks, it is highly advisable to selectively choose the
ones relevant to the current discussion.
{% end %}

Application log labels are English regardless of the selected console language.
Rule names, script messages, tool output and exception details retain their original
text. Console messages remain localized. Invalid language codes fall back to English
and produce a warning:

```text
Language foo not available; defaulting to English.
```

# Directive extraction

The following block in the log file refers to file information and directive
extraction. First, as with the terminal output counterpart, the tool will
display details about the file being processed, including size and modification
status. The project record also identifies its working directory:

```text
Project "Single files" (working directory: /home/islandoftex/Documents)
Processing "/home/islandoftex/Documents/doc12.tex" (size: 74 bytes, last modified: 2018-02-06T05:36:40Z)
```

The next entries refer to finding potential directive patterns in the code,
including multiline support. All matching patterns contain the corresponding
line numbers. Note that these numbers might refer to incorrect lines in the code
if the `--preamble` command line option is used.

```text
Potential directive at line 1: pdftex
Potential directive at line 2: clean: { extensions: [ log ] }
```

When all matching patterns are collected from the code in the previous phase,
arara composes the directives accordingly, including potential parameters and
conditionals. Observe that all directives have an associated list of line
numbers from which they were originally composed. This phase is known as
*directive extraction*.

In this phase, directives are correctly extracted and composed, but are yet to
be validated regarding invalid or reserved parameter keys. The tool then
proceeds to validate parameters and normalize such directives.

# Directive normalization

Once all directives are properly composed, the tool checks for potential
inconsistencies, such as invalid or reserved parameter keys. Then all directives
are validated and internally mapped with special parameters.

```text
All directives were validated.
```

After validation, all directives are listed in a special block in the log file,
including potential parameters and conditionals. This phase is known as
*directive normalization*. Note that the special parameters are already
included, regardless of the directive type. This particular block can be used
specially for debugging purposes, since it contains all details regarding
directives.

```text
-------------------------- DIRECTIVES ---------------------------
Directive: { identifier: pdftex, parameters:
{reference=/home/islandoftex/Documents/doc12.tex},
conditional: { NONE }, lines: [1] }
Directive: { identifier: clean, parameters: {extensions=[log],
reference=/home/islandoftex/Documents/doc12.tex},
conditional: { NONE }, lines: [2] }
-----------------------------------------------------------------
```

Note, however, that potential errors in directive conditionals, as well as
similar inconsistencies in the corresponding rules, can only be caught at
runtime. The next phase covers proper interpretation based on the provided
directives.

# Rule interpretation

Once all directives are normalized, arara proceeds to interpret the potential
conditionals, if any, and the corresponding rules. Note that, when available,
the conditional type dictates whether the rule should be interpreted first or
not. For each rule, the tool informs the identifier and the absolute path of the
corresponding rule file and the selected engine. In this specific scenario, the
rule is part of the default rule pack released with our tool:

```text
Interpreting rule "pdftex"
Rule file: "/opt/islandoftex/arara/rules/arara-rule-pdftex.yaml" (engine: yaml)
```

For each task (or subtask, as it is part of a rule task) defined in the rule
context, arara will interpret it and return the corresponding system
command. The return types can be found in [Important concepts](@/manual/Concepts.md). In this
specific scenario, there is just one task associated with the `pdftex`
rule. Both task name and system command are shown:

```text
Interpreting task "PDFTeX engine" from rule "PDFTeX"
System command: [ pdftex, doc12.tex ]
```

After proper task interpretation, the underlying execution library of arara
executes the provided system command and includes the output from both output
and error streams in an *output buffer* block inside the log file.

```text
---------------------- BEGIN OUTPUT BUFFER ----------------------
This is pdfTeX, Version 3.14159265-2.6-1.40.19 (TeX Live 2018)
(preloaded format=pdftex)
restricted \write18 enabled.
entering extended mode
(./doc12.tex [1{/usr/local/texlive/2018/texmf-var/fonts/map/
pdftex/updmap/pdfte
x.map}] )</usr/local/texlive/2018/texmf-dist/fonts/type1/
public/amsfonts/cm/cmr
10.pfb>
Output written on doc12.pdf (1 page, 11849 bytes).
Transcript written on doc12.log.
----------------------- END OUTPUT BUFFER -----------------------
```

Observe that the above output buffer block contains the relevant information
about the `pdftex` execution on the provided file. It is possible to write a
shell script to extract these blocks from the log file, as a means to provide
individual information on each execution. The process exit code and duration
are recorded before the task result:

```text
System command exited with value 0 after 25 ms
Task result: SUCCESS
```

The execution proceeds to the next directive in the list and then interprets the
`clean` rule. The same steps previously described are applied in this
scenario. Removal commands such as `rm` do not provide output when successful,
so the log explicitly records that the tool produced no output:

```text
Interpreting rule "clean"
Rule file: "/opt/islandoftex/arara/rules/arara-rule-clean.yaml" (engine: yaml)
Interpreting task "Cleaning feature" from rule "Clean"
System command: [ rm, -f, doc12.log ]
The tool produced no output.
System command exited with value 0 after 2 ms
Task result: SUCCESS
```

{% messagebox(title="Command output and failures") %}
Boolean tasks record their value without an output buffer. System commands with
no output produce the explicit silent-output record shown above. Output captured
before a process failure is retained; failures to start or finish a command also
record their category, such as `IO`, `TIMEOUT` or `INTERRUPTED`.
{% end %}

Finally, the log records the application exit code and the execution time
in seconds. Elapsed times use a decimal point regardless of the console
language. As previously mentioned, the execution time has a very simple precision
and should not be considered for command profiling.

```text
Run completed with exit code 0
Elapsed time: 0.330 seconds
```

The logging feature provides a consistent framework for event recording. It is
highly recommended to include at least the `--log` command line option (or
enable it in the configuration file) in your typical automation workflow, as
relevant information is gathered into a single consolidated report.

{% messagebox(title="Log paths") %}
From version 6.0 on, the log file may now be specified as path *anywhere* on the
file system. However, keep in mind that this behavior may be altered for future
updates in safe mode. Please refer to [Configuration](@/manual/Configuration.md) for
more details.
{% end %}

Log records include each project's working directory, the resolved rule file and
engine, command duration and exit code, captured tool output (or an explicit
silent-output record), and the application's final exit code.

# Session tracing

Debug records trace session additions, overwrites and removals by key, and
session clears and environment refreshes by count:

```text
Session variable 'iteration' set.
Session variable 'iteration' overwritten.
Session variable 'iteration' removed.
Session cleared (0 variables).
Session environment refreshed (42 variables).
```

Variable values are not recorded by this tracing. Reads do not produce records,
and failed removals do not claim that a variable was removed.

# JSON Lines output

`--log-format json` writes one JSON object per line. YAML configurations use
`logformat: json`; Lua configurations use `log = { format = "json" }`.
`--log-format text` (or `-l` / `--log`) selects text logging;
`--log-format none` disables it, which is the default. Explicit `--log-format`
selection overrides both configuration and the text shorthand.
The destination remains `arara.log` unless configured otherwise.

Each record has `schemaVersion` (currently `1`), a UTC `timestamp`, `level`,
`logger`, `message`, and an `exception` stack trace when available. Embedded
newlines and quotes are escaped within their JSON string. Log format is independent
of the rule engine, console presentation and console language.

Changing the format reopens the destination using its append setting. Use separate
files for different formats; appended files must use the selected format throughout.

## Structured events

Records use schema version `1`. CLI invocations add a `runId` shared across their
log destinations. Application records add an `event` name and typed `data` fields:

| Event | Data fields |
| --- | --- |
| `run.started`, `application.version` | `version` |
| `project.started` | `name`, `workingDirectory` |
| `file.started` | `path`, `sizeBytes` |
| `task.started` | `rule`, `task` |
| `rule.resolved` | `rule`, `path`, `engine` |
| `command.output` | `output`, `empty` |
| `command.completed` | `argv`, `durationMs`, `exitCode`; available `workingDirectory`, `inputFile` |
| `command.failed` | `argv`, `durationMs`, `failureKind`; available `workingDirectory`, `inputFile` |
| `task.result` | `success` |
| `run.completed`, `run.failed` | `exitCode`, `success` |
| `run.elapsed` | `durationMs` |
| `run.exception`, `run.exception.details` | No additional data; see `message` and `exception` |

Numbers and booleans are JSON values, not strings. A command's exit code is
separate from the task's success predicate and the final application exit code.
Process failures have a failure category instead of an invented exit code.
Exception events include a stack trace when available. Other log sources remain
valid JSON records without application event fields. Consumers should ignore
unknown event names and additional fields within a supported schema version.

For example, `jq 'select(.event == "command.completed") | .data.exitCode' arara.log`
extracts native process exit codes without parsing message text.
