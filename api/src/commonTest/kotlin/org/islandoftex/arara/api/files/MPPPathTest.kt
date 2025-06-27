// SPDX-License-Identifier: BSD-3-Clause
package org.islandoftex.arara.api.files

// The following object import is specific to the JVM platform; while common
// is meant for shared code, it can still reference certain platform-specific
// features if the project is set up to allow it.
import org.islandoftex.arara.api.utils.OS

import kotlin.io.path.Path
import kotlin.io.path.absolutePathString
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

        // for this test, we cannot mock the drive letter in Windows, as
        // an attempt to go parent from root will make it resolve to the
        // actual letter (this was needed so our Windows runner would not
        // fail in case the tests were not running on C:)

        val (expected, actual) = getValueByOS(
                windows = "${drive}:/" to "${drive}:/",
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

    private val drive: String by lazy {
        if (OS.isWindows) Path(".").absolutePathString()
                .substringBefore(":") else "C"
    }
}
