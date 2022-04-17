// SPDX-License-Identifier: BSD-3-Clause
package org.islandoftex.arara.core.files

import org.islandoftex.arara.api.AraraException
import org.islandoftex.arara.api.files.MPPPath
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class FileHandlingTest {
    @Test
    fun shouldChangeExtensionOfFileWithExtension() {
        assertEquals(
            FileHandling.changeExtension(MPPPath("quack.tex"), "log").fileName,
            MPPPath("quack.log").fileName
        )
    }
    @Test
    fun shouldChangeExtensionOfFileWithoutExtension() {
        assertEquals(
            FileHandling.changeExtension(MPPPath("quack"), "log").fileName,
            MPPPath("quack.log").fileName
        )
    }

    @Test
    fun shouldGetSubdirectoryRelationshipRight() {
        assertTrue(FileHandling.isSubDirectory(MPPPath("../docs"), MPPPath("..")))
        assertFalse(FileHandling.isSubDirectory(MPPPath(".."), MPPPath("../docs")))
    }
    @Test
    fun shouldNotTreatFilesAsSubdirectories() {
        assertFalse(FileHandling.isSubDirectory(MPPPath("../LICENSE"), MPPPath("..")))
        assertFalse(FileHandling.isSubDirectory(MPPPath(".."), MPPPath("../LICENSE")))
    }

    @Test
    fun shouldFailGeneratingCRCSumOnInexistentFiles() {
        assertFailsWith<AraraException> {
            FileHandling.calculateHash(MPPPath("QUACK"))
        }
    }
    @Test
    fun shouldGenerateCorrectCRCSums() {
        assertEquals(FileHandling.calculateHash(MPPPath("../LICENSE")), 2951375576)
        assertEquals(FileHandling.calculateHash(MPPPath("../CODE_OF_CONDUCT.md")), 3856623865)
    }
}
