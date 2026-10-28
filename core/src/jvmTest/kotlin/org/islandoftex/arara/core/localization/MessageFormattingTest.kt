// SPDX-License-Identifier: BSD-3-Clause
package org.islandoftex.arara.core.localization

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue
import org.islandoftex.arara.api.AraraException
import org.islandoftex.arara.api.localization.AraraMessages
import org.islandoftex.arara.core.files.FileType

class MessageFormattingTest {
    @Test
    fun translatedTemplatesPreserveEveryArgumentLiterally() {
        val english = EnglishMessages
        val catalogs = listOf(english, GermanMessages, ItalianMessages, DutchMessages,
            BrazilianPortugueseMessages, FrenchMessages, NorfolkMessages)
        for (method in AraraMessages::class.java.methods.filter { it.returnType == String::class.java }) {
            val arguments = method.parameterTypes.mapIndexed { index, type ->
                when (type) {
                    Boolean::class.javaPrimitiveType -> true
                    Int::class.javaPrimitiveType -> index + 1
                    else -> "marker-${index + 1}-${'$'}-\\-%s"
                }
            }.toTypedArray<Any>()
            for (catalog in catalogs) {
                val rendered = method.invoke(catalog, *arguments) as String
                val context = "${catalog.providedLocale}: ${method.name}"
                assertTrue(rendered.isNotBlank(), context)
                arguments.forEach { assertTrue(rendered.contains(it.toString()), context) }
            }
        }
    }

    @Test
    fun invalidPatternsNameThePatternInEveryLocale() {
        val previous = LanguageController.messages
        try {
            for (catalog in listOf(EnglishMessages, GermanMessages, ItalianMessages, DutchMessages,
                BrazilianPortugueseMessages, FrenchMessages, NorfolkMessages)) {
                LanguageController.messages = catalog
                val failure = assertFailsWith<AraraException> { FileType("src", "[") }
                assertEquals(catalog.errorFiletypeInvalidPattern("["), failure.message)
                assertTrue(failure.exception is IllegalArgumentException)
            }
        } finally {
            LanguageController.messages = previous
        }
        assertEquals("FALHA", BrazilianPortugueseMessages.infoLabelOnFailure)
    }
}
