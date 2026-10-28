// SPDX-License-Identifier: BSD-3-Clause
package org.islandoftex.arara.api.localization

/** Typed messages for directive messages. */
public interface DirectiveMessages {
    /**
     * The error shown when the file list of a directive is empty.
     */
    public fun errorValidateEmptyFilesList(lines: String): String

    /**
     * The error shown when the file list of a directive is not a list.
     */
    public fun errorValidateFilesIsNotAList(lines: String): String

    /**
     * The error shown when a directive is malformed.
     */
    public fun errorValidateInvalidDirectiveFormat(lines: String): String

    /**
     * The error shown when a file contains no directives at all.
     */
    public val errorValidateNoDirectivesFound: String

    /**
     * The error shown when a directive line break has no continuation.
     */
    public fun errorValidateOrphanLinebreak(line: String): String

    /**
     * The error shown when a directive uses the reserved reference key.
     */
    public fun errorValidateReferenceIsReserved(lines: String): String

    /**
     * The error shown when the YAML map of a directive cannot be
     * processed.
     */
    public fun errorValidateYamlException(lines: String): String
}
