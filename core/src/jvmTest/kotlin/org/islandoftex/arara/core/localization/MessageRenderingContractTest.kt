// SPDX-License-Identifier: BSD-3-Clause
package org.islandoftex.arara.core.localization

import java.util.Properties
import kotlin.test.Test
import kotlin.test.assertEquals

class MessageRenderingContractTest {
    @Test
    fun `every translation retains its rendered text and literal arguments`() {
        val catalogs = mapOf(
            "Messages" to EnglishMessages,
            "GermanLanguage" to GermanMessages,
            "ItalianLanguage" to ItalianMessages,
            "DutchLanguage" to DutchMessages,
            "BrazilianLanguage" to BrazilianPortugueseMessages,
            "NorfolkLanguage" to NorfolkMessages
        )
        val rendered = Properties()
        requireNotNull(javaClass.getResourceAsStream("/localization/rendered-messages.properties"))
            .bufferedReader(Charsets.UTF_8).use { rendered.load(it) }
        for (key in rendered.stringPropertyNames()) {
            val locale = key.substringBefore('.')
            val member = key.substringAfter('.')
            val catalog = catalogs.getValue(locale)
            val words = member.lowercase().split('_')
            var name = words.first() + words.drop(1).joinToString("") { it.replaceFirstChar(Char::uppercase) }
            name = name.replace("Calculatehash", "CalculateHash")
            name = name.replace("Checkboolean", "CheckBoolean")
            name = name.replace("Checkregex", "CheckRegex")
            name = name.replace("Discoverfile", "DiscoverFile")
            name = name.replace("Getapplicationpath", "GetApplicationPath")
            name = name.replace("Parserule", "ParseRule")
            name = name.replace("Replicatelist", "ReplicateList")
            name = name.replace("Validatebody", "ValidateBody")
            name = name.replace("Validateheader", "ValidateHeader")
            name = name.replace("Dryrun", "DryRun")
            val method = AraraMessages::class.java.methods.single {
                it.name == name || it.name == "get${name.replaceFirstChar(Char::uppercase)}"
            }
            val arguments = method.parameterTypes.mapIndexed { index, type ->
                when (type) {
                    Boolean::class.javaPrimitiveType -> true
                    Int::class.javaPrimitiveType -> index + 1
                    else -> "marker-${index + 1}-${'$'}-\\-%s"
                }
            }.toTypedArray<Any>()
            var expected = rendered.getProperty(key)
            arguments.forEachIndexed { index, argument ->
                if (argument !is String) {
                    expected = expected.replace("marker-${index + 1}-${'$'}-\\-%s", argument.toString())
                }
            }
            assertEquals(expected, method.invoke(catalog, *arguments), "$locale: $member")
        }
        assertEquals(catalogs.size * AraraMessages::class.java.methods.count {
            it.returnType == String::class.java
        }, rendered.size)
    }
}
