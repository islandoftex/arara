// SPDX-License-Identifier: BSD-3-Clause
package org.islandoftex.arara.core.localization

import org.islandoftex.arara.api.localization.MPPLocale

/**
 * The message catalog arara localizes its interface with.
 *
 * Every message of the interface has one implementation per supported
 * locale. The members carry no defaults on purpose: a locale that is
 * missing a message fails to compile instead of silently falling back
 * to English, and the typed parameters of the functions make a wrong
 * argument count or type a compile error instead of a runtime
 * surprise. Zero-argument messages are values; parameterized messages
 * are functions whose implementations interpolate the arguments in
 * plain Kotlin strings, so every locale may order them as its grammar
 * asks.
 */
// the member count mirrors the message surface rather than accumulated
// responsibilities: every member is one localized message
@Suppress("TooManyFunctions")
interface AraraMessages {
    /**
     * The locale these messages are written for.
     */
    val providedLocale: MPPLocale

    /**
     * The error shown when the basename helper is handed a directory
     * instead of a file.
     */
    fun errorBasenameNotAFile(path: String): String
    /**
     * The error shown when the hash of a file cannot be computed due
     * to an IO problem.
     */
    val errorCalculateHashIoException: String
    /**
     * The error shown when a string cannot be interpreted as a
     * boolean value.
     */
    fun errorCheckBooleanNotValidBoolean(value: String): String
    /**
     * The error shown when a file cannot be read during a regex
     * check.
     */
    fun errorCheckRegexIoException(file: String): String
    /**
     * The error shown when the configuration file cannot be parsed.
     */
    val errorConfigurationGenericError: String
    /**
     * The error shown when the loop cap in the configuration is not
     * a positive number.
     */
    val errorConfigurationLoopsInvalidRange: String
    /**
     * The error shown when a Lua configuration file returns
     * something other than a table.
     */
    val errorConfigurationLuaReturnNotATable: String
    /**
     * The error shown when a Lua configuration carries keys the
     * format does not accept.
     */
    fun errorConfigurationUnknownKeys(keys: String): String
    /**
     * The error shown when a configuration key holds a value of
     * the wrong shape.
     */
    fun errorConfigurationKeyRequiresString(key: String): String
    /**
     * The error shown when a configuration key expects an integer.
     */
    fun errorConfigurationKeyRequiresInteger(key: String): String
    /**
     * The error shown when a configuration key expects a boolean.
     */
    fun errorConfigurationKeyRequiresBoolean(key: String): String
    /**
     * The error shown when an entry of a configuration list is not
     * a string.
     */
    fun errorConfigurationEntryNotAString(key: String): String
    /**
     * The error shown when a configuration key expects a list of
     * strings.
     */
    fun errorConfigurationKeyRequiresStringList(key: String): String
    /**
     * The error shown when a configuration key expects a table of
     * string pairs.
     */
    fun errorConfigurationKeyRequiresTable(key: String): String
    /**
     * The error shown when the keys of a configuration table are
     * not strings.
     */
    fun errorConfigurationKeyRequiresStringKeys(key: String): String
    /**
     * The error shown when the values of a configuration table are
     * not strings.
     */
    fun errorConfigurationKeyRequiresStringValues(key: String): String
    /**
     * The error shown when a file type declaration of the
     * configuration is not a table.
     */
    val errorConfigurationFileTypeEntryNotATable: String
    /**
     * The error shown when a file type declaration of the
     * configuration carries no extension.
     */
    val errorConfigurationFileTypeExtensionMissing: String
    /**
     * The error shown when the file type declarations of the
     * configuration are not a list of tables.
     */
    val errorConfigurationFileTypeKeyRequiresTableList: String
    /**
     * The error shown when the pattern of a file type declaration
     * of the configuration is not a string.
     */
    val errorConfigurationFileTypePatternNotAString: String
    /**
     * The error shown when the preamble application policy is
     * not one of the two words it accepts.
     */
    fun errorConfigurationPreamblePrependInvalid(value: String): String
    /**
     * The error shown when a referenced file cannot be found. The
     * extensions parameter carries the tried extensions as a plain
     * enumeration.
     */
    fun errorDiscoverFileFileNotFound(file: String, extensions: String): String
    /**
     * The error shown when a conditional expression cannot be
     * compiled.
     */
    val errorEvaluateCompilationFailed: String
    /**
     * The error shown when a conditional does not evaluate to a
     * boolean value.
     */
    val errorEvaluateNotBooleanValue: String
    /**
     * The error shown when directives cannot be extracted due to an
     * IO problem.
     */
    val errorExtractorIoError: String
    /**
     * The error shown when an entry in the directive options cannot
     * be interpolated.
     */
    val errorExtractorInterpolationFailure: String
    /**
     * The error shown when the Lua engine meets the deprecated orb
     * tag syntax of the YAML engine.
     */
    val errorExtractorOrbTagInLuaEngine: String
    /**
     * The error shown when the pattern a configuration declares
     * for a file type is not a valid regular expression.
     */
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
    fun errorFiletypeUnknownExtension(
        extension: String,
        extensions: String
    ): String
    /**
     * The error shown when the application path cannot be determined
     * due to an encoding problem.
     */
    val errorGetApplicationPathEncodingException: String
    /**
     * The error shown when a required rule argument is missing from
     * the directive.
     */
    fun errorInterpreterArgumentIsRequired(argument: String): String
    /**
     * The error shown when a rule command cannot be evaluated.
     */
    val errorInterpreterCommandRuntimeError: String
    /**
     * The message describing a command that exited with a code its
     * exit check rejected - the semantic record of the error-halt
     * landing, whose spoken witness lives in the log.
     */
    fun errorInterpreterCommandUnsuccessfulExit(command: String): String
    /**
     * The message carried by the halt of a file that a rule
     * requested: its remaining directives are skipped, and the
     * requested status becomes the file's own - the semantic
     * record of the halt landing, whose spoken witness lives in
     * the log.
     */
    val errorInterpreterUserRequestedHalt: String
    /**
     * The error shown when the exit status expression of a rule
     * command cannot be evaluated.
     */
    val errorInterpreterExitRuntimeError: String
    /**
     * The error shown when the flag expression of a rule argument
     * cannot be evaluated.
     */
    val errorInterpreterFlagRuntimeException: String
    /**
     * The error shown when the session value holding a halt status is
     * not a number.
     */
    fun errorInterpreterHaltStatusNotANumber(key: String): String
    /**
     * The error shown when a rule cannot be found in the configured
     * paths.
     */
    fun errorInterpreterRuleNotFound(
        identifier: String,
        expected: String,
        paths: String
    ): String
    /**
     * The error shown when a directive carries keys the rule does not
     * declare.
     */
    fun errorInterpreterUnknownKeys(keys: String): String
    /**
     * The error shown when the selected console presentation is not
     * one the application offers.
     */
    fun errorConsoleModeUnknown(mode: String): String
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
     * The error shown when a file is neither a project specification
     * nor a list of them.
     */
    val errorInvalidProjectFile: String
    /**
     * The error shown when the file database cannot be loaded.
     */
    fun errorLoadCouldNotLoadDatabase(name: String): String
    /**
     * The error shown when the requested preamble is not defined in
     * the configuration.
     */
    fun errorParserInvalidPreamble(name: String): String
    /**
     * The error shown when the argument identifier of a Lua rule is
     * not a string.
     */
    fun errorParseRuleArgumentIdNotAString(): String
    /**
     * The error shown when a Lua rule argument declares neither a
     * flag nor a default.
     */
    fun errorParseRuleArgumentNeedsFlagOrDefault(identifier: String): String
    /**
     * The error shown when the required flag of a Lua rule argument
     * is not a boolean.
     */
    fun errorParseRuleArgumentRequiredNotABoolean(identifier: String): String
    /**
     * The error shown when a Lua rule command declares no command
     * function.
     */
    val errorParseRuleCommandFunctionMissing: String
    /**
     * The error shown when the command name of a Lua rule is not a
     * string.
     */
    val errorParseRuleCommandNameNotAString: String
    /**
     * The error shown when an entry of a Lua rule list is not a
     * string.
     */
    fun errorParseRuleEntryNotAString(key: String): String
    /**
     * The error shown when an entry of a Lua rule list is not a
     * table.
     */
    fun errorParseRuleEntryNotATable(key: String): String
    /**
     * The error shown when a rule cannot be parsed.
     */
    val errorParseRuleGenericError: String
    /**
     * The error shown when a Lua rule declares no identifier.
     */
    val errorParseRuleIdentifierMissing: String
    /**
     * The error shown when the identifier of a Lua rule is not a
     * string.
     */
    val errorParseRuleIdentifierNotAString: String
    /**
     * The error shown when a Lua rule key expects a list of
     * strings.
     */
    fun errorParseRuleKeyRequiresStringList(key: String): String
    /**
     * The error shown when a Lua rule key expects a list of
     * tables.
     */
    fun errorParseRuleKeyRequiresTableList(key: String): String
    /**
     * The error shown when the rule name of a Lua rule is not a
     * string.
     */
    val errorParseRuleNameNotAString: String
    /**
     * The error shown when a rule file uses an unsupported format.
     */
    fun errorParseRuleUnsupportedFormat(
        rule: String,
        location: String,
        format: String
    ): String
    /**
     * The error shown when the project dependencies contain a cycle.
     */
    fun errorProjectCycle(cycle: String): String
    /**
     * The error shown when a project names a dependency no project
     * of the run carries.
     */
    fun errorProjectStrayDependency(dependency: String): String
    /**
     * The error shown when the dependencies of a project
     * specification are not a list of strings.
     */
    val errorProjectDependenciesKeyRequiresStringList: String
    /**
     * The error shown when a dependency entry of a project
     * specification is not a string.
     */
    val errorProjectDependencyEntryNotAString: String
    /**
     * The error shown when a directive entry of a project file
     * specification is not a string.
     */
    val errorProjectDirectiveEntryNotAString: String
    /**
     * The error shown when the directives of a project file
     * specification are not a table.
     */
    val errorProjectDirectivesKeyRequiresTable: String
    /**
     * The error shown when two projects of a list share a name.
     */
    fun errorProjectDuplicateName(name: String): String
    /**
     * The error shown when a project lists a file twice, possibly
     * under two spellings of the same location.
     */
    fun errorProjectDuplicatePath(name: String): String
    /**
     * The error shown when the directives a project file
     * specification carries cannot be extracted.
     */
    fun errorProjectFileDirectivesExtraction(
        file: String,
        details: String
    ): String
    /**
     * The error shown when a project file entry is not keyed by
     * its file name.
     */
    val errorProjectFileEntryKeyNotAString: String
    /**
     * The error shown when a project file entry names a directory.
     */
    val errorProjectFileIsADirectory: String
    /**
     * The error shown when a project file specification is not a
     * table.
     */
    val errorProjectFileSpecNotATable: String
    /**
     * The error shown when the files of a project specification
     * are not a table of file specifications.
     */
    val errorProjectFilesKeyRequiresTable: String
    /**
     * The error shown when a project file type declares no
     * extension.
     */
    val errorProjectFileTypeExtensionRequired: String
    /**
     * The error shown when the file type of a project file is not
     * a table.
     */
    val errorProjectFileTypeKeyRequiresTable: String
    /**
     * The error shown when a project file type declares a pattern
     * that neither it nor the defaults provide.
     */
    val errorProjectFileTypePatternMissing: String
    /**
     * The error shown when the pattern of a project file type is
     * not a string.
     */
    val errorProjectFileTypePatternNotAString: String
    /**
     * The error shown when a Lua project specification is not
     * valid Lua.
     */
    fun errorProjectInvalidLua(details: String): String
    /**
     * The error shown when an entry of a project list is not a
     * table.
     */
    val errorProjectListEntryNotATable: String
    /**
     * The error shown when a project of a list declares no name.
     */
    val errorProjectListEntryNeedsName: String
    /**
     * The error shown when the project name is not a string.
     */
    val errorProjectNameKeyRequiresString: String
    /**
     * The error shown when a project declares no file at all.
     */
    val errorProjectNeedsAFile: String
    /**
     * The error shown when the priority of a project file is not
     * a number.
     */
    val errorProjectPriorityKeyRequiresNumber: String
    /**
     * The error shown when a Lua project specification returns
     * something other than a table.
     */
    val errorProjectReturnNotATable: String
    /**
     * The error shown when a single project specification declares
     * dependencies.
     */
    val errorProjectSingleProjectHasDependencies: String
    /**
     * The error shown when the project specification carries keys
     * the format does not accept.
     */
    fun errorProjectUnknownKey(name: String): String
    /**
     * The error shown when the working directory of a project is
     * not a string.
     */
    val errorProjectWorkingDirectoryKeyRequiresString: String
    /**
     * The error shown when a glob pattern cannot be converted into
     * a regular expression.
     */
    fun errorGlobPatternInvalid(pattern: String, details: String): String
    /**
     * The error shown when list replication has more or fewer
     * parameters than expected.
     */
    val errorReplicateListMissingFormatArgumentsException: String
    /**
     * The header prepended to errors raised by a rule, naming the
     * rule and its location.
     */
    fun errorRuleIdentifierAndPath(
        identifier: String,
        location: String
    ): String
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
     * The error shown when a system command fails due to an IO
     * problem.
     */
    val errorRunIoException: String
    /**
     * The error shown when a system command reaches the configured
     * timeout.
     */
    val errorRunTimeoutException: String
    /**
     * The error shown when a rule tries to load classes from an
     * archive in safe mode.
     */
    val errorSafeModeClassLoading: String
    /**
     * The error shown when a rule tries to run a system command
     * in safe mode.
     */
    val errorSafeModeSystemCommand: String
    /**
     * The error shown when the file database cannot be saved.
     */
    fun errorSaveCouldNotSaveDatabase(name: String): String
    /**
     * The error shown when a session key that is about to be read
     * does not exist.
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
     * The error shown when the file list of a directive is not a
     * list.
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
     * The error shown when a directive line break has no
     * continuation.
     */
    fun errorValidateOrphanLinebreak(line: Int): String
    /**
     * The error shown when a directive uses the reserved reference
     * key.
     */
    fun errorValidateReferenceIsReserved(lines: String): String
    /**
     * The error shown when the YAML map of a directive cannot be
     * processed.
     */
    fun errorValidateYamlException(lines: String): String
    /**
     * The error shown when a rule argument uses a reserved
     * identifier.
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
     * The error shown when a rule identifier does not match the
     * expected one.
     */
    fun errorValidateHeaderWrongIdentifier(
        expected: String,
        found: String
    ): String
    /**
     * The label shown when an exception carries details in the log.
     */
    val infoDisplayExceptionMoreDetails: String
    /**
     * The label of the closing time line.
     */
    val infoDisplayExecutionTime: String
    /**
     * The localized unit the closing time line measures in.
     */
    val infoDisplayTimeUnit: String
    /**
     * The verdict line of a successful styled run.
     */
    fun infoDisplayTotalSuccess(files: Int, tasks: Int): String
    /**
     * The verdict line of a failed styled run.
     */
    fun infoDisplayTotalFailure(files: Int, tasks: Int): String
    /**
     * The verdict line of a styled dry run.
     */
    fun infoDisplayDryRun(tasks: Int): String
    /**
     * The message naming a directive skipped by its conditional.
     */
    fun infoDisplaySkippedConditional(name: String): String
    /**
     * The message pointing at the log file from the tail panel of a
     * failed task.
     */
    val infoDisplayFullOutputInLog: String
    /**
     * The title of the execution summary table.
     */
    val infoDisplaySummaryTitle: String
    /**
     * The file column header of the execution summary table.
     */
    val infoDisplaySummaryFile: String
    /**
     * The task column header of the execution summary table.
     */
    val infoDisplaySummaryTasks: String
    /**
     * The time column header of the execution summary table.
     */
    val infoDisplaySummaryTime: String
    /**
     * The label naming the files that were requested but never
     * reached, when an exception stopped the run before their
     * turn.
     */
    fun infoDisplayUnprocessedFiles(count: Int): String
    /**
     * The message shown while a file is being processed.
     */
    fun infoDisplayFileInformation(
        name: String,
        size: String,
        lastModified: String
    ): String
    /**
     * The message shown when a boolean entry is met in dry-run mode.
     */
    fun infoInterpreterDryRunModeBooleanMode(value: Boolean): String
    /**
     * The message shown instead of running a command in dry-run
     * mode.
     */
    fun infoInterpreterDryRunModeSystemCommand(command: String): String
    /**
     * The message shown when evaluating a provided Lua file as a
     * project specification raised an unexpected error.
     */
    val infoInterpreterLuaFileEvaluationFailed: String
    /**
     * The message shown when a provided Lua file is not a project
     * specification.
     */
    val infoInterpreterLuaFileNotAProject: String
    /**
     * The label shown for a single rule author.
     */
    val infoLabelAuthor: String
    /**
     * The label shown for multiple rule authors.
     */
    val infoLabelAuthors: String
    /**
     * The label shown for the conditional of an entry in dry-run
     * mode.
     */
    val infoLabelConditional: String
    /**
     * The label shown when a rule has no authors.
     */
    val infoLabelNoAuthors: String
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
     * The tag shown by a warning dialog.
     */
    val infoLabelOnWarning: String
    /**
     * The tag shown by a question dialog.
     */
    val infoLabelOnQuestion: String
    /**
     * The tag shown by an informational dialog.
     */
    val infoLabelOnInfo: String
    /**
     * The prompt of a dialog that asks for a numbered choice.
     */
    val infoLabelDialogPrompt: String
    /**
     * The label naming the command a dry run is about to run.
     */
    val infoLabelAboutToRun: String
    /**
     * The label of the verbose field naming the command a task
     * executes.
     */
    val infoLabelSystemCommand: String
    /**
     * The error shown when a dialog awaits an answer and the terminal
     * input has already ended.
     */
    val errorDialogInputClosed: String
    /**
     * The error shown when a rule reaches a dialog and the run has
     * no dialog support configured.
     */
    val errorDialogsNotConfigured: String
    /**
     * The label shown for a rule command without a name.
     */
    val infoLabelUnnamedTask: String
    /**
     * The license note printed in the terminal.
     */
    val infoParserNotes: String
}
