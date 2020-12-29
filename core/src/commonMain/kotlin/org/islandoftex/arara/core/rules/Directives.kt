// SPDX-License-Identifier: BSD-3-Clause
package org.islandoftex.arara.core.rules

import org.islandoftex.arara.api.files.FileType
import org.islandoftex.arara.api.rules.Directive

/**
 * Implements directive auxiliary methods.
 */
expect object Directives {
    var hooks: DirectiveFetchingHooks

    /**
     * Extracts a list of directives from a list of strings. Might be empty.
     *
     * @param lines List of strings.
     * @param parseOnlyHeader Whether to parse only the header.
     * @param fileType The file type of the file to investigate.
     * @return A list of directives.
     */
    fun extractDirectives(
        lines: List<String>,
        parseOnlyHeader: Boolean,
        fileType: FileType
    ): List<Directive>

    /**
     * Replicate a directive for given files.
     *
     * @param holder The list of files.
     * @param parameters The parameters for the directive.
     * @param directive The directive to clone.
     * @return List of cloned directives.
     */
    fun replicateDirective(
        holder: Any,
        parameters: Map<String, Any>,
        directive: Directive
    ): List<Directive>
}
