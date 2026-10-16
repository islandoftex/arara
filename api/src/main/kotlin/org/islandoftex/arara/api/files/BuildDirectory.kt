// SPDX-License-Identifier: BSD-3-Clause
package org.islandoftex.arara.api.files

/** Isolated project workspace. Relative paths are resolved against the source working directory. */
public data class BuildDirectory(
    /** Workspace receiving staged inputs and generated output. */
    public val directory: MPPPath,
    /** Null selects the source tree with ignore files; a list selects only targets and these inputs. */
    public val inputs: List<BuildSelection>? = null,
    /** Explicit source-to-workspace mappings, required for inputs outside the source tree. */
    public val mappings: List<BuildInput> = emptyList(),
    /** Whether symbolic links are preserved and rebased, or materialized as copies. */
    public val symlinks: BuildSymlinks = BuildSymlinks.PRESERVE
)

/** An explicit file/directory path or a source-relative wildcard pattern. */
public sealed interface BuildSelection {
    /** A literal path. Relative paths resolve against the project's source directory. */
    public data class Path(
        /** File or directory within the source tree; wildcard characters have no special meaning. */
        public val path: MPPPath
    ) : BuildSelection

    /** A wildcard selection, matched against paths relative to the source directory. */
    public data class Glob(
        /** Slash-separated wildcard pattern; absolute paths and parent traversal are rejected. */
        public val pattern: String
    ) : BuildSelection
}

/** An input file or directory and its relative destination within a build directory. */
public data class BuildInput(
    /** Source path, resolved against the project's source directory. */
    public val source: MPPPath,
    /** Relative workspace path; parent traversal and absolute paths are rejected. */
    public val destination: String
)

/** Treatment of symbolic links while staging project inputs. */
public enum class BuildSymlinks {
    /** Rebase links to the corresponding staged targets. */
    PRESERVE,
    /** Copy target contents, rejecting directory cycles. */
    COPY
}
