// SPDX-License-Identifier: BSD-3-Clause
package org.islandoftex.arara.core.localization

/**
 * This enumeration contains all application messages.
 *
 * @author Island of TeX
 * @version 5.0
 * @since 4.0
 */
internal enum class Messages {
    // The ''get'' method has found an unknown key ''{0}'' in the session scope.
    // I could not get something I do not have in the first place.
    // Please enter a valid key and try again.
    ERROR_SESSION_OBTAIN_UNKNOWN_KEY,
    // The ''remove'' method has found an unknown key ''{0}'' in the session scope.
    // I could not remove something I do not have in the first place.
    // Please enter a valid key and try again.
    ERROR_SESSION_REMOVE_UNKNOWN_KEY
}
