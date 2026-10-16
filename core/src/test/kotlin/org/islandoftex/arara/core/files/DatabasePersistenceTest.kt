// SPDX-License-Identifier: BSD-3-Clause
package org.islandoftex.arara.core.files

import java.nio.file.Files
import java.nio.file.attribute.FileTime
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue
import org.islandoftex.arara.api.AraraException
import org.islandoftex.arara.api.AraraIOException
import org.islandoftex.arara.api.files.MPPPath
import org.islandoftex.arara.api.files.toJVMPath
import org.islandoftex.arara.api.files.toMPPPath
import org.islandoftex.arara.core.localization.LanguageController

class DatabasePersistenceTest {
    private fun fixture(check: (MPPPath, MPPPath) -> Unit) {
        val root = Files.createTempDirectory("arara cache records ").toMPPPath()
        try {
            check(root, root.resolve("arara.yaml"))
        } finally {
            root.removeDirectory()
        }
    }

    @Test
    fun missingAndEmptyCachesAreEmptyAndSignedLongValuesRoundTrip() = fixture { root, cache ->
        assertEquals(Database(), Database.load(cache))
        assertFalse(cache.exists)
        Database().save(cache)
        assertTrue(cache.readText().startsWith("!database\n"))
        assertEquals(Database(), Database.load(cache))
        val database = Database()
        database[root.resolve("min")] = Long.MIN_VALUE
        database[root.resolve("max")] = Long.MAX_VALUE
        database.save(cache)
        val loaded = Database.load(cache)
        assertEquals(Long.MIN_VALUE, loaded[root.resolve("min")])
        assertEquals(Long.MAX_VALUE, loaded[root.resolve("max")])
    }

    @Test
    fun yamlPreservesUnusualPathsAndOrderingIsDeterministic() = fixture { root, cache ->
        val victim = root.resolve("victim.tex")
        val hostile = mutableMapOf(
            victim.toString() + " 123\nforged.tex" to 7L,
            root.resolve("dir with spaces/ação 目录.tex").toString() to 8L,
            root.toString() + "/tabs\tand\rcarriage.tex" to 9L
        )
        val database = Database(hostile)
        database.save(cache)
        val first = cache.readText()
        assertTrue(first.startsWith("!database\n"))
        assertEquals(database, Database.load(cache))
        assertNull(Database.load(cache)[victim])
        Database(hostile.entries.reversed().associate { it.toPair() }.toMutableMap()).save(cache)
        assertEquals(first, cache.readText())
    }

    @Test
    fun existingYamlCachesRemainReadable() = fixture { root, cache ->
        val source = root.resolve("file.tex")
        val key = source.toString().replace("\\", "\\\\").replace("\"", "\\\"")
        cache.writeText("!database\nmap:\n  \"$key\": 123\n")
        assertEquals(123L, Database.load(cache)[source])
    }

    @Test
    fun legacyFlowMapsAndFullWidthHashesRemainReadable() = fixture { root, cache ->
        val entries = linkedMapOf(
            root.resolve("relative path.tex").normalize() to 4294967295L,
            root.resolve("windows\\file.tex").normalize() to Long.MAX_VALUE,
            root.resolve("001").normalize() to Long.MIN_VALUE
        )
        val flow = entries.entries.joinToString(", ") { (path, hash) ->
            "'${path.toString().replace("'", "''")}': $hash"
        }
        cache.writeText("!database\nmap: { $flow }\n")
        val database = Database.load(cache)
        entries.forEach { (path, hash) -> assertEquals(hash, database[path]) }
        database.save(cache)
        assertEquals(database, Database.load(cache))
    }

    @Test
    fun malformedCachesAreRejectedWithoutChangingTheirContents() = fixture { root, cache ->
        val invalid = listOf(
            "foreign: content", "not-a-database",
            "!database\nmap: [unterminated\n",
            "!database\nmap:\n  file: not-a-number\n",
            "!database\nmap:\n  file: 9223372036854775808\n"
        )
        root.resolve("file.tex").writeText("source")
        for (content in invalid) {
            cache.writeText(content)
            val failure = assertFailsWith<AraraException> { FileHandling.hasChanged(root.resolve("file.tex"), cache) }
            assertEquals(LanguageController.messages.errorLoadCouldNotLoadDatabase(cache.toString()), failure.message)
            assertEquals(content, cache.readText())
        }
    }

    @Test
    fun failuresKeepTheirUnderlyingIoError() = fixture { root, _ ->
        val save = assertFailsWith<AraraException> { Database().save(root) }
        assertTrue(save.exception is AraraIOException)
        val load = assertFailsWith<AraraException> { Database.load(root) }
        assertTrue(load.exception is AraraIOException)
    }

    @Test
    fun changesPersistAcrossReloadsAndUnchangedFilesDoNotRewriteTheCache() = fixture { root, cache ->
        val source = root.resolve("run.tex").apply { writeText("first") }
        assertTrue(FileHandling.hasChanged(source, cache))
        Files.setLastModifiedTime(cache.toJVMPath(), FileTime.fromMillis(123456000L))
        val timestamp = Files.getLastModifiedTime(cache.toJVMPath())
        assertFalse(FileHandling.hasChanged(source, cache))
        assertEquals(timestamp, Files.getLastModifiedTime(cache.toJVMPath()))
        source.writeText("second")
        assertTrue(FileHandling.hasChanged(source, cache))
        assertFalse(FileHandling.hasChanged(source, cache))
        Files.delete(source.toJVMPath())
        assertTrue(FileHandling.hasChanged(source, cache))
        assertFalse(FileHandling.hasChanged(source, cache))
        assertNull(Database.load(cache)[source])
    }
}
