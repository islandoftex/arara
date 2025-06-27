+++
title = "Important concepts"
description = "Important concepts"
weight = 2
+++

Time for our first proper contact with arara! I must stress that is very
important to understand a few concepts in which arara relies before we proceed
to the usage itself. Do not worry, these concepts are easy to follow, yet they
are vital to the comprehension of the application and the logic behind it.

# Rules

A *rule* is a formal description of how arara handles a certain task. For
instance, if we want to use `pdflatex` with our tool, we should have a rule for
that. Directives are mapped to rules, so a call to a non-existent rule `foo`,
for instance, will indeed raise an error:

```sh
  __ _ _ __ __ _ _ __ __ _
 / _` | '__/ _` | '__/ _` |
| (_| | | | (_| | | | (_| |
 \__,_|_|  \__,_|_|  \__,_|

Processing "doc1.tex" (size: 31 B, last modified: 12/28/2020
07:37:37), please wait.

  ERROR

I could not find a rule named "foo" in the provided rule paths.
Perhaps a misspelled word? I was looking for a file named
"foo.yaml" in the following paths in order of priority:
(/opt/islandoftex/arara/rules)

Total: 0.03 seconds
```

Once a rule is defined, arara automatically provides an access layer to that
rule through directives in the source code, a concept to be formally introduced
later on. Observe that a directive reflects a particular instance of a rule of
the same name (i.e, a `foo` directive in a certain source code is an instance of
the `foo` rule).

{% messagebox(title="A note about rules") %}
From version 6.0 on, rules included in the core distribution have been renamed
to have a unique prefix in the texmf tree. File names should not be relied
upon.
{% end %}

In short, a rule is a plain text file written in the YAML format, described in
[YAML](@/manual/YAML.md). I opted for this format because back then it was cleaner
and more intuitive to use than other markup languages such as XML, besides of
course being a data serialization standard for programming languages.

{% messagebox(title="Animal jokes") %}
As a bonus, the acronym *YAML* rhymes with the word *camel*, so arara is heavily
environmentally friendly. Speaking of camels, there is the programming reference
as well, since this amusing animal is usually associated with Perl and friends.
{% end %}

The default rules, i.e, the rules shipped with arara, are placed inside a
special subdirectory named `rules/` inside another special directory named
`ARARA_HOME` (the place where our tool is installed). We will learn later on
that we can add an arbitrary number of paths for storing our own rules, in order
of priority, so do not worry too much about the location of the default rules,
although it is important to understand and acknowledge their existence. Observe,
however, that rules in the core distribution have a different naming scheme than
the ones located in the user space.

The following list describes the basic structure of an arara rule by presenting
the proper elements (or keys, if we consider the proper YAML
nomenclature). Observe that elements marked as **[M]** are mandatory (i.e, the
rule *has* to have them in order to work). Similarly, elements marked as **[O]**
are optional, so you can safely ignore them when writing a rule for our tool. A
key preceded by `context →` indicates a context and should be
properly defined inside it.

- **[M]** `!config`: This keyword is mandatory and must be the first line of any
  arara rule. It denotes the object mapping metadata to be internally used by
  the tool. The tool requires it, so make sure to start all rules with a
  `!config` keyword.

- **[M]** `identifier`: This key acts as a unique identifier for the rule (as
  expected). It is highly recommended to use lowercase letters without spaces,
  accents or punctuation symbols, as good practice (again). As a convention,
  if you have an identifier named `pdflatex`, the rule filename must be
  `pdflatex.yaml` (like our own instance). Please note that, although `yml` is
  known to be a valid YAML extension as well, arara only considers files
  ending with the `yaml` extension. This is a deliberate decision.

  ```yaml
  identifier: pdflatex
  ```

- **[M]** `name`: This key holds the name of the *task* (a rule instantiated
  through a directive) as a plain string. When running arara, this value will
  be displayed in the output enclosed in parentheses.

  ```yaml
  name: PDFLaTeX
  ```

- **[O]** `authors`: We do love blaming people, so arara features a special key
  to name the rule authors (if any) so you can write stern electronic
  communications to them! This key holds a list of strings. If the rule has
  just one author, add it as the first (and only) element of the list.

  ```yaml
  authors:
  - Marco Daniel
  - Paulo Cereda
  ```

- **[M]** `commands`: This key denotes a potential list of commands. From the user
  perspective, each command is called a *subtask* within a task (rule and
  directive) context. A task may represent only a single command (a single
  subtask), as well as a sequence of commands (subtasks). For instance, the
  `frontespizio` rule requires at least two commands. So, as a means of
  normalizing the representation, a task composed of a single command (single
  subtask) is defined as the only element of the list, as opposed to previous
  versions of arara, which had a specific key to hold just one command.

  In order to properly set a subtask, the keys used in this specification are
  defined inside the `commands →` context and presented as follows.

  - **[O]** `commands → name`: This key holds the name of the subtask
    as a plain string. When running arara, this value will be displayed in the
    output. Subtask names are displayed after the main task name. By the way,
    did you notice that this key is entirely optional? That means that a
    subtask can simply be unnamed, if you decide so. However, such practice is
    not recommended, as it's always good to have a visual description of what
    arara is running at the moment, so name your subtasks properly.

  - **[M]** `commands → command`: This key holds the action to be
    performed, typically a system command. The tool offers two types of
    returned values:

    - A `Command` object: arara features an approach for handling system
      commands based on a high level structure with explicit argument parsing
      named `Command`. In order to use this approach, we need to rely on orb
      tags and use a helper method named `getCommand` to obtain the desired
      result. We will detail this method later on.

      ```yaml
      command: "@{ return getCommand('ls') }"
      ```

    - A boolean value: it is also possible to exploit the expressive power of
      the underlying scripting language available in the rule context (see
      [MVEL](@/manual/MVEL.md) for more details) for writing complex code. In this
      particular case, since the computation is being done by arara itself and
      not the underlying operating system, there will not be a command to be
      executed, so simply return a boolean value — either an explicit
      `true` or `false` value or a logical expression — to indicate
      whether the computation was successful.

      ```yaml
      command: "@{ return 1 == 1 }"
      ```

    It is also worth mentioning that arara also supports lists of commands
    represented as `Command` objects, boolean values or a mix of them. This is
    useful if your rule has to decide whether more actions are required in order
    to accomplish a task. In this case, our tool will take care of the list and
    execute each element in the specified order.

    ```yaml
    command: "@{ return [ getCommand('ls'), getCommand('ls') ] }"
    ```

    As an example, please refer to the official `clean` rule for a real scenario
    where a list of commands is successfully employed: for each provided
    extension, the rule creates a new cleaning command and adds it to a list of
    removals to be processed later.

    There is at least one variable available in the `command` context and is
    described as follows (note that MVEL variables and orb tags are discussed in
    [MVEL](@/manual/MVEL.md)). A variable will be denoted by `⋄ variable`
    in this list. For each rule argument (defined later on), there will be a
    corresponding variable in the `command` context, directly accessed through
    its unique identifier.

    - `⋄ reference`: This variable holds the canonical, absolute path
      representation of the file name as a `File` object. This is useful if it's
      necessary to know the hierarchical structure of a project. Since the
      reference is a Java object, we can use methods available in the `File`
      class.

    {% messagebox(title="Quote handling") %}
 The YAML format disallows key values starting with `@` without proper
 quoting. This is the reason we had to use double quotes for the value and
 internally using single quotes for the command string. Also, we could use
 the other way around, or even using only one type and then escaping them
 when needed. This is excessively verbose but needed due to the format
 requirement.

 From version 6.0 on, the `<arara>` shorthand is not supported anymore. We
 encourage the use of a YAML feature named *folded style* when writing such
 values. The idea here is to use the scalar content in folded style. The new
 code will look like this:

 ```yaml
 command: >
   @{
     return getCommand('ls')
   }
 ```

 Mind the indentation, as YAML requires it to properly identify blocks. If
 your code still relies on the `<arara>` shorthand, please update it
 accordingly to use YAML's folded style instead.
    {% end %}

  - **[O]** `commands → exit`: This key holds a special purpose, as
    it represents a custom exit status evaluation for the corresponding
    command. In general, a successful execution has zero as an exit status,
    but sometimes we end up with tools or situations where we need to override
    this check for whatever reason. For this purpose, simply write a MVEL
    expression *without orb tags* as plain string and use the special variable
    `⋄ value` if you need the actual exit status returned by the
    command, available at runtime. For example, if the command returns a
    non-zero value indicating a successful execution, we can write this key
    as:

    ```yaml
    exit: value > 0
    ```

    If the execution should be marked as successful by arara regardless of the
    actual exit status, you can simply write `true` as the key value and this
    rule will never fail, for obvious reasons.

  For instance, consider a full example of the `commands` key, defined with only
  one command, presented as follows. The hyphen denotes a list element, so mind
  the indentation for correctly specifying the component keys. Also, note that,
  in this case, the `exit` key was completely optional, as it does the default
  checking, and it was included for didactic purposes.

  ```yaml
  commands:
  - name: The PDFLaTeX engine
    command: >
      @{
        return getCommand('pdflatex', file)
      }
    exit: value == 0
  ```

- **[M]** `arguments`: This key holds a list of arguments for the current rule,
  if any. The arguments specified in this list will be available to the user
  later on for potential completion through directives. Once instantiated,
  they will become proper variables in the `command` contexts. This key is
  mandatory, so even if your rule does not have arguments, you need to specify
  a list regardless. In this case, use the empty list notation:

  ```yaml
  arguments: []
  ```

  In order to properly set an argument, the keys used in this specification are
  defined inside the `arguments →` context and presented as follows.

  - **[M]** `arguments → identifier`: This key acts as a unique
    identifier for the argument. It is highly recommended to use lowercase
    letters without spaces, accents or punctuation symbols, as a good
    practice. This key will be used later on to set the corresponding value in
    the directive context.

    ```yaml
    identifier: shell
    ```

    It is important to mention that not all names are valid as argument
    identifiers. arara has restrictions on two names, described as follows,
    which cannot be used.

    {% messagebox(title="Reserved names for rule arguments") %}
    Our tool has two names reserved for internal use: `files`, and
    `reference`. Do not use them as argument identifiers!
    {% end %}

  - **[O]** `arguments → flag`: This key holds a plain string and is
    evaluated when the corresponding argument is defined in the directive
    context. After being evaluated, the result will be stored in a variable of
    the same name to be later accessed in the `command` context. In the
    scenario where the argument is not defined in the directive, the variable
    will hold an empty list.

    {% messagebox(title="Return type") %}
From version 6.0 on, the return value for `flag` is now transformed into a
proper `List<String>` type instead of a plain, generic `Object` reference,
as seen in previous versions. The following rules apply:

- If a list is returned, it will be flattened and all values will be turned
  into strings.

  ```java
  [ 'a', 1, [ 2, 'b' ] ] → [ 'a', '1', '2', 'b' ]
  ```

- If a string is returned, a single list with only that string will be
  returned.

  ```java
  'hello world' → [ 'hello world' ]
  ```

- If another type is returned, it will be turned into string.

  ```java
  3.1415 → [ '3.1415' ]
  ```

Other return types than string or lists are not encouraged. However, if such
types are used, they will be transformed into a list of strings, as
previously seen. If you need interoperability of complex `command` code with
older versions, use the following trick to get the value of previously
non-list values:

```java
isList(variable) ? variable[0] : variable
```

In this way, one can keep a compatibility layer for older versions. However,
it is highly recommended to use the latest version of arara whenever
possible.
    {% end %}

    ```yaml
    flag: >
      @{
          isTrue(parameters.shell, '--shell-escape',
                 '--no-shell-escape')
      }
    ```

    There are two variables available in the `flag` context, described as
    follows. Note that are also several helper methods available in the rule
    context (for instance, `❖ isTrue` presented in the previous
    example) which provide interesting features for rule writing. They are
    detailed later on, in [Methods](@/manual/Methods.md).

    - `⋄ parameters`: This variable holds a map of directive
      parameters available at runtime. For each argument identifier listed in
      the `arguments` list in the rule context, there will be an entry in this
      variable. This is useful to get the actual values provided during
      execution and take proper actions. If a parameter is not set in the
      directive context, the reference will still exist in the map, but it will
      be mapped to an empty string.

      ```java
      check = parameters.contains("foo");
      ```

    - `⋄ reference`: This variable holds the canonical, absolute path
      representation of the file name as a `File` object. This is useful if it
      is necessary to know the hierarchical structure of a project. Since the
      reference is a Java object, we can use methods available in the `File`
      class.

      ```java
      parent = reference.getParent();
      ```

    In the previous example, observe that the MVEL expression defined in the
    `flag` key checks if the user provided an affirmative value regarding shell
    escape, through comparing `⋄ parameters.shell` with a set of
    predefined affirmative values. In any case, the corresponding command flag
    is defined as result of such evaluation.

  - **[O]** `arguments → default`: As default behaviour, if a
    parameter is not set in the directive context, the reference will be
    mapped to an empty string. This key exists for the exact purpose of
    overriding such behaviour and always expects a string value, as if it were
    provided by the user in the directive context.

    {% messagebox(title="No more evaluation and variables") %}
In earlier versions, arara used to evaluate the `default` key and return a
plain, generic `Object` reference, which was then forwarded directly to the
corresponding `command` context. The workflow changed for version 6.0 on.

From now on, `default` always expects a string value, as if it were provided
by the user in the directive context. No variables are available and no more
evaluation is expected from this key. Consider the following example:

```yaml
default: "@{ 1 == 1 }"
```

There is an orb tag expression in this string, which should resolve to
`true` in previous versions of arara. However, from now on, it will not be
evaluated at all and the literal string will be assigned to the `default`
key.

The `default` key, whenever available and in the scenario in which the user
does not provide an explicit value for the current argument in the directive
context, is forwarded to the `flag` context for proper evaluation. Then the
workflow proceeds as usual.
    {% end %}

    {% messagebox(title="Return type") %}
The `default` key, whenever available, returns a string to be evaluated in
the corresponding `flag` context. However, if the target evaluation context
does not exist (i.e, there is no corresponding `flag` key), the value is
transformed into a list of strings and then forwarded directly to the
`command` context. For instance:

```yaml
- identifier: foo
  default: 'bar'
```

This scenario will directly forward `[ 'bar' ]` (a list of strings
containing the specified value as single element) as the value for the
`⋄ foo` variable in the corresponding `command` context.
    {% end %}

    ```yaml
    default: 'stable'
    ```

  - **[O]** `arguments → required`: There might be certain scenarios
    in which a rule could make use of required arguments (for instance, a copy
    operation in which source and target must be provided). The `required` key
    acts as a boolean switch to indicate whether the corresponding argument
    should be mandatory. In this case, set the key value to `true` and the
    argument becomes required. Later on at runtime, arara will throw an error
    if a required parameter is missing in the directive.

    ```yaml
    required: false
    ```

    Note that setting the `required` key value to `false` corresponds to
    omitting the key completely in the rule context, which resorts to the
    default behaviour (i.e, all arguments are optional).

  {% messagebox(title="Note on argument keys") %}
  As seen previously, both `flag` and `default` are marked as optional, but at
  least one of them must occur in the argument specification, otherwise arara
  will throw an error, as it makes no sense to have no argument handling at
  all. Please make sure to specify at least one of them for a consistent
  behaviour!
  {% end %}

  For instance, consider a full example of the `arguments` key, defined with
  only one argument, presented as follows. The hyphen denotes a list element, so
  mind the indentation for correctly specifying the component keys. Also, note
  that, in this case, keys `required` and `default` were completely optional,
  and they were included for didactic purposes.

  ```yaml
  arguments:
  - identifier: shell
    flag: >
      @{
          isTrue(parameters.shell,
                 '--shell-escape',
                 '--no-shell-escape')
      }
    required: false
    default: 'false'
  ```

This is the rule structure in the YAML format used by arara. Keep in mind that
all subtasks in a rule are checked against their corresponding exit status. If
an abnormal execution is detected, the tool will instantly halt and the rule
will fail. Even arara itself will return an exit code different than zero when
this situation happens (detailed in [Command line](@/manual/CLI.md)).

# Directives

A *directive* is a special comment inserted in the source file in which you
indicate how arara should behave. You can insert as many directives as you want.
The tool will read and extract directives from beginning of the file by default.
See _Enabling header mode by default_ in next section for more info.

There are two types of directives in arara which determine the way the
corresponding rules will be instantiated. They are listed as follows. Note that
directives are always preceded by the `arara:` pattern.

- **Empty directive**: This type of directive has already been mentioned in
  [Introduction](@/manual/Introduction.md), it has only the rule name (which
  refers to the `identifier` key from the rule of the same name). All rule
  arguments are mapped to empty lists, except the ones with `default` values,
  mapped to lists containing single elements.

  ```tex
  % arara: pdflatex
  ```

- **Parametrized directive**: This type of directive also has the rule name
  (which refers to the `identifier` key from the rule of the same name), and
  also contains a map of parameters in order to provide additional information
  to the corresponding rule. This map is defined in the YAML format, based on
  the inline style.

  ```tex
  % arara: pdflatex: { shell: yes }
  ```

  Observe that arara relies on named parameters, so they are mapped by their
  corresponding argument identifiers and not by their positions. The syntax for
  a parameter is described as follows. Please refer to the map definition in
  [YAML](@/manual/YAML.md).

  ```
  key : value
  ```

  Note that virtually any type of data can be used as parameter value, so lists,
  integers, booleans, sets and other maps are available as well. However, there
  must be the correct handling of such types in the rule context.

When handling parametrized directives, arara always checks if directive
parameters and rule arguments match. If we try to inject a non-existent
parameter in a parametrized directive, the tool will raise an error about it:

```sh
  __ _ _ __ __ _ _ __ __ _
 / _` | '__/ _` | '__/ _` |
| (_| | | | (_| | | | (_| |
 \__,_|_|  \__,_|_|  \__,_|

Processing "hello.tex" (size: 102 B, last modified: 12/28/2020
10:28:00), please wait.

  ERROR

I found these unknown keys in the directive: (foo). This should
be an easy fix, just remove them from your map.

Total: 0.21 seconds
```

As the message suggests, we need to remove the unknown parameter key from our
directive or rewrite the rule in order to include it as an argument. The first
option is, of course, easier.

Sometimes, directives can span several columns of a line, particularly the ones
with several parameters. We can split a directive into multiple lines by using
the `arara: -->` mark (also known as *arrow notation* during development) to
each line which should compose the directive. We call it a *multiline
directive*. Let us see an example:

```tex
% arara: pdflatex: {
% arara: --> shell: yes,
% arara: --> synctex: yes
% arara: --> }
```

It is important to observe that there is no need of them to be in contiguous
lines, i.e, provided that the syntax for parametrized directives hold for the
line composition, lines can be distributed all over the code. In fact, the log
file (when enabled) will contain a list of all line numbers that compose a
directive. This feature is discussed later on.

{% messagebox(title="Keep lines together") %}
Although it is possible to spread lines of a multiline directive all over the
code, it is considered good practice to keep them together for easier reading
and editing. In any case, you can always see which lines compose a directive by
inspecting the log file.
{% end %}

arara provides logical expressions, written in the MVEL language, and special
operators processed at runtime in order to determine whether and how a directive
should be processed. This feature is named *directive conditional*, or simply
*conditional* as an abbreviation. The following list describes all conditional
operators available in the directive context.

- **[a priori]** `if`: The associated MVEL expression is evaluated beforehand,
  and the directive is interpreted if, and only if, the result of such
  evaluation is true. This directive, when the conditional holds true, is
  executed at most once.

  ```tex
  % arara: pdflatex if missing('pdf') || changed('tex')
  ```

- **[a posteriori]** `until`: The directive is interpreted the first time, then
  the associated MVEL expression evaluation is done. While the result holds
  false, the directive is interpreted again and again. There are no guarantees
  of proper halting.

  ```tex
  % arara: pdflatex until !found('log', 'undefined references')
  ```

- **[a priori]** `unless`: Technically an inverted `if` conditional, the
  associated MVEL expression is evaluated beforehand, and the directive is
  interpreted if, and only if, the result is false. This directive, when the
  conditional holds false, is executed at most once.

  ```tex
  % arara: pdflatex unless unchanged('tex') && exists('pdf')
  ```

- **[a priori]** `while`: The associated MVEL expression is evaluated
  beforehand, the directive is interpreted if, and only if, the result is
  true, and the process is repeated while the result still holds true. There
  are no guarantees of proper halting.

  ```tex
  % arara: pdflatex while missing('pdf') ||
  % arara: --> found('log', 'undefined references')
  ```

Several methods are available in the directive context in order to ease the
writing of conditionals, such as `❖ missing`, `❖ changed`,
`❖ found`, `❖ unchanged`, and `❖ exists`
featured in the previous examples. They will be properly detailed later on.

{% messagebox(title="No infinite loops") %}
Although there are no conceptual guarantees for proper halting of unbounded
loops, we have provided a technical solution for potentially infinite
iterations: arara has a predefined maximum number of loops. The default value is
set to 10, but it can be overridden either in the configuration file or with a
command line flag. We discuss this feature later on.
{% end %}

All directives, regardless of their type, are internally mapped alongside with
the `reference` parameter, discussed earlier on, as a special variable in the
rule context. When inspecting the log file, you will find all map keys and
values for each extracted directive (actually, there is an entire log section
devoted to detailing directives found in the code). See, for instance, the
report of the directive extraction and normalization process performed by arara
when inspecting `doc2.tex`, available in the log file. Note that timestamps were
deliberately removed in order to declutter the output, and line breaks were
included in order to easily spot the log entries.

```tex
% arara: pdflatex
% arara: pdflatex: { shell: yes }
\documentclass{article}

\begin{document}
Hello world.
\end{document}
\end{ncodebox}
```

```
Directive: { identifier: pdflatex, parameters:
{reference=/home/islandoftex/doc2.tex},
conditional: { NONE }, lines: [1] }

Directive: { identifier: pdflatex, parameters:
{shell=yes, reference=/home/islandoftex/doc2.tex},
conditional: { NONE }, lines: [2] }
```

The directive context also features another special parameter named `files`
which expects a non-empty list of file names as plain string values. For each
element of this list, arara will replicate the current directive and point the
element being iterated as current `reference` value (resolved to a proper
absolute, canonical path of the file name). See, for instance, the report of the
directive extraction and normalization process performed by arara when
inspecting `doc3.tex`, available in the log file.

```tex
% arara: pdflatex: { files: [ doc1.tex, doc2.tex ] }
Hello world.
\bye
```

```
Directive: { identifier: pdflatex, parameters:
{reference=/home/islandoftex/doc1.tex},
conditional: { NONE }, lines: [1] }

Directive: { identifier: pdflatex, parameters:
{reference=/home/islandoftex/doc2.tex},
conditional: { NONE }, lines: [1] }
```

It is important to observe that, in this case, `doc3.tex` is a plain TeX file,
but `pdflatex` is actually being called on two LaTeX documents, first `doc1.tex`
and then, at last, `doc2.tex`.

Even when a directive is interpreted with a file other than the one being
processed by arara (through the magic of the `files` parameter), it is possible
to use helper methods in the rule context to get access to the original file and
reference. Such methods are detailed later on.

{% messagebox(title="Orb tag expansion in parameter values") %}
From version 6.0 on, arara is able to expand orb tags within a special `options`
parameter in the directive context. For instance:

```tex
% arara: lualatex: {
% arara: --> options: [ '--output-directory=@{getSession().
% arara: -->                          get("arg:builddir")}'
% arara: -->          ]
% arara: --> }
```

This feature supports the following methods with their documented meanings, as
seen in [Methods](@/manual/Methods.md): `❖ getBasename`, `❖
getSession` and `❖ getOriginalReference`.

Keep in mind that this feature is disabled when arara is running in safe mode,
as seen in [Command line](@/manual/CLI.md).
{% end %}

# Important changes in version 7

{% messagebox(title="A note to users") %}
If this is your first time using arara or you do not have custom rules in the
old format, you can safely ignore this section. All rules shipped with our tool
are already written in the new format.
{% end %}

{% messagebox(title="Enabling header mode by default") %}
The header mode (parse only the first commented lines of a file) is now enabled
by default. You may return to the old behavior disabling header mode in the
configuration file or using the `-w`/`--whole-file` command line flag.
{% end %}

{% messagebox(title="Using an own I/O API instead of Java's File objects") %}
In previous versions, arara's rules relied on Java's `File` API. That was bad
for several reasons. Most importantly, we have switched to Java's `Path` API
quite a while ago. Hence, what was used internally and what users accessed
diverged.

With our general refactoring, there has been a change of strategies: we now
avoid exposing any Java-specific API. The new API which you have access to
when using the `toFile("some file.txt")` method exposes the following properties
and methods:

* The properties `isAbsolute`, `fileName`, `fileSize`, `lastModified`, `parent`,
  `exists`, `isDirectory`, and `isRegularFile` do what their names indicate.
* The method `startsWith(File)` checks if the string representation of the one
  file is prefix of the other one's.
* `normalize()` turns a path into an absolute path and normalizes it.
* `resolve(String | File)` resolves a child.
* `resolveSibling(String | File)` resolves a sibling.
* `readLines()` reads the file's content into a `List<String>`.
* `readText()` reads the file's content into a continuous `String`.
* `writeText(String, append? = false)` writes the argument to the file; the
  optional argument allows appending instead of overwriting.

If you use the `toFile` method in your rules, you do not need to change
anything. All the arara-internal methods like `exists(File)` have been adjusted
to accept objects of the new format. In the end, the only need to change
anything is in rules where you have accessed Java's `File` API yourself.
{% end %}

{% messagebox(title="Add projects") %}
arara now supports projects. See [Projects](@/manual/Projects.md)
for further information on this new feature.
{% end %}

This section pretty much covered the basics of the changes to this version. Of
course, it is highly advisable to make use of the new features available in
arara for achieving better results. If you need any help, please do not hesitate
to contact us. See [Introduction](@/manual/Introduction.md) for more details on
how to get help.

If you are upgrading you may also be interested in reading our
[changelog](https://gitlab.com/islandoftex/arara/-/blob/master/CHANGELOG.md) or
the announcement blog post of this release in the [news section on our
website](https://islandoftex.gitlab.io/arara/news/).
