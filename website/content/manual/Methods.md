+++
title = "Methods"
description = "Methods"
weight = 6
+++

arara features several helper methods available in directive conditional and rule contexts which provide interesting features for enhancing the user experience, as well as improving the automation itself. This chapter provides a list of such methods. It is important to observe that virtually all classes from the Java runtime environment can be used within MVEL expressions, so your mileage might vary.

{% messagebox(title="A note on writing code") %}
As seen in [MVEL](/manual/mvel), Java and MVEL code be used interchangeably within expressions and orb tags, including instantiation of classes into objects and invocation of methods. However, be mindful of explicitly importing Java packages and classes through the classic `import` statement, as MVEL does not automatically handle imports, or an exception will surely be raised. Alternatively, you can provide the full qualified name to classes as well.
{% end %}

Methods are listed with their complete signatures, including potential  parameters and corresponding types. Also, the return type of a method is denoted by `type` and refers to a typical Java data type (either class or primitive). Do not worry too much, as there are illustrative examples. A method available in the directive conditional context will be marked by **[C]** next to the corresponding signature. Similarly, an entry marked by **[R]** denotes that the corresponding method is available in the rule context. At last, an entry marked by **[E]** denotes that the corresponding method is available in the orb tag expansion within a special `options` parameter in the directive context.

# Files

This section introduces methods related to file handling, searching and hashing. It is important to observe that no exception is thrown in case of an anomalous method call. In this particular scenario, the methods return empty references, when applied.

- **[R]** `getOriginalFile(): String` This method returns the original file name, as plain string, regardless of a potential override through the special `files` parameter in the directive mapping, as seen in [Concepts](/manual/concepts).

  ```java
  if (file == getOriginalFile()) {
      System.out.println("The 'file' variable
         was not overridden.");
  }
  ```

- **[C|E|R]** `getOriginalReference(): File` This method returns the original file reference, as a `File` object, regardless of a potential reference override indirectly through the special `files` parameter in the directive mapping, as seen in [Concepts](/manual/concepts).

  ```java
  if (reference.equals(getOriginalFile())) {
      System.out.println("The 'reference' variable
         was not overridden.");
  }
  ```

- **[C|R]** `currentFile(): File` This method returns the file reference, as a `File` object, for the current directive. It is important to observe that arara replicates the directive when the special `files` parameter is detected amongst the parameters, so each instance will have a different reference.

  ```tex
  % arara: pdflatex if currentFile().getName() == 'thesis.tex'
  ```

- **[C|R]** `toFile(String reference): File` This method returns a file (or directory) reference, as a `File` object, based on the provided string. Note that the string can refer to either a relative entry or a complete, absolute path. It is worth mentioning that, in Java, despite the curious name, a `File` object can be assigned to either a file or a directory.

  ```java
  f = toFile('thesis.tex');
  ```

- **[C|E|R]** `getBasename(File file): String` This method returns the base name (i.e, the name without the associated extension) of the provided `File` reference, as a string. Observe that this method ignores a potential path reference when extracting the base name. Also, this method will throw an exception if the provided reference is not a proper file.

  ```java
  basename = getBasename(toFile('thesis.tex'));
  ```

- **[R]** `getBasename(String reference): String` This method returns the base name (i.e, the name without the associated extension) of the provided `String` reference, as a string. Observe that this method ignores a potential path reference when extracting the base name.

  ```java
  basename = getBasename('thesis.tex');
  ```

- **[R]** `getFiletype(File file): String` This method returns the file type (i.e, the associated extension specified as a suffix to the name, typically delimited with a full stop) of the provided `File` reference, as a string. This method will throw an exception if the provided reference is not a proper file. An empty string is returned if, and only if, the provided file name has no associated extension.

  ```java
  extension = getFiletype(toFile('thesis.pdf'));
  ```

- **[R]** `getFiletype(String reference): String` This method returns the file type (i.e, the associated extension specified as a suffix to the name, typically delimited with a full stop) of the provided `String` reference, as a string. An empty string is returned if, and only if, the provided file name has no associated extension.

  ```java
  extension = getFiletype('thesis.pdf');
  ```

- **[C|R]** `exists(File file): boolean` This method, as the name implies, returns a boolean value according to whether the provided `File` reference exists. Observe that the provided reference can be either a file or a directory.

  ```tex
  % arara: bibtex if exists(toFile('references.bib'))
  ```

- **[C|R]** `exists(String extension): boolean` This method returns a boolean value according to whether the base name of the `❖ currentFile` reference (i.e, the name without the associated extension) as a string concatenated with the provided `String` extension exists. This method eases the checking of files which share the current file name modulo extension (e.g, log and auxiliary files). Note that the provided string refers to the extension, not the file name.

  ```tex
  % arara: pdftex if exists('tex')
  ```

- **[C|R]** `missing(File file): boolean` This method, as the name implies, returns a boolean value according to whether the provided `File` reference does not exist. It is important to observe that the provided reference can be either a file or a directory.

  ```tex
  % arara: pdftex if missing(toFile('thesis.pdf'))
  ```

- **[C|R]** `missing(String extension): boolean` This method returns a boolean value according to whether the base name of the `❖ currentFile` reference (i.e, the name without the associated extension) as a string concatenated with the provided `String` extension does not exist. This method eases the checking of files which share the current file name modulo extension (e.g, log and auxiliary files). Note that the provided string refers to the extension, not the file name.

  ```tex
  % arara: pdftex if missing('pdf')
  ```

- **[C|R]** `changed(File file): boolean` This method returns a boolean value according to whether the provided `File` reference has changed since last verification, based on a traditional cyclic redundancy check. The file reference, as well as the associated hash, is stored in a YAML database file named `arara.yaml` located in the same directory as the current file (the database name can be overridden in the configuration file. The method semantics (including the return values) is presented as follows.

  | File exists? &nbsp;&nbsp;&nbsp; | Entry exists? &nbsp;&nbsp;&nbsp; | Has changed? &nbsp;&nbsp;&nbsp; | DB action &nbsp;&nbsp;&nbsp; | Result |
  |:------------:|:-------------:|:------------:|:---------:|:------:|
  |      Yes     |      Yes      |      Yes     |   Update  |   Yes  |
  |      Yes     |      Yes      |      No      |     —     |   No   |
  |      Yes     |       No      |       —      |   Insert  |   Yes  |
  |      No      |       No      |       —      |     —     |   No   |
  |      No      |      Yes      |       —      |   Remove  |   Yes  |

  It is important to observe that this method *always* performs a database operation, either an insertion, removal or update on the corresponding entry. When using `❖ changed` within a logical expression, make sure the evaluation order is correct, specially regarding the use of short-circuiting operations. In some scenarios, order does matter.

  ```tex
  % arara: pdflatex if changed(toFile('thesis.tex'))
  ```

  {% messagebox(title="Short-circuit evaluation") %}
According to the [Wikipedia entry](https://en.wikipedia.org/wiki/Short-circuit_evaluation), a *short-circuit evaluation* is the semantics of some boolean operators in some programming languages in which the second argument is executed or evaluated only if the first argument does not suffice to determine the value of the expression. In Java (and consequently MVEL), both short-circuit and standard boolean operators are available.
  {% end %}

  {% messagebox(title="CRC as a hashing algorithm") %}
arara internally relies on a CRC32 implementation for file hashing. This particular choice, although not designed for hashing, offers an interesting trade-off between speed and quality. Besides, since it is not computationally expensive as strong algorithms such as MD5 and SHA1, CRC32 can be used for hashing typical TeX documents and plain text files with little to no collisions.
  {% end %}

- **[C|R]** `changed(String extension): boolean` This method returns a boolean value according to whether the base name of the `❖ currentFile` reference (i.e, the name without the associated extension) as a string concatenated with the provided `String` extension has changed since last verification, based on a traditional cyclic redundancy check. The file reference, as well as the associated hash, is stored in a YAML database file named `arara.yaml` located in the same directory as the current file (the database name can be overridden in the configuration file, as discussed in [Configuration](/manual/configuration). The method semantics (including the return values) is presented as follows.

  | File exists? &nbsp;&nbsp;&nbsp; | Entry exists? &nbsp;&nbsp;&nbsp; | Has changed? &nbsp;&nbsp;&nbsp; | DB action &nbsp;&nbsp;&nbsp; | Result |
  |:------------:|:-------------:|:------------:|:---------:|:------:|
  |      Yes     |      Yes      |      Yes     |   Update  |   Yes  |
  |      Yes     |      Yes      |      No      |     —     |   No   |
  |      Yes     |       No      |       —      |   Insert  |   Yes  |
  |      No      |       No      |       —      |     —     |   No   |
  |      No      |      Yes      |       —      |   Remove  |   Yes  |

  It is important to observe that this method *always* performs a database operation, either an insertion, removal or update on the corresponding entry. When using `❖ changed` within a logical expression, make sure the evaluation order is correct, specially regarding the use of short-circuiting operations. In some scenarios, order does matter.

  ```tex
  % arara: pdflatex if changed('tex')
  ```

- **[C|R]** `unchanged(File file): boolean` This method returns a boolean value according to whether the provided `File` reference has not changed since last verification, based on a traditional cyclic redundancy check. The file reference, as well as the associated hash, is stored in a YAML database file named `arara.yaml` located in the same directory as the current file (the database name can be overridden in the configuration file, as discussed in [Configuration](/manual/configuration). The method semantics (including the return values) is presented as follows.

  | File exists? &nbsp;&nbsp;&nbsp; | Entry exists? &nbsp;&nbsp;&nbsp; | Has changed? &nbsp;&nbsp;&nbsp; | DB action &nbsp;&nbsp;&nbsp; | Result |
  |:------------:|:-------------:|:------------:|:---------:|:------:|
  |      Yes     |      Yes      |      Yes     |   Update  |   No   |
  |      Yes     |      Yes      |      No      |     —     |   Yes  |
  |      Yes     |       No      |       —      |   Insert  |   No   |
  |      No      |       No      |       —      |     —     |   Yes  |
  |      No      |      Yes      |       —      |   Remove  |   No   |

  It is important to observe that this method *always* performs a database operation, either an insertion, removal or update on the corresponding entry. When using `❖ unchanged` within a logical expression, make sure the evaluation order is correct, specially regarding the use of short-circuiting operations. In some scenarios, order does matter.

  ```tex
  % arara: pdflatex if !unchanged(toFile('thesis.tex'))
  ```

- **[C|R]** `unchanged(String extension): boolean` This method returns a boolean value according to whether the base name of the `❖ currentFile` reference (i.e, the name without the associated extension) as a string concatenated with the provided `String` extension has not changed since last verification, based on a traditional cyclic redundancy check. The file reference, as well as the associated hash, is stored in a YAML database file named `arara.yaml` located in the same directory as the current file (the database name can be overridden in the configuration file, as discussed in [Configuration](/manual/configuration). The method semantics (including the return values) is presented as follows.

  | File exists? &nbsp;&nbsp;&nbsp; | Entry exists? &nbsp;&nbsp;&nbsp; | Has changed? &nbsp;&nbsp;&nbsp; | DB action &nbsp;&nbsp;&nbsp; | Result |
  |:------------:|:-------------:|:------------:|:---------:|:------:|
  |      Yes     |      Yes      |      Yes     |   Update  |   No   |
  |      Yes     |      Yes      |      No      |     —     |   Yes  |
  |      Yes     |       No      |       —      |   Insert  |   No   |
  |      No      |       No      |       —      |     —     |   Yes  |
  |      No      |      Yes      |       —      |   Remove  |   No   |

  It is important to observe that this method *always* performs a database operation, either an insertion, removal or update on the corresponding entry. When using `❖ unchanged` within a logical expression, make sure the evaluation order is correct, specially regarding the use of short-circuiting operations. In some scenarios, order does matter.

  ```tex
  % arara: pdflatex if !unchanged('tex')
  ```

- **[R]** `writeToFile(File file, String text, boolean append): boolean` This method performs a write operation based on the provided parameters. In this case, the method writes the `String` text to the `File` reference and returns a boolean value according to whether the operation was successful. The third parameter holds a `boolean` value and acts as a switch indicating whether the text should be appended to the existing content of the provided file. Keep in mind that the existing content of a file is always overwritten if this switch is disabled. Also, note that the switch has no effect if the file is being created at that moment. It is important to observe that this method does not raise any exception.

  ```java
  result = writeToFile(toFile('foo.txt'), 'hello world', false);
  ```

  {% messagebox(title="Read and write operations in Unicode") %}
arara *always* uses Unicode as the encoding format for read and write operations. This decision is deliberate as a means to offer a consistent representation and handling of text. Unicode can be implemented by different character encodings. In our case, the tool relies on UTF-8, which uses one byte for the first 128 code points, and up to 4 bytes for other characters. The first 128 Unicode code points are the ASCII characters, which means that any ASCII text is also UTF-8 text.
  {% end %}

  {% messagebox(title="File system permissions") %}
Most file systems have methods to assign permissions or access rights to specific users and groups of users. These permissions control the ability of the users to view, change, navigate, and execute the contents of the file system. Keep in mind that read and write operations depend on such permissions.
  {% end %}

- **[R]** `writeToFile(String reference, String text, boolean append): boolean` This method performs a write operation based on the provided parameters. In this case, the method writes the `String` text to the `String` reference and returns a boolean value according to whether the operation was successful. The third parameter holds a `boolean` value and acts as a switch indicating whether the text should be appended to the existing content of the provided file. Keep in mind that the existing content of a file is always overwritten if this switch is disabled. Also, note that the switch has no effect if the file is being created at that moment. It is important to observe that this method does not raise any exception.

  ```java
  result = writeToFile('foo.txt', 'hello world', false);
  ```

- **[R]** `writeToFile(File file, List<String> lines, boolean append): boolean` This method performs a write operation based on the provided parameters. In this case, the method writes the `List<String>` lines to the `File` reference and returns a boolean value according to whether the operation was successful. The third parameter holds a `boolean` value and acts as a switch indicating whether the text should be appended to the existing content of the provided file. Keep in mind that the existing content of a file is always overwritten if this switch is disabled. Also, note that the switch has no effect if the file is being created at that moment. It is important to observe that this method does not raise any exception.

  ```java
  result = writeToFile(toFile('foo.txt'),
           [ 'hello world', 'how are you?' ], false);
  ```

- **[R]** `writeToFile(String reference, List<String> lines, boolean append): boolean` This method performs a write operation based on the provided parameters. In this case, the method writes the `List<String>` lines to the `String` reference and returns a boolean value according to whether the operation was successful. The third parameter holds a `boolean` value and acts as a switch indicating whether the text should be appended to the existing content of the provided file. Keep in mind that the existing content of a file is always overwritten if this switch is disabled. Also, note that the switch has no effect if the file is being created at that moment. It is important to observe that this method does not raise any exception.

  ```java
  result = writeToFile('foo.txt', [ 'hello world',
           'how are you?' ], false);
  ```

- **[R]** `readFromFile(File file): List<String>` This method performs a read operation based on the provided parameter. In this case, the method reads the content from the `File` reference and returns a `List<String>` object representing the lines as a list of strings. If the reference does not exist or an exception is raised due to access permission constraints, the `❖ readFromFile` method returns an empty list. Keep in mind that, as a design decision, UTF-8 is *always* used as character encoding for read operations.

  ```java
  lines = readFromFile(toFile('foo.txt'));
  ```

- **[R]** `readFromFile(String reference): List<String>` This method performs a read operation based on the provided parameter. In this case, the method reads the content from the `String` reference and returns a `List<String>` object representing the lines as a list of strings. If the reference does not exist or an exception is raised due to access permission constraints, the `❖ readFromFile` method returns an empty list. Keep in mind that, as a design decision, UTF-8 is *always* used as character encoding for read operations.

  ```java
  lines = readFromFile('foo.txt');
  ```

- **[R]** `listFilesByExtensions(File file, List<String> extensions, boolean recursive): List<File>` This method performs a file search operation based on the provided parameters. In this case, the method list all files from the provided `File` reference according to the `List<String>` extensions as a list of strings, and returns a `List<File>` object representing all matching files. The leading full stop in each extension must be omitted, unless it is part of the search pattern. The third parameter holds a `boolean` value and acts as a switch indicating whether the search must be recursive, i.e, whether all subdirectories must be searched as well. If the reference is not a proper directory or an exception is raised due to access permission constraints, the `❖ listFilesByExtensions` method returns an empty list.

  ```java
  files = listFilesByExtensions(toFile('/home/paulo/Documents'),
          [ 'aux', 'log' ], false);
  ```

- **[R]** `listFilesByExtensions(String reference, List<String> extensions, boolean recursive): List<File>` This method performs a file search operation based on the provided parameters. In this case, the method list all files from the provided `String` reference according to the `List<String>` extensions as a list of strings, and returns a `List<File>` object representing all matching files. The leading full stop in each extension must be omitted, unless it is part of the search pattern. The third parameter holds a `boolean` value and acts as a switch indicating whether the search must be recursive, i.e, whether all subdirectories must be searched as well. If the reference is not a proper directory or an exception is raised due to access permission constraints, the `❖ listFilesByExtensions` method returns an empty list.

  ```java
  files = listFilesByExtensions('/home/paulo/Documents',
          [ 'aux', 'log' ], false);
  ```

- **[R]** `listFilesByPatterns(File file, List<String> patterns, boolean recursive): List<File>` This method performs a file search operation based on the provided parameters. In this case, the method lists all files from the provided `File` reference according to the `List<String>` patterns as a list of strings, and returns a `List<File>` object representing all matching files. The pattern specification is described below. The third parameter holds a `boolean` value and acts as a switch indicating whether the search must be recursive, i.e, whether all subdirectories must be searched as well. If the reference is not a proper directory or an exception is raised due to access permission constraints, the `❖ listFilesByPatterns` method returns an empty list. It is very important to observe that this file search operation might be slow depending on the provided directory. It is highly advisable to not rely on recursive searches whenever possible.

  {% messagebox(title="Patterns for file search operations") %}
arara employs wildcard filters as patterns for file search operations. Testing is case sensitive by default. The wildcard matcher uses the characters `?` and `*` to represent a single or multiple wildcard characters. This is the same as often found on typical terminals.
  {% end %}

  ```java
  files = listFilesByPatterns(toFile('/home/paulo/Documents'),
          [ '*.tex', 'foo?.txt' ], false);
  ```

- **[R]** `listFilesByPatterns(String reference, List<String> patterns, boolean recursive): List<File>` This method performs a file search operation based on the provided parameters. In this case, the method lists all files from the provided `String` reference according to the `List<String>` patterns as a list of strings, and returns a `List<File>` object representing all matching files. The pattern specification follows a wildcard filter. The third parameter holds a `boolean` value and acts as a switch indicating whether the search must be recursive, i.e, whether all subdirectories must be searched as well. If the reference is not a proper directory or an exception is raised due to access permission constraints, the `❖ listFilesByPatterns` method returns an empty list. It is very important to observe that this file search operation might be slow depending on the provided directory. It is highly advisable to not rely on recursive searches whenever possible.

  ```java
  files = listFilesByPatterns('/home/paulo/Documents',
          [ '*.tex', 'foo?.txt' ], false);
  ```

As the methods presented in this section have transparent error handling, the writing of rules and conditionals becomes more fluent and not too complex for the typical user.

# Conditional flow

This section introduces methods related to conditional flow based on *natural boolean values*, i.e, words that semantically represent truth and falsehood signs. Such concept provides a friendly representation of boolean values and eases the use of switches in directive parameters. The tool relies on the following set of natural boolean values:

- **True:** `yes`, `true`, `1`, `on`

- **False:** `no`, `false`, `0`, `off`

All elements from the provided set of natural boolean values can be used interchangeably in directive parameters. It is important to observe that arara throws an exception if a value absent from the set is provided to the methods described in this section.

- **[R]** `isTrue(String string): boolean` This method returns a boolean value according to whether the provided `String` value is contained in the sub-set of natural true boolean values. It is worth mentioning that the verification is case insensitive, i.e, upper case and lower case symbols are treated as equivalent. If the provided value is an empty string, the method returns false.

  ```java
  result = isTrue('yes');
  ```

- **[R]** `isFalse(String string): boolean` This method returns a boolean value according to whether the provided `String` value is contained in the sub-set of natural false boolean values. It is worth mentioning that the verification is case insensitive, i.e, upper case and lower case symbols are treated as equivalent. If the provided value is an empty string, the method returns false.

  ```java
  result = isFalse('off');
  ```

- **[R]** `isTrue(String string, Object yes): Object` This method checks if the first parameter is contained in the sub-set of natural true boolean values. If the result holds true, the second parameter is returned. Otherwise, an empty string is returned. It is worth mentioning that the verification is case insensitive, i.e, upper case and lower case symbols are treated as equivalent. If the first parameter is an empty string, the method returns an empty string.

  ```java
  result = isTrue('on', [ 'ls', '-la' ]);
  ```

- **[R]** `isFalse(String string, Object yes): Object` This method checks if the first parameter is contained in the sub-set of natural false boolean values. If the result holds true, the second parameter is returned. Otherwise, an empty string is returned. It is worth mentioning that the verification is case insensitive, i.e, upper case and lower case symbols are treated as equivalent. If the first parameter is an empty string, the method returns an empty string.

  ```java
  result = isFalse('0', 'pwd');
  ```

- **[R]** `isTrue(String string, Object yes, Object no): Object` This method checks if the first parameter is contained in the sub-set of natural true boolean values. If the result holds true, the second parameter is returned. Otherwise, the third parameter is returned. It is worth mentioning that the verification is case insensitive, i.e, upper case and lower case symbols are treated as equivalent. If the first parameter is an empty string, the method returns the third parameter.

  ```java
  result = isTrue('on', [ 'ls', '-la' ], 'pwd');
  ```

- **[R]** `isFalse(String string, Object yes, Object no): Object` This method checks if the first parameter is contained in the sub-set of natural false boolean values. If the result holds true, the second parameter is returned. Otherwise, the third parameter is returned. It is worth mentioning that the verification is case insensitive, i.e, upper case and lower case symbols are treated as equivalent. If the first parameter is an empty string, the method returns the third parameter.

  ```java
  result = isFalse('0', 'pwd', 'ps');
  ```

- **[R]** `isTrue(String string, Object yes, Object no, Object fallback): Object` This method checks if the first parameter is contained in the sub-set of natural true boolean values. If the result holds true, the second parameter is returned. Otherwise, the third parameter is returned. It is worth mentioning that the verification is case insensitive, i.e, upper case and lower case symbols are treated as equivalent. If the first parameter is an empty string, the method returns the fourth parameter as default value.

  ```java
  result = isTrue('on', 'ls', 'pwd', 'who');
  ```

- **[R]** `isFalse(String string, Object yes, Object no, Object fallback): Object` This method checks if the first parameter is contained in the sub-set of natural false boolean values. If the result holds true, the second parameter is returned. Otherwise, the third parameter is returned. It is worth mentioning that the verification is case insensitive, i.e, upper case and lower case symbols are treated as equivalent. If the first parameter is an empty string, the method returns the fourth parameter as default value.

  ```java
  result = isFalse('0', 'pwd', 'ps', 'ls');
  ```

- **[R]** `isTrue(boolean value, Object yes): Object` This method evaluates the first parameter as a boolean expression. If the result holds true, the second parameter is returned. Otherwise, an empty string is returned.

  ```java
  result = isTrue(1 == 1, 'yes');
  ```

- **[R]** `isFalse(boolean value, Object yes): Object` This method evaluates the first parameter as a boolean expression. If the result holds false, the second parameter is returned. Otherwise, an empty string is returned.

  ```java
  result = isFalse(1 != 1, 'yes');
  ```

- **[R]** `isTrue(boolean value, Object yes, Object no): Object` This method evaluates the first parameter as a boolean expression. If the result holds true, the second parameter is returned. Otherwise, the third parameter is returned.

  ```java
  result = isTrue(1 == 1, 'yes', 'no');
  ```

- **[R]** `isFalse(boolean value, Object yes, Object no): Object` This method evaluates the first parameter as a boolean expression. If the result holds false, the second parameter is returned. Otherwise, the third parameter is returned.

  ```java
  result = isFalse(1 != 1, 'yes', 'no');
  ```

Supported by the concept of natural boolean values, the methods presented in this section ease the use of switches in directive parameters and can be adopted as valid alternatives for traditional conditional flows, when applied.

# Strings

String manipulation constitutes one of the foundations of rule interpretation in our tool. This section introduces methods for handling such types, as a means to offer high level constructs for users.

- **[R]** `isEmpty(String string): boolean` This method returns a boolean value according to whether the provided `String` value is empty, i.e, the string length is equal to zero.

  ```java
  result = isEmpty('not empty');
  ```

- **[R]** `isNotEmpty(String string): boolean` This method returns a boolean value according to whether the provided `String` value is not empty, i.e, the string length is greater than zero.

  ```java
  result = isNotEmpty('not empty');
  ```

- **[R]** `isEmpty(String string, Object yes): boolean` This method checks if the first parameter is empty, i.e, if the string length is equal to zero. If the result holds true, the second parameter is returned. Otherwise, an empty string is returned.

  ```java
  result = isEmpty('not empty', 'ps');
  ```

- **[R]** `isNotEmpty(String string, Object yes): boolean` This method checks if the first parameter is not empty, i.e, if the string length is greater than zero. If the result holds true, the second parameter is returned. Otherwise, an empty string is returned.

  ```java
  result = isNotEmpty('not empty', 'ls');
  ```

- **[R]** `isEmpty(String string, Object yes, Object no): boolean` This method checks if the first parameter is empty, i.e, if the string length is equal to zero. If the result holds true, the second parameter is returned. Otherwise, the third parameter is returned.

  ```java
  result = isEmpty('not empty', 'ps', 'ls');
  ```

- **[R]** `isNotEmpty(String string, Object yes, Object no): boolean` This method checks if the first parameter is not empty, i.e, if the string length is greater than zero. If the result holds true, the second parameter is returned. Otherwise, the third parameter is returned.

  ```java
  result = isNotEmpty('not empty', 'ls', 'ps');
  ```

- **[R]** `buildString(Object... objects): String` This method returns a string based on the provided array of objects, separating each element by one blank space. It is important to observe that empty values are not considered. Also, note that the object array is denoted by a  comma-separated sequence of elements in the actual method call, resulting in a variable number of parameters.

  ```java
  result = buildString('a', 'b', 'c', 'd');
  ```

- **[R]** `trimSpaces(String string): String` This method trims spaces from the provided parameter, i.e, leading and trailing spaces in the `String` reference are removed, and returns the resulting string. It is important to observe that non-boundary spaces inside the string are not removed at all.

  ```java
  result = trimSpaces('   hello world   ');
  ```

- **[R]** `replicatePattern(String pattern, List<Object> values): List<Object>` This method replicates the provided pattern to each element of the second parameter and returns the resulting list. The pattern must contain exactly one placeholder. For instance, `\%s` denotes a string representation of the provided argument. Please refer to the `Formatter` class reference in the [Java documentation](https://docs.oracle.com/javase/7/docs/api/java/util/Formatter.html) for more information on placeholders. This method raises an exception if an invalid pattern is applied.

  ```java
  names = replicatePattern('My name is %s', [ 'Brent', 'Nicola' ]);
  ```

- **[C|R]** `found(File file, String regex): boolean` This method returns a boolean value according to whether the content of the provided `File` reference contains at least one match of the provided `String` regular expression. It is important to observe that this method raises an exception if an invalid regular expression is provided as the parameter or if the provided file reference does not exist.

  ```tex
  % arara: pdflatex while found(toFile('article.log'),
  % arara: --> 'undefined references')
  ```

- **[C|R]** `found(String extension, String regex): boolean` This method returns a boolean value according to whether the content of the base name of the `❖ currentFile` reference (i.e, the name without the associated extension) as a string concatenated with the provided `String` extension contains at least one match of the provided `String` regular expression. It is important to observe that this method raises an exception if an invalid regular expression is provided as the parameter or if the provided file reference does not exist.

  ```tex
  % arara: pdflatex while found('log', 'undefined references')
  ```

The string manipulation methods presented in this section constitute an interesting and straightforward approach to handling directive parameters without the usual verbosity in writing typical Java constructs.

# Operating systems

This section introduces methods related to the underlying operating system detection, as a means of providing a straightforward approach to writing cross-platform rules.

- **[R]** `isWindows(): boolean` This method returns a boolean value according to whether the underlying operating system vendor is Microsoft Windows.

  ```java
  if (isWindows()) { System.out.println('Running Windows.'); }
  ```

- **[R]** `isLinux(): boolean` This method returns a boolean value according to whether the underlying operating system vendor is a Linux instance.

  ```java
  if (isLinux()) { System.out.println('Running Linux.'); }
  ```

- **[R]** `isMac(): boolean` This method returns a boolean value according to whether the underlying operating system vendor is Apple Mac OS.

  ```java
  if (isMac()) { System.out.println('Running Mac OS.'); }
  ```

- **[R]** `isUnix(): boolean` This method returns a boolean value according to whether the underlying operating system vendor is any Unix variation.

  ```java
  if (isUnix()) { System.out.println('Running Unix.'); }
  ```

- **[R]** `isCygwin(): boolean` This method returns a boolean value according to whether the underlying operating system vendor is Microsoft Windows and arara is being executed inside a Cygwin environment.

  ```java
  if (isCygwin()) { System.out.println('Running Cygwin.'); }
  ```

  {% messagebox(title="Cygwin paths") %}
It is worth mentioning that Cygwin has its own path handling which is not reliable when (JVM) applications need to invoke system commands. For instance, the following invocation does not work:

```tex
% arara: pdflatex
```

Since `pdflatex` is a symbolic link to `pdftex` with the proper format, we encourage Cygwin users to rely on the following trick:

```tex
% arara: pdftex: { options: [ '--fmt=pdflatex' ] }
```

The same trick can be employed by other TeX engines with appropriate `--fmt` flags as well. We are still investigating this issue and looking for potential alternatives.
  {% end %}

- **[R]** `isWindows(Object yes, Object no): Object` This method checks if the underlying operating system vendor is Microsoft Windows. If the result holds true, the first parameter is returned. Otherwise, the second parameter is returned.

  ```java
  command = isWindows('del', 'rm');
  ```

- **[R]** `isLinux(Object yes, Object no): Object` This method checks if the underlying operating system vendor is a Linux instance. If the result holds true, the first parameter is returned. Otherwise, the second parameter is returned.

  ```java
  command = isLinux('rm', 'del');
  ```

- **[R]** `isMac(Object yes, Object no): Object` This method checks if the underlying operating system vendor is Apple Mac OS. If the result holds true, the first parameter is returned. Otherwise, the second parameter is returned.

  ```java
  command = isMac('ls', 'dir');
  ```

- **[R]** `isUnix(Object yes, Object no): Object` This method checks if the underlying operating system vendor is any Unix variation. If the result holds true, the first parameter is returned. Otherwise, the second parameter is returned.

  ```java
  command = isUnix('tree', 'dir');
  ```

- **[R]** `isCygwin(Object yes, Object no): Object` This method checks if the underlying operating system vendor is Microsoft Windows and if arara is being executed inside a Cygwin environment. If the result holds true, the first parameter is returned. Otherwise, the second parameter is returned.

  ```java
  command = isCygwin('ls', 'dir');
  ```

The methods presented in the section provide useful information to help users write cross-platform rules and thus enhance the automation experience based on specific features of the underlying operating system.

# Type checking

In certain scenarios, a plain string representation of directive parameters might be inadequate or insufficient given the rule requirements. To this end, this section introduces methods related to type checking as a means to provide support and verification for common data types.

- **[R]** `isString(Object object): boolean` This method returns a boolean value according to whether the provided `Object` object is a string or any extended type.

  ```java
  result = isString('foo');
  ```

- **[R]** `isList(Object object): boolean` This method returns a boolean value according to whether the provided `Object` object is a list or any extended type.

  ```java
  result = isList([ 1, 2, 3 ]);
  ```

- **[R]** `isMap(Object object): boolean` This method returns a boolean value according to whether the provided `Object` object is a map or any extended type.

  ```java
  result = isMap([ 'Paulo' : 'Palmeiras', 'Carla' : 'Inter' ]);
  ```

- **[R]** `isBoolean(Object object): boolean` This method returns a boolean value according to whether the provided `Object` object is a boolean or any extended type.

  ```java
  result = isBoolean(false);
  ```

- **[R]** `checkClass(Class clazz, Object object): boolean` This method returns a boolean value according to whether the provided `Object` object is an instance or a subtype of the provided `Class` class. It is interesting to note that all methods presented in this section internally rely on `❖ checkClass` for type checking.

  ```java
  result = checkClass(List.class, [ 'a', 'b' ]);
  ```

The methods presented in this section cover the most common types used in directive parameters and should suffice for expressing the rule requirements. If a general approach is needed, please refer to the `❖ checkClass` method for checking virtually any type available in the Java environment.

# Classes and objects

arara can be extended at runtime with code from JVM languages, such as Groovy, Scala, Clojure and Kotlin. The tool can load classes from `class` and `jar` files and even instantiate them. This section introduces methods related to class loading and object instantiation.

{% messagebox(title="Ordered pairs") %}
According to the [Wikipedia entry](https://en.wikipedia.org/wiki/Ordered_pair), in mathematics, an *ordered pair* *(a, b)* is a pair of objects. The order in which the objects appear in the pair is significant: the ordered pair *(a, b)* is different from the ordered pair *(b, a)* unless *a = b*. In the ordered pair *(a, b)*, the object *a* is called the *first* entry, and the object *b* the *second* entry of the pair. arara relies on this concept with the helper `Pair<A, B>` class, in which `A` and `B` denote the component classes, i.e, the types associated to the pair elements. In order to access the pair entries, the class provides two property accessors:

- `❖ first: A`: This property accessor, as the name implies, returns the first entry of the ordered pair, as an `A` object.

- `❖ second: B`: This property accessor, as the name implies, returns the second entry of the ordered pair, as a `B` object.


Keep in mind that the entries in the `Pair` class, once defined, cannot be modified to other values. The initial values are set during instantiation and, therefore, only entry getters are available to the user during the object life cycle.
{% end %}

{% messagebox(title="Status for class loading and instantiation") %}
The class loading and instantiation methods provided by arara typically return a pair composed of an integer value and a class or object reference. This integer value acts as a status of the underlying operation itself and might indicate potential issues. The possible values are:

- `0`: Successful execution
- `1`: File does not exist
- `2`: File URL is incorrect
- `3`: Class was not found
- `4`: Access policy violation
- `5`: Instantiation exception

Please make sure to *always* check the returned integer status when using class loading and instantiation methods in directive and rule contexts. This feature is quite powerful yet tricky and subtle!
{% end %}

- **[C|R]** `loadClass(File file, String name): Pair<ClassLoading.ClassLoadingStatus, Object>` This method loads a class based on the canonical name from the provided `File` reference and returns an ordered pair containing the status and the class reference itself. The file must contain the Java bytecode, either directly accessible from a `class` file or packaged inside a `jar` file. If an exception is raised, this method returns the `Object` class reference as second entry of the pair.

  ```java
  result = loadClass(toFile('mymath.jar'),
           'com.github.cereda.mymath.Arithmetic');
  ```

- **[C|R]** `loadClass(String ref,String name): Pair<ClassLoading.ClassLoadingStatus, Object>` This method loads a class based on the canonical name from the provided `String` reference and returns an ordered pair containing the status and the class reference itself. The file must contain the Java bytecode, either directly accessible from a `class` file or packaged inside a `jar` file.  If an exception is raised, this method returns the `Object` class reference as second entry of the pair.

    ```java
    result = loadClass('mymath.jar',
             'com.github.cereda.mymath.Arithmetic');
    ```

- **[C|R]** `loadObject(File file,String name): Pair<ClassLoading.ClassLoadingStatus, Object>` This method loads a class based on the canonical name from the provided `File` reference and returns an ordered pair containing the status and a proper corresponding object instantiation. The file must contain the Java bytecode, either directly accessible from a `class` file or packaged inside a `jar` file. If an exception is raised, this method returns an `Object` object as second entry of the pair.

  ```java
  result = loadObject(toFile('mymath.jar'),
           'com.github.cereda.mymath.Trigonometric');
  ```

- **[C|R]** `loadObject(String ref, String name): Pair<ClassLoading.ClassLoadingStatus, Object>` This method loads a class based on the canonical name from the provided `String` reference and returns an ordered pair containing the status and a proper corresponding object instantiation. The file must contain the Java bytecode, either directly accessible from a `class` file or packaged inside a `jar` file. If an exception is raised, this method returns an `Object` object as second entry of the pair.

  ```java
  result = loadObject('mymath.jar',
           'com.github.cereda.mymath.Trigonometric');
  ```

This section presented class loading and instantiation methods which may significantly enhance the expressiveness of rules and directives. However, make sure to use such feature with great care and attention.

# Dialog boxes

A *dialog box* is a graphical control element, typically a small window, that communicates information to the user and prompts them for a response. This section introduces UI methods related to such interactions.

{% messagebox(title="UI elements") %}
The graphical elements are provided by the Swing toolkit from the Java runtime environment. Note that the default look and feel class reference can be modified through a key in the configuration file, as seen in [Configuration](/manual/configuration). It is important to observe that the methods presented in this section require a graphical interface. If arara is being executed in a headless environment (i.e, an environment with no graphical display available), an exception will be thrown when trying to use such UI methods in either directive or rule contexts.
{% end %}

Each dialog box provided by the UI methods of arara requires the specification of an associated icon. An *icon* is a pictogram displayed on a computer screen in order to help the user quickly identify the message by conveying its meaning through a visual resemblance to a physical object. Our tool features five icon types, illustrated below, to be used with dialog boxes. Observe that each icon type is associated with a unique integer value which is provided later on to the actual method call. Also, it is worth mentioning that the visual appearance of such icons is based on the underlying Java virtual machine and the current look and feel, so your mileage might vary.

- `1`: Error
- `2`: Information
- `3`: Attention
- `4`: Question
- `5`: Plain

As good practice, make sure to provide descriptive messages to be placed in dialog boxes in order to ease and enhance the user experience. It is also highly advisable to always provide an associated icon, so avoid the plain option whenever possible.

{% messagebox(title="Message text width") %}
arara sets the default message text width to 250 pixels. Feel free to override this value according to your needs. Please refer to the appropriate method signatures for specifying a new width.
{% end %}

The UI method signatures are followed by a visual representation of the provided dialog box. For the sake of simplicity, each parameter index refers to the associated number in the figure.

- **[R]** `showMessage(int width, int icon, String title, String text): void`

<img src="/images/manual/messagebox1.png" alt="UI 1" width="701"/>

  This method shows a message box according to the provided parameters. The dialog box is disposed when the user either presses the confirmation button or closes the window. It is important to observe that arara temporarily interrupts the execution and waits for the dialog box disposal. Also note that the total time includes the idle period as well.

  ```java
  showMessage(250, 2, 'My title', 'My message');
  ```

- **[R]** `showMessage(int icon, String title, String text): void`

  <img src="/images/manual/messagebox2.png" alt="UI 2" width="701"/>

  This method shows a message box according to the provided parameters. The dialog box is disposed when the user either presses the confirmation button or closes the window. It is important to observe that arara temporarily interrupts the execution and waits for the dialog box disposal. Also note that the total time includes the idle period as well.

  ```java
  showMessage(2, 'My title', 'My message');
  ```

- **[C|R]** `showOptions(int width, int icon, String title, String text, Object... options): int`

  <img src="/images/manual/optionbox1.png" alt="UI 3" width="701"/>

  This method shows a message box according to the provided parameters, including options represented as an array of `Object` objects. This array is portrayed in the dialog box as a list of buttons. The dialog box is disposed when the user either presses one of the buttons or closes the window. The method returns the natural index of the selected button, starting from `1`. If no button is pressed (e.g, the window is closed), `0` is returned. Note that the object array is denoted by a  comma-separated sequence of elements in the actual method call, resulting in a variable number of parameters. It is important to observe that arara temporarily interrupts the execution and waits for the dialog box disposal. Also note that the total time includes the idle period as well.

  ```tex
  % arara: pdflatex if showOptions(250, 4, 'Important!',
  % arara: --> 'Do you like ice cream?', 'Yes!', 'No!') == 1
  ```

  {% messagebox(title="Button orientation") %}
Keep in mind that your window manager might render the button orientation differently than the original arrangement specified in your array of objects. For instance, I had a window manager that rendered the buttons in the reverse order. However, note that the visual appearance should not interfere with the programming logic! The indices shall remain the same, pristine as ever, regardless of the actual rendering. Trust your code, not your eyes.
  {% end %}

- **[C|R]** `showOptions(int icon, String title, String text, Object... options): int`

  <img src="/images/manual/optionbox2.png" alt="UI 4" width="701"/>

  This method shows a message box according to the provided parameters, including options represented as an array of `Object` objects. This array is portrayed in the dialog box as a list of buttons. The dialog box is disposed when the user either presses one of the buttons or closes the window. The method returns the natural index of the selected button, starting from `1`. If no button is pressed (e.g, the window is closed), `0` is returned. Note that the object array is denoted by a  comma-separated sequence of elements in the actual method call, resulting in a variable number of parameters. It is important to observe that arara temporarily interrupts the execution and waits for the dialog box disposal. Also note that the total time includes the idle period as well.

  ```tex
  % arara: pdflatex if showOptions(4, 'Important!',
  % arara: --> 'Do you like ice cream?', 'Yes!', 'No!') == 1
  ```

- **[C|R]** `showDropdown(int width, int icon, String title, String text, Object... options): int`

  <img src="/images/manual/dropdown1.png" alt="UI 5" width="701"/>

  This method shows a dialog box according to the provided parameters, including options represented as an array of `Object` objects. This array is portrayed in the dialog box as a dropdown list. The first element from the array is automatically selected. The dialog box is disposed when the user either presses one of the buttons or closes the window. The method returns the natural index of the selected item, starting from `1`. If the user cancels the dialog or closes the window, `0` is returned.  Note that the object array is denoted by a comma-separated sequence of elements in the actual method call, resulting in a variable number of parameters. It is important to observe that arara temporarily interrupts the execution and waits for the dialog box disposal. Also note that the total time includes the idle period as well.

  ```tex
  % arara: pdflatex if showDropdown(250, 4, 'Important!',
  % arara: --> 'Who deserves the tick?', 'David Carlisle',
  % arara: --> 'Enrico Gregorio', 'Joseph Wright',
  % arara: --> 'Heiko Oberdiek') == 2
  ```

  {% messagebox(title="Combo boxes and dropdown lists") %}
According to the [Wikipedia entry](https://en.wikipedia.org/wiki/Combo_box), a *combo box* is a combination of a dropdown list or list box and a single line editable textbox, allowing the user to either type a value directly or select a value from the list. The term is sometimes used to mean a dropdown list, but in Java, the term is definitely not a synonym! A dropdown list is sometimes clarified with terms such as non-editable combo box to distinguish it from the original definition of a combo box.
  {% end %}

- **[C|R]** `showDropdown(int icon, String title, String text, Object... options): int`

  <img src="/images/manual/dropdown2.png" alt="UI 6" width="701"/>

  This method shows a dialog box according to the provided parameters, including options represented as an array of `Object` objects. This array is portrayed in the dialog box as a dropdown list. The first element from the array is automatically selected. The dialog box is disposed when the user either presses one of the buttons or closes the window. The method returns the natural index of the selected item, starting from `1`. If the user cancels the dialog or closes the window, `0` is returned.  Note that the object array is denoted by a comma-separated sequence of elements in the actual method call, resulting in a variable number of parameters. It is important to observe that arara temporarily interrupts the execution and waits for the dialog box disposal. Also note that the total time includes the idle period as well.

  ```tex
  % arara: pdflatex if showDropdown(4, 'Important!',
  % arara: --> 'Who deserves the tick?', 'David Carlisle',
  % arara: --> 'Enrico Gregorio', 'Joseph Wright',
  % arara: --> 'Heiko Oberdiek') == 2
  ```

  {% messagebox(title="Swing toolkit") %}
According to the [Wikipedia entry](https://en.wikipedia.org/wiki/Swing_(Java)), the Swing toolkit was developed to provide a more sophisticated set of GUI components than the earlier  AWT widget system. Swing provides a look and feel that emulates the look and feel of several platforms, and also supports a pluggable look and feel that allows applications to have a look and feel unrelated to the underlying platform. It has more powerful and flexible components than AWT. In addition to familiar components such as buttons, check boxes and labels, Swing provides several advanced components, such as scroll panes, trees, tables, and lists.
  {% end %}

- **[C|R]** `showInput(int width, int icon, String title, String text): String`

  <img src="/images/manual/inputbox1.png" alt="UI 7" width="701"/>

  This method shows an input dialog box according to the provided parameters. The dialog box is disposed when the user either presses one of the buttons or closes the window. The method returns the content of the input text field, as a trimmed `String` object. If the user cancels the dialog or closes the window, an empty string is returned. It is important to observe that arara temporarily interrupts the execution and waits for the dialog box disposal. Also note that the total time includes the idle period as well.

  ```tex
  % arara: pdflatex if showInput(250, 4, 'Important!',
  % arara: --> 'Who wrote arara?') == 'Paulo'
  ```

- **[C|R]** `showInput(int icon, String title, String text): String`

  <img src="/images/manual/inputbox2.png" alt="UI 8" width="701"/>

  This method shows an input dialog box according to the provided parameters. The dialog box is disposed when the user either presses one of the buttons or closes the window. The method returns the content of the input text field, as a trimmed `String` object. If the user cancels the dialog or closes the window, an empty string is returned. It is important to observe that arara temporarily interrupts the execution and waits for the dialog box disposal. Also note that the total time includes the idle period as well.

  ```tex
  % arara: pdflatex if showInput(4, 'Important!',
  % arara: --> 'Who wrote arara?') == 'Paulo'
  ```

The UI methods presented in this section can be used for writing TeX tutorials and assisted compilation workflows based on user interactions, including visual input and feedback through dialog boxes.

# Commands

arara features the `Command` object, a new approach for handling system commands based on a high level structure with explicit argument parsing.

{% messagebox(title="The anatomy of a command") %}
From the user perspective, a `Command` object is simply a good old list of `Object` objects, in which the list head (i.e, the first element) is the underlying system command, and the list tail (i.e, the remaining elements), if any, contains the associated command line arguments. For instance, given `[ 'pdflatex', '--shell-escape', '--synctex=1', 'thesis.tex' ]`, we have:

- Head: `'pdflatex'`
- Tail: `[ '--shell-escape', '--synctex=1', 'thesis.tex' ]`

From the previous example, it is important to observe that a potential file name quoting is not necessary. The underlying system command execution library handles the provided arguments accordingly.

Behind the scenes, however, arara employs a different workflow when constructing a `Command` object. The tool sets the working directory path for the current command to `USER_DIR` which is based on the current execution. The working directory path can be explicitly set through specific method calls, described later on in this section.

The list of objects is then completely flattened and all elements are mapped to their string representations through corresponding `❖ toString` calls. Finally, the proper `Command` object is constructed. Keep in mind that, although a command takes a list (or even an array) of objects, which can be of any type, the internal representation is *always* a list of strings.
{% end %}

A list of objects might contain nested lists, i.e, a list within another list. As previously mentioned, arara employs *list flattening* when handling a list of objects during a `Command` object instantiation. As a means to illustrate this handy feature, consider the following list of integers:

```java
[ 1, 2, [ 3, 4 ], 5, [ [ 6, 7 ], 8 ], 9, [ [ 10 ] ]
```
Note that the above list of integers contains nested lists. When applying list flattening, arara recursively adds the elements of nested lists to the original list and then removes the nested occurrences. Please refer to the source code for implementation details. The new flattened list is presented as follows.

```java
[ 1, 2, 3, 4, 5, 6, 7, 8, 9, 10 ]
```

List flattening and string mapping confer expressiveness and flexibility to the `Command` object construction, as users can virtually use any data type to describe the underlying rule logic and yet obtain a consistent representation.

- **[R]** `getCommand(List<String> elements): Command` This method, as the name implies, returns a `Command` object according to the provided list of `String` elements. If the list is empty, the tool will ignore the execution.

  ```java
  return getCommand([ 'ls', '-l' ]);
  ```

- **[R]** `getCommand(Object... elements): Command` This method, as the name implies, returns a `Command` object according to the provided array of `Object` elements. If the array is empty, the tool will ignore the execution. Note that the object array is denoted by a comma-separated sequence of elements in the actual method call, resulting in a variable number of parameters.

  ```java
  return getCommand('pdflatex', '--shell-escape', 'thesis.tex');
  ```

- **[R]** `getCommandWithWorkingDirectory(File directory, List<String> elements): Command` This method, as the name implies, sets the working directory based on the provided `File` reference and returns a proper `Command` object according to the provided list of `String` elements. If the list is empty, the tool will ignore the execution.

  ```java
  return getCommandWithWorkingDirectory(toFile('/home/paulo'),
         [ 'ls', '-l' ]);
  ```

- **[R]** `getCommandWithWorkingDirectory(String path, List<String> elements): Command` This method, as the name implies, sets the working directory based on the provided `String` reference and returns a proper `Command` object according to the provided list of `String` elements. If the list is empty, the tool will ignore the execution.

  ```java
  return getCommandWithWorkingDirectory('/home/paulo',
         [ 'ls', '-l' ]);
  ```

- **[R]** `getCommandWithWorkingDirectory(File directory, Object... elements): Command` This method, as the name implies, sets the working directory based on the provided `File` reference and returns a proper `Command` object according to the provided array of `Object` elements. If the array is empty, the tool will ignore the execution. Note that the object array is denoted by a comma-separated sequence of elements in the actual method call, resulting in a variable number of parameters.

  ```java
  return getCommandWithWorkingDirectory(toFile('/home/paulo'),
         'pdflatex', '--shell-escape', 'thesis.tex');
  ```

- **[R]** `getCommandWithWorkingDirectory(String path, Object... elements): Command` This method, as the name implies, sets the working directory based on the provided `String` reference and returns a proper `Command` object according to the provided array of `Object` elements. If the array is empty, the tool will ignore the execution. Note that the object array is denoted by a comma-separated sequence of elements in the actual method call, resulting in a variable number of parameters.

  ```java
  return getCommandWithWorkingDirectory('/home/paulo',
         'pdflatex', '--shell-escape', 'thesis.tex');
  ```

The methods presented in this section constitute the foundations of underlying system command execution. In particular, whenever possible, it is highly advisable to use `Command` objects through proper `❖ getCommand` method calls, as the plain string approach used in previous versions of our tool is marked as deprecated and will be removed in future versions.

# Others

This section introduces assorted methods provided by arara as a means to improve the automation itself with expressive rules and enhance the user experience. Such methods are properly described as follows.

{% messagebox(title="Session") %}
Rules are designed under the *encapsulation* notion, such that the direct access to internal workings of such structures is restricted. However, as a means to support framework awareness, arara provides a mechanism for data sharing across rule contexts, implemented as a `Session` object. In practical terms, this particular object is a global, persistent map composed of `String` keys and `Object` values available throughout the entire execution. The public methods of a session are described as follows:

- `❖ put(String key, Object value): void`] This method, as the name implies, inserts an object into the session, indexed by the provided key. Observe that, if the session previously contained a mapping for the provided key, the old value is replaced by the specified value.

- `❖ remove(String key): void`] This method, as the name implies, removes  the mapping for the provided key from the session. Be mindful that an attempt to remove a mapping for a nonexistent key will raise an exception.

- `❖ contains(String key): boolean`] This method, as the name implies, returns a boolean value according to whether the session contains a mapping for the provided key. It is highly advisable to use this method before attempting to remove a mapping from the session.

- `❖ get(String key): Object`] This method, as the name implies, returns the object value to which the specified key is mapped. Be mindful that an attempt to return a value for a nonexistent key will raise an exception.

- `❖ forget(): void`] This method, as the name implies, removes all of the existing mappings from the session. The session object will be effectively empty after this call returns.

It is important to observe that the `Session` object provided by our tool follows the *singleton* pattern, i.e, a software design pattern that restricts the instantiation of a class to one object. Therefore, the same session is consistently shared across rule contexts.
{% end %}

- **[C|E|R]** `getSession(): Session` This method, as the name implies, returns the `Session` object for data sharing across rule contexts. Keep in mind that a session cannot contain duplicate keys. Each key can map to at most one value.

  ```java
  name = getSession().get('name');
  ```

- **[R]** `throwError(String message): void` This method deliberately throws an error to be intercepted later on during execution. Consider using such method for an explicit notification about unexpected or unsought scenarios, e.g, wrong parameter types or values. The raised error has an associated message which is displayed in the terminal and added to the log file.

  ```java
  options = 'not a list';
  if (!isList(options)) {
      throwError('I was expecting a list.');
  }
  ```

- **[R]** `isVerboseMode(): boolean` This method, as the name implies, returns a boolean value according to whether arara is being executed in verbose mode, enabled through either the `--verbose` command line option or the corresponding key in the configuration file. Note that the logical negation of such method indicates whether the tool is being executed in silent mode.

  ```java
  verbose = isVerboseMode();
  ```

- **[R]** `isOnPath(String name): boolean` This method, as the name implies, returns a boolean value according to whether the provided `String` reference representing a command name is reachable from the system path.  For portability reasons, there is no need to provide extensions to Microsoft Windows command names, as arara will look for common patterns. This behaviour is expected and by design. However, be mindful that the search is case sensitive.

  ```java
  result = isOnPath('pdftex');
  ```

  {% messagebox(title="Path inspection") %}
According to the [Wikipedia entry](https://en.wikipedia.org/wiki/PATH_(variable)), `PATH` is an environment variable on Unix-like operating systems and Microsoft Windows, specifying a set of directories where executable programs are located. arara performs a file search operation based on all directories specified in the system path, filtering files by name (and extensions, when in Microsoft Windows). When an exact match is found, the search is concluded. Notwithstanding the great effort, it is very important to note that there is no guarantee that our tool will be able to correctly reach the command in all scenarios.
  {% end %}

- **[R]** `unsafelyExecuteSystemCommand(Command command): Pair<Integer, String>` This method, which has a very spooky name, unsafely executes the provided `Command` reference and returns an ordered pair containing the exit status and the command output. Note that, if an exception is raised during the command execution, `-99` is assigned as exit status and an empty string is defined as command output. Please make sure to always check the returned integer status when using this method.

  ```java
  result = unsafelyExecuteSystemCommand(getCommand('ls'));
  ```

  {% messagebox(title="Hic sunt leones") %}
Please *do not abuse* this method! Keep in mind that this particular feature is included for very specific scenarios in which the command streams are needed ahead of time for proper decision making.
  {% end %}

- **[R]** `isSubdirectory(File directory): boolean` This method checks whether the provided `File` reference is a valid subdirectory under the project hierarchy, return a corresponding boolean value. This is a check to impose a possible restriction in the rule scope, so that users can change down to subdirectories in their projects but not up, outside of the root directory.

  ```java
  valid = isSubdirectory(toFile('chapters/'));
  ```

- **[R]** `getOrNull(List<String> list, int index): String` This method attempts to retrieve
  a list element based on an integer index. If the index is out of bounds, a `null` value is returned instead.

  ```java
  list = [ 'a', 'b', 'c' ];
  third = getOrNull(list, 2);
  ```

- **[R]** `getOrNull(List<String> list): String` This method attempts to retrieve
  the first element of the provided list. If the list is empty, a `null` value is returned instead.

  ```java
  list = [ 'a', 'b', 'c' ];
  first = getOrNull(list);
  ```

{% messagebox(title="Flags and reserved storage in a session") %}
From version 6.0 on, there are three reserved namespaces within a session. They are described as follows:

- `environment` This namespace is quite intuitive: arara will store the current state of the systems environment variables in its session. You may alter these values in the session storage but they will not be written back to the system configuration. To access an environment variable, you can use its usual name prefixed by `environment:`:

```java
path = getSession().get('environment:PATH');
```

- `arara` This namespace provides flags that control the underlying behaviour of arara. Flags are used in rules and may be manipulated by the user. Be aware that every change in this namespace will result in the tool acting like you know what you did. Use this power with care. Currently, there is only one relevant flag: `arara:FILENAME:halt`. This will stop the currently run command execution on the file with the specified file name. The value of this map entry is the exit status you want arara to have.

```java
getSession().put('arara:myfile.tex:halt', 42);
```

- `arg` This namespace acts as a bridge between contexts and the command line by providing access to key/value pairs defined at runtime by `--call-property` command line flags.

```java
key = getSession().get('arg:key');
```

Please refer to [Command line](/manual/cli) for more details on the `--call-property` command line flag.
{% end %}

The methods presented in this section provide interesting features for persistent data sharing, error handling, early command execution, and templating. It is important to note that more classes, objects and methods can be incorporated into arara through class loading and object instantiation, extending the features and enhancing the overall user experience.
