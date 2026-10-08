// SPDX-License-Identifier: BSD-3-Clause
package org.islandoftex.arara.core.session

import io.github.oshai.kotlinlogging.KotlinLogging
import org.islandoftex.arara.api.AraraException
import org.islandoftex.arara.api.configuration.LoggingOptions
import org.islandoftex.arara.api.configuration.UserInterfaceOptions
import org.islandoftex.arara.api.session.Session
import org.islandoftex.arara.core.localization.LanguageController

/**
 * Implements the session, i.e. one single run of the whole arara tool.
 * It is a superset of the session exposed to the user.
 */
object Session : Session {
    /**
     * The logger of the session's own mutations: every set,
     * overwrite, removal and wipe writes its line as it happens,
     * so a variable interfering with a run tells its own story
     * between the run's entries - the reads stay silent, as they
     * fire on every conditional.
     */
    private val logger = KotlinLogging.logger { }

    // the session map which holds the execution session;
    // the idea here is to provide wrappers to the map
    // methods, so it could be easily manipulated
    /**
     * The backing store of the session: the key-value pairs
     * the run accumulates.
     */
    private val map = mutableMapOf<String, Any>()

    /**
     * Spells a count of session variables for a log line, so the
     * singular never reads as a plural.
     *
     * @param count The number of session variables.
     * @return The count with its noun.
     */
    private fun variables(count: Int) =
        "$count ${if (count == 1) "variable" else "variables"}"

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
    override operator fun get(key: String): Any =
        map[key] ?: throw AraraException(
            LanguageController.messages
                .errorSessionObtainUnknownKey(key)
        )

    /**
     * Inserts (or overwrites) the object indexed by the provided key into the
     * session. This method holds the map method of the very same name.
     *
     * @param key The provided key.
     * @param value The value to be inserted.
     */
    override fun put(key: String, value: Any) {
        val previous = map.put(key, value)
        logger.info {
            if (previous == null) {
                "Session variable '$key' set to '$value'."
            } else {
                "Session variable '$key' overwritten with " +
                    "'$value' (was '$previous')."
            }
        }
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
            logger.info { "Session variable '$key' removed." }
        } else {
            throw AraraException(
                    LanguageController.messages
                            .errorSessionRemoveUnknownKey(key)
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
    override fun clear() {
        val count = map.size
        map.clear()
        logger.info { "Session cleared (${variables(count)})." }
    }

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
            removalFilter: (String) -> Boolean = { true }
    ) {
        // remove all current environment variables to clean up the session;
        // both filters operate on the plain environment variable names
        map.filterKeys { it.startsWith("environment:") }
                .forEach { (key, _) ->
                    if (removalFilter(key.removePrefix("environment:")))
                        remove(key)
                }
        // add all relevant new environment variables
        map.putAll(
                System.getenv()
                        .filterKeys(additionFilter)
                        .mapKeys { "environment:${it.key}" }
        )
        val count = map.keys.count { it.startsWith("environment:") }
        logger.info { "Session environment refreshed (${variables(count)})." }
    }
}
