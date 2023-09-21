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
            MPPPath("quack.log").fileName,
            FileHandling.changeExtension(MPPPath("quack.tex"), "log").fileName,
        )
    }

    @Test
    fun shouldChangeExtensionOfFileWithoutExtension() {
        assertEquals(
            MPPPath("quack.log").fileName,
            FileHandling.changeExtension(MPPPath("quack"), "log").fileName,
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
    fun shouldGenerateCorrectCRCSum() {
        assertEquals(1616727774, FileHandling.calculateHash(MPPPath("../LICENSE")))
    }
}
