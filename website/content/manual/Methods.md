+++
title = "Methods"
description = "Methods"
weight = 6
+++

\arara\ features several helper methods available in directive conditional and rule contexts which provide interesting features for enhancing the user experience, as well as improving the automation itself. This chapter provides a list of such methods. It is important to observe that virtually all classes from the Java runtime environment can be used within MVEL expressions, so your mileage might vary.

\begin{messagebox}{A note on writing code}{araracolour}{\icok}{white}
As seen in Chapter~\ref{chap:mvel}, on page~\pageref{chap:mvel}, Java and MVEL code be used interchangeably within expressions and orb tags, including instantiation of classes into objects and invocation of methods. However, be mindful of explicitly importing Java packages and classes through the classic \rbox{import} statement, as MVEL does not automatically handle imports, or an exception will surely be raised. Alternatively, you can provide the full qualified name to classes as well.
\end{messagebox}

Methods are listed with their complete signatures, including potential  parameters and corresponding types. Also, the return type of a method is denoted by \rrbox{type} and refers to a typical Java data type (either class or primitive). Do not worry too much, as there are illustrative examples. A method available in the directive conditional context will be marked by \ctbox{C} next to the corresponding signature. Similarly, an entry marked by \ctbox{R} denotes that the corresponding method is available in the rule context. At last, an entry marked by \ctbox{E} denotes that the corresponding method is available in the orb tag expansion within a special \rbox{options} parameter in the directive context.

\section{Files}
\label{sec:files}

This section introduces methods related to file handling, searching and hashing. It is important to observe that no exception is thrown in case of an anomalous method call. In this particular scenario, the methods return empty references, when applied.

\begin{description}
\item[\mdbox{R}{getOriginalFile()}{String}] This method returns the original file name, as plain string, regardless of a potential override through the special \abox{files} parameter in the directive mapping, as seen in Section~\ref{sec:directives}, on page~\pageref{sec:directives}.

\begin{codebox}{Example}{teal}{\icnote}{white}
if (file == getOriginalFile()) {
    System.out.println("The 'file' variable
       was not overridden.");
}
\end{codebox}

\item[\mdddbox{C}{E}{R}{getOriginalReference()}{File}] This method returns the original file reference, as a \rbox{File} object, regardless of a potential reference override indirectly through the special \abox{files} parameter in the directive mapping, as seen in Section~\ref{sec:directives}, on page~\pageref{sec:directives}.

\begin{codebox}{Example}{teal}{\icnote}{white}
if (reference.equals(getOriginalFile())) {
    System.out.println("The 'reference' variable
       was not overridden.");
}
\end{codebox}

\item[\mddbox{C}{R}{currentFile()}{File}] This method returns the file reference, as a \rbox{File} object, for the current directive. It is important to observe that \arara\ replicates the directive when the special \abox{files} parameter is detected amongst the parameters, so each instance will have a different reference.

\begin{codebox}{Example}{teal}{\icnote}{white}
% arara: pdflatex if currentFile().getName() == 'thesis.tex'
\end{codebox}

\item[\mddbox{C}{R}{toFile(String reference)}{File}] This method returns a file (or directory) reference, as a \rbox{File} object, based on the provided string. Note that the string can refer to either a relative entry or a complete, absolute path. It is worth mentioning that, in Java, despite the curious name, a \rbox{File} object can be assigned to either a file or a directory.

\begin{codebox}{Example}{teal}{\icnote}{white}
f = toFile('thesis.tex');
\end{codebox}

\item[\mdddbox{C}{E}{R}{getBasename(File file)}{String}] This method returns the base name (i.e, the name without the associated extension) of the provided \rbox{File} reference, as a string. Observe that this method ignores a potential path reference when extracting the base name. Also, this method will throw an exception if the provided reference is not a proper file.

\begin{codebox}{Example}{teal}{\icnote}{white}
basename = getBasename(toFile('thesis.tex'));
\end{codebox}

\item[\mdbox{R}{getBasename(String reference)}{String}] This method returns the base name (i.e, the name without the associated extension) of the provided \rbox{String} reference, as a string. Observe that this method ignores a potential path reference when extracting the base name.

\begin{codebox}{Example}{teal}{\icnote}{white}
basename = getBasename('thesis.tex');
\end{codebox}

\item[\mdbox{R}{getFiletype(File file)}{String}] This method returns the file type (i.e, the associated extension specified as a suffix to the name, typically delimited with a full stop) of the provided \rbox{File} reference, as a string. This method will throw an exception if the provided reference is not a proper file. An empty string is returned if, and only if, the provided file name has no associated extension.

\begin{codebox}{Example}{teal}{\icnote}{white}
extension = getFiletype(toFile('thesis.pdf'));
\end{codebox}

\item[\mdbox{R}{getFiletype(String reference)}{String}] This method returns the file type (i.e, the associated extension specified as a suffix to the name, typically delimited with a full stop) of the provided \rbox{String} reference, as a string. An empty string is returned if, and only if, the provided file name has no associated extension.

\begin{codebox}{Example}{teal}{\icnote}{white}
extension = getFiletype('thesis.pdf');
\end{codebox}

\item[\mddbox{C}{R}{exists(File file)}{boolean}] This method, as the name implies, returns a boolean value according to whether the provided \rbox{File} reference exists. Observe that the provided reference can be either a file or a directory.

\begin{codebox}{Example}{teal}{\icnote}{white}
% arara: bibtex if exists(toFile('references.bib'))
\end{codebox}

\item[\mddbox{C}{R}{exists(String extension)}{boolean}] This method returns a boolean value according to whether the base name of the \mtbox{currentFile} reference (i.e, the name without the associated extension) as a string concatenated with the provided \rbox{String} extension exists. This method eases the checking of files which share the current file name modulo extension (e.g, log and auxiliary files). Note that the provided string refers to the extension, not the file name.

\begin{codebox}{Example}{teal}{\icnote}{white}
% arara: pdftex if exists('tex')
\end{codebox}

\item[\mddbox{C}{R}{missing(File file)}{boolean}] This method, as the name implies, returns a boolean value according to whether the provided \rbox{File} reference does not exist. It is important to observe that the provided reference can be either a file or a directory.

\begin{codebox}{Example}{teal}{\icnote}{white}
% arara: pdftex if missing(toFile('thesis.pdf'))
\end{codebox}

\item[\mddbox{C}{R}{missing(String extension)}{boolean}] This method returns a boolean value according to whether the base name of the \mtbox{currentFile} reference (i.e, the name without the associated extension) as a string concatenated with the provided \rbox{String} extension does not exist. This method eases the checking of files which share the current file name modulo extension (e.g, log and auxiliary files). Note that the provided string refers to the extension, not the file name.

\begin{codebox}{Example}{teal}{\icnote}{white}
% arara: pdftex if missing('pdf')
\end{codebox}

\item[\mddbox{C}{R}{changed(File file)}{boolean}] This method returns a boolean value according to whether the provided \rbox{File} reference has changed since last verification, based on a traditional cyclic redundancy check. The file reference, as well as the associated hash, is stored in a YAML database file named \rbox{arara.yaml} located in the same directory as the current file (the database name can be overridden in the configuration file, as discussed in Section~\ref{sec:basicstructure}, on page~\pageref{sec:basicstructure}). The method semantics (including the return values) is presented as follows.

\vspace{1em}

{\centering\small
\setlength\tabcolsep{0.8em}
\begin{tabular}{@{}ccccc@{}}
\toprule
\emph{file exists?} & \emph{entry exists?} &
\emph{has changed?} & \emph{DB action} &
\emph{result} \\
\midrule
\cbyes{-2} & \cbyes{-2} & \cbyes{-2} & update & \cbyes{-2} \\
\cbyes{-2} & \cbyes{-2} & \cbno{-2} & --- & \cbno{-2} \\
\cbyes{-2} & \cbno{-2} & --- & insert & \cbyes{-2} \\
\cbno{-2} & \cbno{-2} & --- & --- & \cbno{-2} \\
\cbno{-2} & \cbyes{-2} & --- & remove & \cbyes{-2} \\
\bottomrule
\end{tabular}\par}

\vspace{1.4em}

It is important to observe that this method \emph{always} performs a database operation, either an insertion, removal or update on the corresponding entry. When using \mtbox{changed} within a logical expression, make sure the evaluation order is correct, specially regarding the use of short-circuiting operations. In some scenarios, order does matter.

\begin{codebox}{Example}{teal}{\icnote}{white}
% arara: pdflatex if changed(toFile('thesis.tex'))
\end{codebox}

\begin{messagebox}{Short-circuit evaluation}{araracolour}{\icok}{white}
According to the \href{https://en.wikipedia.org/wiki/Short-circuit_evaluation}{Wikipedia entry}, a \emph{short-circuit evaluation} is the semantics of some boolean operators in some programming languages in which the second argument is executed or evaluated only if the first argument does not suffice to determine the value of the expression. In Java (and consequently MVEL), both short-circuit and standard boolean operators are available.
\end{messagebox}

\begin{messagebox}{CRC as a hashing algorithm}{attentioncolour}{\icattention}{black}
\arara\ internally relies on a CRC32 implementation for file hashing. This particular choice, although not designed for hashing, offers an interesting trade-off between speed and quality. Besides, since it is not computationally expensive as strong algorithms such as MD5 and SHA1, CRC32 can be used for hashing typical \TeX\ documents and plain text files with little to no collisions.
\end{messagebox}

\item[\mddbox{C}{R}{changed(String extension)}{boolean}] This method returns a boolean value according to whether the base name of the \mtbox{currentFile} reference (i.e, the name without the associated extension) as a string concatenated with the provided \rbox{String} extension has changed since last verification, based on a traditional cyclic redundancy check. The file reference, as well as the associated hash, is stored in a YAML database file named \rbox{arara.yaml} located in the same directory as the current file (the database name can be overridden in the configuration file, as discussed in Section~\ref{sec:basicstructure}, on page~\pageref{sec:basicstructure}). The method semantics (including the return values) is presented as follows.

\vspace{1em}

{\centering\small
\setlength\tabcolsep{0.8em}
\begin{tabular}{@{}ccccc@{}}
\toprule
\emph{file exists?} & \emph{entry exists?} &
\emph{has changed?} & \emph{DB action} &
\emph{result} \\
\midrule
\cbyes{-2} & \cbyes{-2} & \cbyes{-2} & update & \cbyes{-2} \\
\cbyes{-2} & \cbyes{-2} & \cbno{-2} & --- & \cbno{-2} \\
\cbyes{-2} & \cbno{-2} & --- & insert & \cbyes{-2} \\
\cbno{-2} & \cbno{-2} & --- & --- & \cbno{-2} \\
\cbno{-2} & \cbyes{-2} & --- & remove & \cbyes{-2} \\
\bottomrule
\end{tabular}\par}

\vspace{1.4em}

It is important to observe that this method \emph{always} performs a database operation, either an insertion, removal or update on the corresponding entry. When using \mtbox{changed} within a logical expression, make sure the evaluation order is correct, specially regarding the use of short-circuiting operations. In some scenarios, order does matter.

\begin{codebox}{Example}{teal}{\icnote}{white}
% arara: pdflatex if changed('tex')
\end{codebox}

\item[\mddbox{C}{R}{unchanged(File file)}{boolean}] This method returns a boolean value according to whether the provided \rbox{File} reference has not changed since last verification, based on a traditional cyclic redundancy check. The file reference, as well as the associated hash, is stored in a YAML database file named \rbox{arara.yaml} located in the same directory as the current file (the database name can be overridden in the configuration file, as discussed in Section~\ref{sec:basicstructure}, on page~\pageref{sec:basicstructure}). The method semantics (including the return values) is presented as follows.

\vspace{1em}

{\centering\small
\setlength\tabcolsep{0.8em}
\begin{tabular}{@{}ccccc@{}}
\toprule
\emph{file exists?} & \emph{entry exists?} &
\emph{has changed?} & \emph{DB action} &
\emph{result} \\
\midrule
\cbyes{-2} & \cbyes{-2} & \cbyes{-2} & update & \cbno{-2} \\
\cbyes{-2} & \cbyes{-2} & \cbno{-2} & --- & \cbyes{-2} \\
\cbyes{-2} & \cbno{-2} & --- & insert & \cbno{-2} \\
\cbno{-2} & \cbno{-2} & --- & --- & \cbyes{-2} \\
\cbno{-2} & \cbyes{-2} & --- & remove & \cbno{-2} \\
\bottomrule
\end{tabular}\par}

\vspace{1.4em}

It is important to observe that this method \emph{always} performs a database operation, either an insertion, removal or update on the corresponding entry. When using \mtbox{unchanged} within a logical expression, make sure the evaluation order is correct, specially regarding the use of short-circuiting operations. In some scenarios, order does matter.

\begin{codebox}{Example}{teal}{\icnote}{white}
% arara: pdflatex if !unchanged(toFile('thesis.tex'))
\end{codebox}

\item[\mddbox{C}{R}{unchanged(String extension)}{boolean}] This method returns a boolean value according to whether the base name of the \mtbox{currentFile} reference (i.e, the name without the associated extension) as a string concatenated with the provided \rbox{String} extension has not changed since last verification, based on a traditional cyclic redundancy check. The file reference, as well as the associated hash, is stored in a YAML database file named \rbox{arara.yaml} located in the same directory as the current file (the database name can be overridden in the configuration file, as discussed in Section~\ref{sec:basicstructure}, on page~\pageref{sec:basicstructure}). The method semantics (including the return values) is presented as follows.

\vspace{1em}

{\centering\small
\setlength\tabcolsep{0.8em}
\begin{tabular}{@{}ccccc@{}}
\toprule
\emph{file exists?} & \emph{entry exists?} &
\emph{has changed?} & \emph{DB action} &
\emph{result} \\
\midrule
\cbyes{-2} & \cbyes{-2} & \cbyes{-2} & update & \cbno{-2} \\
\cbyes{-2} & \cbyes{-2} & \cbno{-2} & --- & \cbyes{-2} \\
\cbyes{-2} & \cbno{-2} & --- & insert & \cbno{-2} \\
\cbno{-2} & \cbno{-2} & --- & --- & \cbyes{-2} \\
\cbno{-2} & \cbyes{-2} & --- & remove & \cbno{-2} \\
\bottomrule
\end{tabular}\par}

\vspace{1.4em}

It is important to observe that this method \emph{always} performs a database operation, either an insertion, removal or update on the corresponding entry. When using \mtbox{unchanged} within a logical expression, make sure the evaluation order is correct, specially regarding the use of short-circuiting operations. In some scenarios, order does matter.

\begin{codebox}{Example}{teal}{\icnote}{white}
% arara: pdflatex if !unchanged('tex')
\end{codebox}

\item[\mdbox{R}{writeToFile(File file, String text, boolean append)}{boolean}] This method performs a write operation based on the provided parameters. In this case, the method writes the \rbox{String} text to the \rbox{File} reference and returns a boolean value according to whether the operation was successful. The third parameter holds a \rbox{boolean} value and acts as a switch indicating whether the text should be appended to the existing content of the provided file. Keep in mind that the existing content of a file is always overwritten if this switch is disabled. Also, note that the switch has no effect if the file is being created at that moment. It is important to observe that this method does not raise any exception.

\begin{codebox}{Example}{teal}{\icnote}{white}
result = writeToFile(toFile('foo.txt'), 'hello world', false);
\end{codebox}

\begin{messagebox}{Read and write operations in Unicode}{attentioncolour}{\icattention}{black}
\arara\ \emph{always} uses Unicode as the encoding format for read and write operations. This decision is deliberate as a means to offer a consistent representation and handling of text. Unicode can be implemented by different character encodings. In our case, the tool relies on UTF-8, which uses one byte for the first 128 code points, and up to 4 bytes for other characters. The first 128 Unicode code points are the ASCII characters, which means that any ASCII text is also UTF-8 text.
\end{messagebox}

\begin{messagebox}{File system permissions}{attentioncolour}{\icattention}{black}
Most file systems have methods to assign permissions or access rights to specific users and groups of users. These permissions control the ability of the users to view, change, navigate, and execute the contents of the file system. Keep in mind that read and write operations depend on such permissions.
\end{messagebox}

\item[\mdbox{R}{writeToFile(String reference, String text, boolean append)}{boolean}] This method performs a write operation based on the provided parameters. In this case, the method writes the \rbox{String} text to the \rbox{String} reference and returns a boolean value according to whether the operation was successful. The third parameter holds a \rbox{boolean} value and acts as a switch indicating whether the text should be appended to the existing content of the provided file. Keep in mind that the existing content of a file is always overwritten if this switch is disabled. Also, note that the switch has no effect if the file is being created at that moment. It is important to observe that this method does not raise any exception.

\begin{codebox}{Example}{teal}{\icnote}{white}
result = writeToFile('foo.txt', 'hello world', false);
\end{codebox}

\item[\mdbox{R}{writeToFile(File file, List<String> lines, boolean append)}{boolean}] This method performs a write operation based on the provided parameters. In this case, the method writes the \rbox{List<String>} lines to the \rbox{File} reference and returns a boolean value according to whether the operation was successful. The third parameter holds a \rbox{boolean} value and acts as a switch indicating whether the text should be appended to the existing content of the provided file. Keep in mind that the existing content of a file is always overwritten if this switch is disabled. Also, note that the switch has no effect if the file is being created at that moment. It is important to observe that this method does not raise any exception.

\begin{codebox}{Example}{teal}{\icnote}{white}
result = writeToFile(toFile('foo.txt'),
         [ 'hello world', 'how are you?' ], false);
\end{codebox}

\item[\mdbox{R}{\parbox{0.51\textwidth}{writeToFile(String reference,\\\hspace*{1em} List<String> lines, boolean append)}}{boolean}] This method performs a write operation based on the provided parameters. In this case, the method writes the \rbox{List<String>} lines to the \rbox{String} reference and returns a boolean value according to whether the operation was successful. The third parameter holds a \rbox{boolean} value and acts as a switch indicating whether the text should be appended to the existing content of the provided file. Keep in mind that the existing content of a file is always overwritten if this switch is disabled. Also, note that the switch has no effect if the file is being created at that moment. It is important to observe that this method does not raise any exception.

\begin{codebox}{Example}{teal}{\icnote}{white}
result = writeToFile('foo.txt', [ 'hello world',
         'how are you?' ], false);
\end{codebox}

\item[\mdbox{R}{readFromFile(File file)}{List<String>}] This method performs a read operation based on the provided parameter. In this case, the method reads the content from the \rbox{File} reference and returns a \rbox{List<String>} object representing the lines as a list of strings. If the reference does not exist or an exception is raised due to access permission constraints, the \mtbox{readFromFile} method returns an empty list. Keep in mind that, as a design decision, UTF-8 is \emph{always} used as character encoding for read operations.

\begin{codebox}{Example}{teal}{\icnote}{white}
lines = readFromFile(toFile('foo.txt'));
\end{codebox}

\item[\mdbox{R}{readFromFile(String reference)}{List<String>}] This method performs a read operation based on the provided parameter. In this case, the method reads the content from the \rbox{String} reference and returns a \rbox{List<String>} object representing the lines as a list of strings. If the reference does not exist or an exception is raised due to access permission constraints, the \mtbox{readFromFile} method returns an empty list. Keep in mind that, as a design decision, UTF-8 is \emph{always} used as character encoding for read operations.

\begin{codebox}{Example}{teal}{\icnote}{white}
lines = readFromFile('foo.txt');
\end{codebox}

\item[\mdbox{R}{\parbox{0.61\textwidth}{listFilesByExtensions(File file,\\\hspace*{1em} List<String> extensions, boolean recursive)}}{List<File>}] This method performs a file search operation based on the provided parameters. In this case, the method list all files from the provided \rbox{File} reference according to the \rbox{List<String>} extensions as a list of strings, and returns a \rbox{List<File>} object representing all matching files. The leading full stop in each extension must be omitted, unless it is part of the search pattern. The third parameter holds a \rbox{boolean} value and acts as a switch indicating whether the search must be recursive, i.e, whether all subdirectories must be searched as well. If the reference is not a proper directory or an exception is raised due to access permission constraints, the \mtbox{listFilesByExtensions} method returns an empty list.

\begin{codebox}{Example}{teal}{\icnote}{white}
files = listFilesByExtensions(toFile('/home/paulo/Documents'),
        [ 'aux', 'log' ], false);
\end{codebox}

\item[\mdbox{R}{\parbox{0.61\textwidth}{listFilesByExtensions(String reference,\\\hspace*{1em} List<String> extensions, boolean recursive)}}{List<File>}] This method performs a file search operation based on the provided parameters. In this case, the method list all files from the provided \rbox{String} reference according to the \rbox{List<String>} extensions as a list of strings, and returns a \rbox{List<File>} object representing all matching files. The leading full stop in each extension must be omitted, unless it is part of the search pattern. The third parameter holds a \rbox{boolean} value and acts as a switch indicating whether the search must be recursive, i.e, whether all subdirectories must be searched as well. If the reference is not a proper directory or an exception is raised due to access permission constraints, the \mtbox{listFilesByExtensions} method returns an empty list.

\begin{codebox}{Example}{teal}{\icnote}{white}
files = listFilesByExtensions('/home/paulo/Documents',
        [ 'aux', 'log' ], false);
\end{codebox}

\item[\mdbox{R}{\parbox{0.59\textwidth}{listFilesByPatterns(File file,\\\hspace*{1em} List<String> patterns, boolean recursive)}}{List<File>}] This method performs a file search operation based on the provided parameters. In this case, the method lists all files from the provided \rbox{File} reference according to the \rbox{List<String>} patterns as a list of strings, and returns a \rbox{List<File>} object representing all matching files. The pattern specification is described below. The third parameter holds a \rbox{boolean} value and acts as a switch indicating whether the search must be recursive, i.e, whether all subdirectories must be searched as well. If the reference is not a proper directory or an exception is raised due to access permission constraints, the \mtbox{listFilesByPatterns} method returns an empty list. It is very important to observe that this file search operation might be slow depending on the provided directory. It is highly advisable to not rely on recursive searches whenever possible.

\begin{messagebox}{Patterns for file search operations}{araracolour}{\icattention}{white}
\arara\ employs wildcard filters as patterns for file search operations. Testing is case sensitive by default. The wildcard matcher uses the characters \rbox[araracolour]{?} and \rbox[araracolour]{*} to represent a single or multiple wildcard characters. This is the same as often found on typical terminals.
\end{messagebox}

\begin{codebox}{Example}{teal}{\icnote}{white}
files = listFilesByPatterns(toFile('/home/paulo/Documents'),
        [ '*.tex', 'foo?.txt' ], false);
\end{codebox}


\item[\mdbox{R}{\parbox{0.59\textwidth}{listFilesByPatterns(String reference,\\\hspace*{1em} List<String> patterns, boolean recursive)}}{List<File>}] This method performs a file search operation based on the provided parameters. In this case, the method lists all files from the provided \rbox{String} reference according to the \rbox{List<String>} patterns as a list of strings, and returns a \rbox{List<File>} object representing all matching files. The pattern specification follows a wildcard filter. The third parameter holds a \rbox{boolean} value and acts as a switch indicating whether the search must be recursive, i.e, whether all subdirectories must be searched as well. If the reference is not a proper directory or an exception is raised due to access permission constraints, the \mtbox{listFilesByPatterns} method returns an empty list. It is very important to observe that this file search operation might be slow depending on the provided directory. It is highly advisable to not rely on recursive searches whenever possible.

\begin{codebox}{Example}{teal}{\icnote}{white}
files = listFilesByPatterns('/home/paulo/Documents',
        [ '*.tex', 'foo?.txt' ], false);
\end{codebox}
\end{description}

As the methods presented in this section have transparent error handling, the writing of rules and conditionals becomes more fluent and not too complex for the typical user.

\section{Conditional flow}
\label{sec:conditionalflow}

This section introduces methods related to conditional flow based on \emph{natural boolean values}, i.e, words that semantically represent truth and falsehood signs. Such concept provides a friendly representation of boolean values and eases the use of switches in directive parameters. The tool relies on the following set of natural boolean values:

\vspace{1em}

{\centering
\setlength\tabcolsep{0.2em}
\begin{tabular}{ccccccccccc}
\cbyes{-2} &
\rbox[araracolour]{\hphantom{w}yes\hphantom{w}} &
\rbox[araracolour]{\hphantom{w}true\hphantom{w}} &
\rbox[araracolour]{\hphantom{w}1\hphantom{w}} &
\rbox[araracolour]{\hphantom{w}on\hphantom{w}} &
\hspace{2em} &
\cbno{-2} &
\rbox[warningcolour]{\hphantom{w}no\hphantom{w}} &
\rbox[warningcolour]{\hphantom{w}false\hphantom{w}} &
\rbox[warningcolour]{\hphantom{w}0\hphantom{w}} &
\rbox[warningcolour]{\hphantom{w}off\hphantom{w}}
\end{tabular}\par}

\vspace{1.4em}

All elements from the provided set of natural boolean values can be used interchangeably in directive parameters. It is important to observe that \arara\ throws an exception if a value absent from the set is provided to the methods described in this section.

\begin{description}
\item[\mdbox{R}{isTrue(String string)}{boolean}] This method returns a boolean value according to whether the provided \rbox{String} value is contained in the sub-set of natural true boolean values. It is worth mentioning that the verification is case insensitive, i.e, upper case and lower case symbols are treated as equivalent. If the provided value is an empty string, the method returns false.

\begin{codebox}{Example}{teal}{\icnote}{white}
result = isTrue('yes');
\end{codebox}

\item[\mdbox{R}{isFalse(String string)}{boolean}] This method returns a boolean value according to whether the provided \rbox{String} value is contained in the sub-set of natural false boolean values. It is worth mentioning that the verification is case insensitive, i.e, upper case and lower case symbols are treated as equivalent. If the provided value is an empty string, the method returns false.

\begin{codebox}{Example}{teal}{\icnote}{white}
result = isFalse('off');
\end{codebox}

\item[\mdbox{R}{isTrue(String string, Object yes)}{Object}] This method checks if the first parameter is contained in the sub-set of natural true boolean values. If the result holds true, the second parameter is returned. Otherwise, an empty string is returned. It is worth mentioning that the verification is case insensitive, i.e, upper case and lower case symbols are treated as equivalent. If the first parameter is an empty string, the method returns an empty string.

\begin{codebox}{Example}{teal}{\icnote}{white}
result = isTrue('on', [ 'ls', '-la' ]);
\end{codebox}

\item[\mdbox{R}{isFalse(String string, Object yes)}{Object}] This method checks if the first parameter is contained in the sub-set of natural false boolean values. If the result holds true, the second parameter is returned. Otherwise, an empty string is returned. It is worth mentioning that the verification is case insensitive, i.e, upper case and lower case symbols are treated as equivalent. If the first parameter is an empty string, the method returns an empty string.

\begin{codebox}{Example}{teal}{\icnote}{white}
result = isFalse('0', 'pwd');
\end{codebox}

\item[\mdbox{R}{isTrue(String string, Object yes, Object no)}{Object}] This method checks if the first parameter is contained in the sub-set of natural true boolean values. If the result holds true, the second parameter is returned. Otherwise, the third parameter is returned. It is worth mentioning that the verification is case insensitive, i.e, upper case and lower case symbols are treated as equivalent. If the first parameter is an empty string, the method returns the third parameter.

\begin{codebox}{Example}{teal}{\icnote}{white}
result = isTrue('on', [ 'ls', '-la' ], 'pwd');
\end{codebox}

\item[\mdbox{R}{isFalse(String string, Object yes, Object no)}{Object}] This method checks if the first parameter is contained in the sub-set of natural false boolean values. If the result holds true, the second parameter is returned. Otherwise, the third parameter is returned. It is worth mentioning that the verification is case insensitive, i.e, upper case and lower case symbols are treated as equivalent. If the first parameter is an empty string, the method returns the third parameter.

\begin{codebox}{Example}{teal}{\icnote}{white}
result = isFalse('0', 'pwd', 'ps');
\end{codebox}

\item[\mdbox{R}{\parbox{0.45\textwidth}{isTrue(String string, Object yes,\\\hspace*{1em} Object no, Object fallback)}}{Object}] This method checks if the first parameter is contained in the sub-set of natural true boolean values. If the result holds true, the second parameter is returned. Otherwise, the third parameter is returned. It is worth mentioning that the verification is case insensitive, i.e, upper case and lower case symbols are treated as equivalent. If the first parameter is an empty string, the method returns the fourth parameter as default value.

\begin{codebox}{Example}{teal}{\icnote}{white}
result = isTrue('on', 'ls', 'pwd', 'who');
\end{codebox}

\item[\mdbox{R}{\parbox{0.46\textwidth}{isFalse(String string, Object yes,\\\hspace*{1em} Object no, Object fallback)}}{Object}] This method checks if the first parameter is contained in the sub-set of natural false boolean values. If the result holds true, the second parameter is returned. Otherwise, the third parameter is returned. It is worth mentioning that the verification is case insensitive, i.e, upper case and lower case symbols are treated as equivalent. If the first parameter is an empty string, the method returns the fourth parameter as default value.

\begin{codebox}{Example}{teal}{\icnote}{white}
result = isFalse('0', 'pwd', 'ps', 'ls');
\end{codebox}

\item[\mdbox{R}{isTrue(boolean value, Object yes)}{Object}] This method evaluates the first parameter as a boolean expression. If the result holds true, the second parameter is returned. Otherwise, an empty string is returned.

\begin{codebox}{Example}{teal}{\icnote}{white}
result = isTrue(1 == 1, 'yes');
\end{codebox}

\item[\mdbox{R}{isFalse(boolean value, Object yes)}{Object}] This method evaluates the first parameter as a boolean expression. If the result holds false, the second parameter is returned. Otherwise, an empty string is returned.

\begin{codebox}{Example}{teal}{\icnote}{white}
result = isFalse(1 != 1, 'yes');
\end{codebox}

\item[\mdbox{R}{isTrue(boolean value, Object yes, Object no)}{Object}] This method evaluates the first parameter as a boolean expression. If the result holds true, the second parameter is returned. Otherwise, the third parameter is returned.

\begin{codebox}{Example}{teal}{\icnote}{white}
result = isTrue(1 == 1, 'yes', 'no');
\end{codebox}

\item[\mdbox{R}{isFalse(boolean value, Object yes, Object no)}{Object}] This method evaluates the first parameter as a boolean expression. If the result holds false, the second parameter is returned. Otherwise, the third parameter is returned.

\begin{codebox}{Example}{teal}{\icnote}{white}
result = isFalse(1 != 1, 'yes', 'no');
\end{codebox}
\end{description}

Supported by the concept of natural boolean values, the methods presented in this section ease the use of switches in directive parameters and can be adopted as valid alternatives for traditional conditional flows, when applied.

\section{Strings}
\label{sec:strings}

String manipulation constitutes one of the foundations of rule interpretation in our tool. This section introduces methods for handling such types, as a means to offer high level constructs for users.

\begin{description}
\item[\mdbox{R}{isEmpty(String string)}{boolean}] This method returns a boolean value according to whether the provided \rbox{String} value is empty, i.e, the string length is equal to zero.

\begin{codebox}{Example}{teal}{\icnote}{white}
result = isEmpty('not empty');
\end{codebox}

\item[\mdbox{R}{isNotEmpty(String string)}{boolean}] This method returns a boolean value according to whether the provided \rbox{String} value is not empty, i.e, the string length is greater than zero.

\begin{codebox}{Example}{teal}{\icnote}{white}
result = isNotEmpty('not empty');
\end{codebox}

\item[\mdbox{R}{isEmpty(String string, Object yes)}{boolean}] This method checks if the first parameter is empty, i.e, if the string length is equal to zero. If the result holds true, the second parameter is returned. Otherwise, an empty string is returned.

\begin{codebox}{Example}{teal}{\icnote}{white}
result = isEmpty('not empty', 'ps');
\end{codebox}

\item[\mdbox{R}{isNotEmpty(String string, Object yes)}{boolean}] This method checks if the first parameter is not empty, i.e, if the string length is greater than zero. If the result holds true, the second parameter is returned. Otherwise, an empty string is returned.

\begin{codebox}{Example}{teal}{\icnote}{white}
result = isNotEmpty('not empty', 'ls');
\end{codebox}

\item[\mdbox{R}{isEmpty(String string, Object yes, Object no)}{boolean}] This method checks if the first parameter is empty, i.e, if the string length is equal to zero. If the result holds true, the second parameter is returned. Otherwise, the third parameter is returned.

\begin{codebox}{Example}{teal}{\icnote}{white}
result = isEmpty('not empty', 'ps', 'ls');
\end{codebox}

\item[\mdbox{R}{isNotEmpty(String string, Object yes, Object no)}{boolean}] This method checks if the first parameter is not empty, i.e, if the string length is greater than zero. If the result holds true, the second parameter is returned. Otherwise, the third parameter is returned.

\begin{codebox}{Example}{teal}{\icnote}{white}
result = isNotEmpty('not empty', 'ls', 'ps');
\end{codebox}

\item[\mdbox{R}{buildString(Object... objects)}{String}] This method returns a string based on the provided array of objects, separating each element by one blank space. It is important to observe that empty values are not considered. Also, note that the object array is denoted by a  comma-separated sequence of elements in the actual method call, resulting in a variable number of parameters.

\begin{codebox}{Example}{teal}{\icnote}{white}
result = buildString('a', 'b', 'c', 'd');
\end{codebox}

\item[\mdbox{R}{trimSpaces(String string)}{String}] This method trims spaces from the provided parameter, i.e, leading and trailing spaces in the \rbox{String} reference are removed, and returns the resulting string. It is important to observe that non-boundary spaces inside the string are not removed at all.

\begin{codebox}{Example}{teal}{\icnote}{white}
result = trimSpaces('   hello world   ');
\end{codebox}

\item[\mdbox{R}{replicatePattern(String pattern, List<Object> values)}{List<Object>}] This method replicates the provided pattern to each element of the second parameter and returns the resulting list. The pattern must contain exactly one placeholder. For instance, \rbox{\%s} denotes a string representation of the provided argument. Please refer to the \rbox{Formatter} class reference in the \href{https://docs.oracle.com/javase/7/docs/api/java/util/Formatter.html}{Java documentation} for more information on placeholders. This method raises an exception if an invalid pattern is applied.

\begin{codebox}{Example}{teal}{\icnote}{white}
names = replicatePattern('My name is %s', [ 'Brent', 'Nicola' ]);
\end{codebox}

\item[\mddbox{C}{R}{found(File file, String regex)}{boolean}] This method returns a boolean value according to whether the content of the provided \rbox{File} reference contains at least one match of the provided \rbox{String} regular expression. It is important to observe that this method raises an exception if an invalid regular expression is provided as the parameter or if the provided file reference does not exist.

\begin{codebox}{Example}{teal}{\icnote}{white}
% arara: pdflatex while found(toFile('article.log'),
% arara: --> 'undefined references')
\end{codebox}

\item[\mddbox{C}{R}{found(String extension, String regex)}{boolean}] This method returns a boolean value according to whether the content of the base name of the \mtbox{currentFile} reference (i.e, the name without the associated extension) as a string concatenated with the provided \rbox{String} extension contains at least one match of the provided \rbox{String} regular expression. It is important to observe that this method raises an exception if an invalid regular expression is provided as the parameter or if the provided file reference does not exist.

\begin{codebox}{Example}{teal}{\icnote}{white}
% arara: pdflatex while found('log', 'undefined references')
\end{codebox}
\end{description}

The string manipulation methods presented in this section constitute an interesting and straightforward approach to handling directive parameters without the usual verbosity in writing typical Java constructs.

\section{Operating systems}
\label{sec:operatingsystems}

This section introduces methods related to the underlying operating system detection, as a means of providing a straightforward approach to writing cross-platform rules.

\begin{description}
\item[\mdbox{R}{isWindows()}{boolean}] This method returns a boolean value according to whether the underlying operating system vendor is Microsoft Windows.

\begin{codebox}{Example}{teal}{\icnote}{white}
if (isWindows()) { System.out.println('Running Windows.'); }
\end{codebox}

\item[\mdbox{R}{isLinux()}{boolean}] This method returns a boolean value according to whether the underlying operating system vendor is a Linux instance.

\begin{codebox}{Example}{teal}{\icnote}{white}
if (isLinux()) { System.out.println('Running Linux.'); }
\end{codebox}

\item[\mdbox{R}{isMac()}{boolean}] This method returns a boolean value according to whether the underlying operating system vendor is Apple Mac OS.

\begin{codebox}{Example}{teal}{\icnote}{white}
if (isMac()) { System.out.println('Running Mac OS.'); }
\end{codebox}

\item[\mdbox{R}{isUnix()}{boolean}] This method returns a boolean value according to whether the underlying operating system vendor is any Unix variation.

\begin{codebox}{Example}{teal}{\icnote}{white}
if (isUnix()) { System.out.println('Running Unix.'); }
\end{codebox}

\item[\mdbox{R}{isCygwin()}{boolean}] This method returns a boolean value according to whether the underlying operating system vendor is Microsoft Windows and \arara\ is being executed inside a Cygwin environment.

\begin{codebox}{Example}{teal}{\icnote}{white}
if (isCygwin()) { System.out.println('Running Cygwin.'); }
\end{codebox}

\begin{messagebox}{Cygwin paths}{attentioncolour}{\icattention}{black}
It is worth mentioning that Cygwin has its own path handling which is not reliable when (JVM) applications need to invoke system commands. For instance, the following invocation does not work:

\begin{codebox}{Example}{teal}{\icnote}{white}
% arara: pdflatex
\end{codebox}

Since \rbox{pdflatex} is a symbolic link to \rbox{pdftex} with the proper format, we encourage Cygwin users to rely on the following trick:

\begin{codebox}{Example}{teal}{\icnote}{white}
% arara: pdftex: { options: [ '--fmt=pdflatex' ] }
\end{codebox}

The same trick can be employed by other \TeX\ engines with appropriate \rbox{--fmt} flags as well. We are still investigating this issue and looking for potential alternatives.
\end{messagebox}

\item[\mdbox{R}{isWindows(Object yes, Object no)}{Object}] This method checks if the underlying operating system vendor is Microsoft Windows. If the result holds true, the first parameter is returned. Otherwise, the second parameter is returned.

\begin{codebox}{Example}{teal}{\icnote}{white}
command = isWindows('del', 'rm');
\end{codebox}

\item[\mdbox{R}{isLinux(Object yes, Object no)}{Object}] This method checks if the underlying operating system vendor is a Linux instance. If the result holds true, the first parameter is returned. Otherwise, the second parameter is returned.

\begin{codebox}{Example}{teal}{\icnote}{white}
command = isLinux('rm', 'del');
\end{codebox}

\item[\mdbox{R}{isMac(Object yes, Object no)}{Object}] This method checks if the underlying operating system vendor is Apple Mac OS. If the result holds true, the first parameter is returned. Otherwise, the second parameter is returned.

\begin{codebox}{Example}{teal}{\icnote}{white}
command = isMac('ls', 'dir');
\end{codebox}

\item[\mdbox{R}{isUnix(Object yes, Object no)}{Object}] This method checks if the underlying operating system vendor is any Unix variation. If the result holds true, the first parameter is returned. Otherwise, the second parameter is returned.

\begin{codebox}{Example}{teal}{\icnote}{white}
command = isUnix('tree', 'dir');
\end{codebox}

\item[\mdbox{R}{isCygwin(Object yes, Object no)}{Object}] This method checks if the underlying operating system vendor is Microsoft Windows and if \arara\ is being executed inside a Cygwin environment. If the result holds true, the first parameter is returned. Otherwise, the second parameter is returned.

\begin{codebox}{Example}{teal}{\icnote}{white}
command = isCygwin('ls', 'dir');
\end{codebox}
\end{description}

The methods presented in the section provide useful information to help users write cross-platform rules and thus enhance the automation experience based on specific features of the underlying operating system.

\section{Type checking}
\label{sec:typechecking}

In certain scenarios, a plain string representation of directive parameters might be inadequate or insufficient given the rule requirements. To this end, this section introduces methods related to type checking as a means to provide support and verification for common data types.

\begin{description}
\item[\mdbox{R}{isString(Object object)}{boolean}] This method returns a boolean value according to whether the provided \rbox{Object} object is a string or any extended type.

\begin{codebox}{Example}{teal}{\icnote}{white}
result = isString('foo');
\end{codebox}

\item[\mdbox{R}{isList(Object object)}{boolean}] This method returns a boolean value according to whether the provided \rbox{Object} object is a list or any extended type.

\begin{codebox}{Example}{teal}{\icnote}{white}
result = isList([ 1, 2, 3 ]);
\end{codebox}

\item[\mdbox{R}{isMap(Object object)}{boolean}] This method returns a boolean value according to whether the provided \rbox{Object} object is a map or any extended type.

\begin{codebox}{Example}{teal}{\icnote}{white}
result = isMap([ 'Paulo' : 'Palmeiras', 'Carla' : 'Inter' ]);
\end{codebox}

\item[\mdbox{R}{isBoolean(Object object)}{boolean}] This method returns a boolean value according to whether the provided \rbox{Object} object is a boolean or any extended type.

\begin{codebox}{Example}{teal}{\icnote}{white}
result = isBoolean(false);
\end{codebox}

\item[\mdbox{R}{checkClass(Class clazz, Object object)}{boolean}] This method returns a boolean value according to whether the provided \rbox{Object} object is an instance or a subtype of the provided \rbox{Class} class. It is interesting to note that all methods presented in this section internally rely on \mtbox{checkClass} for type checking.

\begin{codebox}{Example}{teal}{\icnote}{white}
result = checkClass(List.class, [ 'a', 'b' ]);
\end{codebox}
\end{description}

The methods presented in this section cover the most common types used in directive parameters and should suffice for expressing the rule requirements. If a general approach is needed, please refer to the \mtbox{checkClass} method for checking virtually any type available in the Java environment.

\section{Classes and objects}
\label{sec:classesandobjects}

\arara\ can be extended at runtime with code from JVM languages, such as Groovy, Scala, Clojure and Kotlin. The tool can load classes from \rbox{class} and \rbox{jar} files and even instantiate them. This section introduces methods related to class loading and object instantiation.

\begin{messagebox}{Ordered pairs}{araracolour}{\icok}{white}
According to the \href{https://en.wikipedia.org/wiki/Ordered_pair}{Wikipedia entry}, in mathematics, an \emph{ordered pair} $(a, b)$ is a pair of objects. The order in which the objects appear in the pair is significant: the ordered pair $(a, b)$ is different from the ordered pair $(b, a)$ unless $a = b$. In the ordered pair $(a, b)$, the object $a$ is called the \emph{first} entry, and the object $b$ the \emph{second} entry of the pair. \arara\ relies on this concept with the helper \rbox{Pair<A, B>} class, in which \rbox{A} and \rbox{B} denote the component classes, i.e, the types associated to the pair elements. In order to access the pair entries, the class provides two property accessors:

\begin{description}
\item[\mtbox{first}\hfill\rrbox{A}] This property accessor, as the name implies, returns the first entry of the ordered pair, as an \rbox{A} object.

\item[\mtbox{second}\hfill\rrbox{B}] This property accessor, as the name implies, returns the second entry of the ordered pair, as a \rbox{B} object.
\end{description}

Keep in mind that the entries in the \rbox{Pair} class, once defined, cannot be modified to other values. The initial values are set during instantiation and, therefore, only entry getters are available to the user during the object life cycle.
\end{messagebox}

\begin{messagebox}{Status for class loading and instantiation}{araracolour}{\icok}{white}
The class loading and instantiation methods provided by \arara\ typically return a pair composed of an integer value and a class or object reference. This integer value acts as a status of the underlying operation itself and might indicate potential issues. The possible values are:

\vspace{1em}

{\centering
\def\arraystretch{1.5}
\begin{tabular}{lllll}
\rbox[araracolour]{\hphantom{x}0\hphantom{x}} & Successful execution & \hspace{1.5em} &
\rbox[araracolour]{\hphantom{x}3\hphantom{x}} & Class was not found \\
\rbox[araracolour]{\hphantom{x}1\hphantom{x}} & File does not exist & &
\rbox[araracolour]{\hphantom{x}4\hphantom{x}} & Access policy violation \\
\rbox[araracolour]{\hphantom{x}2\hphantom{x}} & File URL is incorrect & &
\rbox[araracolour]{\hphantom{x}5\hphantom{x}} & Instantiation exception
\end{tabular}\par}

\vspace{1.4em}

Please make sure to \emph{always} check the returned integer status when using class loading and instantiation methods in directive and rule contexts. This feature is quite powerful yet tricky and subtle!
\end{messagebox}

\begin{description}
\item[\mddbox{C}{R}{\parbox{0.32\textwidth}{loadClass(File file,\\ \hspace*{1em}String name)}}{\parbox{0.43\textwidth}{Pair<ClassLoading.\\
\hspace*{1em}ClassLoadingStatus, Object>}}] This method loads a class based on the canonical name from the provided \rbox{File} reference and returns an ordered pair containing the status and the class reference itself. The file must contain the Java bytecode, either directly accessible from a \rbox{class} file or packaged inside a \rbox{jar} file. If an exception is raised, this method returns the \rbox{Object} class reference as second entry of the pair.

\begin{codebox}{Example}{teal}{\icnote}{white}
result = loadClass(toFile('mymath.jar'),
         'com.github.cereda.mymath.Arithmetic');
\end{codebox}

\item[\mddbox{C}{R}{\parbox{0.32\textwidth}{loadClass(String ref,\\ \hspace*{1em}String name)}}{\parbox{0.43\textwidth}{Pair<ClassLoading.\\
\hspace*{1em}ClassLoadingStatus, Object>}}] This method loads a class based on the canonical name from the provided \rbox{String} reference and returns an ordered pair containing the status and the class reference itself. The file must contain the Java bytecode, either directly accessible from a \rbox{class} file or packaged inside a \rbox{jar} file.  If an exception is raised, this method returns the \rbox{Object} class reference as second entry of the pair.

\begin{codebox}{Example}{teal}{\icnote}{white}
result = loadClass('mymath.jar',
         'com.github.cereda.mymath.Arithmetic');
\end{codebox}

\item[\mddbox{C}{R}{\parbox{0.32\textwidth}{loadObject(File file,\\ \hspace*{1em}String name)}}{\parbox{0.43\textwidth}{Pair<ClassLoading.\\
\hspace*{1em}ClassLoadingStatus, Object>}}] This method loads a class based on the canonical name from the provided \rbox{File} reference and returns an ordered pair containing the status and a proper corresponding object instantiation. The file must contain the Java bytecode, either directly accessible from a \rbox{class} file or packaged inside a \rbox{jar} file. If an exception is raised, this method returns an \rbox{Object} object as second entry of the pair.

\begin{codebox}{Example}{teal}{\icnote}{white}
result = loadObject(toFile('mymath.jar'),
         'com.github.cereda.mymath.Trigonometric');
\end{codebox}

\item[\mddbox{C}{R}{\parbox{0.32\textwidth}{loadObject(String ref,\\ \hspace*{1em}String name)}}{\parbox{0.43\textwidth}{Pair<ClassLoading.\\
\hspace*{1em}ClassLoadingStatus, Object>}}] This method loads a class based on the canonical name from the provided \rbox{String} reference and returns an ordered pair containing the status and a proper corresponding object instantiation. The file must contain the Java bytecode, either directly accessible from a \rbox{class} file or packaged inside a \rbox{jar} file. If an exception is raised, this method returns an \rbox{Object} object as second entry of the pair.

\begin{codebox}{Example}{teal}{\icnote}{white}
result = loadObject('mymath.jar',
         'com.github.cereda.mymath.Trigonometric');
\end{codebox}
\end{description}

This section presented class loading and instantiation methods which may significantly enhance the expressiveness of rules and directives. However, make sure to use such feature with great care and attention.

\section{Dialog boxes}
\label{sec:dialogboxes}

A \emph{dialog box} is a graphical control element, typically a small window, that communicates information to the user and prompts them for a response. This section introduces UI methods related to such interactions.

\begin{messagebox}{UI elements}{araracolour}{\icok}{white}
The graphical elements are provided by the Swing toolkit from the Java runtime environment. Note that the default look and feel class reference can be modified through a key in the configuration file, as seen in Section~\ref{sec:basicstructure}, on page~\pageref{sec:basicstructure}. It is important to observe that the methods presented in this section require a graphical interface. If \arara\ is being executed in a headless environment (i.e, an environment with no graphical display available), an exception will be thrown when trying to use such UI methods in either directive or rule contexts.
\end{messagebox}

Each dialog box provided by the UI methods of \arara\ requires the specification of an associated icon. An \emph{icon} is a pictogram displayed on a computer screen in order to help the user quickly identify the message by conveying its meaning through a visual resemblance to a physical object. Our tool features five icons, illustrated below, to be used with dialog boxes. Observe that each icon is associated with a unique integer value which is provided later on to the actual method call. Also, it is worth mentioning that the visual appearance of such icons is based on the underlying Java virtual machine and the current look and feel, so your mileage might vary.

\vspace{1em}

{\centering
\begin{tabularx}{0.8\textwidth}{YYYYY}
\uierror{cyan} &
\uiinfo{cyan} &
\uiwarning{cyan} &
\uiquestion{cyan} &
\uiplain{cyan} \\
{\footnotesize\emph{error}} &
{\footnotesize\emph{information}} &
{\footnotesize\emph{attention}} &
{\footnotesize\emph{question}} &
{\footnotesize\emph{plain}} \\
\rbox[cyan]{\hphantom{ww}1\hphantom{ww}} &
\rbox[cyan]{\hphantom{ww}2\hphantom{ww}} &
\rbox[cyan]{\hphantom{ww}3\hphantom{ww}} &
\rbox[cyan]{\hphantom{ww}4\hphantom{ww}} &
\rbox[cyan]{\hphantom{ww}5\hphantom{ww}}
\end{tabularx}\par}

\vspace{1.4em}

As good practice, make sure to provide descriptive messages to be placed in dialog boxes in order to ease and enhance the user experience. It is also highly advisable to always provide an associated icon, so avoid the plain option whenever possible.

\begin{messagebox}{Message text width}{araracolour}{\icok}{white}
\arara\ sets the default message text width to 250 pixels. Feel free to override this value according to your needs. Please refer to the appropriate method signatures for specifying a new width.
\end{messagebox}

The UI method signatures are followed by a visual representation of the provided dialog box. For the sake of simplicity, each parameter index refers to the associated number in the figure.

\begin{description}
\item[\mdbox{R}{showMessage(int width, int icon, String title, String text)}{void}]\uimethod{messagebox1}

This method shows a message box according to the provided parameters. The dialog box is disposed when the user either presses the confirmation button or closes the window. It is important to observe that \arara\ temporarily interrupts the execution and waits for the dialog box disposal. Also note that the total time includes the idle period as well.

\begin{codebox}{Example}{teal}{\icnote}{white}
showMessage(250, 2, 'My title', 'My message');
\end{codebox}

\item[\mdbox{R}{showMessage(int icon, String title, String text)}{void}]\uimethod{messagebox2}

This method shows a message box according to the provided parameters. The dialog box is disposed when the user either presses the confirmation button or closes the window. It is important to observe that \arara\ temporarily interrupts the execution and waits for the dialog box disposal. Also note that the total time includes the idle period as well.

\begin{codebox}{Example}{teal}{\icnote}{white}
showMessage(2, 'My title', 'My message');
\end{codebox}

\item[\mddbox{C}{R}{\parbox{0.62\textwidth}{showOptions(int width, int icon, String title,\\\hspace*{1em} String text, Object... options)}}{int}]\uimethod{optionbox1}

This method shows a message box according to the provided parameters, including options represented as an array of \rbox{Object} objects. This array is portrayed in the dialog box as a list of buttons. The dialog box is disposed when the user either presses one of the buttons or closes the window. The method returns the natural index of the selected button, starting from \rbox{1}. If no button is pressed (e.g, the window is closed), \rbox{0} is returned. Note that the object array is denoted by a  comma-separated sequence of elements in the actual method call, resulting in a variable number of parameters. It is important to observe that \arara\ temporarily interrupts the execution and waits for the dialog box disposal. Also note that the total time includes the idle period as well.

\begin{codebox}{Example}{teal}{\icnote}{white}
% arara: pdflatex if showOptions(250, 4, 'Important!',
% arara: --> 'Do you like ice cream?', 'Yes!', 'No!') == 1
\end{codebox}

\begin{messagebox}{Button orientation}{attentioncolour}{\icattention}{black}
Keep in mind that your window manager might render the button orientation differently than the original arrangement specified in your array of objects. For instance, I had a window manager that rendered the buttons in the reverse order. However, note that the visual appearance should not interfere with the programming logic! The indices shall remain the same, pristine as ever, regardless of the actual rendering. Trust your code, not your eyes.
\end{messagebox}

\item[\mddbox{C}{R}{\parbox{0.49\textwidth}{showOptions(int icon, String title,\\\hspace*{1em} String text, Object... options)}}{int}]\uimethod{optionbox2}

This method shows a message box according to the provided parameters, including options represented as an array of \rbox{Object} objects. This array is portrayed in the dialog box as a list of buttons. The dialog box is disposed when the user either presses one of the buttons or closes the window. The method returns the natural index of the selected button, starting from \rbox{1}. If no button is pressed (e.g, the window is closed), \rbox{0} is returned. Note that the object array is denoted by a  comma-separated sequence of elements in the actual method call, resulting in a variable number of parameters. It is important to observe that \arara\ temporarily interrupts the execution and waits for the dialog box disposal. Also note that the total time includes the idle period as well.

\begin{codebox}{Example}{teal}{\icnote}{white}
% arara: pdflatex if showOptions(4, 'Important!',
% arara: --> 'Do you like ice cream?', 'Yes!', 'No!') == 1
\end{codebox}

\item[\mddbox{C}{R}{\parbox{0.62\textwidth}{showDropdown(int width, int icon, String title,\\\hspace*{1em} String text, Object... options)}}{int}]\uimethod{dropdown1}

This method shows a dialog box according to the provided parameters, including options represented as an array of \rbox{Object} objects. This array is portrayed in the dialog box as a dropdown list. The first element from the array is automatically selected. The dialog box is disposed when the user either presses one of the buttons or closes the window. The method returns the natural index of the selected item, starting from \rbox{1}. If the user cancels the dialog or closes the window, \rbox{0} is returned.  Note that the object array is denoted by a comma-separated sequence of elements in the actual method call, resulting in a variable number of parameters. It is important to observe that \arara\ temporarily interrupts the execution and waits for the dialog box disposal. Also note that the total time includes the idle period as well.

\begin{codebox}{Example}{teal}{\icnote}{white}
% arara: pdflatex if showDropdown(250, 4, 'Important!',
% arara: --> 'Who deserves the tick?', 'David Carlisle',
% arara: --> 'Enrico Gregorio', 'Joseph Wright',
% arara: --> 'Heiko Oberdiek') == 2
\end{codebox}

\begin{messagebox}{Combo boxes and dropdown lists}{attentioncolour}{\icattention}{black}
According to the \href{https://en.wikipedia.org/wiki/Combo_box}{Wikipedia entry}, a \emph{combo box} is a combination of a dropdown list or list box and a single line editable textbox, allowing the user to either type a value directly or select a value from the list. The term is sometimes used to mean a dropdown list, but in Java, the term is definitely not a synonym! A dropdown list is sometimes clarified with terms such as non-editable combo box to distinguish it from the original definition of a combo box.
\end{messagebox}

\item[\mddbox{C}{R}{\parbox{0.49\textwidth}{showDropdown(int icon, String title,\\\hspace*{1em} String text, Object... options)}}{int}]\uimethod{dropdown2}

This method shows a dialog box according to the provided parameters, including options represented as an array of \rbox{Object} objects. This array is portrayed in the dialog box as a dropdown list. The first element from the array is automatically selected. The dialog box is disposed when the user either presses one of the buttons or closes the window. The method returns the natural index of the selected item, starting from \rbox{1}. If the user cancels the dialog or closes the window, \rbox{0} is returned.  Note that the object array is denoted by a comma-separated sequence of elements in the actual method call, resulting in a variable number of parameters. It is important to observe that \arara\ temporarily interrupts the execution and waits for the dialog box disposal. Also note that the total time includes the idle period as well.

\begin{codebox}{Example}{teal}{\icnote}{white}
% arara: pdflatex if showDropdown(4, 'Important!',
% arara: --> 'Who deserves the tick?', 'David Carlisle',
% arara: --> 'Enrico Gregorio', 'Joseph Wright',
% arara: --> 'Heiko Oberdiek') == 2
\end{codebox}

\begin{messagebox}{Swing toolkit}{araracolour}{\icok}{white}
According to the \href{https://en.wikipedia.org/wiki/Swing_(Java)}{Wikipedia entry}, the Swing toolkit was developed to provide a more sophisticated set of GUI components than the earlier  AWT widget system. Swing provides a look and feel that emulates the look and feel of several platforms, and also supports a pluggable look and feel that allows applications to have a look and feel unrelated to the underlying platform. It has more powerful and flexible components than AWT. In addition to familiar components such as buttons, check boxes and labels, Swing provides several advanced components, such as scroll panes, trees, tables, and lists.
\end{messagebox}

\item[\mddbox{C}{R}{showInput(int width, int icon, String title, String text)}{String}]\uimethod{inputbox1}

This method shows an input dialog box according to the provided parameters. The dialog box is disposed when the user either presses one of the buttons or closes the window. The method returns the content of the input text field, as a trimmed \rbox{String} object. If the user cancels the dialog or closes the window, an empty string is returned. It is important to observe that \arara\ temporarily interrupts the execution and waits for the dialog box disposal. Also note that the total time includes the idle period as well.

\begin{codebox}{Example}{teal}{\icnote}{white}
% arara: pdflatex if showInput(250, 4, 'Important!',
% arara: --> 'Who wrote arara?') == 'Paulo'
\end{codebox}

\item[\mddbox{C}{R}{showInput(int icon, String title, String text)}{String}]\uimethod{inputbox2}

This method shows an input dialog box according to the provided parameters. The dialog box is disposed when the user either presses one of the buttons or closes the window. The method returns the content of the input text field, as a trimmed \rbox{String} object. If the user cancels the dialog or closes the window, an empty string is returned. It is important to observe that \arara\ temporarily interrupts the execution and waits for the dialog box disposal. Also note that the total time includes the idle period as well.

\begin{codebox}{Example}{teal}{\icnote}{white}
% arara: pdflatex if showInput(4, 'Important!',
% arara: --> 'Who wrote arara?') == 'Paulo'
\end{codebox}
\end{description}

The UI methods presented in this section can be used for writing \TeX\ tutorials and assisted compilation workflows based on user interactions, including visual input and feedback through dialog boxes.

\section{Commands}
\label{sec:commands}

\arara\ features the \rbox{Command} object, a new approach for handling system commands based on a high level structure with explicit argument parsing.

\begin{messagebox}{The anatomy of a command}{araracolour}{\icok}{white}
\setlength{\parskip}{1em}
From the user perspective, a \rbox{Command} object is simply a good old list of \rbox{Object} objects, in which the list head (i.e, the first element) is the underlying system command, and the list tail (i.e, the remaining elements), if any, contains the associated command line arguments. For instance:

{\centering
\setlength\tabcolsep{0.2em}
\begin{tabular}{cccc}
{\footnotesize\em head} &
\multicolumn{3}{c}{\footnotesize\em tail (associated command line arguments)} \\
\rbox[cyan]{pdflatex} &
\rbox[araracolour]{{-}-shell-escape} &
\rbox[araracolour]{{-}-synctex=1} &
\rbox[araracolour]{thesis.tex}
\end{tabular}\par}

\vspace{0.4em}

From the previous example, it is important to observe that a potential file name quoting is not necessary. The underlying system command execution library handles the provided arguments accordingly.

Behind the scenes, however, \arara\ employs a different workflow when constructing a \rbox{Command} object. The tool sets the working directory path for the current command to \abox[araracolour]{USER\_DIR} which is based on the current execution. The working directory path can be explicitly set through specific method calls, described later on in this section.

The list of objects is then completely flattened and all elements are mapped to their string representations through corresponding \mtbox{toString} calls. Finally, the proper \rbox{Command} object is constructed. Keep in mind that, although a command takes a list (or even an array) of objects, which can be of any type, the internal representation is \emph{always} a list of strings.
\end{messagebox}

A list of objects might contain nested lists, i.e, a list within another list. As previously mentioned, \arara\ employs \emph{list flattening} when handling a list of objects during a \rbox{Command} object instantiation. As a means to illustrate this handy feature, consider the following list of integers:

\begin{codebox}{A list with nested lists}{teal}{\icnote}{white}
[ 1, 2, [ 3, 4 ], 5, [ [ 6, 7 ], 8 ], 9, [ [ 10 ] ]
\end{codebox}

Note that the above list of integers contains nested lists. When applying list flattening, \arara\ recursively adds the elements of nested lists to the original list and then removes the nested occurrences. Please refer to the source code for implementation details. The new flattened list is presented as follows.

\begin{codebox}{A flattened list}{teal}{\icnote}{white}
[ 1, 2, 3, 4, 5, 6, 7, 8, 9, 10 ]
\end{codebox}

List flattening and string mapping confer expressiveness and flexibility to the \rbox{Command} object construction, as users can virtually use any data type to describe the underlying rule logic and yet obtain a consistent representation.

\begin{description}
\item[\mdbox{R}{getCommand(List<String> elements)}{Command}] This method, as the name implies, returns a \rbox{Command} object according to the provided list of \rbox{String} elements. If the list is empty, the tool will ignore the execution.

\begin{codebox}{Example}{teal}{\icnote}{white}
return getCommand([ 'ls', '-l' ]);
\end{codebox}

\item[\mdbox{R}{getCommand(Object... elements)}{Command}] This method, as the name implies, returns a \rbox{Command} object according to the provided array of \rbox{Object} elements. If the array is empty, the tool will ignore the execution. Note that the object array is denoted by a comma-separated sequence of elements in the actual method call, resulting in a variable number of parameters.

\begin{codebox}{Example}{teal}{\icnote}{white}
return getCommand('pdflatex', '--shell-escape', 'thesis.tex');
\end{codebox}

\item[\mdbox{R}{\parbox{0.62\textwidth}{getCommandWithWorkingDirectory(File directory,\\\hspace*{1em} List<String> elements)}}{Command}] This method, as the name implies, sets the working directory based on the provided \rbox{File} reference and returns a proper \rbox{Command} object according to the provided list of \rbox{String} elements. If the list is empty, the tool will ignore the execution.

\begin{codebox}{Example}{teal}{\icnote}{white}
return getCommandWithWorkingDirectory(toFile('/home/paulo'),
       [ 'ls', '-l' ]);
\end{codebox}

\item[\mdbox{R}{\parbox{0.58\textwidth}{getCommandWithWorkingDirectory(String path,\\\hspace*{1em} List<String> elements)}}{Command}] This method, as the name implies, sets the working directory based on the provided \rbox{String} reference and returns a proper \rbox{Command} object according to the provided list of \rbox{String} elements. If the list is empty, the tool will ignore the execution.

\begin{codebox}{Example}{teal}{\icnote}{white}
return getCommandWithWorkingDirectory('/home/paulo',
       [ 'ls', '-l' ]);
\end{codebox}

\item[\mdbox{R}{\parbox{0.62\textwidth}{getCommandWithWorkingDirectory(File directory,\\\hspace*{1em} Object... elements)}}{Command}] This method, as the name implies, sets the working directory based on the provided \rbox{File} reference and returns a proper \rbox{Command} object according to the provided array of \rbox{Object} elements. If the array is empty, the tool will ignore the execution. Note that the object array is denoted by a comma-separated sequence of elements in the actual method call, resulting in a variable number of parameters.

\begin{codebox}{Example}{teal}{\icnote}{white}
return getCommandWithWorkingDirectory(toFile('/home/paulo'),
       'pdflatex', '--shell-escape', 'thesis.tex');
\end{codebox}

\item[\mdbox{R}{\parbox{0.58\textwidth}{getCommandWithWorkingDirectory(String path,\\\hspace*{1em} Object... elements)}}{Command}] This method, as the name implies, sets the working directory based on the provided \rbox{String} reference and returns a proper \rbox{Command} object according to the provided array of \rbox{Object} elements. If the array is empty, the tool will ignore the execution. Note that the object array is denoted by a comma-separated sequence of elements in the actual method call, resulting in a variable number of parameters.

\begin{codebox}{Example}{teal}{\icnote}{white}
return getCommandWithWorkingDirectory('/home/paulo',
       'pdflatex', '--shell-escape', 'thesis.tex');
\end{codebox}
\end{description}

The methods presented in this section constitute the foundations of underlying system command execution. In particular, whenever possible, it is highly advisable to use \rbox{Command} objects through proper \mtbox{getCommand} method calls, as the plain string approach used in previous versions of our tool is marked as deprecated and will be removed in future versions.

\section{Others}
\label{sec:others}

This section introduces assorted methods provided by \arara\ as a means to improve the automation itself with expressive rules and enhance the user experience. Such methods are properly described as follows.

\begin{messagebox}{Session}{araracolour}{\icok}{white}
Rules are designed under the \emph{encapsulation} notion, such that the direct access to internal workings of such structures is restricted. However, as a means to support framework awareness, \arara\ provides a mechanism for data sharing across rule contexts, implemented as a \rbox{Session} object. In practical terms, this particular object is a global, persistent map composed of \rbox{String} keys and \rbox{Object} values available throughout the entire execution. The public methods of a session are described as follows:

\begin{description}
\item[\mtbox{put(String key, Object value)}\hfill\rrbox{void}] This method, as the name implies, inserts an object into the session, indexed by the provided key. Observe that, if the session previously contained a mapping for the provided key, the old value is replaced by the specified value.

\item[\mtbox{remove(String key)}\hfill\rrbox{void}] This method, as the name implies, removes  the mapping for the provided key from the session. Be mindful that an attempt to remove a mapping for a nonexistent key will raise an exception.

\item[\mtbox{contains(String key)}\hfill\rrbox{boolean}] This method, as the name implies, returns a boolean value according to whether the session contains a mapping for the provided key. It is highly advisable to use this method before attempting to remove a mapping from the session.

\item[\mtbox{get(String key)}\hfill\rrbox{Object}] This method, as the name implies, returns the object value to which the specified key is mapped. Be mindful that an attempt to return a value for a nonexistent key will raise an exception.

\item[\mtbox{forget()}\hfill\rrbox{void}] This method, as the name implies, removes all of the existing mappings from the session. The session object will be effectively empty after this call returns.
\end{description}

It is important to observe that the \rbox{Session} object provided by our tool follows the \emph{singleton} pattern, i.e, a software design pattern that restricts the instantiation of a class to one object. Therefore, the same session is consistently shared across rule contexts.
\end{messagebox}

\begin{description}
\item[\mdddbox{C}{E}{R}{getSession()}{Session}] This method, as the name implies, returns the \rbox{Session} object for data sharing across rule contexts. Keep in mind that a session cannot contain duplicate keys. Each key can map to at most one value.

\begin{codebox}{Example}{teal}{\icnote}{white}
name = getSession().get('name');
\end{codebox}

\item[\mdbox{R}{throwError(String message)}{void}] This method deliberately throws an error to be intercepted later on during execution. Consider using such method for an explicit notification about unexpected or unsought scenarios, e.g, wrong parameter types or values. The raised error has an associated message which is displayed in the terminal and added to the log file.

\begin{codebox}{Example}{teal}{\icnote}{white}
options = 'not a list';
if (!isList(options)) {
    throwError('I was expecting a list.');
}
\end{codebox}

\item[\mdbox{R}{isVerboseMode()}{boolean}] This method, as the name implies, returns a boolean value according to whether \arara\ is being executed in verbose mode, enabled through either the \opbox{{-}verbose} command line option or the corresponding key in the configuration file (detailed in Sections~\ref{sec:options} and~\ref{sec:basicstructure}, respectively). Note that the logical negation of such method indicates whether the tool is being executed in silent mode.

\begin{codebox}{Example}{teal}{\icnote}{white}
verbose = isVerboseMode();
\end{codebox}

\item[\mdbox{R}{isOnPath(String name)}{boolean}] This method, as the name implies, returns a boolean value according to whether the provided \rbox{String} reference representing a command name is reachable from the system path.  For portability reasons, there is no need to provide extensions to Microsoft Windows command names, as \arara\ will look for common patterns. This behaviour is expected and by design. However, be mindful that the search is case sensitive.

\begin{codebox}{Example}{teal}{\icnote}{white}
result = isOnPath('pdftex');
\end{codebox}

\begin{messagebox}{Path inspection}{araracolour}{\icok}{white}
According to the \href{https://en.wikipedia.org/wiki/PATH_(variable)}{Wikipedia entry}, \abox[araracolour]{PATH} is an environment variable on Unix-like operating systems and Microsoft Windows, specifying a set of directories where executable programs are located. \arara\ performs a file search operation based on all directories specified in the system path, filtering files by name (and extensions, when in Microsoft Windows). When an exact match is found, the search is concluded. Notwithstanding the great effort, it is very important to note that there is no guarantee that our tool will be able to correctly reach the command in all scenarios.
\end{messagebox}

\item[\mdbox{R}{unsafelyExecuteSystemCommand(Command command)}{Pair<Integer, String>}] This method, which has a very spooky name, unsafely executes the provided \rbox{Command} reference and returns an ordered pair containing the exit status and the command output. Note that, if an exception is raised during the command execution, \rbox{-99} is assigned as exit status and an empty string is defined as command output. Please make sure to always check the returned integer status when using this method.

\begin{codebox}{Example}{teal}{\icnote}{white}
result = unsafelyExecuteSystemCommand(getCommand('ls'));
\end{codebox}

\begin{messagebox}{Hic sunt leones}{attentioncolour}{\icattention}{black}
Please \emph{do not abuse} this method! Keep in mind that this particular feature is included for very specific scenarios in which the command streams are needed ahead of time for proper decision making.
\end{messagebox}

\item[\mdbox{R}{isSubdirectory(File directory)}{boolean}] This method checks whether the provided \rbox{File} reference is a valid subdirectory under the project hierarchy, return a corresponding boolean value. This is a check to impose a possible restriction in the rule scope, so that users can change down to subdirectories in their projects but not up, outside of the root directory.

\begin{codebox}{Example}{teal}{\icnote}{white}
valid = isSubdirectory(toFile('chapters/'));
\end{codebox}

\item[\mdbox{R}{getOrNull(List<String> list, int index)}{String}] This method attempts to retrieve
a list element based on an integer index. If the index is out of bounds, a \rbox{null} value is returned instead.

\begin{codebox}{Example}{teal}{\icnote}{white}
list = [ 'a', 'b', 'c' ];
third = getOrNull(list, 2);
\end{codebox}

\item[\mdbox{R}{getOrNull(List<String> list)}{String}] This method attempts to retrieve
the first element of the provided list. If the list is empty, a \rbox{null} value is returned instead.

\begin{codebox}{Example}{teal}{\icnote}{white}
list = [ 'a', 'b', 'c' ];
first = getOrNull(list);
\end{codebox}
\end{description}

\begin{messagebox}{Flags and reserved storage in a session}{araracolour}{\icok}{white}
From version 6.0 on, there are three reserved namespaces within a session. They are described as follows:

\begin{description}
\item[{\rbox[araracolour]{environment}}] This namespace is quite intuitive: \arara\ will store the current state of the systems environment variables in its session. You may alter these values in the session storage but they will not be written back to the system configuration. To access an environment variable, you can use its usual name prefixed by \rbox[araracolour]{environment:}:

\begin{codebox}{Example}{teal}{\icnote}{white}
path = getSession().get('environment:PATH');
\end{codebox}

\item[{\rbox[araracolour]{arara}}] This namespace provides flags that control the underlying behaviour of \arara. Flags are used in rules and may be manipulated by the user. Be aware that every change in this namespace will result in the tool acting like you know what you did. Use this power with care. Currently, there is only one relevant flag: \rbox{arara:FILENAME:halt}. This will stop the currently run command execution on the file with the specified file name. The value of this map entry is the exit status you want \arara\ to have.

\begin{codebox}{Example}{teal}{\icnote}{white}
getSession().put('arara:myfile.tex:halt', 42);
\end{codebox}

\item[{\rbox[araracolour]{arg}}] This namespace acts as a bridge between contexts and the command line by providing access to key/value pairs defined at runtime by \opbox{{-}call-property} command line flags.

\begin{codebox}{Example}{teal}{\icnote}{white}
key = getSession().get('arg:key');
\end{codebox}

Please refer to Chapter~\ref{chap:commandline}, on page~\pageref{chap:commandline} for more details on the \opbox{{-}call-property} command line flag.

\end{description}
\end{messagebox}

The methods presented in this section provide interesting features for persistent data sharing, error handling, early command execution, and templating. It is important to note that more classes, objects and methods can be incorporated into \arara\ through class loading and object instantiation, extending the features and enhancing the overall user experience.
