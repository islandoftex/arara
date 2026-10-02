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
 * message names the tool in every language. The completeness of the
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
                infoDisplayExceptionMoreDetails,
                infoInterpreterLuaFileNotAProject,
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
                errorDiscoverFileFileNotFound("alpha", "beta"),
                errorFiletypeNotAFile("alpha"),
                errorFiletypeUnknownExtension("alpha", "beta"),
                errorInterpreterArgumentIsRequired("alpha"),
                errorInterpreterCommandUnsuccessfulExit("alpha"),
                errorInterpreterHaltStatusNotANumber("alpha"),
                errorInterpreterRuleNotFound("alpha", "beta", "gamma"),
                errorInterpreterUnknownKeys("alpha"),
                errorLoadCouldNotLoadDatabase("alpha"),
                errorParserInvalidPreamble("alpha"),
                errorParseRuleUnsupportedFormat("alpha", "beta", "gamma"),
                errorProjectCycle("alpha"),
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
            listOf("quux1") to { it.errorProjectCycle("quux1") },
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
    fun `the halt message names the tool in every language`() {
        catalogs.forEach { catalog ->
            assertTrue(
                "arara" in catalog.errorInterpreterUserRequestedHalt,
                "the halt message of ${catalog.providedLocale} " +
                    "does not name the tool"
            )
        }
    }
}
