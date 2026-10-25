// SPDX-License-Identifier: BSD-3-Clause
package org.islandoftex.arara.smoke

import java.nio.file.Files
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import org.islandoftex.arara.api.files.MPPPath
import org.islandoftex.arara.api.files.toJVMPath

internal fun checkLegacyRuleWarnings(root: MPPPath, launcher: List<String>, jar: MPPPath) {
    root.resolve("probe.lua").writeText("""
        return { identifier = 'probe', name = 'Lua probe',
            commands = { { command = function() return true end } } }
    """.trimIndent())
    root.resolve("probe.yaml").writeText("""
        !config
        identifier: probe
        name: YAML probe
        commands:
          - command: '@{ true }'
        arguments: []
    """.trimIndent())
    fun launch(flags: List<String>): String {
        val result = run(launcher + listOf("-Duser.home=$root", "-jar", jar.toString(),
            "-L", "en", "--log-format", "json") + flags + "run.tex", root)
        check(result.status == 0) { result.output }
        return result.output
    }
    val warning = "Compatibility warning: you have used deprecated YAML rules"
    val default = launch(emptyList())
    check(warning !in default) { default }
    check("probe.lua" in root.resolve("arara.log").readText())
    root.resolve("run.tex").writeText("% arara: probe\n% arara: probe if false\n")
    for (flags in listOf(emptyList(), listOf("--dry-run"), listOf("--console", "styled"))) {
        val legacy = launch(listOf("--preferred-engine", "yaml") + flags)
        check(legacy.split(warning).size == 2) { legacy }
        val records = root.resolve("arara.log").readLines().map { Json.parseToJsonElement(it).jsonObject }
        val warnings = records.filter { it["event"]?.jsonPrimitive?.content == "rule.deprecated" }
        check(warnings.size == 2)
        check(warnings.all { it.getValue("level").jsonPrimitive.content == "WARN" })
    }
    Files.delete(root.resolve("probe.lua").toJVMPath())
    val fallback = launch(emptyList())
    check(warning in fallback) { fallback }
    val resolved = root.resolve("arara.log").readLines().map { Json.parseToJsonElement(it).jsonObject }
        .filter { it["event"]?.jsonPrimitive?.content == "rule.resolved" }
    check(resolved.isNotEmpty())
    check(resolved.all { it.getValue("data").jsonObject.getValue("engine").jsonPrimitive.content == "yaml" })
    root.resolve("arararc.lua").writeText("return { paths = { cwd }, warn_deprecated_yaml = false }\n")
    check(warning !in launch(listOf("--preferred-engine", "yaml")))
    check("rule.deprecated" !in root.resolve("arara.log").readText())
    Files.delete(root.resolve("arararc.lua").toJVMPath())
    root.resolve("arararc.yaml").writeText("!config\npaths: ['.']\nwarnDeprecatedYaml: false\n")
    check(warning !in launch(listOf("--preferred-engine", "yaml")))
    check("rule.deprecated" !in root.resolve("arara.log").readText())
}
