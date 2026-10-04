// SPDX-License-Identifier: BSD-3-Clause
package org.islandoftex.arara.core.configuration

import kotlinx.serialization.Serializable
import org.islandoftex.arara.api.AraraException
import org.islandoftex.arara.api.files.FileType
import org.islandoftex.arara.core.localization.LanguageController
import org.islandoftex.arara.core.session.LinearExecutor

/**
 * Implements the file type model.
 *
 * @author Island of TeX
 */
@Serializable
data class SerialFileType(
    private var extension: String = FileType.INVALID_EXTENSION,
    private var pattern: String = FileType.INVALID_PATTERN
) {

    /**
     * Convert this serialized file type object to a real file type API
     * object.
     *
     * @throws AraraException If the pattern is invalid.
     */
    fun toFileType(): FileType = org.islandoftex.arara.core.files.FileType(
        extension,
        pattern.takeUnless { pattern == FileType.INVALID_PATTERN }
            ?: DefaultConfiguration.defaultFileTypePatterns[extension]
            ?: throw AraraException(
                LanguageController.messages.errorFiletypeUnknownExtension(
                    extension,
                    LinearExecutor.executionOptions.fileTypes
                        .joinToString(" | ", "[ ", " ]")
                )
            )
    )
}
