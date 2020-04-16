// SPDX-License-Identifier: BSD-3-Clause
package org.islandoftex.arara.dsl.language

/**
 * An executable block model to capture DSL methods within.
 */
class DSLExecutable {
    /**
     * The exit value of this block.
     */
    internal var exitValue = 0

    /**
     * Specify the exit value of the command.
     *
     * @param value The exit value.
     * @return The exit value.
     */
    fun exit(value: Int): Int = value.also { exitValue = value }
}
