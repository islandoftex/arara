// SPDX-License-Identifier: BSD-3-Clause
package org.islandoftex.arara.api.files

import java.io.File
import java.nio.file.Path as JVMPath
import java.nio.file.Paths
import kotlin.jvm.Throws
import okio.FileMetadata
import okio.Path
import okio.Path.Companion.toPath
import okio.buffer
import org.islandoftex.arara.api.AraraIOException
import org.islandoftex.arara.api.time.DateTime
import org.islandoftex.arara.api.utils.OS

/**
 * A path model for arara, built on a multiplatform path algebra and
 * retaining both the path exactly as provided and an absolutized,
 * normalized reference of it.
 *
 * Members that inspect the raw spelling (`isAbsolute`, `startsWith`,
 * the `resolve` inputs and the JVM bridges) read the provided path;
 * identity, I/O and display operate on the reference.
 */
// since this should provide a fluent API to deal with paths,
// we are adding an annotation to suppress detekt's warning
// on too many functions
@Suppress("TooManyFunctions")
public class MPPPath {
    internal val providedPath: Path
    internal val referencePath: Path

    /**
     * A constructor that accepts a string to be converted into a
     * native path.
     */
    public constructor(path: String) {
        val provided = platformSpelling(path).toPath()
        this.providedPath = provided
        this.referencePath = provided.absolutized()
    }

    /**
     * A constructor that creates a copy of the provided path, retaining
     * both its provided and reference forms.
     */
    public constructor(initPath: MPPPath) {
        providedPath = initPath.providedPath
        referencePath = initPath.referencePath
    }

    /**
     * Internal constructor to handle platform paths directly. It
     * applies the same canonicalization as the string constructor.
     */
    internal constructor(initPath: Path) {
        providedPath = initPath
        referencePath = initPath.absolutized()
    }

    /**
     * Check whether a path is absolute. Does not guarantee it is normalized.
     */
    public val isAbsolute: Boolean
        @JvmName("mppIsAbsolute")
        get() = providedPath.isAbsolute

    /**
     * Get the last segment of a path which by definition is the file name.
     */
    public val fileName: String
        get() = referencePath.name

    /**
     * Get the file size of this file in bytes.
     */
    public val fileSize: Long
        get() = metadata().size ?: 0L

    /**
     * Get the last modification date of this file as timestamp.
     */
    public val lastModified: DateTime
        get() = DateTime(metadata().lastModifiedAtMillis ?: 0L)

    /**
     * Get the parent object of the currently represented path. If it has no
     * parent, i.e. it is the root, it is returned itself.
     */
    public val parent: MPPPath
        get() = MPPPath(referencePath.parent ?: referencePath.root ?: referencePath)

    /**
     * Indicates whether the file exists.
     */
    public val exists: Boolean
        get() = resolveMetadataOrNull() != null

    /**
     * Indicates whether the file is a directory. If `false` it does not mean,
     * the file is a regular file.
     */
    public val isDirectory: Boolean
        get() = resolveMetadataOrNull()?.isDirectory == true

    /**
     * Indicates whether the file is a regular file. If `false` it does not
     * mean, the file is a directory.
     */
    public val isRegularFile: Boolean
        get() = resolveMetadataOrNull()?.isRegularFile == true

    /**
     * Checks whether the path starts with [p], comparing the provided
     * spellings: the check is lexical, so different spellings of the
     * same location do not match. Normalize both paths first when the
     * comparison should be location-based.
     */
    public fun startsWith(p: MPPPath): Boolean =
            providedPath.startsWith(p.providedPath)

    /**
     * Normalizes the path. For [MPPPath] this means, the path is transformed
     * into an absolute path and then normalized.
     */
    public fun normalize(): MPPPath =
            MPPPath(referencePath)

    /**
     * Resolve the child [p] of the current path.
     */
    public fun resolve(p: String): MPPPath =
        MPPPath(providedPath.resolve(platformSpelling(p)))

    /**
     * Resolve the child [p] of the current path.
     */
    public fun resolve(p: MPPPath): MPPPath =
        MPPPath(providedPath.resolve(p.providedPath))

    /**
     * Resolve the sibling [p] of the current path.
     */
    public fun resolveSibling(p: String): MPPPath =
        MPPPath(parent.resolve(p))

    /**
     * Resolve the sibling [p] of the current path.
     */
    public fun resolveSibling(p: MPPPath): MPPPath =
        MPPPath(parent.resolve(p))

    /**
     * Read lines from the file specified at the current path. Fails with an
     * [AraraIOException] exception if the file is a directory or access is
     * impossible.
     */
    @Throws(AraraIOException::class)
    public fun readLines(): List<String> =
            PathModelFileSystem.current.read(referencePath) {
                readUtf8().lines()
            }

    /**
     * Read whole text from the file specified at the current path. Fails with
     * an [AraraIOException] exception if the file is a directory or access is
     * impossible.
     */
    @Throws(AraraIOException::class)
    public fun readText(): String =
        if (isRegularFile)
            PathModelFileSystem.current.read(referencePath) { readUtf8() }
        else
            throw AraraIOException("Can only read text from files.")

    /**
     * Write [text] to the file specified at the current path. Overwrite by
     * default if [append] is not set. Fails with an [AraraIOException]
     * exception if the file is a directory or access is impossible.
     */
    @Throws(AraraIOException::class)
    public fun writeText(text: String, append: Boolean = false): Unit {
        if (isDirectory) throw AraraIOException("Can only write text to files.")
        if (append) {
            if (!exists) {
                throw AraraIOException("Can only append text to existing files.")
            }
            PathModelFileSystem.current.appendingSink(referencePath)
                .buffer().use { it.writeUtf8(text) }
        }
        else {
            PathModelFileSystem.current.write(referencePath) {
                writeUtf8(text)
            }
        }
    }

    override fun toString(): String =
        referencePath.toString()

    /**
     * The hash code is derived from the absolute, normalized reference
     * path, consistent with [equals].
     */
    override fun hashCode(): Int =
        referencePath.hashCode()

    /**
     * Two paths are equal if they denote the same location, that is, if
     * their absolute, normalized reference paths match. The provided
     * (raw) spellings are irrelevant: `MPPPath("a")` and the absolute
     * form of the same location are equal.
     */
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as MPPPath

        return (referencePath == other.referencePath)
    }

    /**
     * Resolve the child [p] against the current path.
     */
    public operator fun div(p: String): MPPPath = resolve(p)

    /**
     * Resolve the child [p] against the current path.
     */
    public operator fun div(p: MPPPath): MPPPath = resolve(p)

    /**
     * Treat this path as a directory identifier and create a directory at
     * this location including all missing parents.
     * Does nothing if a directory exists at the current location.
     *
     * Fails with an [AraraIOException] if the directory could not be created
     * or a regular file exists at the location.
     */
    @Throws(AraraIOException::class)
    public fun createDirectories() {
        if (!exists) {
            PathModelFileSystem.current.createDirectories(referencePath)
        } else {
            if (isRegularFile) {
                throw AraraIOException("Directory already exists as a file.")
            }
        }
    }

    /**
     * Treat this path as a directory identifier and remove it at
     * this location including its contents.
     * Does nothing if this directory does not exist at the current location.
     *
     * Fails with an [AraraIOException] if the directory could not be removed
     * or a regular file exists at the location.
     */
    @Throws(AraraIOException::class)
    public fun removeDirectory() {
        if (exists) {
            if (isDirectory) {
                PathModelFileSystem.current.deleteRecursively(referencePath)
            }
            else {
                throw AraraIOException("Target directory is a file.")
            }
        }
    }

    // the file system reads metadata without following symlinks and
    // reports the link target separately, while the established
    // semantics of this model follow links; chains are resolved
    // here, and a chain exceeding the limit counts as unresolvable,
    // matching how such loops surface as errors on the platform
    private fun resolveMetadataOrNull(): FileMetadata? {
        var path = referencePath
        repeat(SYMLINK_CHAIN_LIMIT) {
            val metadata = PathModelFileSystem.current.metadataOrNull(path)
            // a missing file or a terminal link ends the walk: the
            // metadata is null in the former case and final in the
            // latter, and both return through the same branch
            val target = metadata?.symlinkTarget ?: return metadata
            path = if (target.isAbsolute) target
            else path.parent?.let { parent -> parent / target } ?: target
        }
        return null
    }

    private fun metadata(): FileMetadata =
        resolveMetadataOrNull()
            ?: throw AraraIOException("The file could not be found.")
}

// the number of symlink hops the model follows before treating a
// chain as a loop; the platform reports such loops as errors
private const val SYMLINK_CHAIN_LIMIT: Int = 8

// the working directory is queried at construction time, matching
// the platform semantics: absolutization resolves against the
// process working directory at the moment of the call
private val workingDirectory: Path
    get() = System.getProperty("user.dir").toPath()

private fun Path.absolutized(): Path =
    (workingDirectory / this).normalized()

// a segment-wise prefix check: the roots must match and the other
// path's segments must form a prefix of this path's segments,
// matching the platform comparison semantics
private fun Path.startsWith(other: Path): Boolean {
    if (root != other.root) return false
    val prefix = other.segments
    val segments = segments
    // the roots match, so the other path is a prefix when its
    // segments form the head of this path's segments
    return prefix.size <= segments.size &&
        segments.subList(0, prefix.size) == prefix
}

// Windows accepts both separators in spellings, but the path
// algebra recognizes a Windows path by its first backslash, and the
// TeX toolchain itself reports forward slashes there;
// drive-prefixed and UNC spellings are re-spelled with backslashes
// before parsing on that platform so they keep their intended shape
private fun platformSpelling(path: String): String {
    if (!OS.isWindows) return path
    val drivePrefixed = path.length >= 2 && path[1] == ':' &&
        (path[0] in 'A'..'Z' || path[0] in 'a'..'z')
    val uncPrefixed = path.startsWith("//")
    return if (drivePrefixed || uncPrefixed) path.replace('/', '\\') else path
}

/**
 * Return a JVM [JVMPath] representation of this multiplatform path.
 */
public fun MPPPath.toJVMPath(): JVMPath =
        Paths.get(providedPath.toString())

/**
 * Return a JVM [File] representation of this multiplatform path.
 */
public fun MPPPath.toJVMFile(): File =
        File(providedPath.toString())

/**
 * Simple wrapper to transform a JVM [JVMPath] into a [MPPPath].
 */
public fun JVMPath.toMPPPath(): MPPPath =
        MPPPath(toString())

/**
 * Simple wrapper to transform a JVM [File] into a [MPPPath].
 */
public fun File.toMPPPath(): MPPPath =
        MPPPath(toString())
