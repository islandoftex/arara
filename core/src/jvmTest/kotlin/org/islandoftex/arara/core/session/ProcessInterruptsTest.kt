// SPDX-License-Identifier: BSD-3-Clause
package org.islandoftex.arara.core.session

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue
import org.islandoftex.arara.api.utils.OS

class ProcessInterruptsTest {

    @Test
    fun interruptDeferralIsNestedScopedAndThreadLocal() {
        val command = listOf("tool", "argument with spaces", "\$(literal)")
        assertEquals(command, ProcessInterrupts.elements(command))
        ProcessInterrupts.defer {
            val wrapped = ProcessInterrupts.elements(command)
            if (OS.isWindows) assertEquals(command, wrapped) else assertEquals(command, wrapped.takeLast(command.size))
            ProcessInterrupts.defer { assertEquals(wrapped, ProcessInterrupts.elements(command)) }
            assertEquals(wrapped, ProcessInterrupts.elements(command))
            val observed = java.util.concurrent.atomic.AtomicReference<List<String>>()
            val thread = Thread { observed.set(ProcessInterrupts.elements(command)) }
            thread.start()
            thread.join()
            assertEquals(command, observed.get())
        }
        assertFailsWith<IllegalStateException> { ProcessInterrupts.defer { error("failure") } }
        assertEquals(command, ProcessInterrupts.elements(command))
        assertTrue(ProcessInterrupts.defer { true })
    }
}
