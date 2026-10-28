// SPDX-License-Identifier: BSD-3-Clause
package org.islandoftex.arara.cli.configuration

import com.github.ajalt.clikt.parameters.arguments.help
import com.github.ajalt.clikt.parameters.groups.OptionGroup
import com.github.ajalt.clikt.parameters.options.associate
import com.github.ajalt.clikt.parameters.options.convert
import com.github.ajalt.clikt.parameters.options.default
import com.github.ajalt.clikt.parameters.options.flag
import com.github.ajalt.clikt.parameters.options.help
import com.github.ajalt.clikt.parameters.options.option
import com.github.ajalt.clikt.parameters.types.choice
import com.github.ajalt.clikt.parameters.types.path
import org.islandoftex.arara.api.files.MPPPath
import org.islandoftex.arara.api.files.toMPPPath
import org.islandoftex.arara.api.rules.RuleEngine

/** Parsed invocation options; registration remains owned by the command. */
internal class InvocationFlags : OptionGroup() {
    val engine by option("-e", "--preferred-engine")
        .help("Preferred rule engine: lua (default) or deprecated yaml; falls back to the other format")
        .choice("lua" to RuleEngine.LUA, "yaml" to RuleEngine.YAML).default(RuleEngine.LUA)

    val cleanBuild by option("--clean-build")
        .help("Remove managed project build directories without executing commands")
        .flag()

    val workingDirectory: MPPPath? by option("-d", "--working-directory")
        .help("Set the working directory for all tools")
        .path(mustExist = true, canBeFile = false, mustBeReadable = true)
        .convert { it.toMPPPath() }

    val parameters: Map<String, String> by option("-P", "--call-property")
        .help("Pass parameters to the application to be used within the session.")
        .associate()

    val properties: MPPPath? by option("-F", "--properties-file")
        .help("Pass a properties file to the application to be used within the session.")
        .path(mustExist = true, canBeDir = false, mustBeReadable = true)
        .convert { it.toMPPPath() }
}
