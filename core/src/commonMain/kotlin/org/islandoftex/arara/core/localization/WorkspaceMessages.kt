// SPDX-License-Identifier: BSD-3-Clause
package org.islandoftex.arara.core.localization

/** Typed messages for workspace messages. */
interface WorkspaceMessages {
    /** Validating build directory: the path */
    fun infoBuildDirectoryValidating(path: String): String

    /** Preparing build directory: the path */
    fun infoBuildDirectoryPreparing(path: String): String

    /** Cleaning build directory: the path */
    fun infoBuildDirectoryCleaning(path: String): String

    /** Build directory ready: copied the file count input files in the elapsed seconds s. */
    fun infoBuildDirectoryPrepared(files: Int, seconds: String): String

    /** Build directory validated. */
    val infoBuildDirectoryValidated: String

    /** Build directory cleaned. */
    val infoBuildDirectoryCleaned: String

    /** Build directory operation failed. */
    val errorBuildDirectoryOperationFailed: String

    /** Invalid or overlapping build directory: the path. */
    fun errorBuildDirectoryInvalid(path: String): String

    /** Input or destination escapes the workspace mapping: the path. */
    fun errorBuildInputInvalid(path: String): String

    /** The build directory is not managed by this project: the path. */
    fun errorBuildWorkspaceUnmanaged(path: String): String

    /** Build input collides with another input or an existing output: the path. */
    fun errorBuildInputCollision(path: String): String

    /** Symbolic link cycle in build inputs: the path. */
    fun errorBuildLinkCycle(path: String): String

    /** Build input does not exist or is not a regular file or directory: the path. */
    fun errorBuildInputMissing(path: String): String
}
