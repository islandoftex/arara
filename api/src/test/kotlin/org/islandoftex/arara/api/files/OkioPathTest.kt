// SPDX-License-Identifier: BSD-3-Clause
package org.islandoftex.arara.api.files

import java.io.IOException
import java.nio.file.Paths
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue
import okio.FileMetadata
import okio.ForwardingFileSystem
import okio.ForwardingSource
import okio.Path
import okio.Source
import okio.fakefilesystem.FakeFileSystem
import org.islandoftex.arara.api.AraraIOException

class OkioPathTest {
    @Test
    fun backendsAreIsolatedAndPropagatedThroughEveryDerivedPath() {
        FakeFileSystem().use { first ->
            FakeFileSystem().use { second ->
                val root = MPPPath(Paths.get("virtual-root").toAbsolutePath(), first)
                root.createDirectories()
                val file = root.resolve("text.txt")
                file.writeText("first")
                val other = MPPPath(Paths.get(root.toString()), second)
                assertFalse(other.exists)
                other.createDirectories()
                other.resolve("text.txt").writeText("second")
                val derivedPaths = listOf(
                    MPPPath(file),
                    file.normalize(),
                    file.parent.resolve("text.txt"),
                    file.resolveSibling("text.txt"),
                    root / "text.txt",
                    root.resolve(MPPPath("text.txt"))
                )
                for (derived in derivedPaths) {
                    assertEquals("first", derived.readText())
                }
                assertEquals("second", other.resolve("text.txt").readText())
                assertFalse(MPPPath(root.toString()).exists)
                assertEquals(listOf(file), root.listFiles())
            }
        }
    }

    @Test
    fun longLinkChainsResolveAndCyclesRemainDistinctFromMissingTargets() {
        FakeFileSystem().use { fs ->
            fs.allowSymlinks = true
            val root = MPPPath(Paths.get("virtual-links").toAbsolutePath(), fs)
            root.createDirectories()
            val target = root.resolve("target").apply { writeText("contents") }
            var link = target
            repeat(64) { index ->
                val next = root.resolve("link-$index")
                fs.createSymlink(next.toOkioPath(), link.toOkioPath())
                link = next
            }
            assertEquals(target.fileSize, link.fileSize)
            assertEquals("contents", link.readText())
            val cycle = root.resolve("cycle")
            fs.createSymlink(cycle.toOkioPath(), cycle.toOkioPath())
            assertFalse(cycle.exists)
            assertFailsWith<AraraIOException> { cycle.metadata() }
            val broken = root.resolve("broken")
            fs.createSymlink(broken.toOkioPath(), root.resolve("missing").toOkioPath())
            assertFalse(broken.exists)
            assertFailsWith<AraraIOException> { broken.metadata() }
            assertFailsWith<AraraIOException> { broken.createDirectories() }
        }
    }

    @Test
    fun unknownMetadataDoesNotBecomeZeroAndPredicateFailuresRemainBestEffort() {
        FakeFileSystem().use { fs ->
            val backend = object : ForwardingFileSystem(fs) {
                override fun metadataOrNull(path: Path): FileMetadata? =
                    if (path.name == "denied") throw IOException("denied: $path")
                    else super.metadataOrNull(path)?.copy(size = null, lastModifiedAtMillis = null)
            }
            val root = MPPPath(Paths.get("virtual-metadata").toAbsolutePath(), backend)
            root.createDirectories()
            val file = root.resolve("file").apply { writeText("") }
            assertTrue(file.exists)
            assertNull(file.metadata().size)
            assertNull(file.metadata().lastModified)
            assertFailsWith<AraraIOException> { file.fileSize }
            assertFailsWith<AraraIOException> { file.lastModified }
            val denied = root.resolve("denied")
            assertFalse(denied.exists)
            assertFalse(denied.isRegularFile)
            assertFalse(denied.isDirectory)
            val error = assertFailsWith<AraraIOException> { denied.metadata() }
            assertTrue(error.exception is IOException)
        }
    }

    @Test
    fun traversalFollowsAliasesSkipsBrokenLinksAndDeletionLeavesTargetsAlone() {
        FakeFileSystem().use { fs ->
            fs.allowSymlinks = true
            val root = MPPPath(Paths.get("virtual-tree").toAbsolutePath(), fs)
            root.createDirectories()
            val target = root.resolve("target").apply { createDirectories() }
            val sentinel = target.resolve("keep").apply { writeText("preserve") }
            val tree = root.resolve("tree").apply { createDirectories() }
            val alias = tree.resolve("alias")
            fs.createSymlink(alias.toOkioPath(), target.toOkioPath())
            fs.createSymlink(tree.resolve("broken").toOkioPath(), root.resolve("absent").toOkioPath())
            assertEquals(listOf(alias.resolve("keep")), tree.listFiles(true))
            assertEquals(emptyList(), tree.listFiles(false))
            tree.removeDirectory()
            assertEquals("preserve", sentinel.readText())
            fs.createSymlink(target.resolve("cycle").toOkioPath(), target.toOkioPath())
            assertFailsWith<AraraIOException> { target.listFiles(true) }
        }
    }

    @Test
    fun nonRegularFilesAreRejectedBeforeOpeningEitherReader() {
        FakeFileSystem().use { fs ->
            var opened = false
            val backend = object : ForwardingFileSystem(fs) {
                override fun metadataOrNull(path: Path): FileMetadata? =
                    super.metadataOrNull(path)?.copy(isRegularFile = false)

                override fun source(file: Path): Source {
                    opened = true
                    return super.source(file)
                }
            }
            val root = MPPPath(Paths.get("virtual-special").toAbsolutePath(), backend)
            root.createDirectories()
            val special = root.resolve("special").apply { writeText("content") }
            assertFailsWith<AraraIOException> { special.readLines() }
            assertFailsWith<AraraIOException> { special.readText() }
            assertFalse(opened)
        }
    }

    @Test
    fun sourcesCloseOnSuccessAndFailureAndBackendErrorsKeepTheirCause() {
        FakeFileSystem().use { fs ->
            var closed = false
            val backend = object : ForwardingFileSystem(fs) {
                override fun source(file: Path): Source = object : ForwardingSource(super.source(file)) {
                    override fun close() {
                        closed = true
                        super.close()
                    }
                }
            }
            val root = MPPPath(Paths.get("virtual-source").toAbsolutePath(), backend)
            root.createDirectories()
            val file = root.resolve("file").apply { writeText("content") }
            assertEquals("content", file.read { it.readUtf8() })
            assertTrue(closed)
            closed = false
            val cause = IOException("read callback failed")
            val error = assertFailsWith<AraraIOException> { file.read<Unit> { throw cause } }
            assertEquals(cause, error.exception)
            assertTrue(closed)
            assertEquals(listOf(file), root.listEntries())
        }
    }

    @Test
    fun readLinesKeepNativeTerminatorsAndStrictUtf8Decoding() {
        FakeFileSystem().use { fs ->
            val root = MPPPath(Paths.get("virtual-read").toAbsolutePath(), fs)
            root.createDirectories()
            val file = root.resolve("text")
            val examples = listOf(
                "" to emptyList(),
                "\n" to listOf(""),
                "a\rb\r\nc\n" to listOf("a", "b", "c")
            )
            for ((text, lines) in examples) {
                file.writeText(text)
                assertEquals(lines, file.readLines())
            }
            fs.write(file.toOkioPath()) { writeByte(0xff) }
            assertFailsWith<AraraIOException> { file.readLines() }
        }
    }
}
