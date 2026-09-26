// SPDX-License-Identifier: BSD-3-Clause
package org.islandoftex.arara.build

import org.gradle.api.DefaultTask
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.Optional
import org.gradle.api.tasks.TaskAction

/**
 * Holds the smoke floor policy and prints the resolved Java runtime's
 * launcher path as the sole output line, so shell consumers can read
 * it; a missing runtime is announced loudly instead of failing the
 * task. A smoke floor that differs from the build floor is announced
 * as well, but never fails the run. The resolution itself happens in
 * the build script that wires this task, which owns the toolchain
 * service on this class's behalf.
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
    @get:Optional
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
        val path = launcherPath.orNull
        if (path == null) {
            logger.warn(
                    "no floor runtime was resolved; the smoke suite will skip " +
                            "its floor leg"
            )
        } else {
            println(path)
        }
    }
}
