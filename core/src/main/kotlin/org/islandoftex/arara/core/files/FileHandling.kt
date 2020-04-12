// SPDX-License-Identifier: BSD-3-Clause
package org.islandoftex.arara.core.files

import java.io.IOException
import java.nio.file.Files
import java.nio.file.Path
import java.util.zip.CRC32
import org.islandoftex.arara.api.AraraException
import org.islandoftex.arara.core.localization.LanguageController

object FileHandling {
    /**
     * Gets a normalized path from the provided path.
     *
     * Please note that this is not the real path but a normalized absolute
     * path. The file represented by this does not have to exist.
     *
     * @param path The path to the file.
     * @return The normalized path from the provided path.
     */
    @Throws(AraraException::class)
    fun normalize(path: Path): Path = path.toAbsolutePath().normalize()

    /**
     * Resolve the sibling of a file with just its extension changed.
     *
     * @param path The path to use as base.
     * @param extension The extension.
     * @return The full file path to the sibling.
     */
    fun changeExtension(path: Path, extension: String): Path {
        val name = path.fileName.toString().substringBeforeLast('.') +
                ".$extension"
        return normalize(path.resolveSibling(name))
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
    fun isSubDirectory(child: Path, parent: Path): Boolean {
        return if (Files.isDirectory(child) && Files.isDirectory(parent)) {
            normalize(child).startsWith(normalize(parent))
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
    fun calculateHash(path: Path): Long =
            try {
                CRC32().run {
                    update(path.toFile().readBytes())
                    value
                }
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
    fun hasChanged(file: Path, databaseFile: Path): Boolean {
        val database = Database.load(databaseFile)
        val path = normalize(file)
        return if (!Files.exists(path)) {
            if (path in database) {
                database.remove(path)
                database.save(databaseFile)
                true
            } else {
                false
            }
        } else {
            val hash = calculateHash(file)
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
