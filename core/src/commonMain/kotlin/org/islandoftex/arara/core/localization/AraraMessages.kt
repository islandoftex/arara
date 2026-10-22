// SPDX-License-Identifier: BSD-3-Clause
package org.islandoftex.arara.core.localization

import org.islandoftex.arara.api.localization.MPPLocale

/**
 * Localized messages. Every locale implements the complete catalog; formatting
 * arguments are named and interpolated literally by each implementation.
 */
@Suppress("TooManyFunctions")
interface AraraMessages {
    /** The locale of this catalog. */
    val providedLocale: MPPLocale

    /** The translators credited for this catalog. */
    val translators: List<String>

    /** A dialog could not read an answer because terminal input ended. */
    val errorDialogInputClosed: String

    /** A dialog was requested without a host renderer. */
    val errorDialogsNotConfigured: String

    /** Script field type error; argument: field path. */
    fun errorScriptString(field: String): String

    /** Script table type error; argument: field path. */
    fun errorScriptTable(field: String): String

    /** Script boolean type error; argument: field path. */
    fun errorScriptBoolean(field: String): String

    /** Script integer type error; argument: field path. */
    fun errorScriptInteger(field: String): String

    /** Non-contiguous script list; argument: field path. */
    fun errorScriptList(field: String): String

    /** Unknown script key; argument: key path. */
    fun errorScriptUnknownKey(field: String): String

    /** Missing script value; argument: field path. */
    fun errorScriptRequired(field: String): String

    /** Blank script string; argument: field path. */
    fun errorScriptNonblank(field: String): String

    /** Duplicate script entry; arguments: field path and repeated value. */
    fun errorScriptDuplicate(field: String, value: String): String

    /** A directory was supplied as a file; argument: field path. */
    fun errorScriptDirectory(field: String): String

    /** Empty required collection; argument: field path. */
    fun errorScriptEmpty(field: String): String

    /** Invalid script mapping keys; argument: field path. */
    fun errorScriptStringKeys(field: String): String

    /** Project script failure; arguments: script path and interpreter detail. */
    fun errorProjectInvalidLua(path: String, detail: String): String

    /** Duplicate project names; argument: conflicting names. */
    fun errorProjectDuplicateNames(names: String): String

    /** Unknown project dependency; arguments: project and dependency names. */
    fun errorProjectUnknownDependency(project: String, dependency: String): String

    /**
     * The error shown when the project dependencies contain a cycle.
     */
    fun errorProjectCycle(cycle: String): String

    /** Shared project input; arguments: file path and both project names. */
    fun errorProjectFileOwnership(path: String, owner: String, project: String): String

    /** Unrecognized project input type; argument: file path. */
    fun errorProjectFiletype(path: String): String

    /** Invalid preamble prepend policy; argument: supplied value. */
    fun errorConfigurationPrepend(value: String): String

    /** Script function type error; argument: field path. */
    fun errorScriptFunction(field: String): String

    /** Non-literal script value; argument: field path. */
    fun errorScriptLiteral(field: String): String

    /** Missing alternative fields; arguments: parent path and permitted alternatives. */
    fun errorScriptAlternatives(field: String, alternatives: String): String

    /** Wrapper for Lua option interpolation failures. */
    val errorLuaInterpolationFailure: String

    /** MVEL interpolation syntax was used with the Lua engine. */
    val errorYamlTagsInLua: String

    /** Lua interpolation syntax was used with the YAML engine. */
    val errorLuaTagsInYaml: String

    /** Missing closing delimiter in a Lua interpolation tag. */
    val errorLuaUnterminatedTag: String

    /** Missing closing quote in a Lua interpolation expression. */
    val errorLuaUnterminatedQuote: String

    /** Missing closing delimiter in a Lua long string. */
    val errorLuaUnterminatedLongString: String

    /** Distinct Kotlin map keys produce the same Lua string key. */
    val errorLuaMapKeys: String

    /** Distinct Lua table keys produce the same Kotlin string key. */
    val errorLuaTableKeys: String

    /** Lossy Long-to-Lua conversion; argument: integer value. */
    fun errorLuaExactInteger(value: String): String

    /** A cyclic collection crossed the Kotlin/Lua value boundary. */
    val errorLuaCyclicValues: String

    /** Helper argument error; arguments: position, helper name, expected type and actual type. */
    fun errorLuaBadArgument(index: Int, helper: String, expected: String, actual: String): String

    /** Helper result error; arguments: helper name and actual result type. */
    fun errorLuaBooleanResult(helper: String, actual: String): String

    /** Missing helper arguments; argument: helper name. */
    fun errorLuaHelperNoArgs(helper: String): String

    /**
     * The error shown when the file database cannot be loaded.
     */
    fun errorLoadCouldNotLoadDatabase(name: String): String

    /**
     * The error shown when the file database cannot be saved.
     */
    fun errorSaveCouldNotSaveDatabase(name: String): String

    /**
     * The error shown when the basename helper is handed a directory
     * instead of a file.
     */
    fun errorBasenameNotAFile(path: String): String

    /**
     * The error shown when the hash of a file cannot be computed due to
     * an IO problem.
     */
    val errorCalculateHashIoException: String

    /**
     * The error shown when a string cannot be interpreted as a boolean
     * value.
     */
    fun errorCheckBooleanNotValidBoolean(value: String): String

    /**
     * The error shown when a file cannot be read during a regex check.
     */
    fun errorCheckRegexIoException(file: String): String

    /**
     * The error shown when the configuration file cannot be parsed.
     */
    val errorConfigurationGenericError: String

    /**
     * The error shown when the loop cap in the configuration is not a
     * positive number.
     */
    val errorConfigurationLoopsInvalidRange: String

    /**
     * The error shown when a referenced file cannot be found.
     */
    fun errorDiscoverFileFileNotFound(file: String, extensions: String): String

    /**
     * The error shown when a conditional expression cannot be compiled.
     */
    val errorEvaluateCompilationFailed: String

    /**
     * The error shown when a conditional does not evaluate to a boolean
     * value.
     */
    val errorEvaluateNotBooleanValue: String

    /**
     * The error shown when directives cannot be extracted due to an IO
     * problem.
     */
    val errorExtractorIoError: String

    /**
     * The error shown when an entry in the directive options cannot be
     * interpolated.
     */
    val errorExtractorInterpolationFailure: String

    /** Invalid configured directive pattern; argument: regular expression. */
    fun errorFiletypeInvalidPattern(pattern: String): String

    /**
     * The error shown when the filetype helper is handed a directory
     * instead of a file.
     */
    fun errorFiletypeNotAFile(path: String): String

    /**
     * The error shown when an unknown extension is to be resolved
     * without an explicit file type.
     */
    fun errorFiletypeUnknownExtension(extension: String, extensions: String): String

    /**
     * The error shown when the application path cannot be determined
     * due to an encoding problem.
     */
    val errorGetApplicationPathEncodingException: String

    /**
     * The error shown when a required rule argument is missing from the
     * directive.
     */
    fun errorInterpreterArgumentIsRequired(argument: String): String

    /**
     * The error shown when a rule command cannot be evaluated.
     */
    val errorInterpreterCommandRuntimeError: String

    /**
     * The error shown when a command exits with a code its exit check
     * rejects.
     */
    fun errorInterpreterCommandUnsuccessfulExit(command: String): String

    /**
     * The message shown when a rule or a directive asks arara to halt.
     */
    val errorInterpreterUserRequestedHalt: String

    /**
     * The error shown when the exit status expression of a rule command
     * cannot be evaluated.
     */
    val errorInterpreterExitRuntimeError: String

    /**
     * The error shown when the flag expression of a rule argument cannot
     * be evaluated.
     */
    val errorInterpreterFlagRuntimeException: String

    /** Unknown diagnostic log format; argument: supplied format. */
    fun errorLogFormatUnknown(value: String): String

    /** Unknown console presentation; argument: supplied mode. */
    fun errorConsoleModeUnknown(mode: String): String

    /** Unsupported rule format; arguments: file path and supplied format. */
    fun errorParseRuleUnsupportedFormat(identifier: String, format: String): String

    /**
     * The error shown when a rule cannot be found in the configured
     * paths.
     */
    fun errorInterpreterRuleNotFound(identifier: String, expected: String, paths: String): String

    /**
     * The error shown when a directive carries keys the rule does not
     * declare.
     */
    fun errorInterpreterUnknownKeys(keys: String): String

    /**
     * The error shown when the exit expression of a rule command does
     * not return a boolean value.
     */
    val errorInterpreterWrongExitClosureReturn: String

    /**
     * The error shown when a rule command evaluates to an unsupported
     * return type.
     */
    val errorInterpreterWrongReturnType: String

    /**
     * The error shown when a file is neither a project specification nor
     * a list of them.
     */
    val errorInvalidProjectFile: String

    /** Legacy cache loading diagnostic; argument: database path. */
    fun errorLoadCouldNotLoadXml(name: String): String

    /**
     * The error shown when the requested preamble is not defined in the
     * configuration.
     */
    fun errorParserInvalidPreamble(name: String): String

    /**
     * The error shown when a rule cannot be parsed.
     */
    val errorParseRuleGenericError: String

    /**
     * The error shown when list replication has more or fewer
     * parameters than expected.
     */
    val errorReplicateListMissingFormatArgumentsException: String

    /**
     * The header prepended to errors raised by a rule, naming the rule
     * and its location.
     */
    fun errorRuleIdentifierAndPath(identifier: String, location: String): String

    /**
     * The error shown when a system command fails for an unspecified
     * reason.
     */
    val errorRunGenericException: String

    /**
     * The error shown when a system command is interrupted.
     */
    val errorRunInterruptedException: String

    /**
     * The error shown when a system command reports an invalid exit
     * value.
     */
    val errorRunInvalidExitValueException: String

    /**
     * The error shown when a system command fails due to an IO problem.
     */
    val errorRunIoException: String

    /**
     * The error shown when a system command reaches the configured
     * timeout.
     */
    val errorRunTimeoutException: String

    /** Legacy cache writing diagnostic; argument: database path. */
    fun errorSaveCouldNotSaveXml(name: String): String

    /**
     * The error shown when a session key that is about to be read does
     * not exist.
     */
    fun errorSessionObtainUnknownKey(key: String): String

    /**
     * The error shown when a session key that is about to be removed
     * does not exist.
     */
    fun errorSessionRemoveUnknownKey(key: String): String

    /**
     * The error shown when the file list of a directive is empty.
     */
    fun errorValidateEmptyFilesList(lines: String): String

    /**
     * The error shown when the file list of a directive is not a list.
     */
    fun errorValidateFilesIsNotAList(lines: String): String

    /**
     * The error shown when a directive is malformed.
     */
    fun errorValidateInvalidDirectiveFormat(lines: String): String

    /**
     * The error shown when a file contains no directives at all.
     */
    val errorValidateNoDirectivesFound: String

    /**
     * The error shown when a directive line break has no continuation.
     */
    fun errorValidateOrphanLinebreak(line: String): String

    /**
     * The error shown when a directive uses the reserved reference key.
     */
    fun errorValidateReferenceIsReserved(lines: String): String

    /**
     * The error shown when the YAML map of a directive cannot be
     * processed.
     */
    fun errorValidateYamlException(lines: String): String

    /**
     * The error shown when a rule argument uses a reserved identifier.
     */
    fun errorValidateBodyArgumentIdIsReserved(identifier: String): String

    /**
     * The error shown when a rule declares duplicate argument
     * identifiers.
     */
    val errorValidateBodyDuplicateArgumentIdentifiers: String

    /**
     * The error shown when a rule argument has neither a flag nor a
     * default.
     */
    val errorValidateBodyMissingKeys: String

    /**
     * The error shown when a rule argument has no identifier.
     */
    val errorValidateBodyNullArgumentId: String

    /**
     * The error shown when a rule command entry is null.
     */
    val errorValidateBodyNullCommand: String

    /**
     * The error shown when a rule has no name.
     */
    val errorValidateHeaderNullName: String

    /**
     * The error shown when a rule identifier does not match the expected
     * one.
     */
    fun errorValidateHeaderWrongIdentifier(expected: String, actual: String): String

    /** Project heading in styled output. */
    val infoLabelProject: String

    /** Heading above the per-project file summaries. */
    val infoDisplaySummaryTitle: String

    /** File column heading. */
    val infoDisplaySummaryFile: String

    /** Evaluated task count column heading. */
    val infoDisplaySummaryTasks: String

    /** Elapsed file processing time column heading. */
    val infoDisplaySummaryTime: String

    /** Hint shown only when a truncated output excerpt has a complete log. */
    val infoDisplayFullOutputInLog: String

    /** Overall verdict and counts for reached files and evaluated tasks. */
    fun infoDisplayTotalSuccess(files: Int, tasks: Int): String

    /** Overall failure and counts for reached files and evaluated tasks. */
    fun infoDisplayTotalFailure(files: Int, tasks: Int): String

    /** Simulation label, without claiming that commands succeeded. */
    fun infoDisplayDryRun(tasks: Int): String

    /** Summary label for files intentionally skipped by execution. */
    val infoConsoleSkipped: String

    /** Summary label for files left unreached after execution stopped. */
    val infoConsoleUnprocessed: String

    /**
     * The label shown when an exception carries details in the log.
     */
    val infoDisplayExceptionMoreDetails: String

    /**
     * The message shown with the total execution time.
     */
    fun infoDisplayExecutionTime(time: String): String

    /**
     * The message shown while a file is being processed.
     */
    fun infoDisplayFileInformation(name: String, size: String, lastModified: String): String

    /**
     * The message shown when a boolean entry is met in dry-run mode.
     */
    fun infoInterpreterDryRunModeBooleanMode(value: Boolean): String

    /**
     * The message shown instead of running a command in dry-run mode.
     */
    fun infoInterpreterDryRunModeSystemCommand(command: String): String

    /**
     * The label shown for a single rule author.
     */
    val infoLabelAuthor: String

    /**
     * The label shown for multiple rule authors.
     */
    val infoLabelAuthors: String

    /**
     * The label shown for the conditional of an entry in dry-run mode.
     */
    val infoLabelConditional: String

    /**
     * The label shown when a rule has no authors.
     */
    val infoLabelNoAuthors: String

    /** The warning dialog label. */
    val infoLabelOnWarning: String

    /** The question dialog label. */
    val infoLabelOnQuestion: String

    /** The information dialog label. */
    val infoLabelOnInfo: String

    /** The numbered selection prompt; the range is appended literally. */
    val infoLabelDialogPrompt: String

    /**
     * The label shown for the details of an entry.
     */
    val infoLabelOnDetails: String

    /**
     * The label shown when an entry raises an error.
     */
    val infoLabelOnError: String

    /**
     * The label shown when an entry fails.
     */
    val infoLabelOnFailure: String

    /**
     * The label shown when an entry succeeds.
     */
    val infoLabelOnSuccess: String

    /**
     * The label shown for a rule command without a name.
     */
    val infoLabelUnnamedTask: String

    /**
     * The license note printed in the terminal.
     */
    val infoParserNotes: String

    /**
     * The log marker that starts the captured output buffer of a
     * command.
     */
    val logInfoBeginBuffer: String

    /**
     * The log line recording the value of a boolean entry.
     */
    fun logInfoBooleanMode(value: Boolean): String

    /**
     * The log heading of the block listing all directives.
     */
    val logInfoDirectivesBlock: String

    /**
     * The log marker that ends the captured output buffer of a command.
     */
    val logInfoEndBuffer: String

    /**
     * The log line recording that a rule is about to be interpreted.
     */
    fun logInfoInterpretRule(rule: String): String

    /**
     * The log line recording that a task of a rule is about to be
     * interpreted.
     */
    fun logInfoInterpretTask(task: String, rule: String): String

    /**
     * The log line recording a potential directive pattern found during
     * extraction.
     */
    fun logInfoPotentialPatternFound(line: String, pattern: String): String

    /**
     * The log line recording the directory a rule was resolved from.
     */
    fun logInfoRuleLocation(path: String): String

    /**
     * The log line recording the system command about to run.
     */
    fun logInfoSystemCommand(command: String): String

    /**
     * The log heading of the result of a task.
     */
    val logInfoTaskResult: String

    /**
     * The log line recording that all directives passed validation.
     */
    val logInfoValidatedDirectives: String

    /**
     * The log line greeting the user with the current version.
     */
    fun logInfoWelcomeMessage(version: String): String
}
