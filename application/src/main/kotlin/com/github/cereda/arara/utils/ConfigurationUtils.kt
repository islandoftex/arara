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
package com.github.cereda.arara.utils

import com.github.cereda.arara.Arara
import com.github.cereda.arara.controller.LanguageController
import com.github.cereda.arara.model.AraraException
import com.github.cereda.arara.model.FileType
import com.github.cereda.arara.model.Messages
import com.github.cereda.arara.model.Resource
import org.apache.commons.lang.SystemUtils
import org.yaml.snakeyaml.Yaml
import org.yaml.snakeyaml.constructor.Constructor
import org.yaml.snakeyaml.error.MarkedYAMLException
import org.yaml.snakeyaml.nodes.Tag
import org.yaml.snakeyaml.representer.Representer
import java.io.File
import java.io.FileReader
import java.io.UnsupportedEncodingException
import java.net.URLDecoder

/**
 * Implements configuration utilitary methods.
 *
 * @author Paulo Roberto Massa Cereda
 * @version 4.0
 * @since 4.0
 */
object ConfigurationUtils {

    // the application messages obtained from the
    // language controller
    private val messages = LanguageController

    // list of default file types provided by arara, in order.
    val defaultFileTypes: List<FileType>
        @Throws(AraraException::class)
        get() = listOf(
                FileType("tex"),
                FileType("dtx"),
                FileType("ltx"),
                FileType("drv"),
                FileType("ins")
        )

    // look for configuration files in the user's working directory first
    // if no configuration files are found in the user's working directory,
    // try to look up in a global directory, that is, the user home
    val configFile: File?
        get() {
            val names = listOf(
                    ".araraconfig.yaml",
                    "araraconfig.yaml",
                    ".arararc.yaml",
                    "arararc.yaml"
            )
            for (name in names) {
                val path = CommonUtils.buildPath(SystemUtils.USER_DIR, name)
                val file = File(path)
                if (file.exists()) {
                    return file
                }
            }
            for (name in names) {
                val path = CommonUtils.buildPath(SystemUtils.USER_HOME, name)
                val file = File(path)
                if (file.exists()) {
                    return file
                }
            }
            return null
        }

    /**
     * The canonical absolute application path.
     *
     * @throws AraraException Something wrong happened, to be caught in the
     * higher levels.
     */
    val applicationPath: String
        @Throws(AraraException::class)
        get() {
            try {
                var path = Arara::class.java.protectionDomain.codeSource
                        .location.path
                path = URLDecoder.decode(path, "UTF-8")
                path = File(path).parentFile.path
                return path
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
    fun validateConfiguration(file: File): Resource {
        val representer = Representer()
        representer.addClassTag(Resource::class.java, Tag("!config"))
        val yaml = Yaml(Constructor(Resource::class.java), representer)
        try {
            val resource = yaml.loadAs(FileReader(file),
                    Resource::class.java)
            if (resource.filetypes != null) {
                val filetypes = resource.filetypes
                if (filetypes.any { it.extension == null }) {
                    throw AraraException(
                            messages.getMessage(
                                    Messages.ERROR_CONFIGURATION_FILETYPE_MISSING_EXTENSION
                            )
                    )
                }
            }
            return resource
        } catch (yamlException: MarkedYAMLException) {
            throw AraraException(
                    messages.getMessage(
                            Messages.ERROR_CONFIGURATION_INVALID_YAML
                    ),
                    yamlException
            )
        } catch (exception: Exception) {
            throw AraraException(
                    messages.getMessage(
                            Messages.ERROR_CONFIGURATION_GENERIC_ERROR
                    )
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
    fun normalizePaths(paths: List<String>): List<String> {
        val pathlist = paths.toMutableList()
        pathlist.add(CommonUtils.buildPath(applicationPath, "rules"))
        return pathlist.distinct()
    }

    /**
     * Normalize a list of file types, removing all duplicates.
     *
     * @param types The list of file types.
     * @return A list of normalized file types, without duplicates.
     * @throws AraraException Something wrong happened, to be caught in the
     * higher levels.
     */
    @Throws(AraraException::class)
    fun normalizeFileTypes(types: List<FileType>): List<FileType> {
        val typelist = types.toMutableList()
        typelist.addAll(defaultFileTypes)
        return typelist.distinct()
    }

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
