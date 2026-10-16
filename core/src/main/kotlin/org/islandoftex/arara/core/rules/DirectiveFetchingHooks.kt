// SPDX-License-Identifier: BSD-3-Clause
package org.islandoftex.arara.core.rules

import org.islandoftex.arara.api.rules.Directive
import org.islandoftex.arara.api.rules.DirectiveConditional

/**
 * Hooks to allow the customization of the directive creation process,
 * intended to increase the flexibility for creating different implementations
 * of the [Directive] interface.
 */
data class DirectiveFetchingHooks(
    /**
     * Whenever the parser found a potential directive, this will be
     * executed. You may manipulate the *textual* representation of the
     * directive here.
     */
    val processPotentialDirective: (Int, String) -> String = { _, s -> s },
    /**
     * Given the directive's characteristics identifier, parameters,
     * conditional and line numbers choose a directive implementation and
     * create a directive. Please make sure to resolve parameters.
     */
    val buildDirectiveRaw: (String, String?, DirectiveConditional, List<Int>) -> Directive = {
            _, _, _, _ ->
        // this is a developer contract violation (the command line
        // application installs the hooks before extracting directives),
        // not a user error, so it deliberately stays an English
        // exception outside the localized message catalog
        throw IllegalStateException(
            "The directive fetching hooks were not initialized; the " +
                "command line application has to install them before " +
                "running."
        )
    },
    /**
     * Given the directive's characteristics identifier, parameters,
     * conditional and line numbers choose a directive implementation and
     * create a directive. Please make sure to resolve parameters.
     */
    val buildDirective: (String, Map<String, Any>, DirectiveConditional, List<Int>) -> Directive = {
            _, _, _, _ ->
        throw IllegalStateException(
            "The directive fetching hooks were not initialized; the " +
                "command line application has to install them before " +
                "running."
        )
    }
)
