// SPDX-License-Identifier: BSD-3-Clause
package org.islandoftex.arara.core.localization

import org.islandoftex.arara.api.localization.Messages
import org.islandoftex.arara.core.utils.formatString
import java.util.Properties
import kotlin.test.Test
import kotlin.test.assertEquals

class MessageRenderingContractTest {
    @Test
    fun `every translation retains its rendered text and literal arguments`() {
        val catalogs = listOf(Messages(), GermanLanguage(), ItalianLanguage(), DutchLanguage(),
            BrazilianLanguage(), NorfolkLanguage()).associateBy { it.javaClass.simpleName }
        val rendered = Properties()
        requireNotNull(javaClass.getResourceAsStream("/localization/rendered-messages.properties"))
            .bufferedReader(Charsets.UTF_8).use { rendered.load(it) }
        for (key in rendered.stringPropertyNames()) {
            val locale = key.substringBefore('.')
            val member = key.substringAfter('.')
            val catalog = catalogs.getValue(locale)
            val template = Messages::class.java.getMethod("get$member").invoke(catalog) as String
            val arguments = (1..Regex("%s").findAll(template).count()).map {
                "marker-$it-${'$'}-\\-%s"
            }.toTypedArray()
            val expected = rendered.getProperty(key)
            assertEquals(expected, template.formatString(*arguments), "$locale: $member")
        }
        assertEquals(catalogs.size * Messages::class.java.methods.count {
            it.name.startsWith("get") && it.returnType == String::class.java
        }, rendered.size)
    }
}
