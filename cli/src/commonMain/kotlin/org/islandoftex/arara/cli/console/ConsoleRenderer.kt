// SPDX-License-Identifier: BSD-3-Clause
package org.islandoftex.arara.cli.console

import org.islandoftex.arara.api.files.ProjectFile
import org.islandoftex.arara.api.rules.DirectiveConditional

/** Terminal presentation only; logging belongs to the display facade. */
@Suppress("TooManyFunctions")
interface ConsoleRenderer {
    fun printBanner()
    fun printEntry(name: String, task: String)
    fun printEntryResult(value: Boolean)
    fun printException(text: String, details: String?)
    fun printWrapped(text: String)
    fun printAuthors(authors: List<String>)
    fun printConditional(conditional: DirectiveConditional)
    fun printTime(seconds: Double)
    fun printFileInformation(file: ProjectFile)
    fun displaySeparator(): String
    fun displayOutputSeparator(message: String): String
}
