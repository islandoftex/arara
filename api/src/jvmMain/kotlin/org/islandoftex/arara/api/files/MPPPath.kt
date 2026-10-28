// SPDX-License-Identifier: BSD-3-Clause
package org.islandoftex.arara.api.files

import java.io.File
import java.nio.file.Path
import java.nio.file.Paths
import kotlin.io.path.name
import okio.BufferedSource
import okio.FileSystem
import okio.FileSystem.Companion.asOkioFileSystem
import okio.Path.Companion.toPath
import org.islandoftex.arara.api.AraraIOException
import org.islandoftex.arara.api.time.DateTime

/** Native path identity with independently composed algebra, reads and writes. */
// Explicit I/O forwards preserve checked exceptions for Java callers; implementations live in the capabilities.
@Suppress("TooManyFunctions")
public actual class MPPPath private constructor(private val identity: NativePathIdentity) :
    PathAlgebra by NativePathAlgebra(identity),
    PathReading, PathWriting {
    private val reading = NativePathReading(identity)
    private val writing = NativePathWriting(identity)

    @Throws(AraraIOException::class)
    override fun readLines(): List<String> = reading.readLines()

    @Throws(AraraIOException::class)
    override fun readText(): String = reading.readText()

    override fun toOkioPath(): okio.Path = reading.toOkioPath()

    @Throws(AraraIOException::class)
    override fun metadata(): PathMetadata = reading.metadata()

    @Throws(AraraIOException::class)
    override fun <T> read(block: (BufferedSource) -> T): T = reading.read(block)

    @Throws(AraraIOException::class)
    override fun listEntries(): List<MPPPath> = reading.listEntries()

    @Throws(AraraIOException::class)
    override fun listFiles(recursive: Boolean): List<MPPPath> = reading.listFiles(recursive)

    @Throws(AraraIOException::class)
    override fun writeText(text: String, append: Boolean): Unit = writing.writeText(text, append)

    @Throws(AraraIOException::class)
    override fun createDirectories(): Unit = writing.createDirectories()

    @Throws(AraraIOException::class)
    override fun removeDirectory(): Unit = writing.removeDirectory()

    internal val providedPath: Path get() = identity.providedPath
    internal val referencePath: Path get() = identity.referencePath

    /** Construct a native path retaining its provided spelling. */
    public actual constructor(path: String) : this(Paths.get(path), suppliedSpelling = path)

    /** Copy path identity and retain its filesystem backend. */
    public constructor(initPath: MPPPath) : this(initPath.identity)

    internal constructor(
        initPath: Path,
        fileSystem: FileSystem = initPath.fileSystem.asOkioFileSystem(),
        suppliedSpelling: String = initPath.toString()
    ) : this(NativePathIdentity(initPath, fileSystem, suppliedSpelling))

    /** Supplied text retained separately from native path parsing and normalized exports. */
    public actual val suppliedSpelling: String get() = identity.suppliedSpelling

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
        get() = metadata().size ?: throw AraraIOException("File size is unavailable: $this")

    /**
     * Get the last modification date of this file as timestamp.
     */
    public actual val lastModified: DateTime
        get() = metadata().lastModified ?: throw AraraIOException("Modification time is unavailable: $this")

    /**
     * Get the parent object of the currently represented path. If it has no
     * parent, i.e. it is the root, it is returned itself.
     */
    public actual val parent: MPPPath
        get() = MPPPath(referencePath.parent ?: referencePath.root, identity.fileSystem)

    /**
     * Indicates whether the file exists.
     */
    public actual val exists: Boolean
        get() = identity.probe { metadata(); true }

    /**
     * Indicates whether the file is a directory. If `false` it does not mean,
     * the file is a regular file.
     */
    public actual val isDirectory: Boolean
        get() = identity.probe { metadata().isDirectory }

    /**
     * Indicates whether the file is a regular file. If `false` it does not
     * mean, the file is a directory.
     */
    public actual val isRegularFile: Boolean
        get() = identity.probe { metadata().isRegularFile }

    /**
     * The platform path spelling, without resolving symbolic links.
     */
    override fun toString(): String =
        referencePath.toString()

    /**
     * Hash of the underlying platform path.
     */
    override fun hashCode(): Int =
        referencePath.hashCode()

    /**
     * Lexical platform path equality; does not compare file contents or resolve symbolic links.
     */
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as MPPPath

        return (referencePath == other.referencePath)
    }
}

/**
 * Return the absolute, normalized JVM [Path] of this multiplatform path.
 */
public fun MPPPath.toJVMPath(): Path =
    this.referencePath

/**
 * Return the absolute, normalized JVM [File] of this multiplatform path.
 */
public fun MPPPath.toJVMFile(): File =
    this.referencePath.toFile()

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
