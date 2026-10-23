// SPDX-License-Identifier: BSD-3-Clause
package org.islandoftex.arara.api

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertSame

class ExceptionsTest {
    @Test
    fun exceptionCausesRetainTheCompatibilityProperty() {
        val original = IllegalArgumentException("original")
        val exception = AraraException("context", original)
        assertSame(original, exception.exception)
        assertSame(original, exception.cause)
        assertEquals("context", exception.message)
        assertNull(AraraException("message only").cause)
    }

    @Test
    fun throwableCausesRemainReachableThroughTheCompatibilityWrapper() {
        val original: Throwable = AssertionError("original")
        val exception = AraraException("context", original)
        assertSame(exception.exception, exception.cause)
        assertSame(original, exception.cause?.cause)
    }
}
