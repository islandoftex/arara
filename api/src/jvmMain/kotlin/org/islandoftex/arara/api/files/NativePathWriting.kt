// SPDX-License-Identifier: BSD-3-Clause
package org.islandoftex.arara.api.files

import java.io.IOException
import kotlin.jvm.Throws
import okio.buffer
import org.islandoftex.arara.api.AraraIOException

/** Native implementation of the focused PathWriting contract. */
internal class NativePathWriting(private val identity: NativePathIdentity) : PathWriting {
    /**
     * Write [text] to the file specified at the current path. Overwrite by
     * default if [append] is not set. Fails with an [AraraIOException]
     * exception if the file is a directory or access is impossible.
     */
    @Throws(AraraIOException::class)
    override fun writeText(text: String, append: Boolean): Unit = identity.io {
        if (identity.isDirectory) throw AraraIOException("Can only write text to files.")
        if (append && !identity.exists) throw AraraIOException("Can only append text to existing files.")

        val sink = if (append) identity.fileSystem.appendingSink(identity.toOkioPath(), mustExist = true)
        else identity.fileSystem.sink(identity.toOkioPath())
        sink.buffer().use { it.write(text.toByteArray(Charsets.UTF_8)) }
    }

    /**
     * Treat this path as a directory identifier and create a directory at
     * this location including all missing parents.
     * Does nothing if a directory exists at the current location.
     *
     * Fails with an [AraraIOException] if the directory could not be created
     * or a regular file exists at the location.
     */
    @Throws(AraraIOException::class)
    override fun createDirectories(): Unit = identity.io {
        if (!identity.exists) {
            if (identity.fileSystem.metadataOrNull(identity.toOkioPath())?.symlinkTarget != null) {
                throw IOException("Cannot create a directory at an unresolved link: ${identity.referencePath}")
            }
            identity.fileSystem.createDirectories(identity.toOkioPath())
        } else if (identity.isRegularFile) throw AraraIOException("Directory already exists as a file.")
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
    override fun removeDirectory(): Unit = identity.io {
        if (identity.exists) {
            if (!identity.isDirectory) throw AraraIOException("Target directory is a file.")
            identity.fileSystem.deleteRecursively(identity.toOkioPath())
        }
    }
}
