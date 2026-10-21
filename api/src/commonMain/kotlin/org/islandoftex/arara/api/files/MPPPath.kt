// SPDX-License-Identifier: BSD-3-Clause
package org.islandoftex.arara.api.files

import okio.BufferedSource
import org.islandoftex.arara.api.AraraIOException
import org.islandoftex.arara.api.time.DateTime

/**
 * A lexical path with its original spelling and an absolute, normalized reference.
 * Equality, hashing, display and I/O use the reference; [isAbsolute], [startsWith]
 * and resolution use the original spelling. Normalization does not resolve symlinks.
 * JVM identity follows the native path provider, including Windows case handling.
 */
// since this should provide a fluent API to deal with paths,
// we are adding an annotation to suppress detekt's warning
// on too many functions
@Suppress("TooManyFunctions")
public expect class MPPPath {
    /**
     * Expect at least a constructor that accepts a string to be converted
     * into a native path.
     */
    // detekt does not identify expect / actual classes yet
    @Suppress("unused")
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

    /**
     * Compares the original path spellings by whole segments. Normalize both
     * paths first to compare their absolute locations.
     */
    public fun startsWith(p: MPPPath): Boolean

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
     * Write [text] to the file specified at the current path. Overwrite by
     * default if [append] is not set. Fails with an [AraraIOException]
     * exception if the file is a directory or access is impossible.
     */
    @Throws(AraraIOException::class)
    public fun writeText(text: String, append: Boolean = false)

    /**
     * Resolve the child [p] against the current path.
     */
    public operator fun div(p: String): MPPPath

    /**
     * Resolve the child [p] against the current path.
     */
    public operator fun div(p: MPPPath): MPPPath

    /**
     * Treat this path as a directory identifier and create a directory at
     * this location including all missing parents.
     * Does nothing if a directory exists at the current location.
     *
     * Fails with an [AraraIOException] if the directory could not be created
     * or a regular file exists at the location.
     */
    @Throws(AraraIOException::class)
    public fun createDirectories()

    /**
     * Treat this path as a directory identifier and remove it at
     * this location including its contents.
     * Does nothing if this directory does not exist at the current location.
     *
     * Fails with an [AraraIOException] if the directory could not be removed
     * or a regular file exists at the location.
     */
    @Throws(AraraIOException::class)
    public fun removeDirectory()

    /**
     * Absolute normalized Okio path, interpreted using the host's native syntax.
     */
    public fun toOkioPath(): okio.Path

    /**
     * Strict metadata lookup; missing paths and inspection errors raise [AraraIOException].
     */
    @Throws(AraraIOException::class)
    public fun metadata(): PathMetadata

    /**
     * Read a regular file using a source closed when [block] returns. I/O failures are wrapped.
     */
    @Throws(AraraIOException::class)
    public fun <T> read(block: (BufferedSource) -> T): T

    /**
     * Direct children, including directories and special files; lookup failures throw.
     */
    @Throws(AraraIOException::class)
    public fun listEntries(): List<MPPPath>

    /**
     * Regular files in path order. Follows directory links; traversal failures and cycles throw.
     */
    @Throws(AraraIOException::class)
    public fun listFiles(recursive: Boolean = false): List<MPPPath>
}
