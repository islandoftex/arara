// SPDX-License-Identifier: BSD-3-Clause
package org.islandoftex.arara.core.localization

/** Typed messages for directive messages. */
interface DirectiveMessages {
    /**
     * The error shown when the file list of a directive is empty.
     */
    fun errorValidateEmptyFilesList(lines: String): String

    /**
     * The error shown when the file list of a directive is not a list.
     */
    fun errorValidateFilesIsNotAList(lines: String): String

    /**
     * The error shown when a directive is malformed.
     */
    fun errorValidateInvalidDirectiveFormat(lines: String): String

    /**
     * The error shown when a file contains no directives at all.
     */
    val errorValidateNoDirectivesFound: String

    /**
     * The error shown when a directive line break has no continuation.
     */
    fun errorValidateOrphanLinebreak(line: String): String

    /**
     * The error shown when a directive uses the reserved reference key.
     */
    fun errorValidateReferenceIsReserved(lines: String): String

    /**
     * The error shown when the YAML map of a directive cannot be
     * processed.
     */
    fun errorValidateYamlException(lines: String): String
}
