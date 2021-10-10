// SPDX-License-Identifier: BSD-3-Clause
package org.islandoftex.arara.api.files

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
        assertTrue(MPPPath("/test").isAbsolute)
    }
    @Test
    fun shouldConsiderRootAbsolute() {
        // korio does not handle this
        assertTrue(MPPPath("/").isAbsolute)
    }

    // path normalization
    @Test
    fun shouldNormalizeDots() {
        assertEquals(
            MPPPath("/tmp/./quack/..").normalize().toString(),
            MPPPath("/tmp/").normalize().toString()
        )
    }
    @Test
    fun shouldNoExceedRoot() {
        assertEquals(
            MPPPath("/tmp/../../..").normalize().toString(),
            MPPPath("/").toString()
        )
    }

    // parenting
    @Test
    fun shouldUseRootAsParentOfRoot() {
        assertEquals(
            MPPPath("/").parent.toString(),
            MPPPath("/").toString()
        )
    }
    @Test
    fun shouldDetermineParentCorrectly() {
        assertEquals(
            MPPPath("/tmp/./quack/..").parent.normalize().toString(),
            MPPPath("/").toString()
        )
        assertEquals(
            MPPPath("/tmp/./quack/..").normalize().parent.toString(),
            MPPPath("/").toString()
        )
    }
}
