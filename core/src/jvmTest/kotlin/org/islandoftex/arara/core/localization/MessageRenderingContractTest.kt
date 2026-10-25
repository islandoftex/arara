// SPDX-License-Identifier: BSD-3-Clause
package org.islandoftex.arara.core.localization

import java.util.Properties
import kotlin.test.Test
import kotlin.test.assertEquals

class MessageRenderingContractTest {
    @Test
    fun `every translation retains its rendered text and literal arguments`() {
        val catalogs = listOf(EnglishMessages, GermanMessages, ItalianMessages, DutchMessages,
            BrazilianPortugueseMessages, FrenchMessages, NorfolkMessages)
        val expected = Properties()
        requireNotNull(javaClass.getResourceAsStream("/localization/rendered-messages.properties"))
            .bufferedReader(Charsets.UTF_8).use { expected.load(it) }
        val methods = AraraMessages::class.java.methods.filter { it.returnType == String::class.java }
        val keys = mutableSetOf<String>()
        for (catalog in catalogs) {
            for (method in methods) {
                val member = if (method.name.startsWith("get")) {
                    method.name.removePrefix("get").replaceFirstChar(Char::lowercase)
                } else method.name
                val key = "${catalog.javaClass.simpleName}.$member"
                keys.add(key)
                val arguments = method.parameterTypes.mapIndexed { index, type ->
                    when (type) {
                        Boolean::class.javaPrimitiveType -> true
                        Int::class.javaPrimitiveType -> index + 1
                        else -> "marker-${index + 1}-${'$'}-\\-%s"
                    }
                }.toTypedArray<Any>()
                assertEquals(expected.getProperty(key), method.invoke(catalog, *arguments), key)
            }
        }
        assertEquals(keys, expected.stringPropertyNames(), "Every catalog member needs a rendering fixture")
    }
}
