// SPDX-License-Identifier: BSD-3-Clause
package org.islandoftex.arara.api.localization

@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.BINARY)
@RequiresOptIn(
    "arara's messages are not considered stable API. Only " +
        "use them in testing scenarios and arara's core projects.",
    RequiresOptIn.Level.WARNING
)
internal annotation class AraraMessages

/**
 * The messages arara will use. They should be treated semi-public and are no
 * stable API.
 *
 * All properties except [providedLocale] and [translators] are strings and
 * intended to be used in a [String.format] call.
 */
@AraraMessages
@Suppress("MaxLineLength", "ConstructorParameterNaming", "LongParameterList")
public open class Messages(
    /**
     * The locale these messages are written for.
     */
    public val providedLocale: MPPLocale = MPPLocale("en"),
    /**
     * The translators credited for this message set.
     */
    public val translators: List<String> = listOf("Paulo Roberto Massa Cereda"),
    /**
     * The error shown when the basename helper is handed a directory
     * instead of a file.
     */
    public val ERROR_BASENAME_NOT_A_FILE: String = "The \"basename\" method requires a file, not a directory. It looks like \"%s\" does not appear to be a file at all. If you need to perform tasks on a directory, you could use a couple of methods from the Java API.",
    /**
     * The error shown when the hash of a file cannot be computed due to
     * an IO problem.
     */
    public val ERROR_CALCULATEHASH_IO_EXCEPTION: String = "For whatever reason, I could not calculate the hash. I have no idea why it failed, though. Perhaps the file was moved or deleted before or during the hashing operation. Or maybe I do not have the proper permissions to read the file.",
    /**
     * The error shown when a string cannot be interpreted as a boolean
     * value.
     */
    public val ERROR_CHECKBOOLEAN_NOT_VALID_BOOLEAN: String = "It looks like \"%s\" is not a valid boolean value. This should be an easy fix. Make sure to use a valid string that represents boolean values (yes and no, true and false, 1 and 0, and on and off).",
    /**
     * The error shown when a file cannot be read during a regex check.
     */
    public val ERROR_CHECKREGEX_IO_EXCEPTION: String = "I could not read the contents of the file \"%s\", I got an IO error. I have no idea why it failed, though. Perhaps the file was moved or deleted before or during the reading operation. Or maybe I do not have the proper permissions to read the file.",
    /**
     * The error shown when the configuration file cannot be parsed.
     */
    public val ERROR_CONFIGURATION_GENERIC_ERROR: String = "I could not parse the configuration file, something bad happened. This part is quite tricky, since it involves aspects of the underlying data serialization format. I will do my best to help you in any way I can.",
    /**
     * The error shown when the loop cap in the configuration is not a
     * positive number.
     */
    public val ERROR_CONFIGURATION_LOOPS_INVALID_RANGE: String = "The value defined in the 'loops' key in the configuration file in order to denote the maximum number of loops has an invalid range. Please make sure to use a positive long value.",
    /**
     * The error shown when a referenced file cannot be found.
     */
    public val ERROR_DISCOVERFILE_FILE_NOT_FOUND: String = "I could not find the provided file \"%s\" %s. Please make sure the file exists and it has a valid extension.",
    /**
     * The error shown when a conditional expression cannot be compiled.
     */
    public val ERROR_EVALUATE_COMPILATION_FAILED: String = "For whatever reason, I could not compile the expression in the provided conditional. This part is quite tricky, since it involves aspects of the underlying expression language. I will do my best to help you in any way I can.",
    /**
     * The error shown when a conditional does not evaluate to a boolean
     * value.
     */
    public val ERROR_EVALUATE_NOT_BOOLEAN_VALUE: String = "The conditional evaluation was expecting a boolean value as result. This should be an easy fix. Just make sure the conditional evaluation resolves to a boolean value in the end.",
    /**
     * The error shown when directives cannot be extracted due to an IO
     * problem.
     */
    public val ERROR_EXTRACTOR_IO_ERROR: String = "There was an IO error while I was trying to extract the directives. I have no idea why it failed, though. Perhaps the file was moved or deleted before or during the extraction operation. Or maybe the file uses a different encoding than UTF-8. Or maybe I do not have the proper permissions to read the file.",
    /**
     * The error shown when an entry in the directive options cannot be
     * interpolated.
     */
    public val ERROR_EXTRACTOR_INTERPOLATION_FAILURE: String = "Your 'options' field contains an entry the MVEL parser is unable to cope with. Please revise your options not to contain any malformed '@{…}' parts.",
    /**
     * The error shown when the Lua engine meets the deprecated orb tag
     * syntax of the YAML engine.
     */
    public val ERROR_EXTRACTOR_ORB_TAG_IN_LUA_ENGINE: String = "Your 'options' field contains an orb tag of the deprecated YAML engine, which the Lua engine cannot interpolate. Please rewrite the entry with '{{ … }}' tags instead.",
    /**
     * The error shown when the filetype helper is handed a directory
     * instead of a file.
     */
    public val ERROR_FILETYPE_NOT_A_FILE: String = "The \"filetype\" method requires a file, not a directory. It looks like \"%s\" does not appear to be a file at all. If you need to perform tasks on a directory, you could use a couple of methods from the Java API.",
    /**
     * The error shown when an unknown extension is to be resolved
     * without an explicit file type.
     */
    public val ERROR_FILETYPE_UNKNOWN_EXTENSION: String = "I cannot recognize \"%s\" as a default extension. If you want to define a new file type, make sure to provide the extension and pattern. These are the default extensions: %s",
    /**
     * The error shown when the application path cannot be determined
     * due to an encoding problem.
     */
    public val ERROR_GETAPPLICATIONPATH_ENCODING_EXCEPTION: String = "There was an encoding problem while trying to obtain the application path. There is nothing much I can do about it.",
    /**
     * The error shown when a required rule argument is missing from the
     * directive.
     */
    public val ERROR_INTERPRETER_ARGUMENT_IS_REQUIRED: String = "It seems that \"%s\" is marked as required in the rule, but I could not find it in the directive parameters. Please make sure to add it as parameter for your directive and try again.",
    /**
     * The error shown when a rule command cannot be evaluated.
     */
    public val ERROR_INTERPRETER_COMMAND_RUNTIME_ERROR: String = "I could not evaluate one of the provided commands. This part is quite tricky, since it involves aspects of the underlying expression language. I will do my best to help you in any way I can.",
    /**
     * The error shown when a command exits with a code its exit check
     * rejects.
     */
    public val ERROR_INTERPRETER_COMMAND_UNSUCCESSFUL_EXIT: String = "The command \"%s\" exited unexpectedly with a non-zero exit code. Please see the command's output to evaluate your next steps.",
    /**
     * The message shown when a rule or a directive asks arara to halt.
     */
    public val ERROR_INTERPRETER_USER_REQUESTED_HALT: String = "arara will now halt on your (or a rule's) request to halt arara.",
    /**
     * The error shown when the exit status expression of a rule command
     * cannot be evaluated.
     */
    public val ERROR_INTERPRETER_EXIT_RUNTIME_ERROR: String = "I could not evaluate the exit status expression of one of the provided commands. This part is quite tricky, since it involves aspects of the underlying expression language. I will do my best to help you in any way I can.",
    /**
     * The error shown when the flag expression of a rule argument cannot
     * be evaluated.
     */
    public val ERROR_INTERPRETER_FLAG_RUNTIME_EXCEPTION: String = "I could not evaluate the flag expression of one of the arguments. This part is quite tricky, since it involves aspects of the underlying expression language. I will do my best to help you in any way I can.",
    /**
     * The error shown when the session value holding a halt status is
     * not a number.
     */
    public val ERROR_INTERPRETER_HALT_STATUS_NOT_A_NUMBER: String = "It looks like the value held under the halt session key \"%s\" is not a valid number, so I cannot report the requested halt status. This should be an easy fix: make sure the halt status is a number and try again.",
    /**
     * The error shown when a rule cannot be found in the configured
     * paths.
     */
    public val ERROR_INTERPRETER_RULE_NOT_FOUND: String = "I could not find a rule named \"%s\" in the provided rule paths. Perhaps a misspelled word? I was looking for a file named \"%s.yaml\" in the following paths in order of priority: %s",
    /**
     * The error shown when a directive carries keys the rule does not
     * declare.
     */
    public val ERROR_INTERPRETER_UNKNOWN_KEYS: String = "I found these unknown keys in the directive: %s. This should be an easy fix, just remove them from your map.",
    /**
     * The error shown when the exit expression of a rule command does
     * not return a boolean value.
     */
    public val ERROR_INTERPRETER_WRONG_EXIT_CLOSURE_RETURN: String = "The 'exit' expression must always return a boolean value (even if there is no computation in the closure body). This should be an easy fix: make sure to correct the type return statement and try again.",
    /**
     * The error shown when a rule command evaluates to an unsupported
     * return type.
     */
    public val ERROR_INTERPRETER_WRONG_RETURN_TYPE: String = "A rule should only return a boolean or a command. Other return types are not supported.",
    /**
     * The error shown when a file is neither a project specification nor
     * a list of them.
     */
    public val ERROR_INVALID_PROJECT_FILE: String = "I have read a file that was supposed to be a project. Unfortunately, it turned out it was neither a project nor a list of projects. Please double-check that your project file follows the documented format.",
    /**
     * The error shown when the file database cannot be loaded.
     */
    public val ERROR_LOAD_COULD_NOT_LOAD_DATABASE: String = "I could not load the YAML database named \"%s\". I have no idea why it failed, though. Perhaps the file was moved or deleted before or during the reading operation. Or maybe I do not have the proper permissions to read the file. By the way, make sure the YAML file is well-formed.",
    /**
     * The error shown when the requested preamble is not defined in the
     * configuration.
     */
    public val ERROR_PARSER_INVALID_PREAMBLE: String = "I am sorry, but the preamble \"%s\" could not be found. Please make sure this key exists in the configuration file.",
    /**
     * The error shown when a rule cannot be parsed.
     */
    public val ERROR_PARSERULE_GENERIC_ERROR: String = "I could not parse the rule, something bad happened. This part is quite tricky, since it involves aspects of the underlying data serialization format. I will do my best to help you in any way I can.",
    /**
     * The error shown when a rule file uses an unsupported format.
     */
    public val ERROR_PARSERULE_UNSUPPORTED_FORMAT: String = "I found an error in rule \"%s\" located at \"%s\". The rule format of \"%s\" is not supported by arara.",
    /**
     * The error shown when the project dependencies contain a cycle.
     */
    public val ERROR_PROJECT_CYCLE: String = "The project dependencies contain a cycle involving %s. Cyclic dependencies cannot be resolved, so please revise your project specification.",
    /**
     * The error shown when list replication has more or fewer
     * parameters than expected.
     */
    public val ERROR_REPLICATELIST_MISSING_FORMAT_ARGUMENTS_EXCEPTION: String = "I could not replicate the list due to a missing format argument. My guess is that there are less (or more) parameters than expected. Make sure to correct the number of parameters and try again.",
    /**
     * The header prepended to errors raised by a rule, naming the rule
     * and its location.
     */
    public val ERROR_RULE_IDENTIFIER_AND_PATH: String = "I have spotted an error in rule \"%s\" located at \"%s\".",
    /**
     * The error shown when a system command fails for an unspecified
     * reason.
     */
    public val ERROR_RUN_GENERIC_EXCEPTION: String = "I could not run the provided system command, something bad happened. This part is quite tricky, since it involves aspects of the underlying expression language. I will do my best to help you in any way I can.",
    /**
     * The error shown when a system command is interrupted.
     */
    public val ERROR_RUN_INTERRUPTED_EXCEPTION: String = "The provided system command execution was suddenly interrupted. Maybe there was an external interruption that forced the command to end abruptly.",
    /**
     * The error shown when a system command reports an invalid exit
     * value.
     */
    public val ERROR_RUN_INVALID_EXIT_VALUE_EXCEPTION: String = "The provided system command execution has returned an invalid exit value.",
    /**
     * The error shown when a system command fails due to an IO problem.
     */
    public val ERROR_RUN_IO_EXCEPTION: String = "The system command execution has failed due to an IO error. Are you sure the provided system command exists in your path? It might be a good idea to check the path and see if the command is available.",
    /**
     * The error shown when a system command reaches the configured
     * timeout.
     */
    public val ERROR_RUN_TIMEOUT_EXCEPTION: String = "The system command execution reached the provided timeout value and was aborted. If the time was way too short, make sure to provide a longer value.",
    /**
     * The error shown when the file database cannot be saved.
     */
    public val ERROR_SAVE_COULD_NOT_SAVE_DATABASE: String = "I could not save the YAML database named \"%s\". I have no idea why it failed, though. Perhaps I do not have the proper permissions to write the YAML file to disk.",
    /**
     * The error shown when a session key that is about to be read does
     * not exist.
     */
    public val ERROR_SESSION_OBTAIN_UNKNOWN_KEY: String = "The \"get\" method has found an unknown key \"%s\" in the session scope. I could not get something I do not have in the first place. Please enter a valid key and try again.",
    /**
     * The error shown when a session key that is about to be removed
     * does not exist.
     */
    public val ERROR_SESSION_REMOVE_UNKNOWN_KEY: String = "The \"remove\" method has found an unknown key \"%s\" in the session scope. I could not remove something I do not have in the first place. Please enter a valid key and try again.",
    /**
     * The error shown when the file list of a directive is empty.
     */
    public val ERROR_VALIDATE_EMPTY_FILES_LIST: String = "I read a directive %s and found out that the provided \"files\" list is empty. This is an easy fix: make sure the list has at least one element and try again.",
    /**
     * The error shown when the file list of a directive is not a list.
     */
    public val ERROR_VALIDATE_FILES_IS_NOT_A_LIST: String = "I read a directive %s and found out that \"files\" requires a list. Please make sure to correct the type to a proper list and try again.",
    /**
     * The error shown when a directive is malformed.
     */
    public val ERROR_VALIDATE_INVALID_DIRECTIVE_FORMAT: String = "I spotted an invalid directive %s in the provided file. Make sure to fix the directive and try again.",
    /**
     * The error shown when a file contains no directives at all.
     */
    public val ERROR_VALIDATE_NO_DIRECTIVES_FOUND: String = "It looks like no directives were found in the provided file. Make sure to include at least one directive and try again.",
    /**
     * The error shown when a directive line break has no continuation.
     */
    public val ERROR_VALIDATE_ORPHAN_LINEBREAK: String = "Apparently there is an orphan directive line break in line %s. I cannot proceed. Please correct the directive and try again.",
    /**
     * The error shown when a directive uses the reserved reference key.
     */
    public val ERROR_VALIDATE_REFERENCE_IS_RESERVED: String = "I read a directive %s and found out that the key \"reference\" was used. This key is reserved, so you cannot use it. But do not worry, this should be an easy fix. Just replace it by another name.",
    /**
     * The error shown when the YAML map of a directive cannot be
     * processed.
     */
    public val ERROR_VALIDATE_YAML_EXCEPTION: String = "There was a problem with the provided YAML map in a directive %s. This part is quite tricky, since it involves aspects of the underlying data serialization format.",
    /**
     * The error shown when a rule argument uses a reserved identifier.
     */
    public val ERROR_VALIDATEBODY_ARGUMENT_ID_IS_RESERVED: String = "The argument identifier \"%s\" is reserved, so you cannot use it. This should be an easy fix. Just replace it by another name.",
    /**
     * The error shown when a rule declares duplicate argument
     * identifiers.
     */
    public val ERROR_VALIDATEBODY_DUPLICATE_ARGUMENT_IDENTIFIERS: String = "Apparently you have duplicate argument identifiers in your rule. Make sure to fix this issue and try again.",
    /**
     * The error shown when a rule argument has neither a flag nor a
     * default.
     */
    public val ERROR_VALIDATEBODY_MISSING_KEYS: String = "When defining a rule argument scope, at least 'flag' or 'default' must be used. Please, make sure to use at least one of them.",
    /**
     * The error shown when a rule argument has no identifier.
     */
    public val ERROR_VALIDATEBODY_NULL_ARGUMENT_ID: String = "I found out that one of the arguments has no identifier. Please, make sure to add a valid identifier to the argument and try again.",
    /**
     * The error shown when a rule command entry is null.
     */
    public val ERROR_VALIDATEBODY_NULL_COMMAND: String = "I found a null command in the provided rule. This should be an easy fix. Make sure to add a valid command to the rule.",
    /**
     * The error shown when a rule has no name.
     */
    public val ERROR_VALIDATEHEADER_NULL_NAME: String = "The provided rule has no name. This should be an easy fix. Make sure to add a valid name and try again.",
    /**
     * The error shown when a rule identifier does not match the expected
     * one.
     */
    public val ERROR_VALIDATEHEADER_WRONG_IDENTIFIER: String = "The rule has a wrong identifier. I was expecting \"%s\", but found \"%s\". This should be an easy fix: just replace the wrong identifier by the correct one.",
    /**
     * The label shown when an exception carries details in the log.
     */
    public val INFO_DISPLAY_EXCEPTION_MORE_DETAILS: String = "There are more details available on this exception:",
    /**
     * The message shown with the total execution time.
     */
    public val INFO_DISPLAY_EXECUTION_TIME: String = "Total: %s seconds",
    /**
     * The message shown while a file is being processed.
     */
    public val INFO_DISPLAY_FILE_INFORMATION: String = "Processing \"%s\" (size: %s, last modified: %s), please wait.",
    /**
     * The message shown when a boolean entry is met in dry-run mode.
     */
    public val INFO_INTERPRETER_DRYRUN_MODE_BOOLEAN_MODE: String = "Although executing in dry-run mode, this entry might be already processed since it returned a boolean value: %s",
    /**
     * The message shown instead of running a command in dry-run mode.
     */
    public val INFO_INTERPRETER_DRYRUN_MODE_SYSTEM_COMMAND: String = "About to run: %s",
    /**
     * The message shown when a provided Lua file is not a project
     * specification.
     */
    public val INFO_INTERPRETER_LUA_FILE_NOT_A_PROJECT: String = "It looks like the provided Lua file could not be parsed as a project specification, so I will treat it as a regular input file. If it is meant to be a project specification, please check the log for details on what went wrong.",
    /**
     * The label shown for a single rule author.
     */
    public val INFO_LABEL_AUTHOR: String = "Author:",
    /**
     * The label shown for multiple rule authors.
     */
    public val INFO_LABEL_AUTHORS: String = "Authors:",
    /**
     * The label shown for the conditional of an entry in dry-run mode.
     */
    public val INFO_LABEL_CONDITIONAL: String = "Conditional:",
    /**
     * The label shown when a rule has no authors.
     */
    public val INFO_LABEL_NO_AUTHORS: String = "No authors provided",
    /**
     * The label shown for the details of an entry.
     */
    public val INFO_LABEL_ON_DETAILS: String = "DETAILS",
    /**
     * The label shown when an entry raises an error.
     */
    public val INFO_LABEL_ON_ERROR: String = "ERROR",
    /**
     * The label shown when an entry fails.
     */
    public val INFO_LABEL_ON_FAILURE: String = "FAILURE",
    /**
     * The label shown when an entry succeeds.
     */
    public val INFO_LABEL_ON_SUCCESS: String = "SUCCESS",
    /**
     * The label shown for a rule command without a name.
     */
    public val INFO_LABEL_UNNAMED_TASK: String = "Unnamed task",
    /**
     * The license note printed in the terminal.
     */
    public val INFO_PARSER_NOTES: String = "arara is released under the New BSD license."
)
