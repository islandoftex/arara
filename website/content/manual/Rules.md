+++
title = "The official rule pack"
description = "The official rule pack"
weight = 8
+++

arara ships with a pack of default rules, placed inside a special subdirectory
named `rules/` inside another special directory named `ARARA_HOME` (the place
where our tool is installed). This chapter introduces the official rules,
including proper listings and descriptions of associated parameters whenever
applied. Note that such rules work off the shelf, without any special
installation, configuration or modification. An option marked by **[S]** before
the corresponding identifier indicates a natural boolean switch. Similarly, the
occurrence of an **[R]** mark indicates that the corresponding option is
required.

{% messagebox(title="Can my rule be distributed within the official pack?") %}
As seen in [Concepts](/manual/concepts), the default rule path can be extended
to include a list of directories in which our tool should search for
rules. However, if you believe your rule is comprehensive enough and deserves to
be in the official pack, please contact us! We will be more  than happy to
discuss the inclusion of your rule in forthcoming updates.
{% end %}

# `animate`

This rule creates an animated `gif` file from the corresponding base name of the
`❖ currentFile` reference (i.e, the name without the associated
extension) as a string concatenated with the `pdf` suffix, using the `convert`
command line utility from the ImageMagick suite.

- `delay` (default: `10`): This option regulates the number of ticks before the
  display of the next image sequence, acting as a pause between still frames.

- `loop` (default: `0`): This option regulates the number of repetitions for the
  animation. When set to zero, the animation repeats itself an infinite number
  of times.

- `density` (default: `300`): This option specifies the horizontal and vertical
  canvas resolution while rendering vector formats into a proper raster image.

- `program` (default: `convert`): This option specifies the command utility path
  as a means to avoid potential clashes with underlying operating system
  commands.

{% messagebox(title="Microsoft Windows woes") %}
According to the [ImageMagick
website](http://www.imagemagick.org/Usage/windows/), the Windows installation
routine adds the program directory to the system path, such that one can call
command line tools directly from the command prompt, without providing a path
name. However, `convert` is also the name of Windows system tool, located in the
system directory, which converts file systems from one format to another.

The best solution to avoid possible future name conflicts, according to the
ImageMagick team, is to call such command line tools by their full path in any
script. Therefore, the `convert` rule provides the `program` option for this
specific scenario.
{% end %}

- `options`: This option, as the name indicates, takes a list of raw command
  line options and appends it to the actual system call. An error is thrown if
  any data structure other than a proper list is provided as the value.

```tex
% arara: animate: { delay: 15, density: 150 }
```

# `asymptote`

This rule executes the `asy` command line program, referring to Asymptote, a
powerful descriptive vector graphics language for technical drawings, inspired
by Metapost but with an improved syntax. Please note that you will have to make
the `.asy` extension known to arara in order to compile Asymptote
files. Furthermore, it is advised to use this in your regular TeX document
specifying the `files` parameter to include all graphics you want to compile for
inclusion in your document.

- `color`: This option, as the name suggests, provides the underlying color
  model to be used in the current execution. Possible values are:

  - `bw`: This option value, as the name suggests, converts all colors to a
    black and white model.

  - `cmyk`: This option value converts the RGB (red, green an blue) color model
    to the CMYK (cyan, magenta, yellow and black) counterpart.

  - `rgb`: This option value converts the CMYK (cyan, magenta, yellow and black)
    color model to the RGB (red, green an blue) counterpart.

  - `gray`: This option value, as the name suggests, converts all colors to a
    grayscale model.

- `engine` (default: `latex`): This option, as the name indicates, sets the
  underlying TeX engine to be used for the current compilation. Make sure to
  take a look at the Asymptote manual for further details on this
  option. Possible values are:

  - `latex`: This value, as the name suggests, sets the underlying TeX engine to
    `latex` for the current compilation. Note that the engine might play a major
    role in the generated code.

  - `pdflatex`: This value, as the name indicates, sets the underlying TeX
    engine to `pdflatex` for the current compilation. Note that the engine might
    play a major role in the generated code.

  - `xelatex`: This value, as the name suggests, sets the underlying TeX engine
    to `xelatex` for the current compilation. Note that the engine might play a
    major role in the generated code.

  - `lualatex`: This value, as the name indicates, sets the underlying TeX
    engine to `lualatex` for the current compilation. Note that the engine might
    play a major role in the generated code.

  - `tex`: This value, as the name suggests, sets the underlying TeX engine to
    `tex` for the current compilation. Note that the engine might play a major
    role in the generated code.

  - `pdftex`: This value, as the name indicates, sets the underlying TeX engine
    to `pdftex` for the current compilation. Note that the engine might play a
    major role in the generated code.

  - `luatex`: This value, as the name suggests, sets the underlying TeX engine
    to `luatex` for the current compilation. Note that the engine might play a
    major role in the generated code.

  - `context`: This value, as the name indicates, sets the underlying TeX engine
    to `context` for the current compilation. Note that the engine might play a
    major role in the generated code.

  - `none`: This value, as the name suggests, sets the underlying TeX engine to
    `none` for the current compilation. In this case, there will be no
    associated engine.

- `format`: This option, as the name suggests, converts each output file to a
  specified format. Make sure to take a look at the Asymptote manual for further
  details.

- `output`: This option, as the name suggests, sets an alternative output
  directory or file name. Make sure to take a look at the Asymptote manual for
  further details.

- `options`: This option, as the name indicates, takes a list of raw command
  line options and appends it to the actual system call. An error is thrown if
  any data structure other than a proper list is provided as the value.

```tex
% arara: asymptote: { files: [ mydrawing.asy ] }
```

# `authorindex`

This rule calls the `authorindex` wrapper, a Perl script that processes
auxiliary files generated by the package of the same name, producing author
index files, with the `ain` extension.

- **[S]** `draft`: This option sets whether the script should write additional
  information to the produced file. For each author, the labels of all
  references and the page numbers where they are cited are included as
  comments. This detail may help if you manually edit the generated author
  index.

- **[S]** `index`: This option sets whether the script should create a file
  suitable for further processing with `makeindex` or the like. For example,
  you could use that to make a common author and subject index. Note the
  extension of the generated file still will be the default one.

- **[S]** `keep`: This option sets whether the script should retain the
  temporarily generated `bst` file after the run finishes. This information
  will give you a good starting point for advanced customization of the author
  index.

- **[S]** `print`: This option, as the name indicates, sets whether the script
  should print the result to standard output instead of writing it to the
  output file.

- **[S]** `recurse`: This option sets whether the script should automatically
  process auxiliary files produced by included files. This behaviour is
  enabled by default.

- `options`: This option, as the name indicates, takes a list of raw command
  line options and appends it to the actual system call. An error is thrown if
  any data structure other than a proper list is provided as the value.

```tex
% arara: authorindex: { draft: yes }
```

# `bib2gls`

This rule executes the `bib2gls` command line application which extracts
glossary information stored in a `bib` file and converts it into glossary entry
definitions in resource files. This rule passes the base name of the
`❖ currentFile` reference (i.e, the name without the associated
extension) as the mandatory argument.

- `dir`: This option sets the directory used for writing auxiliary files. Note
  that this option does not change the current working directory.

- `trans`: This option sets the extension of the transcript file created by
  `bib2gls`. The value should be just the file extension without the leading
  dot. The default is `glg`.

- `locale`: This option specifies the preferred language resource file. Please
  keep in mind that the provided value must be a valid IETF language tag. If
  omitted, the default is obtained by `bib2gls` from the JVM.

- **[S]** `group`: This option sets whether `bib2gls` will try to determine the
  letter group for each entry and add it to a new field called `group` when
  sorting. Be mindful that some `sort` options ignore this setting. The
  default value is off.

- **[S]** `interpret`: This option sets whether the interpreter mode of
  `bib2gls` is enabled. If the interpreter is on, `bib2gls` will attempt to
  convert any LaTeX markup in the sort value to the closest matching Unicode
  characters. If the interpreter is off, the `log` file will not be parsed for
  recognised packages. The default value is on.

- **[S]** `breakspace`: This option sets whether the interpreter will treat a
  tilde character as a non-breaking space (as with TeX) or a normal space. The
  default behaviour treats it as non-breakable.

- **[S]** `trimfields`: This option sets whether `bib2gls` will trim leading and
  trailing spaces from field values. The default behaviour does not trim
  spaces.

- **[S]** `recordcount`: This option sets whether the record counting will be
  enabled. If activated, `bib2gls` will add record count fields to
  entries. The default behaviour is off.

- **[S]** `recordcountunit`: This option sets whether `bib2gls` will add unit
  record count fields to entries. These fields can then be used with special
  commands. The default behaviour is off.

- **[S]** `cite`: This option sets whether `bib2gls` will treat citation
  instances found in the `aux` file as though it was actually an ignored
  record. The default behaviour is off.

- **[S]** `verbose`: This option sets whether `bib2gls` will be executed in
  verbose mode. When enabled, the application will write extra information to
  the terminal and transcript file. This option is unrelated to arara's
  verbose mode. The default behaviour is off.

- **[S]** `merge`: This option sets whether the program will merge `wrglossary`
  counter records. If disabled, one may end up with duplicate page numbers in
  the list of entry locations, but linking to different parts of the page. The
  default is on.

- **[S]** `uniscript`: This option sets whether text superscript and subscript
  will use the corresponding Unicode characters if available. The default is
  on.

- `packages`: This option instructs the interpreter to assume the packages from
  the provided list have been used by the document.

- `ignore`: This option instructs `bib2gls` to skip the check for any package
  from the provided list when parsing the corresponding log file.

- `custom`: This option instructs the interpreter to parse the package files
  from the provided list. The package files need to be quite simple.

- `mapformats`: This option takes a list and sets up the rule of precedence for
  partial location matches. Each element from the provided list must be another
  list of exactly two entries representing a conflict resolution.

- `options`: This option, as the name indicates, takes a list of raw command
  line options and appends it to the actual system call. An error is thrown if
  any data structure other than a proper list is provided as the value.

```tex
% arara: bib2gls: { group: true }
% arara: --> if found('aux', 'glsxtr@resource')
```

# `biber`

This rule runs `biber`, the backend bibliography processor for `biblatex`, on
the corresponding base name of the `❖ currentFile` reference (i.e,
the name without the associated extension) as a string.

- **[S]** `tool`:  This option sets whether the bibliography processor should be
  executed in *tool mode*, intended for transformations and modifications of
  datasources. Since this mode is oriented towards a datasource rather than a
  document, make sure to use it alongside the `options` option.

- `options`: This option, as the name indicates, takes a list of raw command
  line options and appends it to the actual system call. An error is thrown if
  any data structure other than a proper list is provided as the value.

```tex
% arara: biber: { options: [ '--wraplines' ] }
```

# `bibtex`

This rule runs the `bibtex` program, a reference management software, on the
corresponding base name of the `❖ currentFile` reference (i.e, the
name without the associated extension) as a string.

- `options`: This option, as the name indicates, takes a list of raw command
  line options and appends it to the actual system call. An error is thrown if
  any data structure other than a proper list is provided as the value.

```tex
% arara: bibtex: { options: [ '-terse' ] }
% arara: --> if exists(toFile('references.bib'))
```

# `bibtex8`

This rule runs `bibtex8`, an enhanced, portable C version of `bibtex`, on the
corresponding base name of the `❖ currentFile` reference (i.e, the
name without the associated extension) as a string. It is important to note that
this tool can read a character set file containing encoding details.

- `options`: This option, as the name indicates, takes a list of raw command
  line options and appends it to the actual system call. An error is thrown if
  any data structure other than a proper list is provided as the value.

```tex
% arara: bibtex8: { options: [ '--trace', '--huge' ] }
```

# `bibtexu`

This rule runs the `bibtexu` program, an enhanced version of `bibtex` with
Unicode support and language features, on the corresponding base name of the
`❖ currentFile` reference (i.e, the name without the associated
extension) as a string.

- `options`: This option, as the name indicates, takes a list of raw command
  line options and appends it to the actual system call. An error is thrown if
  any data structure other than a proper list is provided as the value.

```tex
% arara: bibtexu: { options: [ '--language', 'fr' ] }
```

# `clean`

This rule removes the provided file reference through the underlying system
command, which can be `rm` in a Unix environment or `del` in Microsoft
Windows. As a security lock, this rule will always throw an error if
`❖ currentFile` is equal to `❖ getOriginalFile`, so the
main file reference cannot be removed. It is highly recommended to use the
special `files` parameter to indicate removal candidates. Alternatively, a list
of file extensions can be provided as well. Be mindful that the security lock
also applies to file removals based on extensions.

- `extensions`: This option, as the name indicates, takes a list of extensions
  and constructs a new list of removals commands according to the base name of
  the `❖ currentFile` reference (i.e, the name without the associated
  extension) as a string concatenated with each extension from the original list
  as suffixes. Keep in mind that, if the special `files` parameter is used with
  this option, the resulting list will contain the cartesian product of file
  base names and extensions. An error is thrown if any data structure other than
  a proper list is provided as the value.

{% messagebox(title="Better safe than sorry!") %}
When in doubt, always remember that the `--dry-run` command line option is your
friend! As seen in [Command line](/manual/cli), this option makes arara go
through all the motions of running tasks and subtasks, but with no actual
calls. It is a very useful feature for testing the sequence of removal commands
without actually losing your files! Also, as good practice, always write plain,
simple, understandable `clean` directives and use as many as needed in your TeX
documents.
{% end %}

```tex
% arara: clean: { extensions: [ aux, log ] }
```

# `context`

This rule runs the `context` TeX engine on the provided `❖
currentFile` reference, generating a corresponding file in the Portable Document
Format. Please refer to the user manual for further details.

- **[S]** `make`: This option, as the name indicates, sets whether the engine
  should create context formats. Please refer to the user manual for further
  details on this option.

- `ctx`: This option, as the name indicates, sets the `ctx` file for process
  management specification. Please refer to the user manual for further details
  on this option.

- `interface`: This option, as the name indicates, sets the specified user
  interface. Please refer to the user manual for further details on this
  option.

- **[S]** `autopdf`: This option, as the name indicates, closes the
  corresponding `pdf` file in viewer and then reopens it afterwards.

- `purge`: This option, as the name indicates, purges files according to the
  provided rule. Possible values are:

  - `partial`: This value, as the name suggests, purges files either or not
    after a proper run, defined through a pattern. Please refer to the user
    manual for further details.

  - `all`: This value, as the name suggests, purges all files either or not
    after a proper run, defined through a pattern. Please refer to the user
    manual for further details.

  - `result`: This value, as the name suggests, purges the resulting file before
    the actual run. Please refer to the user manual for further details.

- `modules`: This option, as the name indicates, sets a list of modules and
  styles to be loaded, normally part of the distribution.

- `environments`: This option, as the name indicates, sets a list of environment
  files (document styles) to load first.

- `mode`: This option, as the name indicates, enables modes according to the
  provided list (conditional processing in styles).

- `path`: This option, as the name indicates, consults the given paths in the
  provided list during a file lookup.

- `arguments`: This option, as the name indicates, sets variables that can be
  consulted during a run. Such variables are defined as key/value pairs.

- `trackers`:  This option, as the name indicates, sets the list of tracker
  variables. Please refer to the user manual for further details.

- `directives`:  This option, as the name indicates, sets the list of directive
  variables. Please refer to the user manual for further details.

- `silent`: This option, as the name indicates, disables the log categories
  based on the provided list. Please refer to the user manual for further
  details.

- `errors`:  This option, as the name indicates, shows errors at the end of a
  run and quits when it the provided list. Please refer to the user manual for
  further details.

- **[S]** `synctex`: This option sets whether `synctex`, an input and output
  synchronization feature that allows navigation from source to typeset
  material and vice versa, available in most TeX engines, is activated.

- `interaction`: This option alters the underlying engine behaviour. If this
  option is omitted, the engine will prompt the user for interaction in the
  event of an error. Possible values are, in order of increasing user
  interaction (courtesy of our master Enrico Gregorio):

  - `batchmode`: In this mode, nothing is printed on the terminal, and errors
    are scrolled as if the `return` key is hit at every error. Missing files
    that TeX tries to input or request from keyboard input cause the job to
    abort.

  - `nonstopmode`: In this mode, the diagnostic message will appear on the
    terminal, but there is no possibility of user interaction just like in batch
    mode, previously described.

- `options`: This option, as the name indicates, takes a list of raw command
  line options and appends it to the actual system call. An error is thrown if
  any data structure other than a proper list is provided as the value.

```tex
% arara: context
```

# `convert`

This rule runs the `convert` program, a member of the ImageMagick suite of
tools. This program is used to convert between image formats as well  as  resize
an  image, blur, crop, despeckle, dither, draw on, flip, join, resample, and
more.

- `program` (default: `convert`): This option specifies the command utility path
  as a means to avoid potential clashes with underlying operating system
  commands.

- **[R]** `options`: This option, as the name indicates, takes a list of raw
  command line options and appends it to the actual script call. An error is
  thrown if any data structure other than a proper list is provided as the
  value.

```tex
% arara: convert: { options: [ 'photo1.jpg', '--resize',
% arara: --> '50%', 'photo2.jpg' ] }
```

# `copy`

This rule copies the `File` reference to the provided target using the
underlying operating system copy operation. The target is *always*
overwritten. However, be mindful that an error will be thrown if you try to
overwrite the file referenced in `❖ getOriginalReference`.

- **[R]** `target`: This option, as the name implies, specifies the target for
  the copy operation. Keep in mind that this option is required.

```tex
% arara: copy: { target: 'backup/thesis.tex' }
```

# `csplain`

This rule runs the `csplain` TeX engine, a conservative extension of Knuth's
plain TeX with direct processing characters and hyphenation patterns for Czech
and Slovak, on the provided `❖ currentFile` reference.

- `interaction`: This option alters the underlying engine behaviour. When such
  option is omitted, TeX will prompt the user for interaction in the event of an
  error. Possible values are, in order of increasing user interaction (courtesy
  of our master Enrico Gregorio):

  - `batchmode`: In this mode, nothing is printed on the terminal, and errors
    are scrolled as if the `return` key is hit at every error. Missing files
    that TeX tries to input or request from keyboard input cause the job to
    abort.

  - `nonstopmode`: In this mode, the diagnostic message will appear on the
    terminal, but there is no possibility of user interaction just like in batch
    mode, previously described.

  - `scrollmode`: In this mode, as the name indicates, TeX will stop only for
    missing files to input or if proper keyboard input is necessary. TeX fixes
    errors itself.

  - `errorstopmode`: In this mode, TeX will stop at each error, asking for
    proper user intervention. This is the most user interactive mode available.

- **[S]** `shell`: This option sets whether the possibility of running
  underlying system commands from within TeX is activated.

- **[S]** `synctex`: This option sets whether `synctex`, an input and output
  synchronization feature that allows navigation from source to typeset
  material and vice versa, available in most TeX engines, is activated.

- **[S]** `draft`: This option sets whether the draft mode, i.e, a mode that
  produces no output, so the engine can check the syntax, is activated.

- `options`: This option, as the name indicates, takes a list of raw command
  line options and appends it to the actual system call. An error is thrown if
  any data structure other than a proper list is provided as the value.

```tex
% arara: csplain: { interaction: batchmode, shell: yes }
```

# `datatooltk`

This rule runs `datatooltk`, an application that creates `datatool` databases in
raw format from several structured data formats, in batch mode. This rule
requires `output` and one of the import options.

- **[R]** `output`: This option provides the database name to be saved as
  output. To guard against accidentally overwriting a document file,
  `datatooltk` now forbids the `tex` extension for output files. This option
  is required.

- `csv`: This option, as the name indicates, imports data from the `csv` file
  reference provided as a plain string value.

- `sep`: This option specifies the character used to separate values in the
  `csv` file. Defaults to a comma.

- `delim`: This option specifies the character used to delimit values in the
  `csv` file. Defaults to a double quote.

- `name`: This option, as the name indicates, sets the label reference of the
  newly created database according to the provided value.

- `sql`: This option imports data from an SQL database where the provided value
  refers to a proper `select` SQL statement.

- `sqldb`: This option, as the name indicates, sets the name of the SQL database
  according to the provided value.

- `sqluser`: This option, as the name indicates, sets the name of the SQL user
  according to the provided value.

- `noconsole` (default: `gui`): This action dictates the password request action
  if such information was not provided earlier. If there is no console
  available, the action is determined by the following values:

  - `error`: As the name indicates, this action issues an error when no password
    was previously provided through the proper option.

  - `stdin`: This action requests the password via the standard input stream,
    which is less secure than using a console.

  - `gui`: This action displays a dialog box in which the user can enter the
    password for the SQL database.

- `probsoln`: This option, as the name indicates, imports data in the `probsoln`
  format from the file name provided as the value.

- `input`: This option, as the name indicates, imports data in the `datatool`
  format from the file name provided as the value.

- `sort`: This option, as the name indicates, sorts the database according to
  the column whose label is provided as the value. The value may be preceded by
  `+` or `-` to indicate ascending or descending order, respectively. If the
  sign is omitted, ascending is assumed.

- `sortlocale`: This option, as the name indicates, sorts the database according
  to alphabetical order rules of the locale provided as the value. If the value
  is set to `none` strings are sorted according to non-locale letter order.

- **[S]** `sortcase`: This option sets whether strings will be sorted using
  case-sensitive comparison for non-locale letter ordering. The default
  behaviour is case-insensitive.

- `seed`: This option, as the name indicates, sets the random generator seed to
  the provided value. The seed is cleared if an empty value is provided.

- **[S]** `shuffle`: This option sets whether the database will be properly
  shuffled. Shuffle is always performed after sort, regardless of the option
  order.

- **[S]** `csvheader`: This option sets whether the `csv` file has a header
  row. The spreadsheet import functions also use this setting.

- **[S]** `debug`: This option, as the name indicates, sets whether the debug
  mode of `datatooltk` is activated. The debug mode is disabled by default.

- **[S]** `owneronly`: This option sets whether read and write permissions when
  saving `dbtex` files should be defined for the owner only. This option has
  no effect on some operating systems.

- **[S]** `maptex`: This option sets whether TeX special characters will be
  properly mapped when importing data from `csv` files or SQL databases.

- `xls`: This option, as the name indicates, imports data from a Microsoft Excel
  `xls` file reference provided as a plain string value.

- `ods`: This option, as the name indicates, imports data from an Open Document
  Spreadsheet `ods` file reference provided as a plain string value.

- `sheet`: This option specifies the sheet to select from the Excel workbook or
  Open Document Spreadsheet. This may either be an index or the name of the
  sheet.

- `filterop`: This option specifies the logical operator to be associated with a
  given filter. Filtering is always performed after sorting and
  shuffling. Possible values are:

  - `or`: This value, as the name indicates, uses the logical `or` operator when
    filtering. This is the default behaviour. Note that this value has no effect
    if only one filter is supplied.

  - `and`: This value, as the name indicates, uses the logical `and` operator
    when filtering. Note that this value has no effect if only one filter is
    supplied.

- `filters`: This option takes a list and sets up a sequence of filters. Each
  element from the provided list must be another list of exactly three entries
  representing a key, an operator and a value, respectively.

- `truncate`: This option truncates the database to the number of rows provided
  as the value. Truncation is always performed after any sorting, shuffling and
  filtering, but before column removal.

```tex
% arara: datatooltk: {
% arara: --> output: books.dbtex,
% arara: --> csv: booklist.csv }
```

# `detex`

This rule runs `detex`, a command line application that, as the name indicates,
reads the provided `❖ currentFile` reference, removes all comments
and TeX control sequences and writes the remainder to the standard output or
file.

- **[S]** `references`: This option defines whether the tool should echo values
  from page counters, references and citations.

- **[S]** `follow`: This option defines whether the tool should follow files
  referenced through typical input mechanisms.

- **[S]** `math`: This option defines whether the tool should replace math with
  nouns and verbs in order to preserve grammar.

- **[S]** `spaces`: This option, as the name indicates, define whether the tool
  should replace control sequences with spaces.

- **[S]** `words`: This option, as the name indicates, define whether the tool
  should output only a list of words.

- `environments`: This option takes a list of environments to be ignored by the
  tool during the text transformation.

- `mode`: This option, as the name indicates, defines the operation mode for the
  tool. Possible values are:

  - `latex`: This value, as the name suggests, enables and forces the LaTeX mode
    for the tool.

  - `tex`: This value, as the name suggests, enables and forces the plain TeX
    mode for the tool.

- `output`: This value, as the name indicates, sets the output file in which the
  contents will be redirected.

```tex
% arara: detex: { output: thesis.txt }
```

# `dvipdfm`

This rule runs `dvipdfm`, a command line utility for file format translation, on
the corresponding base name of the `❖ currentFile` reference (i.e,
the name without the associated extension) as a string concatenated with the
`dvi` suffix, generating a Portable Document Format `pdf` file.

- `output`: This option, as the name indicates, sets the output name for the
  generated `pdf` file. There is no need to provide an extension, as the value
  is always normalized with `❖ getBasename` such that only the name
  without the associated extension is used. The base name of the current file
  reference is used as the default value.

- `options`: This option, as the name indicates, takes a list of raw command
  line options and appends it to the actual system call. An error is thrown if
  any data structure other than a proper list is provided as the value.

```tex
% arara: dvipdfm: { output: thesis }
```

# `dvipdfmx`

This rule runs `dvipdfmx`, an extended version of `dvipdfm` created to support
multibyte character encodings and large character sets for East Asian languages,
on the corresponding base name of the `❖ currentFile` reference (i.e,
the name without the associated extension) as a string concatenated with the
`dvi` suffix, generating a Portable Document Format `pdf` file.

- `output`: This option, as the name indicates, sets the output name for the
  generated `pdf` file. There is no need to provide an extension, as the value
  is always normalized with `❖ getBasename` such that only the name
  without the associated extension is used. The base name of the current file
  reference is used as the default value.

- `options`: This option, as the name indicates, takes a list of raw command
  line options and appends it to the actual system call. An error is thrown if
  any data structure other than a proper list is provided as the value.

```tex
% arara: dvipdfmx: { options: [ '-K', '40' ] }
```

# `dvips`

This rule runs `dvips` on the corresponding base name of the `❖
currentFile` reference (i.e, the name without the associated extension) as a
string concatenated with the `dvi` suffix, generating a PostScript `ps` file.

- `output`: This option, as the name indicates, sets the output name for the
  generated `ps` file. There is no need to provide an extension, as the value is
  always normalized with `❖ getBasename` such that only the name
  without the associated extension is used. The base name of the current file
  reference is used as the default value.

- `options`: This option, as the name indicates, takes a list of raw command
  line options and appends it to the actual system call. An error is thrown if
  any data structure other than a proper list is provided as the value.

```tex
% arara: dvips: { output: thesis }
```

# `dvipspdf`

This rule runs `dvips` in order to obtain a corresponding `ps` file from the
initial `dvi` reference, and then runs `ps2pdf` on the previously generated `ps`
file in order to obtain a `pdf` file. Note that all base names are acquired from
the `❖ currentFile` reference (i.e, the name without the associated
extension) and used to construct the resulting files.

- `output`: This option, as the name indicates, sets the output name for the
  generated `pdf` file. There is no need to provide an extension, as the value
  is always normalized with `❖ getBasename` such that only the name
  without the associated extension is used. The base name of the current file
  reference is used as the default value.

- `options1`: This option, as the name indicates, takes a list of raw command
  line options and appends it to the `dvips` program call. An error is thrown if
  any data structure other than a proper list is provided as the value.

- `options2`: This option, as the name indicates, takes a list of raw command
  line options and appends it to the `ps2pdf` program call. An error is thrown
  if any data structure other than a proper list is provided as the value.

```tex
% arara: dvipspdf: { output: article }
```

# `dvisvgm`

This rule runs `dvisvgm` in order to obtain a corresponding `svg` file, a vector
graphics format based on XML, from the initial reference. It is important to
observe that the base name is acquired from the `❖ currentFile`
reference (i.e, the name without the associated extension) and used to construct
the resulting file.

- `entry`: This option sets the extension to be used for the initial reference
  (i.e, the current file name) as input to the command line tool. The following
  values are available for this option:

  - `dvi`: This value sets the extension to be used for the initial reference as
    a device independent format. This is the default value when no value is
    provided.

  - `xdv`: This value sets the extension to be used for the initial reference as
    an extended device independent format.

  - `eps`: This value sets the extension to be used for the initial reference as
    an encapsulated PostScript graphics format.

  - `pdf`: This value sets the extension to be used for the initial reference as
    a Portable Document Format.

- `pages`: This value, as the name implies, takes a list of integers indicating
  the pages to be processed by the command line tool.

- `options`: This option, as the name indicates, takes a list of raw command
  line options and appends it to the actual system call. An error is thrown if
  any data structure other than a proper list is provided as the value.

```tex
% arara: dvisvgm
```

# `etex`

This rule runs the `etex` extended (plain) TeX engine on the provided
`❖ currentFile` reference, generating a corresponding file in a
device independent format.

- `interaction`: This option alters the underlying engine behaviour. If this
  option is omitted, TeX will prompt the user for interaction in the event of an
  error. Possible values are, in order of increasing user interaction (courtesy
  of our master Enrico Gregorio):

  - `batchmode`: In this mode, nothing is printed on the terminal, and errors
    are scrolled as if the `return` key is hit at every error. Missing files
    that TeX tries to input or request from keyboard input cause the job to
    abort.

  - `nonstopmode`: In this mode, the diagnostic message will appear on the
    terminal, but there is no possibility of user interaction just like in batch
    mode, previously described.

  - `scrollmode`: In this mode, as the name indicates, TeX will stop only for
    missing files to input or if proper keyboard input is necessary. TeX fixes
    errors itself.

  - `errorstopmode`: In this mode, TeX will stop at each error, asking for
    proper user intervention. This is the most user interactive mode available.

- **[S]** `shell`: This option sets whether the possibility of running
  underlying system commands from within TeX is activated.

- `options`: This option, as the name indicates, takes a list of raw command
  line options and appends it to the actual system call. An error is thrown if
  any data structure other than a proper list is provided as the value.

```tex
% arara: etex: {
% arara: --> shell: yes,
% arara: --> interaction: batchmode
% arara: --> }
```

# `fig2dev`

This rule runs `fig2dev`, a command line application that translates `fig` code
in the corresponding base name of the `❖ currentFile` reference (i.e,
the name without the associated extension) as a string concatenated with the
`fig` suffix into the specified graphics language and puts them in the specified
output file.

- **[R]** `language`: This option, as the name indicates, sets the output
  graphics language for proper transformation. Possible values are listed as
  follows (you can infer their types as well, based on the values): `box`,
  `cgm`, `epic`, `eepic`, `eepicemu`, `emf`, `eps`, `gif`, `ibmgl`, `jpeg`,
  `latex`, `map`, `mf`, `mp`, `mmp`, `pcx`, `pdf`, `pdftex`, `pdftex_t`,
  `pic`, `pictex`, `png`, `ppm`, `ps`, `pstex`, `pstex_t`, `ptk`, `shape`,
  `sld`, `svg`, `textyl`, `tiff`, `tk`, `tpic`, `xbm`, and `xpm`.

- **[R]** `output`: This option, as the name indicates, sets the output file
  which will contain the translated code based on the provided file and
  language.

- `magnification`: This option, as the name suggests, sets the magnification
  level at which the figure is rendered.

- `font`: This option sets the default font used for text objects to the
  provided value. Keep in mind that the format of this option depends on the
  graphics language in use.

- `size`: This option, as the name suggests, set the default font size (in
  points) for text objects to the provided value.

- `options`: This option, as the name indicates, takes a list of raw command
  line options and appends it to the actual system call. An error is thrown if
  any data structure other than a proper list is provided as the value.

```tex
% arara: fig2dev: { language: mp, output: drawing.mp }
```

# `frontespizio`

This rule automates the steps required by the `frontespizio` package in order to
help Italian users generate the frontispiece to their thesis. First and
foremost, the frontispiece is generated. If `latex` is used as the underlying
engine, there is an additional intermediate conversion step to a proper `eps`
file. Finally, the final document is compiled.

- `engine` (default: `pdflatex`): This option, as the name indicates, sets the
  underlying TeX engine to be used for both compilations (the frontispiece and
  the document itself). Possible values are:

  - `latex`: This value, as the name indicates, sets the underlying TeX engine
    to `latex` for both compilations (frontispiece and document).

  - `pdflatex`: This value, as the name indicates, sets the underlying TeX
    engine to `pdflatex` for both compilations (frontispiece and document).

  - `xelatex`: This value, as the name indicates, sets the underlying TeX engine
    to `xelatex` for both compilations (frontispiece and document).

  - `lualatex`: This value, as the name indicates, sets the underlying TeX
    engine to `lualatex` for both compilations (frontispiece and document).

- **[S]** `shell`: This option sets whether the possibility of running
  underlying system commands from within the selected TeX engine is activated.

- `interaction`: This option alters the underlying engine behaviour. If this
  option is omitted, TeX will prompt the user for interaction in the event of an
  error. Possible values are, in order of increasing user interaction (courtesy
  of our master Enrico Gregorio):

  - `batchmode`: In this mode, nothing is printed on the terminal, and errors
    are scrolled as if the `return` key is hit at every error. Missing files
    that TeX tries to input or request from keyboard input cause the job to
    abort.

  - `nonstopmode`: In this mode, the diagnostic message will appear on the
    terminal, but there is no possibility of user interaction just like in batch
    mode, previously described.

  - `scrollmode`: In this mode, as the name indicates, TeX will stop only for
    missing files to input or if proper keyboard input is necessary. TeX fixes
    errors itself.

  - `errorstopmode`: In this mode, TeX will stop at each error, asking for
    proper user intervention. This is the most user interactive mode available.

- `options`: This option, as the name indicates, takes a list of raw command
  line options and appends it to the actual TeX engine call. An error is thrown
  if any data structure other than a proper list is provided as the value.

```tex
% arara: frontespizio: { engine: xelatex,
% arara: --> shell: yes, interaction: nonstopmode }
```

# `ghostscript`

This rule runs `ghostscript`, an interpreter for PostScript and Portable
Document Format files, according to the provided parameters.

- `program` (default: `gs`): This option specifies the command utility path as a
  means to avoid potential clashes with underlying operating system commands or
  specific Windows naming schemes.

- **[R]** `options`: This option, as the name indicates, takes a list of raw
  command line options and appends it to the actual `ghostscript` call. An
  error is thrown if any data structure other than a proper list is provided
  as the value. This option is required.

- `device`: This option specifies which output device the tool should use. If
  this option is not given, the default device (usually a display device) is
  used.

- `output`: This option, as the name indicates, specifies a file in which the
  tool should send the output. Please refer to the documentation for more
  details.

```tex
% arara: ghostscript: { options: [ '-dCompatibilityLevel=1.4',
% arara: --> '-dPDFSETTINGS=/printer', '-dNOPAUSE', '-dQUIET',
% arara: --> '-dBATCH', 'input.pdf' ],
% arara: --> output: output.pdf,
% arara: --> device: pdfwrite }
```

# `gnuplot`

This rule runs `gnuplot`, a command-driven plotting program that can generate
plots of functions, data and data fits. The program also provides scripting
capabilities, looping, functions, text processing, variables, macros, arbitrary
pre-processing of input data (usually across columns), as well as the ability to
perform non-linear multi-dimensional multi-set weighted data fitting.

- **[S]** `persist`: This option, as the name implies, sets whether the program
  should let plot windows survive after the main execution exits.

- **[S]** `default`: this option, as the name suggests, sets whether the program
  should read the default settings from either `gnuplotrc` or `~/.gnuplot` on
  entry.

- `commands`: This option, as the name implies, executes the requested commands
  before loading the next input file. Please refer to the user manual for
  further details.

- **[R]** `input`: This required option, as the name indicates, sets the list of
  input file names to be processed by the program. An error is thrown if any
  data structure other than a proper list is provided as the value.

```tex
% arara: gnuplot: { input: [ myplot.gnuplot ], default: yes }
```

# `halt`

This rule, as the name suggests, sets a `halt` flag, which stops the current
interpretation workflow, such that subsequent directives are ignored. This rule
contains no associated options. Please refer to [Methods](/manual/methods) for
more information on flags.

```tex
% arara: halt
```

# `indent`

This rule runs `latexindent`, a Perl script that indents TeX files according to
an indentation scheme, on the provided `❖ currentFile`
reference. Environments, including those with alignment delimiters, and
commands, including those that can split braces and brackets across lines, are
usually handled correctly by the script.

- **[S]** `silent`: This option, as the name indicates, sets whether the script
  will operate in silent mode, in which no output is given to the terminal.

- **[S]** `overwrite`: This option, as the name indicates, sets whether the
  `❖ currentFile` reference will be overwritten. If activated, a
  copy will be made before the actual indentation process.

- `trace`: This option, as the name indicates, enables the script tracing mode,
  such that a verbose output will be given to the `indent.log` log
  file. Possible values are:

  - `default`: This value, as the name indicates, refers to the default tracing
    level. Note that, especially for large files, this value does affect
    performance of the script.

  - `complete`: This value, as the name indicates, refers to the detailed,
    complete tracing level. Note that, especially for large files, performance
    of the script will be significantly affected when this value is used.

- **[S]** `screenlog`: This option, as the name indicates, sets whether
  `latexindent` will output the log file to the screen, as well as to the
  specified log file.

- **[S]** `modifylinebreaks`: This option, as the name indicates, sets whether
  the script will modify line breaks, according to specifications written in a
  configuration file.

- `cruft`: This option sets the provided value as a cruft location in which the
  script will write backup and log files. The default behaviour sets the working
  directory as cruft location.

- `logfile`: This option, as the name indicates, sets the name of the log file
  generated by `latexindent` according to the provided value.

- `output`: This option, as the name indicates, sets the name of the output
  file. Please note that this option has higher priority over some switches, so
  options like `overwrite` will be ignored by the underlying script.

- `options`: This option, as the name indicates, takes a list of raw command
  line options and appends it to the actual script call. An error is thrown if
  any data structure other than a proper list is provided as the value.

- `settings`: This option, as the name indicates, dictates the indentation
  settings to be applied in the current script execution. Two possible values
  are available:

  - `local`: This value, as the name implies, acts a switch to indicate a local
    configuration. In this scenario, the script will look for a proper settings
    file in the same directory as the `❖ currentFile` reference and
    add the corresponding content to the indentation scheme. Optionally, a file
    location can be specified as well. Please refer to the `where` option for
    more details on such feature.

  - `onlydefault`: This value, as the name indicates, ignores any local
    configuration, so the script will resort to the default indentation
    behaviour.

- `where`: This option, as the name indicates, sets the file location containing
  the indentation settings according to the provided value. This option can only
  be used if, and only if, `local` is set as the value for the `settings`
  option, otherwise the rule will throw an error.

- `replacement`: This option, as the name indicates, implements the replacement
  mode switches. Three possible values are available:

  - `full`: This value, as the name indicates, performs indentation and
    replacements, not respecting verbatim code blocks.

  - `noverb`: This value, as the name indicates, performs indentation and
    replacements, and will respect verbatim code blocks.

  - `noindent`: This value, as the name implies, will not perform indentation,
    and will perform replacements not respecting verbatim code blocks.

- `check`: This option, as the name indicates, checks whether the text after
  indentation matches that given in the original file. Two possible values are
  available:

  - `standard`: This value, as the name indicates, refers to the standard
    behaviour. Please refer to the documentation for further details.

  - `verbose`: This value, as the name indicates, refers to including more
    details to the check. The tool will print the result in the standard output,
    as well as in its own log file. Please refer to the documentation for
    further details.

- `lines`: This option instructs the tool to operate only on specific line
  ranges within the file being inspected. Please refer to the documentation for
  further details.

```tex
% arara: indent: { overwrite: yes }
```

# `knitr`

This rule calls the `knitr` package, a transparent engine for dynamic report
generation with R. It takes an `.Rnw` file as input, extracts the R code in it
according to a list of patterns, evaluates the code and writes the output in
another file. It can also tangle R source code from the input document.

- `output` (default: `NULL`): This option sets the output file. when absent,
  `knitr` will try to guess a default, which will be under the current working
  directory.

- **[S]** `tangle`: This option sets whether to tangle the R code from the input
  file. Note that, when used, this option requires `output` to be specified as
  well, otherwise an error is thrown.

- **[S]** `quiet`: This option, as the name indicates, sets whether the tool
  should suppress both progress bar and messages.

- `envir` (default: `parent.frame()`): This option sets the environment in which
  code chunks are to be evaluated. Please refer to the documentation for further
  details.

- `encoding` (default: `getOption("encoding")`): This option, as the name
  indicates, sets the encoding of the input file. Please refer to the
  documentation for further details.

```tex
% arara: knitr: { quiet: yes }
```

# `latex`

This rule runs the `latex` TeX engine on the provided `❖ currentFile`
reference, generating a corresponding file in a device independent format.

- `branch` (default: `stable`): This option allows branching formats for the
  current engine, mainly focused on package development. Users of current TeX
  distributions might benefit from format branching in order to easily test
  documents and code against the upcoming releases. Possible values are:

  - `stable`: This value, as the name implies, enables the stable engine format
    branch. Note that this is the default format.

  - `developer`: For experienced users, this value enables the experimental,
    developer engine format branch.

- `interaction`: This option alters the underlying engine behaviour. If this
  option is omitted, TeX will prompt the user for interaction in the event of an
  error. Possible values are, in order of increasing user interaction (courtesy
  of our master Enrico Gregorio):

  - `batchmode`: In this mode, nothing is printed on the terminal, and errors
    are scrolled as if the `return` key is hit at every error. Missing files
    that TeX tries to input or request from keyboard input cause the job to
    abort.

  - `nonstopmode`: In this mode, the diagnostic message will appear on the
    terminal, but there is no possibility of user interaction just like in batch
    mode, previously described.

  - `scrollmode`: In this mode, as the name indicates, TeX will stop only for
    missing files to input or if proper keyboard input is necessary. TeX fixes
    errors itself.

  - `errorstopmode`: In this mode, TeX will stop at each error, asking for
    proper user intervention. This is the most user interactive mode available.

- **[S]** `shell`: This option sets whether the possibility of running
  underlying system commands from within TeX is activated.

- **[S]** `synctex`: This option sets whether `synctex`, an input and output
  synchronization feature that allows navigation from source to typeset
  material and vice versa, available in most TeX engines, is activated.

- **[S]** `draft`: This option sets whether the draft mode, i.e, a mode that
  produces no output, so the engine can check the syntax, is activated.

- `options`: This option, as the name indicates, takes a list of raw command
  line options and appends it to the actual system call. An error is thrown if
  any data structure other than a proper list is provided as the value.

```tex
% arara: latex: { interaction: scrollmode, draft: yes }
```

# `latexmk`

This rule runs `latexmk`, a fantastic command line tool for fully automated TeX
document generation, on the provided `❖ currentFile` reference.

- `clean`: This option, as the name indicates, removes all temporary files
  generated after a sequence of intermediate calls for document generation. Two
  possible values are available:

  - `all`: This value, as the name indicates, removes all temporary,
    intermediate files, as well as resulting, final formats such as PostScript
    and Portable Document Format. Only relevant source files are kept.

  - `partial`: This value, as the name indicates, removes all temporary,
    intermediate files and keeps the resulting, final formats such as PostScript
    and Portable Document Format.

- `engine`: This option, as the name indicates, sets the underlying TeX engine
  of `latexmk` to be used for the compilation sequence. Possible values are:

  - `latex`: This value, as the name indicates, sets the underlying TeX engine
    of the script to `latex` for the compilation sequence.

  - `latex-dev`: This value, as the name indicates, sets the underlying TeX
    engine of the script to `latex-dev` (the development branch) for the
    compilation sequence.

  - `pdflatex`: This value, as the name indicates, sets the underlying TeX
    engine of the script to `pdflatex` for the compilation sequence.

  - `pdflatex-dev`: This value, as the name indicates, sets the underlying TeX
    engine of the script to `pdflatex-dev` (the development branch) for the
    compilation sequence.

  - `xelatex`: This value, as the name indicates, sets the underlying TeX engine
    of the script to `xelatex` for the compilation sequence.

  - `xelatex-dev`: This value, as the name indicates, sets the underlying TeX
    engine of the script to `xelatex-dev` (the development branch) for the
    compilation sequence.

  - `lualatex`: This value, as the name indicates, sets the underlying TeX
    engine of the script to `lualatex` for the compilation sequence.

  - `lualatex-dev`: This value, as the name indicates, sets the underlying TeX
    engine of the script to `lualatex-dev` (the development branch) for the
    compilation sequence.

- `program`: This option, as the name suggests, sets the TeX engine according to
  the provided value. It is important to note that this option has higher
  priority over `engine` values, so the latter will be discarded.

- `options`: This option, as the name indicates, takes a list of raw command
  line options and appends it to the actual script call. An error is thrown if
  any data structure other than a proper list is provided as the value.

```tex
% arara: latexmk: { engine: pdflatex }
```

# `llmk`

This rule runs `llmk`, a command line tool specific for building LaTeX
documents. The tool's aim is to provide a simple way to specify a workflow of
processing documents and encourage people to always explicitly show the right
workflow for each document.

- `clean`: This option, as the name indicates, removes all temporary files
  generated after a sequence of intermediate calls for document generation. Two
  possible values are available:

  - `all`: This value, as the name indicates, removes all temporary,
    intermediate files, as well as resulting, final formats such as PostScript
    and Portable Document Format. Only relevant source files are kept.

  - `partial`: This value, as the name indicates, removes all temporary,
    intermediate files and keeps the resulting, final formats such as PostScript
    and Portable Document Format.

- `debug`: This option activates the specified debug category, so debugging
  messages related to the activated category will be shown. Please refer to the
  documentation for more details.

- **[S]** `dry`: This option sets whether the tool should display a list of
  commands to be executed without actually invoking them.

- `mode`: This option sets the verbosity level of messages to be displayed
  during a run. Three possible values are available:

  - `quiet`: This value, as the name indicates, suppresses most of the messages
    from the program during execution.

  - `silent`: This value, as the name indicates, silences messages from invoked
    programs by redirecting both standard output and standard error streams to
    the null device.

  - `verbose`: This value, as the name indicates, displays additional
    information such as invoked commands with options and arguments by the
    program.

```tex
% arara: llmk: { mode: verbose }
```

# `ltx2any`

This rule runs `ltx2any`, a command line tool written in Ruby that acts as a
LaTeX build wrapper, on the provided `❖ currentFile` reference.

- **[S]** `clean`: This option, as the name indicates, sets whether all
  intermediate results generated during the compilation to be deleted.

- `engine`: This option, as the name indicates, sets the engine to be using
  during the current execution.

- `parameters`: This option, as the name indicates, takes a list of parameters
  to be passed to the engine. An error is thrown if any data structure other
  than a proper list is provided as the value.

- `tikzimages`: This option takes a list of externalised Ti*k*Z images to
  rebuild. An error is thrown if any data structure other than a proper list is
  provided as the value.

- `jobname`: This option, as the name indicates, sets the job name to be used in
  the resulting file.

- `logname`: This option, as the name indicates, sets the log file name to be
  used during the current execution.

- `logformat`: This option, as the name indicates, sets the log format to be
  used during the current execution. Three possible values are available:

  - `raw`: This value, as the name indicates, sets the log format to be raw,
    i.e, as generated by the underlying engines.

  - `markdown`: This value, as the name indicates, sets the log format to be
    displayed in Markdown.

  - `pdf`: This value, as the name indicates, sets the log format to be
    displayed in the Portable Document Format.

- `loglevel`: This option, as the name indicates, sets the log level to be used
  during the current execution. Three possible values are available:

  - `error`: This value, as the name indicates, sets the base log level to
    report errors only. No other information is appended.

  - `warning`: This value, as the name indicates, sets the base log level to
    report warnings and errors. No other information is appended.

  - `info`: This value, as the name indicates, set the base log level to report
    all information available, regardless of message categories.

- `frequency`: This option, as the name indicates, sets how often the engine
  runs. Values smaller than one will cause it to run until the resulting file no
  longer changes.

- `directory`: This option, as the name indicates, sets the directory to hold
  intermediate files during the compilation.

- **[S]** `synctex`: This option sets whether `synctex`, an input and output
  synchronization feature that allows navigation from source to typeset
  material and vice versa, available in most TeX engines, is activated.

- `options`: This option, as the name indicates, takes a list of raw command
  line options and appends it to the actual system call. An error is thrown if
  any data structure other than a proper list is provided as the value.

```tex
% arara: ltx2any: { synctex: yes }
```

# `luahbtex`

This rule runs the `luahbtex` TeX engine on the provided `❖
currentFile` reference, generating a corresponding file in the Portable Document
Format, as expected.

- `interaction`: This option alters the underlying engine behaviour. If this
  option is omitted, TeX will prompt the user for interaction in the event of an
  error. Possible values are, in order of increasing user interaction (courtesy
  of our master Enrico Gregorio):

  - `batchmode`: In this mode, nothing is printed on the terminal, and errors
    are scrolled as if the `return` key is hit at every error. Missing files
    that TeX tries to input or request from keyboard input cause the job to
    abort.

  - `nonstopmode`: In this mode, the diagnostic message will appear on the
    terminal, but there is no possibility of user interaction just like in batch
    mode, previously described.

  - `scrollmode`: In this mode, as the name indicates, TeX will stop only for
    missing files to input or if proper keyboard input is necessary. TeX fixes
    errors itself.

  - `errorstopmode`: In this mode, TeX will stop at each error, asking for
    proper user intervention. This is the most user interactive mode available.

- **[S]** `shell`: This option sets whether the possibility of running
  underlying system commands from within TeX is activated.

- **[S]** `synctex`: This option sets whether `synctex`, an input and output
  synchronization feature that allows navigation from source to typeset
  material and vice versa, available in most TeX engines, is activated.

- **[S]** `draft`: This option sets whether the draft mode, i.e, a mode that
  produces no output, so the engine can check the syntax, is activated.

- `options`: This option, as the name indicates, takes a list of raw command
  line options and appends it to the actual system call. An error is thrown if
  any data structure other than a proper list is provided as the value.

```tex
% arara: luahbtex: { interaction: batchmode,
% arara: --> shell: yes, draft: yes }
```

# `lualatex`

This rule runs the new `lualatex` TeX engine on the provided `❖
currentFile` reference, generating a corresponding file in the Portable Document
Format, as expected.

- `branch` (default: `stable`): This option allows branching formats for the
  current engine, mainly focused on package development. Users of current TeX
  distributions might benefit from format branching in order to easily test
  documents and code against the upcoming releases. Possible values are:

  - `stable`: This value, as the name implies, enables the stable engine format
    branch. Note that this is the default format.

  - `developer`: For experienced users, this value enables the experimental,
    developer engine format branch.

- `interaction`: This option alters the underlying engine behaviour. If this
  option is omitted, TeX will prompt the user for interaction in the event of an
  error. Possible values are, in order of increasing user interaction (courtesy
  of our master Enrico Gregorio):

  - `batchmode`: In this mode, nothing is printed on the terminal, and errors
    are scrolled as if the `return` key is hit at every error. Missing files
    that TeX tries to input or request from keyboard input cause the job to
    abort.

  - `nonstopmode`: In this mode, the diagnostic message will appear on the
    terminal, but there is no possibility of user interaction just like in batch
    mode, previously described.

  - `scrollmode`: In this mode, as the name indicates, TeX will stop only for
    missing files to input or if proper keyboard input is necessary. TeX fixes
    errors itself.

  - `errorstopmode`: In this mode, TeX will stop at each error, asking for
    proper user intervention. This is the most user interactive mode available.

- **[S]** `shell`: This option sets whether the possibility of running
  underlying system commands from within TeX is activated.

- **[S]** `synctex`: This option sets whether `synctex`, an input and output
  synchronization feature that allows navigation from source to typeset
  material and vice versa, available in most TeX engines, is activated.

- **[S]** `draft`: This option sets whether the draft mode, i.e, a mode that
  produces no output, so the engine can check the syntax, is activated.

- `options`: This option, as the name indicates, takes a list of raw command
  line options and appends it to the actual system call. An error is thrown if
  any data structure other than a proper list is provided as the value.

```tex
% arara: lualatex: { interaction: errorstopmode,
% arara: --> synctex: yes }
```

# `luatex`

This rule runs the `luatex` TeX engine on the provided `❖
currentFile` reference, generating a corresponding file in the Portable Document
Format, as expected.

- `interaction`: This option alters the underlying engine behaviour. If this
  option is omitted, TeX will prompt the user for interaction in the event of an
  error. Possible values are, in order of increasing user interaction (courtesy
  of our master Enrico Gregorio):

  - `batchmode`: In this mode, nothing is printed on the terminal, and errors
    are scrolled as if the `return` key is hit at every error. Missing files
    that TeX tries to input or request from keyboard input cause the job to
    abort.

  - `nonstopmode`: In this mode, the diagnostic message will appear on the
    terminal, but there is no possibility of user interaction just like in batch
    mode, previously described.

  - `scrollmode`: In this mode, as the name indicates, TeX will stop only for
    missing files to input or if proper keyboard input is necessary. TeX fixes
    errors itself.

  - `errorstopmode`: In this mode, TeX will stop at each error, asking for
    proper user intervention. This is the most user interactive mode available.

- **[S]** `shell`: This option sets whether the possibility of running
  underlying system commands from within TeX is activated.

- **[S]** `synctex`: This option sets whether `synctex`, an input and output
  synchronization feature that allows navigation from source to typeset
  material and vice versa, available in most TeX engines, is activated.

- **[S]** `draft`: This option sets whether the draft mode, i.e, a mode that
  produces no output, so the engine can check the syntax, is activated.

- `options`: This option, as the name indicates, takes a list of raw command
  line options and appends it to the actual system call. An error is thrown if
  any data structure other than a proper list is provided as the value.

```tex
% arara: luatex: { interaction: batchmode,
% arara: --> shell: yes, draft: yes }
```

# `make`

This rule runs `make`, a build automation tool that automatically builds
executable programs and libraries from source code, according to a special file
which specifies how to derive the target program.

- `targets`: This option takes a list of targets. Note that `make` updates a
  target if it depends on files that have been modified since the target was
  last modified, or if the target does not exist.

- `options`: This option, as the name indicates, takes a list of raw command
  line options and appends it to the actual system call. An error is thrown if
  any data structure other than a proper list is provided as the value.

```tex
% arara: make: { targets: [ compile, package ] }
```

# `makeglossaries`

This rule runs `makeglossaries`, an efficient Perl script designed for use with
TeX documents that work with the `glossaries` package. All the information
required to be passed to the relevant indexing application should also be
contained in the auxiliary file. The script takes the corresponding base name of
the `❖ currentFile` reference (i.e, the name without the associated
extension) as the mandatory argument.

- `options`: This option, as the name indicates, takes a list of raw command
  line options and appends it to the actual script call. An error is thrown if
  any data structure other than a proper list is provided as the value.

```tex
% arara: makeglossaries if found('aux', '@istfilename')
```

# `makeglossarieslite`

This rule runs `makeglossaries-lite`, a lightweight Lua script designed for use
with TeX documents that work with the `glossaries` package. All the information
required to be passed to the relevant indexing application should also be
contained in the auxiliary file. The script takes the corresponding base name of
the `❖ currentFile` reference (i.e, the name without the associated
extension) as the mandatory argument.

- `options`: This option, as the name indicates, takes a list of raw command
  line options and appends it to the actual script call. An error is thrown if
  any data structure other than a proper list is provided as the value.

```tex
% arara: makeglossarieslite if found('aux', '@istfilename')
```

# `makeindex`

This rule runs `makeindex`, a general purpose hierarchical index generator, on
the corresponding base name of the `❖ currentFile` reference (i.e,
the name without the associated extension) as a string concatenated with the
`idx` suffix, generating an index as a special `ind` file.

- `style`: This option, as the name indicates, sets the underlying index style
  file. Make sure to provide a valid `ist` file when using this option.

- **[S]** `german`: This option, as the name indicates, sets whether German word
  ordering should be used when generating the index, according to the rules
  set forth in DIN 5007.

- `order`: This option, as the name indicates, sets the default ordering scheme
  for the `makeindex` program. Two possible values are available:

  - `letter`: This value, as the name indicates, activates the letter ordering
    scheme. In such scheme, a blank space does not precede any letter in the
    alphabet.

  - `word`: This value, as the name indicates, activates the word ordering
    scheme. In such scheme, a blank space precedes any letter in the alphabet.

- `input` (default: `idx`): This option, as the name indicates, sets the default
  extension for the input file, according to the provided value. Later, this
  value will be concatenated as a suffix for the base name of the `❖
  currentFile` reference (i.e, the name without the associated extension).

- `output` (default: `ind`): This option, as the name indicates, sets the
  default extension for the output file, according to the provided value. Later,
  this value will be concatenated as a suffix for the base name of the
  `❖ currentFile` reference (i.e, the name without the associated
  extension).

- `log` (default: `ilg`): This option, as the name indicates, sets the default
  extension for the log file, according to the provided value. Later, this value
  will be concatenated as a suffix for the base name of the `❖
  currentFile` reference (i.e, the name without the associated extension).

- `options`: This option, as the name indicates, takes a list of raw command
  line options and appends it to the actual system call. An error is thrown if
  any data structure other than a proper list is provided as the value.

```tex
% arara: makeindex: { style: book.ist }
```

# `metapost`

This rule runs `metapost`, a tool to compile the Metapost graphics programming
language. Please note that you will have to make the `.mp` extension known to
arara in order to compile Metapost files. Furthermore, it is advised to use this
in your regular TeX document specifying the `files` parameter to include all
graphics you want to compile for inclusion in your document.

- `interaction`: This option alters the underlying engine behaviour. If this
  option is omitted, TeX will prompt the user for interaction in the event of an
  error. Possible values are, in order of increasing user interaction (courtesy
  of our master Enrico Gregorio):

  - `batchmode`: In this mode, nothing is printed on the terminal, and errors
    are scrolled as if the `return` key is hit at every error. Missing files
    that TeX tries to input or request from keyboard input cause the job to
    abort.

  - `nonstopmode`: In this mode, the diagnostic message will appear on the
    terminal, but there is no possibility of user interaction just like in batch
    mode, previously described.

  - `scrollmode`: In this mode, as the name indicates, TeX will stop only for
    missing files to input or if proper keyboard input is necessary. TeX fixes
    errors itself.

  - `errorstopmode`: In this mode, TeX will stop at each error, asking for
    proper user intervention. This is the most user interactive mode available.

- `numbersystem`: This option sets the number system Metapost will use for
  calculations.

  - `scaled`: In this mode, 32-bit fixed-point arithmetics is used.

  - `double`: In this mode, IEEE floating-point arithmetics with 64 bits is
    used.

  - `binary`: This mode is similary to `double` but without a fixed-length
    mantissa.

  - `decimal`: In this mode, arbitrary precision arithmetics is used and numbers
    are internally represented in base 10.

- `options`: This option, as the name indicates, takes a list of raw command
  line options and appends it to the actual system call. An error is thrown if
  any data structure other than a proper list is provided as the value.

```tex
% arara: metapost: { files: [ graphics.mp ] }
```

# `move`

This rule moves the `File` reference to the provided target using the underlying
operating system move operation. The target is *always* overwritten. However, be
mindful that an error will be thrown if you try to move or overwrite the file
referenced in `❖ getOriginalReference`.

- **[R]** `target`: This option, as the name implies, specifies the target for
  the move operation. Keep in mind that this option is required.

```tex
% arara: move: { files: [ 'thesis.pdf' ],
% arara: --> target: 'backup/thesis.pdf' }
```

# `nomencl`

This rule runs `makeindex` in order to automatically generate a nomenclature
list from TeX documents that work with the `nomencl` package. The program itself
is a general purpose hierarchical index generator and takes the corresponding
base name of the `❖ currentFile` reference (i.e, the name without the
associated extension) as a string concatenated with the `nlo` suffix and a
special style file in order to generate the nomenclature list as a special `nls`
file.

- `style` (default: `nomencl.ist`): This option, as the name indicates, sets the
  underlying index style file. The default value is set to the one automatically
  provided by the `nomencl` package, so it is highly recommended to not override
  it.

- `options`: This option, as the name indicates, takes a list of raw command
  line options and appends it to the actual system call. An error is thrown if
  any data structure other than a proper list is provided as the value.

```tex
% arara: nomencl
```

# `pbibtex`

This rule runs the `pbibtex` program, a reference management software, on the
corresponding base name of the `❖ currentFile` reference (i.e, the
name without the associated extension) as a string.

- `options`: This option, as the name indicates, takes a list of raw command
  line options and appends it to the actual system call. An error is thrown if
  any data structure other than a proper list is provided as the value.

```tex
% arara: pbibtex
% arara: --> if exists(toFile('references.bib'))
```

# `pdfcrop`

This rule runs `pdfcrop`, a command line utility to calculate and remove empty
margins, on corresponding base name of the `❖ currentFile` reference
(i.e, the name without the associated extension) as a string concatenated with
the `pdf` suffix.

- `output`: This option, as the name indicates, sets the output file. When
  omitted, the tool uses the input base name with the `-crop.pdf` suffix.

- **[S]** `verbose`: This option, as the name indicates, sets whether the
  command line tool will be executed in verbose mode.

- **[S]** `debug`: This option, as the name indicates, sets whether the command
  line tool will be executed in debug mode.

- `engine`: This option, as the name indicates, sets the underlying TeX engine
  to be used during the run. Three possible values are available:

  - `pdftex`: This value, as the name indicates, sets `pdftex` as the underlying
    TeX engine to be used during the run.

  - `xetex`: This value, as the name indicates, sets `xetex` as the underlying
    TeX engine to be used during the run.

  - `luatex`: This value, as the name indicates, sets `luatex` as the underlying
    TeX engine to be used during the run.

- `margins`: This option, as the name indicates, takes a list of four elements
  denoting left, top, right and bottom margins, respectivelly. An error will be
  thrown if no list is provided or if the list does not contain exactly four
  elements.

- **[S]** `clip`: This option, as the name indicates, sets whether the command
  line tool should include clipping support, if margins are set.

- **[S]** `hires`: This option, as the name indicates, sets whether the command
  line tool should use a high resolution bounding box feature.

- **[S]** `ini`: This option, as the name indicates, sets whether the `initex`
  variant of the underlying TeX engine is used.

- **[S]** `restricted`: This option, as the name indicates, sets whether the
  command line tool should run on restricted mode.

- `papersize`: This option, as the name indicates, sets the paper
  size. According to the documentation, this option should only be used with
  older versions of `ghostscript`.

- `resolution`: This option, as the name indicates, sets the resolution by
  forwarding the value to the underlying `ghostscript` call.

- `bbox`: This option, as the name indicates, takes a list of four elements
  denoting left, bottom, right and top margins, respectivelly, to override
  bounding box values found by `ghostscript`. An error will be thrown if no list
  is provided or if the list does not contain exactly four elements.

- **[S]** `uncompress`: This option, as the name indicates, sets whether the
  tool should generate an uncompressed Portable Document Format file, useful
  for debugging.

- `options`: This option, as the name indicates, takes a list of raw command
  line options and appends it to the actual system call. An error is thrown if
  any data structure other than a proper list is provided as the value.

```tex
% arara: pdfcrop
```

# `pdfcsplain`

This rule runs the `pdfcsplain` TeX engine, a conservative extension of Knuth's
plain TeX with direct processing characters and hyphenation patterns for Czech
and Slovak, on the provided `❖ currentFile` reference.

- `interaction`: This option alters the underlying engine behaviour. If this
  option is omitted, TeX will prompt the user for interaction in the event of an
  error. Possible values are, in order of increasing user interaction (courtesy
  of our master Enrico Gregorio):

  - `batchmode`: In this mode, nothing is printed on the terminal, and errors
    are scrolled as if the `return` key is hit at every error. Missing files
    that TeX tries to input or request from keyboard input cause the job to
    abort.

  - `nonstopmode`: In this mode, the diagnostic message will appear on the
    terminal, but there is no possibility of user interaction just like in batch
    mode, previously described.

  - `scrollmode`: In this mode, as the name indicates, TeX will stop only for
    missing files to input or if proper keyboard input is necessary. TeX fixes
    errors itself.

  - `errorstopmode`: In this mode, TeX will stop at each error, asking for
    proper user intervention. This is the most user interactive mode available.

- **[S]** `shell`: This option sets whether the possibility of running
  underlying system commands from within TeX is activated.

- **[S]** `synctex`: This option sets whether `synctex`, an input and output
  synchronization feature that allows navigation from source to typeset
  material and vice versa, available in most TeX engines, is activated.

- **[S]** `draft`: This option sets whether the draft mode, i.e, a mode that
  produces no output, so the engine can check the syntax, is activated.

- `options`: This option, as the name indicates, takes a list of raw command
  line options and appends it to the actual system call. An error is thrown if
  any data structure other than a proper list is provided as the value.

```tex
% arara: pdfcsplain: { shell: yes, synctex: yes }
```

# `pdflatex`

This rule runs the `pdflatex` TeX engine on the provided `❖
currentFile` reference, generating a corresponding file in the Portable Document
Format, as expected.

- `branch` (default: `stable`): This option allows branching formats for the
  current engine, mainly focused on package development. Users of current TeX
  distributions might benefit from format branching in order to easily test
  documents and code against the upcoming releases. Possible values are:

  - `stable`: This value, as the name implies, enables the stable engine format
    branch. Note that this is the default format.

  - `developer`: For experienced users, this value enables the experimental,
    developer engine format branch.

- `interaction`: This option alters the underlying engine behaviour. If this
  option is omitted, TeX will prompt the user for interaction in the event of an
  error. Possible values are, in order of increasing user interaction (courtesy
  of our master Enrico Gregorio):

  - `batchmode`: In this mode, nothing is printed on the terminal, and errors
    are scrolled as if the `return` key is hit at every error. Missing files
    that TeX tries to input or request from keyboard input cause the job to
    abort.

  - `nonstopmode`: In this mode, the diagnostic message will appear on the
    terminal, but there is no possibility of user interaction just like in batch
    mode, previously described.

  - `scrollmode`: In this mode, as the name indicates, TeX will stop only for
    missing files to input or if proper keyboard input is necessary. TeX fixes
    errors itself.

  - `errorstopmode`: In this mode, TeX will stop at each error, asking for
    proper user intervention. This is the most user interactive mode available.

- **[S]** `shell`: This option sets whether the possibility of running
  underlying system commands from within TeX is activated.

- **[S]** `synctex`: This option sets whether `synctex`, an input and output
  synchronization feature that allows navigation from source to typeset
  material and vice versa, available in most TeX engines, is activated.

- **[S]** `draft`: This option sets whether the draft mode, i.e, a mode that
  produces no output, so the engine can check the syntax, is activated.

- `options`: This option, as the name indicates, takes a list of raw command
  line options and appends it to the actual system call. An error is thrown if
  any data structure other than a proper list is provided as the value.

```tex
% arara: pdflatex: { interaction: batchmode }
% arara: --> if missing('pdf') || changed('tex')
```

# `pdftex`

This rule runs the `pdftex` TeX engine on the provided `❖
currentFile` reference, generating a corresponding file in the Portable Document
Format, as expected.

- `interaction`: This option alters the underlying engine behaviour. If this
  option is omitted, TeX will prompt the user for interaction in the event of an
  error. Possible values are, in order of increasing user interaction (courtesy
  of our master Enrico Gregorio):

  - `batchmode`: In this mode, nothing is printed on the terminal, and errors
    are scrolled as if the `return` key is hit at every error. Missing files
    that TeX tries to input or request from keyboard input cause the job to
    abort.

  - `nonstopmode`: In this mode, the diagnostic message will appear on the
    terminal, but there is no possibility of user interaction just like in batch
    mode, previously described.

  - `scrollmode`: In this mode, as the name indicates, TeX will stop only for
    missing files to input or if proper keyboard input is necessary. TeX fixes
    errors itself.

  - `errorstopmode`: In this mode, TeX will stop at each error, asking for
    proper user intervention. This is the most user interactive mode available.

- **[S]** `shell`: This option sets whether the possibility of running
  underlying system commands from within TeX is activated.

- **[S]** `synctex`: This option sets whether `synctex`, an input and output
  synchronization feature that allows navigation from source to typeset
  material and vice versa, available in most TeX engines, is activated.

- **[S]** `draft`: This option sets whether the draft mode, i.e, a mode that
  produces no output, so the engine can check the syntax, is activated.

- `options`: This option, as the name indicates, takes a list of raw command
  line options and appends it to the actual system call. An error is thrown if
  any data structure other than a proper list is provided as the value.

```tex
% arara: pdftex: { draft: yes }
```

# `pdftk`

This rule runs `pdftk`, a command line tool for manipulating Portable Document
Format documents, on the corresponding base name of the `❖
currentFile` reference (i.e, the name without the associated extension) as a
string concatenated with the `pdf` suffix.

- `options`: This option, as the name indicates, takes a list of raw command
  line options and appends it to the actual system call. An error is thrown if
  any data structure other than a proper list is provided as the value.

```tex
% arara: pdftk: { options: [ burst ] }
```

# `perltex`

This rule runs `perltex`, a wrapper that enables a symbiosis between Perl, a
popular general purpose programming language, and a TeX engine, on the provided
`❖ currentFile` reference.

- `engine` (default: `latex`): This option, as the name indicates, sets the
  underlying TeX engine to be used for the current compilation. Make sure to
  take a look at the manual for further details on this option. Possible values
  are:

  - `latex`: This value, as the name suggests, sets the underlying TeX engine to
    `latex` for the current compilation. Note that the engine might play a major
    role in the generated code.

  - `pdflatex`: This value, as the name indicates, sets the underlying TeX
    engine to `pdflatex` for the current compilation. Note that the engine might
    play a major role in the generated code.

  - `xelatex`: This value, as the name suggests, sets the underlying TeX engine
    to `xelatex` for the current compilation. Note that the engine might play a
    major role in the generated code.

  - `lualatex`: This value, as the name indicates, sets the underlying TeX
    engine to `lualatex` for the current compilation. Note that the engine might
    play a major role in the generated code.

  - `tex`: This value, as the name suggests, sets the underlying TeX engine to
    `tex` for the current compilation. Note that the engine might play a major
    role in the generated code.

  - `pdftex`: This value, as the name indicates, sets the underlying TeX engine
    to `pdftex` for the current compilation. Note that the engine might play a
    major role in the generated code.

  - `luatex`: This value, as the name suggests, sets the underlying TeX engine
    to `luatex` for the current compilation. Note that the engine might play a
    major role in the generated code.

  - `context`: This value, as the name indicates, sets the underlying TeX engine
    to `context` for the current compilation. Note that the engine might play a
    major role in the generated code.

- **[S]** `safe`: This option sets whether the wrapper should enable
  sandboxing. When explicitly disabled, the wrapper might execute any
  arbitrary Perl code, including that which can harm files.

- `permit`: This option takes a list of values in which indicate particular Perl
  operations to be performed, enabling finer-grained control over the wrapper
  sandbox.

- **[S]** `standalone`: This option generates a specific style file to make the
  document suitable for distribution to users who do not have the wrapper
  installed. Please refer to the manual for further details on this option.

- `interaction`: This option alters the underlying engine behaviour. If this
  option is omitted, TeX will prompt the user for interaction in the event of an
  error. Possible values are, in order of increasing user interaction (courtesy
  of our master Enrico Gregorio):

  - `batchmode`: In this mode, nothing is printed on the terminal, and errors
    are scrolled as if the `return` key is hit at every error. Missing files
    that TeX tries to input or request from keyboard input cause the job to
    abort.

  - `nonstopmode`: In this mode, the diagnostic message will appear on the
    terminal, but there is no possibility of user interaction just like in batch
    mode, previously described.

  - `scrollmode`: In this mode, as the name indicates, TeX will stop only for
    missing files to input or if proper keyboard input is necessary. TeX fixes
    errors itself.

  - `errorstopmode`: In this mode, TeX will stop at each error, asking for
    proper user intervention. This is the most user interactive mode available.

- **[S]** `shell`: This option sets whether the possibility of running
  underlying system commands from within TeX is activated.

- **[S]** `synctex`: This option sets whether `synctex`, an input and output
  synchronization feature that allows navigation from source to typeset
  material and vice versa, available in most TeX engines, is activated.

- `options`: This option, as the name indicates, takes a list of raw command
  line options and appends it to the actual system call. An error is thrown if
  any data structure other than a proper list is provided as the value.

```tex
% arara: perltex: { safe: no, standalone: yes }
```

# `platex`

This rule runs the `platex` TeX engine on the provided `❖
currentFile` reference, generating a corresponding file in a device independent
format.

- `branch` (default: `stable`): This option allows branching formats for the
  current engine, mainly focused on package development. Users of current TeX
  distributions might benefit from format branching in order to easily test
  documents and code against the upcoming releases. Possible values are:

  - `stable`: This value, as the name implies, enables the stable engine format
    branch. Note that this is the default format.

  - `developer`: For experienced users, this value enables the experimental,
    developer engine format branch.

- `interaction`: This option alters the underlying engine behaviour. If this
  option is omitted, TeX will prompt the user for interaction in the event of an
  error. Possible values are, in order of increasing user interaction (courtesy
  of our master Enrico Gregorio):

  - `batchmode`: In this mode, nothing is printed on the terminal, and errors
    are scrolled as if the `return` key is hit at every error. Missing files
    that TeX tries to input or request from keyboard input cause the job to
    abort.

  - `nonstopmode`: In this mode, the diagnostic message will appear on the
    terminal, but there is no possibility of user interaction just like in batch
    mode, previously described.

  - `scrollmode`: In this mode, as the name indicates, TeX will stop only for
    missing files to input or if proper keyboard input is necessary. TeX fixes
    errors itself.

  - `errorstopmode`: In this mode, TeX will stop at each error, asking for
    proper user intervention. This is the most user interactive mode available.

- **[S]** `shell`: This option sets whether the possibility of running
  underlying system commands from within TeX is activated.

- **[S]** `synctex`: This option sets whether `synctex`, an input and output
  synchronization feature that allows navigation from source to typeset
  material and vice versa, available in most TeX engines, is activated.

- `options`: This option, as the name indicates, takes a list of raw command
  line options and appends it to the actual system call. An error is thrown if
  any data structure other than a proper list is provided as the value.

```tex
% arara: platex: { interaction: scrollmode, shell: yes }
```

# `ps2pdf`

This rule runs `ps2pdf`, a tool that converts PostScript to Portable Document
Format, on the corresponding base name of the `❖ currentFile`
reference (i.e, the name without the associated extension) as a string
concatenated with the `ps` suffix.

- `output`: This option, as the name indicates, sets the output name for the
  generated `pdf` file. There is no need to provide an extension, as the value
  is always normalized with `❖ getBasename` such that only the name
  without the associated extension is used. The base name of the current file
  reference is used as the default value.

- `options`: This option, as the name indicates, takes a list of raw command
  line options and appends it to the actual system call. An error is thrown if
  any data structure other than a proper list is provided as the value.

```tex
% arara: ps2pdf: { output: article }
```

# `pythontex`

This rule runs `pythontex`, a wrapper that provides access to Python from within
typical TeX documents, on the provided `❖ currentFile`
reference. Make sure to take a look at the documentation for further details.

- `encoding`: This option sets the encoding of the underlying TeX document and
  all related files. If an encoding is not specified, Unicode is assumed.

- **[S]** `errorcode`: This option determines whether an exit code of 1 is
  returned if there were errors. On by default, but can be turned off since it
  is undesirable when working with some editors.

- **[S]** `runall`: This option sets whether all code to be executed, regardless
  of modification. It is useful when code has not been modified, but a
  dependency such as a library or external data has changed.

- `rerun`: This option, as the name indicates, sets the underlying threshold for
  reexecuting code. By default, the wrapper will rerun code that has been
  modified or that produced errors on the last run. Possible values are:

  - `never`: When this value is used, the wrapper never executes code. In this
    scenario, a warning is issued if there is modified code. Please refer to the
    documentation for further details.

  - `modified`: When this value is used, as the name indicates, the wrapper only
    executes code that has been modified or that has modified dependencies.

  - `errors`: When this value is used, as the name indicates, the wrapper
    executes code that has been modified as well as code that produced errors on
    the last run.

  - `warnings`: When this value is used, as the name indicates, the wrapper
    executes code that has been modified as well as code that produced errors or
    warnings on the last run.

  - `always`: When this value is used, as the name indicates, the wrapper
    executes all code, regardless of modification or errors and warnings. It is
    useful when code has not been modified, but a dependency such as a library
    or external data has changed.

- **[S]** `hashdependencies`: This option, as the name suggests, determines
  whether dependencies are checked for changes via their hashes or
  modification times.

- `jobs`: This option, as the name suggests, takes an integer value denoting the
  maximum number of concurrent processes. By default, the wrapper relies on the
  number of CPUs in the system.

- **[S]** `verbose`: This option sets whether the wrapper should be executed in
  verbose mode, providing more output information, including a list of all
  processes that are launched.

- `options`: This option, as the name indicates, takes a list of raw command
  line options and appends it to the actual system call. An error is thrown if
  any data structure other than a proper list is provided as the value.

```tex
% arara: pythontex: { jobs: 2, verbose: yes }
```

# `qpdf`

This rule runs `qpdf`, a command line application that does structural,
content-preserving transformations of Portable Document Format files, as well as
providing capabilities to developers.

- **[R]** `options`: This option, as the name indicates, takes a list of raw
  command line options and appends it to the actual script call. An error is
  thrown if any data structure other than a proper list is provided as the
  value.

```tex
% arara: qpdf: { options: [ '--linearize', 'input.pdf',
% arara: --> 'output.pdf' ] }
```

# `sage`

This rule runs `sage`, a free open source mathematics software system, on the
corresponding base name of the `❖ currentFile` reference (i.e, the
name without the associated extension) as a string concatenated with the `sage`
extension (which can be overriden).

- `program` (default: `sage`): This option, as the name indicates, sets the
  program name. If the tool is not directly available in your system path, make
  sure to use the full path to the installed `sage` binary.

- `extension` (default: `sage`): This option, as the name indicates, sets the
  default extension to the input file to be processed by `sage`. Three possible
  values are available:

  - `sage`: This value, as the name indicates, sets the extension to refer to
    the Sage format, the default one used by the software system.

  - `py`: This value, as the name indicates, sets the extension to refer to a
    typical Python source code.

  - `spyx`: This value, as the name indicates, sets the extension to refer to
    the SPYX format, a specific Sage compiled source code.

- `command`: This option, as the name indicates, forwards the provided value to
  the command line utility to be evaluated as a Sage code.

- **[S]** `dotsage`: This option, as the name indicates, sets whether the tool
  should consider using the `.sage` directory in the user home directory or a
  temporary one.

- `options`: This option, as the name indicates, takes a list of raw command
  line options and appends it to the actual system call. An error is thrown if
  any data structure other than a proper list is provided as the value.

```tex
% arara: sage
```

# `sketch`

This rule runs `sketch`, a system for producing line drawings of solid objects
and scenes, on the corresponding base name of the `❖ currentFile`
reference (i.e, the name without the associated extension) as a string
concatenated with the `sk` suffix. Note that one needs to add support for this
particular file type, as seen in [Configuration](/manual/configuration).

- `options`: This option, as the name indicates, takes a list of raw command
  line options and appends it to the actual system call. An error is thrown if
  any data structure other than a proper list is provided as the value.

```tex
% arara: sketch
```

# `songidx`

This rule runs `songidx`, a song index generation script for the `songs`
package, on the file reference provided as parameter, generating a proper index
as a special `sbx` file. It is very important to observe that, at the time of
writing, this script is not available off the shelf in TeX Live or MiKTeX
distributions, so a manual deployment is required. The script execution is
performed by the underlying `texlua` interpreter.

- **[R]** `input`: This required option, as the name indicates, sets the input
  name for the song index file specified within the TeX document. There is no
  need to provide an extension, as the value is always normalized with
  `❖ getBasename` such that only the name without the associated
  extension is used.

- `script` (default: `songidx.lua`): This option, as the name indicates, sets
  the script path. The default value is set to the script name, so either make
  sure `songidx.lua` is located in the same directory of your TeX document or
  provide the correct location (preferably a full path).

- `options`: This option, as the name indicates, takes a list of raw command
  line options and appends it to the actual script call. An error is thrown if
  any data structure other than a proper list is provided as the value.

```tex
% arara: songidx: { input: songs }
```

# `spix`

This rule runs `spix`, an interesting command line TeX automation tool written
in Python, on the provided `❖ currentFile` reference.

- `dry`: This option sets whether the tool should display a list of commands to
  be executed without actually invoking them.

- `options`: This option, as the name indicates, takes a list of raw command
  line options and appends it to the actual script call. An error is thrown if
  any data structure other than a proper list is provided as the value.

```tex
% arara: spix
```

# `tex`

This rule runs the `tex` TeX engine on the provided `❖ currentFile`
reference, generating a corresponding file in a device independent format.

- `interaction`: This option alters the underlying engine behaviour. If this
  option is omitted, TeX will prompt the user for interaction in the event of an
  error. Possible values are, in order of increasing user interaction (courtesy
  of our master Enrico Gregorio):

  - `batchmode`: In this mode, nothing is printed on the terminal, and errors
    are scrolled as if the `return` key is hit at every error. Missing files
    that TeX tries to input or request from keyboard input cause the job to
    abort.

  - `nonstopmode`: In this mode, the diagnostic message will appear on the
    terminal, but there is no possibility of user interaction just like in batch
    mode, previously described.

  - `scrollmode`: In this mode, as the name indicates, TeX will stop only for
    missing files to input or if proper keyboard input is necessary. TeX fixes
    errors itself.

  - `errorstopmode`: In this mode, TeX will stop at each error, asking for
    proper user intervention. This is the most user interactive mode available.

- **[S]** `shell`: This option sets whether the possibility of running
  underlying system commands from within TeX is activated.

- `options`: This option, as the name indicates, takes a list of raw command
  line options and appends it to the actual system call. An error is thrown if
  any data structure other than a proper list is provided as the value.

```tex
% arara: tex: { shell: yes }
```

# `texcount`

This rule runs `texcount`, a Perl script designed to count words in TeX and
LaTeX files, ignoring macros, tables, formulae and more on the provided
`❖ currentFile` reference. The script is highly configurable, so make
sure to check the manual for further information.

- `rules`: This option, as the name suggests, sets the rules which dictate how
  the script should work regarding word counting and option handling. Possible
  values are:

  - `relaxed`: This value, as the name indicates, sets a relaxed set of rules,
    allowing more general cases to be counted as either words and macros.

  - `restricted`: This value, as the name indicates, sets a more restricted set
    of rules for word counting and option handling.

- `verbosity`: This option, as the name suggests, sets the verbosity level of
  the script according to the provided integer value. Possible values are:

  - `0`: This value sets the lowest verbosity level of all, such that the script
    does not present parsing details.

  - `1`: This value raises the details a bit and sets the verbosity level to
    include parsed words and marked formulae.

  - `2`: This value adds more details from the previous verbosity level by
    including ignored text as well.

  - `3`: This value adds more details from the previous verbosity level by
    including comments and options.

  - `4`: This value sets the highest verbosity level of all, such that the
    script includes parsed worded, marked formulae, ignored text, comments,
    options and internal states.

- **[S]** `strict`: This option sets whether the tool should enable strict mode,
  so certain groups for which rules are not defined raise warnings.

- **[S]** `html`: This option, as the name suggests, defines whether the tool
  should output the report in the HTML format.

- **[S]** `total`: This option, as the name suggests, defines whether the tool
  should provide a total sum instead of partial sums (per file).

- **[S]** `unicode`: This option, as the name indicates, defines whether the
  tool should select Unicode as encoding for both input and output.

- `output`: This option, as the name suggests, sets the output file name in
  which the report will be written.

- `options`: This option, as the name indicates, takes a list of raw command
  line options and appends it to the actual system call. An error is thrown if
  any data structure other than a proper list is provided as the value.

```tex
% arara: texcount: { output: report.txt }
```

# `texindy`

This rule runs `texindy`, a variant of the `xindy` indexing system focused on
LaTeX documents, on the corresponding base name of the `❖
currentFile` reference (i.e, the name without the associated extension) as a
string concatenated with the `idx` suffix, generating an index as a special
`ind` file.

- **[S]** `quiet`: This option, as the name indicates, sets whether the tool
  will output progress messages. It is important to observe that `texindy`
  always outputs error messages, regardless of this option.

- `codepage`: This option, as the name indicates, specifies the encoding to be
  used for letter group headings. Additionally, it specifies the encoding used
  internally for sorting, but that does not matter for the final result.

- `language`: This option, as the name indicates, specifies the language that
  dictates the rules for index sorting. These rules are encoded in a module.

- `markup`: This option, as the name indicates, specifies the input markup for
  the raw index. The following values are available:

  - `latex`: This value, as the name implies, is emitted by default from the
    LaTeX kernel, and the raw input is encoded in the LaTeX Internal Character
    Representation format.

  - `xelatex`: This value, as the name implies, acts like the previous `latex`
    markup option, but without `inputenc` usage. Raw input is encoded in the
    UTF-8 format.

  - `omega`: This value, as the name implies, acts like the previous `latex`
    markup option, but with Omega's special notation as encoding for characters
    not in the ASCII set.

- `modules`: This option, as the name indicates, takes a list of module
  names. Modules are searched in the usual application path. An error is thrown
  if any data structure other than a proper list is provided as the value.

- `input` (default: `idx`): This option, as the name indicates, sets the default
  extension for the input file, according to the provided value. Later, this
  value will be concatenated as a suffix for the base name of the `❖
  currentFile` reference (i.e, the name without the associated extension).

- `output` (default: `ind`): This option, as the name indicates, sets the
  default extension for the output file, according to the provided value. Later,
  this value will be concatenated as a suffix for the base name of the
  `❖ currentFile` reference (i.e, the name without the associated
  extension).

- `log` (default: `ilg`): This option, as the name indicates, sets the default
  extension for the log file, according to the provided value. Later, this value
  will be concatenated as a suffix for the base name of the `❖
  currentFile` reference (i.e, the name without the associated extension).

- `options`: This option, as the name indicates, takes a list of raw command
  line options and appends it to the actual system call. An error is thrown if
  any data structure other than a proper list is provided as the value.

```tex
% arara: texindy: { markup: latex }
```

# `tikzmake`

This rule runs `make` on a very specific build file generated by the `tikzmake`
package, as a means to simplify the externalization of Ti*k*Z pictures. This
build file corresponds to the base name of the `❖ currentFile`
reference (i.e, the name without the associated extension) as a string
concatenated with the `makefile` suffix.

- **[S]** `force`: This option, as the name indicates, sets whether all targets
  specified in the corresponding build file should be unconditionally made.

- `jobs`: This option, as the name indicates, specifies the number of jobs
  (commands) to run simultaneously. Note that the provided value must be a
  positive integer. The default number of job slots is one, which means serial
  execution.

- `options`: This option, as the name indicates, takes a list of raw command
  line options and appends it to the actual system call. An error is thrown if
  any data structure other than a proper list is provided as the value.

```tex
% arara: tikzmake: { force: yes, jobs: 2 }
```

# `upbibtex`

This rule runs the `upbibtex` program, a reference management software, on the
corresponding base name of the `❖ currentFile` reference (i.e, the
name without the associated extension) as a string.

- `options`: This option, as the name indicates, takes a list of raw command
  line options and appends it to the actual system call. An error is thrown if
  any data structure other than a proper list is provided as the value.

```tex
% arara: pbibtex
% arara: --> if exists(toFile('references.bib'))
```

# `uplatex`

This rule runs the `uplatex` TeX engine on the provided `❖
currentFile` reference, generating a corresponding file in a device independent
format.

- `branch` (default: `stable`): This option allows branching formats for the
  current engine, mainly focused on package development. Users of current TeX
  distributions might benefit from format branching in order to easily test
  documents and code against the upcoming releases. Possible values are:

  - `stable`: This value, as the name implies, enables the stable engine format
    branch. Note that this is the default format.

  - `developer`: For experienced users, this value enables the experimental,
    developer engine format branch.

- `interaction`: This option alters the underlying engine behaviour. If this
  option is omitted, TeX will prompt the user for interaction in the event of an
  error. Possible values are, in order of increasing user interaction (courtesy
  of our master Enrico Gregorio):

  - `batchmode`: In this mode, nothing is printed on the terminal, and errors
    are scrolled as if the `return` key is hit at every error. Missing files
    that TeX tries to input or request from keyboard input cause the job to
    abort.

  - `nonstopmode`: In this mode, the diagnostic message will appear on the
    terminal, but there is no possibility of user interaction just like in batch
    mode, previously described.

  - `scrollmode`: In this mode, as the name indicates, TeX will stop only for
    missing files to input or if proper keyboard input is necessary. TeX fixes
    errors itself.

  - `errorstopmode`: In this mode, TeX will stop at each error, asking for
    proper user intervention. This is the most user interactive mode available.

- **[S]** `shell`: This option sets whether the possibility of running
  underlying system commands from within TeX is activated.

- **[S]** `synctex`: This option sets whether `synctex`, an input and output
  synchronization feature that allows navigation from source to typeset
  material and vice versa, available in most TeX engines, is activated.

- `options`: This option, as the name indicates, takes a list of raw command
  line options and appends it to the actual system call. An error is thrown if
  any data structure other than a proper list is provided as the value.

```tex
% arara: uplatex: { interaction: scrollmode, shell: yes }
```

# `uptex`

This rule runs the `uptex` TeX engine on the provided `❖ currentFile`
reference, generating a corresponding file in a device independent format.

- `interaction`: This option alters the underlying engine behaviour. If this
  option is omitted, TeX will prompt the user for interaction in the event of an
  error. Possible values are, in order of increasing user interaction (courtesy
  of our master Enrico Gregorio):

  - `batchmode`: In this mode, nothing is printed on the terminal, and errors
    are scrolled as if the `return` key is hit at every error. Missing files
    that TeX tries to input or request from keyboard input cause the job to
    abort.

  - `nonstopmode`: In this mode, the diagnostic message will appear on the
    terminal, but there is no possibility of user interaction just like in batch
    mode, previously described.

  - `scrollmode`: In this mode, as the name indicates, TeX will stop only for
    missing files to input or if proper keyboard input is necessary. TeX fixes
    errors itself.

  - `errorstopmode`: In this mode, TeX will stop at each error, asking for
    proper user intervention. This is the most user interactive mode available.

- **[S]** `shell`: This option sets whether the possibility of running
  underlying system commands from within TeX is activated.

- **[S]** `synctex`: This option sets whether `synctex`, an input and output
  synchronization feature that allows navigation from source to typeset
  material and vice versa, available in most TeX engines, is activated.

- `options`: This option, as the name indicates, takes a list of raw command
  line options and appends it to the actual system call. An error is thrown if
  any data structure other than a proper list is provided as the value.

```tex
% arara: uptex
```

# `xdvipdfmx`

This rule runs `xdvipdfmx`, the back end for the `xetex` TeX engine (and not
intended to be invoked directly), on the corresponding base name of the
`❖ currentFile` reference (i.e, the name without the associated
extension) as a string concatenated with a certain suffix, generating a Portable
Document Format `pdf` file.

- `entry`: This option sets the extension to be used for the initial reference
  (i.e, the current file name) as input to the command line tool. The following
  values are available for this option:

  - `dvi`: This value sets the extension to be used for the initial reference as
    a device independent format. This is the default value when no value is
    provided.

  - `xdv`: This value sets the extension to be used for the initial reference as
    an extended device independent format.

- `output`: This option, as the name indicates, sets the output name for the
  generated `pdf` file. There is no need to provide an extension, as the value
  is always normalized with `❖ getBasename` such that only the name
  without the associated extension is used. The base name of the current file
  reference is used as the default value.

- `options`: This option, as the name indicates, takes a list of raw command
  line options and appends it to the actual system call. An error is thrown if
  any data structure other than a proper list is provided as the value.

```tex
% arara: xdvipdfmx: { output: thesis }
```

# `xelatex`

This rule runs the new `xelatex` TeX engine on the provided `❖
currentFile` reference, generating a corresponding file in the Portable Document
Format, as expected.

- `branch` (default: `stable`): This option allows branching formats for the
  current engine, mainly focused on package development. Users of current TeX
  distributions might benefit from format branching in order to easily test
  documents and code against the upcoming releases. Possible values are:

  - `stable`: This value, as the name implies, enables the stable engine format
    branch. Note that this is the default format.

  - `developer`: For experienced users, this value enables the experimental,
    developer engine format branch.

  - `unsafe`: This value enables the unsafe engine format branch. According to
    the documentation, at all costs, avoid using this, or any, unsafe invocation
    with documents off the net or that are otherwise untrusted in any way.

- `interaction`: This option alters the underlying engine behaviour. If this
  option is omitted, TeX will prompt the user for interaction in the event of an
  error. Possible values are, in order of increasing user interaction (courtesy
  of our master Enrico Gregorio):

  - `batchmode`: In this mode, nothing is printed on the terminal, and errors
    are scrolled as if the `return` key is hit at every error. Missing files
    that TeX tries to input or request from keyboard input cause the job to
    abort.

  - `nonstopmode`: In this mode, the diagnostic message will appear on the
    terminal, but there is no possibility of user interaction just like in batch
    mode, previously described.

  - `scrollmode`: In this mode, as the name indicates, TeX will stop only for
    missing files to input or if proper keyboard input is necessary. TeX fixes
    errors itself.

  - `errorstopmode`: In this mode, TeX will stop at each error, asking for
    proper user intervention. This is the most user interactive mode available.

- **[S]** `shell`: This option sets whether the possibility of running
  underlying system commands from within TeX is activated.

- **[S]** `synctex`: This option sets whether `synctex`, an input and output
  synchronization feature that allows navigation from source to typeset
  material and vice versa, available in most TeX engines, is activated.

- `options`: This option, as the name indicates, takes a list of raw command
  line options and appends it to the actual system call. An error is thrown if
  any data structure other than a proper list is provided as the value.

```tex
% arara: xelatex: { shell: yes, synctex: yes }
```

# `xetex`

This rule runs the `xetex` TeX engine on the provided `❖ currentFile`
reference, generating a corresponding file in the Portable Document Format, as
expected.

- `branch` (default: `stable`): This option allows branching formats for the
  current engine, mainly focused on package development. Users of current TeX
  distributions might benefit from format branching in order to easily test
  documents and code against the upcoming releases. Possible values are:

  - `stable`: This value, as the name implies, enables the stable engine format
    branch. Note that this is the default format.

  - `unsafe`: This value enables the unsafe engine format branch. According to
    the documentation, at all costs, avoid using this, or any, unsafe invocation
    with documents off the net or that are otherwise untrusted in any way.

- `interaction`: This option alters the underlying engine behaviour. If this
  option is omitted, TeX will prompt the user for interaction in the event of an
  error. Possible values are, in order of increasing user interaction (courtesy
  of our master Enrico Gregorio):

  - `batchmode`: In this mode, nothing is printed on the terminal, and errors
    are scrolled as if the `return` key is hit at every error. Missing files
    that TeX tries to input or request from keyboard input cause the job to
    abort.

  - `nonstopmode`: In this mode, the diagnostic message will appear on the
    terminal, but there is no possibility of user interaction just like in batch
    mode, previously described.

  - `scrollmode`: In this mode, as the name indicates, TeX will stop only for
    missing files to input or if proper keyboard input is necessary. TeX fixes
    errors itself.

  - `errorstopmode`: In this mode, TeX will stop at each error, asking for
    proper user intervention. This is the most user interactive mode available.

- **[S]** `shell`: This option sets whether the possibility of running
  underlying system commands from within TeX is activated.

- **[S]** `synctex`: This option sets whether `synctex`, an input and output
  synchronization feature that allows navigation from source to typeset
  material and vice versa, available in most TeX engines, is activated.

- `options`: This option, as the name indicates, takes a list of raw command
  line options and appends it to the actual system call. An error is thrown if
  any data structure other than a proper list is provided as the value.

```tex
% arara: xetex: { interaction: scrollmode, synctex: yes }
```

# `xindex`

This rule runs `xindex`, a flexible and powerful indexing system, on a provided
`idx` input. This tool is completely with the `makeindex` program.

- **[R]** `input`: This option, as the name indicates, corresponds to the `idx`
  reference to be processed by the indexing system. Note that this option is
  required.

- `config` (default: `cfg`): This option specifies a configuration
  extension. Make sure to take a look at the documentation for further details.

- `language` (default: `en`): This option, as the name suggests, specifies the
  language. Make sure to take a look at the documentation for further details.

- `options`: This option, as the name indicates, takes a list of raw command
  line options and appends it to the actual system call. An error is thrown if
  any data structure other than a proper list is provided as the value.

```tex
% arara: xindex: { input: mydoc.idx }
```

# `xindy`

This rule runs `xindy`, a flexible and powerful indexing system, on the
corresponding base name of the `❖ currentFile` reference (i.e, the
name without the associated extension) as a string concatenated with the `idx`
suffix, generating an index as a special `ind` file.

- **[S]** `quiet`: This option, as the name indicates, sets whether the tool
  will output progress messages. It is important to observe that `xindy`
  always outputs error messages, regardless of this option.

- `codepage`: This option, as the name indicates, specifies the encoding to be
  used for letter group headings. Additionally, it specifies the encoding used
  internally for sorting, but that does not matter for the final result.

- `language`: This option, as the name indicates, specifies the language that
  dictates the rules for index sorting. These rules are encoded in a module.

- `markup`: This option, as the name indicates, specifies the input markup for
  the raw index. The following values are available:

  - `latex`: This value, as the name implies, is emitted by default from the
    LaTeX kernel, and the raw input is encoded in the LaTeX Internal Character
    Representation format.

  - `xelatex`: This value, as the name implies, acts like the previous `latex`
    markup option, but without `inputenc` usage. Raw input is encoded in the
    UTF-8 format.

  - `omega`: This value, as the name implies, acts like the previous `latex`
    markup option, but with Omega's special notation as encoding for characters
    not in the ASCII set.

  - `xindy`: This value, as the name implies, uses the `xindy` input markup as
    specified in the `xindy` manual.

- `modules`: This option, as the name indicates, takes a list of module
  names. Modules are searched in the usual application path. An error is thrown
  if any data structure other than a proper list is provided as the value.

- `input` (default: `idx`): This option, as the name indicates, sets the default
  extension for the input file, according to the provided value. Later, this
  value will be concatenated as a suffix for the base name of the `❖
  currentFile` reference (i.e, the name without the associated extension).

- `output` (default: `ind`): This option, as the name indicates, sets the
  default extension for the output file, according to the provided value. Later,
  this value will be concatenated as a suffix for the base name of the
  `❖ currentFile` reference (i.e, the name without the associated
  extension).

- `log` (default: `ilg`): This option, as the name indicates, sets the default
  extension for the log file, according to the provided value. Later, this value
  will be concatenated as a suffix for the base name of the `❖
  currentFile` reference (i.e, the name without the associated extension).

- `options`: This option, as the name indicates, takes a list of raw command
  line options and appends it to the actual system call. An error is thrown if
  any data structure other than a proper list is provided as the value.

```tex
% arara: xindy: { markup: xelatex }
```

It is highly advisable to browse the relevant documentation about packages and
tools described in this chapter as a means to learn more about features and
corresponding advanced usage. For TeX Live users, we recommend the use of
`texdoc`, a command line program to find and view documentation. For example,
this manual can be viewed through the following command:

```sh
$ texdoc arara
```

The primary function of the handy `texdoc` tool is to locate relevant
documentation for a given keyword (typically, a package name) on your disk, and
open it in an appropriate viewer. For MiKTeX users, the distribution provides a
similar tool named `mthelp` to find and view documentation. Make sure to use
these tools whenever needed!
