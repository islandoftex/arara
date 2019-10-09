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
package com.github.cereda.arara.configuration

import com.github.cereda.arara.localization.Language
import com.github.cereda.arara.localization.LanguageController
import com.github.cereda.arara.localization.Messages
import com.github.cereda.arara.model.AraraException
import com.github.cereda.arara.model.FileType
import com.github.cereda.arara.utils.CommonUtils
import java.nio.charset.StandardCharsets
import java.util.concurrent.TimeUnit

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
    // the configuration settings are stored in a map;
    // pretty much everything can be stored in this map,
    // as long as you know what to retrieve later on
    private val map: MutableMap<String, Any> = mutableMapOf()

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

        // reset everything
        reset()

        // get the configuration file, if any
        val file = ConfigurationUtils.configFile
        if (file != null) {
            // set the configuration file name for
            // logging purposes
            map["execution.configuration.name"] = CommonUtils
                    .getCanonicalPath(file)

            // then validate it and update the
            // configuration accordingly
            val resource = ConfigurationUtils.loadLocalConfiguration(file)
            update(resource)
        }

        // just to be sure, update the
        // current locale in order to
        // display localized messages
        val locale = (map["execution.language"] as Language).locale
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
        map["execution.loops"] = 10L
        map["directives.charset"] = StandardCharsets.UTF_8
        map["execution.errors.halt"] = true
        map["execution.timeout"] = false
        map["execution.timeout.value"] = 0L
        map["execution.timeout.unit"] = TimeUnit.MILLISECONDS
        map["application.version"] = "5.0.0"
        map["directives.linebreak.pattern"] = "^\\s*-->\\s(.*)$"

        val directive = "^\\s*(\\w+)\\s*(:\\s*(\\{.*\\})\\s*)?"
        val pattern = "(\\s+(if|while|until|unless)\\s+(\\S.*))?$"

        map["directives.pattern"] = directive + pattern
        map["application.pattern"] = "arara:\\s"
        map["application.width"] = 65
        map["execution.database.name"] = "arara"
        map["execution.log.name"] = "arara"
        map["execution.verbose"] = false

        map["trigger.halt"] = false
        map["execution.language"] = Language("en")
        map["execution.logging"] = false
        map["execution.dryrun"] = false
        map["execution.status"] = 0
        map["application.copyright.year"] = "2012-2019"
        map["execution.filetypes"] = ConfigurationUtils.defaultFileTypes
        map["execution.rule.paths"] = listOf(CommonUtils.buildPath(
                ConfigurationUtils.applicationPath,
                "rules"
        ))

        map["execution.preambles"] = mutableMapOf<String, String>()
        map["execution.preamble.active"] = false
        map["execution.configuration.name"] = "[none]"
        map["execution.header"] = false
        map["ui.lookandfeel"] = "none"
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
        if (resource.paths != null) {
            var paths = resource.paths!!
            paths = ConfigurationUtils.normalizePaths(paths)
            map["execution.rule.paths"] = paths
        }

        if (resource.filetypes.isNotEmpty()) {
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
            map["execution.filetypes"] = filetypes
        }

        map["execution.verbose"] = resource.isVerbose
        map["execution.logging"] = resource.isLogging
        map["execution.header"] = resource.isHeader
        map["execution.language"] = Language(resource.language)
        map["ui.lookandfeel"] = resource.laf

        if (resource.dbname != null) {
            map["execution.database.name"] = ConfigurationUtils
                    .cleanFileName(resource.dbname!!)
        }

        if (resource.logname != null) {
            map["execution.log.name"] = ConfigurationUtils
                    .cleanFileName(resource.logname!!)
        }

        val loops = resource.loops
        if (loops > 0) {
            map["execution.loops"] = loops
        } else {
            if (loops < 0) {
                throw AraraException(
                        messages.getMessage(
                                Messages.ERROR_CONFIGURATION_LOOPS_INVALID_RANGE
                        )
                )
            }
        }

        if (resource.preambles.isNotEmpty())
            map["execution.preambles"] = resource.preambles
    }

    /**
     * Returns the object indexed by the provided key. This method provides an
     * easy access to the underlying map.
     * @param key A string representing the key.
     * @return An object indexed by the provided key.
     */
    operator fun get(key: String): Any {
        return map.getValue(key)
    }

    /**
     * Puts the object in the map and indexes it in the provided key. This
     * method provides an easy access to the underlying map.
     * @param key A string representing the key.
     * @param value The object to be indexed by the provided key.
     */
    fun put(key: String, value: Any) {
        map[key] = value
    }

    /**
     * Checks if the map contains the provided key. This is actually a wrapper
     * to the private map's method of the same name.
     * @param key The key to be checked.
     * @return A boolean value indicating if the map contains the provided key.
     */
    operator fun contains(key: String): Boolean {
        return map.containsKey(key)
    }
}
