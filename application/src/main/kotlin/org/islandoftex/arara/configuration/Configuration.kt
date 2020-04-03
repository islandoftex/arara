// SPDX-License-Identifier: BSD-3-Clause
package org.islandoftex.arara.configuration

import java.nio.file.Paths
import org.islandoftex.arara.Arara
import org.islandoftex.arara.AraraException
import org.islandoftex.arara.filehandling.FileHandlingUtils
import org.islandoftex.arara.localization.Language
import org.islandoftex.arara.localization.LanguageController
import org.islandoftex.arara.localization.Messages
import org.islandoftex.arara.utils.LoggingUtils

/**
 * Implements the configuration model, which holds the default settings and can
 * load the configuration file. The idea here is to provide a map that holds
 * all configuration settings used by model and utilitary classes throughout
 * the execution. This controller is implemented as a singleton.
 *
 * @author Island of TeX
 * @version 5.0
 * @since 4.0
 */
object Configuration {
    // the application messages obtained from the
    // language controller
    private val messages = LanguageController

    /**
     * Loads the application configuration.
     *
     * @throws AraraException Something wrong happened, to be caught in the
     * higher levels.
     */
    @Throws(AraraException::class)
    fun load() {
        // initialize both file type and language models,
        // since we can track errors from there instead
        // of relying on a check on this level

        // get the configuration file, if any
        val file = ConfigurationUtils.configFile
        if (file != null) {
            // set the configuration file name for
            // logging purposes
            Arara.config[AraraSpec.Execution.configurationName] =
                    FileHandlingUtils.getCanonicalPath(file)

            // then validate it and update the
            // configuration accordingly
            val resource = ConfigurationUtils.loadLocalConfiguration(file)
            update(resource)
        }

        // just to be sure, update the
        // current locale in order to
        // display localized messages
        val locale = Arara.config[AraraSpec.Execution.language].locale
        LanguageController.setLocale(locale)
    }

    /**
     * Update the configuration based on the provided map.
     *
     * @param resource Map containing the new configuration settings.
     * @throws AraraException Something wrong happened, to be caught in the
     * higher levels.
     */
    @Throws(AraraException::class)
    private fun update(resource: LocalConfiguration) {
        if (resource.paths.isNotEmpty())
            Arara.config[AraraSpec.Execution.rulePaths] =
                    ConfigurationUtils.normalizePaths(resource.paths)

        if (resource.filetypes.isNotEmpty()) {
            Arara.config[AraraSpec.Execution.fileTypes] =
                    ConfigurationUtils.normalizeFileTypes(resource.filetypes)
        }

        Arara.config[AraraSpec.Execution.verbose] = resource.isVerbose
        Arara.config[AraraSpec.Execution.onlyHeader] = resource.isHeader
        Arara.config[AraraSpec.Execution.language] =
                Language(resource.language)
        Arara.config[AraraSpec.UserInteraction.lookAndFeel] = resource.laf

        Arara.config[AraraSpec.Execution.databaseName] =
                Paths.get(ConfigurationUtils.cleanFileName(resource.dbname))
        Arara.config[AraraSpec.Execution.logName] =
                ConfigurationUtils.cleanFileName(resource.logname)

        Arara.config[AraraSpec.Execution.logging] = resource.isLogging
        LoggingUtils.enableLogging(resource.isLogging)

        val loops = resource.loops
        if (loops <= 0) {
            throw AraraException(messages.getMessage(Messages
                    .ERROR_CONFIGURATION_LOOPS_INVALID_RANGE))
        } else {
            Arara.config[AraraSpec.Execution.maxLoops] = loops
        }

        if (resource.preambles.isNotEmpty())
            Arara.config[AraraSpec.Execution.preambles] = resource.preambles
    }
}
