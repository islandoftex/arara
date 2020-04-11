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
    // I could not load the YAML database named ''{0}''. I have no idea why it
    // failed, though. Perhaps the file was moved or deleted before or during
    // the reading operation. Or maybe I do not have the proper permissions to
    // read the file. By the way, make sure the YAML file is well-formed.
    ERROR_LOAD_COULD_NOT_LOAD_XML,
    // I could not save the YAML database named ''{0}''. I have no idea why it
    // failed, though. Perhaps I do not have the proper permissions to write the
    // YAML file to disk.
    ERROR_SAVE_COULD_NOT_SAVE_XML,
    // The ''get'' method has found an unknown key ''{0}'' in the session scope.
    // I could not get something I do not have in the first place.
    // Please enter a valid key and try again.
    ERROR_SESSION_OBTAIN_UNKNOWN_KEY,
    // The ''remove'' method has found an unknown key ''{0}'' in the session scope.
    // I could not remove something I do not have in the first place.
    // Please enter a valid key and try again.
    ERROR_SESSION_REMOVE_UNKNOWN_KEY
}
