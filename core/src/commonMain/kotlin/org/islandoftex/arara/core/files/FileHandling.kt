// SPDX-License-Identifier: BSD-3-Clause
package org.islandoftex.arara.core.files

import com.soywiz.korio.async.runBlockingNoJs
import com.soywiz.korio.file.std.localVfs
import com.soywiz.korio.lang.IOException
import com.soywiz.korio.util.checksum.CRC32
import com.soywiz.korio.util.checksum.checksum
import org.islandoftex.arara.api.AraraException
import org.islandoftex.arara.api.files.MPPPath
import org.islandoftex.arara.core.localization.LanguageController

object FileHandling {
    /**
     * Resolve the sibling of a file with just its extension changed.
     *
     * @param path The path to use as base.
     * @param extension The extension.
     * @return The full file path to the sibling.
     */
    fun changeExtension(path: MPPPath, extension: String): MPPPath {
        val name = path.fileName.substringBeforeLast('.') +
            ".$extension"
        return path.resolveSibling(name).normalize()
    }

    /**
     * Checks whether a directory is under a root directory.
     *
     * @param child Directory to be inspected.
     * @param parent Root directory.
     * @return Logical value indicating whether the directoy is under root.
     * @throws AraraException There was a problem with path retrieval.
     */
    @Throws(AraraException::class)
    fun isSubDirectory(child: MPPPath, parent: MPPPath): Boolean {
        return if (child.isDirectory && parent.isDirectory) {
            child.normalize().startsWith(parent.normalize())
        } else {
            false
        }
    }

    /**
     * Calculates the CRC32 checksum of the provided file.
     *
     * @param path The file.
     * @return The CRC32 checksum of the provided file.
     * @throws AraraException Something wrong happened, to be caught in the
     *   higher levels.
     */
    @Throws(AraraException::class)
    fun calculateHash(path: MPPPath): Long =
        try {
            runBlockingNoJs {
                localVfs(path.normalize().toString())
                    .readBytes()
            }.checksum(CRC32).toUInt().toLong()
        } catch (exception: IOException) {
            throw AraraException(
                LanguageController.messages.ERROR_CALCULATEHASH_IO_EXCEPTION,
                exception
            )
        }

    /**
     * Checks if a file has changed since the last verification.
     *
     * @param file The file.
     * @param databaseFile The database to use for checking for changes.
     * @return A boolean value indicating if the file has changed since the
     *   last verification.
     */
    fun hasChanged(file: MPPPath, databaseFile: MPPPath): Boolean {
        val database = Database.load(databaseFile)
        val path = file.normalize()
        return if (!path.exists) {
            if (path in database) {
                database.remove(path)
                database.save(databaseFile)
                true
            } else {
                false
            }
        } else {
            val hash = calculateHash(path)
            if (path in database) {
                val value = database[path]
                if (hash == value) {
                    false
                } else {
                    database[path] = hash
                    database.save(databaseFile)
                    true
                }
            } else {
                database[path] = hash
                database.save(databaseFile)
                true
            }
        }
    }
}
