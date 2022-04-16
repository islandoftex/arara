+++
title = "Logging"
description = "arara's log file and detailed output"
weight = 5
+++

The logging feature of arara, as discussed earlier on, is activated through
either the `--log` command line option (Section~\ref{sec:options}, on
page~\pageref{sec:options}) or the equivalent key in the configuration file
(Section~\ref{sec:basicstructure}, on page~\pageref{sec:basicstructure}). This
chapter covers the basic structure of a typical log file provided by our tool,
including the important blocks that can be used to identify potential
issues. The following example is used to illustrate this feature:

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
Welcome to arara 6.0.0!
```

The following entries in the log file are the absolute path of the current
deployment of arara (line 1), details about the current Java virtual machine
(namely, vendor and absolute path, in lines 2 and 3, respectively), the
underlying operating system information (namely, system name, architecture and
eventually the kernel version, in line 4), home and working directories (lines 5
and 6, respectively), and the absolute path of the applied configuration file,
if any (line 7). This block is very important to help with tracking possible
issues related to the underlying operating system and the tool configuration
itself.

```text
::: arara @ /opt/paulo/arara
::: Java 1.8.0_171, Oracle Corporation
::: /usr/lib/jvm/java-1.8.0-openjdk-1.8.0.171-4.b10.fc28.x86_64/jre
::: Linux, amd64, 4.16.12-300.fc28.x86_64
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

It is important to observe that localized messages are also applied to the log
file. If a language other than English is selected, either through the
`--language` command line option or the equivalent key in the configuration
file, the logging framework will honour the current setting and entries will be
available in the specified language. Having a log file in your own language
might mitigate the traumatic experience of error tracking for Te\ newbies. From
version 6.0 on, if you pass an invalid language code (for instance, `-L foo` in
the command line), arara will default to English and issue a log warning:

```text
Language foo not available; defaulting to English.
```

# Directive extraction

The following block in the log file refers to file information and directive
extraction. First, as with the terminal output counterpart, the tool will
display details about the file being processed, including size and modification
status:

```text
Processing 'doc12.tex' (size: 74 B, last modified:
06/02/2018 05:36:40), please wait.
```

The next entries refer to finding potential directive patterns in the code,
including multiline support. All matching patterns contain the corresponding
line numbers. Note that these numbers might refer to incorrect lines in the code
if the `--preamble` command line option is used.

```text
I found a potential pattern in line 1: pdftex
I found a potential pattern in line 2: clean: { extensions: [ log ] }
```

When all matching patterns are collected from the code in the previous phase,
arara composes the directives accordingly, including potential parameters and
conditionals. Observe that all directives have an associated list of line
numbers from which they were originally composed. This phase is known as
*directive extraction*.

```text
I found a potential directive: Directive: { identifier: pdftex,
parameters: {}, conditional: { NONE }, lines: [1] }
I found a potential directive: Directive: { identifier: clean,
parameters: {extensions=[log]}, conditional: { NONE }, lines: [2] }
```

In this phase, directives are correctly extracted and composed, but are yet to
be validated regarding invalid or reserved parameter keys. The tool then
proceeds to validate parameters and normalize such directives.

# Directive normalization

Once all directives are properly composed, the tool checks for potential
inconsistencies, such as invalid or reserved parameter keys. Then all directives
are validated and internally mapped with special parameters, as previously
described in Section~\ref{sec:directivenormalization}, on
page~\pageref{sec:directivenormalization}.

```text
All directives were validated. We are good to go.
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
{reference=/home/paulo/Documents/doc12.tex},
conditional: { NONE }, lines: [1] }
Directive: { identifier: clean, parameters: {extensions=[log],
reference=/home/paulo/Documents/doc12.tex},
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
corresponding YAML file. In this specific scenario, the rule is part of the
default rule pack released with our tool:

```text
I am ready to interpret rule 'pdftex'.
Rule location: '/opt/paulo/arara/rules'
```

For each task (or subtask, as it is part of a rule task) defined in the rule
context, arara will interpret it and return the corresponding system
command. The return types can be found in Section~\ref{sec:rule}, on
page~\pageref{sec:rule}. In this specific scenario, there is just one task
associated with the `pdftex` rule. Both task name and system command are
shown:

```text
I am ready to interpret task 'PDFTeX engine' from rule 'PDFTeX'.
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
individual information on each execution. Finally, the task result is shown:

```text
Task result: SUCCESS
```

The execution proceeds to the next directive in the list and then interprets the
`clean` rule. The same steps previously described are applied in this
scenario. Also note that the output buffer block is deliberately empty due to
the nature of the underlying system command, as removal commands such as `rm` do
not provide output at all when successful.

```text
I am ready to interpret rule 'clean'.
Rule location: '/opt/paulo/arara/rules'
I am ready to interpret task 'Cleaning feature' from rule 'Clean'.
System command: [ rm, -f, doc12.log ]
---------------------- BEGIN OUTPUT BUFFER ----------------------

----------------------- END OUTPUT BUFFER -----------------------
Task result: SUCCESS
```

{% messagebox(title="Empty output buffer") %}
If the system command is simply a boolean value, the corresponding block will
remain empty. Also note that not all commands from the underlying operating
system path provide proper stream output, so the output buffer block might be
empty in certain corner scenarios. This is the case, for example, of the
provided `clean` rule.
{% end %}

Finally, as the last entry in the log file, the tool shows the execution time,
in seconds. As previously mentioned, the execution time has a very simple
precision and should not be considered for command profiling.

```text
Total: 0.33 seconds
```

The logging feature provides a consistent framework for event recording. It is
highly recommended to include at least the `--log` command line option (or
enable it in the configuration file) in your typical automation workflow, as
relevant information is gathered into a single consolidated report.

{% messagebox(title="Log paths") %}
From version 6.0 on, the log file may now be specified as path *anywhere* on the
file system. However, keep in mind that this behavior may be altered for future
updates in safe mode. Please refer to Chapter~\ref{chap:configurationfile}, on
page~\pageref{chap:configurationfile}, for more details.
{% end %}
