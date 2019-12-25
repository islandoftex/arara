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
package org.islandoftex.arara.filehandling

import org.islandoftex.arara.Arara
import org.islandoftex.arara.configuration.AraraSpec
import org.islandoftex.arara.localization.LanguageController
import org.islandoftex.arara.localization.Messages
import org.islandoftex.arara.model.AraraException
import org.islandoftex.arara.utils.CommonUtils
import java.io.File
import java.io.FileFilter
import java.nio.file.FileSystems

/**
 * Implements file searching utilitary methods.
 * @author Paulo Roberto Massa Cereda
 * @version 4.0
 * @since 4.0
 */
object FileSearchingUtils {
    /**
     * List all files from the provided directory according to the list of
     * extensions. The leading dot must be omitted, unless it is part of the
     * extension.
     * @param directory The provided directory.
     * @param extensions The list of extensions.
     * @param recursive A flag indicating whether the search is recursive.
     * @return A list of files.
     */
    fun listFilesByExtensions(directory: File,
                              extensions: List<String>, recursive: Boolean):
            List<File> = try {
        // return the result of the
        // provided search
        if (recursive)
            directory.walkTopDown().asSequence()
                    .filter { !it.isDirectory }
                    .filter { extensions.contains(it.extension) }
                    .toList()
        else
            directory.listFiles(
                    FileFilter { extensions.contains(it.extension) })!!
                    .toList()
    } catch (_: Exception) {
        // if something bad happens,
        // gracefully fallback to
        // an empty file list
        listOf()
    }

    /**
     * List all files from the provided directory matching the list of file
     * name patterns. Such list can contain wildcards.
     * @param directory The provided directory.
     * @param patterns The list of file name patterns.
     * @param recursive A flag indicating whether the search is recursive.
     * @return A list of files.
     */
    fun listFilesByPatterns(directory: File,
                            patterns: List<String>, recursive: Boolean):
            List<File> = try {
        // return the result of the provided
        // search, with the wildcard filter
        // and a potential recursive search
        val pathMatcher = patterns.map {
            FileSystems.getDefault().getPathMatcher("glob:$it")
        }
        if (recursive)
            directory.walkTopDown().asSequence()
                    .filter { !it.isDirectory }
                    .filter { file ->
                        pathMatcher.any { it.matches(file.toPath().fileName) }
                    }.toList()
        else
            directory.listFiles { file: File ->
                pathMatcher.any { it.matches(file.toPath().fileName) }
            }!!.toList()
    } catch (_: Exception) {
        // if something bad happens,
        // gracefully fallback to
        // an empty file list
        listOf()
    }

    /**
     * Discovers the file through string reference lookup and sets the
     * configuration accordingly.
     *
     * @param reference The string reference.
     * @throws AraraException Something wrong happened, to be caught in the
     * higher levels.
     */
    @Throws(AraraException::class)
    fun discoverFile(reference: String) {
        lookupFile(reference)
                ?: throw AraraException(
                        LanguageController.getMessage(
                                Messages.ERROR_DISCOVERFILE_FILE_NOT_FOUND,
                                reference,
                                CommonUtils.fileTypesList
                        )
                )
    }

    /**
     * Performs a file lookup based on a string reference.
     *
     * @param reference The file reference as a string.
     * @return The file as result of the lookup operation.
     * @throws AraraException Something wrong happened, to be caught in the
     * higher levels.
     */
    @Throws(AraraException::class)
    private fun lookupFile(reference: String): File? {
        val types = Arara.config[AraraSpec.Execution.fileTypes]
        val file = Arara.config[AraraSpec.Execution.workingDirectory]
                .resolve(reference).toFile()
        val name = file.name
        val parent = FileHandlingUtils.getParentCanonicalFile(file)

        // direct search, so we are considering
        // the reference as a complete name
        val testFile = parent.resolve(name)
        if (testFile.exists() && testFile.isFile) {
            types.firstOrNull {
                testFile.toString().endsWith("." + it.extension)
            }?.let {
                Arara.config[AraraSpec.Execution.filePattern] =
                        it.pattern
                Arara.config[AraraSpec.Execution.reference] = testFile
                return testFile
            }
        }

        // indirect search; in this case, we are considering
        // that the file reference has an implicit extension,
        // so we need to add it and look again
        return types.map { it to parent.resolve("$name.${it.extension}") }
                .firstOrNull { it.second.exists() && it.second.isFile }
                ?.let {
                    Arara.config[AraraSpec.Execution.filePattern] =
                            it.first.pattern
                    Arara.config[AraraSpec.Execution.reference] = it.second
                    file
                }
    }
}
