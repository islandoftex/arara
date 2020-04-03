// SPDX-License-Identifier: BSD-3-Clause
package org.islandoftex.arara.configuration

import com.charleskorn.kaml.Yaml
import java.io.File
import java.io.UnsupportedEncodingException
import java.net.URLDecoder
import java.nio.file.Path
import java.nio.file.Paths
import org.islandoftex.arara.Arara
import org.islandoftex.arara.AraraException
import org.islandoftex.arara.files.FileType
import org.islandoftex.arara.localization.LanguageController
import org.islandoftex.arara.localization.Messages
import org.islandoftex.arara.model.FileTypeImpl
import org.islandoftex.arara.utils.CommonUtils

/**
 * Implements configuration utilitary methods.
 *
 * @author Island of TeX
 * @version 5.0
 * @since 4.0
 */
object ConfigurationUtils {
    // the application messages obtained from the
    // language controller
    private val messages = LanguageController

    /**
     * This map contains all file types that arara accepts
     * and their corresponding search patterns (for comments).
     */
    val defaultFileTypePatterns = mapOf(
            "tex" to "^\\s*%\\s+",
            "dtx" to "^\\s*%\\s+",
            "ltx" to "^\\s*%\\s+",
            "drv" to "^\\s*%\\s+",
            "ins" to "^\\s*%\\s+"
    )

    /**
     * Set of default file types provided by arara.
     * Initialization may throw AraraException if file types are wrong
     */
    val defaultFileTypes: Set<FileType> by lazy {
        defaultFileTypePatterns
                .map { (extension, pattern) -> FileTypeImpl(extension, pattern) }
                .toSet()
    }

    /**
     * The configuration file in use.
     *
     * Look for configuration files in the user's working directory first
     * if no configuration files are found in the user's working directory,
     * try to look up in a global directory, that is, the user home.
     */
    val configFile: File?
        get() {
            val names = listOf(".araraconfig.yaml",
                    "araraconfig.yaml", ".arararc.yaml", "arararc.yaml")
            Arara.config[AraraSpec.Execution.workingDirectory]
                    .let { workingDir ->
                        val first = names
                                .map { workingDir.resolve(it).toFile() }
                                .firstOrNull { it.exists() }
                        if (first != null)
                            return first
                    }
            CommonUtils.getSystemPropertyOrNull("user.home")?.let { userHome ->
                return names.map { File(userHome).resolve(it) }
                        .firstOrNull { it.exists() }
            }
            return null
        }

    /**
     * The canonical absolute application path.
     *
     * @throws AraraException Something wrong happened, to be caught in the
     * higher levels.
     */
    val applicationPath: Path
        @Throws(AraraException::class)
        get() {
            try {
                var path = Arara::class.java.protectionDomain.codeSource
                        .location.path
                path = URLDecoder.decode(path, "UTF-8")
                return Paths.get(File(path).toURI()).parent.toAbsolutePath()
            } catch (exception: UnsupportedEncodingException) {
                throw AraraException(
                        messages.getMessage(
                                Messages.ERROR_GETAPPLICATIONPATH_ENCODING_EXCEPTION
                        ),
                        exception
                )
            }
        }

    /**
     * Validates the configuration file.
     *
     * @param file The configuration file.
     * @return The configuration file as a resource.
     * @throws AraraException Something wrong happened, to be caught in the
     * higher levels.
     */
    @Throws(AraraException::class)
    fun loadLocalConfiguration(file: File): LocalConfiguration {
        return file.runCatching {
            val text = readText()
            if (!text.startsWith("!config"))
                throw Exception("Configuration should start with !config")
            Yaml.default.parse(LocalConfiguration.serializer(),
                    text)
        }.getOrElse {
            throw AraraException(
                    messages.getMessage(
                            Messages.ERROR_CONFIGURATION_GENERIC_ERROR
                    ), it
            )
        }
    }

    /**
     * Normalize a list of rule paths, removing all duplicates.
     *
     * @param paths The list of rule paths.
     * @return A list of normalized paths, without duplicates.
     * @throws AraraException Something wrong happened, to be caught in the
     * higher levels.
     */
    @Throws(AraraException::class)
    fun normalizePaths(paths: Iterable<String>): Set<String> =
            paths.union(AraraSpec.Execution.rulePaths.default)

    /**
     * Normalize a list of file types, removing all duplicates.
     *
     * @param types The list of file types.
     * @return A list of normalized file types, without duplicates.
     * @throws AraraException Something wrong happened, to be caught in the
     * higher levels.
     */
    @Throws(AraraException::class)
    fun normalizeFileTypes(types: Iterable<FileType>): Set<FileType> =
            types.union(defaultFileTypes)

    /**
     * Cleans the file name to avoid invalid entries.
     *
     * @param name The file name.
     * @return A cleaned file name.
     */
    fun cleanFileName(name: String): String {
        val result = File(name).name.trim()
        return if (result.isEmpty()) "arara" else result.trim()
    }
}
