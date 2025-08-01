// SPDX-License-Identifier: BSD-3-Clause
package org.islandoftex.arara.api.files

import org.islandoftex.arara.api.AraraIOException
import org.islandoftex.arara.api.time.DateTime
import java.io.File
import java.nio.file.Path
import java.nio.file.Paths
import kotlin.io.path.ExperimentalPathApi
import kotlin.io.path.Path
import kotlin.io.path.appendText
import kotlin.io.path.createDirectories
import kotlin.io.path.deleteRecursively
import kotlin.io.path.exists
import kotlin.io.path.fileSize
import kotlin.io.path.getLastModifiedTime
import kotlin.io.path.isDirectory
import kotlin.io.path.isRegularFile
import kotlin.io.path.name
import kotlin.io.path.readLines
import kotlin.io.path.readText
import kotlin.io.path.writeText
import kotlin.jvm.Throws

/**
 * Platform-independent object to deal with paths.
 */
public actual class MPPPath {
    internal val providedPath: Path
    internal val referencePath: Path

    /**
     * A constructor that accepts a string to be converted into a native path.
     */
    public actual constructor(path: String) {
        Paths.get(path).let {
            this.providedPath = it
            this.referencePath = it.toAbsolutePath().normalize()
        }
    }

    /**
     * Get the last segment of a path which by definition is the file name.
     */
    public constructor(initPath: MPPPath) {
        providedPath = initPath.providedPath
        referencePath = initPath.referencePath
    }

    /**
     * Internal constructor to ease handling JVM paths directly.
     * It should apply the same logic in the actual constructor
     * (absolute path + normalization).
     */
    internal constructor(initPath: Path) {
        providedPath = initPath
        referencePath = initPath.toAbsolutePath().normalize()
    }

    /**
     * Check whether a path is absolute. Does not guarantee it is normalized.
     */
    public actual val isAbsolute: Boolean
        @JvmName("mppIsAbsolute")
        get() = providedPath.isAbsolute

    /**
     * Get the last segment of a path which by definition is the file name.
     */
    public actual val fileName: String
        get() = referencePath.name

    /**
     * Get the file size of this file in bytes.
     */
    public actual val fileSize: Long
        get() = referencePath.fileSize()

    /**
     * Get the last modification date of this file as timestamp.
     */
    public actual val lastModified: DateTime
        get() = DateTime(referencePath.getLastModifiedTime().toMillis())

    /**
     * Get the parent object of the currently represented path. If it has no
     * parent, i.e. it is the root, it is returned itself.
     */
    public actual val parent: MPPPath
        get() = MPPPath(referencePath.parent ?: referencePath.root)

    /**
     * Indicates whether the file exists.
     */
    public actual val exists: Boolean
        get() = referencePath.exists()

    /**
     * Indicates whether the file is a directory. If `false` it does not mean,
     * the file is a regular file.
     */
    public actual val isDirectory: Boolean
        get() = referencePath.isDirectory()

    /**
     * Indicates whether the file is a regular file. If `false` it does not
     * mean, the file is a directory.
     */
    public actual val isRegularFile: Boolean
        get() = referencePath.isRegularFile()

    /**
     * Checks whether the path starts with [p].
     */
    public actual fun startsWith(p: MPPPath): Boolean =
            providedPath.startsWith(p.providedPath)

    /**
     * Normalizes the path. For [MPPPath] this means, the path is transformed
     * into an absolute path and then normalized.
     */
    public actual fun normalize(): MPPPath =
            MPPPath(providedPath.toAbsolutePath().normalize())

    /**
     * Resolve the child [p] of the current path.
     */
    public actual fun resolve(p: String): MPPPath =
        MPPPath(providedPath.resolve(p))

    /**
     * Resolve the child [p] of the current path.
     */
    public actual fun resolve(p: MPPPath): MPPPath =
        MPPPath(providedPath.resolve(p.providedPath))

    /**
     * Resolve the sibling [p] of the current path.
     */
    public actual fun resolveSibling(p: String): MPPPath =
        MPPPath(parent.resolve(p))

    /**
     * Resolve the sibling [p] of the current path.
     */
    public actual fun resolveSibling(p: MPPPath): MPPPath =
        MPPPath(parent.resolve(p))

    /**
     * Read lines from the file specified at the current path. Fails with an
     * [AraraIOException] exception if the file is a directory or access is
     * impossible.
     */
    @Throws(AraraIOException::class)
    public actual fun readLines(): List<String> =
            referencePath.readLines()

    /**
     * Read whole text from the file specified at the current path. Fails with
     * an [AraraIOException] exception if the file is a directory or access is
     * impossible.
     */
    @Throws(AraraIOException::class)
    public actual fun readText(): String =
        if (isRegularFile) referencePath.readText()
        else throw AraraIOException("Can only read text from files.")

    /**
     * Write [text] to the file specified at the current path. Overwrite by
     * default if [append] is not set. Fails with an [AraraIOException]
     * exception if the file is a directory or access is impossible.
     */
    @Throws(AraraIOException::class)
    public actual fun writeText(text: String, append: Boolean): Unit =
        if (!isDirectory) {
            if (append) {
                if (exists) {
                    referencePath.appendText(text)
                }
                else {
                    throw AraraIOException("Can only append text to existing files.")
                }
            }
            else {
                referencePath.writeText(text)
            }
        }
        else {
            throw AraraIOException("Can only write text to files.")
        }

    override fun toString(): String =
        referencePath.toString()

    override fun hashCode(): Int =
        referencePath.hashCode()

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as MPPPath

        return (referencePath == other.referencePath)
    }

    /**
     * Resolve the child [p] against the current path.
     */
    public actual operator fun div(p: String): MPPPath = resolve(p)

    /**
     * Resolve the child [p] against the current path.
     */
    public actual operator fun div(p: MPPPath): MPPPath = resolve(p)

    /**
     * Treat this path as a directory identifier and create a directory at
     * this location including all missing parents.
     * Does nothing if a directory exists at the current location.
     *
     * Fails with an [AraraIOException] if the directory could not be created
     * or a regular file exists at the location.
     */
    @Throws(AraraIOException::class)
    public actual fun createDirectories() {
        if (!exists) {
            referencePath.createDirectories()
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
     * As VfsFile::delete can only remove empty directories, a recursive
     * function call is made to clear the directory first.
     *
     * Fails with an [AraraIOException] if the directory could not be removed
     * or a regular file exists at the location.
     */
    @Throws(AraraIOException::class)
    @OptIn(ExperimentalPathApi::class)
    public actual fun removeDirectory() {
        if (exists) {
            if (isDirectory) {
                referencePath.deleteRecursively()
            }
            else {
                throw AraraIOException("Target directory is a file.")
            }
        }
    }

}

/**
 * Return a JVM [Path] representation of this multiplatform path.
 */
public fun MPPPath.toJVMPath(): Path =
        this.providedPath

/**
 * Return a JVM [File] representation of this multiplatform path.
 */
public fun MPPPath.toJVMFile(): File =
        this.providedPath.toFile()

/**
 * Simple wrapper to transform a JVM [Path] into a [MPPPath].
 */
public fun Path.toMPPPath(): MPPPath =
        MPPPath(this)

/**
 * Simple wrapper to transform a JVM [File] into a [MPPPath].
 */
public fun File.toMPPPath(): MPPPath =
        MPPPath(this.toPath())
