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
