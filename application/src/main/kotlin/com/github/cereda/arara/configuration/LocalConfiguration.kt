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

import com.github.cereda.arara.Arara
import com.github.cereda.arara.model.FileTypeResource
import com.github.cereda.arara.utils.CommonUtils
import org.mvel2.templates.TemplateRuntime

/**
 * A local configuration which resembles configuration files in the working
 * directory.
 *
 * @author Paulo Roberto Massa Cereda
 * @version 4.0
 * @since 4.0
 */
class LocalConfiguration {
    // rule paths
    var paths: List<String> = listOf()
        get() {
            val user = mutableMapOf<String, Any>(
                    "home" to (CommonUtils.getSystemPropertyOrNull("user.home")
                            ?: ""),
                    "dir" to (CommonUtils.getSystemPropertyOrNull("user.dir")
                            ?: ""),
                    "name" to (CommonUtils.getSystemPropertyOrNull("user.name")
                            ?: ""))
            val map = mutableMapOf<String, Any>("user" to user)

            // TODO: do we call this often?
            return field.map { input ->
                var path = CommonUtils.removeKeywordNotNull(input)
                try {
                    path = TemplateRuntime.eval(path, map) as String
                } catch (_: RuntimeException) {
                    // do nothing, gracefully fallback to
                    // the default, unparsed path
                }
                path
            }
        }

    // file types
    var filetypes: List<FileTypeResource> = listOf()

    // the application language
    // default to English
    var language: String = Arara.config[AraraSpec.Application
            .defaultLanguageCode]
        get() = CommonUtils.removeKeywordNotNull(field)

    // maximum number of loops
    var loops: Int = Arara.config[AraraSpec.Execution.maxLoops]

    // verbose flag
    var isVerbose: Boolean = Arara.config[AraraSpec.Execution.verbose]

    // logging flag
    var isLogging: Boolean = Arara.config[AraraSpec.Execution.logging]

    // header flag
    var isHeader: Boolean = Arara.config[AraraSpec.Execution.header]

    // database name
    var dbname: String = Arara.config[AraraSpec.Execution.databaseName]
        get() = CommonUtils.removeKeywordNotNull(field)

    // log name
    var logname: String = Arara.config[AraraSpec.Execution.logName]
        get() = CommonUtils.removeKeywordNotNull(field)

    // map of preambles
    var preambles: Map<String, String> = Arara.config[AraraSpec.Execution
            .preambles]

    // look and feel
    // default to none
    var laf: String = Arara.config[AraraSpec.UserInteraction.lookAndFeel]
        get() = CommonUtils.removeKeywordNotNull(field)
}
