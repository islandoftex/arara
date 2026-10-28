// SPDX-License-Identifier: BSD-3-Clause
package org.islandoftex.arara.api.files

/** Input selection for a continuously compiled Lua project; paths resolve against its working directory. */
public data class WatchOptions(
    /** Additional files or directory trees, including paths outside the project. */
    public val paths: List<MPPPath> = emptyList(),
    /** Root-relative globs overriding ignore files and generated-file defaults. */
    public val include: List<String> = emptyList(),
    /** Root-relative globs excluding inputs; compilation targets remain watched. */
    public val exclude: List<String> = emptyList()
)

/** Notification preference; all modes reconcile contents periodically to recover missed events. */
public enum class WatchMode {
    /** Prefer native notifications, falling back to polling when unavailable. */
    AUTO,
    /** Require native notification registration; report unsupported filesystems. */
    NATIVE,
    /** Use timed content scans without native notification registration. */
    POLLING
}
