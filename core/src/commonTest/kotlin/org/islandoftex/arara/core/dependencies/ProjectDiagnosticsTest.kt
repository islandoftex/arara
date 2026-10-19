// SPDX-License-Identifier: BSD-3-Clause
package org.islandoftex.arara.core.dependencies

import org.islandoftex.arara.api.AraraException
import org.islandoftex.arara.api.files.MPPPath
import org.islandoftex.arara.api.localization.Messages
import org.islandoftex.arara.core.files.Project
import org.islandoftex.arara.core.localization.BrazilianLanguage
import org.islandoftex.arara.core.localization.DutchLanguage
import org.islandoftex.arara.core.localization.GermanLanguage
import org.islandoftex.arara.core.localization.ItalianLanguage
import org.islandoftex.arara.core.localization.LanguageController
import org.islandoftex.arara.core.localization.NorfolkLanguage
import org.islandoftex.arara.core.utils.formatString
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class ProjectDiagnosticsTest {
    @Test
    fun graphErrorsUseTheSelectedLanguageAndLiteralProjectNames() {
        val previous = LanguageController.messages
        val firstName = "A-${'$'}1-%s-\\"
        val first = Project(firstName, MPPPath("."), emptySet(), setOf("B"))
        val second = Project("B", MPPPath("."), emptySet(), setOf(firstName))
        val downstream = Project("Downstream", MPPPath("."), emptySet(), setOf("B"))
        try {
            for (messages in listOf(Messages(), GermanLanguage(), ItalianLanguage(), DutchLanguage(),
                BrazilianLanguage(), NorfolkLanguage())) {
                LanguageController.messages = messages
                val cycle = assertFailsWith<AraraException> {
                    ProjectValidation.executionOrder(listOf(downstream, first, second))
                }
                assertEquals(messages.ERROR_PROJECT_CYCLE.formatString("$firstName, B"), cycle.message)
                val duplicate = assertFailsWith<AraraException> {
                    ProjectValidation.executionOrder(listOf(first, first))
                }
                assertEquals(messages.ERROR_PROJECT_DUPLICATE_NAMES.formatString(firstName), duplicate.message)
                val missing = assertFailsWith<AraraException> { ProjectValidation.executionOrder(listOf(first)) }
                assertEquals(messages.ERROR_PROJECT_UNKNOWN_DEPENDENCY.formatString(firstName, "B"), missing.message)
            }
        } finally {
            LanguageController.messages = previous
        }
    }
}
