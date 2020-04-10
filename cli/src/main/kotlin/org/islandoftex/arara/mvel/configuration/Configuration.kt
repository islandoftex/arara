// SPDX-License-Identifier: BSD-3-Clause
package org.islandoftex.arara.mvel.configuration

import java.nio.file.Path
import org.islandoftex.arara.Arara
import org.islandoftex.arara.api.AraraException
import org.islandoftex.arara.cli.configuration.AraraSpec
import org.islandoftex.arara.cli.configuration.ConfigurationUtils
import org.islandoftex.arara.cli.localization.LanguageController
import org.islandoftex.arara.core.session.ExecutionOptions

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
    /**
     * Loads the application configuration.
     *
     * @throws AraraException Something wrong happened, to be caught in the
     * higher levels.
     */
    @Throws(AraraException::class)
    fun load(file: Path) {
        // then validate it and update the configuration accordingly
        val resource = ConfigurationUtils.loadLocalConfiguration(file)
        val executionOptions = resource.toExecutionOptions()
        val baseOptions = Arara.config[AraraSpec.executionOptions]
        Arara.config[AraraSpec.executionOptions] = ExecutionOptions(
                maxLoops = executionOptions.maxLoops,
                timeoutValue = baseOptions.timeoutValue,
                parallelExecution = baseOptions.parallelExecution,
                haltOnErrors = baseOptions.haltOnErrors,
                databaseName = executionOptions.databaseName,
                verbose = executionOptions.verbose,
                executionMode = baseOptions.executionMode,
                fileTypes = executionOptions.fileTypes,
                rulePaths = executionOptions.rulePaths,
                parseOnlyHeader = executionOptions.parseOnlyHeader
        )
        Arara.config[AraraSpec.loggingOptions] = resource.toLoggingOptions()
        Arara.config[AraraSpec.userInterfaceOptions] = resource.toUserInterfaceOptions()

        // just to be sure, update the current locale in order to
        // display localized messages
        val locale = Arara.config[AraraSpec.Execution.language].locale
        LanguageController.setLocale(locale)
    }
}
