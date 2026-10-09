+++
title = "The command line"
description = "Every flag arara understands, and the interface that answers them"
weight = 3
+++

arara is a command line tool. It can be used in a plethora of command
interpreter implementations, from bash to a Windows prompt, provided that the
Java runtime environment is accessible within the current session. This
chapter covers the user interface design, as well as the options, also known
as flags or switches, that modify the underlying application behaviour.

# User interface design

The goal of a user interface design is to make the interaction as simple and
efficient as possible. Good user interface design facilitates finishing the
task at hand without drawing unnecessary attention to itself. We redesigned
the interface in order to look more pleasant to the eye — after all, we work
with TeX and friends:

```sh
$ arara hello.tex
  __ _ _ __ __ _ _ __ __ _
 / _` | '__/ _` | '__/ _` |
| (_| | | | (_| | | | (_| |
 \__,_|_|  \__,_|_|  \__,_|

arara 8.0.0, the cool TeX automation tool.

Processing "hello.tex" (size: 87 B, last modified: 2026-10-09 09:49:05), please
wait.

(PDFLaTeX) PDFLaTeX engine ............................................ SUCCESS

TOTAL SUCCESS · 1 file, 1 task

Elapsed time: 0.5566 seconds
```

First of all, we have the application logo, displayed using ASCII art. The
entire layout is based on monospaced font spacing, usually used in terminal
prompts — hopefully you follow the conventional use of a monospaced font in
your terminal, otherwise the visual effect will not be so pleasant. Below the
logo, the banner names the tool and its version, and then arara displays
details about the file being processed, including size and modification
status.

The list of tasks is fully justified, and each entry displays both task and
subtask names — the former enclosed in parentheses — besides of course the
usual execution result. If a task fails, arara halts the entire execution at
once and immediately reports back, as mentioned in
[Important concepts](Concepts.md):

```sh
(PDFLaTeX) PDFLaTeX engine ............................................ FAILURE
```

When other files were still waiting for their turn, the run tells you which
ones it never reached:

```sh
TOTAL FAILURE · 1 file, 1 task

File not processed: plain2.tex
```

The tool displays the execution time before terminating, in seconds. The
execution time has a very simple precision, as it is meant to be easily
readable, and should not be considered for command profiling.

The tool has two execution modes: *silent*, which is the default, and
*verbose*, which prints as much information about tasks as possible. When in
silent mode, arara will simply display the task and subtask names, as well as
the execution result — nothing more is added to the output. When executed in
verbose mode, arara will display the underlying system command output as
well, when applied, framed by dotted rules so each task is easy to spot:

```sh
Processing "hello.tex" (size: 87 B, last modified: 2026-10-09 09:49:05), please
wait.

(PDFLaTeX) PDFLaTeX engine
---------------------------------------------------------------------------
This is pdfTeX, Version 3.141592653-2.6-1.40.26 (TeX Live 2025)
entering extended mode
...
--------------------------------------------------------------- SUCCESS
```

It is important to observe that, when in verbose mode, arara can offer proper
interaction if the system command requires user intervention. However, when
in silent mode, the tool will simply discard this requirement and the command
will almost surely fail.

{% <devlab.callout type="tip" title="A second face for the console"> %}
The plain layout above is the default, but it is not the only one: version 8
introduces the styled presentation, a modern face for the terminal with
tinted rules, live feedback while a task runs, and a closing summary of the
run. The `-c`/`--console` flag below chooses between the two, and the whole
story lives in [The console](Console.md).
{% </devlab.callout> %}

# Options

In order to run arara on your TeX file, the simplest possible way is to
provide the file name to the tool in your favourite command interpreter
session, provided that the file has at least one directive:

```sh
$ arara hello.tex
```

arara may receive more than one file as parameter, and it will compile them
sequentially, starting with the leftmost. The process fails on the first
failure of these executions, and the files it never reached are reported:

```sh
$ arara plain1.tex plain2.tex

  __ _ _ __ __ _ _ __ __ _
 / _` | '__/ _` | '__/ _` |
| (_| | | | (_| | | | (_| |
 \__,_|_|  \__,_|_|  \__,_|

arara 8.0.0, the cool TeX automation tool.

Processing "plain1.tex" (size: 87 B, last modified: 2026-10-09 09:50:11),
please wait.

(PDFLaTeX) PDFLaTeX engine ............................................ SUCCESS

Processing "plain2.tex" (size: 87 B, last modified: 2026-10-09 09:50:11),
please wait.

(PDFLaTeX) PDFLaTeX engine ............................................ SUCCESS

TOTAL SUCCESS · 2 files, 2 tasks

Elapsed time: 0.8421 seconds
```

The tool has a set of command line options, also known as flags or switches,
that modify the underlying execution behaviour or enhance the execution
workflow. If you do not provide any parameters at all, arara will display
the tool usage and the available options:

```sh
$ arara
Usage: arara [<options>] <file>...

    __ _ _ __ __ _ _ __ __ _
   / _` | '__/ _` | '__/ _` |
  | (_| | | | (_| | | | (_| |
   \__,_|_|  \__,_|_|  \__,_|

  The cool TeX automation tool.

  arara executes the TeX workflow you tell it to execute. Simply specify your
  needs within your TeX file and let arara do the work. These directives
  feature conditional execution and parameter expansion.

Options:
  -l, --log                     Generate a log output
  -v, --verbose                 Print the command output
  -s, --silent                  Keep the command output unprinted, even when
                                the configuration asks for it
  -n, --dry-run                 Go through all the motions of running a
                                command, but with no actual calls
  -S, --safe-run                Run in safe mode and disable potentially
                                harmful features. Make sure your project uses
                                only allowed features.
  -w, --whole-file              Extract directives in the file, not only in the
                                header
  -p, --preamble=<text>         Set the file preamble based on the
                                configuration file
  -t, --timeout=<int>           Set the execution timeout (in milliseconds)
  -L, --language=<text>         Set the application language
  -m, --max-loops=<int>         Set the maximum number of loops (> 0)
  -e, --engine=(yaml|lua)       Set the rule engine and thus the rule format to
                                use: 'yaml' (deprecated) or 'lua'
  -c, --console=(plain|styled)  Set the console presentation to use: 'plain' or
                                'styled'
  -d, --working-directory=<path>
                                Set the working directory for all tools
  -P, --call-property=<value>   Pass parameters to the application to be used
                                within the session.
  -F, --properties-file=<path>  Pass a properties file to the application to be
                                used within the session.
  --generate-completion=(bash|zsh|fish)
                                Generate a completion script for arara. Add
                                'source <(arara --generate-completion <shell>)'
                                to your shell's init file.
  -V, --version                 Show the version and exit
  -h, --help                    Show this message and exit

Arguments:
  <file>  The file(s) to evaluate and process
```

The available options are detailed as follows. Each option contains short
and long variations, denoted by `-o` and `--option` respectively. When a
parameter is required, it is denoted by `<parameter>` in the description.

- `-h` / `--help`: as the name indicates, this option prints the help
  message containing the tool usage and the list of all available options,
  and the tool exits afterwards. When running arara without any options or a
  file to be processed, this is the default behaviour. This option has the
  highest priority over the others.

- `-w` / `--whole-file`: this option changes the mechanics of how arara
  extracts the directives from the code, covered in
  [Important concepts](Concepts.md). By default, the tool reads the header
  only — the run of blank and comment lines that opens the file. With this
  switch, arara will extract directives from the entire file, hence the
  option name. The choice can also be made in the configuration file.

- `-l` / `--log`: this option enables the logging feature of our tool. All
  streams from all system commands will be logged and, at the end of the
  execution, a consolidated log file named `arara.log` will be generated.
  The log records much more than the commands themselves — the engine, the
  verdict, the provenance of the run, even the session's every mutation.
  Refer to [Logging](Logging.md) for the whole story.

- `-L` / `--language` `<code>`: this option sets the language of the current
  execution of arara according to the language code provided as the
  parameter. The language code tries to follow the IETF BCP 47 norm, a
  standardized nomenclature used to classify languages. For example, this is
  our tool speaking Dutch:

  ```sh
  $ arara -L nl hello.tex
    __ _ _ __ __ _ _ __ __ _
   / _` | '__/ _` | '__/ _` |
  | (_| | | | (_| | | | (_| |
   \__,_|_|  \__,_|_|  \__,_|

  arara 8.0.0, the cool TeX automation tool.

  Verwerken van 'hello.tex' (grootte: 87 B, laatst gewijzigd: 2026-10-09
  09:49:05), een ogenblik geduld.

  (PDFLaTeX) PDFLaTeX engine .......................................... SUCCESVOL

  TOTAAL SUCCES · 1 bestand, 1 taak

  Verstreken tijd: 0,5575 seconden
  ```

  {% <devlab.callout type="info" title="Navis volitans mihi anguillis plena est"> %}
  At the time of writing, arara is able to speak English, German, Dutch,
  Italian and Brazilian Portuguese out of the box. There is also a special
  dialect named Broad Norfolk, spoken by those living in the county of
  Norfolk in England.

  *Available languages:* `en` for English, `de` for German, `en-QN` for
  Broad Norfolk, `it` for Italian, `nl` for Dutch, and `pt-BR` for
  Portuguese (BR).

  Would you like to make arara speak your own language? Splendid! We would
  love to have you in the team! Just send us an electronic mail, join our
  [dedicated
  chatroom](https://matrix.to/#/!HfEWIEvFtDplCLSQvz:matrix.org?via=matrix.org)
  or [open an issue](https://gitlab.com/islandoftex/arara/issues) about it.
  The localization process is quite straightforward, we can help you. Any
  language is welcome!
  {% </devlab.callout> %}

  {% <devlab.callout type="info" title="Invalid language codes"> %}
  If you pass an invalid language code, arara will run in English and issue
  a warning, but not fail. Failing due to the wrong language in the output
  was considered inappropriate.
  {% </devlab.callout> %}

- `-m` / `--max-loops` `<number>`: as a means to avoid infinite iterations,
  arara has a predefined maximum number of loops, with the default set to 10,
  as a technical solution. For instance, consider the following directive:

  ```tex
  % arara: pdflatex while true
  ```

  The `--max-loops` option redefines the maximum number of loops our tool
  will allow for potentially infinite iterations. Any positive integer can be
  used as the `<number>` value. This option can also be specified in the
  configuration file.

- `-e` / `--engine` `(yaml|lua)`: this option sets the rule engine and thus
  the rule format arara uses to resolve directives. The default `yaml`
  engine interprets the traditional YAML rules with MVEL snippets; it is
  deprecated and will be removed in a future version. The `lua` engine
  interprets rules written as Lua scripts, which is the format all official
  rules also ship in. See [Engines](Engines.md) for the whole story.

- `-c` / `--console` `(plain|styled)`: this option sets the console
  presentation for the run. The `plain` presentation is the historic layout
  you have been looking at in this chapter, and the default. The `styled`
  presentation is the modern face: tinted rules, live feedback while a task
  runs, and a closing summary of the whole run — and when the terminal
  cannot carry colors or glyphs, it degrades gracefully to the same ASCII
  structure. The choice can also be made in the configuration file, and the
  command line always wins over it. An unoffered name is an error rather
  than a silent fallback. The whole story lives in
  [The console](Console.md).

- `-n` / `--dry-run`: this option makes arara go through all the motions of
  running tasks and subtasks, but with no actual calls. It is a very useful
  feature for testing the sequence of underlying system commands to be
  performed on a file:

  ```sh
  $ arara -n hello.tex
    __ _ _ __ __ _ _ __ __ _
   / _` | '__/ _` | '__/ _` |
  | (_| | | | (_| | | | (_| |
   \__,_|_|  \__,_|_|  \__,_|

  arara 8.0.0, the cool TeX automation tool.

  Processing "hello.tex" (size: 87 B, last modified: 2026-10-09 09:49:05), please
  wait.

  [DR] (PDFLaTeX) PDFLaTeX engine
  ---------------------------------------------------------------------------
  Author: Island of TeX
  About to run: [pdflatex, hello.tex] @ /home/islandoftex

  DRY RUN · 1 task

  Elapsed time: 0.2340 seconds
  ```

  Note that the rule authors are displayed — so they can be blamed in case
  anything goes wrong — as well as the system command that would be executed.
  It is an interesting approach to see everything that will happen to your
  document, and in which order.

  {% <devlab.callout type="info" title="Conditionals and boolean values"> %}
  It is very important to observe that conditionals are not evaluated when
  our tool is executed in the `--dry-run` mode, although they are properly
  listed. Also, when a rule returns a boolean value, the code is executed
  regardless of this mode.
  {% </devlab.callout> %}

- `-p` / `--preamble` `<name>`: some TeX documents require the same
  automation steps, e.g., a set of articles. To this end, so as to avoid
  repeating the same preamble over and over in this specific scenario,
  arara has the possibility of setting predefined preambles in a special
  section of the configuration file, identified by a unique key for later
  use. This command line option prepends the predefined preamble referenced
  by the `<name>` key to the current document and then proceeds to extract
  directives, as usual. It is important to note that this is just a
  directive-based preamble and nothing else, so a line other than a directive
  is discarded. Line breaks and conditionals are supported. Trying to
  exploit this area for other purposes will not work. The feature is covered
  in [Configuration](Configuration.md).

- `-t` / `--timeout` `<number>`: this option sets an execution timeout for
  every task, in milliseconds. If the timeout is reached before the task
  ends, arara will kill it and halt the execution. Any positive integer can
  be used as the `<number>` value — of course, use a sensible value to allow
  proper time for a task to be executed. When the limit is reached, the task
  meets the localized timeout verdict and the run halts with it.

- `-d` / `--working-directory` `<path>`: this option allows you to change
  the working directory to `<path>`. That is, the commands will run from a
  different directory than the one you launched arara in. This is
  especially useful when calling a TeX engine, as they resolve files against
  the working directory. For that reason, arara will also resolve each file
  you pass to it that has no absolute path against the working directory.
  The working directory is fixed for the whole call: passing multiple files
  to arara will resolve all of them against, and execute all actions within,
  that one working directory.

- `-V` / `--version`: this option, as the name indicates, prints the
  current version. The banner carries the logo, the version, the copyright
  and the new features of the release, and then the application exits. Note
  that this option has the second highest priority over the others.

- `-v` / `--verbose`: this option enables the verbose mode of arara. It also
  enables all streams to be flushed directly to the terminal, including
  potential user input interactions — the exact opposite of silent mode.
  This option can also be activated by default in the configuration file.

- `-s` / `--silent`: this option keeps the command output unprinted, even
  when the configuration asks for it. A verbose configuration is a
  standing choice; the silent alias negates it for the one invocation that
  asks for quiet. When both `--verbose` and `--silent` are given, the
  verbose request wins.

- `-S` / `--safe-run`: this option enables the safe mode of arara,
  protecting the system by disallowing certain user actions. Currently,
  the following features are restricted:

  - File lookup will only perform explicit file resolution. Wildcard
    filters are disabled.

  - `unsafely_execute_system_command` will raise an exception and abort
    the run. Keep in mind that rules are still allowed to construct
    arbitrary commands using command objects, so this restriction only
    disallows arbitrary system commands that would not get logged and are
    thus invisible to the user.

  - Interpolation in the `options` parameter of directives is disabled.

- `-P` / `--call-property` `<entry>`: this option forwards the provided
  `<entry>`, in the `key=value` format, to the session map. For instance,
  consider the following call:

  ```sh
  $ arara -P foo=bar hello.tex
  ```

  In a rule, you may now retrieve the value associated to the `foo` key,
  which is `bar`, set at runtime, by calling the following in your code:

  ```lua
  getSession().get('arg:foo')
  ```

  This option may be called multiple times, as a means to provide as many
  data pairs as needed. Please refer to [Important concepts](Concepts.md)
  for the session's whole story. Note that any entries defined with this
  option take precedence over entries with the same keys defined in a
  `.properties` file: if a key exists in both the command line and a
  `.properties` file, the value from the command line will be used.

- `-F` / `--properties-file` `<path>`: this option loads multiple entries in
  the `key=value` format from a specified `.properties` file into the
  session map. For instance, consider the following `env.properties` file:

  ```
  # content of env.properties
  foo=bar
  ```

  The `.properties` file can be passed to the application via this option:

  ```sh
  $ arara -F env.properties hello.tex
  ```

  In a rule, the value associated to the `foo` key, loaded at runtime, is
  retrieved the same way:

  ```lua
  getSession().get('arg:foo')
  ```

  Note that this option has lower precedence than entries defined in the
  command line.

- `--generate-completion` `<shell>`: this option generates a shell
  completion script for arara, enhancing the user experience by providing
  autocompletion for command line options and arguments. Possible values
  are `bash`, `zsh` and `fish`. To enable the generated completion script,
  add the following line to your shell's initialization file, e.g.,
  `.bashrc` for Bash, `.zshrc` for Zsh, or `config.fish` for Fish:

  ```bash
  source <(arara --generate-completion <shell>)
  ```

You can combine options, use long or short variations interchangeably and
write them in any order, provided that a file name is given at some point in
the command line, otherwise the usage will be printed. Use the provided
features in order to enhance and optimize your automation workflow.

# File name lookup

arara, as a command line application, provides support for a restricted
range of file types. In particular, the tool recognizes five file types
based on their extensions. These types are presented as follows, as well as
the lookup order: `tex` → `dtx` → `ltx` → `drv` → `ins`.

Note that other extensions can be added through a proper mapping in the
configuration file, as well as modifying the lookup order. arara employs
the following scheme for file name lookup:

- First and foremost, if the provided file name already contains a valid
  extension, the tool attempts an exact match. If the file exists, it will
  be selected. This is the best approach if your working directory contains
  other files sharing the same base name.

  ```sh
  $ arara doc11.tex
  ```

- If the provided file name has an unsupported extension or no extension at
  all, the tool iterates through the list of known extensions, appending
  the current element to the file name and attempting an exact match. If
  the file exists, it will be selected.

  ```sh
  $ arara doc11
  ```

- Many shells complete file names that have multiple extensions in the
  same directory, so that they end with a period. We try to resolve
  against them as well!

  ```sh
  $ arara doc11.
  ```

It is highly recommended to use complete file names with our tool, in order
to ensure the correct file is being processed. If your command line
interpreter features tab completion, you can use it to automatically fill
partially typed file names from your working directory.

{% <devlab.callout type="info" title="Exit status support"> %}
arara follows the good practices of software development and provides
values for exit status, so our tool can be programmatically used in scripts
and other complex workflows: `0` for a successful execution, `1` when one of
the rules failed, and `2` when an exception was raised. Additionally, a
`halt` directive can request its own exit code, which reaches the process
unchanged — a handy device for scripted workflows.

Please refer to the documentation of your favourite command line interpreter
to learn more about exit status captures. Programming languages also offer
methods for retrieving such information.
{% </devlab.callout> %}
