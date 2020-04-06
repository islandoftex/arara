// SPDX-License-Identifier: BSD-3-Clause
package org.islandoftex.arara.cli.filehandling

import com.charleskorn.kaml.Yaml
import java.io.File
import org.islandoftex.arara.Arara
import org.islandoftex.arara.api.AraraException
import org.islandoftex.arara.cli.configuration.AraraSpec
import org.islandoftex.arara.cli.localization.LanguageController
import org.islandoftex.arara.cli.localization.Messages

/**
 * Implements database utilitary methods.
 *
 * @author Island of TeX
 * @version 5.0
 * @since 4.0
 */
object DatabaseUtils {
    // the application messages obtained from the
    // language controller
    private val messages = LanguageController

    /**
     * Gets the file representing the YAML file (database).
     *
     * @throws AraraException Something wrong happened, to be caught in the
     * higher levels.
     */
    private val file: File
        @Throws(AraraException::class)
        get() {
            val reference = Arara.config[AraraSpec.Execution.reference]
            val name = Arara.config[AraraSpec.Execution.databaseName].toString()
            val path = FileHandlingUtils.getParentCanonicalFile(reference)
            return path.resolve(name)
        }

    /**
     * Loads the YAML file representing the database.
     *
     * @return The database object.
     * @throws AraraException Something wrong happened, to be caught in the
     * higher levels.
     */
    @Throws(AraraException::class)
    fun load(): Database {
        return if (!exists()) {
            Database()
        } else {
            file.runCatching {
                val text = readText()
                if (!text.startsWith("!database"))
                    throw Exception("Database should start with !database")
                Yaml.default.parse(Database.serializer(), text)
            }.getOrElse {
                it.printStackTrace()
                throw AraraException(
                    messages.getMessage(
                        Messages.ERROR_LOAD_COULD_NOT_LOAD_XML,
                        file.name
                    ), it
                )
            }
        }
    }

    /**
     * Saves the database on a YAML file.
     *
     * @param database The database object.
     * @throws AraraException Something wrong happened, to be caught in the
     * higher levels.
     */
    @Throws(AraraException::class)
    fun save(database: Database) {
        file.runCatching {
            val content = "!database\n" +
                    Yaml.default.stringify(Database.serializer(), database)
            writeText(content)
        }.getOrElse {
            throw AraraException(
                messages.getMessage(
                    Messages.ERROR_SAVE_COULD_NOT_SAVE_XML,
                    file.name
                ), it
            )
        }
    }

    /**
     * Checks if the YAML file representing the database exists.
     *
     * @return A boolean value indicating if the YAML file exists.
     * @throws AraraException Something wrong happened, to be caught in the
     * higher levels.
     */
    @Throws(AraraException::class)
    private fun exists(): Boolean {
        return file.exists()
    }
}
