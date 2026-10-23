// SPDX-License-Identifier: BSD-3-Clause
package org.islandoftex.arara.cli.utils

import io.kotest.core.spec.style.ShouldSpec
import io.kotest.matchers.shouldBe
import java.nio.file.FileSystems
import java.nio.file.Files
import java.util.Properties
import org.islandoftex.arara.api.files.MPPPath
import org.islandoftex.arara.api.files.toJVMPath
import org.islandoftex.arara.api.files.toMPPPath
import org.islandoftex.arara.cli.configuration.ConfigurationFiles

class PropertiesTest : ShouldSpec({
    context("UTF-8 support for properties files") {
        val path = MPPPath("src/jvmTest/resources/executiontests/properties/duck.properties")
        val charset = Charsets.UTF_8
        val translations: Properties = Properties().apply { ConfigurationFiles.loadProperties(this, path, charset) }

        should("not be empty") {
            translations.isNotEmpty() shouldBe true
        }

        val expected = mapOf(
                "english" to "duck",
                "spanish" to "patito",
                "french" to "canard",
                "german" to "ente",
                "portuguese" to "pato",
                "italian" to "anatra",
                "russian" to "\u0443\u0442\u043A\u0430",
                "chinese" to "\u9E2D\u5B50",
                "japanese" to "\u30A2\u30D2\u30EB",
                "korean" to "\uC624\uB9AC",
                "arabic" to "\u0628\u0637\u0647",
                "hindi" to "\u092C\u0924\u094D\u0924\u0916",
                "turkish" to "\u00F6\u0072\u0064\u0065\u006B",
                "swedish" to "anka",
                "dutch" to "eend",
                "norwegian" to "and"
        )

        expected.forEach { (language, translation) ->
            should("get the correct string for $language") {
                translations[language] shouldBe translation
            }
        }
    }

    should("read properties through their filesystem and requested charset") {
        val root = Files.createTempDirectory("arara properties backend ").toMPPPath()
        try {
            FileSystems.newFileSystem(root.resolve("properties.zip").toJVMPath(), mapOf("create" to "true"))
                .use { filesystem ->
                    val path = filesystem.getPath("/translations.properties").toMPPPath()
                    // MPPPath writes UTF-8 text; this fixture needs a different encoding.
                    Files.write(path.toJVMPath(), "word=canard é\n".toByteArray(Charsets.UTF_16LE))
                    val properties = Properties()
                    ConfigurationFiles.loadProperties(properties, path, Charsets.UTF_16LE)
                    properties.getProperty("word") shouldBe "canard é"
                }
        } finally {
            root.removeDirectory()
        }
    }

    should("reject malformed encoded properties without replacing their bytes") {
        val root = Files.createTempDirectory("arara properties encoding ").toMPPPath()
        try {
            val path = root.resolve("invalid.properties")
            Files.write(path.toJVMPath(), byteArrayOf(0xc3.toByte(), 0x28))
            val properties = Properties()
            ConfigurationFiles.loadProperties(properties, path)
            properties.isEmpty() shouldBe true
        } finally {
            root.removeDirectory()
        }
    }
})
