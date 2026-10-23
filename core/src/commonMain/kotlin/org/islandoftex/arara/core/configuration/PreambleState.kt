// SPDX-License-Identifier: BSD-3-Clause
package org.islandoftex.arara.core.configuration

/** Preambles shared by both rule engines; the CLI restores them between projects and invocations. */
object PreambleState {
    /**
     * Save the available preambles from the configuration file.
     */
    val preambles: MutableMap<String, String> = mutableMapOf()

    /**
     * Specify the default preamble to pick if there is neither a preamble
     * nor directives specified.
     */
    var defaultPreamble: String? = null

    /**
     * Whether to prepend preamble directives even if there are directives
     * in the document. Useful for enabling preambles only for documents
     * without preambles.
     *
     * Set to `true` for reasons of backward compatibility.
     */
    var prependPreambleIfDirectivesGiven: Boolean = true
}
