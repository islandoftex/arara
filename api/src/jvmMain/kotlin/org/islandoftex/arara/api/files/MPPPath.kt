// SPDX-License-Identifier: BSD-3-Clause
package org.islandoftex.arara.api.files

import com.soywiz.klock.DateTime
import com.soywiz.korio.async.runBlockingNoJs
import com.soywiz.korio.file.VfsFile
import com.soywiz.korio.file.baseName
import com.soywiz.korio.file.std.localVfs
import java.io.File
import java.nio.file.Path
import java.nio.file.Paths
import org.islandoftex.arara.api.AraraIOException

/**
 * Platform-independent object to deal with paths.
 */
public actual class MPPPath {
    internal val path: Path
    internal val vfsFile: VfsFile

    /**
     * A constructor that accepts a string to be converted into a native path.
     */
    public actual constructor(path: String) {
        this.path = Paths.get(path)
        vfsFile = localVfs(this.path.toAbsolutePath().normalize().toString())
    }

    /**
     * Get the last segment of a path which by definition is the file name.
     */
    public constructor(initPath: MPPPath) {
        path = initPath.path
        vfsFile = initPath.vfsFile
    }

    /**
     * Check whether a path is absolute. Does not guarantee it is normalized.
     */
    public actual val isAbsolute: Boolean
        @JvmName("mppIsAbsolute")
        get() = path.isAbsolute

    /**
     * Get the last segment of a path which by definition is the file name.
     */
    public actual val fileName: String
        get() = vfsFile.baseName

    /**
     * Get the file size of this file in bytes.
     */
    public actual val fileSize: Long
        get() = runBlockingNoJs {
            vfsFile.size()
        }

    /**
     * Get the last modification date of this file as timestamp.
     */
    public actual val lastModified: DateTime
        get() = runBlockingNoJs {
            vfsFile.stat().modifiedTime
        }

    /**
     * Get the parent object of the currently represented path. If it has no
     * parent, i.e. it is the root, it is returned itself.
     */
    public actual val parent: MPPPath
        get() = this.takeIf { path == path.root } ?: MPPPath(path.parent.toString())

    /**
     * Indicates whether the file exists.
     */
    public actual val exists: Boolean
        get() = runBlockingNoJs { vfsFile.exists() }

    /**
     * Indicates whether the file is a directory. If `false` it does not mean,
     * the file is a regular file.
     */
    public actual val isDirectory: Boolean
        get() = runBlockingNoJs { vfsFile.isDirectory() }

    /**
     * Indicates whether the file is a regular file. If `false` it does not
     * mean, the file is a directory.
     */
    public actual val isRegularFile: Boolean
        get() = runBlockingNoJs { vfsFile.isFile() }

    /**
     * Checks whether the path starts with [p].
     */
    public actual fun startsWith(p: MPPPath): Boolean =
        path.startsWith(p.path)

    /**
     * Checks whether the path ends with [p].
     */
    public actual fun endsWith(p: MPPPath): Boolean =
        path.endsWith(p.path)

    /**
     * Normalizes the path. For [MPPPath] this means, the path is transformed
     * into an absolute path and then normalized.
     */
    public actual fun normalize(): MPPPath =
            MPPPath(path.toAbsolutePath().normalize().toString())

    /**
     * Resolve the child [p] of the current path.
     */
    public actual fun resolve(p: String): MPPPath =
            MPPPath(vfsFile[p].absolutePath)

    /**
     * Resolve the child [p] of the current path.
     */
    public actual fun resolve(p: MPPPath): MPPPath =
            MPPPath(vfsFile[p.path.toString()].absolutePath)

    /**
     * Resolve the sibling [p] of the current path.
     */
    public actual fun resolveSibling(p: String): MPPPath =
            MPPPath(vfsFile.parent[p].absolutePath)

    /**
     * Resolve the sibling [p] of the current path.
     */
    public actual fun resolveSibling(p: MPPPath): MPPPath =
            MPPPath(vfsFile.parent[p.vfsFile.path].absolutePath)

    /**
     * Read lines from the file specified at the current path. Fails with an
     * [AraraIOException] exception if the file is a directory or access is
     * impossible.
     */
    public actual fun readLines(): List<String> = runBlockingNoJs {
        if (isRegularFile)
            vfsFile.readLines().toList()
        else
            throw AraraIOException("Can only read lines from files.")
    }

    /**
     * Read whole text from the file specified at the current path. Fails with
     * an [AraraIOException] exception if the file is a directory or access is
     * impossible.
     */
    public actual fun readText(): String = runBlockingNoJs {
        if (isRegularFile)
            vfsFile.readString()
        else
            throw AraraIOException("Can only read text from files.")
    }

    /**
     * Write [text] to the file specified at the current path. Fails with an
     * [AraraIOException] exception if the file is a directory or access is
     * impossible.
     */
    public actual fun writeText(text: String): Unit = runBlockingNoJs {
        if (isRegularFile)
            vfsFile.writeString(text)
        else
            throw AraraIOException("Can only write text to files.")
    }

    override fun toString(): String = vfsFile.absolutePath

    override fun hashCode(): Int = vfsFile.absolutePath.hashCode()

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as MPPPath

        if (vfsFile.absolutePath != vfsFile.absolutePath) return false

        return true
    }

    /**
     * Resolve the child [p] against the current path.
     */
    public actual operator fun div(p: String): MPPPath = resolve(p)

    /**
     * Resolve the child [p] against the current path.
     */
    public actual operator fun div(p: MPPPath): MPPPath = resolve(p)
}

/**
 * Return a JVM [Path] representation of this multiplatform path.
 */
public fun MPPPath.toJVMPath(): Path = Paths.get(this.vfsFile.absolutePath)

/**
 * Return a JVM [File] representation of this multiplatform path.
 */
public fun MPPPath.toJVMFile(): File = File(this.vfsFile.absolutePath)

/**
 * Simple wrapper to transform a JVM [Path] into a [MPPPath].
 */
public fun Path.toMPPPath(): MPPPath = MPPPath(this.toString())

/**
 * Simple wrapper to transform a JVM [File] into a [MPPPath].
 */
public fun File.toMPPPath(): MPPPath = MPPPath(this.toString())
