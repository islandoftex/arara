// SPDX-License-Identifier: BSD-3-Clause
package org.islandoftex.arara.core.localization

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import org.islandoftex.arara.api.localization.MPPLocale

/**
 * Verifies the typed message catalogs: every locale provides the
 * locale it declares, every message renders a non-blank text, and the
 * labels are the language's own. The completeness of the catalogs is
 * not asserted here - the abstract members of the interface make the
 * compiler enforce that a locale without every message cannot build.
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
                infoDisplayExecutionTime("alpha"),
                infoDisplayFileInformation("alpha", "beta", "gamma"),
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
    fun `the success label is the language's own`() {
        // the label flows to the console, so it must stay localized;
        // the frozen English token of the log lives at its own site
        // and never passes through here
        assertEquals("SUCCESS", EnglishMessages.infoLabelOnSuccess)
        assertEquals("ERFOLGREICH", GermanMessages.infoLabelOnSuccess)
        assertEquals("SUCESSO", BrazilianPortugueseMessages.infoLabelOnSuccess)
    }
}
