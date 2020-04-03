package org.islandoftex.arara.session

/**
 *  This interface wraps a map that holds the execution session, that is, a
 *  dirty maneuver to exchange pretty much any data between commands and even
 *  rules.
 */
interface Session {
    /**
     * Gets the object indexed by the provided key from the session. This method
     * holds the map method of the very same name.
     *
     * @param key The provided key.
     * @return The object indexed by the provided key.
     */
    operator fun get(key: String): Any

    /**
     * Inserts (or overwrites) the object indexed by the provided key into the
     * session. This method holds the map method of the very same name.
     *
     * @param key The provided key.
     * @param value The value to be inserted.
     */
    fun put(key: String, value: Any)

    /**
     * Removes the entry indexed by the provided key from the session. This method
     * holds the map method of the same name.
     *
     * @param key The provided key.
     */
    fun remove(key: String)

    /**
     * Checks if the provided key exists in the session.
     *
     * @param key The provided key.
     * @return A boolean value indicating if the provided key exists in the
     * session.
     */
    operator fun contains(key: String): Boolean

    /**
     * Clears the session (map). This method, as usual, holds the map method of
     * the same name.
     */
    fun clear()
}
