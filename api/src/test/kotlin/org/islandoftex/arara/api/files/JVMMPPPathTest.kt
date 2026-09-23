// SPDX-License-Identifier: BSD-3-Clause
package org.islandoftex.arara.api.files

import java.io.File
import java.nio.file.Path
import kotlin.io.path.readText
import kotlin.io.path.writeText
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertTrue
import org.islandoftex.arara.api.AraraIOException
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir

/**
 * Verifies the JVM path model of [MPPPath]: reading and writing text,
 * the value semantics of the two paths every model carries (the
 * user-provided spelling and the absolutized reference), the lexical
 * prefix check, the resolution chain and the JVM escape hatches.
 */
class JVMMPPPathTest {
    @Nested
    inner class `read and write operations` {
        @Test
        fun `reads from a file`(@TempDir tempDir: Path) {
            val tmp = tempDir.resolve("quack.txt")
            tmp.writeText("quack")
            assertEquals(
                "quack",
                MPPPath(tmp.toString()).readText(),
                "text read back from the file"
            )
            assertEquals(
                listOf("quack"),
                MPPPath(tmp.toString()).readLines(),
                "lines read back from the file"
            )
        }

        @Test
        fun `writes to a file`(@TempDir tempDir: Path) {
            val tmp = tempDir.resolve("quack.txt")
            MPPPath(tmp.toString()).writeText("quack")
            assertEquals(
                "quack",
                tmp.readText(),
                "text written to the file"
            )
        }

        @Test
        fun `fails reading from a directory`(@TempDir tempDir: Path) {
            val exception = assertFailsWith<AraraIOException>(
                "reading text from a directory must be rejected"
            ) {
                tempDir.toMPPPath().readText()
            }
            assertTrue(
                exception.message?.contains("read text from file") == true,
                "the read error names the offending operation"
            )
        }

        @Test
        fun `fails writing to a directory`(@TempDir tempDir: Path) {
            val exception = assertFailsWith<AraraIOException>(
                "writing text to a directory must be rejected"
            ) {
                tempDir.toMPPPath().writeText("quack")
            }
            assertTrue(
                exception.message?.contains("write text to file") == true,
                "the write error names the offending operation"
            )
        }

        @Test
        fun `fails appending to a missing file`(@TempDir tempDir: Path) {
            val missing = tempDir.resolve("missing-append.txt")
            val exception = assertFailsWith<AraraIOException>(
                "appending to a missing file must be rejected"
            ) {
                MPPPath(missing.toString()).writeText("quack", append = true)
            }
            assertTrue(
                exception.message?.contains("append text to existing") == true,
                "the append error names the offending operation"
            )
        }
    }

    @Nested
    inner class `value semantics` {
        @Test
        fun `treats different spellings of the same location as equal`() {
            val relative = MPPPath("equality-probe.txt")
            val absolute = MPPPath(File("equality-probe.txt").absolutePath)

            assertEquals(
                absolute, relative,
                "paths denoting the same location are equal"
            )
            assertEquals(
                absolute.hashCode(), relative.hashCode(),
                "hash codes of equal locations agree"
            )
        }

        @Test
        fun `deduplicates equal paths in sets`() {
            val relative = MPPPath("equality-probe.txt")
            val absolute = MPPPath(File("equality-probe.txt").absolutePath)

            assertEquals(
                1, setOf(relative, absolute).size,
                "a set of equal paths holds a single element"
            )
        }

        @Test
        fun `preserves equality across normalization`() {
            assertEquals(
                MPPPath("equality-probe.txt").normalize(),
                MPPPath("equality-probe.txt"),
                "normalization keeps equality intact"
            )
        }

        @Test
        fun `answers isAbsolute from the provided path`() {
            val path = MPPPath("equality-probe.txt")

            // the duality of MPPPath: isAbsolute reflects what the caller
            // wrote, while toString operates on the absolutized reference
            assertFalse(
                path.isAbsolute,
                "isAbsolute reports the user-provided spelling"
            )
            assertEquals(
                File("equality-probe.txt").toPath()
                    .toAbsolutePath().normalize().toString(),
                path.toString(),
                "toString reports the absolutized reference path"
            )
        }

        @Test
        fun `answers startsWith lexically and honors normalization`() {
            val directory = MPPPath("lexical-probe-dir")
            val inner = directory.resolve("inner.txt")

            assertTrue(
                inner.startsWith(directory),
                "relative spellings match lexically"
            )
            assertTrue(
                inner.normalize().startsWith(directory.normalize()),
                "normalized spellings match location-based"
            )
            assertFalse(
                inner.normalize().startsWith(directory),
                "a normalized spelling does not match a raw one"
            )
        }
    }

    @Nested
    inner class `resolution and siblings` {
        @Test
        fun `resolves children and canonicalizes the result`() {
            val parent = MPPPath("resolution-probe-dir")

            assertEquals(
                File("resolution-probe-dir")
                    .toPath().resolve("child.txt")
                    .toAbsolutePath().normalize().toString(),
                parent.resolve("child.txt").toString(),
                "resolving a relative child recanonicalizes against " +
                    "the working directory"
            )
        }

        @Test
        fun `treats the div operator as resolution`() {
            val parent = MPPPath("resolution-probe-dir")

            assertEquals(
                parent.resolve("child.txt"),
                parent / "child.txt",
                "the div operator resolves like the resolve method"
            )
        }

        @Test
        fun `resolves siblings against the reference parent`(@TempDir tempDir: Path) {
            val file = MPPPath(tempDir.resolve("target.txt").toString())

            assertEquals(
                MPPPath(tempDir.toString()),
                file.resolveSibling("other.txt").parent,
                "a sibling lives beside the original file"
            )
        }

        @Test
        fun `reports the parent of a relative path against the working directory`() {
            assertEquals(
                MPPPath("."),
                MPPPath("parent-probe.txt").parent,
                "the parent of a relative path is the working directory"
            )
        }
    }

    @Nested
    inner class `escape hatches` {
        @Test
        fun `exposes the provided spelling to the JVM bridges`() {
            assertEquals(
                "bridge-probe.txt",
                MPPPath("bridge-probe.txt").toJVMPath().toString(),
                "the JVM path bridge reports the raw spelling"
            )
            assertEquals(
                "bridge-probe.txt",
                MPPPath("bridge-probe.txt").toJVMFile().toString(),
                "the JVM file bridge reports the raw spelling"
            )
        }
    }
}
