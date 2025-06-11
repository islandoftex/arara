// SPDX-License-Identifier: BSD-3-Clause
package org.islandoftex.arara.api.session

/**
 *  This interface wraps a map that holds the execution session, that is, a
 *  dirty maneuver to exchange pretty much any data between commands and even
 *  rules.
 */
public interface Session {
    /**
     * Gets the object indexed by the provided key from the session. This method
     * holds the map method of the very same name.
     *
     * @param key The provided key.
     * @return The object indexed by the provided key.
     */
    public operator fun get(key: String): Any

    /**
     * Inserts (or overwrites) the object indexed by the provided key into the
     * session. This method holds the map method of the very same name.
     *
     * @param key The provided key.
     * @param value The value to be inserted.
     */
    public fun put(
        key: String,
        value: Any,
    )

    /**
     * Removes the entry indexed by the provided key from the session. This method
     * holds the map method of the same name.
     *
     * @param key The provided key.
     */
    public fun remove(key: String)

    /**
     * Checks if the provided key exists in the session.
     *
     * @param key The provided key.
     * @return A boolean value indicating if the provided key exists in the
     * session.
     */
    public operator fun contains(key: String): Boolean

    /**
     * Clears the session (map). This method, as usual, holds the map method of
     * the same name.
     */
    public fun clear()
}
