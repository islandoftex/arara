// SPDX-License-Identifier: BSD-3-Clause
package org.islandoftex.arara.core.session

import com.soywiz.korio.lang.Environment
import org.islandoftex.arara.api.AraraException
import org.islandoftex.arara.api.configuration.LoggingOptions
import org.islandoftex.arara.api.configuration.UserInterfaceOptions
import org.islandoftex.arara.api.session.Session
import org.islandoftex.arara.core.localization.LanguageController
import org.islandoftex.arara.core.utils.formatString

/**
 * Implements the session, i.e. one single run of the whole arara tool.
 * It is a superset of the session exposed to the user.
 *
 * @author Island of TeX
 * @version 5.0
 * @since 4.0
 */
object Session : Session {
    // the session map which holds the execution session;
    // the idea here is to provide wrappers to the map
    // methods, so it could be easily manipulated
    private val map = mutableMapOf<String, Any>()

    /**
     * arara's user interface configuration.
     */
    var userInterfaceOptions: UserInterfaceOptions =
        org.islandoftex.arara.core.configuration.UserInterfaceOptions()

    /**
     * arara's logging configuration.
     */
    var loggingOptions: LoggingOptions =
        org.islandoftex.arara.core.configuration.LoggingOptions()

    /**
     * Gets the object indexed by the provided key from the session. This method
     * holds the map method of the very same name.
     *
     * @param key The provided key.
     * @return The object indexed by the provided key.
     * @throws AraraException Something wrong happened, to be caught in the
     * higher levels.
     */
    @Throws(AraraException::class)
    override operator fun get(key: String): Any {
        return if (contains(key)) {
            map.getValue(key)
        } else {
            throw AraraException(
                LanguageController.messages.ERROR_SESSION_OBTAIN_UNKNOWN_KEY
                    .formatString(key),
            )
        }
    }

    /**
     * Inserts (or overwrites) the object indexed by the provided key into the
     * session. This method holds the map method of the very same name.
     *
     * @param key The provided key.
     * @param value The value to be inserted.
     */
    override fun put(key: String, value: Any) {
        map[key] = value
    }

    /**
     * Removes the entry indexed by the provided key from the session. This method
     * holds the map method of the same name.
     *
     * @param key The provided key.
     * @throws AraraException Something wrong happened, to be caught in the
     * higher levels.
     */
    @Throws(AraraException::class)
    override fun remove(key: String) {
        if (contains(key)) {
            map.remove(key)
        } else {
            throw AraraException(
                LanguageController.messages.ERROR_SESSION_REMOVE_UNKNOWN_KEY
                    .formatString(key),
            )
        }
    }

    /**
     * Checks if the provided key exists in the session.
     *
     * @param key The provided key.
     * @return A boolean value indicating if the provided key exists in the
     * session.
     */
    override operator fun contains(key: String): Boolean = map.containsKey(key)

    /**
     * Clears the session (map). This method, as usual, holds the map method of
     * the same name.
     */
    override fun clear() = map.clear()

    /**
     * Update the environment variables stored in the session.
     *
     * @param additionFilter Which environment variables to include. You can
     *   filter their names (the string parameter) but not their values. By
     *   default all values will be added.
     * @param removalFilter Which environment variables to remove beforehand.
     *   By default all values will be removed.
     */
    fun updateEnvironmentVariables(
        additionFilter: (String) -> Boolean = { true },
        removalFilter: (String) -> Boolean = { true },
    ) {
        // remove all current environment variables to clean up the session
        map.filterKeys { it.startsWith("environment:") }
            .filterKeys(removalFilter)
            .forEach { remove(it.key) }
        // add all relevant new environment variables
        map.putAll(
            Environment.getAll()
                .filterKeys(additionFilter)
                .mapKeys { "environment:${it.key}" },
        )
    }
}
