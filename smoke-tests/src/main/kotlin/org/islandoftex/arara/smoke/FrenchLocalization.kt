// SPDX-License-Identifier: BSD-3-Clause
package org.islandoftex.arara.smoke

import java.nio.file.Files
import org.islandoftex.arara.api.files.MPPPath
import org.islandoftex.arara.api.files.toJVMPath

internal fun checkFrenchLocalization(root: MPPPath, launcher: List<String>, jar: MPPPath) {
    root.resolve("probe.lua").writeText("""
        return { identifier = 'probe', name = 'French probe',
            commands = { { command = function() return true end } } }
    """.trimIndent())
    root.resolve("probe.yaml").writeText("""
        !config
        identifier: probe
        name: French probe
        commands:
          - command: '@{ true }'
        arguments: []
    """.trimIndent())
    val command = launcher + listOf("-Duser.home=$root", "-jar", jar.toString())
    val presentations = listOf("lua", "yaml").flatMap { engine ->
        listOf("plain", "styled").map { console -> engine to console }
    }
    for (format in listOf("lua", "yaml")) {
        val config = root.resolve("arararc.$format")
        config.writeText(if (format == "lua") "return { paths = { cwd }, language = 'fr' }"
            else "!config\npaths: ['.']\nlanguage: fr\n")
        for ((engine, console) in presentations) {
            val result = run(command + listOf("--engine", engine, "--console", console,
                "--log-format", "text", "run.tex"), root)
            check(result.status == 0) { result.output }
            check("SUCCÈS" in result.output) { result.output }
            if (console == "styled") check("Bilan de l’exécution" in result.output) { result.output }
            if (engine == "yaml") check("Avertissement de compatibilité" in result.output) { result.output }
            check("Run completed with exit code 0" in root.resolve("arara.log").readText())
        }
        Files.delete(config.toJVMPath())
    }
    root.resolve("arararc.yaml").writeText("!config\npaths: ['.']\n")
    val selected = run(command + listOf("--language", "fr", "run.tex"), root)
    check(selected.status == 0 && "SUCCÈS TOTAL" in selected.output) { selected.output }
    root.resolve("arararc.lua").writeText("return { verbose = 'true' }")
    val invalid = run(command + listOf("--language", "fr", "run.tex"), root)
    check(invalid.status == 2 && "verbose doit être un booléen." in invalid.output) { invalid.output }
}
