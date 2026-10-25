// SPDX-License-Identifier: BSD-3-Clause
package org.islandoftex.arara.core.localization

import org.islandoftex.arara.api.localization.MPPLocale

/** English message catalog. */
@Suppress("MaxLineLength", "TooManyFunctions")
object EnglishMessages : AraraMessages {
    override val warningDeprecatedYaml: String =
        "Compatibility warning: you have used deprecated YAML rules which will be discontinued in v9."

    override val providedLocale: MPPLocale = MPPLocale("en")

    override val translators: List<String> = listOf("Paulo Roberto Massa Cereda")

    override fun errorProjectFileDirectivesExtraction(
        file: String,
        details: String
    ) =
        "I found an error while extracting the directives of '${file}': ${details}."

    override fun errorGlobPatternInvalid(pattern: String, details: String) =
        "The glob pattern \"${pattern}\" could not be converted into a regular expression: ${details}. Please make sure the pattern is well formed and try again."

    override val errorSafeModeClassLoading =
        "In safe mode, rules are not allowed to load arbitrary classes from user-provided archives."

    override val errorSafeModeSystemCommand =
        "In safe mode, rules are not allowed to execute arbitrary system commands within a step of execution."

    override val errorDialogInputClosed: String =
        "It looks like the terminal input ended while a dialog was waiting for an answer, so I cannot read your choice. Please run arara in an interactive terminal so the dialog can be answered."

    override val errorDialogsNotConfigured: String =
        "It looks like this run has no dialog support configured, so I cannot show the dialog a rule asked for. Please provide a dialog implementation in the application running arara, or avoid rules with interactive dialogs."

    override fun errorScriptString(field: String): String =
        "${field} must be a string."

    override fun errorScriptTable(field: String): String =
        "${field} must be a table."

    override fun errorScriptBoolean(field: String): String =
        "${field} must be a boolean."

    override fun errorScriptInteger(field: String): String =
        "${field} must be an integer."

    override fun errorScriptList(field: String): String =
        "${field} must be a contiguous positional list."

    override fun errorScriptUnknownKey(field: String): String =
        "Unknown key ${field}."

    override fun errorScriptRequired(field: String): String =
        "${field} is required."

    override fun errorScriptNonblank(field: String): String =
        "${field} must not be blank."

    override fun errorScriptDuplicate(field: String, value: String): String =
        "${field} repeats '${value}'."

    override fun errorScriptDirectory(field: String): String =
        "${field} is a directory."

    override fun errorScriptEmpty(field: String): String =
        "${field} must contain at least one entry."

    override fun errorScriptStringKeys(field: String): String =
        "${field} requires nonblank string keys."

    override fun errorProjectInvalidLua(path: String, detail: String): String =
        "Detected invalid Lua in ${path}: ${detail}"

    override fun errorProjectDuplicateNames(names: String): String =
        "Project names must be unique: ${names}."

    override fun errorProjectUnknownDependency(project: String, dependency: String): String =
        "Project '${project}' has unknown dependency '${dependency}'."

    override fun errorProjectCycle(cycle: String): String =
        "Project dependencies contain a cycle: ${cycle}."

    override fun errorProjectFileOwnership(path: String, owner: String, project: String): String =
        "File '${path}' belongs to both '${owner}' and '${project}'."

    override fun errorProjectFiletype(path: String): String =
        "No configured file type matches '${path}'."

    override fun errorConfigurationPrepend(value: String): String =
        "Invalid preamble.prepend '${value}'; use 'always' or 'without-directives'."

    override fun errorScriptFunction(field: String): String =
        "${field} must be a function."

    override fun errorScriptLiteral(field: String): String =
        "${field} must be a literal value."

    override fun errorScriptAlternatives(field: String, alternatives: String): String =
        "${field} requires at least one of ${alternatives}."

    override val errorLuaInterpolationFailure: String =
        "The 'options' field contains an invalid Lua expression or interpolation tag."

    override val errorYamlTagsInLua: String =
        "MVEL tags require the YAML engine; Lua uses {{ expression }}."

    override val errorLuaTagsInYaml: String =
        "Lua tags require --engine lua."

    override val errorLuaUnterminatedTag: String =
        "Unterminated interpolation tag."

    override val errorLuaUnterminatedQuote: String =
        "Unterminated quoted string in interpolation tag."

    override val errorLuaUnterminatedLongString: String =
        "Unterminated long string in interpolation tag."

    override val errorLuaMapKeys: String =
        "Map keys collide as Lua strings."

    override val errorLuaTableKeys: String =
        "Lua table keys collide as strings."

    override fun errorLuaExactInteger(value: String): String =
        "Integer ${value} cannot be represented exactly as a Lua number."

    override val errorLuaCyclicValues: String =
        "Cyclic tables and collections cannot be converted."

    override fun errorLuaBadArgument(index: Int, helper: String, expected: String, actual: String): String =
        "bad argument #${index} to '${helper}' (${expected} expected, got ${actual})"

    override fun errorLuaBooleanResult(helper: String, actual: String): String =
        "bad result from '${helper}' (boolean expected, got ${actual})"

    override fun errorLuaHelperNoArgs(helper: String): String =
        "${helper} requires at least one argument."

    override fun errorLoadCouldNotLoadDatabase(name: String): String =
        "Could not load database '${name}'. Check file access and YAML cache integrity."

    override fun errorSaveCouldNotSaveDatabase(name: String): String =
        "Could not save database '${name}'. Check that its parent directory exists and is writable."

    override fun errorBasenameNotAFile(path: String): String =
        "The \"basename\" method requires a file, not a directory. It looks like \"${path}\" does not appear to be a file at all. If you need to perform tasks on a directory, you could use a couple of methods from the Java API."

    override val errorCalculateHashIoException: String =
        "For whatever reason, I could not calculate the hash. I have no idea why it failed, though. Perhaps the file was moved or deleted before or during the hashing operation. Or maybe I do not have the proper permissions to read the file."

    override fun errorCheckBooleanNotValidBoolean(value: String): String =
        "It looks like \"${value}\" is not a valid boolean value. This should be an easy fix. Make sure to use a valid string that represents boolean values (yes and no, true and false, 1 and 0, and on and off)."

    override fun errorCheckRegexIoException(file: String): String =
        "I could not read the contents of the file \"${file}\", I got an IO error. I have no idea why it failed, though. Perhaps the file was moved or deleted before or during the reading operation. Or maybe I do not have the proper permissions to read the file."

    override val errorConfigurationGenericError: String =
        "I could not parse the configuration file, something bad happened. This part is quite tricky, since it involves aspects of the underlying data serialization format. I will do my best to help you in any way I can."

    override val errorConfigurationLoopsInvalidRange: String =
        "The value defined in the 'loops' key in the configuration file in order to denote the maximum number of loops has an invalid range. Please make sure to use a positive long value."

    override fun errorDiscoverFileFileNotFound(file: String, extensions: String): String =
        "I could not find the provided file \"${file}\" ${extensions}. Please make sure the file exists and it has a valid extension."

    override fun errorDiscoverFileSafeModeNoCompletion(file: String) =
        "I could not find the provided file \"${file}\". Safe mode refuses to complete file references from their extensions, so please provide the full file name, extension included."

    override fun errorDiscoverFileUnknownExtension(
        file: String,
        extensions: String
    ) =
        "I could not determine the file type of '${file}': no file type of this run carries its extension. The extensions of this run are ${extensions}. Add the missing extension to the 'file_types' (Lua) / 'filetypes' (YAML) of the configuration."

    override fun errorDiscoverFileUnknownLuaExtension(
        file: String,
        extensions: String
    ) =
        "I could not determine the file type of '${file}': no file type of this run carries its extension. The extensions of this run are ${extensions}. Add the extension to the 'file_types' (Lua) / 'filetypes' (YAML) of the configuration - or, if the file is a project specification, its suffix is '.proj.lua'."

    override val errorEvaluateCompilationFailed: String =
        "For whatever reason, I could not compile the expression in the provided conditional. This part is quite tricky, since it involves aspects of the underlying expression language. I will do my best to help you in any way I can."

    override val errorEvaluateNotBooleanValue: String =
        "The conditional evaluation was expecting a boolean value as result. This should be an easy fix. Just make sure the conditional evaluation resolves to a boolean value in the end."

    override val errorExtractorIoError: String =
        "There was an IO error while I was trying to extract the directives. I have no idea why it failed, though. Perhaps the file was moved or deleted before or during the extraction operation. Or maybe the file uses a different encoding than UTF-8. Or maybe I do not have the proper permissions to read the file."

    override val errorExtractorInterpolationFailure: String =
        "Your 'options' field contains an entry the MVEL parser is unable to cope with. Please revise your options not to contain any malformed '@{…}' parts."

    override fun errorFiletypeInvalidPattern(pattern: String): String =
        "It looks like the pattern \"${pattern}\" you wanted for this file type is not a valid regular expression. This should be an easy fix: make sure to use a valid expression and try again."

    override fun errorFiletypeNotAFile(path: String): String =
        "The \"filetype\" method requires a file, not a directory. It looks like \"${path}\" does not appear to be a file at all. If you need to perform tasks on a directory, you could use a couple of methods from the Java API."

    override fun errorFiletypeUnknownExtension(extension: String, extensions: String): String =
        "I cannot recognize \"${extension}\" as a default extension. If you want to define a new file type, make sure to provide the extension and pattern. These are the default extensions: ${extensions}"

    override val errorGetApplicationPathEncodingException: String =
        "There was an encoding problem while trying to obtain the application path. There is nothing much I can do about it."

    override fun errorInterpreterArgumentIsRequired(argument: String): String =
        "It seems that \"${argument}\" is marked as required in the rule, but I could not find it in the directive parameters. Please make sure to add it as parameter for your directive and try again."

    override val errorInterpreterCommandRuntimeError: String =
        "I could not evaluate one of the provided commands. This part is quite tricky, since it involves aspects of the underlying expression language. I will do my best to help you in any way I can."

    override fun errorInterpreterCommandUnsuccessfulExit(command: String): String =
        "The command \"${command}\" exited unexpectedly with a non-zero exit code. Please see the command's output to evaluate your next steps."

    override val errorInterpreterUserRequestedHalt: String =
        "arara will now halt on your (or a rule's) request to halt arara."

    override val errorInterpreterExitRuntimeError: String =
        "I could not evaluate the exit status expression of one of the provided commands. This part is quite tricky, since it involves aspects of the underlying expression language. I will do my best to help you in any way I can."

    override val errorInterpreterFlagRuntimeException: String =
        "I could not evaluate the flag expression of one of the arguments. This part is quite tricky, since it involves aspects of the underlying expression language. I will do my best to help you in any way I can."

    override fun errorLogFormatUnknown(value: String): String =
        "Unknown log format '${value}'. Use none, text or json."

    override fun errorConsoleModeUnknown(mode: String): String =
        "Unknown console mode '${mode}'. Use plain or styled."

    override fun errorParseRuleUnsupportedFormat(identifier: String, format: String): String =
        "Unsupported rule format for '${identifier}': '${format}'. Use a Lua or YAML rule file."

    override fun errorInterpreterRuleNotFound(identifier: String, expected: String, paths: String): String =
        "I could not find a rule named \"${identifier}\" in the provided rule paths. Perhaps a misspelled word? I was looking for a file named \"${expected}\" in the following paths in order of priority: ${paths}"

    override fun errorInterpreterUnknownKeys(keys: String): String =
        "I found these unknown keys in the directive: ${keys}. This should be an easy fix, just remove them from your map."

    override val errorInterpreterWrongExitClosureReturn: String =
        "The 'exit' expression must always return a boolean value (even if there is no computation in the closure body). This should be an easy fix: make sure to correct the type return statement and try again."

    override val errorInterpreterWrongReturnType: String =
        "A rule should only return a boolean or a command. Other return types are not supported."

    override val errorInvalidProjectFile: String =
        "I have read a file that was supposed to be a project. Unfortunately, it turned out it was neither a project nor a list of projects. Please double-check that your project file follows the documented format."

    override fun errorLoadCouldNotLoadXml(name: String): String =
        "I could not load the YAML database named \"${name}\". I have no idea why it failed, though. Perhaps the file was moved or deleted before or during the reading operation. Or maybe I do not have the proper permissions to read the file. By the way, make sure the YAML file is well-formed."

    override fun errorParserInvalidPreamble(name: String): String =
        "I am sorry, but the preamble \"${name}\" could not be found. Please make sure this key exists in the configuration file."

    override val errorParseRuleGenericError: String =
        "I could not parse the rule, something bad happened. This part is quite tricky, since it involves aspects of the underlying data serialization format. I will do my best to help you in any way I can."

    override val errorReplicateListMissingFormatArgumentsException: String =
        "I could not replicate the list due to a missing format argument. My guess is that there are less (or more) parameters than expected. Make sure to correct the number of parameters and try again."

    override fun errorRuleIdentifierAndPath(identifier: String, location: String): String =
        "I have spotted an error in rule \"${identifier}\" located at \"${location}\"."

    override val errorRunGenericException: String =
        "I could not run the provided system command, something bad happened. This part is quite tricky, since it involves aspects of the underlying expression language. I will do my best to help you in any way I can."

    override val errorRunInterruptedException: String =
        "The provided system command execution was suddenly interrupted. Maybe there was an external interruption that forced the command to end abruptly."

    override val errorRunInvalidExitValueException: String =
        "The provided system command execution has returned an invalid exit value."

    override val errorRunIoException: String =
        "The system command execution has failed due to an IO error. Are you sure the provided system command exists in your path? It might be a good idea to check the path and see if the command is available."

    override val errorRunTimeoutException: String =
        "The system command execution reached the provided timeout value and was aborted. If the time was way too short, make sure to provide a longer value."

    override fun errorSaveCouldNotSaveXml(name: String): String =
        "I could not save the YAML database named \"${name}\". I have no idea why it failed, though. Perhaps I do not have the proper permissions to write the YAML file to disk."

    override fun errorSessionObtainUnknownKey(key: String): String =
        "The \"get\" method has found an unknown key \"${key}\" in the session scope. I could not get something I do not have in the first place. Please enter a valid key and try again."

    override fun errorSessionRemoveUnknownKey(key: String): String =
        "The \"remove\" method has found an unknown key \"${key}\" in the session scope. I could not remove something I do not have in the first place. Please enter a valid key and try again."

    override fun errorValidateEmptyFilesList(lines: String): String =
        "I read a directive ${lines} and found out that the provided \"files\" list is empty. This is an easy fix: make sure the list has at least one element and try again."

    override fun errorValidateFilesIsNotAList(lines: String): String =
        "I read a directive ${lines} and found out that \"files\" requires a list. Please make sure to correct the type to a proper list and try again."

    override fun errorValidateInvalidDirectiveFormat(lines: String): String =
        "I spotted an invalid directive ${lines} in the provided file. Make sure to fix the directive and try again."

    override val errorValidateNoDirectivesFound: String =
        "It looks like no directives were found in the provided file. Make sure to include at least one directive and try again."

    override fun errorValidateOrphanLinebreak(line: String): String =
        "Apparently there is an orphan directive line break in line ${line}. I cannot proceed. Please correct the directive and try again."

    override fun errorValidateReferenceIsReserved(lines: String): String =
        "I read a directive ${lines} and found out that the key \"reference\" was used. This key is reserved, so you cannot use it. But do not worry, this should be an easy fix. Just replace it by another name."

    override fun errorValidateYamlException(lines: String): String =
        "There was a problem with the provided YAML map in a directive ${lines}. This part is quite tricky, since it involves aspects of the underlying data serialization format."

    override fun errorValidateBodyArgumentIdIsReserved(identifier: String): String =
        "The argument identifier \"${identifier}\" is reserved, so you cannot use it. This should be an easy fix. Just replace it by another name."

    override val errorValidateBodyDuplicateArgumentIdentifiers: String =
        "Apparently you have duplicate argument identifiers in your rule. Make sure to fix this issue and try again."

    override val errorValidateBodyMissingKeys: String =
        "When defining a rule argument scope, at least 'flag' or 'default' must be used. Please, make sure to use at least one of them."

    override val errorValidateBodyNullArgumentId: String =
        "I found out that one of the arguments has no identifier. Please, make sure to add a valid identifier to the argument and try again."

    override val errorValidateBodyNullCommand: String =
        "I found a null command in the provided rule. This should be an easy fix. Make sure to add a valid command to the rule."

    override val errorValidateHeaderNullName: String =
        "The provided rule has no name. This should be an easy fix. Make sure to add a valid name and try again."

    override fun errorValidateHeaderWrongIdentifier(expected: String, actual: String): String =
        "The rule has a wrong identifier. I was expecting \"${expected}\", but found \"${actual}\". This should be an easy fix: just replace the wrong identifier by the correct one."

    override val infoLabelProject = "Project:"

    override val infoDisplaySummaryTitle = "Execution summary"

    override val infoDisplaySummaryFile = "File"

    override val infoDisplaySummaryTasks = "Tasks"

    override val infoDisplaySummaryTime = "Time"

    override val infoDisplayFullOutputInLog =
        "the full output is in the log file"

    override fun infoDisplayTotalSuccess(files: Int, tasks: Int) =
        "TOTAL SUCCESS · ${if (files == 1) "1 file" else "$files files"}, " +
            "${if (tasks == 1) "1 task" else "$tasks tasks"}"

    override fun infoDisplayTotalFailure(files: Int, tasks: Int) =
        "TOTAL FAILURE · ${if (files == 1) "1 file" else "$files files"}, " +
            "${if (tasks == 1) "1 task" else "$tasks tasks"}"

    override fun infoDisplayDryRun(tasks: Int) =
        "DRY RUN · ${if (tasks == 1) "1 task" else "$tasks tasks"}"

    override val infoConsoleSkipped: String =
        "Skipped"

    override val infoConsoleUnprocessed: String =
        "Not processed"

    override val infoDisplayExceptionMoreDetails: String =
        "There are more details available on this exception:"

    override fun infoDisplayExecutionTime(time: String): String =
        "Total: ${time} seconds"

    override fun infoDisplayFileInformation(name: String, size: String, lastModified: String): String =
        "Processing \"${name}\" (size: ${size}, last modified: ${lastModified}), please wait."

    override fun infoInterpreterDryRunModeBooleanMode(value: Boolean): String =
        "Although executing in dry-run mode, this entry might be already processed since it returned a boolean value: ${value}"

    override fun infoInterpreterDryRunModeSystemCommand(command: String): String =
        "About to run: ${command}"

    override val infoLabelAuthor: String =
        "Author:"

    override val infoLabelAuthors: String =
        "Authors:"

    override val infoLabelSystemCommand = "System command:"

    override val infoLabelConditional: String =
        "Conditional:"

    override val infoLabelNoAuthors: String =
        "No authors provided"

    override val infoLabelOnWarning: String =
        "WARNING"

    override val infoLabelOnQuestion: String =
        "QUESTION"

    override val infoLabelOnInfo: String =
        "INFO"

    override val infoLabelDialogPrompt: String =
        "Choose an option"

    override val infoLabelOnDetails: String =
        "DETAILS"

    override val infoLabelOnError: String =
        "ERROR"

    override val infoLabelOnFailure: String =
        "FAILURE"

    override val infoLabelOnSuccess: String =
        "SUCCESS"

    override val infoLabelUnnamedTask: String =
        "Unnamed task"

    override val infoParserNotes: String =
        "arara is released under the New BSD license."

    override val logInfoBeginBuffer: String =
        "BEGIN OUTPUT BUFFER"

    override fun logInfoBooleanMode(value: Boolean): String =
        "Boolean value: ${value}"

    override val logInfoDirectivesBlock: String =
        "DIRECTIVES"

    override val logInfoEndBuffer: String =
        "END OUTPUT BUFFER"

    override fun logInfoInterpretRule(rule: String): String =
        "I am ready to interpret rule \"${rule}\"."

    override fun logInfoInterpretTask(task: String, rule: String): String =
        "I am ready to interpret task \"${task}\" from rule \"${rule}\"."

    override fun logInfoPotentialPatternFound(line: String, pattern: String): String =
        "I found a potential pattern in line ${line}: ${pattern}"

    override fun logInfoRuleLocation(path: String): String =
        "Rule location: \"${path}\""

    override fun logInfoSystemCommand(command: String): String =
        "System command: ${command}"

    override val logInfoTaskResult: String =
        "Task result:"

    override val logInfoValidatedDirectives: String =
        "All directives were validated. We are good to go."

    override fun logInfoWelcomeMessage(version: String): String =
        "Welcome to arara ${version}!"
}
