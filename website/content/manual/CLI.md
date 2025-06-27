+++
title = "Command line"
description = "Command line"
weight = 3
+++

arara is a command line tool. It can be used in a plethora of command
interpreter implementations, from bash to a Windows prompt, provided that the
Java runtime environment is accessible within the current session. This chapter
covers the user interface design, as well as options (also known as flags or
switches) that modify the underlying application behaviour.

# User interface design

The goal of a user interface design is to make the interaction as simple and
efficient as possible. Good user interface design facilitates finishing the task
at hand without drawing unnecessary attention to itself. We redesigned the
interface in order to look more pleasant to the eye, after all, we work with TeX
and friends:

```sh
  __ _ _ __ __ _ _ __ __ _
 / _` | '__/ _` | '__/ _` |
| (_| | | | (_| | | | (_| |
 \__,_|_|  \__,_|_|  \__,_|

Processing 'doc5.tex' (size: 285 B, last modified: 03/01/2020
19:25:40), please wait.

(PDFLaTeX) PDFLaTeX engine .............................. SUCCESS
(BibTeX) The BibTeX reference management software ....... SUCCESS
(PDFLaTeX) PDFLaTeX engine .............................. SUCCESS
(PDFLaTeX) PDFLaTeX engine .............................. SUCCESS

Total: 1.14 seconds
```

First of all, we have the nice application logo, displayed using ASCII art. The
entire layout is based on monospaced font spacing, usually used in terminal
prompts. Hopefully you follow the conventional use of a monospaced font in your
terminal, otherwise the visual effect will not be so pleasant. First and
foremost, arara displays details about the file being processed, including size
and modification status:

```sh
Processing 'doc5.tex' (size: 285 B, last modified: 03/01/2020
19:25:40), please wait.
```

The list of tasks was also redesigned to be fully justified, and each entry
displays both task and subtask names (the former being displayed enclosed in
parentheses), besides of course the usual execution result:

```sh
(PDFLaTeX) PDFLaTeX engine .............................. SUCCESS
(BibTeX) The BibTeX reference management software ....... SUCCESS
(PDFLaTeX) PDFLaTeX engine .............................. SUCCESS
(PDFLaTeX) PDFLaTeX engine .............................. SUCCESS
```

As previously mentioned in [Important concepts](@/manual/Concepts.md), if a task
fails, arara will halt the entire execution at once and immediately report back
to the user. This is an example of how a failed task looks like:

```sh
(PDFLaTeX) PDFLaTeX engine .............................. FAILURE
```

Also, observe that our tool displays the execution time before terminating, in
seconds. The execution time has a very simple precision, as it is meant to be
easily readable, and should not be considered for command profiling.

```sh
Total: 1.14 seconds
```

The tool has two execution modes: *silent*, which is the default, and *verbose*,
which prints as much information about tasks as possible. When in silent mode,
arara will simply display the task and subtask names, as well as the execution
result. Nothing more is added to the output. For instance:

```sh
(BibTeX) The BibTeX reference management software ....... SUCCESS
```

When executed in verbose mode, arara will display the underlying system command
output as well, when applied. In version 4.0 of our tool, this mode was also
entirely redesigned in order to avoid unnecessary clutter, so it would be easier
to spot each task. For instance:

```sh
-----------------------------------------------------------------
(BibTeX) The BibTeX reference management software
-----------------------------------------------------------------

This is BibTeX, Version 0.99d (TeX Live 2020)
The top-level auxiliary file: doc5.aux
The style file: plain.bst
Database file #1: mybib.bib

--------------------------------------------------------- SUCCESS
```

It is important to observe that, when in verbose mode, arara can offer proper
interaction if the system command requires user intervention. However, when in
silent mode, the tool will simply discard this requirement and the command will
almost surely fail.

# Options

In order to run arara on your TeX file, the simplest possible way is to provide
the file name to the tool in your favourite command interpreter session,
provided that the file has at least one directive:

```sh
$ arara doc6.tex
```

From version 5.0 on, arara may receive more than one file as parameter. It will
compile them sequentially (starting with the leftmost). The process fails on the
first failure of these executions. For the files to be flawlessly compiled by
TeX, they should be in the same working directory. If you process your files
with other tools, this requirement could be lifted.

```sh
$ arara doc20.tex doc21.tex

  __ _ _ __ __ _ _ __ __ _
 / _` | '__/ _` | '__/ _` |
| (_| | | | (_| | | | (_| |
 \__,_|_|  \__,_|_|  \__,_|

Processing 'doc20.tex' (size: 28 B, last modified: 02/28/2020
07:15:02), please wait.

(PDFTeX) PDFTeX engine .................................. SUCCESS

Processing 'doc21.tex' (size: 28 B, last modified: 02/28/2020
07:15:10), please wait.

(PDFTeX) PDFTeX engine .................................. SUCCESS

Total: 1.20 seconds
```

The tool has a set of command line options (also known as flags or switches)
that modify the underlying execution behaviour or enhance the execution
workflow. If you do not provide any parameters, arara will display the tool
usage and the available options:

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
  -v, --verbose / -s, --silent  Print the command output
  -n, --dry-run                 Go through all the motions of running a
                                command, but with no actual calls
  -S, --safe-run                Run in safe mode and disable potentially
                                harmful features. Make sure your projects uses
                                only allowed features.
  -w, --whole-file              Extract directives in the file, not only in the
                                header
  -p, --preamble=<text>         Set the file preamble based on the
                                configuration file
  -t, --timeout=<int>           Set the execution timeout (in milliseconds)
  -L, --language=<text>         Set the application language
  -m, --max-loops=<int>         Set the maximum number of loops (> 0)
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

The available options for our tool are detailed as follows. Each option contains
short and long variations, which are denoted by `-o` and `--option` in the
command line, respectively. Additionally, when a parameter is required by the
current option, it will be denoted by `parameter` in the description.

- `-h` / `--help`: As the name indicates, this option prints the help message
  containing the tool usage and the list of all available options. The tool
  exits afterwards. When running arara without any options or a file to be
  processed, this is the default behaviour. This option has the highest priority
  over the others.

- `-w` / `--whole-file`: This option changes the mechanics of how arara extracts
  the directives from the code. The tool always reads from the beginning of the
  file until it reaches a line that is not empty and it is not a
  comment. However, by activating this switch, arara will extract all directives
  from the entire file (hence the option name). Consider the following example:

    ```tex
    % arara: pdftex
    Hello world.
    \bye

    % arara: pdftex
    ```

    When running arara without this option, one directive will be extracted
    (line 1). However, with `-w` enabled, the directives in lines 1 and 5 will
    be extracted. This option can also be disabled by default in the
    configuration file.

- `-l` / `--log`: This option enables the logging feature of our tool. All
  streams from all system commands will be logged and, at the end of the
  execution, a consolidated log file named `arara.log` will be generated. This
  option can also be activated by default in the configuration file. Refer to
  [Logging](@/manual/Logging.md) for more details on the logging feature.

- `-L` / `--language`: This option sets the language of the current execution of
  arara according to the language code identified by the `code` value provided
  as the parameter. The language code tries to follow the IETF BCP 47 norm,
  standardized nomenclature used to classify languages. For example, this is our
  tool speaking Dutch:

    ```sh
    $ arara -L nl doc5.tex
      __ _ _ __ __ _ _ __ __ _
     / _` | '__/ _` | '__/ _` |
    | (_| | | | (_| | | | (_| |
     \__,_|_|  \__,_|_|  \__,_|

    Verwerken van 'doc5.tex' (grootte: 285 B, laatst gewijzigd:
    03/01/2020 19:25:40), een ogenblik geduld.

    (PDFLaTeX) PDFLaTeX engine ............................ SUCCESVOL
    (BibTeX) The BibTeX reference management software ..... SUCCESVOL
    (PDFLaTeX) PDFLaTeX engine ............................ SUCCESVOL
    (PDFLaTeX) PDFLaTeX engine ............................ SUCCESVOL

    Totaal: 1,07 seconden
    ```

  {% messagebox(title="Navis volitans mihi anguillis plena est") %}
  At time of writing, arara is able to speak English, German, Dutch, Italian
  and Brazilian Portuguese out of the box. There is also a special dialect
  named Broad Norfolk, spoken by those living in the county of Norfolk in
  England.

  *Available languages:* `en` for English, `de` for German, `en-QN` for Broad
   Norfolk, `it` for Italian, `nl` for Dutch, and `pt-BR` for Portuguese
   (BR).

  Would you like to make arara speak your own language? Splendid! We would
  love to have you in the team! Just send us an electronic mail, join our
  [dedicated
  chatroom](https://matrix.to/#/!HfEWIEvFtDplCLSQvz:matrix.org?via=matrix.org)
  or [open an issue](https://gitlab.com/islandoftex/arara/issues) about
  it. The localization process is quite straightforward, we can help you. Any
  language is welcome!
  {% end %}

  This option can also be specified in the configuration file. However, one can
  always override this setting by running the tool with an explicit `-L`
  option.

  {% messagebox(title="Invalid language codes") %}
  From version 6.0 on, if you pass an invalid language code, arara will now
  run in English and issue a log warning but not fail anymore. Failing due to
  the wrong language in the output was considered inappropriate.
  {% end %}

- `-m` / `max-loops` `<number>`: As a means to avoid infinite iterations, arara
  has a predefined maximum number of loops, with the default set to 10, as a
  technical solution. For instance, consider the following directive:

    ```tex
    % arara: pdftex while true
    ```

    The `--max-loops` option is used to redefine the maximum number of loops our
    tool will allow for potentially infinite iterations. Any positive integer
    can be used as the `<number>` value for this option. An execution of the
    previous directive with a lower maximum number of loops is shown as follows:

    ```sh
    $ arara -m 2 doc8.tex
      __ _ _ __ __ _ _ __ __ _
     / _` | '__/ _` | '__/ _` |
    | (_| | | | (_| | | | (_| |
     \__,_|_|  \__,_|_|  \__,_|

    Processing 'doc8.tex' (size: 45 B, last modified: 05/29/2018
    12:32:14), please wait.

    (PDFTeX) PDFTeX engine .................................. SUCCESS
    (PDFTeX) PDFTeX engine .................................. SUCCESS

    Total: 0.58 seconds
    ```

    This option can also be specified in the configuration file. However, one
    can always override this setting by running the tool with an explicit `-m`
    option.

- `-n` / `--dry-run`: This option makes arara go through all the motions of
  running tasks and subtasks, but with no actual calls. It is a very useful
  feature for testing the sequence of underlying system commands to be performed
  on a file. For instance, consider the following execution:

    ```sh
    $ arara -n doc5.tex
      __ _ _ __ __ _ _ __ __ _
     / _` | '__/ _` | '__/ _` |
    | (_| | | | (_| | | | (_| |
     \__,_|_|  \__,_|_|  \__,_|

    Processing "doc5.tex" (size: 360 B, last modified: 12/28/2020
    13:03:32), please wait.

    [DR] (PDFLaTeX) PDFLaTeX engine
    -----------------------------------------------------------------
    Author: Island of TeX
    About to run: [pdflatex, doc5.tex] @ /home/islandoftex/Downloads

    [DR] (BibTeX) The BibTeX reference management software
    -----------------------------------------------------------------
    Author: Island of TeX
    About to run: [bibtex, doc5] @ /home/islandoftex/Downloads

    [DR] (PDFLaTeX) PDFLaTeX engine
    -----------------------------------------------------------------
    Author: Island of TeX
    About to run: [pdflatex, doc5.tex] @ /home/islandoftex/Downloads

    [DR] (PDFLaTeX) PDFLaTeX engine
    -----------------------------------------------------------------
    Author: Island of TeX
    About to run: [pdflatex, doc5.tex] @ /home/islandoftex/Downloads

    Total: 0.18 seconds
    ```

    Note that the rule authors are displayed (so they can be blamed in case
    anything goes wrong), as well as the system command to be executed. It is an
    interesting approach to see everything that will happen to your document and
    in which order.

    {% messagebox(title="Conditionals and boolean values") %}
    It is very important to observe that conditionals are not evaluated when our
    tool is executed in the `--dry-run` mode, although they are properly
    listed. Also, when a rule returns a boolean value, the code is executed
    regardless of this mode.
    {% end %}

- `p` / `--preamble` `<name>`: Some TeX documents require the same automation
  steps, e.g, a set of articles. To this end, so as to avoid repeating the same
  preamble over and over in this specific scenario, arara has the possibility of
  setting predefined preambles in a special section of the configuration file
  identified by a unique key for later use. This command line option prepends
  the predefined preamble referenced by the `<name>` key to the current document
  and then proceeds to extract directives, as usual. For instance:

    ```yaml
    twopdftex: |
      % arara: pdftex
      % arara: pdftex
    ```

    ```tex
    Hello world.
    \bye
    ```

    In this example, we have a preamble named `twopdftex` and a TeX file named
    `doc9.tex` with no directives. Of course, our tool will complain about
    missing directives, unless we deliberately inject the two directives from
    the predefined preamble into the current execution:

    ```sh
    $ arara -p twopdftex doc9.tex
      __ _ _ __ __ _ _ __ __ _
     / _` | '__/ _` | '__/ _` |
    | (_| | | | (_| | | | (_| |
     \__,_|_|  \__,_|_|  \__,_|

    Processing 'doc9.tex' (size: 18 B, last modified: 05/29/2018
    14:39:21), please wait.

    (PDFTeX) PDFTeX engine .................................. SUCCESS
    (PDFTeX) PDFTeX engine .................................. SUCCESS

    Total: 0.96 seconds
    ```

    It is important to note that this is just a directive-based preamble and
    nothing else, so a line other than a directive is discarded. Line breaks and
    conditionals are supported. Trying to exploit this area for other purposes
    will not work.

- `-t` / `--timeout` `<number>`: This option sets an execution timeout for every
  task, in milliseconds. If the timeout is reached before the task ends, arara
  will kill it and halt the execution. Any positive integer can be used as the
  `<number>` value for this option. Of course, use a sensible value to allow
  proper time for a task to be executed. For instance, consider the following
  recursive call:

    ```tex
    % arara: pdftex
    \def\foo{\foo}
    This will go \foo forever.
    \bye
    ```

    ```sh
    $ arara --timeout 3000 doc9.tex
      __ _ _ __ __ _ _ __ __ _
     / _` | '__/ _` | '__/ _` |
    | (_| | | | (_| | | | (_| |
     \__,_|_|  \__,_|_|  \__,_|

    Processing 'doc10.tex' (size: 63 B, last modified: 05/29/2018
    15:24:06), please wait.

    (PDFTeX) PDFTeX engine .................................   ERROR

    The system command execution reached the provided timeout value
    and was aborted. If the time was way too short, make sure to
    provide a longer value. There are more details available on this
    exception:

    DETAILS ---------------------------------------------------------
    Timed out waiting for java.lang.UNIXProcess@6b53e23f to finish,
    timeout: 3000 milliseconds, executed command [pdftex, doc10.tex]

    Total: 3.37 seconds
    ```

    If left unattended, this particular execution would never finish (and
    probably crash the engine at a certain point), as expected by the recursive
    calls without a proper fixed point. The `--timeout` option was set at 3000
    milliseconds and the task was aborted when the time limit was reached. Note
    that the tool raised an error about it.

- `d` / `--working-directory` `<path>` This option allows you to change the
  working directory to `<path>`. That is, the commands will run from a different
  directory than the directory you launched arara in. This is especially useful
  when calling a TeX engine as they resolve files against the working
  direcotry. For that reason, arara will also resolve each file you pass to it
  that has no absolute path against the working directory. The working directory
  is fixed for the whole call; passing multiple files to arara will resolve all
  of them against and execute all actions within that one working directory.

- `-V` / `--version`: This option, as the name indicates, prints the current
  version. It also prints the current revision and a list of libraries with
  their corresponding licenses. Finally, it simply exits the application. Note
  that this option has the second highest priority over the others.

- `-v` / `--verbose`: This option enables the verbose mode of arara. It also
  enables all streams to be flushed directly to the terminal, including
  potential user input interactions (the exact opposite of silent mode). This
  option can also be activated by default in the configuration file.

- `-s` / `--silent`: This option disables the verbose mode of arara (thus
  activating the default silent mode), if previously enabled by a proper
  configuration file (see [Configuration file](@/manual/Configuration.md)). It is
  important to note that this command line option has higher priority over the
  `--verbose` counterpart.

- `-S` / `--safe-run`: This option enables the safe mode of arara, protecting
  the system  by disallowing certain user actions. Currently, the following
  features are restricted:

  - File lookup will only perform explicit file resolution. Wildcard filters are
    disabled.

  - `❖ unsafelyExecuteSystemCommand` will raise an exception and
    abort the run. Keep in mind that rules are still allowed to construct
    arbitrary commands using `Command` objects, so this restriction only
    disallows arbitrary system commands that would not get logged and are thus
    invisible to the user.

  - The `--options` parameter does not expand orb tags in any directive.

- `-P` / `--call-property` `<entry>`: This option forwards the provided
  `<entry>`, in the `key=value` format, to the session map. For instance,
  consider the following call:

    ```sh
    $ arara -P foo=bar hello.tex
    ```

    In a rule, you may now retrieve the value associated to the `foo` key, which
    is `bar`, set at runtime, by calling the following method in your code:

    ```java
    getSession().get('arg:foo')
    ```

    This option may be called multiple times, as a means to provide as many data
    pairs as needed. Please refer to `❖ getSession` in
    [Methods](@/manual/Methods.md) for more details. Please note that any entries
    defined with this option take precedence over entries with the same keys defined
    in a `.properties` file. This means that if a key exists in both the command line
    and a `.properties` file, the value from the command line will be used.

- `-F` / `--properties-file` `<path>`: This option loads multiple entries in the
  `key=value` format from a specified `.properties` file into the session map.
  For instance, consider the following `env.properties` file:

  ```
  # content of env.properties
  foo=bar
  ```

  The `.properties` file can be passed to the application via this option:

  ```sh
  $ arara -F env.properties hello.tex
  ```

  In a rule, you may now retrieve the value associated to the `foo` key, which
  is `bar`, set in the `.properties` file and loaded at runtime, by calling the
  following method in your code:

  ```java
  getSession().get('arg:foo')
  ```
  
  Please refer to `❖ getSession` in [Methods](@/manual/Methods.md) for more details.
  Note that this option has lower precedence over entries defined in the command line.

- `--generate-completion` `<shell>`: This option generates a shell completion script
  for arara, enhancing the user experience by providing autocompletion for command 
  line options and arguments. Possible values are `bash`, `zsh` and `fish`.

  To enable the generated completion script, add the following line to your shell's
  initialization file (e.g., `.bashrc` for Bash, `.zshrc` for Zsh, or `config.fish`
  for Fish):

  ```bash
  source <(arara --generate-completion <shell>)
  ```

You can combine options, use long or short variations interchangeably and write
them in any order, provided that a file name is given at some point in the
command line, otherwise the usage will be printed. Use the provided features in
order to enhance and optimize your automation workflow.

# File name lookup

arara, as a command line application, provides support for a restricted range of
file types. In particular, the tool recognizes five file types based on their
extensions. These types are presented as follows, as well as the lookup order:
`tex` `→` `dtx` `→` `ltx`  `→` `drv`
`→` `ins`.

Note that other extensions can be added through a proper mapping in the
configuration file, as well as modifying the lookup order. This feature is
detailed later on. arara employs the following scheme for file name lookup:

- First and foremost, if the provided file name already contains a valid
  extension, the tool attempts an exact match. If the file exists, it will be
  selected. This is the best approach if your working directory contains other
  files sharing the same base name.

    ```sh
    $ arara doc11.tex
      __ _ _ __ __ _ _ __ __ _
     / _` | '__/ _` | '__/ _` |
    | (_| | | | (_| | | | (_| |
     \__,_|_|  \__,_|_|  \__,_|

    Processing 'doc11.tex' (size: 34 B, last modified: 05/29/2018
    19:40:35), please wait.

    (PDFTeX) PDFTeX engine .................................. SUCCESS

    Total: 0.69 seconds
    ```

- If the provided file name has an unsupported extension or no extension at all,
  the tool iterates through the list of default extensions, appending the
  current element to the file name and attempting an exact match. If the file
  exists, it will be selected.

    ```sh
    $ arara doc11
      __ _ _ __ __ _ _ __ __ _
     / _` | '__/ _` | '__/ _` |
    | (_| | | | (_| | | | (_| |
     \__,_|_|  \__,_|_|  \__,_|

    Processing 'doc11.tex' (size: 34 B, last modified: 05/29/2018
    19:40:35), please wait.

    (PDFTeX) PDFTeX engine .................................. SUCCESS

    Total: 0.69 seconds
    ```

- Many shells complete file names that have multiple extensions in the same
  directory, so that they end with a period. We try to resolve against them as
  well!

    ```sh
    $ arara doc11.
      __ _ _ __ __ _ _ __ __ _
     / _` | '__/ _` | '__/ _` |
    | (_| | | | (_| | | | (_| |
     \__,_|_|  \__,_|_|  \__,_|

    Processing 'doc11.tex' (size: 34 B, last modified: 05/29/2018
    19:40:35), please wait.

    (PDFTeX) PDFTeX engine .................................. SUCCESS

    Total: 0.69 seconds
    ```

It is highly recommended to use complete file names with our tool, in order to
ensure the correct file is being processed. If your command line interpreter
features tab completion, you can use it to automatically fill partially typed
file names from your working directory.

{% messagebox(title="Exit status support") %}
arara follows the good practices of software development and provides three
values for exit status, so our tool can be programmatically used in scripts and
other complex workflows: `0` `→` successful execution, `1`
`→` one of the rules failed, and `2` `→` an exception was
raised.

Please refer to the documentation of your favourite command line interpreter to
learn more about exit status captures. Programming languages also offer methods
for retrieving such information.
{% end %}
