// SPDX-License-Identifier: BSD-3-Clause
package org.islandoftex.arara.utils

import kotlin.math.ceil
import org.islandoftex.arara.files.Project
import org.islandoftex.arara.files.ProjectFile

/**
 * Abbreviate a String to a maximal width.
 *
 * @param maxWidth The maximal width to truncate to.
 * @param ellipsis The string to use to indicate an ellipsis.
 * @throws IllegalArgumentException If the string would consist only of the
 *   ellipsis after shortening.
 * @return The abbreviated string.
 */
@Throws(IllegalArgumentException::class)
fun String.abbreviate(maxWidth: Int, ellipsis: String = "…"): String {
    return when {
        maxWidth < ellipsis.length + 1 ->
            throw IllegalArgumentException("Can't abbreviate text further")
        this.length < maxWidth -> this
        else -> this.substring(0, maxWidth - ellipsis.length) + ellipsis
    }
}

/**
 * Center a string within a specified number of columns.
 *
 * This does not center anything if the string is longer than the specified
 * width.
 *
 * @param width The number of columns.
 * @param padChar The char to pad with.
 * @return The padded string.
 */
fun String.center(width: Int, padChar: Char): String {
    return if (this.length > width) this
    else {
        val charsLeft = width - this.length
        padChar.toString().repeat(charsLeft / 2) + this +
                padChar.toString().repeat(ceil(charsLeft.toDouble() / 2.0).toInt())
    }
}

/**
 * Wrap text at a specified width.
 *
 * Algorithm from Wikipedia:
 * https://en.wikipedia.org/wiki/Line_wrap_and_word_wrap#Minimum_number_of_lines
 *
 * @param width The width to wrap at.
 * @return Wrapped text.
 */
fun String.wrap(width: Int): String {
    val words = this.split(" ")
    var wrapped = words[0]
    var spaceLeft = width - wrapped.length
    words.drop(1).forEach {
        val len = it.length
        wrapped += if (len + 1 > spaceLeft) {
            spaceLeft = width - len
            "\n$it"
        } else {
            spaceLeft -= len + 1
            " $it"
        }
    }
    return wrapped
}

/**
 * Get the files of a project as absolute paths.
 */
val Project.absoluteFiles: Set<ProjectFile>
    get() = files.map {
        if (it.isAbsolute)
            it
        else
            it.copy(path = workingDirectory.resolve(it).toRealPath())
    }.toSet()

/**
 * Get the project's files in order of compilation.
 */
val Project.filesByPriority: List<ProjectFile>
    get() = files.sortedBy {
        it.priority
    }
