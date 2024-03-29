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
     * relative file names against. Furthermore, all tools will be started
     * within the project's working directory.
     */
    public val workingDirectory: MPPPath

    /**
     * The project's files. A file can only be added once to a project.
     */
    public val files: Set<ProjectFile>

    /**
     * Projects this project depends on. Dependencies are identified by
     * their name.
     */
    public val dependencies: Set<String>
}
