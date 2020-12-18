// SPDX-License-Identifier: BSD-3-Clause
package org.islandoftex.arara.api.files

public expect class MPPPath {
    public val isAbsolute: Boolean
    public val fileName: String
    public val parent: MPPPath

    public val exists: Boolean
    public val isDirectory: Boolean

    public fun startsWith(p: MPPPath): Boolean
    public fun endsWith(p: MPPPath): Boolean
    public fun normalize(): MPPPath
    public fun resolve(p: String): MPPPath
    public fun resolve(p: MPPPath): MPPPath
    public fun resolveSibling(p: String): MPPPath
    public fun resolveSibling(p: MPPPath): MPPPath
    public fun toAbsolutePath(): MPPPath
}
