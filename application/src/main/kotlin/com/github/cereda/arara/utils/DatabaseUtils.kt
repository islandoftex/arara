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

import com.github.cereda.arara.configuration.Configuration
import com.github.cereda.arara.localization.LanguageController
import com.github.cereda.arara.model.AraraException
import com.github.cereda.arara.model.Database
import com.github.cereda.arara.localization.Messages
import org.yaml.snakeyaml.Yaml
import org.yaml.snakeyaml.constructor.Constructor
import org.yaml.snakeyaml.nodes.Tag
import org.yaml.snakeyaml.representer.Representer
import java.io.File

/**
 * Implements database utilitary methods.
 *
 * @author Paulo Roberto Massa Cereda
 * @version 4.0
 * @since 4.0
 */
object DatabaseUtils {
    // the application messages obtained from the
    // language controller
    private val messages = LanguageController

    /**
     * Gets the path to the YAML file representing the database as string.
     *
     * @throws AraraException Something wrong happened, to be caught in the
     * higher levels.
     */
    private val path: String
        @Throws(AraraException::class)
        get() {
            val name = Configuration["execution.database.name"] as String + ".yaml"
            val path = CommonUtils.getParentCanonicalPath(reference)
            return CommonUtils.buildPath(path, name)
        }

    /**
     * Gets the main file reference.
     *
     * @return The main file reference.
     */
    private val reference: File
        get() = Configuration["execution.reference"] as File

    /**
     * Loads the YAML file representing the database.
     *
     * @return The database object.
     * @throws AraraException Something wrong happened, to be caught in the
     * higher levels.
     */
    @Throws(AraraException::class)
    fun load(): Database {
        return if (!exists()) {
            Database()
        } else {
            val file = File(path)
            try {
                val representer = Representer()
                representer.addClassTag(Database::class.java, Tag("!database"))
                Yaml(Constructor(Database::class.java), representer)
                        .loadAs(file.readText(), Database::class.java)
            } catch (exception: Exception) {
                throw AraraException(
                        messages.getMessage(
                                Messages.ERROR_LOAD_COULD_NOT_LOAD_XML,
                                file.name
                        ),
                        exception
                )
            }

        }
    }

    /**
     * Saves the database on a YAML file.
     *
     * @param database The database object.
     * @throws AraraException Something wrong happened, to be caught in the
     * higher levels.
     */
    @Throws(AraraException::class)
    fun save(database: Database) {
        val file = File(path)
        try {
            val representer = Representer()
            representer.addClassTag(Database::class.java, Tag("!database"))
            file.writeText(
                    Yaml(Constructor(Database::class.java), representer)
                            .dump(database))
        } catch (exception: Exception) {
            throw AraraException(
                    messages.getMessage(
                            Messages.ERROR_SAVE_COULD_NOT_SAVE_XML,
                            file.name
                    ),
                    exception
            )
        }

    }

    /**
     * Checks if the YAML file representing the database exists.
     *
     * @return A boolean value indicating if the YAML file exists.
     * @throws AraraException Something wrong happened, to be caught in the
     * higher levels.
     */
    @Throws(AraraException::class)
    private fun exists(): Boolean {
        return File(path).exists()
    }

}
