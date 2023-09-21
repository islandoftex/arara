// SPDX-License-Identifier: BSD-3-Clause
package org.islandoftex.arara.api.localization

@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.BINARY)
@RequiresOptIn(
    "arara's messages are not considered stable API. Only " +
        "use them in testing scenarios and arara's core projects.",
    RequiresOptIn.Level.WARNING,
)
internal annotation class AraraMessages

// TODO: translate for v7:
//  * ERROR_EXTRACTOR_IO_ERROR
//  * ERROR_INVALID_PROJECT_FILE

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
    public val providedLocale: MPPLocale = MPPLocale("en"),
    public val translators: List<String> = listOf("Paulo Roberto Massa Cereda"),
    public val ERROR_BASENAME_NOT_A_FILE: String = "The \"basename\" method requires a file, not a directory. It looks like \"%s\" does not appear to be a file at all. If you need to perform tasks on a directory, you could use a couple of methods from the Java API.",
    public val ERROR_CALCULATEHASH_IO_EXCEPTION: String = "For whatever reason, I could not calculate the hash. I have no idea why it failed, though. Perhaps the file was moved or deleted before or during the hashing operation. Or maybe I do not have the proper permissions to read the file.",
    public val ERROR_CHECKBOOLEAN_NOT_VALID_BOOLEAN: String = "It looks like \"%s\" is not a valid boolean value. This should be an easy fix. Make sure to use a valid string that represents boolean values (yes and no, true and false, 1 and 0, and on and off).",
    public val ERROR_CHECKREGEX_IO_EXCEPTION: String = "I could not read the contents of the file \"%s\", I got an IO error. I have no idea why it failed, though. Perhaps the file was moved or deleted before or during the reading operation. Or maybe I do not have the proper permissions to read the file.",
    public val ERROR_CONFIGURATION_GENERIC_ERROR: String = "I could not parse the configuration file, something bad happened. This part is quite tricky, since it involves aspects of the underlying data serialization format. I will do my best to help you in any way I can.",
    public val ERROR_CONFIGURATION_LOOPS_INVALID_RANGE: String = "The value defined in the 'loops' key in the configuration file in order to denote the maximum number of loops has an invalid range. Please make sure to use a positive long value.",
    public val ERROR_DISCOVERFILE_FILE_NOT_FOUND: String = "I could not find the provided file \"%s\" %s. Please make sure the file exists and it has a valid extension.",
    public val ERROR_EVALUATE_COMPILATION_FAILED: String = "For whatever reason, I could not compile the expression in the provided conditional. This part is quite tricky, since it involves aspects of the underlying expression language. I will do my best to help you in any way I can.",
    public val ERROR_EVALUATE_NOT_BOOLEAN_VALUE: String = "The conditional evaluation was expecting a boolean value as result. This should be an easy fix. Just make sure the conditional evaluation resolves to a boolean value in the end.",
    public val ERROR_EXTRACTOR_IO_ERROR: String = "There was an IO error while I was trying to extract the directives. I have no idea why it failed, though. Perhaps the file was moved or deleted before or during the extraction operation. Or maybe the file uses a different encoding than UTF-8. Or maybe I do not have the proper permissions to read the file.",
    public val ERROR_EXTRACTOR_INTERPOLATION_FAILURE: String = "Your 'options' field contains an entry the MVEL parser is unable to cope with. Please revise your options not to contain any malformed '@{…}' parts.",
    public val ERROR_FILETYPE_NOT_A_FILE: String = "The \"filetype\" method requires a file, not a directory. It looks like \"%s\" does not appear to be a file at all. If you need to perform tasks on a directory, you could use a couple of methods from the Java API.",
    public val ERROR_FILETYPE_UNKNOWN_EXTENSION: String = "I cannot recognize \"%s\" as a default extension. If you want to define a new file type, make sure to provide the extension and pattern. These are the default extensions: %s",
    public val ERROR_GETAPPLICATIONPATH_ENCODING_EXCEPTION: String = "There was an encoding problem while trying to obtain the application path. There is nothing much I can do about it.",
    public val ERROR_INTERPRETER_ARGUMENT_IS_REQUIRED: String = "It seems that \"%s\" is marked as required in the rule, but I could not find it in the directive parameters. Please make sure to add it as parameter for your directive and try again.",
    public val ERROR_INTERPRETER_COMMAND_RUNTIME_ERROR: String = "I could not evaluate one of the provided commands. This part is quite tricky, since it involves aspects of the underlying expression language. I will do my best to help you in any way I can.",
    public val ERROR_INTERPRETER_COMMAND_UNSUCCESSFUL_EXIT: String = "The command \"%s\" exited unexpectedly with a non-zero exit code. Please see the command's output to evaluate your next steps.",
    public val ERROR_INTERPRETER_USER_REQUESTED_HALT: String = "arara will now halt on your (or a rule's) request to halt arara.",
    public val ERROR_INTERPRETER_EXIT_RUNTIME_ERROR: String = "I could not evaluate the exit status expression of one of the provided commands. This part is quite tricky, since it involves aspects of the underlying expression language. I will do my best to help you in any way I can.",
    public val ERROR_INTERPRETER_FLAG_RUNTIME_EXCEPTION: String = "I could not evaluate the flag expression of one of the arguments. This part is quite tricky, since it involves aspects of the underlying expression language. I will do my best to help you in any way I can.",
    public val ERROR_INTERPRETER_RULE_NOT_FOUND: String = "I could not find a rule named \"%s\" in the provided rule paths. Perhaps a misspelled word? I was looking for a file named \"%s.yaml\" in the following paths in order of priority: %s",
    public val ERROR_INTERPRETER_UNKNOWN_KEYS: String = "I found these unknown keys in the directive: %s. This should be an easy fix, just remove them from your map.",
    public val ERROR_INTERPRETER_WRONG_EXIT_CLOSURE_RETURN: String = "The 'exit' expression must always return a boolean value (even if there is no computation in the closure body). This should be an easy fix: make sure to correct the type return statement and try again.",
    public val ERROR_INTERPRETER_WRONG_RETURN_TYPE: String = "A rule should only return a boolean or a command. Other return types are not supported.",
    public val ERROR_INVALID_PROJECT_FILE: String = "I have read a file that was supposed to be a project. Unfortunately, it turned out it was neither a project nor a list of projects. Please double-check that your project file follows the documented format.",
    public val ERROR_LOAD_COULD_NOT_LOAD_XML: String = "I could not load the YAML database named \"%s\". I have no idea why it failed, though. Perhaps the file was moved or deleted before or during the reading operation. Or maybe I do not have the proper permissions to read the file. By the way, make sure the YAML file is well-formed.",
    public val ERROR_PARSER_INVALID_PREAMBLE: String = "I am sorry, but the preamble \"%s\" could not be found. Please make sure this key exists in the configuration file.",
    public val ERROR_PARSERULE_GENERIC_ERROR: String = "I could not parse the rule, something bad happened. This part is quite tricky, since it involves aspects of the underlying data serialization format. I will do my best to help you in any way I can.",
    public val ERROR_REPLICATELIST_MISSING_FORMAT_ARGUMENTS_EXCEPTION: String = "I could not replicate the list due to a missing format argument. My guess is that there are less (or more) parameters than expected. Make sure to correct the number of parameters and try again.",
    public val ERROR_RULE_IDENTIFIER_AND_PATH: String = "I have spotted an error in rule \"%s\" located at \"%s\".",
    public val ERROR_RUN_GENERIC_EXCEPTION: String = "I could not run the provided system command, something bad happened. This part is quite tricky, since it involves aspects of the underlying expression language. I will do my best to help you in any way I can.",
    public val ERROR_RUN_INTERRUPTED_EXCEPTION: String = "The provided system command execution was suddenly interrupted. Maybe there was an external interruption that forced the command to end abruptly.",
    public val ERROR_RUN_INVALID_EXIT_VALUE_EXCEPTION: String = "The provided system command execution has returned an invalid exit value.",
    public val ERROR_RUN_IO_EXCEPTION: String = "The system command execution has failed due to an IO error. Are you sure the provided system command exists in your path? It might be a good idea to check the path and see if the command is available.",
    public val ERROR_RUN_TIMEOUT_EXCEPTION: String = "The system command execution reached the provided timeout value and was aborted. If the time was way too short, make sure to provide a longer value.",
    public val ERROR_SAVE_COULD_NOT_SAVE_XML: String = "I could not save the YAML database named \"%s\". I have no idea why it failed, though. Perhaps I do not have the proper permissions to write the YAML file to disk.",
    public val ERROR_SESSION_OBTAIN_UNKNOWN_KEY: String = "The \"get\" method has found an unknown key \"%s\" in the session scope. I could not get something I do not have in the first place. Please enter a valid key and try again.",
    public val ERROR_SESSION_REMOVE_UNKNOWN_KEY: String = "The \"remove\" method has found an unknown key \"%s\" in the session scope. I could not remove something I do not have in the first place. Please enter a valid key and try again.",
    public val ERROR_VALIDATE_EMPTY_FILES_LIST: String = "I read a directive %s and found out that the provided \"files\" list is empty. This is an easy fix: make sure the list has at least one element and try again.",
    public val ERROR_VALIDATE_FILES_IS_NOT_A_LIST: String = "I read a directive %s and found out that \"files\" requires a list. Please make sure to correct the type to a proper list and try again.",
    public val ERROR_VALIDATE_INVALID_DIRECTIVE_FORMAT: String = "I spotted an invalid directive %s in the provided file. Make sure to fix the directive and try again.",
    public val ERROR_VALIDATE_NO_DIRECTIVES_FOUND: String = "It looks like no directives were found in the provided file. Make sure to include at least one directive and try again.",
    public val ERROR_VALIDATE_ORPHAN_LINEBREAK: String = "Apparently there is an orphan directive line break in line %s. I cannot proceed. Please correct the directive and try again.",
    public val ERROR_VALIDATE_REFERENCE_IS_RESERVED: String = "I read a directive %s and found out that the key \"reference\" was used. This key is reserved, so you cannot use it. But do not worry, this should be an easy fix. Just replace it by another name.",
    public val ERROR_VALIDATE_YAML_EXCEPTION: String = "There was a problem with the provided YAML map in a directive %s. This part is quite tricky, since it involves aspects of the underlying data serialization format.",
    public val ERROR_VALIDATEBODY_ARGUMENT_ID_IS_RESERVED: String = "The argument identifier \"%s\" is reserved, so you cannot use it. This should be an easy fix. Just replace it by another name.",
    public val ERROR_VALIDATEBODY_DUPLICATE_ARGUMENT_IDENTIFIERS: String = "Apparently you have duplicate argument identifiers in your rule. Make sure to fix this issue and try again.",
    public val ERROR_VALIDATEBODY_MISSING_KEYS: String = "When defining a rule argument scope, at least 'flag' or 'default' must be used. Please, make sure to use at least one of them.",
    public val ERROR_VALIDATEBODY_NULL_ARGUMENT_ID: String = "I found out that one of the arguments has no identifier. Please, make sure to add a valid identifier to the argument and try again.",
    public val ERROR_VALIDATEBODY_NULL_COMMAND: String = "I found a null command in the provided rule. This should be an easy fix. Make sure to add a valid command to the rule.",
    public val ERROR_VALIDATEHEADER_NULL_NAME: String = "The provided rule has no name. This should be an easy fix. Make sure to add a valid name and try again.",
    public val ERROR_VALIDATEHEADER_WRONG_IDENTIFIER: String = "The rule has a wrong identifier. I was expecting \"%s\", but found \"%s\". This should be an easy fix: just replace the wrong identifier by the correct one.",
    public val INFO_DISPLAY_EXCEPTION_MORE_DETAILS: String = "There are more details available on this exception:",
    public val INFO_DISPLAY_EXECUTION_TIME: String = "Total: %s seconds",
    public val INFO_DISPLAY_FILE_INFORMATION: String = "Processing \"%s\" (size: %s, last modified: %s), please wait.",
    public val INFO_INTERPRETER_DRYRUN_MODE_BOOLEAN_MODE: String = "Although executing in dry-run mode, this entry might be already processed since it returned a boolean value: %s",
    public val INFO_INTERPRETER_DRYRUN_MODE_SYSTEM_COMMAND: String = "About to run: %s",
    public val INFO_LABEL_AUTHOR: String = "Author:",
    public val INFO_LABEL_AUTHORS: String = "Authors:",
    public val INFO_LABEL_CONDITIONAL: String = "Conditional:",
    public val INFO_LABEL_NO_AUTHORS: String = "No authors provided",
    public val INFO_LABEL_ON_DETAILS: String = "DETAILS",
    public val INFO_LABEL_ON_ERROR: String = "ERROR",
    public val INFO_LABEL_ON_FAILURE: String = "FAILURE",
    public val INFO_LABEL_ON_SUCCESS: String = "SUCCESS",
    public val INFO_LABEL_UNNAMED_TASK: String = "Unnamed task",
    public val INFO_PARSER_NOTES: String = "arara is released under the New BSD license.",
    public val LOG_INFO_BEGIN_BUFFER: String = "BEGIN OUTPUT BUFFER",
    public val LOG_INFO_BOOLEAN_MODE: String = "Boolean value: %s",
    public val LOG_INFO_DIRECTIVES_BLOCK: String = "DIRECTIVES",
    public val LOG_INFO_END_BUFFER: String = "END OUTPUT BUFFER",
    public val LOG_INFO_INTERPRET_RULE: String = "I am ready to interpret rule \"%s\".",
    public val LOG_INFO_INTERPRET_TASK: String = "I am ready to interpret task \"%s\" from rule \"%s\".",
    public val LOG_INFO_POTENTIAL_PATTERN_FOUND: String = "I found a potential pattern in line %s: %s",
    public val LOG_INFO_RULE_LOCATION: String = "Rule location: \"%s\"",
    public val LOG_INFO_SYSTEM_COMMAND: String = "System command: %s",
    public val LOG_INFO_TASK_RESULT: String = "Task result:",
    public val LOG_INFO_VALIDATED_DIRECTIVES: String = "All directives were validated. We are good to go.",
    public val LOG_INFO_WELCOME_MESSAGE: String = "Welcome to arara %s!",
)
