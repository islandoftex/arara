package com.github.cereda.arara.utils

import org.junit.Assert.assertEquals
import org.junit.Test
import java.io.File

class CommonUtilsTest {
    @Test
    fun checkByteFormatting() {
        mapOf(800 to "800 B",
                1000 to "1,0 kB",
                1024 to "1,0 kB",
                1000000 to "1,0 MB").forEach { (key, value) ->
            assertEquals(value, CommonUtils.byteSizeToString(key.toLong()))
        }
    }

    @Test
    fun checkCRCgeneration() {
        assertEquals("17f430a5", CommonUtils.calculateHash(File("../LICENSE")))
        assertEquals("536c426f", CommonUtils.calculateHash(File("../CODE_OF_CONDUCT.md")))
    }
}