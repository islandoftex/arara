/*
 * Arara, the cool TeX automation tool
 * Copyright (c) 2012 -- 2019, Paulo Roberto Massa Cereda
 * All rights reserved.
 *
 * Redistribution and  use in source  and binary forms, with  or without
 * modification, are  permitted provided  that the  following conditions
 * are met:
 *
 * 1. Redistributions  of source  code must  retain the  above copyright
 * notice, this list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form  must reproduce the above copyright
 * notice, this list  of conditions and the following  disclaimer in the
 * documentation and/or other materials provided with the distribution.
 *
 * 3. Neither  the name  of the  project's author nor  the names  of its
 * contributors may be used to  endorse or promote products derived from
 * this software without specific prior written permission.
 *
 * THIS SOFTWARE IS  PROVIDED BY THE COPYRIGHT  HOLDERS AND CONTRIBUTORS
 * "AS IS"  AND ANY  EXPRESS OR IMPLIED  WARRANTIES, INCLUDING,  BUT NOT
 * LIMITED  TO, THE  IMPLIED WARRANTIES  OF MERCHANTABILITY  AND FITNESS
 * FOR  A PARTICULAR  PURPOSE  ARE  DISCLAIMED. IN  NO  EVENT SHALL  THE
 * COPYRIGHT HOLDER OR CONTRIBUTORS BE  LIABLE FOR ANY DIRECT, INDIRECT,
 * INCIDENTAL, SPECIAL, EXEMPLARY,  OR CONSEQUENTIAL DAMAGES (INCLUDING,
 * BUT  NOT LIMITED  TO, PROCUREMENT  OF SUBSTITUTE  GOODS OR  SERVICES;
 * LOSS  OF USE,  DATA, OR  PROFITS; OR  BUSINESS INTERRUPTION)  HOWEVER
 * CAUSED AND  ON ANY THEORY  OF LIABILITY, WHETHER IN  CONTRACT, STRICT
 * LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY
 * WAY  OUT  OF  THE USE  OF  THIS  SOFTWARE,  EVEN  IF ADVISED  OF  THE
 * POSSIBILITY OF SUCH DAMAGE.
 */
package org.islandoftex.arara.configuration

import org.islandoftex.arara.Arara
import org.islandoftex.arara.filehandling.FileHandlingUtils
import org.islandoftex.arara.localization.Language
import org.islandoftex.arara.localization.LanguageController
import org.islandoftex.arara.localization.Messages
import org.islandoftex.arara.model.AraraException
import kotlin.time.ExperimentalTime

/**
 * Implements the configuration model, which holds the default settings and can
 * load the configuration file. The idea here is to provide a map that holds
 * all configuration settings used by model and utilitary classes throughout
 * the execution. This controller is implemented as a singleton.
 *
 * @author Paulo Roberto Massa Cereda
 * @version 4.0
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
    @ExperimentalTime
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
            Arara.config[AraraSpec.Execution.fileTypes] = ConfigurationUtils.normalizeFileTypes(resource.filetypes)
        }

        Arara.config[AraraSpec.Execution.verbose] = resource.isVerbose
        Arara.config[AraraSpec.Execution.logging] = resource.isLogging
        Arara.config[AraraSpec.Execution.onlyHeader] = resource.isHeader
        Arara.config[AraraSpec.Execution.language] =
                Language(resource.language)
        Arara.config[AraraSpec.UserInteraction.lookAndFeel] = resource.laf

        Arara.config[AraraSpec.Execution.databaseName] =
                ConfigurationUtils.cleanFileName(resource.dbname)
        Arara.config[AraraSpec.Execution.logName] =
                ConfigurationUtils.cleanFileName(resource.logname)

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
