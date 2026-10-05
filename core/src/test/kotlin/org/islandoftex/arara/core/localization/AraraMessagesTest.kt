// SPDX-License-Identifier: BSD-3-Clause
package org.islandoftex.arara.core.localization

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import org.islandoftex.arara.api.localization.MPPLocale

/**
 * Verifies the typed message catalogs: every locale provides the
 * locale it declares, every message renders a non-blank text, every
 * argument of a parameterized message reaches the rendering of every
 * locale, the success labels are the language's own, and the halt
 * message names the file in every language. The completeness of the
 * catalogs is not asserted here - the abstract members of the
 * interface make the compiler enforce that a locale without every
 * message cannot build.
 */
class AraraMessagesTest {
    private val catalogs = listOf(
        EnglishMessages,
        GermanMessages,
        ItalianMessages,
        BrazilianPortugueseMessages,
        DutchMessages,
        NorfolkMessages
    )

    private val locales = listOf(
        MPPLocale("en"),
        MPPLocale("de"),
        MPPLocale("it"),
        MPPLocale("pt-BR"),
        MPPLocale("nl"),
        MPPLocale("en-QN")
    )

    private fun assertRenders(messages: AraraMessages) {
        with(messages) {
            listOf(
                errorCalculateHashIoException,
                errorConfigurationGenericError,
                errorConfigurationLoopsInvalidRange,
                errorConfigurationLuaReturnNotATable,
                errorConfigurationFileTypeEntryNotATable,
                errorConfigurationFileTypeExtensionMissing,
                errorConfigurationFileTypeKeyRequiresTableList,
                errorEvaluateCompilationFailed,
                errorEvaluateNotBooleanValue,
                errorExtractorIoError,
                errorExtractorInterpolationFailure,
                errorExtractorOrbTagInLuaEngine,
                errorGetApplicationPathEncodingException,
                errorInterpreterCommandRuntimeError,
                errorInterpreterUserRequestedHalt,
                errorInterpreterExitRuntimeError,
                errorInterpreterFlagRuntimeException,
                errorInterpreterWrongExitClosureReturn,
                errorInterpreterWrongReturnType,
                errorInvalidProjectFile,
                errorParseRuleGenericError,
                errorParseRuleCommandFunctionMissing,
                errorParseRuleCommandNameNotAString,
                errorParseRuleIdentifierMissing,
                errorParseRuleIdentifierNotAString,
                errorParseRuleNameNotAString,
                errorReplicateListMissingFormatArgumentsException,
                errorRunGenericException,
                errorRunInterruptedException,
                errorRunInvalidExitValueException,
                errorRunIoException,
                errorRunTimeoutException,
                errorValidateNoDirectivesFound,
                errorValidateBodyDuplicateArgumentIdentifiers,
                errorValidateBodyMissingKeys,
                errorValidateBodyNullArgumentId,
                errorValidateBodyNullCommand,
                errorValidateHeaderNullName,
                errorProjectDependenciesKeyRequiresStringList,
                errorProjectDependencyEntryNotAString,
                errorProjectDirectiveEntryNotAString,
                errorProjectDirectivesKeyRequiresTable,
                errorProjectFileEntryKeyNotAString,
                errorProjectFileIsADirectory,
                errorProjectFileSpecNotATable,
                errorProjectFilesKeyRequiresTable,
                errorProjectFileTypeExtensionRequired,
                errorProjectFileTypeKeyRequiresTable,
                errorProjectFileTypePatternMissing,
                errorProjectFileTypePatternNotAString,
                errorProjectListEntryNotATable,
                errorProjectListEntryNeedsName,
                errorProjectNameKeyRequiresString,
                errorProjectNeedsAFile,
                errorProjectPriorityKeyRequiresNumber,
                errorProjectReturnNotATable,
                errorProjectSingleProjectHasDependencies,
                errorProjectWorkingDirectoryKeyRequiresString,
                infoDisplayExceptionMoreDetails,
                infoDisplayFullOutputInLog,
                infoDisplaySummaryTitle,
                infoDisplaySummaryFile,
                infoDisplaySummaryTasks,
                infoDisplaySummaryTime,
                infoDisplayUnprocessedFiles(4242),
                infoLabelAboutToRun,
                infoLabelSystemCommand,
                errorDialogsNotConfigured,
                errorSafeModeClassLoading,
                errorSafeModeSystemCommand,
                errorConfigurationFileTypePatternNotAString,
                infoLabelAuthor,
                infoLabelAuthors,
                infoLabelConditional,
                infoLabelNoAuthors,
                infoLabelOnDetails,
                infoLabelOnError,
                infoLabelOnFailure,
                infoLabelOnSuccess,
                infoLabelOnWarning,
                infoLabelOnQuestion,
                infoLabelOnInfo,
                infoLabelDialogPrompt,
                errorDialogInputClosed,
                infoLabelUnnamedTask,
                infoParserNotes,
                errorBasenameNotAFile("alpha"),
                errorCheckBooleanNotValidBoolean("alpha"),
                errorCheckRegexIoException("alpha"),
                errorConfigurationKeyRequiresString("alpha"),
                errorConfigurationKeyRequiresInteger("alpha"),
                errorConfigurationKeyRequiresBoolean("alpha"),
                errorConfigurationEntryNotAString("alpha"),
                errorConfigurationKeyRequiresStringList("alpha"),
                errorConfigurationKeyRequiresTable("alpha"),
                errorConfigurationKeyRequiresStringKeys("alpha"),
                errorConfigurationKeyRequiresStringValues("alpha"),
                errorDiscoverFileFileNotFound("alpha", "beta"),
                errorDiscoverFileUnknownExtension("alpha", "beta"),
                errorDiscoverFileUnknownLuaExtension("alpha", "beta"),
                errorDiscoverFileIsAProjectSpecification("alpha"),
                errorFiletypeInvalidPattern("alpha"),
                errorFiletypeNotAFile("alpha"),
                errorFiletypeUnknownExtension("alpha", "beta"),
                errorGlobPatternInvalid("alpha", "beta"),
                errorInterpreterArgumentIsRequired("alpha"),
                errorInterpreterCommandUnsuccessfulExit("alpha"),
                errorInterpreterHaltStatusNotANumber("alpha"),
                errorInterpreterRuleNotFound("alpha", "beta", "gamma"),
                errorInterpreterUnknownKeys("alpha"),
                errorLoadCouldNotLoadDatabase("alpha"),
                errorParserInvalidPreamble("alpha"),
                errorParseRuleUnsupportedFormat("alpha", "beta", "gamma"),
                errorParseRuleArgumentIdNotAString(),
                errorParseRuleArgumentNeedsFlagOrDefault("alpha"),
                errorParseRuleArgumentRequiredNotABoolean("alpha"),
                errorParseRuleEntryNotAString("alpha"),
                errorParseRuleEntryNotATable("alpha"),
                errorParseRuleKeyRequiresStringList("alpha"),
                errorParseRuleKeyRequiresTableList("alpha"),
                errorProjectCycle("alpha"),
                errorProjectDuplicateName("alpha"),
                errorProjectDuplicatePath("alpha"),
                errorProjectFileInMultipleProjects("alpha", "beta"),
                errorProjectInvalidLua("alpha"),
                errorProjectStrayDependency("alpha"),
                errorProjectFileTypeUnknownExtension("alpha"),
                errorProjectUnknownKey("alpha"),
                errorConfigurationUnknownKeys("alpha"),
                errorRuleIdentifierAndPath("alpha", "beta"),
                errorSaveCouldNotSaveDatabase("alpha"),
                errorSessionObtainUnknownKey("alpha"),
                errorSessionRemoveUnknownKey("alpha"),
                errorValidateEmptyFilesList("alpha"),
                errorValidateFilesIsNotAList("alpha"),
                errorValidateInvalidDirectiveFormat("alpha"),
                errorValidateOrphanLinebreak(7),
                errorValidateReferenceIsReserved("alpha"),
                errorValidateYamlException("alpha"),
                errorValidateBodyArgumentIdIsReserved("alpha"),
                errorValidateHeaderWrongIdentifier("alpha", "beta"),
                infoDisplayExecutionTime,
                infoDisplayFileInformation("alpha", "beta", "gamma"),
                infoDisplayTimeUnit,
                infoInterpreterDryRunModeBooleanMode(true),
                infoInterpreterDryRunModeSystemCommand("alpha"),
            ).forEach { rendered ->
                assertTrue(
                    rendered.isNotBlank(),
                    "a message of ${providedLocale} renders blank"
                )
            }
        }
    }

    /**
     * The parameterized messages, paired with the argument fixtures
     * their renderings must contain. The fixtures resemble no natural
     * text, and the assertion checks containment rather than
     * position: every locale orders its arguments as its grammar
     * asks.
     */
    private val parameterized: List<Pair<List<String>, (AraraMessages) -> String>> =
        listOf(
            listOf("quux1") to { it.errorBasenameNotAFile("quux1") },
            listOf("quux1") to { it.errorCheckBooleanNotValidBoolean("quux1") },
            listOf("quux1") to { it.errorCheckRegexIoException("quux1") },
            listOf("quux1", "quux2") to
                { it.errorDiscoverFileFileNotFound("quux1", "quux2") },
            listOf("quux1", "quux2") to
                { it.errorDiscoverFileUnknownExtension("quux1", "quux2") },
            listOf("quux1", "quux2") to
                { it.errorDiscoverFileUnknownLuaExtension("quux1", "quux2") },
            listOf("quux1") to
                { it.errorDiscoverFileIsAProjectSpecification("quux1") },
            listOf("quux1") to { it.errorFiletypeNotAFile("quux1") },
            listOf("quux1", "quux2") to
                { it.errorFiletypeUnknownExtension("quux1", "quux2") },
            listOf("quux1") to { it.errorInterpreterArgumentIsRequired("quux1") },
            listOf("quux1") to
                { it.errorInterpreterCommandUnsuccessfulExit("quux1") },
            listOf("quux1") to
                { it.errorInterpreterHaltStatusNotANumber("quux1") },
            listOf("quux1", "quux2", "quux3") to
                { it.errorInterpreterRuleNotFound("quux1", "quux2", "quux3") },
            listOf("quux1") to { it.errorInterpreterUnknownKeys("quux1") },
            listOf("quux1") to { it.errorLoadCouldNotLoadDatabase("quux1") },
            listOf("quux1") to { it.errorParserInvalidPreamble("quux1") },
            listOf("quux1", "quux2", "quux3") to
                { it.errorParseRuleUnsupportedFormat("quux1", "quux2", "quux3") },
            listOf("quux1") to
                { it.errorConfigurationKeyRequiresString("quux1") },
            listOf("quux1") to
                { it.errorConfigurationKeyRequiresInteger("quux1") },
            listOf("quux1") to
                { it.errorConfigurationKeyRequiresBoolean("quux1") },
            listOf("quux1") to
                { it.errorConfigurationEntryNotAString("quux1") },
            listOf("quux1") to
                { it.errorConfigurationKeyRequiresStringList("quux1") },
            listOf("quux1") to
                { it.errorConfigurationKeyRequiresTable("quux1") },
            listOf("quux1") to
                { it.errorConfigurationKeyRequiresStringKeys("quux1") },
            listOf("quux1") to
                { it.errorConfigurationKeyRequiresStringValues("quux1") },
            listOf("quux1") to { it.errorConfigurationUnknownKeys("quux1") },
            listOf("quux1") to
                { it.errorParseRuleArgumentNeedsFlagOrDefault("quux1") },
            listOf("quux1") to
                { it.errorParseRuleArgumentRequiredNotABoolean("quux1") },
            listOf("quux1") to { it.errorParseRuleEntryNotAString("quux1") },
            listOf("quux1") to { it.errorParseRuleEntryNotATable("quux1") },
            listOf("quux1") to
                { it.errorParseRuleKeyRequiresStringList("quux1") },
            listOf("quux1") to
                { it.errorParseRuleKeyRequiresTableList("quux1") },
            listOf("quux1") to { it.errorProjectInvalidLua("quux1") },
            listOf("quux1") to { it.errorProjectStrayDependency("quux1") },
            listOf("quux1") to { it.errorProjectDuplicateName("quux1") },
            listOf("quux1") to { it.errorProjectDuplicatePath("quux1") },
            listOf("quux1", "quux2") to
                { it.errorProjectFileInMultipleProjects("quux1", "quux2") },
            listOf("quux1") to
                { it.errorProjectFileTypeUnknownExtension("quux1") },
            listOf("quux1") to { it.errorProjectUnknownKey("quux1") },
            listOf("quux1", "quux2") to
                { it.errorProjectFileDirectivesExtraction("quux1", "quux2") },
            listOf("quux1") to
                { it.errorFiletypeInvalidPattern("quux1") },
            listOf("quux1", "quux2") to
                { it.errorGlobPatternInvalid("quux1", "quux2") },
            listOf("quux1") to { it.errorProjectCycle("quux1") },
            listOf("quux1") to { it.errorConsoleModeUnknown("quux1") },
            listOf("4242", "1717") to { it.infoDisplayTotalSuccess(4242, 1717) },
            listOf("4242", "1717") to { it.infoDisplayTotalFailure(4242, 1717) },
            listOf("4242") to { it.infoDisplayDryRun(4242) },
            listOf("quux1") to { it.infoDisplaySkippedConditional("quux1") },
            listOf("quux1", "quux2") to
                { it.errorRuleIdentifierAndPath("quux1", "quux2") },
            listOf("quux1") to { it.errorSaveCouldNotSaveDatabase("quux1") },
            listOf("quux1") to { it.errorSessionObtainUnknownKey("quux1") },
            listOf("quux1") to { it.errorSessionRemoveUnknownKey("quux1") },
            listOf("quux1") to { it.errorValidateEmptyFilesList("quux1") },
            listOf("quux1") to { it.errorValidateFilesIsNotAList("quux1") },
            listOf("quux1") to { it.errorValidateInvalidDirectiveFormat("quux1") },
            listOf("7") to { it.errorValidateOrphanLinebreak(7) },
            listOf("quux1") to { it.errorValidateReferenceIsReserved("quux1") },
            listOf("quux1") to { it.errorValidateYamlException("quux1") },
            listOf("quux1") to
                { it.errorValidateBodyArgumentIdIsReserved("quux1") },
            listOf("quux1", "quux2") to
                { it.errorValidateHeaderWrongIdentifier("quux1", "quux2") },
            listOf("quux1", "quux2", "quux3") to
                { it.infoDisplayFileInformation("quux1", "quux2", "quux3") },
            listOf("true") to { it.infoInterpreterDryRunModeBooleanMode(true) },
            listOf("quux1") to
                { it.infoInterpreterDryRunModeSystemCommand("quux1") },
        )

    @Test
    fun `every locale provides the locale it declares`() {
        catalogs.zip(locales).forEach { (catalog, locale) ->
            assertEquals(locale, catalog.providedLocale, "the declared locale")
        }
    }

    @Test
    fun `every message renders a non blank text in every locale`() {
        catalogs.forEach { assertRenders(it) }
    }

    @Test
    fun `every argument reaches the rendering in every locale`() {
        catalogs.forEach { catalog ->
            parameterized.forEach { (arguments, render) ->
                val text = render(catalog)
                arguments.forEach { argument ->
                    assertTrue(
                        argument in text,
                        "the argument $argument does not reach the " +
                            "rendering of ${catalog.providedLocale}"
                    )
                }
            }
        }
    }

    @Test
    fun `the success label is the language's own`() {
        // the label flows to the console, so it must stay localized;
        // the frozen English token of the log lives at its own site
        // and never passes through here
        assertEquals("SUCCESS", EnglishMessages.infoLabelOnSuccess)
        assertEquals("ERFOLGREICH", GermanMessages.infoLabelOnSuccess)
        assertEquals("SUCCESS", ItalianMessages.infoLabelOnSuccess)
        assertEquals("SUCESSO", BrazilianPortugueseMessages.infoLabelOnSuccess)
        assertEquals("SUCCESVOL", DutchMessages.infoLabelOnSuccess)
        assertEquals(
            "THASS A MASTERLY JOB, MY BEWTY",
            NorfolkMessages.infoLabelOnSuccess
        )
    }

    @Test
    fun `the halt message names the file in every language`() {
        val fileWord = mapOf(
            MPPLocale("en") to "file",
            MPPLocale("de") to "Datei",
            MPPLocale("it") to "file",
            MPPLocale("pt-BR") to "arquivo",
            MPPLocale("nl") to "bestand",
            MPPLocale("en-QN") to "file"
        )
        catalogs.forEach { catalog ->
            assertTrue(
                fileWord.getValue(catalog.providedLocale) in
                    catalog.errorInterpreterUserRequestedHalt,
                "the halt message of ${catalog.providedLocale} " +
                    "does not name the file"
            )
        }
    }
}
