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
package com.github.cereda.arara.model

import com.github.cereda.arara.controller.ConfigurationController
import com.github.cereda.arara.controller.LanguageController
import com.github.cereda.arara.utils.CommonUtils
import com.github.cereda.arara.utils.ConfigurationUtils
import java.nio.charset.StandardCharsets
import java.util.concurrent.TimeUnit

/**
 * Implements the configuration model, which holds the default settings and can
 * load the configuration file.
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
    @Throws(AraraException::class)
    fun load() {
        // initialize both file type and language models,
        // since we can track errors from there instead
        // of relying on a check on this level
        // TODO: check if still working (removed init functions because
        // the static variables should be initialized)

        // reset everything
        reset()

        // get the configuration file, if any
        val file = ConfigurationUtils.configFile
        if (file != null) {
            // set the configuration file name for
            // logging purposes
            ConfigurationController.put("execution.configuration.name",
                    CommonUtils.getCanonicalPath(file)
            )

            // then validate it and update the
            // configuration accordingly
            val resource = ConfigurationUtils.validateConfiguration(file)
            update(resource)
        }

        // just to be sure, update the
        // current locale in order to
        // display localized messages
        val locale = (ConfigurationController["execution.language"] as Language).locale
        LanguageController.setLocale(locale)
    }

    /**
     * Resets the configuration to initial settings.
     *
     * @throws AraraException Something wrong happened, to be caught in the
     * higher levels.
     */
    @Throws(AraraException::class)
    private fun reset() {
        // put everything in a map to be
        // later assigned to the configuration
        // controller, which holds the settings
        val mapping = mutableMapOf<String, Any>()

        mapping["execution.loops"] = 10L
        mapping["directives.charset"] = StandardCharsets.UTF_8
        mapping["execution.errors.halt"] = true
        mapping["execution.timeout"] = false
        mapping["execution.timeout.value"] = 0L
        mapping["execution.timeout.unit"] = TimeUnit.MILLISECONDS
        mapping["application.version"] = "5.0"
        mapping["application.revision"] = "0"
        mapping["directives.linebreak.pattern"] = "^\\s*-->\\s(.*)$"

        val directive = "^\\s*(\\w+)\\s*(:\\s*(\\{.*\\})\\s*)?"
        val pattern = "(\\s+(if|while|until|unless)\\s+(\\S.*))?$"

        mapping["directives.pattern"] = directive + pattern
        mapping["application.pattern"] = "arara:\\s"
        mapping["application.width"] = 65
        mapping["execution.database.name"] = "arara"
        mapping["execution.log.name"] = "arara"
        mapping["execution.verbose"] = false

        mapping["trigger.halt"] = false
        mapping["execution.language"] = Language("en")
        mapping["execution.logging"] = false
        mapping["execution.dryrun"] = false
        mapping["execution.status"] = 0
        mapping["application.copyright.year"] = "2012-2019"
        mapping["execution.filetypes"] = ConfigurationUtils.defaultFileTypes
        mapping["execution.rule.paths"] = listOf(CommonUtils.buildPath(
                ConfigurationUtils.applicationPath,
                "rules"
        ))

        mapping["execution.preambles"] = mutableMapOf<String, String>()
        mapping["execution.preamble.active"] = false
        mapping["execution.configuration.name"] = "[none]"
        mapping["execution.header"] = false
        mapping["ui.lookandfeel"] = "none"

        // get the configuration controller and
        // set every map key to it
        val controller = ConfigurationController
        mapping.forEach { (key, value) ->
            controller.put(key, value)
        }
    }

    /**
     * Update the configuration based on the provided map.
     *
     * @param resource Map containing the new configuration settings.
     * @throws AraraException Something wrong happened, to be caught in the
     * higher levels.
     */
    @Throws(AraraException::class)
    private fun update(resource: Resource) {
        val controller = ConfigurationController

        if (resource.paths != null) {
            var paths = resource.paths!!
            paths = ConfigurationUtils.normalizePaths(paths)
            controller.put("execution.rule.paths", paths)
        }

        if (resource.filetypes != null && resource.filetypes.isNotEmpty()) {
            val resources = resource.filetypes
            var filetypes = mutableListOf<FileType>()
            for (type in resources) {
                if (type.pattern != null) {
                    filetypes.add(
                            FileType(type.extension!!, type.pattern!!)
                    )
                } else {
                    filetypes.add(FileType(type.extension!!))
                }
            }
            filetypes = ConfigurationUtils.normalizeFileTypes(filetypes)
                    .toMutableList()
            controller.put("execution.filetypes", filetypes)
        }

        controller.put("execution.verbose", resource.isVerbose)
        controller.put("execution.logging", resource.isLogging)
        controller.put("execution.header", resource.isHeader)

        if (resource.dbname != null) {
            controller.put("execution.database.name",
                    ConfigurationUtils.cleanFileName(resource.dbname!!))
        }

        if (resource.logname != null) {
            controller.put("execution.log.name",
                    ConfigurationUtils.cleanFileName(resource.logname!!))
        }

        if (resource.language != null) {
            controller.put("execution.language",
                    Language(resource.language!!))
        }

        val loops = resource.loops
        if (loops > 0) {
            controller.put("execution.loops", loops)
        } else {
            if (loops < 0) {
                throw AraraException(
                        messages.getMessage(
                                Messages.ERROR_CONFIGURATION_LOOPS_INVALID_RANGE
                        )
                )
            }
        }

        if (resource.preambles != null && resource.preambles.isNotEmpty()) {
            controller.put("execution.preambles",
                    resource.preambles)
        }

        if (resource.laf != null) {
            controller.put("ui.lookandfeel",
                    resource.laf!!)
        }

    }

}
