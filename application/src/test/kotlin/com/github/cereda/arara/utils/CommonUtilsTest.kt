package com.github.cereda.arara.utils

import com.github.cereda.arara.configuration.ConfigurationController
import com.github.cereda.arara.localization.Language
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import java.io.File

class CommonUtilsTest {
    @Test
    fun checkByteFormatting() {
        ConfigurationController.put("execution.language", Language("en"))
        mapOf(800 to "800 B",
                1000 to "1.0 kB",
                1024 to "1.0 kB",
                1000000 to "1.0 MB").forEach { (key, value) ->
            assertEquals(value, CommonUtils.byteSizeToString(key.toLong()))
        }
    }

    @Test
    fun checkCRCgeneration() {
        assertEquals("17f430a5", CommonUtils.calculateHash(File("../LICENSE")))
        assertEquals("536c426f", CommonUtils.calculateHash(File("../CODE_OF_CONDUCT.md")))
    }
}