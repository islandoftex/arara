// SPDX-License-Identifier: BSD-3-Clause
package org.islandoftex.arara.api.files

//import korlibs.io.async.runBlockingNoJs
//import korlibs.io.async.use
//import korlibs.io.file.File_separatorChar
//import korlibs.io.file.VfsFile
//import korlibs.io.file.VfsOpenMode
//import korlibs.io.file.baseName
//import korlibs.io.file.fullPathNormalized
//import korlibs.io.file.getPathComponents
//import korlibs.io.file.normalize
//import korlibs.io.file.std.localVfs
//import korlibs.io.lang.lastIndexOfOrNull
//import korlibs.io.stream.copyTo
//import korlibs.io.stream.openAsync
import org.islandoftex.arara.api.AraraIOException
//import org.islandoftex.arara.api.utils.OS.isWindows
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
    // ---------- KLPN ----------
    // Replace local VFS file by just a JVM Path
    internal val providedPath: Path
    internal val referencePath: Path

// KLPN-remove
//internal val originalPath: Path
//internal val vfsFile: VfsFile

    /**
     * A constructor that accepts a string to be converted into a native path.
     */
    public actual constructor(path: String) {
        // ---------- KLPN ----------
        // The original MPPPath implementation always
        // have the local VFS get an absolute + normalized
        // reference of the provided path, so we will reproduce
        // the behaviour here by
        // 1. converting current path to an absolute path
        // 2. normalizing the provided path
        Paths.get(path).let {
            this.providedPath = it
            this.referencePath = it.toAbsolutePath().normalize()
        }

// KLPN-remove
//        this.originalPath = Paths.get(adjustRoot(path))
//        vfsFile = localVfs(this.originalPath.toAbsolutePath().normalize().toString())
    }

    /**
     * Get the last segment of a path which by definition is the file name.
     */
    public constructor(initPath: MPPPath) {
        // ---------- KLPN ----------
        // reference the provided path
        providedPath = initPath.providedPath
        referencePath = initPath.referencePath

// KLPN-remove
//        originalPath = initPath.originalPath
//        vfsFile = initPath.vfsFile
    }

    /**
     * Internal constructor to ease handling JVM paths directly.
     * It should apply the same logic in the actual constructor
     * (absolute path + normalization).
     */
    internal constructor(initPath: Path) {
        // ---------- KLPN ----------
        // mimic the behaviour applied to the
        // original local VFS reference
        providedPath = initPath
        referencePath = initPath.toAbsolutePath().normalize()

// KLPN-remove
//        originalPath = initPath
//        vfsFile = localVfs(initPath.toAbsolutePath().normalize().toString())
    }

    /**
     * Check whether a path is absolute. Does not guarantee it is normalized.
     */
    public actual val isAbsolute: Boolean
        @JvmName("mppIsAbsolute")
        // ---------- KLPN ----------
        // Simply return what Path says
        get() = providedPath.isAbsolute

        // KLPN-remove
        // get() = originalPath.isAbsolute

    /**
     * Get the last segment of a path which by definition is the file name.
     */
    public actual val fileName: String
        // ---------- KLPN ----------
        // Simply return what Path says
        get() = referencePath.name

        // KLPN-remove
        // get() = vfsFile.baseName

    /**
     * Get the file size of this file in bytes.
     */
    public actual val fileSize: Long
        // ---------- KLPN ----------
        // Simply return what Path says
        get() = referencePath.fileSize()

        // KLPN-remove
        // get() = runBlockingNoJs {
        //     vfsFile.size()
        // }

    /**
     * Get the last modification date of this file as long.
     */
    public actual val lastModified: Long
        // ---------- KLPN ----------
        // Get the reference as FileTime object and convert it
        // to milliseconds (represented as long)
        get() = referencePath.getLastModifiedTime().toMillis()

        // KLPN-remove
        // get() = runBlockingNoJs {
        //    vfsFile.stat().modifiedTime.unixMillisLong
        // }

    /**
     * Get the parent object of the currently represented path. If it has no
     * parent, i.e. it is the root, it is returned itself.
     */
    public actual val parent: MPPPath
        // ---------- KLPN ----------
        // Path.parent can be null in case the user wants to
        // go beyond root, so if that happens, just get the
        // root element (path.toAbsolutePath().root would
        // also be an idea, but the path is already converted +
        // normalized when the MPPPath object is instantiated)
        get() = MPPPath(referencePath.parent ?: referencePath.root)

        // KLPN-remove
        // get() = this.takeIf { vfsFile == vfsFile.root }
        //     ?: vfsFile.fullPathNormalized
        //             .substring(0, vfsFile.fullPathNormalized.lastIndexOfOrNull('/') ?: 0)
        //             .takeIf { it.isNotBlank() }
        //             ?.let { MPPPath(it) }
        //         // prevent fallback to current working directory as korio
        //         // would do; we fall back to the file system root
        //         // TODO: check behavior for relative paths
        //         ?: MPPPath("/")

    /**
     * Indicates whether the file exists.
     */
    public actual val exists: Boolean
        // ---------- KLPN ----------
        // Simply return what Path says
        get() = referencePath.exists()

        // KLPN-remove
        // get() = runBlockingNoJs { vfsFile.exists() }

    /**
     * Indicates whether the file is a directory. If `false` it does not mean,
     * the file is a regular file.
     */
    public actual val isDirectory: Boolean
        // ---------- KLPN ----------
        // Simply return what Path says
        get() = referencePath.isDirectory()

        // KLPN-remove
        // get() = runBlockingNoJs { vfsFile.isDirectory() }

    /**
     * Indicates whether the file is a regular file. If `false` it does not
     * mean, the file is a directory.
     */
    public actual val isRegularFile: Boolean
        // ---------- KLPN ----------
        // Simply return what Path says
        get() = referencePath.isRegularFile()

        // KLPN-remove
        // get() = runBlockingNoJs { vfsFile.isFile() }

    /**
     * Checks whether the path starts with [p].
     */
    public actual fun startsWith(p: MPPPath): Boolean =
            // ---------- KLPN ----------
            // Simply return what Path says
            providedPath.startsWith(p.providedPath)

// KLPN-remove
//    {
//        val components = vfsFile.getPathComponents()
//        val otherComponents = p.vfsFile.getPathComponents()
//
//        return if (otherComponents.size > components.size ||
//            otherComponents.isEmpty() && isAbsolute
//        ) {
//            // other path is longer or has no name elements
//            false
//        } else {
//            // check component-wise
//            otherComponents.forEachIndexed { i, element ->
//                if (element != components[i])
//                    return false
//            }
//            true
//        }
//    }

    /**
     * Normalizes the path. For [MPPPath] this means, the path is transformed
     * into an absolute path and then normalized.
     */
    public actual fun normalize(): MPPPath =
            // ---------- KLPN ----------
            // Simply apply the Path method
            MPPPath(providedPath.toAbsolutePath().normalize())

        // KLPN-remove
        // // TODO: align with parent, check relative paths
        // MPPPath(
        //     vfsFile.absolutePathInfo.normalize()
        //         .takeIf { it.isNotBlank() } ?: "/"
        // )

    /**
     * Resolve the child [p] of the current path.
     */
    public actual fun resolve(p: String): MPPPath =
        // ---------- KLPN ----------
        // Simply return what Path says
        MPPPath(providedPath.resolve(p))

        // KLPN-remove
        // MPPPath(vfsFile[p].absolutePath)

    /**
     * Resolve the child [p] of the current path.
     */
    public actual fun resolve(p: MPPPath): MPPPath =
        // ---------- KLPN ----------
        // Simply return what Path says
        MPPPath(providedPath.resolve(p.providedPath))

        // KLPN-remove
        // MPPPath(vfsFile[p.originalPath.toString()].absolutePath)

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
            // ---------- KLPN ----------
            // Simply forward the request to Path
            providedPath.readLines()

// KLPN-remove
//            runBlockingNoJs {
//        if (isRegularFile)
//            vfsFile.readLines().toList()
//        else
//            throw AraraIOException("Can only read lines from files.")
//    }

    /**
     * Read whole text from the file specified at the current path. Fails with
     * an [AraraIOException] exception if the file is a directory or access is
     * impossible.
     */
    @Throws(AraraIOException::class)
    public actual fun readText(): String =
        // ---------- KLPN ----------
        // reference has to exist and be an actual
        // file; if so, forward request to Path,
        // otherwise throw an exception
        if (isRegularFile) providedPath.readText()
        else throw AraraIOException("Can only read text from files.")

// KLPN-remove
//            runBlockingNoJs {
//        if (isRegularFile)
//            vfsFile.readString()
//        else
//            throw AraraIOException("Can only read text from files.")
//    }

    /**
     * Write [text] to the file specified at the current path. Overwrite by
     * default if [append] is not set. Fails with an [AraraIOException]
     * exception if the file is a directory or access is impossible.
     */
    @Throws(AraraIOException::class)
    public actual fun writeText(text: String, append: Boolean): Unit =
        // ---------- KLPN ----------
        // Since this method can write text to files that do not exist to
        // this point, and that Path will return false if isRegularFile()
        // is checked against a non-existing file, it's better to check
        // if the reference is not a directory, then apply the logic. Also,
        // we do an additional check when appending text (it would not make
        // sense to append a text to a non-existing file, but this might
        // be a breaking change).
        if (!isDirectory) {
            if (append) {
                if (exists) {
                    providedPath.appendText(text)
                }
                else {
                    throw AraraIOException("Can only append text to existing files.")
                }
            }
            else {
                providedPath.writeText(text)
            }
        }
        else {
            throw AraraIOException("Can only write text to files.")
        }

// KLPN-remove
//        runBlockingNoJs {
//            if (isRegularFile) {
//                val openMode = if (append)
//                    VfsOpenMode.APPEND
//                else
//                // if not appending choose the same open mode
//                // korio would use instead
//                    VfsOpenMode.CREATE_OR_TRUNCATE
//                vfsFile.vfs.open(vfsFile.absolutePath, openMode)
//                    .use {
//                        text.openAsync().copyTo(this)
//                    }
//            } else {
//                throw AraraIOException("Can only write text to files.")
//            }
//        }

    override fun toString(): String =
        // ---------- KLPN ----------
        // forward request to Path
        referencePath.toString()

        // KLPN-remove
        // vfsFile.absolutePath.replace('/', File_separatorChar)

    override fun hashCode(): Int =
        // ---------- KLPN ----------
        referencePath.hashCode()

        // KLPN-remove
        // vfsFile.absolutePath.hashCode()

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as MPPPath

        // ---------- KLPN ----------
        // check if paths match (original code has an
        // absolute path check, but this is not needed
        // since this already happens when MPPPath is
        // instantiated)
//        TODO return
//        return (path == other.path)
        return (referencePath == other.referencePath)

// KLPN-remove
//        if (vfsFile.absolutePath != other.vfsFile.absolutePath) return false
//
//        return true
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
        // ---------- KLPN ----------
        // The logic is close to the original; just the last
        // check is now done on isRegularFile() instead of
        // !isDirectory() just to be safe for Path
        if (!exists) {
            referencePath.createDirectories()
        } else {
            if (isRegularFile) {
                throw AraraIOException("Directory already exists as a file.")
            }
        }
    }

// KLPN-remove
//            runBlockingNoJs {
//        if (!vfsFile.exists()) {
//            vfsFile.mkdirs()
//        } else if (!vfsFile.isDirectory()) {
//            throw AraraIOException("Directory already exists as a file.")
//        }
//    }

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
        // ---------- KLPN ----------
        // Logic is simplified because Path has a method
        // that recursively deletes everything
        if (exists) {
            if (isDirectory) {
                referencePath.deleteRecursively()
            }
            else {
                throw AraraIOException("Target directory is a file.")
            }
        }
    }

// KLPN-remove
//        fun removePotentiallyNonEmptyDir(file: VfsFile) {
//            runBlockingNoJs {
//                if (file.exists()) {
//                    if (file.isDirectory()) {
//                        for (child in file.listSimple()) {
//                            removePotentiallyNonEmptyDir(child)
//                        }
//                    }
//                    file.delete()
//                }
//            }
//        }
//        runBlockingNoJs {
//            if (vfsFile.exists()) {
//                if (vfsFile.isDirectory()) {
//                    removePotentiallyNonEmptyDir(vfsFile)
//                } else {
//                    throw AraraIOException("Target directory is a file.")
//                }
//            }
//        }
//    }

// KLPN-remove
//    /**
//     * Potentially adjusts root path in Windows to ensure correct resolution.
//     * See [issue #128](https://gitlab.com/islandoftex/arara/-/issues/128)
//     * for more details.
//     */
//    private fun adjustRoot(path: String): String =
//            path.takeIf { !(isWindows && it.length == 2
//                    && it[0].isLetter() && it[1] == ':') } ?: "${path}/"

}

/**
 * Return a JVM [Path] representation of this multiplatform path.
 */
public fun MPPPath.toJVMPath(): Path =
        // ---------- KLPN ----------
        // Simply return Path itself
        this.providedPath

        // KLPN-remove
        // Paths.get(this.vfsFile.absolutePath)

/**
 * Return a JVM [File] representation of this multiplatform path.
 */
public fun MPPPath.toJVMFile(): File =
        // ---------- KLPN ----------
        // Simply return the File reference from Path
        this.providedPath.toFile()

        // KLPN-remove
        // File(this.vfsFile.absolutePath)

/**
 * Simple wrapper to transform a JVM [Path] into a [MPPPath].
 */
public fun Path.toMPPPath(): MPPPath =
        // ---------- KLPN ----------
        // Simply wrap Path in the constructor
        MPPPath(this)

        // KLPN-remove
        // MPPPath(this.toString())

/**
 * Simple wrapper to transform a JVM [File] into a [MPPPath].
 */
public fun File.toMPPPath(): MPPPath =
        // ---------- KLPN ----------
        // Simply wrap Path in the constructor
        MPPPath(this.toPath())

        // KLPN-remove
        // MPPPath(this.toString())
