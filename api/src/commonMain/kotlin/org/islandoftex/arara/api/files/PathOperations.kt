// SPDX-License-Identifier: BSD-3-Clause
package org.islandoftex.arara.api.files

import okio.BufferedSource
import org.islandoftex.arara.api.AraraIOException

/** Lexical path operations, retaining the receiver’s filesystem backend. */
public interface PathAlgebra {
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
     * Resolve the child [p] against the current path.
     */
    public operator fun div(p: String): MPPPath

    /**
     * Resolve the child [p] against the current path.
     */
    public operator fun div(p: MPPPath): MPPPath
}

/** Read-only filesystem access through a path’s instance-bound backend. */
public interface PathReading {
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

/** Filesystem mutation through a path’s instance-bound backend. */
public interface PathWriting {
    /**
     * Write [text] to the file specified at the current path. Overwrite by
     * default if [append] is not set. Fails with an [AraraIOException]
     * exception if the file is a directory or access is impossible.
     */
    @Throws(AraraIOException::class)
    public fun writeText(text: String, append: Boolean = false)

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
}
