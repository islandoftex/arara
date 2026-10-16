// SPDX-License-Identifier: BSD-3-Clause
package org.islandoftex.arara.core.files

import kotlin.io.encoding.Base64
import org.islandoftex.arara.api.AraraException
import org.islandoftex.arara.api.files.Database
import org.islandoftex.arara.api.files.MPPPath
import org.islandoftex.arara.core.localization.LanguageController

/**
 * The database model, which keeps track on file changes.
 *
 * This database is a map because it maps files to hashes. So the key will
 * always be a file representation and the value always a CRC32 hash.
 *
 * On disk, the database is a private, machine-owned cache: one record
 * per line, the encoded file name followed by its hash. The encoding
 * holds every possible file name inside its own line, so no name can
 * break the record grammar, and a file that does not follow the
 * grammar is refused instead of being overwritten.
 *
 * The class intentionally shares its simple name with the api interface
 * it implements; the local declaration shadows the import on purpose.
 */
data class Database(
    /**
     * The whole database is implemented as a map, where
     * the key is the absolute canonical file and the value
     * is its corresponding CRC32 hash.
     */
    private val map: MutableMap<String, Long> = mutableMapOf()
) : Database {
    /**
     * Check whether the database contains a file.
     *
     * @param path The path to a file. It should be implemented in a way that
     *   non-canonical paths are dealt with.
     * @return Whether the object represented by the path is in the database.
     */
    override fun contains(path: MPPPath): Boolean =
        path.normalize().toString() in map

    /**
     * Get the hash value associated with a file.
     *
     * @param path The path to a file. Should use the same mechanics as
     *   [contains].
     * @return The hash value associated with the file if any and `null` if
     *   the element is not contained in the database.
     */
    override fun get(path: MPPPath): Long? =
        map[path.normalize().toString()]

    /**
     * Set the hash value for a given file.
     *
     * @param path The path of a file. Try to resolve it to an absolute path.
     * @param hash The hash value of the file.
     */
    override fun set(path: MPPPath, hash: Long) {
        map[path.normalize().toString()] = hash
    }

    /**
     * Remove the entry associated with the key (file).
     *
     * @param path The file acting as key in the database.
     * @throws NoSuchElementException exception if the [path] is not contained
     *   within the database.
     */
    @Throws(NoSuchElementException::class)
    override fun remove(path: MPPPath) {
        val normalPath = path.normalize().toString()
        map.remove(normalPath) ?: throw NoSuchElementException(
            "Attempt to remove non-existent path from database."
        )
    }

    /**
     * Save the database.
     *
     * The records sit in the sorted order of their names, so the same
     * database always writes the same bytes.
     *
     * @param path Where to save it.
     */
    @Throws(AraraException::class)
    override fun save(path: MPPPath) {
        runCatching {
            val records = map.entries
                .sortedBy { it.key }
                .joinToString("\n") { (file, hash) ->
                    "${Base64.encode(file.encodeToByteArray())} $hash"
                }
            path.normalize().writeText(
                if (records.isEmpty()) records else "$records\n"
            )
        }.getOrElse {
            throw AraraException(
                LanguageController.messages.errorSaveCouldNotSaveDatabase(
                    path.fileName
                ),
                it
            )
        }
    }

    /**
     * Holds the loader of the file database.
     */
    companion object {
        /**
         * Loads the file holding the database.
         *
         * An absent file holds an empty database, so a fresh start is
         * indistinguishable from the very first run. A file that
         * exists but does not follow the record grammar is refused:
         * the database is a cache and a fresh one is welcome, but a
         * foreign file the configuration pointed at is not ours to
         * overwrite.
         *
         * @return The database object.
         * @throws AraraException Something wrong happened, to be caught in the
         *   higher levels.
         */
        @Throws(AraraException::class)
        fun load(path: MPPPath): Database {
            return if (!path.exists) {
                Database()
            } else {
                runCatching {
                    val lines = path.normalize().readText()
                        .lines()
                        .filterNot { it.isBlank() }
                    val records = lines.associate { line ->
                        val separator = line.indexOf(' ')
                        require(separator > 0) {
                            "record has no separator"
                        }
                        val hash = line.substring(separator + 1)
                        require(
                            hash.isNotEmpty() && hash.all { it in '0'..'9' }
                        ) {
                            "record hash is not a number"
                        }
                        val file = Base64
                            .decode(line.substring(0, separator))
                            .decodeToString()
                        file to hash.toLong()
                    }
                    require(records.size == lines.size) {
                        "record file name is duplicated"
                    }
                    Database(records.toMutableMap())
                }.getOrElse {
                    throw AraraException(
                        LanguageController
                            .messages.errorLoadCouldNotLoadDatabase(
                                path.fileName
                            ),
                        it
                    )
                }
            }
        }
    }
}
