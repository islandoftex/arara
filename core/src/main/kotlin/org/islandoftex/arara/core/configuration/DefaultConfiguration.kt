// SPDX-License-Identifier: BSD-3-Clause
package org.islandoftex.arara.core.configuration

import org.islandoftex.arara.api.AraraException
import org.islandoftex.arara.api.files.FileType
import org.islandoftex.arara.api.files.MPPPath
import org.islandoftex.arara.core.localization.LanguageController
import org.islandoftex.arara.core.session.Environment

/**
 * Implements the built-in defaults the configuration model falls back
 * to: the known file type patterns and the application location.
 */
object DefaultConfiguration {
    /**
     * The default file type patterns, mapped from extension to the
     * comment pattern a file of that type starts its directives with.
     */
    val defaultFileTypePatterns = mapOf(
        "tex" to "^\\s*%\\s*",
        "dtx" to "^\\s*%\\s*",
        "ltx" to "^\\s*%\\s*",
        "drv" to "^\\s*%\\s*",
        "ins" to "^\\s*%\\s*"
    )

    /**
     * The default file types, derived from [defaultFileTypePatterns].
     */
    val defaultFileTypes: List<FileType> by lazy {
        defaultFileTypePatterns
            .map { (extension, pattern) ->
                org.islandoftex.arara.core.files.FileType(extension, pattern)
            }.distinct()
    }

    /**
     * The directory the arara application itself was launched from.
     */
    val applicationPath: MPPPath
        @Throws(AraraException::class)
        get() = runCatching {
            val codeLocation = requireNotNull(this::class.java.protectionDomain.codeSource?.location?.toURI()?.path)
                .let {
                    // Experiments have found that Windows might have paths like "/D:/…"
                    // where the first slash is unexpected; this caused multiple issues,
                    // e.g. #35 or #83. In the path, `Paths.get(File(path).toURI())`
                    // was used for before getting parent and normalizing but this
                    // conversion including three path types is also error-prone and not
                    // understandable, hence the current workaround here to fix symptoms
                    if (Environment.checkOS(Environment.SupportedOS.WINDOWS) && it.startsWith('/')) {
                        it.removePrefix("/")
                    } else {
                        it
                    }
                }
            MPPPath(codeLocation).parent.normalize()
        }.getOrElse {
            throw AraraException(
                LanguageController.messages.errorGetApplicationPathEncodingException,
                it
            )
        }
}
