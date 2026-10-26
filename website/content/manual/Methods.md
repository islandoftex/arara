+++
title = "Methods"
description = "Methods"
weight = 8
+++

For new Lua rules, start with [Lua rules](@/manual/Lua.md), including its helper
reference. The MVEL examples in this chapter require the deprecated
`--preferred-engine yaml` engine, which will be discontinued in v9.

arara features several helper methods available in directive conditional and
rule contexts which provide interesting features for enhancing the user
experience, as well as improving the automation itself. This chapter provides a
list of such methods. It is important to observe that virtually all classes from
the Java runtime environment can be used within MVEL expressions, so your
mileage might vary.

{% messagebox(title="A note on writing code") %}
As seen in [MVEL](@/manual/MVEL.md), Java and MVEL code can be used interchangeably
within expressions and orb tags, including instantiation of classes into objects
and invocation of methods. However, be mindful of explicitly importing Java
packages and classes through the classic `import` statement, as MVEL does not
automatically handle imports, or an exception will surely be
raised. Alternatively, you can provide the full qualified name to classes as
well.
{% end %}

{% messagebox(title="A file by any other name") %}
As of version 7, we now avoid exposing any Java-specific API. When you see a
mention to `File`, keep in mind that this is actually a wrapper to our new
internal representation.
{% end %}

Methods are listed with their complete signatures, including potential
parameters and corresponding types. Also, the return type of a method is denoted
by `type` and refers to a typical Java data type (either class or primitive). Do
not worry too much, as there are illustrative examples. A method available in
the directive conditional context will be marked by **[C]** next to the
corresponding signature. Similarly, an entry marked by **[R]** denotes that the
corresponding method is available in the rule context. At last, an entry marked
by **[E]** denotes that the corresponding method is available in the orb tag
expansion within a special `options` parameter in the directive context.

# Lua calls

The typed signatures and Java-labelled examples below use MVEL notation. Lua
helpers have both camelCase and snake_case names: `toFile` and `to_file`, for
instance, refer to the same helper. Examples in this chapter use snake_case for
Lua. The context marks still distinguish rule functions from conditionals.
The Lua code blocks below are rule-function fragments unless a directive is
shown.

Lua uses tables for lists and maps, with list indices starting at one. Pass a
list as `{ "one", "two" }`, rather than MVEL's `[ 'one', 'two' ]`. Paths and
commands returned by helpers retain their runtime types. Paths expose the
selected properties and methods described below; commands can be passed to
other helpers. Neither exposes arbitrary Java methods. The `ctx.reference`
table in rule functions is described in [Lua rules](@/manual/Lua.md#commands-and-arguments).

Some MVEL helpers have no Lua wrapper: `getOriginalFile`, `getOrNull` and
`checkClass`. Lua provides table indexing and `type()` for the latter two
operations; `get_original_reference()` returns the original file reference.

# Path objects

Prefer `MPPPath` properties and methods for file operations in new rules.
`to_file` creates a path from a string. Lua paths support colon calls for methods
and dot access for properties; camelCase and snake_case spellings are equivalent.
The existing file helpers remain available.

```lua
local source = to_file("thesis.tex")
local log = source:resolve_sibling("thesis.log")
if log.exists then
  local text = log:read_text()
end
local output = to_file("output")
output:create_directories()
output:resolve("notes.txt"):write_text("First line\n")
```

| Lua property | Kotlin/MVEL property | Meaning |
| --- | --- | --- |
| `file_name` | `fileName` | Last path segment |
| `file_size` | `fileSize` | Size in bytes; raises an error if unavailable |
| `parent` | `parent` | Parent path; a root is its own parent |
| `exists` | `exists` | Whether the path exists |
| `is_absolute` | `isAbsolute` | Whether the original path spelling is absolute |
| `is_directory` | `isDirectory` | Whether the path is a directory |
| `is_regular_file` | `isRegularFile` | Whether the path is a regular file |

| Lua method | Kotlin/MVEL method | Meaning |
| --- | --- | --- |
| `normalize()` | `normalize()` | Absolute, normalized path |
| `resolve(child)` | `resolve(child)` | Resolve a string or path against this path |
| `resolve_sibling(sibling)` | `resolveSibling(sibling)` | Resolve a string or path against the parent |
| `starts_with(path)` | `startsWith(path)` | Compare whole path segments using their original spellings |
| `read_text()` | `readText()` | Read the complete text |
| `read_lines()` | `readLines()` | Read lines into a list |
| `write_text(text, append)` | `writeText(text, append)` | Write text; `append` defaults to `false` |
| `create_directories()` | `createDirectories()` | Create the directory and missing parents |
| `list_entries()` | `listEntries()` | Direct children as paths |
| `list_files(recursive)` | `listFiles(recursive)` | Regular files in path order; `recursive` defaults to `false` |

Paths display and access their absolute, normalized locations without resolving
symbolic links. `is_absolute`, resolution and `starts_with` retain the original
path spelling; normalize both operands to compare absolute locations.
The existence and file-kind properties return `false` when inspection fails.
Text I/O, size and listing methods raise errors when the operation fails, unlike
`read_from_file` and `write_to_file`, which return an empty list or `false`.
Recursive listing follows directory links and reports cycles as errors.

In MVEL, the reference is already an `MPPPath`:

```java
log = reference.resolveSibling('thesis.log');
if (log.exists) {
    text = log.readText();
}
```

Kotlin callers use the same properties and methods. The complete API also
provides strict metadata, scoped reads and filesystem exports; see
[Using arara as a library](@/manual/Library.md). Lua exposes only the members
listed here. Paths remain valid helper arguments, command arguments and session
values. `ctx.reference` remains a table; use `to_file(ctx.reference.path)` for
path methods in a Lua rule. `current_file()` returns a path;
`get_original_reference()` returns path text in rule functions, which can also
be converted with `to_file`.

# Files

This section introduces methods related to file handling, searching and
hashing. The individual descriptions specify whether an unsuccessful operation
returns an empty result, returns false or raises an error.

In Lua, the explicit file form uses a handle from `to_file`; a string passed
to `exists`, `missing`, `changed`, `unchanged` or `found` is an extension of the
current file:

```lua
local source = to_file("thesis.tex")
local source_exists = exists(source)
local pdf_exists = exists("pdf")
local matches = found(to_file("thesis.log"), "undefined references")
local lines = read_from_file(source)
local written = write_to_file("notes.txt", { "first", "second" }, false)
```

A relative path passed to `to_file` is resolved from the process working
directory. The extension form instead uses the current file's path.

The last argument of `write_to_file` is a boolean: `true` appends and `false`
overwrites. File-reading errors produce an empty list; write errors produce
`false`. File-change checks update a persistent record, explained in
[The file database](@/manual/Database.md).

- **[R]** `getOriginalFile(): String` This method returns the original file
    name, as plain string, regardless of a potential override through the
    special `files` parameter in the directive mapping, as seen in
    [Important concepts](@/manual/Concepts.md).

  ```java
  if (reference.fileName == getOriginalFile()) {
      System.out.println("The 'file' variable
         was not overridden.");
  }
  ```

- **[C|E|R]** `getOriginalReference(): File` This method returns the original
    file reference, as a `File` object, regardless of a potential reference
    override indirectly through the special `files` parameter in the directive
    mapping, as seen in [Important concepts](@/manual/Concepts.md).

  ```java
  if (reference.equals(getOriginalReference())) {
      System.out.println("The 'reference' variable
         was not overridden.");
  }
  ```

- **[C|R]** `currentFile(): File` This method returns the file reference, as a
    `File` object, for the current directive. It is important to observe that
    arara replicates the directive when the special `files` parameter is
    detected amongst the parameters, so each instance will have a different
    reference.

  ```tex
  % arara: pdflatex if currentFile().fileName == 'thesis.tex'
  ```

- **[C|R]** `toFile(String reference): File` This method returns a file (or
    directory) reference, as a `File` object, based on the provided string. Note
    that the string can refer to either a relative entry or a complete, absolute
    path. It is worth mentioning that, in Java, despite the curious name, a
    `File` object can be assigned to either a file or a directory.

  ```java
  f = toFile('thesis.tex');
  ```

- **[C|E|R]** `getBasename(File file): String` This method returns the base name
    (i.e, the name without the associated extension) of the provided `File`
    reference, as a string. Observe that this method ignores a potential path
    reference when extracting the base name. Also, this method will throw an
    exception if the provided reference is not a proper file.

  ```java
  basename = getBasename(toFile('thesis.tex'));
  ```

- **[R]** `getBasename(String reference): String` This method returns the base
    name (i.e, the name without the associated extension) of the provided
    `String` reference, as a string. Observe that this method ignores a
    potential path reference when extracting the base name.

  ```java
  basename = getBasename('thesis.tex');
  ```

- **[R]** `getFiletype(File file): String` This method returns the file type
    (i.e, the associated extension specified as a suffix to the name, typically
    delimited with a full stop) of the provided `File` reference, as a
    string. This method will throw an exception if the provided reference is not
    a proper file. An empty string is returned if, and only if, the provided
    file name has no associated extension.

  ```java
  extension = getFiletype(toFile('thesis.pdf'));
  ```

- **[R]** `getFiletype(String reference): String` This method returns the file
    type (i.e, the associated extension specified as a suffix to the name,
    typically delimited with a full stop) of the provided `String` reference, as
    a string. An empty string is returned if, and only if, the provided file
    name has no associated extension.

  ```java
  extension = getFiletype('thesis.pdf');
  ```

- **[C|R]** `exists(File file): boolean` This method, as the name implies,
    returns a boolean value according to whether the provided `File` reference
    exists. Observe that the provided reference can be either a file or a
    directory.

  ```tex
  % arara: bibtex if exists(toFile('references.bib'))
  ```

- **[C|R]** `exists(String extension): boolean` This method returns a boolean
    value according to whether the base name of the `❖ currentFile`
    reference (i.e, the name without the associated extension) as a string
    concatenated with the provided `String` extension exists. This method eases
    the checking of files which share the current file name modulo extension
    (e.g, log and auxiliary files). Note that the provided string refers to the
    extension, not the file name.

  ```tex
  % arara: pdftex if exists('tex')
  ```

- **[C|R]** `missing(File file): boolean` This method, as the name implies,
    returns a boolean value according to whether the provided `File` reference
    does not exist. It is important to observe that the provided reference can
    be either a file or a directory.

  ```tex
  % arara: pdftex if missing(toFile('thesis.pdf'))
  ```

- **[C|R]** `missing(String extension): boolean` This method returns a boolean
    value according to whether the base name of the `❖ currentFile`
    reference (i.e, the name without the associated extension) as a string
    concatenated with the provided `String` extension does not exist. This
    method eases the checking of files which share the current file name modulo
    extension (e.g, log and auxiliary files). Note that the provided string
    refers to the extension, not the file name.

  ```tex
  % arara: pdftex if missing('pdf')
  ```

- **[C|R]** `changed(File file): boolean` This method returns a boolean value
    according to whether the provided `File` reference has changed since last
    verification, based on a traditional cyclic redundancy check. The file
    reference, as well as the associated hash, is stored in a YAML database file
    named `arara.yaml` located in the project's working directory (the
    database name can be overridden in the configuration file). The method
    semantics (including the return values) is presented as follows.

  | File exists? &nbsp;&nbsp;&nbsp; | Entry exists? &nbsp;&nbsp;&nbsp; | Has changed? &nbsp;&nbsp;&nbsp; | DB action &nbsp;&nbsp;&nbsp; | Result |
  |:------------:|:-------------:|:------------:|:---------:|:------:|
  |      Yes     |      Yes      |      Yes     |   Update  |   Yes  |
  |      Yes     |      Yes      |      No      |     —     |   No   |
  |      Yes     |       No      |       —      |   Insert  |   Yes  |
  |      No      |       No      |       —      |     —     |   No   |
  |      No      |      Yes      |       —      |   Remove  |   Yes  |

  This method updates the database when an entry is inserted, removed or
  changed; an unchanged entry does not need to be rewritten. When using `❖ changed` within a logical expression, make
  sure the evaluation order is correct, specially regarding the use of
  short-circuiting operations. In some scenarios, order does matter.

  ```tex
  % arara: pdflatex if changed(toFile('thesis.tex'))
  ```

  {% messagebox(title="Short-circuit evaluation") %}
  According to the [Wikipedia
  entry](https://en.wikipedia.org/wiki/Short-circuit_evaluation), a *short-circuit
  evaluation* is the semantics of some boolean operators in some programming
  languages in which the second argument is executed or evaluated only if the
  first argument does not suffice to determine the value of the expression. In
  Java (and consequently MVEL), both short-circuit and standard boolean operators
  are available.
  {% end %}

  {% messagebox(title="CRC as a hashing algorithm") %}
  arara internally relies on a CRC32 implementation for file hashing. This
  particular choice, although not designed for hashing, offers an interesting
  trade-off between speed and quality. Besides, since it is not computationally
  expensive as strong algorithms such as MD5 and SHA1, CRC32 can be used for
  hashing typical TeX documents and plain text files with little to no
  collisions.
  {% end %}

- **[C|R]** `changed(String extension): boolean` This method returns a boolean
    value according to whether the base name of the `❖ currentFile`
    reference (i.e, the name without the associated extension) as a string
    concatenated with the provided `String` extension has changed since last
    verification, based on a traditional cyclic redundancy check. The file
    reference, as well as the associated hash, is stored in a YAML database file
    named `arara.yaml` located in the project's working directory (the
    database name can be overridden in the configuration file, as discussed in
    [Configuration](@/manual/Configuration.md). The method semantics (including the
    return values) is presented as follows.

  | File exists? | Entry exists? | Has changed? | DB action | Result |
  |:------------:|:-------------:|:------------:|:---------:|:------:|
  |      Yes     |      Yes      |      Yes     |   Update  |   Yes  |
  |      Yes     |      Yes      |      No      |     —     |   No   |
  |      Yes     |       No      |       —      |   Insert  |   Yes  |
  |      No      |       No      |       —      |     —     |   No   |
  |      No      |      Yes      |       —      |   Remove  |   Yes  |

  This method updates the database when an entry is inserted, removed or
  changed; an unchanged entry does not need to be rewritten. When using `❖ changed` within a logical expression, make
  sure the evaluation order is correct, specially regarding the use of
  short-circuiting operations. In some scenarios, order does matter.

  ```tex
  % arara: pdflatex if changed('tex')
  ```

- **[C|R]** `unchanged(File file): boolean` This method returns a boolean value
    according to whether the provided `File` reference has not changed since
    last verification, based on a traditional cyclic redundancy check. The file
    reference, as well as the associated hash, is stored in a YAML database file
    named `arara.yaml` located in the project's working directory (the
    database name can be overridden in the configuration file, as discussed in
    [Configuration](@/manual/Configuration.md). The method semantics (including the
    return values) is presented as follows.

  | File exists? | Entry exists? | Has changed? | DB action | Result |
  |:------------:|:-------------:|:------------:|:---------:|:------:|
  |      Yes     |      Yes      |      Yes     |   Update  |   No   |
  |      Yes     |      Yes      |      No      |     —     |   Yes  |
  |      Yes     |       No      |       —      |   Insert  |   No   |
  |      No      |       No      |       —      |     —     |   Yes  |
  |      No      |      Yes      |       —      |   Remove  |   No   |

  This method updates the database when an entry is inserted, removed or
  changed; an unchanged entry does not need to be rewritten. When using `❖ unchanged` within a logical expression, make
  sure the evaluation order is correct, specially regarding the use of
  short-circuiting operations. In some scenarios, order does matter.

  ```tex
  % arara: pdflatex if !unchanged(toFile('thesis.tex'))
  ```

- **[C|R]** `unchanged(String extension): boolean` This method returns a boolean
    value according to whether the base name of the `❖ currentFile`
    reference (i.e, the name without the associated extension) as a string
    concatenated with the provided `String` extension has not changed since last
    verification, based on a traditional cyclic redundancy check. The file
    reference, as well as the associated hash, is stored in a YAML database file
    named `arara.yaml` located in the project's working directory (the
    database name can be overridden in the configuration file, as discussed in
    [Configuration](@/manual/Configuration.md). The method semantics (including the
    return values) is presented as follows.

  | File exists? | Entry exists? | Has changed? | DB action | Result |
  |:------------:|:-------------:|:------------:|:---------:|:------:|
  |      Yes     |      Yes      |      Yes     |   Update  |   No   |
  |      Yes     |      Yes      |      No      |     —     |   Yes  |
  |      Yes     |       No      |       —      |   Insert  |   No   |
  |      No      |       No      |       —      |     —     |   Yes  |
  |      No      |      Yes      |       —      |   Remove  |   No   |

  This method updates the database when an entry is inserted, removed or
  changed; an unchanged entry does not need to be rewritten. When using `❖ unchanged` within a logical expression, make
  sure the evaluation order is correct, specially regarding the use of
  short-circuiting operations. In some scenarios, order does matter.

  ```tex
  % arara: pdflatex if !unchanged('tex')
  ```

- **[R]** `writeToFile(File file, String text, boolean append): boolean` This
    method performs a write operation based on the provided parameters. In this
    case, the method writes the `String` text to the `File` reference and
    returns a boolean value according to whether the operation was
    successful. The third parameter holds a `boolean` value and acts as a switch
    indicating whether the text should be appended to the existing content of
    the provided file. Keep in mind that the existing content of a file is
    always overwritten if this switch is disabled. Also, note that the switch
    has no effect if the file is being created at that moment. It is important
    to observe that this method does not raise any exception.

  ```java
  result = writeToFile(toFile('foo.txt'), 'hello world', false);
  ```

  {% messagebox(title="Read and write operations in Unicode") %}
  arara *always* uses Unicode as the encoding format for read and write
  operations. This decision is deliberate as a means to offer a consistent
  representation and handling of text. Unicode can be implemented by different
  character encodings. In our case, the tool relies on UTF-8, which uses one
  byte for the first 128 code points, and up to 4 bytes for other
  characters. The first 128 Unicode code points are the ASCII characters, which
  means that any ASCII text is also UTF-8 text.
  {% end %}

  {% messagebox(title="File system permissions") %}
  Most file systems have methods to assign permissions or access rights to
  specific users and groups of users. These permissions control the ability of
  the users to view, change, navigate, and execute the contents of the file
  system. Keep in mind that read and write operations depend on such
  permissions.
  {% end %}

- **[R]** `writeToFile(String reference, String text, boolean append): boolean`
    This method performs a write operation based on the provided parameters. In
    this case, the method writes the `String` text to the `String` reference and
    returns a boolean value according to whether the operation was
    successful. The third parameter holds a `boolean` value and acts as a switch
    indicating whether the text should be appended to the existing content of
    the provided file. Keep in mind that the existing content of a file is
    always overwritten if this switch is disabled. Also, note that the switch
    has no effect if the file is being created at that moment. It is important
    to observe that this method does not raise any exception.

  ```java
  result = writeToFile('foo.txt', 'hello world', false);
  ```

- **[R]** `writeToFile(File file, List<String> lines, boolean append): boolean`
    This method performs a write operation based on the provided parameters. In
    this case, the method writes the `List<String>` lines to the `File`
    reference and returns a boolean value according to whether the operation was
    successful. The third parameter holds a `boolean` value and acts as a switch
    indicating whether the text should be appended to the existing content of
    the provided file. Keep in mind that the existing content of a file is
    always overwritten if this switch is disabled. Also, note that the switch
    has no effect if the file is being created at that moment. It is important
    to observe that this method does not raise any exception.

  ```java
  result = writeToFile(toFile('foo.txt'),
           [ 'hello world', 'how are you?' ], false);
  ```

- **[R]** `writeToFile(String reference, List<String> lines, boolean append):
    boolean` This method performs a write operation based on the provided
    parameters. In this case, the method writes the `List<String>` lines to the
    `String` reference and returns a boolean value according to whether the
    operation was successful. The third parameter holds a `boolean` value and
    acts as a switch indicating whether the text should be appended to the
    existing content of the provided file. Keep in mind that the existing
    content of a file is always overwritten if this switch is disabled. Also,
    note that the switch has no effect if the file is being created at that
    moment. It is important to observe that this method does not raise any
    exception.

  ```java
  result = writeToFile('foo.txt', [ 'hello world',
           'how are you?' ], false);
  ```

- **[R]** `readFromFile(File file): List<String>` This method performs a read
    operation based on the provided parameter. In this case, the method reads
    the content from the `File` reference and returns a `List<String>` object
    representing the lines as a list of strings. If the reference does not exist
    or an exception is raised due to access permission constraints, the
    `❖ readFromFile` method returns an empty list. Keep in mind that,
    as a design decision, UTF-8 is *always* used as character encoding for read
    operations.

  ```java
  lines = readFromFile(toFile('foo.txt'));
  ```

- **[R]** `readFromFile(String reference): List<String>` This method performs a
    read operation based on the provided parameter. In this case, the method
    reads the content from the `String` reference and returns a `List<String>`
    object representing the lines as a list of strings. If the reference does
    not exist or an exception is raised due to access permission constraints,
    the `❖ readFromFile` method returns an empty list. Keep in mind
    that, as a design decision, UTF-8 is *always* used as character encoding for
    read operations.

  ```java
  lines = readFromFile('foo.txt');
  ```

- **[R]** `listFilesByExtensions(File file, List<String> extensions, boolean
    recursive): List<File>` This method performs a file search operation based
    on the provided parameters. In this case, the method list all files from the
    provided `File` reference according to the `List<String>` extensions as a
    list of strings, and returns a `List<File>` object representing all matching
    files, sorted by path. The leading full stop in each extension must be
    omitted, unless it is part of the search pattern. The third parameter holds
    a `boolean` value and acts as a switch indicating whether the search must
    be recursive, i.e, whether all subdirectories must be searched as well. If
    the reference is not a proper directory or an exception is raised due to
    access permission constraints, the `❖ listFilesByExtensions` method returns
    an empty list.

  ```java
  files = listFilesByExtensions(toFile('/home/islandoftex/Documents'),
          [ 'aux', 'log' ], false);
  ```

- **[R]** `listFilesByExtensions(String reference, List<String> extensions,
    boolean recursive): List<File>` This method performs a file search operation
    based on the provided parameters. In this case, the method list all files
    from the provided `String` reference according to the `List<String>`
    extensions as a list of strings, and returns a `List<File>` object
    representing all matching files. The leading full stop in each extension
    must be omitted, unless it is part of the search pattern. The third
    parameter holds a `boolean` value and acts as a switch indicating whether
    the search must be recursive, i.e, whether all subdirectories must be
    searched as well. If the reference is not a proper directory or an exception
    is raised due to access permission constraints, the `❖
    listFilesByExtensions` method returns an empty list.

  ```java
  files = listFilesByExtensions('/home/islandoftex/Documents',
          [ 'aux', 'log' ], false);
  ```

- **[R]** `listFilesByPatterns(File file, List<String> patterns, boolean
    recursive): List<File>` This method performs a file search operation based
    on the provided parameters. In this case, the method lists all files from
    the provided `File` reference according to the `List<String>` patterns as a
    list of strings, and returns a `List<File>` object representing all matching
    files, sorted by path. The pattern specification is described below. The
    third parameter holds a `boolean` value and acts as a switch indicating
    whether the search must be recursive, i.e, whether all subdirectories must
    be searched as well. If the reference is not a proper directory or an
    exception is raised due to access permission constraints, the
    `❖ listFilesByPatterns` method returns an empty list. It is very important
    to observe that this file search operation might be slow depending on the
    provided directory. It is highly advisable to not rely on recursive
    searches whenever possible.

  {% messagebox(title="Patterns for file search operations") %}
  arara employs wildcard filters as patterns for file search operations. Testing
  is case sensitive by default. The wildcard matcher uses the characters `?` and
  `*` to represent a single or multiple wildcard characters. This is the same as
  often found on typical terminals.
  {% end %}

  ```java
  files = listFilesByPatterns(toFile('/home/islandoftex/Documents'),
          [ '*.tex', 'foo?.txt' ], false);
  ```

- **[R]** `listFilesByPatterns(String reference, List<String> patterns, boolean
    recursive): List<File>` This method performs a file search operation based
    on the provided parameters. In this case, the method lists all files from
    the provided `String` reference according to the `List<String>` patterns as
    a list of strings, and returns a `List<File>` object representing all
    matching files. The pattern specification follows a wildcard filter. The
    third parameter holds a `boolean` value and acts as a switch indicating
    whether the search must be recursive, i.e, whether all subdirectories must
    be searched as well. If the reference is not a proper directory or an
    exception is raised due to access permission constraints, the `❖
    listFilesByPatterns` method returns an empty list. It is very important to
    observe that this file search operation might be slow depending on the
    provided directory. It is highly advisable to not rely on recursive searches
    whenever possible.

  ```java
  files = listFilesByPatterns('/home/islandoftex/Documents',
          [ '*.tex', 'foo?.txt' ], false);
  ```

As the methods presented in this section have transparent error handling, the
writing of rules and conditionals becomes more fluent and not too complex for
the typical user.

# Conditional flow

This section introduces methods related to conditional flow based on *natural
boolean values*, i.e, words that semantically represent truth and falsehood
signs. Such concept provides a friendly representation of boolean values and
eases the use of switches in directive parameters. The tool relies on the
following set of natural boolean values:

- **True:** `yes`, `true`, `1`, `on`

- **False:** `no`, `false`, `0`, `off`

All elements from the provided set of natural boolean values can be used
interchangeably in directive parameters. It is important to observe that arara
throws an exception if a value absent from the set is provided to the methods
described in this section.

- **[R]** `isTrue(String string): boolean` This method returns a boolean value
    according to whether the provided `String` value is contained in the sub-set
    of natural true boolean values. It is worth mentioning that the verification
    is case insensitive, i.e, upper case and lower case symbols are treated as
    equivalent. If the provided value is an empty string, the method returns
    false.

  ```java
  result = isTrue('yes');
  ```

- **[R]** `isFalse(String string): boolean` This method returns a boolean value
    according to whether the provided `String` value is contained in the sub-set
    of natural false boolean values. It is worth mentioning that the
    verification is case insensitive, i.e, upper case and lower case symbols are
    treated as equivalent. If the provided value is an empty string, the method
    returns false.

  ```java
  result = isFalse('off');
  ```

- **[R]** `isTrue(String string, Object yes): Object` This method checks if the
    first parameter is contained in the sub-set of natural true boolean
    values. If the result holds true, the second parameter is
    returned. Otherwise, an empty string is returned. It is worth mentioning
    that the verification is case insensitive, i.e, upper case and lower case
    symbols are treated as equivalent. If the first parameter is an empty
    string, the method returns an empty string.

  ```java
  result = isTrue('on', [ 'ls', '-la' ]);
  ```

- **[R]** `isFalse(String string, Object yes): Object` This method checks if the
    first parameter is contained in the sub-set of natural false boolean
    values. If the result holds true, the second parameter is
    returned. Otherwise, an empty string is returned. It is worth mentioning
    that the verification is case insensitive, i.e, upper case and lower case
    symbols are treated as equivalent. If the first parameter is an empty
    string, the method returns an empty string.

  ```java
  result = isFalse('0', 'pwd');
  ```

- **[R]** `isTrue(String string, Object yes, Object no): Object` This method
    checks if the first parameter is contained in the sub-set of natural true
    boolean values. If the result holds true, the second parameter is
    returned. Otherwise, the third parameter is returned. It is worth mentioning
    that the verification is case insensitive, i.e, upper case and lower case
    symbols are treated as equivalent. If the first parameter is an empty
    string, the method returns the third parameter.

  ```java
  result = isTrue('on', [ 'ls', '-la' ], 'pwd');
  ```

- **[R]** `isFalse(String string, Object yes, Object no): Object` This method
    checks if the first parameter is contained in the sub-set of natural false
    boolean values. If the result holds true, the second parameter is
    returned. Otherwise, the third parameter is returned. It is worth mentioning
    that the verification is case insensitive, i.e, upper case and lower case
    symbols are treated as equivalent. If the first parameter is an empty
    string, the method returns the third parameter.

  ```java
  result = isFalse('0', 'pwd', 'ps');
  ```

- **[R]** `isTrue(String string, Object yes, Object no, Object fallback):
    Object` This method checks if the first parameter is contained in the
    sub-set of natural true boolean values. If the result holds true, the second
    parameter is returned. Otherwise, the third parameter is returned. It is
    worth mentioning that the verification is case insensitive, i.e, upper case
    and lower case symbols are treated as equivalent. If the first parameter is
    an empty string, the method returns the fourth parameter as default value.

  ```java
  result = isTrue('on', 'ls', 'pwd', 'who');
  ```

- **[R]** `isFalse(String string, Object yes, Object no, Object fallback):
    Object` This method checks if the first parameter is contained in the
    sub-set of natural false boolean values. If the result holds true, the
    second parameter is returned. Otherwise, the third parameter is returned. It
    is worth mentioning that the verification is case insensitive, i.e, upper
    case and lower case symbols are treated as equivalent. If the first
    parameter is an empty string, the method returns the fourth parameter as
    default value.

  ```java
  result = isFalse('0', 'pwd', 'ps', 'ls');
  ```

- **[R]** `isTrue(boolean value, Object yes): Object` This method evaluates the
    first parameter as a boolean expression. If the result holds true, the
    second parameter is returned. Otherwise, an empty string is returned.

  ```java
  result = isTrue(1 == 1, 'yes');
  ```

- **[R]** `isFalse(boolean value, Object yes): Object` This method evaluates the
    first parameter as a boolean expression. If the result holds false, the
    second parameter is returned. Otherwise, an empty string is returned.

  ```java
  result = isFalse(1 != 1, 'yes');
  ```

- **[R]** `isTrue(boolean value, Object yes, Object no): Object` This method
    evaluates the first parameter as a boolean expression. If the result holds
    true, the second parameter is returned. Otherwise, the third parameter is
    returned.

  ```java
  result = isTrue(1 == 1, 'yes', 'no');
  ```

- **[R]** `isFalse(boolean value, Object yes, Object no): Object` This method
    evaluates the first parameter as a boolean expression. If the result holds
    false, the second parameter is returned. Otherwise, the third parameter is
    returned.

  ```java
  result = isFalse(1 != 1, 'yes', 'no');
  ```

Supported by the concept of natural boolean values, the methods presented in
this section ease the use of switches in directive parameters and can be adopted
as valid alternatives for traditional conditional flows, when applied.

# Strings

In Lua, `build_string` joins non-empty values with spaces and
`replicate_pattern` applies a format to each list entry:

```lua
local options = build_string("--draft", "", "--synctex=1")
local names = replicate_pattern("chapter-%s.tex", { "one", "two" })
```

String manipulation constitutes one of the foundations of rule interpretation in
our tool. This section introduces methods for handling such types, as a means to
offer high level constructs for users.

- **[R]** `isEmpty(String string): boolean` This method returns a boolean value
    according to whether the provided `String` value is empty, i.e, the string
    length is equal to zero.

  ```java
  result = isEmpty('not empty');
  ```

- **[R]** `isNotEmpty(String string): boolean` This method returns a boolean
    value according to whether the provided `String` value is not empty, i.e,
    the string length is greater than zero.

  ```java
  result = isNotEmpty('not empty');
  ```

- **[R]** `isEmpty(String string, Object yes): boolean` This method checks if
    the first parameter is empty, i.e, if the string length is equal to zero. If
    the result holds true, the second parameter is returned. Otherwise, an empty
    string is returned.

  ```java
  result = isEmpty('not empty', 'ps');
  ```

- **[R]** `isNotEmpty(String string, Object yes): boolean` This method checks if
    the first parameter is not empty, i.e, if the string length is greater than
    zero. If the result holds true, the second parameter is returned. Otherwise,
    an empty string is returned.

  ```java
  result = isNotEmpty('not empty', 'ls');
  ```

- **[R]** `isEmpty(String string, Object yes, Object no): boolean` This method
    checks if the first parameter is empty, i.e, if the string length is equal
    to zero. If the result holds true, the second parameter is
    returned. Otherwise, the third parameter is returned.

  ```java
  result = isEmpty('not empty', 'ps', 'ls');
  ```

- **[R]** `isNotEmpty(String string, Object yes, Object no): boolean` This
    method checks if the first parameter is not empty, i.e, if the string length
    is greater than zero. If the result holds true, the second parameter is
    returned. Otherwise, the third parameter is returned.

  ```java
  result = isNotEmpty('not empty', 'ls', 'ps');
  ```

- **[R]** `buildString(Object... objects): String` This method returns a string
    based on the provided array of objects, separating each element by one blank
    space. It is important to observe that empty values are not
    considered. Also, note that the object array is denoted by a
    comma-separated sequence of elements in the actual method call, resulting in
    a variable number of parameters.

  ```java
  result = buildString('a', 'b', 'c', 'd');
  ```

- **[R]** `trimSpaces(String string): String` This method trims spaces from the
    provided parameter, i.e, leading and trailing spaces in the `String`
    reference are removed, and returns the resulting string. It is important to
    observe that non-boundary spaces inside the string are not removed at all.

  ```java
  result = trimSpaces('   hello world   ');
  ```

- **[R]** `replicatePattern(String pattern, List<Object> values): List<Object>`
    This method replicates the provided pattern to each element of the second
    parameter and returns the resulting list. The pattern must contain exactly
    one placeholder. For instance, `\%s` denotes a string representation of the
    provided argument. Please refer to the `Formatter` class reference in the
    [Java
    documentation](https://docs.oracle.com/javase/7/docs/api/java/util/Formatter.html)
    for more information on placeholders. This method raises an exception if an
    invalid pattern is applied.

  ```java
  names = replicatePattern('My name is %s', [ 'Brent', 'Nicola' ]);
  ```

- **[C|R]** `found(File file, String regex): boolean` This method returns a
    boolean value according to whether the content of the provided `File`
    reference contains at least one match of the provided `String` regular
    expression. It is important to observe that this method raises an exception
    if an invalid regular expression is provided as the parameter or if the
    provided file reference does not exist.

  ```tex
  % arara: pdflatex while found(toFile('article.log'),
  % arara: --> 'undefined references')
  ```

- **[C|R]** `found(String extension, String regex): boolean` This method returns
    a boolean value according to whether the content of the base name of the
    `❖ currentFile` reference (i.e, the name without the associated
    extension) as a string concatenated with the provided `String` extension
    contains at least one match of the provided `String` regular expression. It
    is important to observe that this method raises an exception if an invalid
    regular expression is provided as the parameter or if the provided file
    reference does not exist.

  ```tex
  % arara: pdflatex while found('log', 'undefined references')
  ```

The string manipulation methods presented in this section constitute an
interesting and straightforward approach to handling directive parameters
without the usual verbosity in writing typical Java constructs.

# Operating systems

This section introduces methods related to the underlying operating system
detection, as a means of providing a straightforward approach to writing
cross-platform rules.

- **[R]** `isWindows(): boolean` This method returns a boolean value according
    to whether the underlying operating system vendor is Microsoft Windows.

  ```java
  if (isWindows()) { System.out.println('Running Windows.'); }
  ```

- **[R]** `isLinux(): boolean` This method returns a boolean value according to
    whether the underlying operating system vendor is a Linux instance.

  ```java
  if (isLinux()) { System.out.println('Running Linux.'); }
  ```

- **[R]** `isMac(): boolean` This method returns a boolean value according to
    whether the underlying operating system vendor is Apple Mac OS.

  ```java
  if (isMac()) { System.out.println('Running Mac OS.'); }
  ```

- **[R]** `isUnix(): boolean` This method returns a boolean value according to
    whether the underlying operating system vendor is any Unix variation.

  ```java
  if (isUnix()) { System.out.println('Running Unix.'); }
  ```

- **[R]** `isCygwin(): boolean` This method returns a boolean value according to
    whether the underlying operating system vendor is Microsoft Windows and
    arara is being executed inside a Cygwin environment.

  ```java
  if (isCygwin()) { System.out.println('Running Cygwin.'); }
  ```

  {% messagebox(title="Cygwin paths") %}
  It is worth mentioning that Cygwin has its own path handling which is not
  reliable when (JVM) applications need to invoke system commands. For instance,
  the following invocation does not work:

```tex
% arara: pdflatex
```

  Since `pdflatex` is a symbolic link to `pdftex` with the proper format, we
  encourage Cygwin users to rely on the following trick:

```tex
% arara: pdftex: { options: [ '--fmt=pdflatex' ] }
```

  The same trick can be employed by other TeX engines with appropriate `--fmt`
  flags as well. We are still investigating this issue and looking for potential
  alternatives.
  {% end %}

- **[R]** `isWindows(Object yes, Object no): Object` This method checks if the
    underlying operating system vendor is Microsoft Windows. If the result holds
    true, the first parameter is returned. Otherwise, the second parameter is
    returned.

  ```java
  command = isWindows('del', 'rm');
  ```

- **[R]** `isLinux(Object yes, Object no): Object` This method checks if the
    underlying operating system vendor is a Linux instance. If the result holds
    true, the first parameter is returned. Otherwise, the second parameter is
    returned.

  ```java
  command = isLinux('rm', 'del');
  ```

- **[R]** `isMac(Object yes, Object no): Object` This method checks if the
    underlying operating system vendor is Apple Mac OS. If the result holds
    true, the first parameter is returned. Otherwise, the second parameter is
    returned.

  ```java
  command = isMac('ls', 'dir');
  ```

- **[R]** `isUnix(Object yes, Object no): Object` This method checks if the
    underlying operating system vendor is any Unix variation. If the result
    holds true, the first parameter is returned. Otherwise, the second parameter
    is returned.

  ```java
  command = isUnix('tree', 'dir');
  ```

- **[R]** `isCygwin(Object yes, Object no): Object` This method checks if the
    underlying operating system vendor is Microsoft Windows and if arara is
    being executed inside a Cygwin environment. If the result holds true, the
    first parameter is returned. Otherwise, the second parameter is returned.

  ```java
  command = isCygwin('ls', 'dir');
  ```

The methods presented in the section provide useful information to help users
write cross-platform rules and thus enhance the automation experience based on
specific features of the underlying operating system.

# Type checking

Lua helpers `is_string`, `is_boolean`, `is_list` and `is_map` test Lua values.
Both `is_list` and `is_map` recognize tables; they do not distinguish a sequence
from a keyed table. Check the expected keys or sequence entries when that
distinction matters. The `checkClass` method below belongs to MVEL's Java type
system.

In certain scenarios, a plain string representation of directive parameters
might be inadequate or insufficient given the rule requirements. To this end,
this section introduces methods related to type checking as a means to provide
support and verification for common data types.

- **[R]** `isString(Object object): boolean` This method returns a boolean value
    according to whether the provided `Object` object is a string or any
    extended type.

  ```java
  result = isString('foo');
  ```

- **[R]** `isList(Object object): boolean` This method returns a boolean value
    according to whether the provided `Object` object is a list or any extended
    type.

  ```java
  result = isList([ 1, 2, 3 ]);
  ```

- **[R]** `isMap(Object object): boolean` This method returns a boolean value
    according to whether the provided `Object` object is a map or any extended
    type.

  ```java
  result = isMap([ 'Paulo' : 'Palmeiras', 'Carla' : 'Inter' ]);
  ```

- **[R]** `isBoolean(Object object): boolean` This method returns a boolean
    value according to whether the provided `Object` object is a boolean or any
    extended type.

  ```java
  result = isBoolean(false);
  ```

- **[R]** `checkClass(Class clazz, Object object): boolean` This method returns
    a boolean value according to whether the provided `Object` object is an
    instance or a subtype of the provided `Class` class. It is interesting to
    note that all methods presented in this section internally rely on
    `❖ checkClass` for type checking.

  ```java
  result = checkClass(List.class, [ 'a', 'b' ]);
  ```

The methods presented in this section cover the most common types used in
directive parameters and should suffice for expressing the rule requirements. If
a general approach is needed, please refer to the `❖ checkClass`
method for checking virtually any type available in the Java environment.

# Classes and objects

arara can be extended at runtime with code from JVM languages, such as Groovy,
Scala, Clojure and Kotlin. The tool can load classes from `class` and `jar`
files and even instantiate them. This section introduces methods related to
class loading and object instantiation.

{% messagebox(title="Ordered pairs") %}
According to the [Wikipedia entry](https://en.wikipedia.org/wiki/Ordered_pair),
in mathematics, an *ordered pair* *(a, b)* is a pair of objects. The order in
which the objects appear in the pair is significant: the ordered pair *(a, b)*
is different from the ordered pair *(b, a)* unless *a = b*. In the ordered pair
*(a, b)*, the object *a* is called the *first* entry, and the object *b* the
*second* entry of the pair. arara relies on this concept with the helper
`Pair<A, B>` class, in which `A` and `B` denote the component classes, i.e, the
types associated to the pair elements. In order to access the pair entries, the
class provides two property accessors:

- `❖ first: A`: This property accessor, as the name implies, returns
  the first entry of the ordered pair, as an `A` object.

- `❖ second: B`: This property accessor, as the name implies, returns
  the second entry of the ordered pair, as a `B` object.

Keep in mind that the entries in the `Pair` class, once defined, cannot be
modified to other values. The initial values are set during instantiation and,
therefore, only entry getters are available to the user during the object life
cycle.
{% end %}

{% messagebox(title="Status for class loading and instantiation") %}
The class loading and instantiation methods provided by arara typically return a
pair composed of a status value and a class or object reference. The status
acts as a description of the underlying operation itself and might indicate
potential issues. The possible values are:

- `SUCCESS`: Successful execution
- `FILE_NOT_FOUND`: File does not exist
- `MALFORMED_URL`: File URL is incorrect
- `CLASS_NOT_FOUND`: Class was not found
- `ILLEGAL_ACCESS`: Access policy violation
- `INSTANTIATION_EXCEPTION`: Instantiation exception

Please make sure to *always* check the returned status when using class
loading and instantiation methods in directive and rule contexts. This feature
is quite powerful yet tricky and subtle!
{% end %}

Lua returns a table with `status` (one of the names above) and `value` (the
opaque class or object reference), rather than an ordered pair:

```lua
local result = load_class("mymath.jar", "com.github.cereda.mymath.Arithmetic")
if result.status ~= "SUCCESS" then
  throw_error("Could not load the arithmetic class")
end
```

The Java-method syntax in MVEL examples does not apply to these loaded values in Lua.
Both loading helpers are rejected in safe mode.

- **[C|R]** `loadClass(File file, String name):
    Pair<ClassLoading.ClassLoadingStatus, Object>` This method loads a class
    based on the canonical name from the provided `File` reference and returns
    an ordered pair containing the status and the class reference itself. The
    reference must name a class-path directory or a `jar` archive. If an exception is raised,
    this method returns the `Object` class reference as second entry of the
    pair.

  ```java
  result = loadClass(toFile('mymath.jar'),
           'com.github.cereda.mymath.Arithmetic');
  ```

- **[C|R]** `loadClass(String ref,String name):
    Pair<ClassLoading.ClassLoadingStatus, Object>` This method loads a class
    based on the canonical name from the provided `String` reference and returns
    an ordered pair containing the status and the class reference itself. The
    reference must name a class-path directory or a `jar` archive.  If an exception is raised,
    this method returns the `Object` class reference as second entry of the
    pair.

    ```java
    result = loadClass('mymath.jar',
             'com.github.cereda.mymath.Arithmetic');
    ```

- **[C|R]** `loadObject(File file,String name):
    Pair<ClassLoading.ClassLoadingStatus, Object>` This method loads a class
    based on the canonical name from the provided `File` reference and returns
    an ordered pair containing the status and a proper corresponding object
    instantiation. The reference must name a class-path directory or a `jar`
    archive. If an
    exception is raised, this method returns an `Object` object as second entry
    of the pair.

  ```java
  result = loadObject(toFile('mymath.jar'),
           'com.github.cereda.mymath.Trigonometric');
  ```

- **[C|R]** `loadObject(String ref, String name):
    Pair<ClassLoading.ClassLoadingStatus, Object>` This method loads a class
    based on the canonical name from the provided `String` reference and returns
    an ordered pair containing the status and a proper corresponding object
    instantiation. The reference must name a class-path directory or a `jar`
    archive. If an
    exception is raised, this method returns an `Object` object as second entry
    of the pair.

  ```java
  result = loadObject('mymath.jar',
           'com.github.cereda.mymath.Trigonometric');
  ```

This section presented class loading and instantiation methods which may
significantly enhance the expressiveness of rules and directives. However, make
sure to use such feature with great care and attention.

# Dialog boxes

The dialog helpers interact through the terminal. They work without a graphical
display and accept redirected standard input. Questions pause execution while
waiting for an answer; this waiting time is included in the total runtime.

The first argument selects a message label:

- `1`: Error
- `2`: Information
- `3`: Warning
- `4`: Question
- Any other value: Plain

Dialog labels, selection prompts and input errors use the configured language;
titles, messages and option text remain as supplied by the rule.
Messages wrap at the configured terminal output width; dialog calls no longer
take a pixel-width argument.

{% messagebox(title="Migrating graphical dialogs") %}
Remove the pixel-width argument from calls such as
`showInput(250, 4, 'Title', 'Question')`; use
`showInput(4, 'Title', 'Question')` instead. Remove the `laf` configuration key.
Rename `showDropdown` to `showList` (Lua: `show_dropdown` to `show_list`).
The old helper names are no longer available. Selection indices still start at one.
{% end %}

- **[R]** `showMessage(int type, String title, String text): void`

  Prints the message and returns immediately, without reading input or waiting
  for confirmation.

  ```java
  showMessage(2, 'My title', 'My message');
  ```

- **[C|R]** `showOptions(int type, String title, String text, Object... options): int`

  Prints the options as a numbered list. Enter a number starting at `1` to
  select an option, or `0` to cancel. Invalid answers prompt again. An empty
  list returns `0` without reading input.

  ```tex
  % arara: pdflatex if showOptions(4, 'Important!',
  % arara: --> 'Do you like ice cream?', 'Yes!', 'No!') == 1
  ```

- **[C|R]** `showList(int type, String title, String text, Object... options): int`

  Uses the same numbered terminal selection as `showOptions`, including `0`
  to cancel. No item is selected automatically.

  ```tex
  % arara: pdflatex if showList(4, 'Important!',
  % arara: --> 'Who deserves the tick?', 'David Carlisle',
  % arara: --> 'Enrico Gregorio', 'Joseph Wright',
  % arara: --> 'Heiko Oberdiek') == 2
  ```

- **[C|R]** `showInput(int type, String title, String text): String`

  Reads one line and returns its trimmed text. Pressing Enter on a blank line
  returns an empty string.

  ```tex
  % arara: pdflatex if showInput(4, 'Important!',
  % arara: --> 'Who wrote arara?') == 'Paulo'
  ```

{% messagebox(title="Closed input") %}
If standard input ends while a selection or text answer is required, arara
reports an error. Closed input does not cancel a selection or accept a default
answer. For unattended runs, supply the required answers through standard input
or use rules without interactive questions.
{% end %}

# Commands

Lua command helpers accept nested argument tables and return a command handle:

```lua
return get_command_with_working_directory("build", "pdflatex",
    { "--interaction=nonstopmode", "main file.tex" })
```

The file name remains one argument; it does not need shell quoting. The working
directory is relative to the process directory unless an absolute path is given.
The `get_command` variant uses the current project's working directory.

arara features the `Command` object, a new approach for handling system commands
based on a high level structure with explicit argument parsing.

{% messagebox(title="The anatomy of a command") %}
From the user perspective, a `Command` object is simply a good old list of
`Object` objects, in which the list head (i.e, the first element) is the
underlying system command, and the list tail (i.e, the remaining elements), if
any, contains the associated command line arguments. For instance, given `[
'pdflatex', '--shell-escape', '--synctex=1', 'thesis.tex' ]`, we have:

- Head: `'pdflatex'`
- Tail: `[ '--shell-escape', '--synctex=1', 'thesis.tex' ]`

From the previous example, it is important to observe that a potential file name
quoting is not necessary. The underlying system command execution library
handles the provided arguments accordingly.

Behind the scenes, however, arara employs a different workflow when constructing
a `Command` object. The tool sets the working directory path for the current
command to `USER_DIR` which is based on the current execution. The working
directory path can be explicitly set through specific method calls, described
later on in this section.

The list of objects is then completely flattened and all elements are mapped to
their string representations through corresponding `❖ toString`
calls. Finally, the proper `Command` object is constructed. Keep in mind that,
although a command takes a list (or even an array) of objects, which can be of
any type, the internal representation is *always* a list of strings.
{% end %}

A list of objects might contain nested lists, i.e, a list within another
list. As previously mentioned, arara employs *list flattening* when handling a
list of objects during a `Command` object instantiation. As a means to
illustrate this handy feature, consider the following list of integers:

```java
[ 1, 2, [ 3, 4 ], 5, [ [ 6, 7 ], 8 ], 9, [ [ 10 ] ]
```
Note that the above list of integers contains nested lists. When applying list
flattening, arara recursively adds the elements of nested lists to the original
list and then removes the nested occurrences. Please refer to the source code
for implementation details. The new flattened list is presented as follows.

```java
[ 1, 2, 3, 4, 5, 6, 7, 8, 9, 10 ]
```

List flattening and string mapping confer expressiveness and flexibility to the
`Command` object construction, as users can virtually use any data type to
describe the underlying rule logic and yet obtain a consistent representation.

- **[R]** `getCommand(List<String> elements): Command` This method, as the name
    implies, returns a `Command` object according to the provided list of
    `String` elements. If the list is empty, the tool will ignore the
    execution.

  ```java
  return getCommand([ 'ls', '-l' ]);
  ```

- **[R]** `getCommand(Object... elements): Command` This method, as the name
    implies, returns a `Command` object according to the provided array of
    `Object` elements. If the array is empty, the tool will ignore the
    execution. Note that the object array is denoted by a comma-separated
    sequence of elements in the actual method call, resulting in a variable
    number of parameters.

  ```java
  return getCommand('pdflatex', '--shell-escape', 'thesis.tex');
  ```

- **[R]** `getCommandWithWorkingDirectory(File directory, List<String>
    elements): Command` This method, as the name implies, sets the working
    directory based on the provided `File` reference and returns a proper
    `Command` object according to the provided list of `String` elements. If the
    list is empty, the tool will ignore the execution.

  ```java
  return getCommandWithWorkingDirectory(toFile('/home/islandoftex'),
         [ 'ls', '-l' ]);
  ```

- **[R]** `getCommandWithWorkingDirectory(String path, List<String> elements):
    Command` This method, as the name implies, sets the working directory based
    on the provided `String` reference and returns a proper `Command` object
    according to the provided list of `String` elements. If the list is empty,
    the tool will ignore the execution.

  ```java
  return getCommandWithWorkingDirectory('/home/islandoftex',
         [ 'ls', '-l' ]);
  ```

- **[R]** `getCommandWithWorkingDirectory(File directory, Object... elements):
    Command` This method, as the name implies, sets the working directory based
    on the provided `File` reference and returns a proper `Command` object
    according to the provided array of `Object` elements. If the array is empty,
    the tool will ignore the execution. Note that the object array is denoted by
    a comma-separated sequence of elements in the actual method call, resulting
    in a variable number of parameters.

  ```java
  return getCommandWithWorkingDirectory(toFile('/home/islandoftex'),
         'pdflatex', '--shell-escape', 'thesis.tex');
  ```

- **[R]** `getCommandWithWorkingDirectory(String path, Object... elements):
    Command` This method, as the name implies, sets the working directory based
    on the provided `String` reference and returns a proper `Command` object
    according to the provided array of `Object` elements. If the array is empty,
    the tool will ignore the execution. Note that the object array is denoted by
    a comma-separated sequence of elements in the actual method call, resulting
    in a variable number of parameters.

  ```java
  return getCommandWithWorkingDirectory('/home/islandoftex',
         'pdflatex', '--shell-escape', 'thesis.tex');
  ```

The methods presented in this section constitute the foundations of underlying
system command execution. In particular, whenever possible, it is highly
advisable to use `Command` objects through proper `❖ getCommand`
method calls, as the plain string approach used in previous versions of our tool
is marked as deprecated and will be removed in future versions.

# Others

This section introduces assorted methods provided by arara as a means to improve
the automation itself with expressive rules and enhance the user
experience. Such methods are properly described as follows.

{% messagebox(title="Session") %}
Rules are designed under the *encapsulation* notion, such that the direct access
to internal workings of such structures is restricted. However, as a means to
support framework awareness, arara provides a mechanism for data sharing across
rule contexts, implemented as a `Session` object. In practical terms, this
particular object is a global, persistent map composed of `String` keys and
`Object` values available throughout the entire execution. The public methods of
a session are described as follows:

- `❖ put(String key, Object value): void` This method, as the name
  implies, inserts an object into the session, indexed by the provided
  key. Observe that, if the session previously contained a mapping for the
  provided key, the old value is replaced by the specified value.

- `❖ remove(String key): void` This method, as the name implies,
  removes  the mapping for the provided key from the session. Be mindful that an
  attempt to remove a mapping for a nonexistent key will raise an exception.

- `❖ contains(String key): boolean` This method, as the name implies,
  returns a boolean value according to whether the session contains a mapping
  for the provided key. It is highly advisable to use this method before
  attempting to remove a mapping from the session.

- `❖ get(String key): Object` This method, as the name implies,
  returns the object value to which the specified key is mapped. Be mindful that
  an attempt to return a value for a nonexistent key will raise an exception.

- `❖ forget(): void` This method, as the name implies, removes all
  of the existing mappings from the session. The session object will be
  effectively empty after this call returns.

It is important to observe that the `Session` object provided by our tool
follows the *singleton* pattern, i.e, a software design pattern that restricts
the instantiation of a class to one object. Therefore, the same session is
consistently shared across rule contexts.
{% end %}

- **[R]** `halt(int status = 0): void` Stops processing the current file after
  the current rule expression. `halt()` requests a successful stop; a nonzero
  status requests a failure. No command needs to be returned after this call.
  Halt requests are cleared at file boundaries, including for files with the
  same name.

  {% messagebox(title="Halt requests in version 8") %}
  In arara 7, rules could request a halt through a session entry:

  ```java
  getSession().put('arara:myfile.tex:halt', 42);
  ```

  In arara 8, that entry is no longer read and setting it does not stop
  execution. Replace it with a call affecting the current file:

  ```java
  halt(42);
  ```
  {% end %}

- **[C|E|R]** `getSession(): Session` This method, as the name implies, returns
    the `Session` object for data sharing across rule contexts. Keep in mind
    that a session cannot contain duplicate keys. Each key can map to at most
    one value.

  ```java
  name = getSession().get('name');
  ```

  In Lua, the session is a table of operations called with a dot, not a colon:

  ```lua
  local session = get_session()
  session.put("source", to_file("thesis.tex"))
  local source = session.get("source")
  local present = exists(source)
  session.remove("source")
  ```

  File and command handles keep their types when stored in the session.
  Session values last for the current process, unlike the file database.

- **[R]** `throwError(String message): void` This method deliberately throws an
    error to be intercepted later on during execution. Consider using such
    method for an explicit notification about unexpected or unsought scenarios,
    e.g, wrong parameter types or values. The raised error has an associated
    message which is displayed in the terminal and added to the log file.

  ```java
  options = 'not a list';
  if (!isList(options)) {
      throwError('I was expecting a list.');
  }
  ```

- **[R]** `isVerboseMode(): boolean` This method, as the name implies, returns a
    boolean value according to whether arara is being executed in verbose mode,
    enabled through either the `--verbose` command line option or the
    corresponding key in the configuration file. Note that the logical negation
    of such method indicates whether the tool is being executed in silent mode.

  ```java
  verbose = isVerboseMode();
  ```

- **[R]** `isOnPath(String name): boolean` This method, as the name implies,
    returns a boolean value according to whether the provided `String` reference
    representing a command name is reachable from the system path.  For
    portability reasons, there is no need to provide extensions to Microsoft
    Windows command names, as arara will look for common patterns. This
    behaviour is expected and by design. However, be mindful that the search is
    case sensitive.

  ```java
  result = isOnPath('pdftex');
  ```

  {% messagebox(title="Path inspection") %}
  According to the [Wikipedia
  entry](https://en.wikipedia.org/wiki/PATH_(variable)), `PATH` is an
  environment variable on Unix-like operating systems and Microsoft Windows,
  specifying a set of directories where executable programs are located. arara
  performs a file search operation based on all directories specified in the
  system path, filtering files by name (and extensions, when in Microsoft
  Windows). When an exact match is found, the search is
  concluded. Notwithstanding the great effort, it is very important to note that
  there is no guarantee that our tool will be able to correctly reach the
  command in all scenarios.
  {% end %}

- **[R]** `unsafelyExecuteSystemCommand(Command command): Pair<Integer, String>`
    This method, which has a very spooky name, unsafely executes the provided
    `Command` reference and returns an ordered pair containing the exit status
    and combined standard output and standard error. A completed command keeps
    its actual exit status. If launching or waiting for the command fails,
    including a timeout, the pair contains `-99` and the exception description.
    Always check the returned status. This helper is unavailable in safe mode.

  ```java
  result = unsafelyExecuteSystemCommand(getCommand('ls'));
  ```

  {% messagebox(title="Hic sunt leones") %}
  Please *do not abuse* this method! Keep in mind that this particular feature
  is included for very specific scenarios in which the command streams are
  needed ahead of time for proper decision making.
  {% end %}

- **[R]** `isSubdirectory(File directory): boolean` This method checks whether
    the provided `File` reference is a valid subdirectory under the project
    hierarchy, return a corresponding boolean value. This is a check to impose a
    possible restriction in the rule scope, so that users can change down to
    subdirectories in their projects but not up, outside of the root directory.

  ```java
  valid = isSubdirectory(toFile('chapters/'));
  ```

- **[R]** `getOrNull(List<String> list, int index): String` This method attempts to retrieve
  a list element based on an integer index. If the index is out of bounds, a
  `null` value is returned instead.

  ```java
  list = [ 'a', 'b', 'c' ];
  third = getOrNull(list, 2);
  ```

- **[R]** `getOrNull(List<String> list): String` This method attempts to retrieve
  the first element of the provided list. If the list is empty, a `null` value
  is returned instead.

  ```java
  list = [ 'a', 'b', 'c' ];
  first = getOrNull(list);
  ```

{% messagebox(title="Flags and reserved storage in a session") %}
The following session namespaces provide environment and argument values:

- `environment`: This namespace is quite intuitive: arara will store the current
  state of the systems environment variables in its session. You may alter these
  values in the session storage but they will not be written back to the system
  configuration. To access an environment variable, you can use its usual name
  prefixed by `environment:`:

  ```java
  path = getSession().get('environment:PATH');
  ```

- `arg`: This namespace acts as a bridge between contexts and the command line
  and/or properties files by providing access to key/value pairs defined at 
  runtime through `--call-property` or `--properties-file` command line flags.

  ```java
  key = getSession().get('arg:key');
  ```

Please refer to [Command line](@/manual/CLI.md) for more details on
both `--call-property` and `--properties-file` command line flags.
{% end %}

The methods presented in this section provide interesting features for
persistent data sharing, error handling, early command execution, and
templating. It is important to note that more classes, objects and methods can
be incorporated into arara through class loading and object instantiation,
extending the features and enhancing the overall user experience.
