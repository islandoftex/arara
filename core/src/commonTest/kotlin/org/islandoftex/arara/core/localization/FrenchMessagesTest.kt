// SPDX-License-Identifier: BSD-3-Clause
package org.islandoftex.arara.core.localization

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertSame
import org.islandoftex.arara.api.localization.MPPLocale

class FrenchMessagesTest {
    @Test
    fun frenchSelectionPreservesAccentsAndUsesADecimalComma() {
        val previous = LanguageController.messages
        try {
            LanguageController.loadMessagesFor(MPPLocale("fr"))
            assertSame(FrenchMessages, LanguageController.messages)
            assertEquals("ÉCHEC", LanguageController.messages.infoLabelOnFailure)
            assertEquals("SUCCÈS", LanguageController.messages.infoLabelOnSuccess)
            assertEquals(',', LanguageController.messages.providedLocale.decimalSeparator)
        } finally {
            LanguageController.messages = previous
        }
    }

    @Test
    fun summariesUseFrenchSingularAndPluralForms() {
        assertEquals("SUCCÈS TOTAL · 0 fichier, 0 tâche", FrenchMessages.infoDisplayTotalSuccess(0, 0))
        assertEquals("SUCCÈS TOTAL · 1 fichier, 1 tâche", FrenchMessages.infoDisplayTotalSuccess(1, 1))
        assertEquals("SUCCÈS TOTAL · 2 fichiers, 3 tâches", FrenchMessages.infoDisplayTotalSuccess(2, 3))
        assertEquals("ÉCHEC TOTAL · 1 fichier, 2 tâches", FrenchMessages.infoDisplayTotalFailure(1, 2))
        assertEquals("SIMULATION · 0 tâche", FrenchMessages.infoDisplayDryRun(0))
        assertEquals("SIMULATION · 1 tâche", FrenchMessages.infoDisplayDryRun(1))
        assertEquals("SIMULATION · 2 tâches", FrenchMessages.infoDisplayDryRun(2))
    }
}
