// SPDX-License-Identifier: BSD-3-Clause
package org.islandoftex.arara.cli.configuration

import org.islandoftex.arara.Arara
import org.islandoftex.arara.api.AraraException
import org.islandoftex.arara.cli.filehandling.FileHandlingUtils
import org.islandoftex.arara.cli.localization.Language
import org.islandoftex.arara.cli.localization.LanguageController
import org.islandoftex.arara.cli.localization.Messages
import org.islandoftex.arara.cli.utils.LoggingUtils
import org.islandoftex.arara.mvel.configuration.LocalConfiguration

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
        val executionOptions = resource.toExecutionOptions()
        val loggingOptions = resource.toLoggingOptions()
        val uiOptions = resource.toUserInterfaceOptions()

        Arara.config[AraraSpec.Execution.rulePaths] = executionOptions.rulePaths
        Arara.config[AraraSpec.Execution.fileTypes] = executionOptions.fileTypes

        Arara.config[AraraSpec.Execution.verbose] = executionOptions.verbose
        Arara.config[AraraSpec.Execution.onlyHeader] = executionOptions.parseOnlyHeader
        Arara.config[AraraSpec.Execution.language] = Language(uiOptions.languageCode)
        Arara.config[AraraSpec.UserInteraction.lookAndFeel] = uiOptions.swingLookAndFeel

        Arara.config[AraraSpec.Execution.databaseName] = executionOptions.databaseName
        Arara.config[AraraSpec.Execution.logName] = loggingOptions.logFile.toString()

        Arara.config[AraraSpec.Execution.logging] = loggingOptions.enableLogging
        LoggingUtils.enableLogging(loggingOptions.enableLogging)

        val loops = executionOptions.maxLoops
        if (loops <= 0) {
            throw AraraException(
                messages.getMessage(
                    Messages.ERROR_CONFIGURATION_LOOPS_INVALID_RANGE
                )
            )
        } else {
            Arara.config[AraraSpec.Execution.maxLoops] = loops
        }

        if (resource.preambles.isNotEmpty())
            Arara.config[AraraSpec.Execution.preambles] = resource.preambles
    }
}
