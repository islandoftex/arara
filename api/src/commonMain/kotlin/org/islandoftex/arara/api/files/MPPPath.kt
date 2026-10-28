// SPDX-License-Identifier: BSD-3-Clause
package org.islandoftex.arara.api.files

import org.islandoftex.arara.api.AraraIOException
import org.islandoftex.arara.api.time.DateTime

/**
 * A lexical path with its original spelling and an absolute, normalized reference.
 * [suppliedSpelling] exposes the supplied text explicitly. Equality, hashing,
 * display and I/O use the reference; [isAbsolute], [startsWith]
 * and resolution use the original spelling. Normalization does not resolve symlinks.
 * JVM identity follows the native path provider, including Windows case handling.
 */
public expect class MPPPath : PathAlgebra, PathReading, PathWriting {
    /**
     * Expect at least a constructor that accepts a string to be converted
     * into a native path.
     */
    // The expect constructor is implemented by each platform.
    @Suppress("unused")
    public constructor(path: String)

    /**
     * Spelling supplied when constructing this path, before absolutizing or normalizing it.
     *
     * String construction retains the exact text, including redundant separators and an empty string.
     * Native path conversions use the native path's string representation. Copies retain this value;
     * derived paths use their resulting native spelling, so [normalize] produces a normalized spelling.
     * This value is for display or explicit textual matching, not path identity or filesystem access.
     */
    public val suppliedSpelling: String

    /**
     * Check whether a path is absolute. Does not guarantee it is normalized.
     */
    public val isAbsolute: Boolean

    /**
     * Get the last segment of a path which by definition is the file name.
     */
    public val fileName: String

    /**
     * Get the byte size; throws [AraraIOException] when size is unavailable.
     */
    public val fileSize: Long

    /**
     * Get the modification time; throws [AraraIOException] when it is unavailable.
     */
    public val lastModified: DateTime

    /**
     * Get the parent object of the currently represented path. If it has no
     * parent, i.e. it is the root, it is returned itself.
     */
    public val parent: MPPPath

    /**
     * Best-effort existence check; false if missing or inspection fails.
     */
    public val exists: Boolean

    /**
     * Indicates whether the file is a directory. If `false` it does not mean,
     * the file is a regular file. Inspection failures return false.
     */
    public val isDirectory: Boolean

    /**
     * Indicates whether the file is a regular file. If `false` it does not
     * mean, the file is a directory. Inspection failures return false.
     */
    public val isRegularFile: Boolean
}
