// SPDX-License-Identifier: BSD-3-Clause
package org.islandoftex.arara.api.files

import java.io.File
import java.io.IOException
import java.nio.charset.CodingErrorAction
import java.nio.file.FileSystemLoopException
import java.nio.file.Path
import java.nio.file.Paths
import kotlin.io.path.name
import kotlin.jvm.Throws
import okio.BufferedSource
import okio.FileMetadata
import okio.FileSystem
import okio.FileSystem.Companion.asOkioFileSystem
import okio.Path.Companion.toPath
import okio.buffer
import org.islandoftex.arara.api.AraraIOException
import org.islandoftex.arara.api.time.DateTime

/**
 * Platform-independent object to deal with paths.
 */
// since this should provide a fluent API to deal with paths,
// we are adding an annotation to suppress detekt's warning
// on too many functions
@Suppress("TooManyFunctions")
public actual class MPPPath {
    internal val providedPath: Path
    internal val referencePath: Path
    private val fileSystem: FileSystem

    /**
     * A constructor that accepts a string to be converted into a native path.
     */
    public actual constructor(path: String) : this(Paths.get(path))

    /**
     * Get the last segment of a path which by definition is the file name.
     */
    public constructor(initPath: MPPPath) {
        providedPath = initPath.providedPath
        referencePath = initPath.referencePath
        fileSystem = initPath.fileSystem
    }

    /**
     * Internal constructor to ease handling JVM paths directly.
     * It should apply the same logic in the actual constructor
     * (absolute path + normalization).
     */
    internal constructor(initPath: Path, fileSystem: FileSystem = initPath.fileSystem.asOkioFileSystem()) {
        this.fileSystem = fileSystem
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
        get() = MPPPath(referencePath.parent ?: referencePath.root, fileSystem)

    /**
     * Indicates whether the file exists.
     */
    public actual val exists: Boolean
        get() = probe { metadata(); true }

    /**
     * Indicates whether the file is a directory. If `false` it does not mean,
     * the file is a regular file.
     */
    public actual val isDirectory: Boolean
        get() = probe { metadata().isDirectory }

    /**
     * Indicates whether the file is a regular file. If `false` it does not
     * mean, the file is a directory.
     */
    public actual val isRegularFile: Boolean
        get() = probe { metadata().isRegularFile }

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
            MPPPath(referencePath, fileSystem)

    /**
     * Resolve the child [p] of the current path.
     */
    public actual fun resolve(p: String): MPPPath =
        MPPPath(providedPath.resolve(p), fileSystem)

    /**
     * Resolve the child [p] of the current path.
     */
    public actual fun resolve(p: MPPPath): MPPPath =
        MPPPath(providedPath.resolve(p.providedPath), fileSystem)

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
    public actual fun readLines(): List<String> {
        if (exists && !isRegularFile) throw AraraIOException("Can only read lines from files.")
        return read { source ->
            val decoder = Charsets.UTF_8.newDecoder().onMalformedInput(CodingErrorAction.REPORT)
                .onUnmappableCharacter(CodingErrorAction.REPORT)
            java.io.InputStreamReader(source.inputStream(), decoder).buffered().readLines()
        }
    }

    /**
     * Read whole text from the file specified at the current path. Fails with
     * an [AraraIOException] exception if the file is a directory or access is
     * impossible.
     */
    @Throws(AraraIOException::class)
    public actual fun readText(): String {
        if (exists && !isRegularFile) throw AraraIOException("Can only read text from files.")
        return read { source -> source.inputStream().reader(Charsets.UTF_8).readText() }
    }

    /**
     * Write [text] to the file specified at the current path. Overwrite by
     * default if [append] is not set. Fails with an [AraraIOException]
     * exception if the file is a directory or access is impossible.
     */
    @Throws(AraraIOException::class)
    public actual fun writeText(text: String, append: Boolean): Unit = io {
        if (isDirectory) throw AraraIOException("Can only write text to files.")
        if (append && !exists) throw AraraIOException("Can only append text to existing files.")

        val sink = if (append) fileSystem.appendingSink(toOkioPath(), mustExist = true)
            else fileSystem.sink(toOkioPath())
        sink.buffer().use { it.write(text.toByteArray(Charsets.UTF_8)) }
    }

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
    public actual fun createDirectories(): Unit = io {
        if (!exists) {
            if (fileSystem.metadataOrNull(toOkioPath())?.symlinkTarget != null) {
                throw IOException("Cannot create a directory at an unresolved link: $this")
            }
            fileSystem.createDirectories(toOkioPath())
        } else if (isRegularFile) throw AraraIOException("Directory already exists as a file.")
    }

    /**
     * Treat this path as a directory identifier and remove it at
     * this location including its contents.
     * Does nothing if this directory does not exist at the current location.
     * Directory links are removed without deleting their targets.
     *
     * Fails with an [AraraIOException] if the directory could not be removed
     * or a regular file exists at the location.
     */
    @Throws(AraraIOException::class)
    public actual fun removeDirectory(): Unit = io {
        if (exists) {
            if (!isDirectory) throw AraraIOException("Target directory is a file.")
            fileSystem.deleteRecursively(toOkioPath())
        }
    }

    /**
     * Absolute normalized backend path; never exports the provided spelling.
     */
    public actual fun toOkioPath(): okio.Path = referencePath.toString().toPath()

    /**
     * Strict lookup: missing files, cycles and inspection failures raise [AraraIOException].
     */
    @Throws(AraraIOException::class)
    public actual fun metadata(): PathMetadata = io {
        val metadata = resolvedMetadata().second
        PathMetadata(
            metadata.isRegularFile,
            metadata.isDirectory,
            metadata.size,
            metadata.lastModifiedAtMillis?.let { DateTime(it) }
        )
    }

    /**
     * The source belongs to this call and is closed after [block] returns.
     */
    @Throws(AraraIOException::class)
    public actual fun <T> read(block: (BufferedSource) -> T): T = io {
        val (resolved, metadata) = resolvedMetadata()
        if (!metadata.isRegularFile) throw AraraIOException("Can only read from files: $this")
        fileSystem.source(resolved).buffer().use(block)
    }

    /**
     * Direct children from this backend, including directories and special files.
     */
    @Throws(AraraIOException::class)
    public actual fun listEntries(): List<MPPPath> = io {
        fileSystem.list(toOkioPath()).map { MPPPath(referencePath.fileSystem.getPath(it.toString()), fileSystem) }
    }

    /**
     * Follow directory links, detecting ancestor cycles without suppressing sibling aliases.
     */
    @Throws(AraraIOException::class)
    public actual fun listFiles(recursive: Boolean): List<MPPPath> = io {
        val files = mutableListOf<MPPPath>()
        val ancestors = mutableSetOf<okio.Path>()

        fun visit(path: MPPPath, descend: Boolean) {
            val (resolved, metadata) = path.resolvedMetadata()
            if (metadata.isRegularFile) {
                files.add(path)
            } else if (metadata.isDirectory && descend) {
                val canonical = fileSystem.canonicalize(resolved)
                if (!ancestors.add(canonical)) throw FileSystemLoopException(path.toString())
                try {
                    fileSystem.list(path.toOkioPath()).forEach { child ->
                        try {
                            visit(MPPPath(referencePath.fileSystem.getPath(child.toString()), fileSystem), recursive)
                        } catch (_: java.io.FileNotFoundException) {
                            // Broken links and entries removed during traversal are not regular files.
                        }
                    }
                } finally {
                    ancestors.remove(canonical)
                }
            }
        }

        visit(this, true)
        files.sortedBy { it.toString() }
    }

    private fun resolvedMetadata(): Pair<okio.Path, FileMetadata> {
        var path = toOkioPath()
        val visited = mutableSetOf<okio.Path>()
        while (true) {
            if (!visited.add(path)) throw FileSystemLoopException(path.toString())
            val metadata = fileSystem.metadata(path)
            val target = metadata.symlinkTarget ?: return path to metadata
            path = (if (target.isAbsolute) target else path.parent!!.resolve(target)).normalized()
        }
    }

    private inline fun probe(block: () -> Boolean): Boolean = try {
        block()
    } catch (_: AraraIOException) {
        false
    } catch (_: SecurityException) {
        false
    }

    private inline fun <T> io(operation: () -> T): T = try {
        operation()
    } catch (exception: IOException) {
        throw AraraIOException(exception.toString(), exception)
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
