// SPDX-License-Identifier: BSD-3-Clause
package org.islandoftex.arara.api.files

import java.io.IOException
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue
import org.junit.jupiter.api.Assumptions.assumeTrue

class PathContractTest {
    @Test
    fun suppliedSpellingRetainsExactInputWithoutChangingNormalizedExports() {
        for (spelling in listOf("", ".", "./folder//../chapter ü.tex")) {
            val path = MPPPath(spelling)
            val normalized = Paths.get(spelling).toAbsolutePath().normalize()
            assertEquals(spelling, path.suppliedSpelling)
            assertEquals(normalized.toString(), path.toString())
            assertEquals(normalized, path.toJVMPath())
            assertEquals(normalized.toFile(), path.toJVMFile())
            assertEquals(normalized.toString(), path.toOkioPath().toString())
            assertEquals(path, path.normalize())
            assertEquals(path.hashCode(), path.normalize().hashCode())
        }
    }

    @Test
    fun copiesRetainSuppliedSpellingAndDerivedPathsDescribeTheirOwnSpelling() {
        val spelling = "./parent//nested/../chapter.tex"
        val native = Paths.get(spelling)
        val path = MPPPath(spelling)
        assertEquals(spelling, MPPPath(path).suppliedSpelling)
        assertEquals(native.toString(), native.toMPPPath().suppliedSpelling)
        assertEquals(native.toFile().toPath().toString(), native.toFile().toMPPPath().suppliedSpelling)
        assertEquals(path.toString(), path.normalize().suppliedSpelling)
        assertEquals(native.resolve("child.tex").toString(), path.resolve("child.tex").suppliedSpelling)
        assertEquals(path.parent.toString(), path.parent.suppliedSpelling)
        assertEquals(spelling, path.suppliedSpelling)
    }

    @Test
    fun equivalentSpellingsShareIdentityWithoutLosingTheirOriginalForm() {
        val raw = Paths.get("path contracts", "nested", "..", "file.tex")
        val path = MPPPath(raw.toString())
        val absolute = raw.toAbsolutePath().normalize()
        assertFalse(path.isAbsolute)
        assertEquals(raw, path.providedPath)
        assertEquals(absolute, path.toJVMPath())
        assertEquals(path, MPPPath(absolute.toString()))
        assertEquals(path.hashCode(), MPPPath(absolute.toString()).hashCode())
        assertEquals(absolute.toString(), path.toString())
        assertEquals(path, MPPPath(path))
        assertEquals(absolute, path.normalize().toJVMPath())
        assertTrue(path.normalize().isAbsolute)
    }

    @Test
    fun prefixesCompareWholeProvidedSegmentsAndResolutionHonorsAbsoluteChildren() {
        val path = MPPPath("parent/child/file.tex")
        assertTrue(path.startsWith(MPPPath("parent/child")))
        assertFalse(path.startsWith(MPPPath("parent/chi")))
        assertFalse(path.startsWith(MPPPath("parent/child").normalize()))
        assertTrue(path.normalize().startsWith(MPPPath("parent/child").normalize()))
        val absolute = MPPPath("child.tex").normalize()
        assertEquals(absolute, MPPPath("other").resolve(absolute))
        assertEquals(absolute, MPPPath("other").resolve(absolute.toString()))
        assertEquals(MPPPath("parent/child/sibling.tex"), path.resolveSibling("sibling.tex"))
    }

    @Test
    fun emptyPathsRootParentsAndNativeCaseRulesRemainConsistent() {
        assertEquals(MPPPath("."), MPPPath(""))
        val root = MPPPath(Paths.get("").toAbsolutePath().root.toString())
        assertEquals(root, root.parent)
        val upper = Paths.get("CaseSensitivePath.tex")
        val lower = Paths.get("casesensitivepath.tex")
        assertEquals(upper == lower, MPPPath(upper.toString()) == MPPPath(lower.toString()))
        assertEquals(upper.startsWith(lower), MPPPath(upper.toString()).startsWith(MPPPath(lower.toString())))
    }

    @Test
    fun existingPathsExposeLiveMetadataAndUtf8Content() {
        val root = Files.createTempDirectory("arara path contract ").toMPPPath()
        try {
            val file = root.resolve("ação 目录.tex")
            assertFalse(file.exists)
            file.writeText("first\r\nsecond\nlast")
            assertTrue(file.isRegularFile)
            assertFalse(file.isDirectory)
            assertEquals(listOf("first", "second", "last"), file.readLines())
            assertEquals("first\r\nsecond\nlast", file.readText())
            assertEquals(Files.size(file.toJVMPath()), file.fileSize)
            file.writeText("short")
            assertEquals(5L, file.fileSize)
            assertTrue(root.isDirectory)
            assertFalse(root.isRegularFile)
        } finally {
            root.removeDirectory()
        }
    }

    @Test
    fun linksExposeTargetContentWithoutChangingLexicalIdentity() {
        val root = Files.createTempDirectory("arara path links ").toMPPPath()
        try {
            val target = root.resolve("target.tex").apply { writeText("target content") }
            val link = root.resolve("link.tex")
            val chained = root.resolve("chained.tex")
            val supported = try {
                Files.createSymbolicLink(link.toJVMPath(), Paths.get("target.tex"))
                Files.createSymbolicLink(chained.toJVMPath(), Paths.get("link.tex"))
                true
            } catch (_: IOException) {
                false
            } catch (_: UnsupportedOperationException) {
                false
            } catch (_: SecurityException) {
                false
            }
            assumeTrue(supported, "Symbolic links are unavailable on this host")
            assertTrue(chained.exists)
            assertTrue(chained.isRegularFile)
            assertEquals(target.fileSize, chained.fileSize)
            assertEquals(target.lastModified.toString(), chained.lastModified.toString())
            assertEquals(target.readText(), chained.readText())
            assertFalse(chained == target)
            assertEquals("chained.tex", chained.fileName)
            Files.delete(target.toJVMPath())
            assertFalse(chained.exists)
            assertFalse(chained.isRegularFile)
        } finally {
            root.removeDirectory()
        }
    }

    @Test
    fun nativeBridgesExportNormalizedAbsolutePaths() {
        val native: Path = Paths.get("relative", "..", "file.tex")
        assertEquals(native.toAbsolutePath().normalize(), native.toMPPPath().toJVMPath())
        assertEquals(native.toAbsolutePath().normalize().toFile(), native.toFile().toMPPPath().toJVMFile())
        assertEquals(native.toAbsolutePath().normalize(), native.toMPPPath().normalize().toJVMPath())
    }

    @Test
    fun changingUserDirDoesNotChangeTheNativeResolutionBase() {
        val previous = System.getProperty("user.dir")
        val before = MPPPath("working-directory-contract")
        try {
            System.setProperty("user.dir", Paths.get(previous, "other").toString())
            assertEquals(before, MPPPath("working-directory-contract"))
        } finally {
            System.setProperty("user.dir", previous)
        }
    }

    @Test
    fun nativeFilesystemProvidersAreNotRedirectedToTheSystemFilesystem() {
        val root = Files.createTempDirectory("arara zip provider ")
        val uri = java.net.URI.create("jar:" + root.resolve("paths.zip").toUri())
        try {
            java.nio.file.FileSystems.newFileSystem(uri, mapOf("create" to "true")).use { fs ->
                val directory = fs.getPath("/book").toMPPPath()
                directory.createDirectories()
                val file = directory.resolve("chapter.tex")
                file.writeText("zip content")
                assertEquals("zip content", file.readText())
                assertEquals(fs, file.toJVMPath().fileSystem)
                assertEquals(listOf(file), directory.listFiles())
                assertEquals(listOf(file), directory.listEntries())
            }
        } finally {
            root.toMPPPath().removeDirectory()
        }
    }
}
