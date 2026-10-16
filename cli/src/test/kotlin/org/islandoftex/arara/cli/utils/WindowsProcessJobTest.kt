// SPDX-License-Identifier: BSD-3-Clause
package org.islandoftex.arara.cli.utils

import com.sun.jna.Native
import com.sun.jna.Pointer
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue

class WindowsProcessJobTest {
    private class Kernel : WindowsProcessJob.Kernel32 {
        val operations = mutableListOf<String>()
        var create = true
        var configure = true
        var assign = true
        override fun CreateJobObjectW(attributes: Pointer?, name: Pointer?): Pointer? {
            operations.add("create")
            assertEquals(null, attributes)
            assertEquals(null, name)
            return if (create) Pointer(1) else null
        }
        override fun SetInformationJobObject(job: Pointer, informationClass: Int,
            information: Pointer, length: Int): Boolean {
            assertEquals(9, informationClass)
            assertEquals(if (Native.POINTER_SIZE == 8) 144 else 112, length)
            operations.add("flags:${information.getInt(16)}")
            return configure
        }
        override fun GetCurrentProcess(): Pointer = Pointer(-1)
        override fun AssignProcessToJobObject(job: Pointer, process: Pointer): Boolean {
            operations.add("assign")
            assertEquals(Pointer(-1), process)
            return assign
        }
        override fun CloseHandle(handle: Pointer): Boolean {
            operations.add("close")
            return true
        }
        override fun GetLastError(): Int = 5
    }

    @Test
    fun jobIsConfiguredBeforeAssignmentAndDisarmedBeforeNormalClose() {
        val kernel = Kernel()
        val lifetime = WindowsProcessJob.Lifetime(kernel)
        lifetime.assign()
        assertEquals(listOf("create", "flags:8192", "assign"), kernel.operations)
        lifetime.disarm()
        lifetime.disarm()
        assertEquals(listOf("create", "flags:8192", "assign", "flags:0", "close"), kernel.operations)
    }

    @Test
    fun normalCompletionCanRelaxAndRearmTheSameJobWithoutClosingOrReassigning() {
        val kernel = Kernel()
        val lifetime = WindowsProcessJob.Lifetime(kernel)
        lifetime.assign()
        lifetime.relax()
        lifetime.arm()
        assertEquals(listOf("create", "flags:8192", "assign", "flags:0", "flags:8192"), kernel.operations)
        lifetime.disarm()
    }

    @Test
    fun initializationFailuresCloseOnlyOwnedUnassignedHandles() {
        for (stage in listOf("create", "configure", "assign")) {
            val kernel = Kernel().apply {
                create = stage != "create"
                configure = stage != "configure"
                assign = stage != "assign"
            }
            val lifetime = WindowsProcessJob.Lifetime(kernel)
            val error = assertFailsWith<IllegalStateException> { lifetime.assign() }
            assertTrue("Windows error 5" in error.message.orEmpty())
            lifetime.disarm()
            assertEquals(if (stage == "create") 0 else 1, kernel.operations.count { it == "close" })
        }
    }

    @Test
    fun aFailedDisarmNeverClosesAnArmedJob() {
        val kernel = Kernel()
        val lifetime = WindowsProcessJob.Lifetime(kernel)
        lifetime.assign()
        kernel.configure = false
        lifetime.disarm()
        assertTrue("close" !in kernel.operations)
    }
}
