// SPDX-License-Identifier: BSD-3-Clause
package org.islandoftex.arara.core.localization

import org.islandoftex.arara.api.AraraException
import org.islandoftex.arara.api.localization.Messages
import org.islandoftex.arara.core.files.FileType
import org.islandoftex.arara.core.utils.formatString
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue

class MessageFormattingTest {
    @Test
    fun translatedTemplatesPreserveEveryArgumentLiterally() {
        val english = Messages()
        val catalogs = listOf(english, GermanLanguage(), ItalianLanguage(), DutchLanguage(),
            BrazilianLanguage(), NorfolkLanguage())
        for (getter in Messages::class.java.methods.filter {
            it.name.startsWith("get") && it.returnType == String::class.java && it.parameterCount == 0
        }) {
            val original = getter.invoke(english) as String
            val count = Regex("%s").findAll(original).count()
            val arguments = (1..count).map { "marker-$it-${'$'}-\\-%s" }.toTypedArray()
            for (catalog in catalogs) {
                val template = getter.invoke(catalog) as String
                val context = "${catalog.providedLocale}: ${getter.name}"
                assertEquals(count, Regex("%s").findAll(template).count(), context)
                val rendered = template.formatString(*arguments)
                arguments.forEach { assertTrue(rendered.contains(it), context) }
            }
        }
    }

    @Test
    fun invalidPatternsNameThePatternInEveryLocale() {
        val previous = LanguageController.messages
        try {
            for (catalog in listOf(Messages(), GermanLanguage(), ItalianLanguage(), DutchLanguage(),
                BrazilianLanguage(), NorfolkLanguage())) {
                LanguageController.messages = catalog
                val failure = assertFailsWith<AraraException> { FileType("src", "[") }
                assertEquals(catalog.ERROR_FILETYPE_INVALID_PATTERN.formatString("["), failure.message)
                assertTrue(failure.exception is IllegalArgumentException)
            }
        } finally {
            LanguageController.messages = previous
        }
        assertEquals("FALHA", BrazilianLanguage().INFO_LABEL_ON_FAILURE)
    }
}
