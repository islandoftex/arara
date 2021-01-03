// SPDX-License-Identifier: BSD-3-Clause
package org.islandoftex.arara.api.files

import com.soywiz.klock.DateTime
import org.islandoftex.arara.api.AraraIOException

/**
 * Platform-independent object to deal with paths.
 */
public expect class MPPPath {
    /**
     * Expect at least a constructor that accepts a string to be converted
     * into a native path.
     */
    public constructor(path: String)

    /**
     * Check whether a path is absolute. Does not guarantee it is normalized.
     */
    public val isAbsolute: Boolean

    /**
     * Get the last segment of a path which by definition is the file name.
     */
    public val fileName: String

    /**
     * Get the file size of this file in bytes.
     */
    public val fileSize: Long

    /**
     * Get the last modification date of this file as timestamp.
     */
    public val lastModified: DateTime

    /**
     * Get the parent object of the currently represented path. If it has no
     * parent, i.e. it is the root, it is returned itself.
     */
    public val parent: MPPPath

    /**
     * Indicates whether the file exists.
     */
    public val exists: Boolean

    /**
     * Indicates whether the file is a directory. If `false` it does not mean,
     * the file is a regular file.
     */
    public val isDirectory: Boolean

    /**
     * Indicates whether the file is a regular file. If `false` it does not
     * mean, the file is a directory.
     */
    public val isRegularFile: Boolean

    /**
     * Checks whether the path starts with [p].
     */
    public fun startsWith(p: MPPPath): Boolean

    /**
     * Checks whether the path ends with [p].
     */
    public fun endsWith(p: MPPPath): Boolean

    /**
     * Normalizes the path. For [MPPPath] this means, the path is transformed
     * into an absolute path and then normalized.
     */
    public fun normalize(): MPPPath

    /**
     * Resolve the child [p] of the current path.
     */
    public fun resolve(p: String): MPPPath

    /**
     * Resolve the child [p] of the current path.
     */
    public fun resolve(p: MPPPath): MPPPath

    /**
     * Resolve the sibling [p] of the current path.
     */
    public fun resolveSibling(p: String): MPPPath

    /**
     * Resolve the sibling [p] of the current path.
     */
    public fun resolveSibling(p: MPPPath): MPPPath

    /**
     * Read lines from the file specified at the current path. Fails with an
     * [AraraIOException] exception if the file is a directory or access is
     * impossible.
     */
    @Throws(AraraIOException::class)
    public fun readLines(): List<String>

    /**
     * Read whole text from the file specified at the current path. Fails with
     * an [AraraIOException] exception if the file is a directory or access is
     * impossible.
     */
    @Throws(AraraIOException::class)
    public fun readText(): String

    /**
     * Write [text] to the file specified at the current path. Fails with an
     * [AraraIOException] exception if the file is a directory or access is
     * impossible.
     */
    @Throws(AraraIOException::class)
    public fun writeText(text: String)

    /**
     * Resolve the child [p] against the current path.
     */
    public operator fun div(p: String): MPPPath

    /**
     * Resolve the child [p] against the current path.
     */
    public operator fun div(p: MPPPath): MPPPath
}
