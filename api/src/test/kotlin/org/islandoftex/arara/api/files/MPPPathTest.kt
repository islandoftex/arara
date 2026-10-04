// SPDX-License-Identifier: BSD-3-Clause
package org.islandoftex.arara.api.files

import kotlin.io.path.Path
import kotlin.io.path.absolutePathString
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertTrue
import org.islandoftex.arara.api.utils.OS
import org.junit.jupiter.api.io.TempDir

/**
 * Verifies the path attributes and normalization of [MPPPath] across
 * operating systems: absolute path detection, dot segment resolution
 * and parent lookup, including the root edge cases. Platform-specific
 * expectations are selected by the [getValueByOS] helper.
 */
class MPPPathTest {
    // attributes
    @Test
    fun `does not classify relative paths as absolute`() {
        assertFalse(
            MPPPath(".").isAbsolute,
            "a relative path is not reported as absolute"
        )
    }

    @Test
    fun `correctly detects absolute paths`() {
        val path = getValueByOS(windows = "C:/test", unix = "/test")

        assertTrue(
            MPPPath(path).isAbsolute,
            "an absolute path is reported as absolute"
        )
    }
    @Test
    fun `considers the root absolute`() {
        val path = getValueByOS(windows = "C:/", unix = "/")

        assertTrue(
            MPPPath(path).isAbsolute,
            "the root is reported as absolute"
        )
    }

    // path normalization
    @Test
    fun `normalizes dots`() {
        val (expected, actual) = getValueByOS(
                windows = "C:/tmp/./quack/.." to "C:/tmp/",
                unix = "/tmp/./quack/.." to "/tmp/"
        )

        assertEquals(
            MPPPath(actual).normalize().toString(),
            MPPPath(expected).normalize().toString(),
            "dot segments are resolved during normalization"
        )
    }
    @Test
    fun `does not exceed the root when normalizing`() {
        val (expected, actual) = getValueByOS(
                windows = "C:/tmp/../../.." to "C:/",
                unix = "/tmp/../../.." to "/"
        )

        assertEquals(
            MPPPath(actual).toString(),
            MPPPath(expected).toString(),
            "normalization stops at the root"
        )
    }

    // parenting
    @Test
    fun `uses the root as the parent of the root`() {

        // for this test, we cannot mock the drive letter in Windows, as
        // an attempt to go parent from root will make it resolve to the
        // actual letter (this was needed so our Windows runner would not
        // fail in case the tests were not running on C:)

        val (expected, actual) = getValueByOS(
                windows = "${drive}:/" to "${drive}:/",
                unix = "/" to "/"
        )

        assertEquals(
            MPPPath(actual).toString(),
            MPPPath(expected).parent.toString(),
            "the root is its own parent"
        )
    }
    @Test
    fun `determines the parent correctly`() {
        val (expected, actual) = getValueByOS(
                windows = "C:/tmp/./quack/.." to "C:/",
                unix = "/tmp/./quack/.." to "/"
        )

        assertEquals(
            MPPPath(actual).toString(),
            MPPPath(expected).parent.normalize().toString(),
            "the parent of a path with dot segments resolves"
        )
        assertEquals(
            MPPPath(actual).toString(),
            MPPPath(expected).normalize().parent.toString(),
            "the parent of a normalized path resolves equally"
        )
    }

    @Test
    fun `reports the size and the last modification of a file`(
        @TempDir tempDir: java.nio.file.Path
    ) {
        val file = tempDir.resolve("quack.log")
        java.nio.file.Files.writeString(file, "Moo! Quack!")
        val path = MPPPath(file.toString())
        assertTrue(
            path.fileSize > 0L,
            "a written file reports a positive size"
        )
        // the stamp the platform reports is the stamp the path
        // carries, at the whole-second precision the model keeps
        assertEquals(
            java.nio.file.Files.getLastModifiedTime(file).toMillis() / 1000,
            path.lastModified.toLong() / 1000,
            "a written file reports its modification stamp"
        )
    }

    @Test
    fun `raises for the metadata of a missing file`() {
        // the getters read the live metadata, so a missing path raises
        // the path exception rather than inventing a stamp
        val path = MPPPath("arara-file-not-there-quack.log")
        assertFailsWith<org.islandoftex.arara.api.AraraIOException>(
            "the size of a missing file raises"
        ) {
            path.fileSize
        }
        assertFailsWith<org.islandoftex.arara.api.AraraIOException>(
            "the stamp of a missing file raises"
        ) {
            path.lastModified
        }
    }

    /**
     * Selects the operating system specific expectation for a test.
     */
    private inline fun <reified T> getValueByOS(windows: T, unix: T): T =
            if (OS.isWindows) windows else unix

    private val drive: String by lazy {
        if (OS.isWindows) Path(".").absolutePathString()
                .substringBefore(":") else "C"
    }
}
