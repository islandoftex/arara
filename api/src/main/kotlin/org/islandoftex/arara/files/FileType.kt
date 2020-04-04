// SPDX-License-Identifier: BSD-3-Clause
package org.islandoftex.arara.files

/**
 * Implements the file type model.
 */
interface FileType {
    /**
     * This identifies the file's extension without dot.
     */
    val extension: String

    /**
     * This identifies the search pattern for arara's directive comments.
     */
    val pattern: String

    companion object {
        /**
         * This constant identifies an invalid extension. As unices do not
         * allow a forward and Windows does not allow a backward slash, this
         * should suffice.
         */
        const val INVALID_EXTENSION = "/\\"

        /**
         * This constant identifies an invalid pattern. This is an opening
         * character class which is invalid.
         */
        const val INVALID_PATTERN = ""
    }

    /**
     * This indicates whether an instance of an implementation is invalid.
     */
    val isUnknown: Boolean
        get() = extension == INVALID_EXTENSION || pattern == INVALID_PATTERN
}
