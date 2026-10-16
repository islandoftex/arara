// SPDX-License-Identifier: BSD-3-Clause
package org.islandoftex.arara.core.configuration

/**
 * Holds the preamble configuration the local configuration files
 * accumulate over a run: the named preambles they define, the
 * default one to apply when nothing else asks for a name, and
 * the policy deciding where the default applies. A preamble is
 * plain directive text evaluated by whichever engine runs it, so
 * the state is engine-independent and lives with the
 * configuration model rather than with any rule dialect.
 */
object PreambleState {
    /**
     * The named directive lists the configuration files provide,
     * mapped from preamble name to its directives.
     */
    val preambles: MutableMap<String, String> = mutableMapOf()

    /**
     * The preamble applied when nothing else asks for one by
     * name.
     */
    var defaultPreamble: String? = null

    /**
     * Whether the default preamble is applied to all files, the
     * historical behaviour, or only to the files that carry no
     * directives of their own.
     */
    var prependPreambleIfDirectivesGiven: Boolean = true
}
