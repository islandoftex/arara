// SPDX-License-Identifier: BSD-3-Clause
package org.islandoftex.arara.api.files

import java.io.File
import java.io.IOException
import java.nio.file.Files
import java.nio.file.Path
import kotlin.io.path.readText
import kotlin.io.path.writeText
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertNotEquals
import kotlin.test.assertTrue
import okio.fakefilesystem.FakeFileSystem
import org.islandoftex.arara.api.AraraIOException
import org.islandoftex.arara.api.time.DateTime
import org.islandoftex.arara.api.utils.OS
import org.junit.jupiter.api.Assumptions.assumeTrue
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir

/**
 * Verifies the JVM path model of [MPPPath]: reading and writing text,
 * the value semantics of the two paths every model carries (the
 * user-provided spelling and the absolutized reference), the lexical
 * prefix check, the resolution chain, the JVM escape hatches, the
 * symlink chains the metadata queries follow, the degenerate
 * spellings that collapse at construction, the Windows-shaped drive
 * and UNC spellings, and the in-memory file system the model's I/O
 * can ride.
 */
class JVMMPPPathTest {
    /**
     * The text-level I/O of the path model.
     */
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
        fun `failed reads surface as the path exception with the cause kept`(
            @TempDir tempDir: Path
        ) {
            val missing = MPPPath(tempDir.resolve("not-there.txt").toString())
            assertFailsWith<AraraIOException> { missing.readText() }
            val wrapped = assertFailsWith<AraraIOException> { missing.readLines() }
            assertTrue(
                wrapped.hasException(),
                "the wrapping keeps the underlying system failure as the cause"
            )
            assertFailsWith<AraraIOException> { MPPPath(tempDir.toString()).readLines() }
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

    /**
     * The equality and hashing semantics of the model.
     */
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

    /**
     * The resolution of children and the prefix relations.
     */
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

    /**
     * The JVM bridges that expose the underlying spellings.
     */
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

    /**
     * The traversal of symbolic links the model performs on its
     * metadata queries.
     */
    @Nested
    inner class `symlink resolution` {
        /**
         * Creates a symbolic link, answering whether the platform
         * allowed it: creating symlinks requires elevated privileges
         * on some systems, so the capability is probed first and
         * the tests are skipped wherever creation is denied.
         *
         * @param link The link to create.
         * @param target The link's target.
         * @return Whether the link was created.
         */
        private fun createSymlink(link: Path, target: Path): Boolean =
            try {
                Files.createSymbolicLink(link, target)
                true
            } catch (_: IOException) {
                false
            } catch (_: UnsupportedOperationException) {
                false
            }

        @Test
        fun `follows symlinks for existence and type queries`(@TempDir tempDir: Path) {
            val target = tempDir.resolve("target.txt")
            target.writeText("quack")
            val link = tempDir.resolve("link.txt")
            assumeTrue(
                createSymlink(link, target),
                "the host cannot create symlinks"
            )

            val path = MPPPath(link.toString())
            assertTrue(path.exists, "a link to an existing file exists")
            assertTrue(path.isRegularFile, "a link to a file is a regular file")
        }

        @Test
        fun `resolves symlink chains for size and time queries`(@TempDir tempDir: Path) {
            val target = tempDir.resolve("target.txt")
            target.writeText("quack")
            val intermediate = tempDir.resolve("intermediate.txt")
            val link = tempDir.resolve("link.txt")
            assumeTrue(
                createSymlink(intermediate, target),
                "the host cannot create symlinks"
            )
            assumeTrue(
                createSymlink(link, intermediate),
                "the host cannot create symlinks"
            )

            val path = MPPPath(link.toString())
            assertEquals(
                5L,
                path.fileSize,
                "the size comes from the link target"
            )
            assertEquals(
                DateTime(Files.getLastModifiedTime(target).toMillis()).toString(),
                path.lastModified.toString(),
                "the modification time comes from the link target"
            )
        }

        @Test
        fun `reports a broken symlink as missing`(@TempDir tempDir: Path) {
            val link = tempDir.resolve("broken.txt")
            assumeTrue(
                createSymlink(link, tempDir.resolve("missing.txt")),
                "the host cannot create symlinks"
            )

            assertFalse(
                MPPPath(link.toString()).exists,
                "a broken link does not exist"
            )
        }
    }

    /**
     * The spellings that collapse at construction, and the ones
     * that stay as written.
     */
    @Nested
    inner class `degenerate spellings` {
        @Test
        fun `collapses redundant spellings at construction`() {
            // spellings that are neither drive-prefixed nor UNC take
            // the host's directory separator at construction, matching
            // the canonicalization the previous platform backend
            // performed; drive-prefixed and UNC spellings keep their
            // backslashes on every host, as the Windows-shaped group
            // pins
            assertEquals(
                "a" + File.separator + "b",
                MPPPath("a//b").toJVMPath().toString(),
                "redundant separators collapse in the provided spelling"
            )
            assertEquals(
                "a" + File.separator + "b",
                MPPPath("a/./b").toJVMPath().toString(),
                "interior dot segments collapse in the provided spelling"
            )
        }

        @Test
        fun `consumes leading dotdot on absolute spellings`() {
            assertEquals(
                File.separator + "x",
                MPPPath("/../x").toJVMPath().toString(),
                "leading dotdot on an absolute spelling is consumed eagerly"
            )
        }

        @Test
        fun `maps the empty spelling to the working directory`() {
            assertEquals(
                MPPPath(".").toString(),
                MPPPath("").toString(),
                "the empty spelling denotes the working directory"
            )
            assertEquals(
                ".",
                MPPPath("").toJVMPath().toString(),
                "the empty spelling is re-spelled as the working directory"
            )
        }
    }

    /**
     * The starts-with relation, compared segment by segment.
     */
    @Nested
    inner class `lexical prefix semantics` {
        @Test
        fun `compares segments instead of prefixes`() {
            assertFalse(
                MPPPath("/foobar").startsWith(MPPPath("/foo")),
                "a segment is not a prefix of a longer segment"
            )
            assertTrue(
                MPPPath("a/b").startsWith(MPPPath("a")),
                "a matching segment prefix matches"
            )
        }

        @Test
        fun `requires matching roots in the prefix check`() {
            assertFalse(
                MPPPath("/a/b").startsWith(MPPPath("a")),
                "an absolute spelling does not start with a relative one"
            )
        }
    }

    /**
     * The drive-prefixed and UNC spellings and their re-spelling
     * on Windows.
     */
    @Nested
    inner class `Windows-shaped spellings` {
        @Test
        fun `parses backslashed drive spellings as absolute`() {
            assertTrue(
                MPPPath("C:\\probe.txt").isAbsolute,
                "a backslashed drive spelling is absolute on every host"
            )
        }

        @Test
        fun `parses UNC spellings as absolute`() {
            assertTrue(
                MPPPath("\\\\probe-server\\probe.txt").isAbsolute,
                "a UNC spelling is absolute on every host"
            )
        }

        @Test
        fun `treats case variants as distinct spellings`() {
            assertNotEquals(
                MPPPath("C:\\Probe.txt"),
                MPPPath("c:\\probe.txt"),
                "equality is case-sensitive under the new algebra"
            )
        }

        @Test
        fun `applies the first-separator rule to mixed spellings`() {
            // the algebra recognizes a Windows path by its first
            // backslash, so a forward-slashed drive spelling follows
            // the host rules: re-spelled to backslashes on Windows,
            // a relative path everywhere else
            assertEquals(
                OS.isWindows,
                MPPPath("C:/probe.txt").isAbsolute,
                "a forward-slashed drive spelling follows the host rules"
            )
        }
    }

    /**
     * The model's I/O through the injectable in-memory file system,
     * keeping the host platform untouched.
     */
    @Nested
    inner class `in-memory file system` {
        /**
         * Swaps the global file system for the in-memory implementation
         * around the action, keeping the host platform untouched while
         * the model operations run, and the previous instance is always
         * restored.
         *
         * @param action The block to run against the fake.
         * @return Whatever the block returns.
         */
        private fun <T> withFakeFileSystem(action: () -> T): T {
            val original = PathModelFileSystem.current
            PathModelFileSystem.current = FakeFileSystem()
            try {
                return action()
            } finally {
                PathModelFileSystem.current = original
            }
        }

        @Test
        fun `serves the path model operations from an injected fake`() {
            withFakeFileSystem {
                val file = MPPPath("probe-dir/probe.txt")
                file.parent.createDirectories()
                assertFalse(file.exists, "the file is not in the fake yet")
                file.writeText("quack")
                assertTrue(file.exists, "the write landed in the fake")
                assertEquals(
                    "quack",
                    file.readText(),
                    "the read comes from the fake"
                )
                file.parent.removeDirectory()
                assertFalse(file.exists, "the removal hit the fake")
            }
        }

        @Test
        fun `serves Windows-shaped spellings from an injected fake`() {
            withFakeFileSystem {
                val file = MPPPath("C:\\probe\\probe.txt")
                assertTrue(
                    file.isAbsolute,
                    "a backslashed drive spelling is absolute"
                )
                file.parent.createDirectories()
                file.writeText("quack")
                assertTrue(file.exists, "the write landed in the fake")
                assertEquals(
                    "quack",
                    file.readText(),
                    "the read comes from the fake"
                )
            }
        }
    }
}
