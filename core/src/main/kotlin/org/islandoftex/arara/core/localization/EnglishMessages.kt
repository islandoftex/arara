// SPDX-License-Identifier: BSD-3-Clause
package org.islandoftex.arara.core.localization

import org.islandoftex.arara.api.localization.MPPLocale

/**
 * English localization of the arara messages.
 * Written by Paulo Cereda.
 */
@Suppress("MaxLineLength", "TooManyFunctions")
object EnglishMessages : AraraMessages {
    override val providedLocale = MPPLocale("en")

    override fun errorBasenameNotAFile(path: String) =
        "The \"basename\" method requires a file, not a directory. It looks like \"${path}\" does not appear to be a file at all. If you need to perform tasks on a directory, you could use a couple of methods from the Java API."

    override val errorCalculateHashIoException =
        "For whatever reason, I could not calculate the hash. I have no idea why it failed, though. Perhaps the file was moved or deleted before or during the hashing operation. Or maybe I do not have the proper permissions to read the file."

    override fun errorCheckBooleanNotValidBoolean(value: String) =
        "It looks like \"${value}\" is not a valid boolean value. This should be an easy fix. Make sure to use a valid string that represents boolean values (yes and no, true and false, 1 and 0, and on and off)."

    override fun errorCheckRegexIoException(file: String) =
        "I could not read the contents of the file \"${file}\", I got an IO error. I have no idea why it failed, though. Perhaps the file was moved or deleted before or during the reading operation. Or maybe I do not have the proper permissions to read the file."

    override val errorConfigurationGenericError =
        "I could not parse the configuration file, something bad happened. This part is quite tricky, since it involves aspects of the underlying data serialization format. I will do my best to help you in any way I can."

    override val errorConfigurationLoopsInvalidRange =
        "The value defined in the 'loops' key in the configuration file in order to denote the maximum number of loops has an invalid range. Please make sure to use a positive long value."

    override fun errorDiscoverFileFileNotFound(file: String, details: String) =
        "I could not find the provided file \"${file}\" ${details}. Please make sure the file exists and it has a valid extension."

    override val errorEvaluateCompilationFailed =
        "For whatever reason, I could not compile the expression in the provided conditional. This part is quite tricky, since it involves aspects of the underlying expression language. I will do my best to help you in any way I can."

    override val errorEvaluateNotBooleanValue =
        "The conditional evaluation was expecting a boolean value as result. This should be an easy fix. Just make sure the conditional evaluation resolves to a boolean value in the end."

    override val errorExtractorIoError =
        "There was an IO error while I was trying to extract the directives. I have no idea why it failed, though. Perhaps the file was moved or deleted before or during the extraction operation. Or maybe the file uses a different encoding than UTF-8. Or maybe I do not have the proper permissions to read the file."

    override val errorExtractorInterpolationFailure =
        "Your 'options' field contains an entry the MVEL parser is unable to cope with. Please revise your options not to contain any malformed '@{…}' parts."

    override val errorExtractorOrbTagInLuaEngine =
        "Your 'options' field contains an orb tag of the deprecated YAML engine, which the Lua engine cannot interpolate. Please rewrite the entry with '{{ … }}' tags instead."

    override fun errorFiletypeNotAFile(path: String) =
        "The \"filetype\" method requires a file, not a directory. It looks like \"${path}\" does not appear to be a file at all. If you need to perform tasks on a directory, you could use a couple of methods from the Java API."

    override fun errorFiletypeUnknownExtension(extension: String, extensions: String) =
        "I cannot recognize \"${extension}\" as a default extension. If you want to define a new file type, make sure to provide the extension and pattern. These are the default extensions: ${extensions}"

    override val errorGetApplicationPathEncodingException =
        "There was an encoding problem while trying to obtain the application path. There is nothing much I can do about it."

    override fun errorInterpreterArgumentIsRequired(argument: String) =
        "It seems that \"${argument}\" is marked as required in the rule, but I could not find it in the directive parameters. Please make sure to add it as parameter for your directive and try again."

    override val errorInterpreterCommandRuntimeError =
        "I could not evaluate one of the provided commands. This part is quite tricky, since it involves aspects of the underlying expression language. I will do my best to help you in any way I can."

    override fun errorInterpreterCommandUnsuccessfulExit(command: String) =
        "The command \"${command}\" exited unexpectedly with a non-zero exit code. Please see the command's output to evaluate your next steps."

    override val errorInterpreterUserRequestedHalt =
        "arara will now halt on your (or a rule's) request to halt arara."

    override val errorInterpreterExitRuntimeError =
        "I could not evaluate the exit status expression of one of the provided commands. This part is quite tricky, since it involves aspects of the underlying expression language. I will do my best to help you in any way I can."

    override val errorInterpreterFlagRuntimeException =
        "I could not evaluate the flag expression of one of the arguments. This part is quite tricky, since it involves aspects of the underlying expression language. I will do my best to help you in any way I can."

    override fun errorInterpreterHaltStatusNotANumber(key: String) =
        "It looks like the value held under the halt session key \"${key}\" is not a valid number, so I cannot report the requested halt status. This should be an easy fix: make sure the halt status is a number and try again."

    override fun errorInterpreterRuleNotFound(identifier: String, expected: String, paths: String) =
        "I could not find a rule named \"${identifier}\" in the provided rule paths. Perhaps a misspelled word? I was looking for a file named \"${expected}\" in the following paths in order of priority: ${paths}"

    override fun errorInterpreterUnknownKeys(keys: String) =
        "I found these unknown keys in the directive: ${keys}. This should be an easy fix, just remove them from your map."

    override fun errorConsoleModeUnknown(mode: String) =
        "I cannot set the console to '${mode}', as it is not one of the available presentations. This should be an easy fix, just choose one of the available modes."

    override val errorInterpreterWrongExitClosureReturn =
        "The 'exit' expression must always return a boolean value (even if there is no computation in the closure body). This should be an easy fix: make sure to correct the type return statement and try again."

    override val errorInterpreterWrongReturnType =
        "A rule should only return a boolean or a command. Other return types are not supported."

    override val errorInvalidProjectFile =
        "I have read a file that was supposed to be a project. Unfortunately, it turned out it was neither a project nor a list of projects. Please double-check that your project file follows the documented format."

    override fun errorLoadCouldNotLoadDatabase(name: String) =
        "I could not load the YAML database named \"${name}\". I have no idea why it failed, though. Perhaps the file was moved or deleted before or during the reading operation. Or maybe I do not have the proper permissions to read the file. By the way, make sure the YAML file is well-formed."

    override fun errorParserInvalidPreamble(name: String) =
        "I am sorry, but the preamble \"${name}\" could not be found. Please make sure this key exists in the configuration file."

    override val errorParseRuleGenericError =
        "I could not parse the rule, something bad happened. This part is quite tricky, since it involves aspects of the underlying data serialization format. I will do my best to help you in any way I can."

    override fun errorParseRuleUnsupportedFormat(rule: String, location: String, format: String) =
        "I found an error in rule \"${rule}\" located at \"${location}\". The rule format of \"${format}\" is not supported by arara."

    override fun errorProjectCycle(cycle: String) =
        "The project dependencies contain a cycle involving ${cycle}. Cyclic dependencies cannot be resolved, so please revise your project specification."

    override val errorReplicateListMissingFormatArgumentsException =
        "I could not replicate the list due to a missing format argument. My guess is that there are less (or more) parameters than expected. Make sure to correct the number of parameters and try again."

    override fun errorRuleIdentifierAndPath(identifier: String, location: String) =
        "I have spotted an error in rule \"${identifier}\" located at \"${location}\"."

    override val errorRunGenericException =
        "I could not run the provided system command, something bad happened. This part is quite tricky, since it involves aspects of the underlying expression language. I will do my best to help you in any way I can."

    override val errorRunInterruptedException =
        "The provided system command execution was suddenly interrupted. Maybe there was an external interruption that forced the command to end abruptly."

    override val errorRunInvalidExitValueException =
        "The provided system command execution has returned an invalid exit value."

    override val errorRunIoException =
        "The system command execution has failed due to an IO error. Are you sure the provided system command exists in your path? It might be a good idea to check the path and see if the command is available."

    override val errorRunTimeoutException =
        "The system command execution reached the provided timeout value and was aborted. If the time was way too short, make sure to provide a longer value."

    override fun errorSaveCouldNotSaveDatabase(name: String) =
        "I could not save the YAML database named \"${name}\". I have no idea why it failed, though. Perhaps I do not have the proper permissions to write the YAML file to disk."

    override fun errorSessionObtainUnknownKey(key: String) =
        "The \"get\" method has found an unknown key \"${key}\" in the session scope. I could not get something I do not have in the first place. Please enter a valid key and try again."

    override fun errorSessionRemoveUnknownKey(key: String) =
        "The \"remove\" method has found an unknown key \"${key}\" in the session scope. I could not remove something I do not have in the first place. Please enter a valid key and try again."

    override fun errorValidateEmptyFilesList(lines: String) =
        "I read a directive ${lines} and found out that the provided \"files\" list is empty. This is an easy fix: make sure the list has at least one element and try again."

    override fun errorValidateFilesIsNotAList(lines: String) =
        "I read a directive ${lines} and found out that \"files\" requires a list. Please make sure to correct the type to a proper list and try again."

    override fun errorValidateInvalidDirectiveFormat(lines: String) =
        "I spotted an invalid directive ${lines} in the provided file. Make sure to fix the directive and try again."

    override val errorValidateNoDirectivesFound =
        "It looks like no directives were found in the provided file. Make sure to include at least one directive and try again."

    override fun errorValidateOrphanLinebreak(line: Int) =
        "Apparently there is an orphan directive line break in line ${line}. I cannot proceed. Please correct the directive and try again."

    override fun errorValidateReferenceIsReserved(lines: String) =
        "I read a directive ${lines} and found out that the key \"reference\" was used. This key is reserved, so you cannot use it. But do not worry, this should be an easy fix. Just replace it by another name."

    override fun errorValidateYamlException(lines: String) =
        "There was a problem with the provided YAML map in a directive ${lines}. This part is quite tricky, since it involves aspects of the underlying data serialization format."

    override fun errorValidateBodyArgumentIdIsReserved(identifier: String) =
        "The argument identifier \"${identifier}\" is reserved, so you cannot use it. This should be an easy fix. Just replace it by another name."

    override val errorValidateBodyDuplicateArgumentIdentifiers =
        "Apparently you have duplicate argument identifiers in your rule. Make sure to fix this issue and try again."

    override val errorValidateBodyMissingKeys =
        "When defining a rule argument scope, at least 'flag' or 'default' must be used. Please, make sure to use at least one of them."

    override val errorValidateBodyNullArgumentId =
        "I found out that one of the arguments has no identifier. Please, make sure to add a valid identifier to the argument and try again."

    override val errorValidateBodyNullCommand =
        "I found a null command in the provided rule. This should be an easy fix. Make sure to add a valid command to the rule."

    override val errorValidateHeaderNullName =
        "The provided rule has no name. This should be an easy fix. Make sure to add a valid name and try again."

    override fun errorValidateHeaderWrongIdentifier(expected: String, found: String) =
        "The rule has a wrong identifier. I was expecting \"${expected}\", but found \"${found}\". This should be an easy fix: just replace the wrong identifier by the correct one."

    override val infoDisplayExceptionMoreDetails =
        "There are more details available on this exception:"

    override val infoDisplayExecutionTime = "Elapsed time:"
    override val infoDisplayTimeUnit = "seconds"

    override fun infoDisplayTotalSuccess(files: Int, tasks: Int) =
        "TOTAL SUCCESS · ${if (files == 1) "1 file" else "$files files"}, " +
            "${if (tasks == 1) "1 task" else "$tasks tasks"}"

    override fun infoDisplayTotalFailure(files: Int, tasks: Int) =
        "TOTAL FAILURE · ${if (files == 1) "1 file" else "$files files"}, " +
            "${if (tasks == 1) "1 task" else "$tasks tasks"}"

    override fun infoDisplayDryRun(tasks: Int) =
        "DRY RUN · ${if (tasks == 1) "1 task" else "$tasks tasks"}"
    override fun infoDisplaySkippedConditional(name: String) =
        "${name} skipped · the conditional evaluated to false"

    override val infoDisplayFullOutputInLog =
        "the full output is in the log file"


    override val infoDisplaySummaryTitle = "Execution summary"
    override val infoDisplaySummaryFile = "File"
    override val infoDisplaySummaryTasks = "Tasks"
    override val infoDisplaySummaryTime = "Time"

        override fun infoDisplayUnprocessedFiles(count: Int) =
        if (count == 1) "File not processed: " else "Files not processed: "


    override fun infoDisplayFileInformation(name: String, size: String, lastModified: String) =
        "Processing \"${name}\" (size: ${size}, last modified: ${lastModified}), please wait."

    override fun infoInterpreterDryRunModeBooleanMode(value: Boolean) =
        "Although executing in dry-run mode, this entry might be already processed since it returned a boolean value: ${value}"

    override fun infoInterpreterDryRunModeSystemCommand(command: String) =
        "About to run: ${command}"

    override val infoInterpreterLuaFileNotAProject =
        "It looks like the provided Lua file could not be parsed as a project specification, so I will treat it as a regular input file. If it is meant to be a project specification, please check the log for details on what went wrong."

    override val infoLabelAuthor =
        "Author:"

    override val infoLabelAuthors =
        "Authors:"

    override val infoLabelConditional =
        "Conditional:"

    override val infoLabelNoAuthors =
        "No authors provided"

    override val infoLabelOnDetails =
        "Details"

    override val infoLabelOnError =
        "ERROR"

    override val infoLabelOnFailure =
        "FAILURE"

    override val infoLabelOnSuccess =
        "SUCCESS"
    override val infoLabelOnWarning = "WARNING"
    override val infoLabelOnQuestion = "QUESTION"
    override val infoLabelOnInfo = "INFO"
    override val infoLabelDialogPrompt = "Choose an option"
    override val infoLabelAboutToRun =
        "About to run:"
    override val infoLabelSystemCommand = "System command:"

    override val errorDialogInputClosed =
        "It looks like the terminal input ended while a dialog was waiting for an answer, so I cannot read your choice. Please run arara in an interactive terminal so the dialog can be answered."
    override val errorDialogsNotConfigured =
        "It looks like this run has no dialog support configured, so I cannot show the dialog a rule asked for. Please provide a dialog implementation in the application running arara, or avoid rules with interactive dialogs."
    override val infoLabelUnnamedTask =
        "Unnamed task"

    override val infoParserNotes =
        "arara is released under the New BSD license."
}
