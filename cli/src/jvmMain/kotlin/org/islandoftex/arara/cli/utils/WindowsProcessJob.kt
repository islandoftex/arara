// SPDX-License-Identifier: BSD-3-Clause
package org.islandoftex.arara.cli.utils

import com.sun.jna.IntegerType
import com.sun.jna.Native
import com.sun.jna.Pointer
import com.sun.jna.Structure
import com.sun.jna.win32.StdCallLibrary
import io.github.oshai.kotlinlogging.KotlinLogging
import org.islandoftex.arara.api.utils.OS

/** Windows closes the private job handle even when TerminateProcess bypasses JVM shutdown hooks. */
internal object WindowsProcessJob {
    private val logger = KotlinLogging.logger { }
    private val lifetime by lazy {
        try {
            val job = Lifetime(Native.load("kernel32", Kernel32::class.java))
            Runtime.getRuntime().addShutdownHook(Thread({ job.disarm() }, "arara-windows-job"))
            job.assign()
            job
        } catch (exception: LinkageError) {
            unavailable(exception)
        } catch (exception: IllegalStateException) {
            unavailable(exception)
        } catch (exception: IllegalArgumentException) {
            unavailable(exception)
        } catch (exception: SecurityException) {
            unavailable(exception)
        }
    }

    private fun unavailable(exception: Throwable): Lifetime? {
        logger.warn(exception) { "Windows process-tree protection is unavailable" }
        return null
    }

    fun install() {
        if (OS.isWindows) lifetime?.arm()
    }

    fun finish() {
        if (OS.isWindows) lifetime?.relax()
    }

    internal class Lifetime(private val kernel: Kernel32) {
        private var handle: Pointer? = null

        @Synchronized
        fun assign() {
            check(handle == null)
            val job = checkNotNull(kernel.CreateJobObjectW(null, null)) { failure("CreateJobObjectW") }
            var assigned = false
            try {
                configure(job, KILL_ON_JOB_CLOSE)
                check(kernel.AssignProcessToJobObject(job, kernel.GetCurrentProcess())) {
                    failure("AssignProcessToJobObject")
                }
                handle = job
                assigned = true
            } finally {
                if (!assigned) kernel.CloseHandle(job)
            }
        }

        @Synchronized
        fun arm() = setFlags(KILL_ON_JOB_CLOSE)

        @Synchronized
        fun relax() = setFlags(0)

        private fun setFlags(flags: Int) {
            val job = handle ?: return
            try {
                configure(job, flags)
            } catch (exception: IllegalStateException) {
                logger.warn(exception) { "Could not update Windows process-tree protection" }
            }
        }

        @Synchronized
        fun disarm() {
            val job = handle ?: return
            // Normal completion may intentionally leave a detached viewer or background tool running.
            // Forced termination never runs this hook, so Windows kills every process in the job.
            try {
                configure(job, 0)
                kernel.CloseHandle(job)
                handle = null
            } catch (exception: IllegalStateException) {
                logger.warn(exception) { "Could not disarm Windows process-tree protection" }
            }
        }

        private fun configure(job: Pointer, flags: Int) {
            val limits = Limits().apply { basic.flags = flags; write() }
            check(kernel.SetInformationJobObject(job, EXTENDED_LIMIT_INFORMATION, limits.pointer, limits.size())) {
                failure("SetInformationJobObject")
            }
        }

        private fun failure(operation: String): String = "$operation failed (Windows error ${kernel.GetLastError()})"
    }

    internal class SizeT : IntegerType(Native.SIZE_T_SIZE, 0, true) {
        override fun toByte(): Byte = toLong().toByte()
        override fun toShort(): Short = toLong().toShort()
    }

    @Structure.FieldOrder("processTime", "jobTime", "flags", "minimumWorkingSet", "maximumWorkingSet",
        "activeProcessLimit", "affinity", "priorityClass", "schedulingClass")
    internal class BasicLimits : Structure(ALIGN_MSVC) {
        @JvmField var processTime: Long = 0
        @JvmField var jobTime: Long = 0
        @JvmField var flags: Int = 0
        @JvmField var minimumWorkingSet = SizeT()
        @JvmField var maximumWorkingSet = SizeT()
        @JvmField var activeProcessLimit: Int = 0
        @JvmField var affinity = SizeT()
        @JvmField var priorityClass: Int = 0
        @JvmField var schedulingClass: Int = 0
    }

    @Structure.FieldOrder("readOperations", "writeOperations", "otherOperations",
        "readBytes", "writeBytes", "otherBytes")
    internal class IoCounters : Structure(ALIGN_MSVC) {
        @JvmField var readOperations: Long = 0
        @JvmField var writeOperations: Long = 0
        @JvmField var otherOperations: Long = 0
        @JvmField var readBytes: Long = 0
        @JvmField var writeBytes: Long = 0
        @JvmField var otherBytes: Long = 0
    }

    @Structure.FieldOrder("basic", "io", "processMemory", "jobMemory", "peakProcessMemory", "peakJobMemory")
    internal class Limits : Structure(ALIGN_MSVC) {
        @JvmField var basic = BasicLimits()
        @JvmField var io = IoCounters()
        @JvmField var processMemory = SizeT()
        @JvmField var jobMemory = SizeT()
        @JvmField var peakProcessMemory = SizeT()
        @JvmField var peakJobMemory = SizeT()
    }

    // JNA resolves these names against the native Windows exports.
    @Suppress("FunctionName")
    internal interface Kernel32 : StdCallLibrary {
        fun CreateJobObjectW(attributes: Pointer?, name: Pointer?): Pointer?
        fun SetInformationJobObject(job: Pointer, informationClass: Int, information: Pointer, length: Int): Boolean
        fun GetCurrentProcess(): Pointer
        fun AssignProcessToJobObject(job: Pointer, process: Pointer): Boolean
        fun CloseHandle(handle: Pointer): Boolean
        fun GetLastError(): Int
    }

    private const val EXTENDED_LIMIT_INFORMATION = 9
    private const val KILL_ON_JOB_CLOSE = 0x2000
}
