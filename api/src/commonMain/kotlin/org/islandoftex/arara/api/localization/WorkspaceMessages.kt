// SPDX-License-Identifier: BSD-3-Clause
package org.islandoftex.arara.api.localization

/** Typed messages for workspace messages. */
public interface WorkspaceMessages {
    /** Validating build directory: the path */
    public fun infoBuildDirectoryValidating(path: String): String

    /** Preparing build directory: the path */
    public fun infoBuildDirectoryPreparing(path: String): String

    /** Cleaning build directory: the path */
    public fun infoBuildDirectoryCleaning(path: String): String

    /** Build directory ready: copied the file count input files in the elapsed seconds s. */
    public fun infoBuildDirectoryPrepared(files: Int, seconds: String): String

    /** Build directory validated. */
    public val infoBuildDirectoryValidated: String

    /** Build directory cleaned. */
    public val infoBuildDirectoryCleaned: String

    /** Build directory operation failed. */
    public val errorBuildDirectoryOperationFailed: String

    /** Invalid or overlapping build directory: the path. */
    public fun errorBuildDirectoryInvalid(path: String): String

    /** Input or destination escapes the workspace mapping: the path. */
    public fun errorBuildInputInvalid(path: String): String

    /** The build directory is not managed by this project: the path. */
    public fun errorBuildWorkspaceUnmanaged(path: String): String

    /** Build input collides with another input or an existing output: the path. */
    public fun errorBuildInputCollision(path: String): String

    /** Symbolic link cycle in build inputs: the path. */
    public fun errorBuildLinkCycle(path: String): String

    /** Build input does not exist or is not a regular file or directory: the path. */
    public fun errorBuildInputMissing(path: String): String
}
