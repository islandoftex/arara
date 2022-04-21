+++
title = "Configuration file"
description = "Configuration file"
weight = 4
+++

arara provides a persistent model of modifying the underlying execution behaviour or enhancing the execution workflow through the concept of a configuration file. This chapter provides the basic structure of that file, as well as details on the file lookup in the operating system.

# File lookup

Our tool looks for the presence of at least one of four very specific files before execution. These files are presented as follows. Observe that the directories must have the correct permissions for proper lookup and access. The lookup order is also presented: `.araraconfig.yaml` `→` `araraconfig.yaml` `→` `.arararc.yaml` `→` `arararc.yaml`.

arara provides two approaches regarding the location of a configuration file. They dictate how the execution should behave and happen from a user perspective, and are described as follows.

- **Global configuration file:** For this approach, the configuration file should be located at `USER_HOME` which is the home directory of the current user. All subsequent executions of arara will read this configuration file and apply the specified settings accordingly. However, it is important to note that this approach has the lowest lookup priority, which means that a local configuration, presented as follows, will always supersede a global counterpart.

- **Local configuration file:** For this approach, the configuration file should be located at `USER_DIR` which is the working directory associated with the current execution. This directory can also be interpreted as the one relative to the processed file. This approach offers a project-based solution for complex workflows, e.g, a thesis or a book. However, arara must be executed within the working directory, or the local configuration file lookup will fail. Observe that this approach has the highest lookup priority, which means that it will always supersede a global configuration.

{% messagebox(title="Beware of empty configuration files") %}
A configuration file should never be empty, otherwise arara will complain about it. Make sure to populate it with at least one key, or do not write a configuration file at all. The available keys are described later on.
{% end %}

If the logging feature is properly enabled, arara will indicate in the corresponding `arara.log` file whether a configuration file was used during the execution and, if so, the corresponding canonical, absolute path. Logging is detailed later on, in [Logging](/manual/logging).

# Basic structure

The following list describes the basic structure of an arara configuration file by presenting the proper elements (or keys, if we consider the proper YAML nomenclature). Observe that elements marked as **[M]** are mandatory (i.e, the configuration file *has* to have them in order to work). Similarly, elements marked as **[O]** are optional, so you can safely ignore them when writing a configuration file for our tool.

- **[M]** `!config`: This keyword is mandatory and must be the first line of a configuration file. It denotes the object mapping metadata to be internally used by the tool.

- **[O]** *(string list)* `paths`: When looking for rules, arara always searches the default rule path, which consists of a special subdirectory named `rules/` inside another special directory named `ARARA_HOME` (the place where our tool is installed). If no rule is found, the execution halts with an error. The `paths` key specifies a list of directories, represented as plain strings, in which our tool should search for rules. The default path is appended to the list. Then the search happens from the first to the last element, in order.

    ```yaml
    paths:
    - '/home/paulo/rules'
    - '/opt/paulo/rules'
    ```

  There are three variables available in the `paths` context and are described as follows (note that MVEL variables and orb tags are discussed in [MVEL](/manual/mvel). A variable will be denoted by `⋄ variable` in this list.

  - `⋄ user.home`: This variable, as the name implies, holds the value of the absolute, canonical path of `USER_HOME` which is the home directory of the current user, as plain string. Note that the specifics of the home directory (such as name and location) are defined by the operating system involved.

    ```yaml
    paths:
    - '@{user.home}/rules'
    ```

  - `⋄ user.name`: This variable, as the name implies, holds the value of the current user account name, as plain string. On certain operating systems, this value is used to build the home directory structure.

    ```yaml
    paths:
    - '/home/@{user.name}/rules'
    ```

  - `⋄ application.workingDirectory`: This variable, as the name implies, holds the value of the absolute, canonical path of the working directory associated with the current execution, as plain string.

    ```yaml
    paths:
    - '@{application.workingDirectory}/rules'
    ```

  Observe that the `⋄ user` and `⋄ application` variables actually holds maps. However, for didactic purposes, it is easier to use the property navigation feature of MVEL and consider the map references as three independent variables. You can use property navigation styles interchangeably.

{% messagebox(title="Avoid folded and literal styles for scalars in a path") %}
Do not use folded or literal styles for scalars in a path! The orb tag resolution for a path in plain string should be kept as simple as possible, so *always* use the inline style.
{% end %}

- **[O]** *(string)* `language` (default: `en`): This key sets the language of all subsequent executions of arara according to the provided language code value, as plain string. The default language is set to English. Also, it is very important to observe that the `--language` command line option can override this setting.

    ```yaml
    language: nl
    ```

- **[O]** *(integer)* `loops` (default: `10`): This key redefines the maximum number of loops arara will allow for potentially infinite iterations. Any positive integer can be used as the value for this variable. Also, it is very important to observe that the `--max-loops` command line option can override this setting.

    ```yaml
    loops: 30
    ```

- **[O]** *(boolean)* `verbose` (default: `false`): This key activates or deactivates the verbose mode of arara as default mode, according to the associated boolean value. Also, it is very important to observe that the `--verbose` command line option can override this setting if, and only if, this variable holds `false` as the value. Similarly, the `--silent` command line option can override this setting if, and only if, this variable holds `true` as the value.

    ```yaml
    verbose: true
    ```

- **[O]** *(boolean)* `logging` (default: `false`): This key activates or deactivates the logging feature of arara as the default behaviour, according to the associated boolean value. Also, it is very important to observe that the `--log` command line option can override this setting if, and only if, this variable holds `false` as the value.

    ```
    logging: true
    ```

- **[O]** *(boolean)* `header` (default: `true`): This key modifies the directive extraction, according to the associated boolean value. If enabled, arara will extract all directives from the beginning of the file until it reaches a line that is not empty and it is not a comment. Otherwise, the tool will resort to the default behaviour and extract all directives from the entire file. It is very important to observe that the `--whole-file` command line option can override this setting if, and only if, this variable holds `true` as the value.

    ```yaml
    header: false
    ```

- **[O]** *(string)* `logname` (default: `arara`): This key modifies the default log file name, according to the associated plain string value, plus the `log` extension. The value cannot be empty or contain invalid characters. There is no orb tag evaluation in this specific context, only a plain string value. The log file will be written by our tool if, and only if, the `--log` command line option is used.

  {% messagebox(title="Log paths") %}
From version 6.0 on, the log file may now be specified as path *anywhere* on the file system. However, keep in mind that this behavior may be altered for future updates in safe mode.
  {% end %}

    ```yaml
    logname: mylog
    ```

- **[O]** *(string)* `dbname` (default: `arara`): This key modifies the default YAML database file name, according to the associated plain string value, plus the `yaml` extension. The value cannot be empty or contain invalid characters. There is no orb tag evaluation in this specific context, only a plain string value. This database is used by file hashing operations.

    ```yaml
    dbname: mydb
    ```

- **[O]** *(string)* `laf` (default: `none`): This key modifies the default look and feel class reference, i.e,  the appearance of GUI widgets provided by our tool, according to the associated plain string value. The value cannot be empty or contain invalid characters. There is no orb tag evaluation in this specific context, only a plain string value. This look and feel setting is used by UI methods, detailed in [Methods](/manual/methods). Note that this value is used by the underlying Java runtime environment, so a full qualified class name is expected.

    ```yaml
    laf: 'javax.swing.plaf.nimbus.NimbusLookAndFeel'
    ```

  {% messagebox(title="Special keywords for the look and feel setting") %}
Look and feel values other than the default provided by Java offer a more pleasant visual experience to the user, so if your rules or directives employ UI methods, it might be interesting to provide a value to the `laf` key. At the time of writing, arara provides two special keywords that are translated to the corresponding fully qualified Java class names: `none` `→` default look and feel, and `system` `→` system look and feel.

The system look and feel, of course, offers the best option of all since it mimics the native appearance of graphical applications in the underlying system. However, some systems might encounter slow rendering times when this option is used, so your mileage might vary.
  {% end %}

- **[O]** *(string map)* `preambles`: This key holds a string map containing predefined preambles for later use with the `--preamble` option (see [Command line](/manual/cli)). Note that each map key must be unique. Additionally, it it is highly recommended to use lowercase letters without spaces, accents or punctuation symbols, as key values. Only directives, line breaks and conditionals are recognized.

    ```yaml
    preambles:
      twopdftex: |
        % arara: pdftex
        % arara: pdftex
    ```

  {% messagebox(title="Literal style when defining a preamble") %}
When defining preambles in the configuration file, *always* use the literal style for scalar blocks. The reason for this requirement is the proper retention of line breaks, which are significant when parsing the strings into proper directive lines. Using the folded style in this particular scenario will almost surely be problematic.
  {% end %}

- **[O]** *(string)* `defaultPreamble`: This key allows to specify a preamble for arara to use even if there are no directives in the file nor preambles specified on the command-line. Preambles are resolved at execution time, which means that preambles from local configurations will take precedence over global preambles.

    ```yaml
    defaultPreamble: twopdftex
    ```

- **[O]** *(boolean)* `prependPreambleIfDirectivesGiven`: This key allows you to specify a boolean value indicating whether preambles should be applied to all files or only those without directives. It defaults to `true` to avoid breaking existing workflows.

    ```yaml
    prependPreambleIfDirectivesGiven: false
    ```

- **[O]** *(file type list)* `filetypes`: This key holds a list of file types supported by arara when searching for a file name, as well as their corresponding directive lookup patterns. In order to properly set a file type, the keys used in this specification are defined inside the `filetypes →` context and presented as follows.

  - **[M]** `filetypes → extension`: This key, as the name implies, holds the file extension, represented as a plain string and without the leading dot (unless it is part of the extension). An extension is an identifier specified as a suffix to the file name and indicates a characteristic of the corresponding content or intended use. Observe that this key is mandatory when specifying a file type, as our tool does not support files without a proper extension.

    ```yaml
    extension: c
    ```

  - **[M|O]** `filetypes → pattern`: This key holds the directive lookup pattern as a regular expression (which is, of course, represented as a plain string). When introducing a new file type, arara must know how to interpret each line and how to properly find and extract directives, hence this key. Observe that this key is marked as optional and mandatory. The reason for such an unusual indication highly depends on the current scenario and is illustrated as follows.

    - The `pattern` key is entirely *optional* for known file types (henceforth named *default* file types), in case you just want to modify the file name lookup order. It is important to observe that default file types already have their directive lookup patterns set, which incidentally are the same, presented as follows.

      ```
      ^\s*%\s+
      ```

    - The `pattern` key is *mandatory* for new file types and for overriding existing patterns for default file types. Make sure to provide a valid regular expression as key value. It is very important to note that, regardless of the underlying pattern (default or provided through this key), the special `arara:` keyword is immutable and thus included by our tool in every directive lookup pattern.

      ```yaml
      pattern: ^\s*//\s*
      ```

    For instance, let us reverse the default file name lookup order presented in [Command line](/manual/cli). Since the default lookup patterns will be preserved, the corresponding `pattern` keys can be safely omitted. Now it is just a matter of rearranging the entries in the desired order, presented as follows.

    ```yaml
    filetypes:
    - extension: ins
    - extension: drv
    - extension: ltx
    - extension: dtx
    - extension: tex
    ```

    If a default file type is included in the `filetypes` list but others from the same tier are left out, these file types not on the list will implicitly have the lowest priority over the explicit list element during the file name lookup, although still respecting their original lookup order modulo the specified file type. For instance, consider the following list:

    ```yaml
    filetypes:
    - extension: ins
    - extension: drv
    ```

    According to the previous example, three out of five default file types were deliberately left out of the `filetypes` list. As expected, the two default file types provided to this list will have the highest priority during the file name lookup. It is important to note that arara will always honour the original lookup order for omitted default file types, yet favouring the explicit elements. The following list is semantically equivalent to the previous example.

    ```yaml
    filetypes:
    - extension: ins
    - extension: drv
    - extension: tex
    - extension: dtx
    - extension: ltx
    ```

    The following example introduces the definition of a new file type to support `c` files. Observe that, for this specific scenario, the `pattern` key is mandatory, as previously discussed. The resulting list is presented as follows, including the corresponding regular expression pattern.

    ```yaml
    filetypes:
    - extension: c
      pattern: ^\s*//\s*
    ```

    It is important to note that, if no default file type is explicitly specified, as seen in previous example, the original list of default file types will have the highest priority over the `filetypes` values during the file name lookup. The following list is semantically equivalent to the previous example.

    ```yaml
    filetypes:
    - extension: tex
    - extension: dtx
    - extension: ltx
    - extension: drv
    - extension: ins
    - extension: c
      pattern: ^\s*//\s*
      ```

    {% messagebox(title="Do not escape backslashes") %}
When writing a file type pattern, there is no need for escaping backslashes as one does for strings in a typical programming language (including MVEL expressions). In this specific scenario, key values are represented as plain, literal strings.

However, please note that character escaping might be required by the underlying regular expression in some scenarios (i.e, a literal dot in the pattern). It is highly recommended to consult a proper regular expression documentation for a comprehensive overview.
{% end %}

Since arara allows four different names for configuration files, as well as global and local approaches, it is highly advisable to run our tool with the `--log` command line option enabled, in order to easily identify which file was considered for that specific execution. The logging feature is discussed later on, in [Logging](/manual/logging).
