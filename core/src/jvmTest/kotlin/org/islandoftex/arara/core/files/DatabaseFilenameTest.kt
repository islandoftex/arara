// SPDX-License-Identifier: BSD-3-Clause
package org.islandoftex.arara.core.files

import java.nio.file.Path
import kotlin.random.Random
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue
import org.islandoftex.arara.api.files.MPPPath
import org.islandoftex.arara.api.files.toMPPPath
import org.junit.jupiter.api.io.TempDir

class DatabaseFilenameTest {
    @TempDir
    lateinit var directory: Path

    private fun assertRoundTrip(names: List<String>) {
        val root = directory.toMPPPath()
        val cache = root.resolve("arara.yaml")
        val entries = names.mapIndexed { index, name ->
            "${root}/$name" to index.toLong()
        }.toMap().toMutableMap()
        val database = Database(entries)
        database.save(cache)
        assertEquals(database, Database.load(cache))
        val content = cache.readText()
        Database(entries.entries.reversed().associate { it.toPair() }.toMutableMap()).save(cache)
        assertEquals(content, cache.readText())
    }

    @Test
    fun yamlSyntaxAndScalarLookingNamesRemainLiteralKeys() {
        assertRoundTrip(listOf(
            "true", "false", "null", "~", "001", "1e3", ".nan", "2026-10-29",
            "'single'", "\"double\"", "colon: value", "hash # comment", "[a, b]", "{a: b}",
            "&anchor", "*alias", "!database", "---", "...", "? key", "- item", "|", ">",
            "back\\slash", " leading", "trailing ", "  both  ", "line\nmap:\n  forged: 42"
        ))
    }

    @Test
    fun controlCharactersAndUnicodeSeparatorsRoundTripWithoutFolding() {
        // These are serialization keys, not files created on the host filesystem.
        val controls = (1..31).map(Int::toChar) + (0x7f..0x9f).map(Int::toChar) +
            listOf('\u2028', '\u2029')
        assertRoundTrip(controls.map { "before${it}after.tex" } + listOf(
            "ação 目录 Ελληνικά.tex", "parrot 🦜.tex", "é.tex", "e\u0301.tex", "zero\u200bwidth.tex"
        ))
    }

    @Test
    fun generatedUnicodeNamesAndLongPathsRoundTripDeterministically() {
        val random = Random(33)
        val alphabet = (1..127).filter { it != '/'.code }.map(Int::toChar) +
            listOf('é', '目', '\u0085', '\u2028', '\u2029', '\u200b')
        val names = List(128) { index ->
            "$index-" + List(64) { alphabet[random.nextInt(alphabet.size)] }.joinToString("")
        }
        assertRoundTrip(names + List(160) { "segment-$it" }.joinToString("/", postfix = "/file.tex"))
    }

    @Test
    fun portableUnusualFilenamesWorkThroughChangeDetection() {
        val root = directory.toMPPPath()
        val cache = root.resolve("arara.yaml")
        listOf("two words.tex", "ação 目录.tex", "[draft] #1.tex", "it's a file.tex").forEach { name ->
            val source: MPPPath = root.resolve(name).apply { writeText("first") }
            assertTrue(FileHandling.hasChanged(source, cache))
            assertFalse(FileHandling.hasChanged(source, cache))
            source.writeText("second")
            assertTrue(FileHandling.hasChanged(source, cache))
            assertFalse(FileHandling.hasChanged(source, cache))
        }
    }
}
