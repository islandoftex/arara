// SPDX-License-Identifier: BSD-3-Clause
package org.islandoftex.arara.api.files

import org.islandoftex.arara.api.utils.OS
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class MPPPathTest {
    // attributes
    @Test
    fun shouldNotClassifyRelativeAsAbsolutePaths() {
        assertFalse(MPPPath(".").isAbsolute)
    }
    @Test
    fun shouldCorrectlyDetectAbsolutePaths() {
        val path = getValueByOS(windows = "C:/test", unix = "/test")

        assertTrue(MPPPath(path).isAbsolute)
    }
    @Test
    fun shouldConsiderRootAbsolute() {
        val path = getValueByOS(windows = "C:/", unix = "/")

        // korio does not handle this
        assertTrue(MPPPath(path).isAbsolute)
    }

    // path normalization
    @Test
    fun shouldNormalizeDots() {
        val (expected, actual) = getValueByOS(
                windows = "C:/tmp/./quack/.." to "C:/tmp/",
                unix = "/tmp/./quack/.." to "/tmp/"
        )

        assertEquals(
            MPPPath(expected).normalize().toString(),
            MPPPath(actual).normalize().toString()
        )
    }
    @Test
    fun shouldNoExceedRoot() {
        val (expected, actual) = getValueByOS(
                windows = "C:/tmp/../../.." to "C:/",
                unix = "/tmp/../../.." to "/"
        )

        assertEquals(
            MPPPath(expected).normalize().toString(),
            MPPPath(actual).toString()
        )
    }

    // parenting
    @Test
    fun shouldUseRootAsParentOfRoot() {
        val (expected, actual) = getValueByOS(
                windows = "C:/" to "C:/",
                unix = "/" to "/"
        )

        assertEquals(
            MPPPath(expected).parent.toString(),
            MPPPath(actual).toString()
        )
    }
    @Test
    fun shouldDetermineParentCorrectly() {
        val (expected, actual) = getValueByOS(
                windows = "C:/tmp/./quack/.." to "C:/",
                unix = "/tmp/./quack/.." to "/"
        )

        assertEquals(
            MPPPath(expected).parent.normalize().toString(),
            MPPPath(actual).toString()
        )
        assertEquals(
            MPPPath(expected).normalize().parent.toString(),
            MPPPath(actual).toString()
        )
    }

    private inline fun <reified T> getValueByOS(windows: T, unix: T): T =
            if (OS.isWindows) windows else unix
}
