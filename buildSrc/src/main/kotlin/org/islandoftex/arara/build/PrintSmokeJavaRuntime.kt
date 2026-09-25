// SPDX-License-Identifier: BSD-3-Clause
package org.islandoftex.arara.build

import org.gradle.api.DefaultTask
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.TaskAction

/**
 * Holds the smoke floor policy and prints the resolved Java runtime's
 * launcher path as the sole output line, so shell preambles can
 * consume it. A smoke floor that differs from the build floor is
 * announced loudly, but never fails the task. The runtime itself is
 * resolved by the build script that wires this task, which holds the
 * engine-internal toolchain service reference on this class's behalf.
 */
abstract class PrintSmokeJavaRuntime : DefaultTask() {

    /** The Java version the smoke suite verifies on. */
    @get:Input
    abstract val smokeVersion: Property<Int>

    /** The vendor the smoke suite verifies on. */
    @get:Input
    abstract val smokeVendor: Property<String>

    /** The version the build itself compiles against, for the notice. */
    @get:Input
    abstract val buildFloor: Property<Int>

    /** The launcher path of the runtime, provisioned on demand. */
    @get:Input
    abstract val launcherPath: Property<String>

    @TaskAction
    fun print() {
        val floor = smokeVersion.get()
        if (floor != buildFloor.get()) {
            logger.warn(
                    "the smoke floor ({}) differs from the build floor ({}); " +
                            "presumed intentional",
                    floor,
                    buildFloor.get()
            )
        }
        println(launcherPath.get())
    }
}
