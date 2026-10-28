// SPDX-License-Identifier: BSD-3-Clause
package org.islandoftex.arara.core.configuration

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import org.islandoftex.arara.api.AraraException
import org.islandoftex.arara.core.localization.LanguageController
import org.islandoftex.arara.core.session.LinearExecutor

class SerialFileTypeTest {
    @Test
    fun explicitPatternsAllowUnknownExtensionsAndOverrideKnownDefaults() {
        listOf("custom", "tex").forEach { extension ->
            val type = SerialFileType(extension, "^#").toFileType()
            assertEquals(extension, type.extension)
            assertEquals("^#", type.pattern)
        }
    }

    @Test
    fun everyKnownExtensionHasItsDefaultPattern() {
        ConfigurationUtils.defaultFileTypePatterns.forEach { (extension, pattern) ->
            val type = SerialFileType(extension).toFileType()
            assertEquals(extension, type.extension)
            assertEquals(pattern, type.pattern)
        }
    }

    @Test
    fun unknownExtensionsRequirePatternsAndReportTheConfiguredChoices() {
        val failure = assertFailsWith<AraraException> { SerialFileType("unknown").toFileType() }
        assertEquals(LanguageController.messages.errorFiletypeUnknownExtension(
            "unknown", LinearExecutor.executionOptions.fileTypes.joinToString(" | ", "[ ", " ]")
        ), failure.message)
        assertFailsWith<AraraException> { SerialFileType("custom", "[").toFileType() }
    }
}
