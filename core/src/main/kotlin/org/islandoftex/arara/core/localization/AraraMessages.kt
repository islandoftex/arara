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
     * The error shown when a referenced file cannot be found.
     */
    fun errorDiscoverFileFileNotFound(file: String, details: String): String
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
     * The error shown when a command exits with a code its exit check
     * rejects.
     */
    fun errorInterpreterCommandUnsuccessfulExit(command: String): String
    /**
     * The message shown when a rule or a directive asks arara to
     * halt.
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
     * The error shown when a rule cannot be parsed.
     */
    val errorParseRuleGenericError: String
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
     * The message shown with the total execution time.
     */
    fun infoDisplayExecutionTime(time: String): String
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
     * The error shown when a dialog awaits an answer and the terminal
     * input has already ended.
     */
    val errorDialogInputClosed: String
    /**
     * The label shown for a rule command without a name.
     */
    val infoLabelUnnamedTask: String
    /**
     * The license note printed in the terminal.
     */
    val infoParserNotes: String
}
