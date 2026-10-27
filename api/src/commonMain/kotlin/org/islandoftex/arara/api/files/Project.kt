// SPDX-License-Identifier: BSD-3-Clause
package org.islandoftex.arara.api.files

/**
 * A project in arara's sense is a collection of files that should be compiled
 * together. Files in a project are not dependent on each other.
 */
public interface Project {
    /**
     * A project has a name. This is user-defined. If run from the
     * command-line arara will fetch the name from the [workingDirectory].
     * Within one run, the name identifies a project uniquely.
     */
    public val name: String

    /**
     * The project's home. The working directory will be used to resolve
     * relative file names and load local configuration. Tools run here unless
     * [buildDirectory] selects a separate [executionDirectory].
     */
    public val workingDirectory: MPPPath

    /**
     * The project's files. A source path may occur only once across all projects
     * in one invocation. Use multiple directives to process a file repeatedly.
     */
    public val files: Set<ProjectFile>

    /** Optional isolated workspace; configuration and source ownership still use [workingDirectory]. */
    public val buildDirectory: BuildDirectory? get() = null

    /** Source selection for continuous compilation; ordinary execution ignores these settings. */
    public val watch: WatchOptions get() = WatchOptions()

    /** Directory in which tools run; inputs are staged here when a workspace is configured. */
    public val executionDirectory: MPPPath
        get() = buildDirectory?.let { workingDirectory.resolve(it.directory).normalize() } ?: workingDirectory

    /**
     * Projects this project depends on. Dependencies are identified by
     * their name.
     */
    public val dependencies: Set<String>
}
